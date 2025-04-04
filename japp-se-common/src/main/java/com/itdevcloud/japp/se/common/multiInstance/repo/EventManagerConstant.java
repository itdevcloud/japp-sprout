package com.itdevcloud.japp.se.common.multiInstance.repo;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.service.ConfigurationManager;

public class EventManagerConstant {
	
	private static final JulLogger logger = JulLogger
			.getLogger(EventManagerConstant.class.getName());


	//**************************************************************
	//* constants below can not be overwritten by property file
	//**************************************************************

	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String ID_DATE_FORMAT = "yyMMddHHmmssSSS";

	public static final String EVENT_PROCESS_STATUS_SUCCESS = "SUCCESS";
	public static final String EVENT_PROCESS_STATUS_FAIL = "FAIL";

	public static final String EVENT_NAME_LOCK = "LOCK";
	public static final String EVENT_NAME_REPO_LOCK = "REPO_LOCK";

	//public static final String EVENT_TYPE_REPO_LOCK = "REPO_LOCK_EVENT";
	public static final String EVENT_TYPE_LOCK = "LOCK_EVENT";
	public static final String EVENT_TYPE_PROCESS_STATUS = "PROCESS_STATUS_EVENT";
	public static final String EVENT_TYPE_BROADCAST = "BROADCAST_EVENT";
	public static final String EVENT_TYPE_DATA = "DATA_";
	
	public static final String EVENT_INFO_EMPTY_CONTENT = " ";

	public static final int DEFAULT_EVENT_UID_RANDOM_LENGTH = 5;
	public static final int DEFAULT_EVENT_EXPIRE_MINUTES = 5;
	public static final int DEFAULT_LOCK_CHECK_INTERVAL_MILLIS = 1000;
	public static final int DEFAULT_PROCESS_STATUS_RETENTION_DAY = 3;
	public static final int DEFAULT_REPO_LOCK_TIMEOUT_MILLIS = 20000;
	public static final int DEFAULT_REPO_LOCK_RETRY_INTERVAL_MILLIS = 500;
	public static final int DEFAULT_MONITOR_CHECK_INTERVAL_MILLIS = 5000;
	public static final int DEFAULT_PROCESSOR_RETRY_COUNT = 1;
	public static final int DEFAULT_PROCESSOR_RETRY_MAX_COUNT = 5;
	public static final int DEFAULT_PROCESSOR_RETRY_INTERVAL_MILLIS = 10000;
	public static final int DEFAULT_CONNECTION_RETRY_INTERVAL_MINS = 5;

	public static final String REPO_TYPE_REPO_LOCK = "REPO_TYPE_REPO_LOCK";
	public static final String REPO_TYPE_CONTENT = "REPO_TYPE_CONTENT";
	public static final String REPO_TYPE_UPDATE_TIME = "REPO_TYPE_UPDATE_TIME";

	
	// MUST NOT SAME AS SEGMENT SEPERATOR, can not be "|" - it means or '' for
	// split()
	// type alt + 21 (numeric keypad only - right side number key pad)
	// public static final String CONTENT_SEPARATOR = "§○◄►";
	// alt+16, 17
	public static final String CONTENT_SEPARATOR = "◄►";

	public static final String EVENT_INFO_STRING_FORMAT = "appName" + EventManagerConstant.CONTENT_SEPARATOR + 
			"uid" + EventManagerConstant.CONTENT_SEPARATOR +
			"name" + EventManagerConstant.CONTENT_SEPARATOR + 
			"source" + EventManagerConstant.CONTENT_SEPARATOR +
			"eventDate" +  EventManagerConstant.CONTENT_SEPARATOR + 
			"expiryDate" +  EventManagerConstant.CONTENT_SEPARATOR + 
			"content";

	public static final String EVENT_INFO_PROCESS_STATUS_STRING_FORMAT = EVENT_INFO_STRING_FORMAT +
			 EventManagerConstant.CONTENT_SEPARATOR + 
			"proccessedEventUid" + EventManagerConstant.CONTENT_SEPARATOR +
			"processor" + EventManagerConstant.CONTENT_SEPARATOR + 
			"processStatus" + EventManagerConstant.CONTENT_SEPARATOR +
			"processedDate";

	public static final String DATA_INFO_STRING_FORMAT = "appName" + EventManagerConstant.CONTENT_SEPARATOR + 
			"expiryDate" +  EventManagerConstant.CONTENT_SEPARATOR + 
			"content";

	//**************************************************************
	//* constants below can be changed by property file
	//**************************************************************
	public static String INSTANCE_lOCK_FILE_NAME = null;;
	public static String INSTANCE_EVENT_FILE_PATH = null;
	public static String INSTANCE_EVENT_FILE_NAME = null;
	public static String INSTANCE_EVENT_FILE_FULL_NAME = null;
	
	
	static {
		init();
	}
	
	public static void init() {
		ConfigurationManager manager = new ConfigurationManager();
		String prefix = "multi.instance.support.file.";
		
		INSTANCE_lOCK_FILE_NAME = manager.getPropertyAsString(prefix + "INSTANCE_lOCK_FILE_NAME", "C:\\temp\\test-lock.txt");
		INSTANCE_EVENT_FILE_PATH =  manager.getPropertyAsString(prefix + "INSTANCE_EVENT_FILE_PATH", "C:\\temp");
		INSTANCE_EVENT_FILE_NAME =  manager.getPropertyAsString(prefix + "INSTANCE_EVENT_FILE_NAME", "test-event.txt");
		INSTANCE_EVENT_FILE_FULL_NAME = INSTANCE_EVENT_FILE_PATH + "\\" + INSTANCE_EVENT_FILE_NAME;

		String str = "Changable Constant Values = \n" +
		"INSTANCE_lOCK_FILE_NAME = " + INSTANCE_lOCK_FILE_NAME + "\n" +
		"INSTANCE_EVENT_FILE_PATH = " + INSTANCE_EVENT_FILE_PATH + "\n" +
		"INSTANCE_EVENT_FILE_NAME = " + INSTANCE_EVENT_FILE_NAME + "\n" +
		"INSTANCE_EVENT_FILE_FULL_NAME = " + INSTANCE_EVENT_FILE_FULL_NAME  ;

		logger.info(str);
	}
}
