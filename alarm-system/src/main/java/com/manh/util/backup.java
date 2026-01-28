// package com.manh.util;

// import java.sql.CallableStatement;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.LinkedBlockingQueue;
// import org.apache.log4j.Logger;
// import java.util.concurrent.TimeUnit;
// import java.sql.Timestamp;
// import java.text.ParseException;
// import java.text.SimpleDateFormat;

// import com.victor.model.StructureAlarm;
// import com.victor.util.DbUtil;

// public class backup implements Runnable {
//     private static final Logger logger = Logger.getLogger(Handler.class);
//     private LinkedBlockingQueue<StructureAlarm> queue;
// 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:m:s");
	
// 	private Connection insertConnection;
// 	private Connection historyConnection;
	
//     private PreparedStatement insertStmt; 
// 	private PreparedStatement delStmt;
// 	private PreparedStatement moveStmt;

// 	private final int MAX_BATCH_SIZE = 120;
// 	private final int MAX_WAIT_TIME = 80;

// 	private int count = 0;
// 	private long historyBatchStart = 0L;
// 	private int historyBatchCounter = 0;

//     private String type;
// 	private String deleteSQL = "DELETE FROM alarm.alarm_active WHERE nbi_alarm_id = ? AND ne = ?";
//     private String moveSQL = "CALL alarm.move_to_alarm_history(?,?,?);";
//     private String insertSQL = "CALL alarm.insert_alarm_active(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
// 	private long execTimeMillis = 0L;
// 	private long totalInsertCount = 0L;
// 	private long totalHistoryMoveSuccess = 0L;
// 	private long totalHistoryMoveFailure = 0L;

// 	// Hàm constructor
//     public Handler(String type, LinkedBlockingQueue<StructureAlarm> queue) {
//         try {
//             this.type = type;
//             this.queue = queue;

// 			// insert Alarm
//             insertConnection = DbUtil.takeConnection();
//             insertStmt = insertConnection.prepareStatement(insertSQL);

// 			// Delete and Move Alarm
// 			historyConnection = DbUtil.takeConnection();
// 			delStmt = historyConnection.prepareStatement(deleteSQL);
// 			moveStmt = historyConnection.prepareStatement(moveSQL);
// 			historyConnection.setAutoCommit(false); // batch mode

//         } catch (SQLException e) {
//             logger.error(e, e);
//         }
// 	}

// 	// Hàm insert alarm
//     private void insertAlarm(StructureAlarm structAlarm) {
// 		try {
// 			insertStmt.setString(1, structAlarm.nbiAlarmId);
// 			insertStmt.setString(2, structAlarm.ne);
			
// 			insertStmt.setString(3, structAlarm.nbiAlarmType);
// 			insertStmt.setString(4, structAlarm.nbiPerceivedSeverity);
// 			insertStmt.setString(5, structAlarm.nbiSpecificProblem);
// 			insertStmt.setString(6, structAlarm.nbiAdditionalText);
// 			insertStmt.setString(7, structAlarm.nbiObjectInstance);

// 			insertStmt.setString(8, structAlarm.cellid);
// 			insertStmt.setString(9, structAlarm.site);
// 			insertStmt.setString(10, structAlarm.neType);
// 			insertStmt.setString(11, structAlarm.ipAddress);

// 			insertStmt.setString(12, structAlarm.nbiClearTime);
// 			insertStmt.setString(13, structAlarm.nbiAlarmTime);
// 			insertStmt.setString(14, structAlarm.recordType);

// 			insertStmt.setString(15, structAlarm.network);
// 			insertStmt.setString(16, structAlarm.region);
// 			insertStmt.setString(17, structAlarm.province);
// 			insertStmt.setString(18, structAlarm.district);
// 			insertStmt.setString(19, structAlarm.dept);
// 			insertStmt.setString(20, structAlarm.team);



// 			// if (structAlarm.nbiClearTime!=null && !structAlarm.nbiClearTime.isEmpty()) {
// 			// 	insertStmt.setTimestamp(11, new Timestamp(sdf.parse(structAlarm.nbiClearTime).getTime()));
// 			// } else {
// 			// 	insertStmt.setNull(11, java.sql.Types.TIMESTAMP);	
// 			// }

// 			// if (structAlarm.nbiAlarmTime!=null && !structAlarm.nbiAlarmTime.isEmpty()) {
// 			// 	insertStmt.setTimestamp(12, new Timestamp(sdf.parse(structAlarm.nbiAlarmTime).getTime()));
// 			// } else {
// 			// 	insertStmt.setNull(12, java.sql.Types.TIMESTAMP);
// 			// }
			
			
// 			insertStmt.setString(21, structAlarm.isSendSms);
// 			insertStmt.setString(22, structAlarm.tgNhan);

// 			// Execute to insert
// 			insertStmt.executeUpdate();

// 		// } catch (ParseException e) {
// 		// 	logger.error("Lỗi parse thời gian: " + e);
// 		} catch (Exception e) {
// 			logger.error("Lỗi khi INSERT ALARM vào DB: " + e);
// 		}
// 	}

// 	// Hàm move alarm từ active sang history
// 	private void moveToHistoryBatch(StructureAlarm alarm) {
// 		try {
// 			System.out.println("Di chuyển Alarm sang bảng alarm_history");
			
// 			moveStmt.setString(1, alarm.nbiClearTime);     
// 			moveStmt.setString(2, alarm.nbiAlarmId);    
// 			moveStmt.setString(3, alarm.ne);   

// 			delStmt.setString(1, alarm.nbiAlarmId);
// 			delStmt.setString(2, alarm.ne);

// 			// Lưu lại thời điểm ghi nhận stmt đầu tiên
// 			if (historyBatchStart == 0L) {
// 				historyBatchStart = System.currentTimeMillis();
// 				execTimeMillis = historyBatchStart;
// 			}

// 			if (historyBatchCounter < MAX_BATCH_SIZE) {
// 				moveStmt.addBatch();
// 				delStmt.addBatch();
// 			}
// 			historyBatchCounter++;

// 			long elapsedSeconds = (System.currentTimeMillis() - historyBatchStart) / 1000;
// 			boolean reachedBatchSize = (historyBatchCounter == MAX_BATCH_SIZE);
// 			boolean reachedTimeout = (elapsedSeconds >= MAX_WAIT_TIME) && (historyBatchCounter >= 1);

// 			if (reachedBatchSize || reachedTimeout) {
// 				logger.info("Executing batch: size=" + historyBatchCounter + ", timeout=" + reachedTimeout + 
// 						   ", elapsed=" + elapsedSeconds + "s");
// 				execMoveToHistoryBatch();
// 			} 
// 		} catch (SQLException e) {
// 			logger.error("Lỗi class moveToHistoryBatch: " + e);
// 		}
// 	}

// 	// Hàm triển khai delete và move
// 	private void execMoveToHistoryBatch() {
// 		long batchStartTime = System.currentTimeMillis();
// 		int insertedCount = 0;
// 		int deletedCount = 0;
// 		int failedCount = 0;

// 		try {
// 			System.out.println("EXECUTING BATCH: Moving " + historyBatchCounter + " alarms to HISTORY...");
			
// 			// Execute INSERT batch (copy to HISTORY)
// 			int[] insertResults = moveStmt.executeBatch();
			
// 			// Count successful inserts
// 			for (int result : insertResults) {
// 				if (result > 0 || result == PreparedStatement.SUCCESS_NO_INFO) {
// 					insertedCount++;
// 				} else if (result == PreparedStatement.EXECUTE_FAILED) {
// 					failedCount++;
// 				}
// 			}
			
// 			logger.info("Moving executed: success=" + insertedCount + ", failed=" + failedCount);
			
// 			// Execute DELETE batch (remove from ACTIVE)
// 			int[] deleteResults = delStmt.executeBatch();
			
// 			// Count successful deletes
// 			for (int result : deleteResults) {
// 				if (result > 0 || result == PreparedStatement.SUCCESS_NO_INFO) {
// 					deletedCount++;
// 				}
// 			}
			
// 			logger.info("DELETE batch executed: deleted=" + deletedCount);
			
// 			// Commit (cả insert và delete)
// 			historyConnection.commit();
			
// 			// Update statistics
// 			totalHistoryMoveSuccess += insertedCount;
// 			if (failedCount > 0) {
// 				totalHistoryMoveFailure += failedCount;
// 			}
			
// 			// Calculate execution time
// 			long batchExecTime = System.currentTimeMillis() - batchStartTime;
// 			long totalExecTime = System.currentTimeMillis() - execTimeMillis;
			
// 			// Log detailed results
// 			logger.error(
// 				"\n========================================" +
// 				"\n[BATCH MOVE TO HISTORY - " + type + "]" +
// 				"\n========================================" +
// 				"\n  ✓ Inserted to HISTORY: " + insertedCount +
// 				"\n  ✓ Deleted from ACTIVE: " + deletedCount +
// 				"\n  ✗ Failed operations: " + failedCount +
// 				"\n  ⏱ Batch execution time: " + batchExecTime / 1000 + "." + batchExecTime % 1000 + " secs" +
// 				"\n  ⏱ Total processing time: " + totalExecTime / 1000 + "." + totalExecTime % 1000 + " secs" +
// 				"\n  📊 Total moved (cumulative): " + totalHistoryMoveSuccess +
// 				"\n  📊 Total failed (cumulative): " + totalHistoryMoveFailure +
// 				"\n  📬 Queue remaining: " + queue.size() +
// 				"\n========================================\n"
// 			);
			
// 			// Clear batch
// 			moveStmt.clearBatch();
// 			delStmt.clearBatch();
			
// 			// Reset counters and timers
// 			historyBatchCounter = 0;
// 			historyBatchStart= 0L; 
// 			execTimeMillis = 0L;
			
// 		} catch (SQLException e) {
// 			logger.error("ERROR executing move to history batch: " + e.getMessage(), e);
// 		}
// 	}

// 	public void run() {
// 		long execOneStmt = 0L;
// 		long startTime = 0l;

// 		while(true) {
// 			StructureAlarm alarm = new StructureAlarm();
// 			try {
// 				alarm = queue.take();
// 				logger.debug(type + " queue remaining: " + queue.size());
// 				alarm = AlarmInfo.setAlarmInfo(alarm);
// 				logger.info("Processing alarm: " + alarm.toString());

// 				// Bỏ qua alarm nếu network là 2G
// 				if ("2G".equals(alarm.network)) {
// 					continue;
// 				}

// 				if (alarm.recordType.equals("END")
// 						&& !alarm.nbiSpecificProblem.startsWith("8502")
// 						&& !alarm.nbiSpecificProblem.startsWith("9047")) {
// 					logger.debug("Moving to history: " + alarm);
// 					moveToHistoryBatch(alarm);
// 				} else {
// 					startTime = System.currentTimeMillis();
// 					insertAlarm(alarm);
// 					execOneStmt += (System.currentTimeMillis() - startTime);
// 					if (++count >= 50) {
// 						logger.error(
// 							"INSERT PROCESSED ==> " + type + ": " + count + " ALARMS" +
// 							" / TOTAL => " + (totalInsertCount += count) +
// 							"\nREMAINED IN QUEUE => " + queue.size() + "\n"
// 						);
// 					}
// 					count = 0;
// 					execOneStmt = 0;
// 				}
// 			} catch (Exception e) {
// 				logger.error("Lỗi class run(): " + e);
// 			}
// 		}
// 	}

// 	public void cleanUpHandler() {
// 		try {
// 			if (historyBatchCounter > 0) {
// 				System.out.println("Triển khai BATCH còn lại trước khi cleanup: " + historyBatchCounter);
// 				execMoveToHistoryBatch();
// 			}

// 			// Đóng Stmt
// 			if (insertStmt != null) insertStmt.close();
// 			if (delStmt != null) delStmt.close();
// 			if (moveStmt != null) moveStmt.close();

// 			// Đóng connection
// 			if (insertConnection != null) insertConnection.close();
// 			if (historyConnection != null) historyConnection.close();

// 			System.out.println("CLEAN UP HANDLER SUCCESSFULLY!!");
// 		} catch (SQLException e) {
// 			logger.error("ERROR DURING CLEAN UP: " + e);
// 		}
// 	}


// }
