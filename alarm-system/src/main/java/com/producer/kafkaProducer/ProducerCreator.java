package com.producer.kafkaProducer;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
// import org.apache.kafka.common.serialization.StringSerializer;



public class ProducerCreator {
    public static <T> Producer<Long, T> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.CLIENT_ID_CONFIG));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.ACKS_CONFIG));
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.MAX_BLOCK_MS_CONFIG));
        
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        return new KafkaProducer<>(props);
    }
}