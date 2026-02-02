package com.producer.kafkaProducer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Node;
import org.apache.log4j.Logger;

import com.producer.model.StructureAlarm;
import com.producer.trap.TrapProbe;


public class ProducerService extends Thread {
    private Producer<Long, StructureAlarm> producer;
    private static Producer<Long, StructureAlarm> testProducer;
    private static Logger logger = Logger.getLogger(ProducerService.class);
    private boolean kafkaRunning = true;

    // Hàm khởi tạo (khi tạo đối tượng ProducerService thì hàm này sẽ chạy đầu tiên)
    public ProducerService() {
        this.producer = ProducerCreator.createProducer();
        this.testProducer = ProducerCreator.createProducer();
    }

    // Hàm gửi message tới Kafka
    public void sendMessage(String topic, StructureAlarm message) {
        ProducerRecord<Long, StructureAlarm> record = new ProducerRecord<>(topic, message);
        try {
            RecordMetadata metadata = producer.send(record).get();
            System.out.println("Message gửi tới: " + metadata.partition() + " partition " + metadata.offset() + " offset");
        } catch (Exception e) {
            System.out.println("Lỗi mẹ rồi");
            logger.error("[ERROR]: " + e.getMessage());
            retrySend(record);
            // retrySend(record);
        }
    }

    // Hàm kiểm tra xem Kafka còn chạy không
    public static boolean isKafkaServerRunning() {
        boolean temp = true;
        try {
            temp = sendTestMessage();
        } catch(Exception e) {
            logger.error("[LỖI]: " + e.getMessage());
            return false;
        }
        return temp;
    }

    // Hàm gửi thử message tới Kafka
    public static boolean sendTestMessage() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(formatter);
        String testTopic = KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.TEST_TOPIC_NAME);
        StructureAlarm test = new StructureAlarm();

        test.nbiAlarmTime = formatDateTime;

        ProducerRecord<Long, StructureAlarm> record = new ProducerRecord<>(testTopic, test);
        try {
            testProducer.send(record).get();
            return true;
        } catch(Exception e) {
            System.out.println("Lỗi mẹ rồi");
            return true;
        }
    }

    // Hàm gửi lại message tối đa 3 lần nếu xảy ra lỗi
    private void retrySend(ProducerRecord<Long, StructureAlarm> record) {
        int maxRetry = 3;
        int retrySleepSecond = 5;
        for (int retryCount=0; retryCount <= maxRetry; retryCount++) {
            try {
                System.out.println("Gửi lại message lần " + retryCount);
                producer.send(record).get();
            } catch(Exception e) {
                System.out.println("Lỗi khi gửi lại message lần " + retryCount);
                e.printStackTrace();
            }
            try {
            TimeUnit.SECONDS.sleep(retrySleepSecond);
            } catch(Exception e) {
                System.out.println("Lỗi khi gửi lại message lần " + retryCount);
                e.printStackTrace();
            }
        }
        System.err.println("Đã thử lại tối đa 3 lần. Gửi Message thất bại");
    }

    @Override
    public void run() {
        // Observer's timer
        long _lStartObserving = 0L;
        final long _wait = 0xf * 0x3c * 0x3e8;
        int _count = 0;
        // long _startTime = System.currentTimeMillis();
        String topic = KafkaProducerConfig.KAFKA_CONFIG.getProperty(KafkaProducerSetting.TOPIC_NAME);
        
        while (kafkaRunning) {
            try {
                if (TrapProbe.getProbeDataQueue().size() > 0) {
                    StructureAlarm _s = TrapProbe.getProbeDataQueue().take();
                    sendMessage(topic, _s);
                    // sendMessage(_s.ipAddress, _s);
                    //_s = null;
                } else {
                    while (TrapProbe.getProbeDataQueue().size() <= 0) {
                        TimeUnit.MILLISECONDS.sleep(0xf);
                        if (_count++ > 0xa * 0xa) {
                            break;
                        }
                    }
                    _count = 0;
                }
                if ((System.currentTimeMillis() - _lStartObserving) > _wait) {
                    if (!ProducerService.isKafkaServerRunning()) {
                        if (recreateProducer() > 0) {
                            kafkaRunning = false;
                        }
                    }
                    _lStartObserving = System.currentTimeMillis();
                }
            } catch (Exception e) {
                System.out.println("[INFO] => Kafka running: " + e);
                logger.error("[ERROR]: " + e.getMessage());
            }
        } 
    }

    public void shutdown() {
        System.out.println("DỪNG ProducerService!");
        kafkaRunning = false;
        try {
            this.join(5000); //5s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Hàm tạo lại Producer
    private int recreateProducer() {
        int retcode = 0;
        int count = 0;
        try {
            if(producer != null) {
                try {
                    producer.close();
                    producer = null;
                } catch (Exception e) {
                    System.out.println("Lỗi: " + e);
                    producer = null;
                }
            }
            while (true) {
                if(sendTestMessage()) {
                    producer = ProducerCreator.createProducer();
                    if(producer != null){
                        retcode = 0; 
                        break;
                    }
                } else {
                    System.out.println("Lỗi khi cố gắng tạo Producer, thử lại...");
                    if (count > 20) {
                        retcode = 1;
                        break;
                    }
                    TimeUnit.SECONDS.sleep(0xf);
                }
            }
        } catch(Exception e) {
            retcode = 1;
            System.out.println("Lỗi: " + e);
        }
        return retcode;
    }
}

