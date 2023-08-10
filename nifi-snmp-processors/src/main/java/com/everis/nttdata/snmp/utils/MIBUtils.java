package com.everis.nttdata.snmp.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;

public class MIBUtils {

	private static final Logger log = LoggerFactory.getLogger(MIBUtils.class);
	
	/**
	 * Realiza al carga de OIDS desde el mibFile indicado.
	 * @param filePath Ruta donde se ubica el archivo MIB
	 * @return Map Mapa con los OID y sus descripciones
	 * @throws IOException
	 * @throws MibLoaderException
	 */
    @SuppressWarnings("unchecked")
	public static Map<String, String> extractOids(String filePath)
            throws IOException, MibLoaderException {
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
    	       
        return map;
    }

    private static ObjectIdentifierValue extractOid(MibSymbol symbol) {
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                return (ObjectIdentifierValue) value;
            }
        }
        return null;
    }
}

