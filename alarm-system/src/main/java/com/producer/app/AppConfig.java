package com.producer.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Properties;

import com.producer.common.Global;

public class AppConfig {
	private static String DB_CONFIG = "db-config.properties";
	private static String OID_MAPPING_INFO = "oid-mapping2.properties";

	// --- Load từ điển OID ---
    public static void loadOIDMap() throws FileNotFoundException, IOException {
		Global.OID_CONFIG.clear();
		String connectInfo = "config/" + OID_MAPPING_INFO;
		File filePath = new File(connectInfo);
		
		// connectInfo = getLocation(CONF_PATH) + "/" + OID_MAPPING_INFO;
		InputStream propsStream = null;
		try {
			propsStream = new FileInputStream(filePath);
			Global.OID_CONFIG.load(propsStream);
			propsStream.close();

		} catch (FileNotFoundException e) {
            System.out.println("Lỗi: " + e.getMessage());
		} catch (IOException e) {
            System.out.println("Lỗi: " + e.getMessage());
		}
	}

	// --- Load DB config ---
	public static Properties loadDBConnectInfo(){
		Properties prop = new Properties();
		String connectInfo = "config/" + DB_CONFIG;
		File filePath = new File(connectInfo);
		
		try {		
			prop.load(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			System.out.println("Không tìm thấy file");
		} catch (IOException e) {
			System.out.println("Lỗi đọc file");
		}
		return prop;
	}


	// --- Test ---
    public static void main (String[] args) throws FileNotFoundException, IOException {
        AppConfig.loadOIDMap();
        System.out.println("Load OID Map thành công, số key = " + Global.OID_CONFIG.size());
    } 	
}
