package com.victor.util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.victor.model.HProvinceCode;


public class DbUtilTest {
    // public static void main(String[] args) {
    //     try (Connection conn = DbUtil.takeConnection()) {

    //         System.out.println("✅ Lấy connection thành công");
    //         System.out.println("AutoCommit = " + conn.getAutoCommit());

    //         // Test query đơn giản
    //         String sql = "SELECT objectname FROM mapping.dimran3g";
    //         try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
    //             ResultSetMetaData meta = rs.getMetaData();
    //             int colCount = meta.getColumnCount();
    //             while (rs.next()) {
    //                 for (int i = 1; i <= colCount; i++) {
    //                     System.out.print(meta.getColumnName(i) + "=" + rs.getObject(i) + " | ");
    //                 }
    //                 System.out.println();
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.err.println("❌ Test DbUtil FAILED");
    //         e.printStackTrace();
    //     }
    // }

    public static void main(String[] args) {
        try {
            DbUtil.initLoad();
            System.out.println("Kích cỡ list: " + DbUtil.BTS_3G.size());
            DbUtil.BTS_3G.forEach(p ->
                System.out.println(p.getCellname())
            );

        } catch (Exception e) {
            System.err.println("Lỗi: " + e);
        }
    }
}
