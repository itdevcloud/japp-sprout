package com.itdevcloud.japp.core.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class AppLogger {

	private static final String DEFAULT_JAVA_UTIL_LOGGER_PROPERTY_FILE_NAME = "javaUtilLogging-apicore-default.properties";
	
	private static boolean useLog4J = true;
	private static boolean inited = false;

	private JulLogger julLogger = null;
	private Logger log4JLogger = null;
	

	private AppLogger(Class clazz) {
		if(clazz == null) {
			clazz = this.getClass();
		}
		if(!inited) {
			init(null);
			inited = true;
		}
		if(useLog4J) {
			log4JLogger = LogManager.getLogger(clazz);
			//log4JLogger.info("-----------log4JLogger--------------" + clazz.getSimpleName());
			julLogger = null;
		}else {
			//use JulLogger -- Java Util Logger
			julLogger = JulLogger.getLogger(clazz.getName());
			//julLogger.info("-----------JulLogger--------------" + clazz.getSimpleName());
			log4JLogger = null;
		}
	}

	public synchronized static void init(String propertyFileName) {
		// must set before the Logger
		// loads logging.properties from the classpath
		if (StringUtil.isEmptyOrNull(propertyFileName)) {
			propertyFileName = DEFAULT_JAVA_UTIL_LOGGER_PROPERTY_FILE_NAME;
		}
		if (useLog4J) {
			return;
		}else {
			JulLogger.initJavaUtilLogger(propertyFileName);
		}
	}

	public static AppLogger getLogger(Class clazz) {
		return new AppLogger(clazz);
	}

	// finest
	public void finest(String msg) {
		if(useLog4J) {
			log4JLogger.debug(msg);
		}else {
			julLogger.finest(msg);
		}
	}

	public void finest(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(msg, t);
		}else {
			julLogger.finest(msg, t);
		}
	}
	public void finest(Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(t);
		}else {
			julLogger.finest("Error Detected. ", t);
		}
	}

	// finer
	public void finer(String msg) {
		if(useLog4J) {
			log4JLogger.debug(msg);
		}else {
			julLogger.finer(msg);
		}
	}

	public void finer(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(msg, t);
		}else {
			julLogger.finer(msg, t);
		}
	}
	public void finer(Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(t);
		}else {
			julLogger.finer("Error Detected. ", t);
		}
	}

	// fine
	public void fine(String msg) {
		if(useLog4J) {
			log4JLogger.debug(msg);
		}else {
			julLogger.fine(msg);
		}
	}

	public void fine(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(msg, t);
		}else {
			julLogger.fine(msg, t);
		}
	}
	public void fine(Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(t);
		}else {
			julLogger.fine("Error Detected. ", t);
		}
	}

	// debug
	public void debug(String msg) {
		if(useLog4J) {
			log4JLogger.debug(msg);
		}else {
			julLogger.fine(msg);
		}
	}

	public void debug(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(msg, t);
		}else {
			julLogger.fine(msg, t);
		}
	}

	public void debug(Throwable t) {
		if(useLog4J) {
			log4JLogger.debug(t);
		}else {
			julLogger.fine("Error Detected. ", t);
		}
	}
	
	// Info
	public void info(String msg) {
		if(useLog4J) {
			log4JLogger.info(msg);
		}else {
			julLogger.info(msg);
		}
	}

	public void info(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.info(msg, t);
		}else {
			julLogger.info(msg, t);
		}
	}
	public void info(Throwable t) {
		if(useLog4J) {
			log4JLogger.info(t);
		}else {
			julLogger.info("Error Detected. ", t);
		}
	}

	// warning
	public void warning(String msg) {
		if(useLog4J) {
			log4JLogger.warn(msg);
		}else {
			julLogger.warning(msg);
		}
	}

	public void warning(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.warn(msg, t);
		}else {
			julLogger.warning(msg, t);
		}
	}
	public void warning(Throwable t) {
		if(useLog4J) {
			log4JLogger.warn(t);
		}else {
			julLogger.warning("Error Detected. ", t);
		}
	}

	// warn
	public void warn(String msg) {
		if(useLog4J) {
			log4JLogger.warn(msg);
		}else {
			julLogger.warning(msg);
		}
	}

	public void warn(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.warn(msg, t);
		}else {
			julLogger.warning(msg, t);
		}
	}
	public void warn(Throwable t) {
		if(useLog4J) {
			log4JLogger.warn(t);
		}else {
			julLogger.warning("Error Detected. ", t);
		}
	}

	// config
	public void config(String msg) {
		if(useLog4J) {
			log4JLogger.info(msg);
		}else {
			julLogger.info(msg);
		}
	}

	public void config(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.info(msg, t);
		}else {
			julLogger.info(msg, t);
		}
	}
	
	public void config(Throwable t) {
		if(useLog4J) {
			log4JLogger.info(t);
		}else {
			julLogger.info("Error Detected. ", t);
		}
	}

	// severe
	public void severe(String msg) {
		if(useLog4J) {
			log4JLogger.error(msg);
		}else {
			julLogger.severe(msg);
		}
	}

	public void severe(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.error(msg, t);
		}else {
			julLogger.severe(msg, t);
		}
	}

	public void severe( Throwable t) {
		if(useLog4J) {
			log4JLogger.error(t);
		}else {
			julLogger.severe("Error Detected. ", t);
		}
	}
	// error
	public void error(String msg) {
		if(useLog4J) {
			log4JLogger.error(msg);
		}else {
			julLogger.severe(msg);
		}
	}

	public void error(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.error(msg, t);
		}else {
			julLogger.severe(msg, t);
		}
	}

	public void error(Throwable t) {
		if(useLog4J) {
			log4JLogger.error(t);
		}else {
			julLogger.severe("Error Detected. ", t);
		}
	}

	
	// fatal
	public void fatal(String msg) {
		if(useLog4J) {
			log4JLogger.fatal(msg);
		}else {
			julLogger.severe(msg);
		}
	}

	public void fatal(String msg, Throwable t) {
		if(useLog4J) {
			log4JLogger.fatal(msg, t);
		}else {
			julLogger.severe(msg, t);
		}
	}
	public void fatal(Throwable t) {
		if(useLog4J) {
			log4JLogger.fatal(t);
		}else {
			julLogger.severe("Error Detected. ", t);
		}
	}

}
