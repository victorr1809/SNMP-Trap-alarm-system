package com.consumer.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.consumer.util.DbUtil;

public class TestDB {

    private static Connection connection;
    private static PreparedStatement connectionStmt; 
    private static String insertSQL = "CALL alarm.insert_alarm_all(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static String updateSQL = "CALL alarm.update_alarm_all(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static void main(String[] args) {
        try {
            connection = DbUtil.takeConnection();
            connectionStmt = connection.prepareStatement(insertSQL);

            connectionStmt.setString(1,  "CELL_TEST_004");
            connectionStmt.setString(2,  "ALARM_TYPE_TEST");
            connectionStmt.setString(3,  "MAJOR");
            connectionStmt.setString(4,  "SPECIFIC_PROBLEM_TEST");
            connectionStmt.setString(5,  "SITE_TEST");
            connectionStmt.setString(6,  "NE_TEST");
            connectionStmt.setString(7,  "ADDITIONAL_TEXT_TEST");
            connectionStmt.setString(8,  "RECORD_TEST");
            connectionStmt.setString(9,  "ALARMID_TEST_001");
            connectionStmt.setString(10, "NETYPE_TEST");

            connectionStmt.setString(11, "OBJECT/INSTANCE/TEST");

            // timestamp (để string theo format Postgres dễ cast)
            connectionStmt.setString(12,"2025-12-12 10:00:00");
            connectionStmt.setString(13,"2025-12-12 09:59:00");

            // boolean (để string "true"/"false" cho Postgres cast được)
            connectionStmt.setString(14, "true");
            connectionStmt.setString(15, "true");

            connectionStmt.setString(16, "4G");
            connectionStmt.setString(17, "REGION_TEST");
            connectionStmt.setString(18, "PROVINCE_TEST");
            connectionStmt.setString(19, "DISTRICT_TEST");
            connectionStmt.setString(20, "DEPT_TEST");
            connectionStmt.setString(21, "TEAM_TEST");

            connectionStmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Lỗi: " + e);
        }
        

    }
}
