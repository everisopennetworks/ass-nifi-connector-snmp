package com.everis.altanredes.snmp.listener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
//import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.everis.altanredes.snmp.receiver.SNMPTrapReceiver;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * @author ccerrillo
 *
 */
public class MibOids implements Serializable {
	
	
	private static final Logger log = LoggerFactory.getLogger(MibOids.class);

	private static final long serialVersionUID = 1L;
	//private static final Logger log = Logger.getLogger(MibOids.class);
	private Map<String, String> oids;
	private String mibFilePath;
		
	public Map<String, String> getOids() {
		return oids;
	}

	public void setOids(Map<String, String> oids) {
		this.oids = oids;
	}

	public String getMibFilePath() {
		return mibFilePath;
	}

	public void setMibFilePath(String mibFilePath) {
		this.mibFilePath = mibFilePath;
	}

	public void init() {
		log.error("SNMPProcessor MibOids init Start");
		try {
			this.oids = extractOids(mibFilePath);
		} catch (IOException | MibLoaderException e) {
			log.error("SNMPProcessor MibOids Exception:" + e.getMessage());
		}
		log.error("SNMPProcessor MibOids init End");
	}
	
	
	/**
	 * Realiza al carga de OIDS desde el mibFile indicado.
	 * @param filePath Ruta donde se ubica el archivo MIB
	 * @return Map Mapa con los OID y sus descripciones
	 * @throws IOException
	 * @throws MibLoaderException
	 */
    @SuppressWarnings("unchecked")
	private Map<String, String> extractOids(String filePath)
            throws IOException, MibLoaderException {
    	
    	log.error("SNMPProcessor extractOids string init Start");
    	
    	final Map<String, String> map = new TreeMap<>();
    	String [] path = filePath.split(","); 
    	
    	for (String string : path) {
    		log.debug("Inciando carga de OIDS desde mibfile" + filePath);
            File f = new File(filePath);
            MibLoader loader = new MibLoader();
            loader.addDir(f.getParentFile());
            Mib mib =  loader.load(f);

            
            Stream<MibSymbol> allSymbols = mib.getAllSymbols().stream().map(MibSymbol.class::cast);

            allSymbols.forEach(symbol -> {
            	
                ObjectIdentifierValue oid = extractOid(symbol);
                if (oid != null)
                    map.put(oid.toString(), symbol.getName());
            });
		}
    	
    	log.error("SNMPProcessor extractOids string init End");
    	       
        return map;
    }

    private ObjectIdentifierValue extractOid(MibSymbol symbol) {
    	
    	log.error("SNMPProcessor extractOids symbol Start");
    	
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                return (ObjectIdentifierValue) value;
            }
        }
        
        log.error("SNMPProcessor extractOids symbol end");
        
        return null;
    }

}