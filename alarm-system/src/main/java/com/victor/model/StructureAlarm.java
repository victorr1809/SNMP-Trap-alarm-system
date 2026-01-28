package com.victor.model;


public class StructureAlarm {
	public String cellid;
	public String nbiAlarmType;
	public String nbiPerceivedSeverity;
	public String nbiSpecificProblem;
	public String site;
	public String ne;
	public String nbiAdditionalText;
	public String recordType;
	public String nbiAlarmId;
	public String neType;
	public String nbiClearTime;
	public String nbiAlarmTime;
	public String nbiObjectInstance;
	public String alarmMappingId;
	public String alarmMappingName;
	public String isSendSms;
	public String isMonitor;
	public String network;
	public String region;
	public String province;
	public String district;
	public String dept;
	public String team; 
	public String nbiEventTime;
	public String nbiAckState;
	public String nbiProbableCause;
	public String nbiAckTime;
	public String nbiAckUser;
	public String nbiClearUser;
	public String ipAddress;
	public String isMll;
	public String tgNhan;

	public String headerConvert() {
		String header = "recordType;nbiAlarmTime;nbiClearTime;nbiAckTime;nbiObjectInstance;nbiAckState;nbiPerceivedSeverity;"
				+ "nbiProbableCause;nbiSpecificProblem;nbiAckUser;nbiAdditionalText;nbiAlarmType;nbiClearUser;nbiAlarmId;"
				+ "ne;site;cellid;network;neType;alarmMappingId;alarmMappingName;isSendSms;isMonitor;isMll;region;province;district;"
				+ "dept;team;ipAddress;tgNhan";
		return header;
	}
	
	@Override
	public String toString(){
		return 
				(recordType==null?"":recordType) 
		+ ";" + (nbiAlarmTime==null?"":nbiAlarmTime)
		+ ";" + (nbiClearTime==null?"":nbiClearTime)
		+ ";" + (nbiAckTime==null?"":nbiAckTime)
		+ ";" + (nbiObjectInstance==null?"":nbiObjectInstance)
		+ ";" + (nbiAckState==null?"":nbiAckState)
		+ ";" + (nbiPerceivedSeverity==null?"":nbiPerceivedSeverity)
		+ ";" + (nbiProbableCause==null?"":nbiProbableCause)
		+ ";" + (nbiSpecificProblem==null?"":nbiSpecificProblem) 
		+ ";" + (nbiAckUser==null?"":nbiAckUser)
		+ ";" + (nbiAdditionalText==null?"":nbiAdditionalText)  
		+ ";" + (nbiAlarmType==null?"":nbiAlarmType) 
		+ ";" + (nbiClearUser==null?"":nbiClearUser)
		+ ";" + (nbiAlarmId==null?"":nbiAlarmId) 
		+ ";" + (ne==null?"":ne)
		+ ";" + (site==null?"":site)
		+ ";" + (cellid==null?"":cellid)
		+ ";" + (network==null?"":network)
		+ ";" + (neType==null?"":neType)
		+ ";" + (alarmMappingId==null?"":alarmMappingId)
		+ ";" + (alarmMappingName==null?"":alarmMappingName)
		+ ";" + (isSendSms==null?"":isSendSms)
		+ ";" + (isMonitor==null?"":isMonitor)
		+ ";" + (isMll==null?"":isMll)
		+ ";" + (region==null?"":region)
		+ ";" + (province==null?"":province)
		+ ";" + (district==null?"":district)
		+ ";" + (dept==null?"":dept)
		+ ";" + (team==null?"":team)
		+ ";" + (ipAddress==null?"":ipAddress)
		+ ";" + (tgNhan==null?"":tgNhan);
	}; 
}