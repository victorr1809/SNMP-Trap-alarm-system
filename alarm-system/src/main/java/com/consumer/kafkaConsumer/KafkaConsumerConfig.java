package com.consumer.kafkaConsumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class KafkaConsumerConfig {
    public static final Properties KAFKA_CONFIG = new Properties();
    private static String KAFKA_CONSUMER_CONF = "kafka-consumer.properties";

    public static void loadKafkaConsumerConfig() throws FileNotFoundException, IOException {
        String connectInfo = "config/" + KAFKA_CONSUMER_CONF;
        File filePath = new File(connectInfo);
        InputStream propsStream = null;
        try {
            propsStream = new FileInputStream(filePath);
            KAFKA_CONFIG.load(propsStream);
            propsStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e);
        } catch (IOException e) {
            System.err.println("Can't read file: " + e);
        }
    }
}
