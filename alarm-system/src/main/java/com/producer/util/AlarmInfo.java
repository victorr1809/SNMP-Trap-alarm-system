// StructureAlarm
package com.producer.util;

import com.producer.model.CellN3G;
import com.producer.model.CellN4G;
import com.producer.model.HCoreN;
import com.producer.model.HProvinceCode;
import com.producer.model.StructureAlarm;


public class AlarmInfo {
	private static final String alarmRNC 	= "PLMN-PLMN/RNC";
	private static final String alarmBSC 	= "PLMN-PLMN/BSC"; 
	private static final String alarmRAN4G  = "PLMN-PLMN/MRBTS"; 
	private static final String alarmGGSN 	= "PLMN-PLMN/GPBB";
	private static final String alarmGGSN2 	= "PLMN-PLMN/FING";
	private static final String alarmSGSN 	= "PLMN-PLMN/SGSN";
	private static final String alarmSGSN2 	= "PLMN-PLMN/FLEXINS";
	private static final String alarmSGSN3 	= "PLMN-PLMN/SAM"; 

	// Hàm điền network, nbiEventTime, recordType, nbiPerceivedSeverity
	public static StructureAlarm classifyAlarm(StructureAlarm structAlarm) {
		String objRef = structAlarm.nbiObjectInstance;
        // Đặt network theo ip
		if(structAlarm.ipAddress.equals("10.51.130.13")) {
			structAlarm.network = "PS_CORE"; 
		} else {
			// Đặt network theo nbiObjectInstance
			if(objRef.contains(alarmBSC)){
				structAlarm.network = "2G";  
			}else if(objRef.contains(alarmRNC)){
				structAlarm.network = "3G"; 
			}else if(objRef.contains(alarmRAN4G)){
				structAlarm.network = "RAN_4G";
			}
        }
		
        if(structAlarm.network == null) {
            if(structAlarm.ipAddress.equals("10.19.236.13")
			||structAlarm.ipAddress.equals("10.19.237.13")
			||structAlarm.ipAddress.equals("10.19.239.13")
			||structAlarm.ipAddress.equals("10.30.111.13")
			||structAlarm.ipAddress.equals("10.30.113.13")
			||structAlarm.ipAddress.equals("10.52.100.13")
		    ){
            structAlarm.network = "2G";
			} else {
				structAlarm.network = "RAN_4G";
			}
		}

        // Loại bỏ phần thập phân trong thời gian (HH:MM:SS.sss)
        if(structAlarm.nbiEventTime != null && !structAlarm.nbiEventTime.isEmpty()){
            int dotIndex = structAlarm.nbiEventTime.indexOf(".");
            if(dotIndex > 0) {
                structAlarm.nbiEventTime = structAlarm.nbiEventTime.substring(0, dotIndex);
            }
        }
        // Điền recordType
		if(structAlarm.nbiAlarmTime != null && !structAlarm.nbiAlarmTime.isEmpty()){
			structAlarm.recordType = "START";
			structAlarm.nbiAlarmTime = structAlarm.nbiAlarmTime.substring(0,structAlarm.nbiAlarmTime.indexOf("."));
		}else if(structAlarm.nbiClearTime != null && !structAlarm.nbiClearTime.isEmpty()){
			structAlarm.recordType = "END";
			structAlarm.nbiClearTime = structAlarm.nbiClearTime.substring(0,structAlarm.nbiClearTime.indexOf("."));
		}
		// else if(structAlarm.nbiAckTime != null && !structAlarm.nbiAckTime.isEmpty()){
		// 	structAlarm.recordType = "ACK";
		// 	structAlarm.nbiAckTime = structAlarm.nbiAckTime.substring(0,structAlarm.nbiAckTime.indexOf("."));
		// } 
	
		// Set nbiPerceivedSeverity
		structAlarm.nbiPerceivedSeverity = AlarmInfo.getSecurity(structAlarm.nbiPerceivedSeverity);
		return structAlarm;
	}

	// Hàm điền nbiAlarmType, site, ne, neType, thông tin địa lý
	public static StructureAlarm setAlarmInfo(StructureAlarm structAlarm) {
		AlarmInfo.getBSCId(structAlarm);
		// mapping nbiAlarmType...

		// Điền site
		try {
			// Thay đổi site name nếu cảnh báo là MAT DIEN CRAN
			// if (structAlarm.nbiAlarmType.equals("POWER")) {
			// 	// site name theo alarm info
			// 	if(structAlarm.nbiAdditionalText.contains("<") && structAlarm.nbiAdditionalText.contains(">")) {
			// 		structAlarm.site = structAlarm.nbiAdditionalText.substring(structAlarm.nbiAdditionalText.lastIndexOf("<") + 1, structAlarm.nbiAdditionalText.lastIndexOf(">"));
			// 	}
			// 	// site name theo alarm name
			// 	if(structAlarm.nbiSpecificProblem.contains("<") && structAlarm.nbiSpecificProblem.contains(">")) {
			// 		structAlarm.site = structAlarm.nbiSpecificProblem.substring(structAlarm.nbiSpecificProblem.lastIndexOf("<") + 1, structAlarm.nbiSpecificProblem.lastIndexOf(">"));
			// 	}
			// }

			// Thay đổi tên trạm nếu cảnh báo máy phát điện (vì những cảnh báo này có chứa tên trạm chính xác ở trong dấu ngoặc nhọn)
			if(structAlarm.nbiSpecificProblem.toUpperCase().contains("GENERATOR") || structAlarm.nbiAdditionalText.toUpperCase().contains("GENERATOR")) {
				// Site name theo alarm info
				if(structAlarm.nbiAdditionalText.contains("<") && structAlarm.nbiAdditionalText.contains(">")) {
					structAlarm.site = structAlarm.nbiAdditionalText.substring(structAlarm.nbiAdditionalText.lastIndexOf("<") + 1, structAlarm.nbiAdditionalText.lastIndexOf(">"));
				}
				// Site name theo alarm name
				if(structAlarm.nbiSpecificProblem.contains("<") && structAlarm.nbiSpecificProblem.contains(">")) {
					structAlarm.site = structAlarm.nbiSpecificProblem.substring(structAlarm.nbiSpecificProblem.lastIndexOf("<") + 1, structAlarm.nbiSpecificProblem.lastIndexOf(">"));
				}
			}
		} catch (Exception e) {}

		// Set ne = site
		if (structAlarm.network.equals("RAN_4G")) {
			if (structAlarm.site != null && !structAlarm.site.equals("")) {
				structAlarm.ne = structAlarm.site;
			}
		}

		// Điền network type
		structAlarm.neType = AlarmInfo.getNeType(structAlarm.ne, structAlarm.site, structAlarm.cellid, structAlarm.network);

		// Mapping thông tin quận huyện
		getProvince(structAlarm);

		// Set region default
		if (structAlarm.region == null) {
			structAlarm.region = "UNKNOWN";
		}

		return structAlarm;
	}


	// ----- HÀM DÙNG CHO PHẦN TRÊN -----

	private static void getBSCId(StructureAlarm structAlarm) {
		String[] arrAll = structAlarm.nbiObjectInstance.split("/");
		// 3G
		if (structAlarm.network.equals("3G")){
			if (structAlarm.nbiObjectInstance.contains("WCEL")) {
				CellN3G cellN3G = AlarmInfo.getCellN3G(1, arrAll[0] + "/" + arrAll[1] + "/" + arrAll[2] + "/" + arrAll[3]);

				if (cellN3G != null) {
					structAlarm.ne = cellN3G.getRncname();
					structAlarm.site = cellN3G.getWbtsname();
					structAlarm.cellid = cellN3G.getCellname();
				} else {
					structAlarm.ne = arrAll[1];
					structAlarm.site = arrAll[2];
					structAlarm.cellid = arrAll[3];
				}
			}
			else if (structAlarm.nbiObjectInstance.contains("WBTS")) {
				CellN3G cellN3G = AlarmInfo.getCellN3G(2, arrAll[0] + "/" + arrAll[1] + "/" + arrAll[2]);
				if (cellN3G != null ) {
					structAlarm.ne = cellN3G.getRncname();
					structAlarm.site = cellN3G.getWbtsname();
				} else {
					structAlarm.ne = arrAll[1];
					structAlarm.site = arrAll[2];
				}
			}
			else {
				CellN3G cellN3G = DbUtil.RNCID.get(arrAll[0] + "/" + arrAll[1]);
				if (cellN3G != null) {
					structAlarm.ne = cellN3G.getRncname();
				} else {
					structAlarm.ne = arrAll[1];
				}
			}
		}
		// 4G
		else if(structAlarm.network.equals("RAN_4G")) {
			if(structAlarm.nbiObjectInstance.contains("LNCEL")) {
				CellN4G cellN4G = AlarmInfo.getCellN4G(arrAll[1].substring(arrAll[1].indexOf("-")+1, arrAll[1].length()), arrAll[3].substring(arrAll[3].indexOf("-")+1, arrAll[3].length()));
		
				if(cellN4G != null) {
					structAlarm.ne = cellN4G.getNodeName();
					structAlarm.site = cellN4G.getNodeName();
					structAlarm.cellid = cellN4G.getCellName();
				} else {
					structAlarm.ne = arrAll[1];
					structAlarm.site = arrAll[1];
					structAlarm.cellid = arrAll[3];
				}
			} 
			else if (structAlarm.nbiObjectInstance.contains("LNBTS")) {
				CellN4G cellN4G = DbUtil.NODE4GID.get(arrAll[1].substring(arrAll[1].indexOf("-")+1, arrAll[1].length()));
				if (cellN4G != null) {
					structAlarm.ne = cellN4G.getNodeName();
					structAlarm.site = cellN4G.getNodeName();
				}else {
					structAlarm.ne = arrAll[1];
					structAlarm.site = arrAll[1];
				}
			} 
			else {
				CellN4G cellN4G = DbUtil.NODE4GID.get(arrAll[1].substring(arrAll[1].indexOf("-")+1, arrAll[1].length()));
				if (cellN4G != null) {
					structAlarm.ne = cellN4G.getNodeName(); 
				} else {
					structAlarm.ne = arrAll[1]; 
				}
			}
		}
		// CORE PS
		else {
			if(structAlarm.nbiObjectInstance.contains(alarmSGSN) || structAlarm.nbiObjectInstance.contains(alarmSGSN2) ||structAlarm.nbiObjectInstance.contains(alarmGGSN2)) { 
				
				HCoreN hCoreN = getCoreN(arrAll[1].substring(arrAll[1].indexOf("-")+1, arrAll[1].length()));
				
				if(hCoreN != null) {
					structAlarm.ne = hCoreN.getNe();
					structAlarm.neType = hCoreN.getNeType();
					structAlarm.region = hCoreN.getRegion();
					structAlarm.dept = hCoreN.getDept();
					structAlarm.team = hCoreN.getTeam();
				}else {
					structAlarm.ne = arrAll[1]; 
				}
				 
			} else if (structAlarm.nbiObjectInstance.contains(alarmGGSN)) {
				for(String item: arrAll) {
					if(item.contains("GGSN-")) {
						
						HCoreN hCoreN = getCoreN(item.substring(item.indexOf("-")+1, item.length()));
						
						if(hCoreN != null) {
							structAlarm.ne = hCoreN.getNe();
							structAlarm.neType = hCoreN.getNeType();
							structAlarm.region = hCoreN.getRegion();
							structAlarm.dept = hCoreN.getDept();
							structAlarm.team = hCoreN.getTeam();
						}else {
							structAlarm.ne = item; 
						}
					}
				}
			} else if(structAlarm.nbiObjectInstance.contains(alarmSGSN3)){
				HCoreN hCoreN = getCoreN(arrAll[2].substring(arrAll[2].indexOf("@")+1, arrAll[2].length()));
				
				if(hCoreN != null) {
					structAlarm.ne = hCoreN.getNe();
					structAlarm.neType = hCoreN.getNeType();
					structAlarm.region = hCoreN.getRegion();
					structAlarm.dept = hCoreN.getDept();
					structAlarm.team = hCoreN.getTeam();
				}else {
					structAlarm.ne = arrAll[1]; 
				}
			} else {
				structAlarm.ne = arrAll[1]; 
			} 
		}
	}
	
	public static CellN3G getCellN3G(int type, String name) {
		// objectname (WCEL)
		if (type == 1) {
			for (CellN3G item: DbUtil.BTS_3G) {
				if (item.getObjectname().equals(name)) {
					return item;
				}
			}
		} 
		// bts_objectname (WBTS)
		else {
			for (CellN3G item: DbUtil.BTS_3G) {
				if (item.getBtsObjectname().equals(name)) {
					return item;
				}
			}
		}
		return null;
	}

	public static CellN4G getCellN4G(String nodeId, String cellId) {
		for (CellN4G item: DbUtil.CELL_4G) {
			if (item.getNodeId().equals(nodeId)
					&& item.getCellId().equals(cellId)) {
				return item;
			}
		}	
		return null;
	} 

	public static HCoreN getCoreN(String nodeId) {
		for(HCoreN item : DbUtil.CORE_LIST) {
			if(item.getNodeId().equals(nodeId)) {
				return item;
			}
		}
		return null;
	}

	// --- Điền các trường địa lý ---
	public static void getProvince(StructureAlarm structAlarm) {
		HProvinceCode hProvinceCode = null;
		// 4G
		if(structAlarm.network.equals("RAN_4G")) {
			for (HProvinceCode item: DbUtil.PROVINCE_BY_CODE) {
				if (structAlarm.ne.startsWith(item.getCode())) {
					structAlarm.region = item.getRegion();
					structAlarm.province = item.getProvince();
					structAlarm.district = item.getDistrict();
					structAlarm.dept = item.getDept();
					structAlarm.team = item.getTeam();
				}
			}
		}
		// 2G, 3G
		else if(structAlarm.network.equals("2G") || structAlarm.network.equals("3G")) {
			// site
			if(structAlarm.site != null) {
				for (HProvinceCode item: DbUtil.PROVINCE_BY_CODE) {
					if (structAlarm.site.startsWith(item.getCode())) {
						structAlarm.region = item.getRegion();
						structAlarm.province = item.getProvince();
						structAlarm.district = item.getDistrict();
						structAlarm.dept = item.getDept();
						structAlarm.team = item.getTeam();		
						break;
					}
				}
			}
			//ne
			else {
				if(structAlarm.ne != null) {
					hProvinceCode = DbUtil.PROVINCE_BY_NE.get(structAlarm.ne);
					if(hProvinceCode != null) {
						structAlarm.region = hProvinceCode.getRegion();
						structAlarm.province = hProvinceCode.getProvince();
						structAlarm.district = hProvinceCode.getDistrict();
						structAlarm.dept = hProvinceCode.getDept();
						structAlarm.team = hProvinceCode.getTeam();
					}
				}
			}  
		} 
	} 
	
	// --- Điền severity code ----
	public static String getSecurity(String code) {
		if (code.equals("1")) return "A1";
		else if (code.equals("2")) return "A2";
		else if (code.equals("3")) return "A3";
		else return "A4";
	}

	// --- Điền Network Type ---
	public static String getNeType(String ne, String site, String cell, String network) {
		String neType = "";
		try {
			if(network.equals("2G")) {
				if (cell!=null && !cell.equals("")) neType = "CELL";
				else {
					if(site != null && !site.equals("")) neType = "BTS";
				}
			} else if (network.equals("3G")) {
				neType = "RNC";
			} else if (network.equals("RAN_4G")) {
				if (cell!=null && !cell.equals("")) neType = "EUTRANCELL";
				else neType = "ENODEB";
			} else {
				neType = "UNKNOWN";
			}
		} catch (Exception e) {}
		return neType;
	}
	

}