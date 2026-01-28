package com.victor.model;

public class Cell2G {

	private String btsObjectname;
	private String objectName;
	private String bscId;
	private String bscName;
	private String btsName;
	private String segmentName;
	
	public Cell2G() {}
	
	public Cell2G(String btsObjectname, String objectName, String bscId, String bscName, String btsName, String segmentName) {
		this.btsObjectname = btsObjectname;
		this.objectName = objectName;
		this.bscId = bscId;
		this.bscName = bscName;
		this.btsName = btsName;
		this.segmentName = segmentName;
	}
	
	public String getBtsObjectname() {
		return btsObjectname;
	}
	public void setBtsObjectname(String btsObjectname) {
		this.btsObjectname = btsObjectname;
	}
	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public String getBscId() {
		return bscId;
	}
	public void setBscId(String bscId) {
		this.bscId = bscId;
	}

	public String getBscName() {
		return bscName;
	}
	public void setBscName(String bscName) {
		this.bscName = bscName;
	}
	public String getBtsName() {
		return btsName;
	}
	public void setBtsName(String btsName) {
		this.btsName = btsName;
	}
	public String getSegmentName() {
		return segmentName;
	}
	public void setSegmentName(String segmentName) {
		this.segmentName = segmentName;
	}
	
}
