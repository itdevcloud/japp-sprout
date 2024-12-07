package com.itdevcloud.japp.se.common.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class CommonLogFormatter extends Formatter {
	// ANSI escape code
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    
	// Here you can configure the format of the output and
	// its color by using the ANSI escape codes defined above.

	// format is called for every console log message
	@Override
	public String format(LogRecord record) {
		// This example will print date/time, class, and log level in yellow,
		// followed by the log message and it's parameters in white .
		StringBuilder builder = new StringBuilder();
		
		//set max length of level name in log file to 6
		String logLevelName = record.getLevel().getName();
		logLevelName = Level.WARNING.getName().equalsIgnoreCase(logLevelName)?"WARN":logLevelName;
		
		//level
		String color = getColor(record.getLevel());
		builder.append(color);
		builder.append("[");
		builder.append(StringUtil.appendCharToExtendLength(logLevelName, ' ', 6));
		builder.append("]");

		
		//date
		builder.append(" ");
		builder.append(calcDate(record.getMillis()));
		
		//source class
		String source = getSimpleClassName(record.getSourceClassName()) + "." + record.getSourceMethodName() + "()";
		builder.append(ANSI_BLACK);
		builder.append(" ");
		builder.append(source);
		// builder.append("]");

		//content
		builder.append(ANSI_BLACK);
		builder.append(" - ");
		builder.append(record.getMessage());

		//parameters
		Object[] params = record.getParameters();
		if (params != null) {
			builder.append(ANSI_CYAN);
			builder.append("\nParameters:");
			for (int i = 0; i < params.length; i++) {
				builder.append(params[i]);
				if (i < params.length - 1)
					builder.append(", ");
			}
		}
		//exception
		builder.append(ANSI_RED);
       if (record.getThrown() != null) {
			builder.append("\n" + CommonUtil.getStackTrace(record.getThrown()));
        }
		builder.append(ANSI_RESET);
		builder.append("\n");
		return builder.toString();
	}
	private String calcDate(long millisecs) {
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date resultdate = new Date(millisecs);
		return date_format.format(resultdate);
	}
	private String getColor(Level level) {
		String color = null;
		if (level == null) {
			color = ANSI_BLACK;
		} else if (Level.INFO.equals(level)) {
			color = ANSI_BLUE;
		} else if (Level.WARNING.equals(level)) {
			color = ANSI_YELLOW;
		} else if (Level.SEVERE.equals(level)) {
			color = ANSI_PURPLE;
		} else if (Level.CONFIG.equals(level)) {
			color = ANSI_CYAN;
		} else if (Level.FINER.equals(level)) {
			color = ANSI_CYAN;
		} else {
			color = ANSI_BLACK;
		}
		return color;
	}
	private String getSimpleClassName(String fullClassName) {
		String name = null;
		if (StringUtil.isEmptyOrNull(fullClassName)) {
			name = "UnknownClass";
		} else {
			int idx = fullClassName.lastIndexOf(".");
			if (idx != -1) {
				name = fullClassName.substring(idx+1);
			} else {
				name = fullClassName;
			}
		}
		return name;
	}

}