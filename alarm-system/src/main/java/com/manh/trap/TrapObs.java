package com.manh.trap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import com.manh.kafka.consumer.ConsumerService;
import com.manh.kafka.consumer.KafkaConsumerConfig;
import com.manh.kafka.consumer.KafkaConsumerSetting;
import com.victor.model.StructureAlarm;
import com.victor.app.AppConfig;
import com.victor.util.DbUtil;

public class TrapObs {
    public static LinkedBlockingQueue<StructureAlarm> _mAlarmQueueFromSocket = null;
    private static Boolean _getDataFromKafka = true;
    static ProcessAlarm mProcessAlarm = null;

    // Hàm khởi tạo
    public TrapObs() throws FileNotFoundException, IOException {
        _mAlarmQueueFromSocket = new LinkedBlockingQueue<>();
        mProcessAlarm = new ProcessAlarm(_mAlarmQueueFromSocket);
    }


    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        AppConfig.loadOIDMap();
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
                // Lấy msg từ Kafka và đẩy vào _mAlarmDataQueue
				_ps.start();

                // Lấy msg từ _mAlarmDataQueue và đẩy vào 3 hàng đợi khác nhau
				mProcessAlarm.start();
				new Thread(mProcessAlarm._3GThread).start();
				new Thread(mProcessAlarm._4GThread).start();
				new Thread(mProcessAlarm._coreThread).start();
			}
            // long lastReloadTime = System.currentTimeMillis(); 
	        // long reloadInterval = TimeUnit.HOURS.toMillis(3);

            // while(true) {
            //     try {
            //         if(!ConsumerService.isConsumerConnected()) {
            //             System.out.println("Cố gắng kết nối tới Kafka...");
            //             TimeUnit.SECONDS.sleep(0xa);
            //         } else {
            //             TimeUnit.SECONDS.sleep(0xa * 0x6);
            //         }
            //     } catch (Exception e) {
            //         System.out.println("Lỗi: " + e);
            //         System.exit(1);
            //     }
            // }

		} catch (Exception e) {
			System.out.println("OBS gặp lỗi: " + e);
            System.exit(1);
		}
	}

}
