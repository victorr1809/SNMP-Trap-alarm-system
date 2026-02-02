package com.producer.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.producer.common.Global;

public class AppConfig {
    // private static final String OID_MAPPING_INFO = "oid-mapping-cisco.properties";  
	private static String dbConfig = "/Users/manh/Documents/ĐA2 OSS/alarm-system/config/db-config.properties";

    public static void loadOIDMap() throws FileNotFoundException, IOException {
		Global.OID_CONFIG.clear();
		// connectInfo = getLocation(CONF_PATH) + "/" + OID_MAPPING_INFO;

		// --- Thay đổi path ở đây ---
		String connectInfo = "/Users/manh/Documents/ĐA2 OSS/alarm-system/config/oid-mapping2.properties";
		InputStream propsStream = null;
		try {
			propsStream = new FileInputStream(connectInfo);
			Global.OID_CONFIG.load(propsStream);
			propsStream.close();
		} catch (FileNotFoundException e) {
            System.out.println("Lỗi: " + e.getMessage());
		} catch (IOException e) {
            System.out.println("Lỗi: " + e.getMessage());
		}
	}

	public static Properties loadDBConnectInfo(){
		Properties prop = new Properties();
		
		try {		
			prop.load(new FileInputStream(dbConfig));
		} catch (FileNotFoundException e) {
			System.out.println("Không tìm thấy file");
		} catch (IOException e) {
			System.out.println("Lỗi đọc file");
		}
		return prop;
	}

	

    // public static void main (String[] args) throws FileNotFoundException, IOException {
    //     AppConfig.loadOIDMap();
    //     System.out.println("Load OID Map thành công, số key = " + Global.OID_CONFIG.size());

    // } 	
}
