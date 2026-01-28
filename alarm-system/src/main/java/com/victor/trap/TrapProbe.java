package com.victor.trap;


// import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.victor.model.StructureAlarm;
import com.victor.kafka.producer.*;



public class TrapProbe extends Thread{
    private static BlockingQueue<StructureAlarm> probeDataQueue = null;
    public static int _queueSize = 10000;
    public static boolean _sendToKafka = true;


    public static BlockingQueue<StructureAlarm> getProbeDataQueue() {
		return probeDataQueue;
	}

    public static void setProbeDataQueue(BlockingQueue<StructureAlarm> probeDataQueue) {
        TrapProbe.probeDataQueue = probeDataQueue;
    }

    public static void addProbeDataQueue(StructureAlarm _data ) {
        if(probeDataQueue.size() < 10000) {
            probeDataQueue.add(_data);
        } else {
            System.out.println("Đầy rồi");
        }
    }

    public static void main(String[] args){
        probeDataQueue = new ArrayBlockingQueue<>(10000);
        try {
            if (_sendToKafka) {
                // Thread _ps = (Thread) new ProducerService();
                ProducerService _ps = new ProducerService();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutting down gracefully...");
                    if (_ps != null) {
                        _ps.shutdown(); // Gọi method shutdown của PS
                    }
                }));
                _ps.start();
            }
            while(true) {
                System.out.println("Kiểm tra trạng thái của Kafka...");
                try {
                    if(!ProducerService.isKafkaServerRunning()) {
                        System.out.println("Chưa kết nối được. Cố gắng...");
                        TimeUnit.SECONDS.sleep(0xa);
                    } else {
                        System.out.println(getProbeDataQueue().size());
                        TimeUnit.SECONDS.sleep(0xa * 0x6);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi" + e);
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi Probe: " + e);
        }
    }
}
