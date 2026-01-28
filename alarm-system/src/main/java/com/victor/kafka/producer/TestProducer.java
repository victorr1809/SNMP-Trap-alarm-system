package com.victor.kafka.producer;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
// import org.apache.kafka.clients.producer.RecordMetadata;
import com.victor.model.StructureAlarm;
// import com.victor.kafka.producer.KafkaProducerConfig;
// Import các class cấu hình của bạn
// import ... KafkaProducerConfig;
// import ... StructureAlarm;

public class TestProducer {

    public static void main(String[] args) {

        // Load biến vào KafkaConfig
        try {
            KafkaProducerConfig.loadKafkaProducerConfig();
        } catch (FileNotFoundException e) {
            System.err.println("File config not found at path: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading config file " );
            e.printStackTrace();
        }

        Producer<Long, Object> producer = null;
        try {
            // Gọi hàm tạo Producer
            System.out.println("[1] Đang khởi tạo Producer...");
            producer = ProducerCreator.createProducer();

            if (producer == null) {
                System.err.println("[LỖI] Producer trả về null! Kiểm tra lại code tạo.");
                return;
            }
            System.out.println("[OK] Producer đã được tạo thành công.");

            // Dùng chính StructureAlarm của bạn để test cho chuẩn JsonSerializer
            StructureAlarm testData = new StructureAlarm();
            testData.ipAddress = "127.0.0.1";
            testData.nbiAdditionalText = "Test connection from Java";
            testData.recordType = "TEST_MSG";
            
            String topicName = "test-connection-topic"; 
            long key = System.currentTimeMillis();
            ProducerRecord<Long, Object> record = new ProducerRecord<>(topicName, key, testData);

            System.out.println("[2] Đang gửi tin nhắn đến topic: " + topicName);
            // Lệnh .get() sẽ bắt chương trình đợi cho đến khi Kafka phản hồi
            producer.send(record).get();
            System.out.println("=== GỬI THÀNH CÔNG! ===");

        } catch (Exception e) {
            System.err.println("=== GỬI THẤT BẠI ===");
            System.err.println("Lỗi chi tiết: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (producer != null) {
                producer.close();
                System.out.println("[OK] Đã đóng Producer.");
            }
        }
    }
    
}
