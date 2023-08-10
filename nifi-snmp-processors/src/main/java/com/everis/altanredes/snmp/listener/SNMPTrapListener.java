package com.everis.altanredes.snmp.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.apache.log4j.Logger;
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

//import com.everis.altanredes.snmp.kafka.KafkaJsonSerializer;
//import com.everis.altanredes.snmp.kafka.conf.ConfKafka;
import com.everis.altanredes.snmp.models.Event;
import com.everis.altanredes.snmp.utils.MIBUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.percederberg.mibble.MibLoaderException;


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
		
	    log.error("processPdu Start" + crEvent.getPDU().toString());
	  
	    log.debug("Iniciando procesamiento de traps....");
		
	    Map<PropertyDescriptor, String> processorProperties = context.getProperties();
	    Map<String, String> generatedAttributes = new HashMap<String, String>();
	    for (final Map.Entry<PropertyDescriptor, String> entry : processorProperties.entrySet()) {
	        PropertyDescriptor property = entry.getKey();
	        if (property.isDynamic() && property.isExpressionLanguageSupported()) {
	            String dynamicValue = context.getProperty(property).evaluateAttributeExpressions().getValue();
	            generatedAttributes.put(property.getName(), dynamicValue);
	        }
	    }
       
	  	final ProcessSession session = sessionFactory.createSession();
	  	
	    try {
	  	
	      log.error("processPdu Start 1");
	      
	      FlowFile flowFile = session.create();
	      
	      log.error("processPdu Start 2");
	      
	      flowFile = session.write(flowFile, new OutputStreamCallback() {
	          @Override
	          public void process(final OutputStream out) throws IOException {
	        	  processPduInternal(out, crEvent);
	          }
	      });
	
	      log.error("processPdu Start 3");
	      
	      flowFile = session.putAllAttributes(flowFile, generatedAttributes);
	      
	      log.error("Prueba manual 3");
	
	      session.getProvenanceReporter().create(flowFile);
	      
	      log.error("Prueba manual 3");
	      
	      session.transfer(flowFile, outputRelationship);		
	      
	      log.error("Prueba manual 4");
	      
	      session.commit();
	      
	      log.error("Final Commit");
	      
	      
	    } catch (final Throwable t) {
	    	log.error("ROLLBACK");
	        session.rollback(true);
	        throw t;
	    }
	      
	      log.error("processPdu End");
	      
	}
	
      
  	public void processPduInternal(OutputStream out, CommandResponderEvent crEvent) {
  		
  		log.error("processPduInternal Start");
  	
		log.debug("Iniciando procesamiento de traps....");
		
		PDU pdu = crEvent.getPDU();

		if (pdu.getType() == PDU.V1TRAP) {

			PDUv1 pduV1 = (PDUv1) pdu;

			log.error("===== NEW SNMP 1 TRAP RECEIVED ====");
			log.error("agentAddr " + pduV1.getAgentAddress().toString());
			log.error("enterprise " + pduV1.getEnterprise().toString());
			log.error("timeStamp " + pduV1.getTimestamp());
			log.error("genericTrap " + pduV1.getGenericTrap());
			log.error("specificTrap " + pduV1.getSpecificTrap());
			log.error("snmpVersion " + PDU.V1TRAP);
			log.error("communityString " + new String(crEvent.getSecurityName()));

		} else if (pdu.getType() == PDU.TRAP) {

			log.error("===== NEW SNMP 2/3 TRAP RECEIVED ====");

			log.error("errorStatus " + pdu.getErrorStatus());
			log.error("errorIndex " + pdu.getErrorIndex());
			log.error("requestID " + pdu.getRequestID());
			log.error("snmpVersion " + PDU.TRAP);
			log.error("communityString " + new String(crEvent.getSecurityName()));

		}

		List<? extends VariableBinding> varBinds = pdu.getVariableBindings();
		if (varBinds != null && !varBinds.isEmpty()) {
			
			log.error("processPduInternal Start VARBINDS");
			
			Event event = new Event();
			events = new ArrayList<>();
			for (VariableBinding vb : varBinds) {

				
				log.error("processPduInternal Start VARBINDS 1");
				
				String syntaxstr = vb.getVariable().getSyntaxString();
				log.error("------");
				log.error("OID: " + vb.getOid().toString());
				log.error("Name: " + oids.getOids().get(vb.getOid().toString()));
				log.error("Type: " + syntaxstr);
				log.error("Value: " + vb.getVariable());
				
				 event.putProperty(oids.getOids().get(vb.getOid().toString()), vb.getVariable().toString());
                	
				
                 if (oids.getOids().get(vb.getOid().toString()).equals("probableCause")) {
					event.setProbableCause(vb.getVariable().toString());
				} 
					else if (oids.getOids().get(vb.getOid().toString()).equals("eventTime")) 
					{
						event.setAlarmRaisedTime(dateToEpoch(vb.getVariable().toString()));
						event.setSourceSystem("OMS");
						event.setVendorName("Nokia");
					    event.setNetworkType("TX");
					}
					else if (oids.getOids().get(vb.getOid().toString()).equals("eventType")) 
					{
						event.setAlarmTypeName(vb.getVariable().toString());
					}
				//Update 12/12 id and name 
					else if (oids.getOids().get(vb.getOid().toString()).equals("friendlyName")) 
					{
						event.setAlarmedObjectId(vb.getVariable().toString());
						String string = (vb.getVariable().toString());
						String[] parts = string.split("/");
						String part1 = parts[0]; 
						String part2 = parts[1]; 
						event.setalarmedObjectName(part1);
						event.setAlarmDetail(part2);
						/*String[] pts = part1.split("-");
						String pt1 = pts[0];
						String pt2 = pts[1];
						String pt3 = pts[2];
						
						event.setaltan_id(pt1);*/
					}
				
					else if (oids.getOids().get(vb.getOid().toString()).equals("currentAlarmId")) 
					{
						event.setExternalAlarmId(vb.getVariable().toString());
						
					}
				   
				
					else if (oids.getOids().get(vb.getOid().toString()).equals("acknowledgementStatus")) 
					{
						event.setAckState(vb.getVariable().toString());
					}
				
					else if (oids.getOids().get(vb.getOid().toString()).equals("additionalText")) 
					{
						event.setSpecificProblem(vb.getVariable().toString());
						
					}
				
			     // IMO Test 1
				
					else if (oids.getOids().get(vb.getOid().toString()).equals("perceivedSeverity")) 
					{
						
						switch((vb.getVariable().toString())) {
						case "cleared":
							event.setperceivedSeverity("6");
							event.seteventType("x5");
						    break;
						case "warning":
							event.setperceivedSeverity("5");
							event.seteventType("x1");
							break;
						case "minor":
							event.setperceivedSeverity("4");
							event.seteventType("x1");
							break;
						case "major":
							event.setperceivedSeverity("3");
							event.seteventType("x1");
							break;
						case "critical":
							event.setperceivedSeverity("2");
							event.seteventType("x1");
							break;
						case "indeterminate":
							event.setperceivedSeverity("1");
							event.seteventType("x1");
							break;
						}
					}
						
				 // IMO Test 1 
			}
			
			log.error("processPduInternal Start VARBINDS 2");
			
			if(event.getperceivedSeverity() == null)
			{
				event.setperceivedSeverity("6");
				event.seteventType("x5");
			}
			
			log.error("processPduInternal Start VARBINDS 3");
			
			events.add(event);
			
			
			log.error("processPduInternal Start VARBINDS 4");
			
			try {
				log.error("processPduInternal Start VARBINDS 5");
				final byte[] data = serialize(event);
				final byte[] local = Arrays.copyOf(data, data.length);
				log.error("About to write " + local.length + " bytes:" +  local.toString());
				out.write(local);
				log.error("processPduInternal Start VARBINDS 6");
			} catch (IOException e) {
				log.error("processPduInternal Start VARBINDS 6 error");
				log.error("SNMPTrapListener processPduInternal Exception:" + e.getMessage());
			}
			
		}
		log.debug("Termina procesamiento de trap");
		
		log.error("processPduInternal End");

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
