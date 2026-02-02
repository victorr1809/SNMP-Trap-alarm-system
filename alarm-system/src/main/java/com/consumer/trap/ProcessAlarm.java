package com.consumer.trap;

// import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.consumer.util.Handler;
import com.producer.model.StructureAlarm;

public class ProcessAlarm extends Thread {
    private final Logger logger = Logger.getLogger(ProcessAlarm.class.getName()); 
    private static final int QUEUE_SIZE = 10000;

    BlockingQueue<StructureAlarm> _mAlarmQueue;

    // Các hàng đợi xử lý từng loại mạng
    private LinkedBlockingQueue<StructureAlarm> queue3g;
    private LinkedBlockingQueue<StructureAlarm> queue4g;
    private LinkedBlockingQueue<StructureAlarm> queueCore;

    // Các luồng xử lý alarm
	protected Handler _3GThread;
	protected Handler _4GThread;
	protected Handler _coreThread;  

    // Hàm khởi tạo
    public ProcessAlarm(BlockingQueue<StructureAlarm> _alarmQueue) {
		_mAlarmQueue = _alarmQueue;

        // Hàng đợi
		queue3g = new LinkedBlockingQueue<StructureAlarm>();
		queue4g = new LinkedBlockingQueue<StructureAlarm>();
		queueCore = new LinkedBlockingQueue<StructureAlarm>(); 

        // Luồng xử lý
		_3GThread = new Handler("RAN_3G", queue3g);
		_4GThread = new Handler("RAN_4G", queue4g);
		_coreThread = new Handler("CORE", queueCore); 
    }
    

    @Override
    public void run() {
        System.out.println("[PROCESS ALARM] Bắt đầu đẩy vào hàng đợi riêng biệt");
        try{
            while(true) {
                StructureAlarm structAlarm = _mAlarmQueue.take();

                if (structAlarm.network.equals("2G")) {
                    continue;
                } else if (structAlarm.network.equals("3G")) {
                    while(queue3g.size() > QUEUE_SIZE)
                        Thread.sleep(50000);
                    queue3g.add(structAlarm);
                    // System.out.println("Thêm alarm 3G vào queue3g, alarmId: " + structAlarm.nbiAlarmId);
                } else if(structAlarm.network.equals("RAN_4G")) {
                    while(queue4g.size() > QUEUE_SIZE)
                        Thread.sleep(50000);
                    queue4g.add(structAlarm);
                    // System.out.println("Thêm alarm 4G vào queue4g, alarmId: " + structAlarm.nbiAlarmId);
                } else {
                    while(queueCore.size() > QUEUE_SIZE)
                        Thread.sleep(50000);
                    queueCore.add(structAlarm);
                    // System.out.println("Thêm alarm Core vào queueCore, alarmId: " + structAlarm.nbiAlarmId);
                }

                System.out.println(
                    "SIZE: queue3g=" + queue3g.size() + 
                    ", queue4g=" + queue4g.size() + 
                    ", queueCore=" + queueCore.size() + "\n"
                );
                // System.out.println("Size queue4g: " + queue4g.size());
                // System.out.println("Size queueCore: " + queueCore.size() + "\n");
            }

        } catch (Exception e) {
            logger.error("---------------_QUEUE_SIZE = "+QUEUE_SIZE);
			logger.error("---------------_mAlarmQueue.size = "+_mAlarmQueue.size());
			logger.error("---------------_queue3G.size = "+queue3g.size());
			logger.error("---------------_queue4G.size = "+queue4g.size());
			logger.error("---------------_queueCore.size = "+queueCore.size()); 
			logger.error("---------------_Loi ProcessAlarm run(): "+e);
        }
    }
}
