package com.everis.altanredes.snmp.models;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Event implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String altan_id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long alarmTypeId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String alarmTypeName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String specificProblem;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String alarmedObjectType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String alarmedObjectId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vendorName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String alarmDetail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long alarmRaisedTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long alarmChangedTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long alarmClearedTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sourceSystem;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String externalAlarmId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ackState;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ackUser;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String perceivedSeverity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String serviceAffecting;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String probableCause;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long alarmOSSCreatedTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String clearUser;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String affectedServiceName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String networkType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String eventType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String alarmedObjectName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String severity;
    private Map<String,String> properties;

@JsonIgnore
// TEST    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final static String NOKIA="Nokia";

	public Event(){}
	
	public String getaltan_id() {
        return altan_id;
    }
	
	public String getseverity() {
        return severity;
    }
	
	public String getalarmedObjectName() {
        return alarmedObjectName;
    }
	
	public String geteventType() {
        return eventType;
    }

	public long getAlarmChangedTime() {
        return alarmChangedTime;
    }

    public long getAlarmClearedTime() {
        return alarmClearedTime;
    }

    public long getAlarmRaisedTime() {
        return alarmRaisedTime;
    }

    public long getAlarmTypeId() {
        return alarmTypeId;
    }

    public long getAlarmOSSCreatedTime() {
        return alarmOSSCreatedTime;
    }

    public String getAckState() {
        return ackState;
    }

    public String getAckUser() {
        return ackUser;
    }

    public String getAlarmDetail() {
        return alarmDetail;
    }

    public String getAlarmedObjectId() {
        return alarmedObjectId;
    }

    public String getAffectedServiceName() {
        return affectedServiceName;
    }

    public String getAlarmedObjectType() {
        return alarmedObjectType;
    }

    public String getAlarmTypeName() {
        return alarmTypeName;
    }

    public String getExternalAlarmId() {
        return externalAlarmId;
    }

    public String getperceivedSeverity() {
        return perceivedSeverity;
    }

    public String getClearUser() {
        return clearUser;
    }

    public String getServiceAffecting() {
        return serviceAffecting;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getSpecificProblem() {
        return specificProblem;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setaltan_id(String altan_id) {
        this.altan_id = altan_id;
    }
    
    public void setseverity(String severity) {
        this.severity = severity;
    }
    
    public void setalarmedObjectName(String alarmedObjectName) {
        this.alarmedObjectName = alarmedObjectName;
    }
    
    public void seteventType(String eventType) {
        this.eventType = eventType;
    }
    
    public void setAckState(String ackState) {
        this.ackState = ackState;
    }

    public void setAckUser(String ackUser) {
        this.ackUser = ackUser;
    }

    public void setAffectedServiceName(String affectedServiceName) {
        this.affectedServiceName = affectedServiceName;
    }

    public void setAlarmChangedTime(long alarmChangedTime) {
        this.alarmChangedTime = alarmChangedTime;
    }

    public void setAlarmClearedTime(long alarmClearedTime) {
        this.alarmClearedTime = alarmClearedTime;
    }

    public void setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
    }

    public void setAlarmedObjectId(String alarmedObjectId) {
        this.alarmedObjectId = alarmedObjectId;
    }

    public void setAlarmedObjectType(String alarmedObjectType) {
        this.alarmedObjectType = alarmedObjectType;
    }

    public void setAlarmOSSCreatedTime(long alarmOSSCreatedTime) {
        this.alarmOSSCreatedTime = alarmOSSCreatedTime;
    }

    public void setAlarmRaisedTime(long alarmRaisedTime) {
        this.alarmRaisedTime = alarmRaisedTime;
    }

    public void setAlarmTypeId(long alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
    }

    public void setAlarmTypeName(String alarmTypeName) {
        this.alarmTypeName = alarmTypeName;
    }

    public void setClearUser(String clearUser) {
        this.clearUser = clearUser;
    }

    public void setExternalAlarmId(String externalAlarmId) {
        this.externalAlarmId = externalAlarmId;
    }
    
    public void setperceivedSeverity(String perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
    }

    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    public void setServiceAffecting(String serviceAffecting) {
        this.serviceAffecting = serviceAffecting;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setSpecificProblem(String specificProblem) {
        this.specificProblem = specificProblem;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void putProperty(String key,String value){
        if(properties==null){
            properties=new TreeMap<String, String> ();
        }
        properties.put(key,value);
    }


    public void setValue(String vendor, String variable, long value){
        if(NOKIA.equals(vendor)){
            switch(variable){
                case "i":
                    alarmTypeId=value;
                    break;
                case "b":
                    alarmRaisedTime=value;
                    break;
                case "mm":
                    alarmChangedTime=value;
                    break;
                case "ll":
                    alarmClearedTime=value;
                    break;
                default:
                	
        			break;         
            }
            putProperty(variable,value+"");
        }     
      
    }

    
    
    public void setValue(String vendor, String variable, String value){
        if(NOKIA.equals(vendor)){
            switch(variable){
                case "eventType":
                    alarmTypeName=value;
                    break;
                case "j":
                    specificProblem=value;
                    alarmDetail=value;
                    break;
                case "d":
                    alarmedObjectType=value;
                    break;
                case "w":
                    alarmedObjectId=value;
                    break;
                case "vendorName":
                    vendorName=value;
                    break;
                case "sourceSystem":
                    sourceSystem=value;
                    break;
                case "f":
                    externalAlarmId=value;
                    break;
                case "n":
                    ackState=value;
                    break;
                case "l":
                    ackUser=value;
                    break;
                case "h":
                    perceivedSeverity=value;
                    break;
                case "g":
                    probableCause=value;
                    break;
                case "y":
                    clearUser=value;
                   break;
                default:
                	
        			break;
            }
            putProperty(variable,value);
        }
    }
    /*Actualizacion eventType*/
    public void setperceivedSeverityif(String value){
        if(value.equals("cleared"))
        
        {
            
            putProperty("eventType","x5");
        }     
        
        else {
        		putProperty("eventType","x1");	
        }
          
    }
    /*Actualización eventType*/
    @Override
    public String toString() {
    	ObjectMapper mapper = new ObjectMapper();	
        try {

        return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        Event e = new Event();


        e.setValue(NOKIA,"a","Value");


        System.out.println(e.toString());
    }
}

