package com.itdevcloud.japp.se.common.multiInstance.v1.file;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.service.ConfigurationManager;

public class MultiInstanceFileSupportConstant {
	
	private static final JulLogger logger = JulLogger
			.getLogger(MultiInstanceFileSupportConstant.class.getName());


	//**************************************************************
	//* constants below can not be overwritten by property file
	//**************************************************************

	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	// MUST NOT SAME AS SEGMENT SEPERATOR, can not be "|" - it means or '' for
	// splict()
	// type alt + 21 (numeric keypad only - right side number key pad)
	// public static final String CONTENT_SEPARATOR = "§○◄►";
	// alt+16, 17
	public static final String CONTENT_SEPARATOR = "◄►";

	
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
		ConfigurationManager manager = ConfigurationManager.getInstance();
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
