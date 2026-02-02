package com.producer.alarm;

public class AlarmType {
    private String node;
	private String blockValue;
	private String alarmInfoValue;
	private String alarmType;
	private String search;
	
	public AlarmType(String node, String blockValue, String alarmInfoValue, String alarmType, String alarmMappingName,
			String alarmMappingId, String isMonitor, String isSendSms, String isMll, String search) {
		super();
		this.node = node;
		this.blockValue = blockValue;
		this.alarmInfoValue = alarmInfoValue;
		this.alarmType = alarmType;
		this.search = search;
	}
	
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getBlockValue() {
		return blockValue;
	}
	public void setBlockValue(String blockValue) {
		this.blockValue = blockValue;
	}
	public String getAlarmInfoValue() {
		return alarmInfoValue;
	}
	public void setAlarmInfoValue(String alarmInfoValue) {
		this.alarmInfoValue = alarmInfoValue;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	} 
}
