package com.producer.app;

public class AppSetting {
	//Config connect snmp
	public static final String SNMP_SECURITY_NAME= "securityName";
	public static final String SNMP_PORT = "snmpPort";
	public static final String SNMP_AUTHENTICATION_PROTOCOL = "authenticationProtocol";
	public static final String SNMP_AUTHENTICATION_PASSPHRASE = "authenticationPassphrase";
	public static final String SNMP_PRIVACY_PROTOCOL = "privacyProtocol";
	public static final String SNMP_PRIVACY_PASSPHRASE = "privacyPassphrase";
	
	//Config OID Alarm
	public static final String NBI_EVENT_TIME= "nbiEventTime";
	public static final String NBI_OBJECT_INSTANCE = "nbiObjectInstance";
	public static final String NBI_ACK_STATE = "nbiAckState";
	public static final String NBI_PERCEIVED_SEVERITY = "nbiPerceivedSeverity";
	public static final String NBI_PROBABLE_CAUSE = "nbiProbableCause";
	public static final String NBI_SPECIFIC_PROBLEM = "nbiSpecificProblem";
	public static final String NBI_ACK_TIME = "nbiAckTime";
	public static final String NBI_ACK_USER = "nbiAckUser";
	public static final String NBI_ADDITIONAL_TEXT = "nbiAdditionalText";
	public static final String NBI_ALARM_ID = "nbiAlarmId";
	public static final String NBI_ALARM_TIME = "nbiAlarmTime";
	public static final String NBI_ALARM_TYPE = "nbiAlarmType";
	public static final String NBI_CLEAR_TIME = "nbiClearTime";
	public static final String NBI_CLEAR_USER = "nbiClearUser";

	// IP
	public static final String IP_ADDRESS = "ipAddress";
	
	//Config OID Cisco
	public static final String CEN_ALARM_TIMESTAMP ="cenAlarmTimestamp";
	public static final String CEN_ALARM_UPDATED_TIMESTAMP = "cenAlarmUpdatedTimestamp";
	public static final String CEN_ALARM_INSTANCE_ID = "cenAlarmInstanceID";
	public static final String CEN_ALARM_STATUS = "cenAlarmStatus";
	public static final String CEN_ALARM_STATUS_DEFINITION = "cenAlarmStatusDefinition";
	public static final String CEN_ALARM_CATEGORY_DEFINITION ="cenAlarmCategoryDefinition";
	public static final String CEN_ALARM_MANAGED_OBJECT_CLASS ="cenAlarmManagedObjectClass";
	public static final String CEN_ALARM_MANAGED_OBJECT_ADDRESS ="cenAlarmManagedObjectAddress";
	public static final String CEN_ALARM_DESCRIPTION ="cenAlarmDescription";
	public static final String CEN_ALARM_SEVERITY_DEFINITION ="cenAlarmSeverityDefinition";
	public static final String CEN_USER_MESSAGE1 ="cenUserMessage1";
	
	//Config list IP gui canh bao
	public static final String ALARM_SNMP_IP_LIST = "ipList";
}

