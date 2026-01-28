package com.victor.trap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import com.victor.app.AppConfig;
import com.victor.app.AppSetting;
import com.victor.model.StructureAlarm;
import com.victor.alarm.AlarmInfo;
import com.victor.kafka.producer.KafkaProducerConfig;
import com.victor.common.Global;

public class TrapReceiver implements CommandResponder{
    private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool; 
	// final static Logger logger = Logger.getLogger(SNMPTrapReceiver.class); 
	final static SimpleDateFormat dff = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
	private String port = "1234";
	static TrapProbe snmpProbe = null;
	
	// Hàm khởi tạo (Probe được tạo ngay sau khi khởi tạo Receiver)
	public TrapReceiver() throws FileNotFoundException, IOException {
		snmpProbe = new TrapProbe();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		AppConfig.loadOIDMap();  	
		KafkaProducerConfig.loadKafkaProducerConfig();
		TrapReceiver receiver = new TrapReceiver();
		receiver.runReceiver();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("DỪNG Trap Receiver!");
        }));
	
		TrapProbe.main(new String[0]);
	}

	// Hàm chạy receiver
	private void runReceiver() {
		try {
			initListener(); 
			snmp.addCommandResponder(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Hàm khởi tạo Listener để nhận bản tin
	private void initListener() throws UnknownHostException, IOException {
		port = Global.OID_CONFIG.getProperty(AppSetting.SNMP_PORT); // port 1234
		threadPool = ThreadPool.create("Trap", 10);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", "udp:0.0.0.0/"+port));
		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
		} else {
			transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
		}
		USM usm = new USM(
				SecurityProtocols.getInstance().addDefaultProtocols(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		usm.setEngineDiscoveryEnabled(true);

		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
		SecurityModels.getInstance().addSecurityModel(usm);
		
		OID authOID = AuthMD5.ID;
		if (Global.OID_CONFIG.getProperty(AppSetting.SNMP_SECURITY_NAME) != null
			&& Global.OID_CONFIG.getProperty(AppSetting.SNMP_AUTHENTICATION_PROTOCOL).equals("SHA"))
		    authOID = AuthSHA.ID;
			OID privOID = PrivAES128.ID;
		if (Global.OID_CONFIG.getProperty(AppSetting.SNMP_SECURITY_NAME) != null
			&& Global.OID_CONFIG.getProperty(AppSetting.SNMP_PRIVACY_PROTOCOL).equals("3DES"))
		    privOID = Priv3DES.ID;
		
		snmp.getUSM().addUser(
				new OctetString(Global.OID_CONFIG.getProperty(AppSetting.SNMP_SECURITY_NAME)),
				new UsmUser(new OctetString(Global.OID_CONFIG.getProperty(AppSetting.SNMP_SECURITY_NAME)),
					authOID, new OctetString(Global.OID_CONFIG.getProperty(AppSetting.SNMP_AUTHENTICATION_PASSPHRASE)), privOID,
					new OctetString(Global.OID_CONFIG.getProperty(AppSetting.SNMP_PRIVACY_PASSPHRASE))));
		
		System.out.println("TrapReceiver lắng nghe cổng: "+port+" ... ");
		snmp.listen(); 
	}


	@Override
	public void processPdu(CommandResponderEvent event) {
		// String ipAddress =  event.getPeerAddress().toString();
		StructureAlarm data = new StructureAlarm();
		MappingOID mapping = new MappingOID();
		List<? extends VariableBinding> varBinds = event.getPDU().getVariableBindings();

		if (varBinds != null && !varBinds.isEmpty()) {
			Iterator<? extends VariableBinding> varIter = varBinds.iterator();
			while (varIter.hasNext()) {
				VariableBinding var = varIter.next(); 
				mapping.setData(var.getOid().toString(), data, var.toValueString().replace(";", ",").replace("\n", ""));
			} 
				//set thoi gian nhan
				data.tgNhan = dff.format(new Date());
			
				//set network, recordType, security
				data = AlarmInfo.classifyAlarm(data); 
				if(!data.recordType.equals("ACK")) {
					TrapProbe.addProbeDataQueue(data);
				}
				
		}
	}
}
