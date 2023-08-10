package com.everis.altanredes.snmp.receiver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import com.everis.altanredes.snmp.listener.SNMPTrapListener;

/**
 * Prepara el receptor de traps mediante las propiedades cargadas en la configuracion
 * @author ccerrillo
 *
 */
public class SNMPTrapReceiver {

	private static final Logger log = LoggerFactory.getLogger(SNMPTrapReceiver.class);
	
	private String threadPoolName;
	private int threadPoolSize;
	private String bindAddress;
	private int port;
	private CommandResponder listener;
	private Snmp snmp = null;
	ThreadPool threadPool = null;

	public void setThreadPoolName(String threadPoolName) {
		this.threadPoolName = threadPoolName;
	}

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

	/**
	 * Ejecuta el procesamiento de traps
	 */
	public void execute() {
		
		log.error("SNMPTrapReceiver execute Start");
		
		try {
			init();
			snmp.addCommandResponder(listener);
		} catch (Exception ex) {
			log.error("SNMPTrapReceiver execute Exception:" + ex.getMessage());
		}
		
		log.error("SNMPTrapReceiver execute End");
	}
	
	public void close() {
		try {
			snmp.close();
			threadPool.stop();
		} catch (IOException e) {
			log.error("SNMPTrapReceiver execute Exception:" + e.getMessage());
		}
	}

	/**
	 * Inicializa el receptor de traps
	 * @throws IOException
	 */
	private void init() throws IOException {
		
		log.error("SNMPTrapReceiver init Start");
		
		threadPool = ThreadPool.create(threadPoolName, threadPoolSize);
		MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());

		// TRANSPORT
		Address listenAddress = GenericAddress
				.parse(System.getProperty("snmp4j.listenAddress", "udp:" + bindAddress + "/" + port));
		
		log.error("SNMPTrapReceiver init ADDRESS " +  listenAddress.toString());
		
		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
			log.error("SNMPTrapReceiver init UDP TRANSPORT SELECTED");
		} else {
			transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
			log.error("SNMPTrapReceiver init TCP TRANSPORT SELECTED");
		}

		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.listen();
			
		if(transport.isListening())
		{
			log.error("SNMPTrapReceiver TRANSPORT IS LISTENING");
		}
		
		log.error("SNMPTrapReceiver TRANSPORT address: " + transport.getListenAddress().toString() + " object " + transport.toString() + "<");
		
		log.error("SNMPTrapReceiver init End");
	}
}
