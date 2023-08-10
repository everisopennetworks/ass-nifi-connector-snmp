/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.everis.snmp.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.TriggerSerially;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractSessionFactoryProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.everis.nttdata.snmp.listener.MibOids;
import com.everis.nttdata.snmp.listener.SNMPTrapListener;
import com.everis.nttdata.snmp.receiver.SNMPTrapReceiver;

@InputRequirement(Requirement.INPUT_FORBIDDEN)
@Tags({"process", "source", "external", "listener", "snmp"})
@CapabilityDescription("Listens to SNMP messages and writes the information received to a FlowFile,")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
@TriggerSerially
public class SNMPProcessor extends AbstractSessionFactoryProcessor {

	private final Logger logger = LoggerFactory.getLogger(SNMPProcessor.class);



	private final String currentVersion = "v.0.051";

    static Pattern patval = Pattern.compile(".*");
		
    public static final PropertyDescriptor MIB_FILE_PATH = new PropertyDescriptor
            .Builder().name("MIBFilePath")
            .displayName("MIB file path")
            .description("SNMP MIB file path")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor UDP_HOST_LISTENER = new PropertyDescriptor
            .Builder().name("UDPHostListener")
            .displayName("UDP Host listener")
            .defaultValue("0.0.0.0")
            .description("IP to listen to traps specify 0.0.0.0 for all interfaces")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    public static final PropertyDescriptor UDP_PORT_LISTENER = new PropertyDescriptor
            .Builder().name("UDPPortListener")
            .displayName("UDP port listener")
            .description("UDP port for SNMP messages")
            .defaultValue("162")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .addValidator(StandardValidators.PORT_VALIDATOR)
            .build();

    public static final PropertyDescriptor ENGINE_ID = new PropertyDescriptor
            .Builder().name("engineID")
            .displayName("Engine ID (SNMP v3)")
            .description("Engine ID (SNMP v3)")
            .required(false)
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .build();

    public static final PropertyDescriptor username = new PropertyDescriptor
            .Builder().name("username")
            .displayName("userName (SNMP v3)")
            .description("userName (SNMP v3)")
            .required(false)
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .build();

    public static final PropertyDescriptor password = new PropertyDescriptor
            .Builder().name("password")
            .displayName("password (SNMP v3)")
            .description("password (SNMP v3)")
            .required(false)
            .sensitive(true)
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .build();

    public static final PropertyDescriptor authModel = new PropertyDescriptor
            .Builder().name("authModel")
            .displayName("Authentication Model (SNMP v3)")
            .description("Protocol of encryption for authentication (SNMP v3)")
            .allowableValues("MD5","SHA-1")
            .required(false)
            .build();

    public static final PropertyDescriptor privProtocol = new PropertyDescriptor
            .Builder().name("privProtocol")
            .displayName("Privacy Protocol(SNMP v3)")
            .description("Protocol of encryption for privacy (SNMP v3)")
            .allowableValues("DES","3DES","AES128","AES192","AES256")
            .required(false)
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .build();

    public static final PropertyDescriptor privPassword = new PropertyDescriptor
            .Builder().name("privPassword")
            .displayName("Privacy Password (SNMP v3)")
            .description("Privacy password (SNMP v3)")
            .required(false)
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .sensitive(true)
            .build();

    public static final PropertyDescriptor perRestart = new PropertyDescriptor
            .Builder().name("perRestart")
            .displayName("engineBoot periodic refresh")
            .description("In case sender always send a fixed value of engineBoot and engineTime. Not recommended.")
            .required(false)
            .defaultValue("false")
            .allowableValues("true","false")
            .addValidator(StandardValidators.createRegexMatchingValidator(patval))
            .build();

    public static final PropertyDescriptor frequency = new PropertyDescriptor
            .Builder().name("frequency")
            .displayName("only used if needed engineBoot periodic refresh")
            .description("number of milliseconds to refresh the engineId and engineTime")
            .required(false)
            .addValidator(StandardValidators.INTEGER_VALIDATOR)
            .build();



    public static final Relationship SNMP_EVENTS = new Relationship.Builder()
            .name("success")
            .description("SNMP Events are routed to this relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;
    
    private AtomicBoolean startedListener = new AtomicBoolean(false);

    @Override
    protected void init(final ProcessorInitializationContext context) {

        logger.info("SNMPProcessor startup. Lili " + currentVersion);
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        
        descriptors.add(MIB_FILE_PATH);
        descriptors.add(UDP_PORT_LISTENER);
        descriptors.add(UDP_HOST_LISTENER);
        descriptors.add(ENGINE_ID);
        descriptors.add(username);
        descriptors.add(authModel);
        descriptors.add(privProtocol);
        descriptors.add(privPassword);
        descriptors.add(password);
        descriptors.add(perRestart);
        descriptors.add(frequency);


        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        
        relationships.add(SNMP_EVENTS);
        
        this.relationships = Collections.unmodifiableSet(relationships);
           
        logger.debug("SNMPProcessor startup. End"  + currentVersion);
                
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {  	  	
    	logger.debug("SNMPProcessor Scheduled. " + currentVersion);	      
    }
    
    @OnUnscheduled
    public void shutdownExecutor() {
        try{
            logger.debug("SNMPProcessor Shutdown. " + currentVersion);
            receiver.close();
            startedListener.set(false);
            logger.debug("SNMPProcessor Shutdown. End" + currentVersion);

        } catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @OnStopped
    public void alternateshutdown() {
       shutdownExecutor();
    }

    @OnShutdown
    public void OnShutdown () {
        shutdownExecutor();
    }


    private volatile MibOids mibs;
    private volatile SNMPTrapListener listener;
    private volatile SNMPTrapReceiver receiver;
    
    @Override
    public final void onTrigger(final ProcessContext context, final ProcessSessionFactory sessionFactory)
    {
    	if (!startedListener.get()) {
    		
    		logger.info("SNMPProcessor OnTrigger" + currentVersion);

    						
    		mibs = new MibOids();	
    		mibs.setMibFilePath(context.getProperty(MIB_FILE_PATH).getValue());
    		mibs.init();
            
            listener = new SNMPTrapListener(context, sessionFactory, SNMP_EVENTS);
                listener.setOids(mibs);
                     
            receiver = new SNMPTrapReceiver();
            receiver.setThreadPoolSize(context.getMaxConcurrentTasks());
            receiver.setBindAddress(context.getProperty(UDP_HOST_LISTENER).getValue());
            receiver.setPort(Integer.parseInt(context.getProperty(UDP_PORT_LISTENER).getValue()));
            receiver.setEngineId(context.getProperty(ENGINE_ID).getValue());
            receiver.setUserName(context.getProperty(username).getValue());
            receiver.setAuthModel(context.getProperty(authModel).getValue());
            receiver.setPrivacyProtocol(context.getProperty(privProtocol).getValue());
            receiver.setPrivacyPassword(context.getProperty(privPassword).getValue());
            receiver.setPassword(context.getProperty(password).getValue());
            if(context.getProperty(perRestart).asBoolean()){
                receiver.setRestart(context.getProperty(perRestart).asBoolean(),context.getProperty(frequency).asInteger());
            }
            receiver.setListener(listener);
            receiver.execute();
            startedListener.set(true);
	
	    	logger.debug("SNMPProcessor OnTrigger. End" + currentVersion);	
    	}    	
    }
}
