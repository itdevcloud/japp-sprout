package com.itdevcloud.japp.se.common.multiInstance.socket;

import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.service.ConfigurationManager;

public class MultiInstanceSocketSupportConstant {
	
	private static final CommonLogger logger = CommonLogger
			.getLogger(MultiInstanceSocketSupportConstant.class.getName());


	//**************************************************************
	//* constants below can not be overwritten by property file
	//**************************************************************
	public static String LOCK_STATUS_LOCKED = "LOCKED";
    public static String LOCK_STATUS_RELEASED = "RELEASED";
	public static String LOCK_STATUS_REJECTED = "REJECTED";

	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String BOUNDARY_LEFT = "[";
	public static final String BOUNDARY_RIGHT = "]";
	public static final String SEGMENT_SEPARATOR = "«»";

	// MUST NOT SAME AS SEGMENT SEPERATOR, can not be "|" - it means or '' for
	// splict()
	// type alt + 21 (numeric keypad only - right side number key pad)
	// public static final String CONTENT_SEPARATOR = "§○◄►";
	// alt+16, 17
	public static final String CONTENT_SEPARATOR = "◄►";

	public static final String TRANSFER_STATUS_SUCCESS = "SUCCESS";
	public static final String TRANSFER_STATUS_ERROR = "ERROR";
	
	public static final String RESPONSE_STATUS_SUCCESS = "RESPONSE_SUCCESS";
	public static final String RESPONSE_STATUS_ERROR = "RESPONSE_ERROR";

	public static final String PROCESS_STATUS_SUCCESS = "0000";
	public static final String PROCESS_STATUS_RECEIVED = "0001";
	public static final String PROCESS_STATUS_WARNING = "W001";
	public static final String PROCESS_STATUS_ERROR = "E001";
	public static final String PROCESS_STATUS_NA = "NA";

	public static final String TYPE_PING = "PING";
	public static final String TYPE_EVENT = "EVENT";
	public static final String TYPE_ADD_LOCK = "ADDLOCK";
	public static final String TYPE_RELEASE_LOCK = "RELEASE";
	public static final String TYPE_INQUIRY_EVENT_STATUS = "EVENT_STATUS_INQUIRY";

	public static final String TYPE_NA = "N/A";
	public static final String NAME_NA = "N/A";
	public static final String SOURCE_NA = "N/A";
	public static final String CONTENT_EMPTY = "";

	public static final String DIRECTION_INBOUND = "INBOUND";
	public static final String DIRECTION_OUTBOUND = "OUTBOUND";


	
	public static final int DEFAULT_RECEIVE_RETRY = 3; 
	public static final int RECEIVE_RETRY_WAIT = 1000; // 1 seconds
	
	//**************************************************************
	//* constants below can be changed by property file
	//**************************************************************
	
	public static int DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND = 30000; // 30 seconds
	public static int DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND = 15000; // 10 seconds
	public static int DEFAULT_SOCKET_START_PORT = 8800; 
	public static int DEFAULT_SOCKET_END_PORT = 8810; 
	public static int DEFAULT_SOCKET_PORT = 8800;
	public static int DEFAULT_SOCKET_TIMEOUT = 30000; // 30 seconds
	
	public static String MAIN_INSTANCE_FILE_NAME = "C:\\temp\\Main_Instance_File.txt";
	public static String MAIN_INSTANCE_LIVE_CHECK_FILE_NAME = "C:\\temp\\Main_Instance_live_Check.txt";
	public static String INSTANCE_lOCK_FILE_NAME = "C:\\temp\\Multiple_Instance_Lock_File.txt";

	public static int MESSAGE_STATUS_CACHE_RETENTION_DAY = 7; //7 DAYS
	
	static {
		init();
	}
	
	public static void init() {
		ConfigurationManager manager = new ConfigurationManager();
		String prefix = "multi.instance.support.socket.";
		
		DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND = manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND", 30000);
		DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND =  manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND", 15000);
		DEFAULT_SOCKET_START_PORT =  manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_START_PORT", 8800);
		DEFAULT_SOCKET_END_PORT = manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_END_PORT", 8810);
		DEFAULT_SOCKET_PORT = manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_PORT", 8800);
		DEFAULT_SOCKET_TIMEOUT = manager.getPropertyAsInt(prefix + "DEFAULT_SOCKET_TIMEOUT", 30000);
		
		MAIN_INSTANCE_FILE_NAME = manager.getPropertyAsString(prefix + "MAIN_INSTANCE_FILE_NAME", "C:\\Users\\Sun\\Downloads\\Main_Instance_File.txt");
		MAIN_INSTANCE_LIVE_CHECK_FILE_NAME = manager.getPropertyAsString(prefix + "MAIN_INSTANCE_LIVE_CHECK_FILE_NAME", "C:\\Users\\Sun\\Downloads\\Main_Instance_live_Check.txt");
		INSTANCE_lOCK_FILE_NAME = manager.getPropertyAsString(prefix + "INSTANCE_lOCK_FILE_NAME", "C:\\Users\\Sun\\Downloads\\Multiple_Instance_Lock_File.txt");

		MESSAGE_STATUS_CACHE_RETENTION_DAY = manager.getPropertyAsInt(prefix + "MESSAGE_STATUS_CACHE_RETENTION_DAY", 7);

		String str = "Changable Constant Values = \n" +
		"DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND = " + DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND + "\n" +
		"DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND = " + DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND + "\n" +
		"DEFAULT_SOCKET_START_PORT = " + DEFAULT_SOCKET_START_PORT + "\n" +
		"DEFAULT_SOCKET_END_PORT = " + DEFAULT_SOCKET_END_PORT + "\n" +
		"DEFAULT_SOCKET_PORT = " + DEFAULT_SOCKET_PORT + "\n" +
		"DEFAULT_SOCKET_TIMEOUT = " + DEFAULT_SOCKET_TIMEOUT + "\n" +
		"MAIN_INSTANCE_FILE_NAME = " + MAIN_INSTANCE_FILE_NAME + "\n" +
		"MAIN_INSTANCE_LIVE_CHECK_FILE_NAME = " + MAIN_INSTANCE_LIVE_CHECK_FILE_NAME + "\n" +
		"INSTANCE_lOCK_FILE_NAME = " + INSTANCE_lOCK_FILE_NAME + "\n" +
		"MESSAGE_STATUS_CACHE_RETENTION_DAY = " + MESSAGE_STATUS_CACHE_RETENTION_DAY ;

		logger.info(str);
	}
}
