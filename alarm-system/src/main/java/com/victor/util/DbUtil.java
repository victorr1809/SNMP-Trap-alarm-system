package com.victor.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Properties;

import org.apache.log4j.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.victor.app.AppConfig;
import com.victor.model.Cell2G;
import com.victor.model.CellN3G;
import com.victor.model.CellN4G;
import com.victor.model.HCoreN;
import com.victor.model.HProvinceCode;
import com.victor.alarm.AlarmType;

public class DbUtil {
    final static Logger logger = Logger.getLogger(DbUtil.class);
    private static HikariDataSource dataSource = null;
    private static int queryTimeout = 30; // giây

    public static List<Cell2G> BTS = new ArrayList<>();
    public static List<CellN3G> BTS_3G = new ArrayList<>();
    public static List<HCoreN> CORE_LIST = new ArrayList<>();
    public static List<CellN4G> CELL_4G = new ArrayList<>();

    public static ConcurrentMap<String, Cell2G> BSCID = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, CellN3G> RNCID = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, CellN4G> NODE4GID = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, HProvinceCode> PROVINCE_BY_NE = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, String> H_MAP_IP_NODE_CISCO = new ConcurrentHashMap<>();

    public static List<AlarmType> ALARM_CONFIG = new ArrayList<>();
    public static List<HProvinceCode> PROVINCE_BY_CODE = new ArrayList<>();

    // Kết nối DB (chạy ngay khi gọi tới class DbUtil)
    static {
        try {
            Properties p = AppConfig.loadDBConnectInfo();
            HikariConfig cfg = new HikariConfig();

            String jdbcUrl = p.getProperty("jdbcUrl");
            String username = p.getProperty("username");
            String password = p.getProperty("password");
            String driverClassName = p.getProperty("driverClassName");

            cfg.setJdbcUrl(jdbcUrl.trim());
            cfg.setUsername(username.trim());
            cfg.setPassword(password);
            cfg.setDriverClassName(driverClassName.trim());

            cfg.setPoolName("alarm-db-pool");
            cfg.setMaximumPoolSize(20);
            cfg.setMinimumIdle(5);
            cfg.setConnectionTimeout(5000);
            cfg.setIdleTimeout(600_000);      // 10 phút
            cfg.setMaxLifetime(600_000);    // 30 phút

            dataSource = new HikariDataSource(cfg);
            System.out.println("Hikari Connection Pool khởi tạo thành công");
        } catch (Exception e) {
            System.err.println("[LỖI] Không tạo được Connection Pool" + e.getMessage());
        }
        // Lên lịch reset (sau)
    }
    
    // Hàm lấy Connection từ Connection Pool
    public static Connection takeConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void initLoad() {
        loadCELL_4G();

        loadNODE4GID();

        loadCOREID();

        loadBTS_3G();

        loadRNCID();

        loadProvinceByCode();

        loadProvinceByNe();

    }

    // ---- Các hàm LOAD ----
    
    private static void loadProvinceByCode() {
        String mSQL = "select code, region, province, district, dept, team from mapping.h_provinces_code";

        try (Connection conn = DbUtil.takeConnection(); 
                Statement stmt = conn.createStatement(); 
                ResultSet rs = stmt.executeQuery(mSQL)) {
            
            stmt.setQueryTimeout(queryTimeout);
            if(rs != null) {
                PROVINCE_BY_CODE.clear();

                HProvinceCode hProvinceCode = null;
                while(rs.next()) {
                    hProvinceCode = new HProvinceCode(
                            rs.getString("code"), 
                            rs.getString("region"), 
                            rs.getString("province"), 
							rs.getString("district"), 
                            rs.getString("dept"), 
                            rs.getString("team")
                        );
                    PROVINCE_BY_CODE.add(hProvinceCode);
                }
                System.out.println("Load thành công PROVINCE_BY_CODE");
            }
        } catch(Exception e) {
            System.err.println("Không load được PROVINCE_BY_CODE:" + e);
        }
    }

    private static void loadCELL_4G() {
        String mSQL = "select nodeid, cellid, nodename, cellname from mapping.dimran4gcell where vendor='NOKIA SIEMENS' order by DATEADDED desc";

        try (Connection conn = DbUtil.takeConnection(); 
                Statement stmt = conn.createStatement(); 
                ResultSet rs = stmt.executeQuery(mSQL)) {

            stmt.setQueryTimeout(queryTimeout);
            if (rs != null) {
                CELL_4G.clear();
                CellN4G cell4G = null;
                while(rs.next()) {
					cell4G = new  CellN4G(rs.getString("NODEID"), rs.getString("CELLID"), rs.getString("NODENAME"), rs.getString("CELLNAME"));
					CELL_4G.add(cell4G);
				}
            }
            System.out.println("Load thành công CELL_4G");
        } catch (Exception e) {
            System.err.println("Không load được CELL_4G:" + e);
        }
    }

    private static void loadCOREID() {
        String mSQL = "select region, ne_type, ne, node_id, dept, team, vendor from mapping.h_core_n";
        try (Connection conn = DbUtil.takeConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(mSQL)) {
            stmt.setQueryTimeout(queryTimeout);
			
			if(rs != null) {
				// clear
				CORE_LIST.clear();
				
				HCoreN hCoreN = null;
				while(rs.next()) {
					hCoreN = new HCoreN(rs.getString("REGION"), rs.getString("NE_TYPE"), rs.getString("NE"), rs.getString("NODE_ID"), 
							rs.getString("DEPT"), rs.getString("TEAM"), rs.getString("VENDOR"));
					CORE_LIST.add(hCoreN);
				}
            }
            System.out.println("Load thành công COREID");
        } catch (Exception e) {
            System.err.println("Không load được COREID: " + e);
        }
    }

    private static void loadBTS_3G() {
        String mSQL = "SELECT substring(t.objectname FROM 1 FOR position('/WCEL' IN t.objectname) - 1) AS bts_objectname, t.objectname, t.rncname, t.wbtsname, t.cellname \r\n"
                + "from mapping.dimran3g t\r\n" 
                + "ORDER BY t.dateadded DESC;";
        try (Connection conn = DbUtil.takeConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(mSQL)) {
            stmt.setQueryTimeout(queryTimeout);
        
            if(rs != null ){
                BTS_3G.clear();
				
				CellN3G cellN3G = null;
				while(rs.next()) {
					cellN3G = new CellN3G(rs.getString("bts_objectname"), rs.getString("objectname"), null,
							rs.getString("rncname"), rs.getString("wbtsname"),
							rs.getString("cellname"));
					BTS_3G.add(cellN3G);
				}
            }
            System.out.println("Load thành công BTS 3G");
        } catch (Exception e) {
            System.err.println("Không load được BTS 3G");
        }
    }

    private static void loadNODE4GID() {
        String sql = "select nodeid, nodename from (\r\n"
				+ "    select nodeid, nodename, max(dateadded) dateadded \r\n"
				+ "    from mapping.dimran4gcell where vendor='NOKIA SIEMENS'\r\n"
				+ "    group by nodeid, nodename\r\n"
				+ ") order by dateadded desc";
                
        try (Connection conn = DbUtil.takeConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            
            stmt.setQueryTimeout(queryTimeout);
            if(rs != null) {
                NODE4GID.clear();

                CellN4G cellN4G = null;
                while (rs.next()) {
                    cellN4G = new CellN4G(rs.getString("NODEID"), null, rs.getString("NODENAME"), null);
                    NODE4GID.putIfAbsent(cellN4G.getNodeId(), cellN4G);
                }
            }
            System.out.println("Load thành công NODE4GID");
        } catch (Exception e) {
            System.err.println("Không load được NODE4GID");
        }
    }

    private static void loadProvinceByNe() {
		String sql = "select code, region, province, district, dept, team from (" + 
				"select bscid code, region, province, location_name district, dept, team, vendor FROM mapping.h_bsc " + 
				"UNION ALL " + 
				"select bscid code, region, province, location_name district, dept, team, vendor FROM mapping.h_bsc_3g " + 
				") " + 
				"where vendor='NOKIA SIEMENS'";
        
        try (Connection conn = DbUtil.takeConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            stmt.setQueryTimeout(queryTimeout);
            if (rs != null) {
                PROVINCE_BY_NE.clear();

                HProvinceCode hProvinceCode = null;
                while(rs.next()) {
                    hProvinceCode = new HProvinceCode(rs.getString("code"), rs.getString("region"), rs.getString("province"), 
							rs.getString("district"), rs.getString("dept"), rs.getString("team"));
					
					PROVINCE_BY_NE.putIfAbsent(hProvinceCode.getCode(), hProvinceCode);
                }
            }
            System.out.println("Load thành công PROVINCE_BY_NE");
        } catch (Exception e) {
            System.err.println("Không load được PROVINCE_BY_NE " + e);
        }
    }

    private static void loadRNCID() {
		String sql = "select rncid, rncname from (\r\n"
				+ "    SELECT split_part(objectname, '/WBTS', 1) AS rncid, rncname, max(dateadded) AS dateadded \r\n"
				+ "    FROM mapping.dimran3g \r\n"
				+ "    WHERE objectname LIKE '%/WBTS%' \r\n"
                + "    GROUP BY split_part(objectname, '/WBTS', 1), rncname \r\n"
				+ ") order by dateadded desc";
        try (Connection conn = DbUtil.takeConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            stmt.setQueryTimeout(queryTimeout);
            if (rs != null) {
                RNCID.clear();
                CellN3G cellN3G = null;
                while (rs.next()) {
					cellN3G = new CellN3G(null, null, rs.getString("rncid"),
							rs.getString("rncname"), null, null);
					RNCID.putIfAbsent(cellN3G.getRncid(), cellN3G);                    
                }
            }
            System.out.println("Load thành công RNCID");
        } catch (Exception e) {
            System.err.println("Không load được RNCID " + e);
        }
    }
}
