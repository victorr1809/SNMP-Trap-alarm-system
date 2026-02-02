package com.consumer.test;

import java.time.Duration;
import java.util.Collections;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.consumer.kafkaConsumer.ConsumerCreator;
import com.consumer.kafkaConsumer.KafkaConsumerConfig;
import com.consumer.kafkaConsumer.KafkaConsumerSetting;
import com.producer.model.StructureAlarm;

public class TestKafkaConsumer {
    public static void main(String[] args) {
        Consumer<Long, StructureAlarm> consumer = ConsumerCreator.createConsumer(StructureAlarm.class);

        String topicName = KafkaConsumerConfig.KAFKA_CONFIG.getProperty(KafkaConsumerSetting.TOPIC_NAME);
        consumer.subscribe(Collections.singletonList(topicName));

        System.out.println("Bắt đầu consume messages từ topic: " + topicName);

        while (true) {
            ConsumerRecords<Long, StructureAlarm> records = consumer.poll(Duration.ofMillis(100));

            if (!records.isEmpty()) {
                records.forEach(record -> {
                    System.out.println("Key: " + record.key() +
                                       ", Value: " + record.value() +
                                       ", Partition: " + record.partition() +
                                       ", Offset: " + record.offset());
                });
            }
        }
    }
}