package com.consumer.kafkaConsumer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class KafkaConsumerConfig {
    public static final Properties KAFKA_CONFIG = new Properties();
    private static String connectInfo = "/Users/manh/Documents/ĐA2 OSS/alarm-system/src/main/java/config/kafka-consumer.properties";

    public static void loadKafkaConsumerConfig() throws FileNotFoundException, IOException {
        InputStream propsStream = null;
        try {
            propsStream = new FileInputStream(connectInfo);
            KAFKA_CONFIG.load(propsStream);
            propsStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e);
        } catch (IOException e) {
            System.err.println("Can't read file: " + e);
        }
    }
}
