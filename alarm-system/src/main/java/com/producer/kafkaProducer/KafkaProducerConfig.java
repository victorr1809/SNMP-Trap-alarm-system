package com.producer.kafkaProducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// import java.io.UnsupportedEncodingException;
// import java.net.MalformedURLException;
// import java.net.URL;
// import java.net.URLDecoder;
// import java.security.CodeSource;
// import org.apache.log4j.Logger;
// import source.app.AppConfig;
// import source.common.Global;

public class KafkaProducerConfig {

    public static final Properties KAFKA_CONFIG = new Properties();
	private static final String KAFKA_CONF_FILE = "kafka-producer.properties";
    
	
	public static void loadKafkaProducerConfig() throws FileNotFoundException, IOException {
        String connectInfo = "config/" + KAFKA_CONF_FILE;
        File filePath = new File(connectInfo);
		InputStream propsStream = null;
		try {
			propsStream = new FileInputStream(filePath);
			KAFKA_CONFIG.load(propsStream);
			propsStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("File config not found at path: " + filePath);
            e.printStackTrace(); // In chi tiết lỗi để debug
        } catch (IOException e) {
            System.err.println("Error reading config file: " + filePath);
            e.printStackTrace();
        }
	}
    
    public static void main (String[] args) {
        try {
            loadKafkaProducerConfig();
            // 2. Kiểm tra kết quả
            if (KAFKA_CONFIG.isEmpty()) {
                System.err.println("❌ THẤT BẠI: Không load được cấu hình nào (Properties rỗng).");
                System.err.println("👉 Hãy kiểm tra xem file đã tạo chưa và đường dẫn in ra bên trên có đúng không.");
            } else {
                System.out.println("✅ THÀNH CÔNG: Đã load được cấu hình!");
                System.out.println("--- Nội dung cấu hình ---");
                KAFKA_CONFIG.forEach((key, value) -> {
                    System.out.println("   " + key + " = " + value);
                });
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}