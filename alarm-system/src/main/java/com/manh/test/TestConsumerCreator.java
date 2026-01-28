package com.manh.test;

import org.apache.kafka.clients.consumer.Consumer;

import com.manh.kafka.consumer.ConsumerCreator;
import com.manh.kafka.consumer.KafkaConsumerConfig;
import com.victor.model.StructureAlarm;

public class TestConsumerCreator {
    public static void main(String[] args) {
        System.out.println("LOAD kafka config");
        try {
            KafkaConsumerConfig.loadKafkaConsumerConfig();
        } catch (Exception e) {
            System.out.println("Lỗi r");
        }
        
        System.out.println("Testing ConsumerCreator...");

        // Tạo consumer
        Consumer<Long, StructureAlarm> consumer = ConsumerCreator.createConsumer(StructureAlarm.class);
        System.out.println("Consumer created successfully: " + consumer.toString());
        
        // Đóng consumer
        consumer.close();
        // consumerTest.close();
        System.out.println("Consumers closed.");
    }
}