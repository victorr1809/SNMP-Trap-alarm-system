package com.victor.trap;


import java.io.FileNotFoundException;
import java.io.IOException;
import com.victor.app.AppConfig;
import com.victor.common.Global;
import com.victor.model.StructureAlarm;
import com.victor.app.AppSetting;


public class MappingOID {
    public void setData(String OID_var, StructureAlarm data, String value){ 
		if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_EVENT_TIME))){data.nbiEventTime = value;} 
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_OBJECT_INSTANCE))){data.nbiObjectInstance = value;} 
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ACK_STATE))){data.nbiAckState = value;} 
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_PERCEIVED_SEVERITY))){data.nbiPerceivedSeverity = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_PROBABLE_CAUSE))){data.nbiProbableCause = value;} 
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_SPECIFIC_PROBLEM))){data.nbiSpecificProblem = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ACK_TIME))){data.nbiAckTime = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ACK_USER))){data.nbiAckUser = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ADDITIONAL_TEXT))){data.nbiAdditionalText = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ALARM_ID))){data.nbiAlarmId = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ALARM_TIME))){data.nbiAlarmTime = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_ALARM_TYPE))){data.nbiAlarmType = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_CLEAR_TIME))){data.nbiClearTime = value;}
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.NBI_CLEAR_USER))){data.nbiClearUser = value;}  
		// Ip
		else if(OID_var.equals(Global.OID_CONFIG.getProperty(AppSetting.IP_ADDRESS))){data.ipAddress = value;} 
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		AppConfig.loadOIDMap();
		MappingOID mapper = new MappingOID();
		StructureAlarm test = new StructureAlarm();
		String oidAdditionalText= Global.OID_CONFIG.getProperty(AppSetting.NBI_ADDITIONAL_TEXT);
		mapper.setData(oidAdditionalText, test, "abcxyz");
		System.out.println("OID additional text: " + oidAdditionalText);
		System.out.println("OID additional text mapping: " + test.nbiAdditionalText);
		System.out.println("Structure Alarm: " + test);
	}
}