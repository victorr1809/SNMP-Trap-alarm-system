package com.producer.trap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;

public class TrapSender extends Thread {
    // Điều chỉnh tốc độ ở đây
    private static final int TARGET_MPS = 500;

    private static final String community = "public";
    private static final String ipAddressReceiver = "127.0.0.1";
	private static final int port = 1234;

    // Thay đổi path ở đây
	private static String logFilePath = "/Users/manh/Documents/ĐA2 OSS/data đồ án/log_20251206_16.txt";
    private static Pattern pattern = Pattern.compile("peerAddress=([0-9\\.]+)/(\\d+)");

    public static void main(String[] args) {
        System.out.println(logFilePath);
        sendAllTrap(logFilePath);
    }

    // Hàm lặp từng dòng trong file và gửi đi
    private static void sendAllTrap(String logFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath, StandardCharsets.UTF_8))) {
            // Mở UDP socket
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            transport.listen();
            Snmp snmp = new Snmp(transport);

            // Tạo target (điểm đích)
            CommunityTarget comtarget = new CommunityTarget();      
            comtarget.setCommunity(new OctetString(community));     
            comtarget.setVersion(SnmpConstants.version2c);
            comtarget.setAddress(new UdpAddress(ipAddressReceiver + "/" + port));       // IP và port của Trap Receiver
            comtarget.setRetries(2);
            comtarget.setTimeout(5000);

            String line;
            long startTime = System.currentTimeMillis();
            int count  = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {continue;}
                PDU pdu = createPduFromLogLine(line, SnmpConstants.version2c);
                if (pdu == null) {continue;}
                try {
                    snmp.send(pdu, comtarget);
                    count++;
                    if(count % 500 == 0) {
                        System.out.println("Đã gửi " + count + " bản tin tới " + ipAddressReceiver + ":" + port);
                    }
                    // --- Logic điều chỉnh thời gian ---
                    if (TARGET_MPS > 0) {
                        long expectedTime = (long) ((count * 1000.0) / TARGET_MPS);
                        long actualTime = System.currentTimeMillis() - startTime;
                        
                        // Nếu gửi quá nhanh (thực tế < lý thuyết), thì ngủ bù khoảng chênh lệch
                        if (expectedTime > actualTime) {
                            Thread.sleep(expectedTime - actualTime);
                        }
                    }    
                } catch (Exception e) {
                    System.err.println("Lỗi khi gửi Trap: " + e.getMessage());
                }
            }
            // --- Đã gửi xong ---
            long endTime = System.currentTimeMillis();
            System.out.println("Tổng số bản tin: " + count);
            System.out.println("Tổng thời gian: " + (endTime - startTime) / 1000.0 + " giây");

            // --- Đóng cổng ---
            snmp.close();
            transport.close();

        } catch (IOException e) {  
            System.err.println("Lỗi: " + e.getMessage());
        }
    }
    
    // Hàm tạo PDU từ dòng log
    private static PDU createPduFromLogLine(String line, int version) {
        if(!line.contains("pdu=TRAP")) {
            return null;
        }
        // Lọc ra IP máy gửi
        String ipAddressOrigin = null;
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ipAddressOrigin = matcher.group(1);
        }

        // Tạo PDU
        // PDU pdu = new PDU();
        PDU pdu = DefaultPDUFactory.createPDU(version);
        if (version == SnmpConstants.version1) {pdu.setType(PDU.V1TRAP);}
        if (version == SnmpConstants.version2c){pdu.setType(PDU.TRAP);}

        // Tìm khối VBS
        int vbsStart = line.indexOf("VBS[");
        int vbsEnd = line.indexOf("]]", vbsStart);

        if (vbsStart == -1 || vbsEnd == -1) {
            return null;
        }
        
        // Lấy nội dung khối VBS
        String vbsContent = line.substring(vbsStart + 4, vbsEnd);
        // Split theo "; " để tách các cặp OID=Value
        String[] bindings = vbsContent.split(";\\s*");

        for(String binding : bindings) {
            binding = binding.trim();
            if (binding.isEmpty()){
                continue;
            }

            // Tìm vị trí dấu "=" đầu tiên
            int equalIndex = binding.indexOf(" = ");
            if (equalIndex == -1) {
                continue;
            }

            // Tách OID và Value
            String oidStr = binding.substring(0, equalIndex).trim();
            String value = binding.substring(equalIndex + 3).trim();
            try {
                OID oid = new OID(oidStr);
                pdu.add(new VariableBinding(oid, new OctetString(value)));
            } catch (Exception e) {
                System.err.println("Lỗi khi parsing ở: " + oidStr);
            }
        }
        if (pdu.size() == 0){return null;}
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.28458.1.26.2.1.6.25"), new OctetString(ipAddressOrigin)));
        // printPDU(pdu);
        return pdu;
    }
    
}

    // @Override
    // public void run() {
    //     PDU pdu = createSamplePdu(SnmpConstants.version2c);
    //     sendV1orV2Trap(pdu, SnmpConstants.version2c, community, ipAddress, port);
    // }
    
