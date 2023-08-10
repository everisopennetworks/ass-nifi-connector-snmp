package com.everis.nttdata.snmp.listener;

import java.io.IOException;
import java.io.OutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.VariableBinding;

import com.everis.nttdata.snmp.models.Event;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;

/**
 * Realiza el procesamiento de las PDU recibidas en el trap SNMP
 *
 * @author ccerrillo
 *
 */
public class SNMPTrapListener implements CommandResponder {

	private static final Logger log = LoggerFactory.getLogger(SNMPTrapListener.class);
	private List<Event> events;
	private MibOids oids;
	private String mibfilePath;

	final ProcessContext context;
	final ProcessSessionFactory sessionFactory;
	final Relationship outputRelationship;

	public SNMPTrapListener(final ProcessContext context,final ProcessSessionFactory sessionFactory, Relationship outputRelationship) {
		this.context = context;
		this.sessionFactory = sessionFactory;
		this.outputRelationship = outputRelationship;
	}

	public String getMibfilePath() {
		return mibfilePath;
	}

	public void setMibfilePath(String mibfilePath) {
		this.mibfilePath = mibfilePath;
	}


	public List<Event> getEvents() {
		return events;
	}


	public MibOids getOids() {
		return oids;
	}

	public void setOids(MibOids oids) {

		this.oids = oids;
	}

	/**
	 * Realiza el procesamiento de los PDUs contenidos en el evento
	 */
	@Override
	public void processPdu(CommandResponderEvent crEvent) {

		log.info("a trap arrived...");

	    log.info("processPdu Start" + crEvent.getPDU().toString());

	    log.info("Iniciando procesamiento de traps....");

	    Map<PropertyDescriptor, String> processorProperties = context.getProperties();
	    Map<String, String> generatedAttributes = new HashMap<String, String>();
	    for (final Map.Entry<PropertyDescriptor, String> entry : processorProperties.entrySet()) {

	        PropertyDescriptor property = entry.getKey();
	        if (property.isDynamic() && property.isExpressionLanguageSupported()) {

	            String dynamicValue = context.getProperty(property).evaluateAttributeExpressions().getValue();
	            generatedAttributes.put(property.getName(), dynamicValue);
	        }
	    }

        generatedAttributes.put("TrapSender",crEvent.getPeerAddress().toString());
	  	final ProcessSession session = sessionFactory.createSession();

	    try {

	        FlowFile flowFile = session.create();

	      flowFile = session.write(flowFile, new OutputStreamCallback() {
	          @Override
	          public void process(final OutputStream out) throws IOException {

	        	  processPduInternalv2(out, crEvent, processorProperties);


	          }
	      });


	     flowFile = session.putAllAttributes(flowFile, generatedAttributes);


	     session.getProvenanceReporter().create(flowFile);
 		flowFile = session.putAttribute(flowFile,"Host",crEvent.getPeerAddress().toString());
		flowFile = session.putAttribute(flowFile,"filename","Trap");

	     session.transfer(flowFile, outputRelationship);

	     session.commit();


	     log.info("Process session commit");

	    } catch (final Throwable t) {
	    	log.error(t.getMessage()+" "+t.toString());
	        session.rollback(true);
	        throw t;
	    }

	    log.info("processPdu End");
	}


	public void processPduInternalv2(OutputStream out, CommandResponderEvent crEvent, Map<PropertyDescriptor, String> processorProperties) {
		log.info("Iniciando procesamiento internal v2 de traps...."+crEvent.getPeerAddress().toString());
		PDU pdu = crEvent.getPDU();
		String host="";

		host=crEvent.getPeerAddress().toString();
        log.info("Peer address: "+host );
		if (pdu.getType() == -92) {
			PDUv1 pduV1 = (PDUv1)pdu;

			log.info("===== NEW SNMP 1 TRAP RECEIVED ====");
			log.info("agentAddr " + pduV1.getAgentAddress().toString());
			host=pduV1.getAgentAddress().toString();
			log.info("host updated to "+host);
			log.info("enterprise " + pduV1.getEnterprise().toString());
			log.info("timeStamp " + pduV1.getTimestamp());
			log.info("genericTrap " + pduV1.getGenericTrap());
			log.info("specificTrap " + pduV1.getSpecificTrap());
			log.info("snmpVersion -92");
			log.info("communityString " + new String(crEvent.getSecurityName()));
		} else if (pdu.getType() == -89) {

			log.info("===== NEW SNMP 2/3 TRAP RECEIVED ====");
			log.info("errorStatus " + pdu.getErrorStatus());
			log.info("errorIndex " + pdu.getErrorIndex());
			log.info("requestID " + pdu.getRequestID());
			log.info("snmpVersion -89");
			log.info("communityString " + new String(crEvent.getSecurityName()));
		}

		List<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		if (varBinds != null && !varBinds.isEmpty()) {
			log.info(String.valueOf(varBinds));
			Event event = new Event();

			for (VariableBinding vb : varBinds) {
				try{
					String syntaxstr = vb.getVariable().getSyntaxString();
					log.info("------");
					log.info("OID: " + vb.getOid().toString());
					log.info("Name: " + oids.getOids().get(vb.getOid().toString()));
					log.info("Type: " + syntaxstr);
					log.info("Value: " + vb.getVariable());

					if(oids.getOids().get(vb.getOid().toString())!=null){
						event.putProperty(oids.getOids().get(vb.getOid().toString()), vb.getVariable().toString());
					}else{
						event.putProperty(vb.getOid().toString(), vb.getVariable().toString());
					}
				/* Desde que se inicia el primer if se comienza a recibir los campos que han sido decifrados del Trap que
				   nos a enviado el gesto. Estos campos entraran en una coincidencia especifica para asi poder ser mapeados */

				}catch(Exception e){
					e.printStackTrace();
				}
			}




			try {
				final byte[] data = serialize(event);
				final byte[] local = Arrays.copyOf(data, data.length);
				out.write(local);
			} catch (IOException e) {
				log.error("SNMPTrapListener processPduInternal Exception:" + e.getMessage());
			}

		}
		log.info("Termina procesamiento de trap");
		log.info("processPduInternal End");

	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	public byte[] serialize(Event o) {

		byte[] retVal = null;

		try {
			retVal = objectMapper.writeValueAsBytes(o);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retVal;
	}

	private static final long dateToEpoch(String fecha) {
		long result = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date dt = sdf.parse(fecha);
			result = dt.getTime();
//			(long)(epoch/1000);
		} catch (ParseException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
}
