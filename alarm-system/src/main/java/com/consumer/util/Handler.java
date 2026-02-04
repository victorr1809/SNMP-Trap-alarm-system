package com.consumer.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

import com.consumer.model.StructureAlarm;
import com.producer.util.DbUtil;

import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Handler implements Runnable {
	private static final Logger logger = Logger.getLogger(Handler.class);
	private LinkedBlockingQueue<StructureAlarm> queue;

	private Connection updateConnection;
	private Connection insertConnection;

	private PreparedStatement insertStmt;
	private PreparedStatement updateStmt;

	private int count = 0;

	private String type;
	private String insertSQL = "CALL alarm.insert_alarm_all(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private String updateSQL = "CALL alarm.update_alarm_end(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private long execTimeMillis = 0L;
	private long totalInsertCount = 0L;
	private long totalUpdateSuccess = 0L;
	private long totalUpdateFailure = 0L;
	private long updateBatchStart = 0L;
	private int updateBatchCounter = 0;

	private final int MAX_BATCH_SIZE = 100;
	private final int MAX_WAIT_TIME = 80;


	// Hàm constructor
	public Handler(String type, LinkedBlockingQueue<StructureAlarm> queue) {
		try {
			this.type = type;
			this.queue = queue;

			// insert alarm
			insertConnection = DbUtil.takeConnection();
			insertStmt = insertConnection.prepareStatement(insertSQL);

			// update alarm
			updateConnection = DbUtil.takeConnection();
			updateStmt = updateConnection.prepareStatement(updateSQL);
			updateConnection.setAutoCommit(false); // batch mode

		} catch (SQLException e) {
			logger.error(e, e);
		}
	}

	// Hàm insert alarm
	private void insertAlarm(StructureAlarm structAlarm) {
		try {
			insertStmt.setString(1, structAlarm.nbiAlarmId);
			insertStmt.setString(2, structAlarm.ne);

			insertStmt.setString(3, structAlarm.nbiAlarmType);
			insertStmt.setString(4, structAlarm.nbiPerceivedSeverity);
			insertStmt.setString(5, structAlarm.nbiSpecificProblem);
			insertStmt.setString(6, structAlarm.nbiAdditionalText);
			insertStmt.setString(7, structAlarm.nbiObjectInstance);

			insertStmt.setString(8, structAlarm.cellid);
			insertStmt.setString(9, structAlarm.site);
			insertStmt.setString(10, structAlarm.neType);
			insertStmt.setString(11, structAlarm.ipAddress);

			insertStmt.setString(12, structAlarm.nbiAlarmTime);
			insertStmt.setString(13, structAlarm.nbiClearTime);
			insertStmt.setString(14, structAlarm.recordType);

			insertStmt.setString(15, structAlarm.network);
			insertStmt.setString(16, structAlarm.region);
			insertStmt.setString(17, structAlarm.province);
			insertStmt.setString(18, structAlarm.district);
			insertStmt.setString(19, structAlarm.dept);
			insertStmt.setString(20, structAlarm.team);

			insertStmt.setString(22, structAlarm.tgNhan);

			// Execute to insert
			insertStmt.executeUpdate();

		} catch (Exception e) {
			logger.error("Lỗi khi INSERT ALARM vào DB: " + e);
		}
	}

	// Hàm update alarm khi nhận END record (Nếu END đến trước START, procedure sẽ tự động INSERT với status= CLEARED)
	private void updateAlarmEnd(StructureAlarm alarm) {
		try {
			updateStmt.setString(1, alarm.nbiAlarmId);
			updateStmt.setString(2, alarm.ne);

			updateStmt.setString(3, alarm.nbiAlarmType);
			updateStmt.setString(4, alarm.nbiPerceivedSeverity);
			updateStmt.setString(5, alarm.nbiSpecificProblem);
			updateStmt.setString(6, alarm.nbiAdditionalText);
			updateStmt.setString(7, alarm.nbiObjectInstance);

			updateStmt.setString(8, alarm.cellid);
			updateStmt.setString(9, alarm.site);
			updateStmt.setString(10, alarm.neType);
			updateStmt.setString(11, alarm.ipAddress);

			updateStmt.setString(12, alarm.nbiAlarmTime);
			updateStmt.setString(13, alarm.nbiClearTime);
			updateStmt.setString(14, alarm.recordType);

			updateStmt.setString(15, alarm.network);
			updateStmt.setString(16, alarm.region);
			updateStmt.setString(17, alarm.province);
			updateStmt.setString(18, alarm.district);
			updateStmt.setString(19, alarm.dept);
			updateStmt.setString(20, alarm.team);
			
			updateStmt.setString(22, alarm.tgNhan);

			// Lưu lại thời điểm ghi nhận stmt đầu tiên
			if (updateBatchStart == 0L) {
				updateBatchStart = System.currentTimeMillis();
				execTimeMillis = updateBatchStart;
			}

			if (updateBatchCounter < MAX_BATCH_SIZE) {
				updateStmt.addBatch();
			}
			updateBatchCounter++;

			long elapsedSeconds = (System.currentTimeMillis() - updateBatchStart) / 1000;
			boolean reachedBatchSize = (updateBatchCounter == MAX_BATCH_SIZE);
			boolean reachedTimeout = (elapsedSeconds >= MAX_WAIT_TIME) && (updateBatchCounter >= 1);

			if (reachedBatchSize || reachedTimeout) {
				System.out.println("Executing batch: size=" + updateBatchCounter + ", timeout=" + reachedTimeout + 
						   ", elapsed=" + elapsedSeconds + "s");
				execUpdateBatch();
			} 

		} catch (SQLException e) {
			logger.error("Lỗi khi UPDATE/INSERT ALARM kết thúc: " + e);
		}
	}

	private void execUpdateBatch() {
		int updatedCount = 0;
		int failedCount = 0;

		try {
			int[] updateResults = updateStmt.executeBatch();

			// Thống kê update thành công và thất bại
			for (int result : updateResults) {
				if (result > 0 || result == PreparedStatement.SUCCESS_NO_INFO) {
					updatedCount++;
				} else if (result == PreparedStatement.EXECUTE_FAILED) {
					failedCount++;
				}
			}
			// commit
			updateConnection.commit();

			// totalUpdateSuccess += updatedCount;
			// if (failedCount > 0)
			// 	totalUpdateFailure += failedCount;
			// System.out.println(
			// 	"\n===== UPDATED OVERVIEW =====" +
			// 	"\nSucess update: " + updatedCount +
			// 	"\nFailded update: " + failedCount +
			// 	"\nTotal update: " + totalUpdateSuccess +
			// 	"\nTotal failed: " + totalUpdateFailure
			// );

			// Clear batch
			updateStmt.clearBatch();

			// Reset Counter
			updateBatchCounter = 0;
			updateBatchStart = 0L;
			execTimeMillis = 0L;

		} catch (SQLException e) {
			System.out.println("LỖI khi update!!: " + e);
		}
	}

	public void run() {
		while (true) {
			StructureAlarm alarm = new StructureAlarm();
			try {
				alarm = queue.take();
				System.out.println(type + " queue remaining: " + queue.size());
				alarm = AlarmInfo.setAlarmInfo(alarm);

				// test insert alarm
				// if (!alarm.recordType.equals("END")) {
					// startTime = System.currentTimeMillis();
					// insertAlarm(alarm);
					// execOneStmt += (System.currentTimeMillis() - startTime);
					// if (++count >= 50) {
					// 	System.out.println(
					// 			"INSERTING ==> " + type + ": " + count + " ALARMS" +
					// 					" / TOTAL => " + (totalInsertCount += count) +
					// 					"\nREMAINED IN QUEUE => " + queue.size() + "\n");
					// 	count = 0;
					// 	execOneStmt = 0;
					// }
				// }

				if (alarm.recordType.equals("END")
						&& !alarm.nbiSpecificProblem.startsWith("8502")
						&& !alarm.nbiSpecificProblem.startsWith("9047")) {
					// System.out.println("Phát hiện bản ghi END: " + alarm);
					updateAlarmEnd(alarm);

					// Log statistics every 50 updates
					// if (totalUpdateSuccess % 50 == 0 && totalUpdateSuccess > 0) {
					// 	System.out.println(
					// 			"UPDATED ==> " + type + ": " + totalUpdateSuccess + " ALARMS" +
					// 					" / FAILED => " + totalUpdateFailure +
					// 					"\nREMAINED IN QUEUE => " + queue.size() + "\n");
					// }
				} else {
					insertAlarm(alarm);
					if (++count >= 50) {
						System.out.println(
								"INSERTING ==> " + type + ": " + count + " ALARMS" +
										" / TOTAL => " + (totalInsertCount += count) +
										"\nREMAINED IN QUEUE => " + queue.size() + "\n");
						count = 0;
					}
				}
			} catch (Exception e) {
				logger.error("Lỗi class run(): " + e);
			}
		}
	}

	public void cleanUpHandler() {
		try {
			if (updateBatchCounter > 0) {
				System.out.println("Update batch còn lại trước khi cleanup: " + updateBatchCounter);
				execUpdateBatch();
			}
			// Đóng Stmt
			if (insertStmt != null)
				insertStmt.close();
			if (updateStmt != null)
				updateStmt.close();

			// Đóng connection
			if (insertConnection != null)
				insertConnection.close();
			if (updateConnection != null)
				updateConnection.close();

			System.out.println("CLEAN UP HANDLER SUCCESSFULLY!!");
		} catch (SQLException e) {
			logger.error("ERROR DURING CLEAN UP: " + e);
		}
	}

}
