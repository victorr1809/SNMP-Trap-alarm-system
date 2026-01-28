package com.victor.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;

public class KafkaProducerSetting {
	
	public static final String BOOTSTRAP_SERVERS_CONFIG = "kafkaBrokers";
	public static final String CLIENT_ID_CONFIG = "clientIdConfig";
	public static final String ACKS_CONFIG = "acksConfig";
	public static final String MAX_BLOCK_MS_CONFIG = "maxBlockMs";
	public static final String TOPIC_NAME = "topicName";
	public static final String TEST_TOPIC_NAME = "testTopicName";

}


    
