package com.consumer.trap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import com.consumer.kafkaConsumer.ConsumerService;
import com.consumer.kafkaConsumer.KafkaConsumerConfig;
import com.consumer.kafkaConsumer.KafkaConsumerSetting;
import com.consumer.model.StructureAlarm;
import com.consumer.util.DbUtil;

import com.producer.app.AppConfig;

public class TrapObs {
    public static LinkedBlockingQueue<StructureAlarm> tempDataQueue = null;
    private static Boolean _getDataFromKafka = true;
    static ProcessAlarm mProcessAlarm = null;

    // Hàm khởi tạo
    public TrapObs() throws FileNotFoundException, IOException {
        tempDataQueue = new LinkedBlockingQueue<>();
        mProcessAlarm = new ProcessAlarm(tempDataQueue);
    }


    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        // AppConfig.loadOIDMap();
        DbUtil.initLoad();
        KafkaConsumerConfig.loadKafkaConsumerConfig();
        TrapObs obs = new TrapObs();
        obs.run();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("DỪNG Trap Obs!");
            mProcessAlarm._3GThread.cleanUpHandler();
            mProcessAlarm._4GThread.cleanUpHandler();
            mProcessAlarm._coreThread.cleanUpHandler();
        }));
    }

	private void run() {
		try {
			if(_getDataFromKafka) {
				Thread _ps = new ConsumerService();
				_ps.start();                // [ConsumerService] Lấy msg từ Kafka và đẩy vào tempDataQueue
				mProcessAlarm.start();      // [ProcessAlarm] Lấy msg từ tempDataQueue và đẩy vào 3 hàng đợi khác nhau

				new Thread(mProcessAlarm._3GThread).start();
				new Thread(mProcessAlarm._4GThread).start();
				new Thread(mProcessAlarm._coreThread).start();
			}

		} catch (Exception e) {
			System.out.println("OBS gặp lỗi: " + e);
            System.exit(1);
		}
	}

}
