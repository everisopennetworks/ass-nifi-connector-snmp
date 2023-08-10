package com.everis.nttdata.snmp.receiver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;


import static java.lang.Thread.sleep;


/**
 * Prepara el receptor de traps mediante las propiedades cargadas en la configuracion
 * @author ccerrillo
 *
 */
import java.util.*;

public class SNMPTrapReceiver {

	private static final Logger log = LoggerFactory.getLogger(SNMPTrapReceiver.class);
	

	private int threadPoolSize;
	private String bindAddress;
	private int port;
	private CommandResponder listener;
	private Snmp snmp = null;
	private OctetString engineId = null;
	ThreadPool threadPool = null;
	//private ExecutorService executor;
	private String eid;
	private String uName;
	private String aModel;
	private String pPassword;
	private String pProtocol;
	private String pass;
	private Boolean periodicrestart=false;
	private int freq=60000;

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setListener(CommandResponder listener) {
		this.listener = listener;
	}

	public void setEngineId(String eidval) {
		this.eid = eidval;
	}
	public void setUserName(String uNameval) {
		this.uName = uNameval;
	}
	public void setAuthModel(String aModelval) {
		this.aModel = aModelval;
	}
	public void setPrivacyProtocol(String pProtocolval) {
		this.pProtocol = pProtocolval;
	}
	public void setPrivacyPassword(String pPasswordval) {
		this.pPassword = pPasswordval;
	}
	public void setPassword(String passval) {
		this.pass = passval;
	}
	public void setRestart(Boolean pRestart, int f) {
		this.periodicrestart = pRestart; freq=f;
	}

	private  Timer my_thread;

	/**
	 * Ejecuta el procesamiento de traps
	 */
	public void execute() {
		
		log.info("SNMPTrapReceiver execute Start");
		
		try {
			init();
			snmp.addCommandResponder(listener);
		} catch (Exception ex) {
			log.error("SNMPTrapReceiver execute Exception:" + ex.getMessage());
		}
		
		log.info("SNMPTrapReceiver execute End");

		/*while(periodicrestart){

			log.info("reseteara el engineboot y engine time");
			try{
				snmp.setLocalEngine(engineId.getValue(), 0,0);
				log.info("reseteo el engineboot y engine time");
				//sleep(freq);
				my_thread.wait(freq);
			}
			catch(Exception e){log.debug(e.getMessage());}

		}*/

		if(periodicrestart){
			my_thread=new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					snmp.setLocalEngine(engineId.getValue(), 0,0);
				}
			};
			my_thread.scheduleAtFixedRate(task,0,freq);
		}
	}
	
	public void close() {
		try {


			snmp.close();
			if(my_thread!=null) {
				my_thread.cancel();
				my_thread.purge();
				my_thread=null;
			}

		} catch (IOException e) {
			log.error("SNMPTrapReceiver execute Exception:" + e.getMessage());
		}
	}

	/**
	 * Inicializa el receptor de traps
	 * @throws IOException
	 */
	private void init() throws IOException {
		log.debug("SNMPTrapReceiver init Start");
		threadPool = ThreadPool.create("SNMPProcessor", threadPoolSize);
		MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());
		// TRANSPORT
		Address listenAddress = GenericAddress
				.parse(System.getProperty("snmp4j.listenAddress", "udp:" + bindAddress + "/" + port));
		
		log.debug("SNMPTrapReceiver init ADDRESS " +  listenAddress.toString());
		
		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
			log.debug("SNMPTrapReceiver init UDP TRANSPORT SELECTED");
		} else {
			transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
			log.debug("SNMPTrapReceiver init TCP TRANSPORT SELECTED");
		}

		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		log.debug("will check engineID");
		log.debug("EngineID is "+eid);

		if (eid == null){
			log.debug("empty engineId using dummy values No V3 set");
			eid="dummyengine";
			uName="unameuname";
			pass="passpass";
			pPassword="ppassppass";
			aModel="MD5";
			pProtocol="DES";
		}
		engineId = OctetString.fromHexString(eid);
		log.debug("Lili ---engineID: " + eid);
        /*if(eid.startsWith("0x")){
			engineId = OctetString.fromHexString(eid.substring(2));
			log.debug("entro al if Converted Hex from String: " + engineId);
		}else
		{
			StringBuilder stringBuilder = new StringBuilder();
			char[] charArray = eid.toCharArray();
			for (char c : charArray) {
				String charToHex = Integer.toHexString(c);

				stringBuilder.append(charToHex);
			}

			log.debug("entro al sino Converted Hex from String: " + stringBuilder.toString());
			engineId=OctetString.fromString(stringBuilder.toString(),16);
		}*/

		USM usm = new USM(SecurityProtocols.getInstance(), engineId, 0);
		OID privid;
		OID authid;
		switch (aModel)
		{
			case "MD5":  SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5()); authid=AuthMD5.ID;
				break;
			case "SHA-1":  SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA()); authid=AuthSHA.ID;
				break;
			default: SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5()); authid=AuthMD5.ID;
				break;
		}
		switch (pProtocol)
		{
			case "DES":  SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES()); privid=PrivDES.ID;
				break;
			case "3DES":  SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES()); privid=Priv3DES.ID;
				break;
			case "AES128":  SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128()); privid=PrivAES128.ID;
				break;
			case "AES192":  SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES192()); privid=PrivAES192.ID;
				break;
			case "AES256":  SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256());privid=PrivAES256.ID;
				break;
			default: SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());privid=PrivDES.ID;
				break;
		}



		usm.setEngineDiscoveryEnabled(true);

		SecurityModels.getInstance().addSecurityModel(usm);

		MPv3 v3 = new MPv3(usm);


		snmp.getMessageDispatcher().addMessageProcessingModel(v3);



		UsmUser jnetmanUser = new UsmUser(new OctetString(uName), authid,new OctetString(pass), privid,	new OctetString(pPassword));

        snmp.getUSM().addUser(new OctetString(uName), engineId,jnetmanUser);
		log.debug("Engine ID >> " + snmp.getLocalEngineID());
		log.debug("New USM User added >> " + snmp.getUSM().getUserTable().getUserEntries());
		snmp.listen();
				
		log.info("SNMPTrapReceiver TRANSPORT address: " + transport.getListenAddress().toString() + " object " + transport.toString() + "<");

		log.debug("SNMPTrapReceiver init End");
	}
}
