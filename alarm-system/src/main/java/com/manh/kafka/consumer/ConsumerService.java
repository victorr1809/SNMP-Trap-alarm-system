package com.manh.kafka.consumer;

import java.sql.Struct;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.log4j.Logger;

import com.manh.trap.TrapObs;
import com.victor.model.StructureAlarm;

public class ConsumerService extends Thread{
    private static Consumer<Long, StructureAlarm> consumer = null;
    private static Consumer<Long, StructureAlarm> consumerTest = null;
    private static LinkedBlockingQueue<StructureAlarm> tempDataQueue = null;
    private static boolean _subcribeDataFromKafka = true;

    // Hàm khởi tạo (Chạy đầu tiên)
    public ConsumerService () {
        this.consumer = ConsumerCreator.createConsumer(StructureAlarm.class);
        tempDataQueue = new LinkedBlockingQueue<>();
    }

    // Hàm đợi nếu Queue đầy
    public static void waitTempDataQueue() {
        try {
            if (tempDataQueue.size() > 10000) {
                Thread.sleep(50000);
            }
        } catch (InterruptedException ex){
    		ex.printStackTrace();
        }
    }

    // Hàm lấy message từ Kafka và đẩy vào tempDataQueue
    public static void consumerMessages(String topicName) throws InterruptedException {
        System.out.println("[CONSUMER SERVICE] Bắt đầu lấy từ topic alarm-snmp");
        try {
            consumer.subscribe(Collections.singletonList(topicName));
        } catch (InvalidTopicException e) {
             _subcribeDataFromKafka = false;
        }

        while (_subcribeDataFromKafka) {
        	
        	ConsumerRecords<Long, StructureAlarm> consumerRecords = null ;
            Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

            try {
            	waitTempDataQueue();
            	consumerRecords = consumer.poll(Duration.ofMillis(100));
            	
            } catch (TimeoutException e) {
                _subcribeDataFromKafka = false;
            } catch (BrokerNotAvailableException e) {
                _subcribeDataFromKafka = false;
            }  catch (DisconnectException e) {
                _subcribeDataFromKafka = false;
            } catch (Exception e) {
                _subcribeDataFromKafka = false;
            }

            if(consumerRecords.count()>0) {
            	System.out.println("Tổng số bản ghi lấy được: " + consumerRecords.count());

            	/*
            	consumerRecords.forEach(record -> {
             	System.out.println("Record Key: " + record.key() +
             		", Record Value: " + record.value() +
                     ", Record Partition: " + record.partition() +
                     ", Record Offset: " + record.offset());
             });
             */

                consumerRecords.forEach(record -> {
                    StructureAlarm structAlarm = record.value();
                    tempDataQueue.add(structAlarm);
                });
            }

            try {
                consumer.commitAsync();
            } catch(Exception e) {
                System.out.println("Lỗi commit");
                retryCommit(consumer);
            }

            while(!tempDataQueue.isEmpty()) {
                StructureAlarm structAlarm = tempDataQueue.poll();
                while(TrapObs._mAlarmQueueFromSocket.size() > 10000)
                    Thread.sleep(10000);
                TrapObs._mAlarmQueueFromSocket.add(structAlarm);
            }
        }
        
        consumer.close();
        
    }

    // Kiểm tra Consumer đã kết nối được với KAFKA chưa
    public static boolean isConsumerConnected() {
        try {
            consumerTest = ConsumerCreator.createConsumerTest(StructureAlarm.class);
            consumerTest.poll(Duration.ofMillis(1000));
            consumerTest.close();
            return true;
        } catch (TimeoutException e) {
            consumerTest.close();
            return false;
        } catch (BrokerNotAvailableException e) {
            consumerTest.close();
            return false;
        } catch (DisconnectException e) {
            consumerTest.close();
            return false;
        } catch (Exception e) {
            consumerTest.close();
            return false;
        }
    }

    public static boolean isConsumerServiceRunning() {
        return _subcribeDataFromKafka;
    }

    @Override
    public void run() {
        try {
            _subcribeDataFromKafka = true;
            consumerMessages(KafkaConsumerConfig.KAFKA_CONFIG.getProperty(KafkaConsumerSetting.TOPIC_NAME));
        } catch (InterruptedException e) {
            restart();
            System.out.println("Lỗi Consumer service: " + e.getMessage());
        }
    }

    // Hàm khởi động lại nếu consumer bị lỗi
    public void restart() {
        consumer = ConsumerCreator.createConsumer(StructureAlarm.class);
        try {
            consumerMessages(KafkaConsumerConfig.KAFKA_CONFIG.getProperty(KafkaConsumerSetting.TOPIC_NAME));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Hàm thử commit lại tối đa 3 lần
    public static void retryCommit(Consumer<Long, StructureAlarm> consumer) {
        int retryCount = 1;
        boolean commitSuccess = false;

        while(!commitSuccess && retryCount < 4) {
            try {
                consumer.commitAsync();
                commitSuccess = true;
            } catch (Exception e) {
                System.err.println("Lỗi commit, lần " + retryCount);
                retryCount ++;

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
