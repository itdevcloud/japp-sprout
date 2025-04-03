package com.itdevcloud.japp.se.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * this class may be used by stanalong applications (to reduce log library dependencies, 
 * but it may not be good for an app running in a server environment
 * due to limitation of JUL - so far it is per VM based.
 */
public class JulLogger {

	private static final String DEFAULT_JAVA_UTIL_LOGGER_PROPERTY_FILE_NAME = "javaUtilLogging-default.properties";
	private static boolean propertyFileLoaded = false;

	private static Logger mylogger = Logger.getLogger(JulLogger.class.getName());
	private Logger logger = null;
	
	//to use JUL logger and custom property file
	//must call this method as the first call when starting an application
	public synchronized static void initJavaUtilLogger(String propertyFileName) {
		// must set before the Logger
		// loads logging.properties from the classpath
		
		if (propertyFileLoaded) {
			return;
		}
		propertyFileLoaded = true;
		if (StringUtil.isEmptyOrNull(propertyFileName)) {
			propertyFileName = DEFAULT_JAVA_UTIL_LOGGER_PROPERTY_FILE_NAME;
		}
		mylogger.info("Loading Java Util Logger property file: " + propertyFileName);
		//System.out.println("Loading Java Util Logger property file: " + propertyFileName);
		InputStream in = null;
		try {
			in = JulLogger.class.getClassLoader().getResourceAsStream(propertyFileName);
			if(in  == null) {
				mylogger.warning("Can not load Java Util Logger property file (" + propertyFileName + ")......no file found in the class path! ");
				//System.out.println("Can not load Java Util Logger property file (" + propertyFileName + ")......no file file in the class path! ");
			}else {
				mylogger.info("LogManager.getLogManager().readConfiguration()..........start..............." );
				//System.out.println("LogManager.getLogManager().readConfiguration()..........start..............." );
				LogManager.getLogManager().readConfiguration(in);
				//System.out.println("LogManager.getLogManager().readConfiguration()..........end................" );
				mylogger.info("LogManager.getLogManager().readConfiguration()..........end................" );
			}
		} catch (Throwable t) {
			//System.out.println("Can not load Java Util Logger property file (" + propertyFileName + ")......Error: " + t);
			mylogger.warning("Can not load Java Util Logger property file (" + propertyFileName + ")......Error: " + t);
			t.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

	}

	private JulLogger(String className) {
		this(className, null);
	}

	private JulLogger(String className, String propertyFileName) {
		// must be the first call
		initJavaUtilLogger(propertyFileName);

		if (StringUtil.isEmptyOrNull(className)) {
			className = JulLogger.class.getName();
		}
		this.logger = Logger.getLogger(className);
	}

	public static JulLogger getLogger(String className) {
		return new JulLogger(className);
	}

	// finest
	public void finest(String msg) {
//		this.logger.log(Level.FINEST, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINEST, callerList.get(0), callerList.get(1), msg);
	}

	public void finest(String msg, Throwable thrown) {
		//this.logger.log(Level.FINEST, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINEST, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// finer
	public void finer(String msg) {
		//this.logger.log(Level.FINER, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINER, callerList.get(0), callerList.get(1), msg);
	}

	public void finer(String msg, Throwable thrown) {
		//this.logger.log(Level.FINER, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINER, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// fine
	public void fine(String msg) {
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINE, callerList.get(0), callerList.get(1), msg);
	}

	public void fine(String msg, Throwable thrown) {
		//this.logger.log(Level.FINE, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.FINE, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// Info
	public void info(String msg) {
		//this.logger.log(Level.INFO, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.INFO, callerList.get(0), callerList.get(1), msg);
	}

	public void info(String msg, Throwable thrown) {
		//this.logger.log(Level.INFO, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.INFO, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// warning
	public void warning(String msg) {
		//this.logger.log(Level.WARNING, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.WARNING, callerList.get(0), callerList.get(1), msg);
	}

	public void warning(String msg, Throwable thrown) {
		//this.logger.log(Level.WARNING, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.WARNING, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// config
	public void config(String msg) {
		//this.logger.log(Level.CONFIG, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.CONFIG, callerList.get(0), callerList.get(1), msg);
	}

	public void config(String msg, Throwable thrown) {
		//this.logger.log(Level.CONFIG, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.CONFIG, callerList.get(0), callerList.get(1), msg, thrown);
	}

	// severe
	public void severe(String msg) {
		//this.logger.log(Level.SEVERE, msg);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.SEVERE, callerList.get(0), callerList.get(1), msg);
	}

	public void severe(String msg, Throwable thrown) {
		//this.logger.log(Level.SEVERE, msg, thrown);
		List<String> callerList = getCallerClassAndMethodNames();
		this.logger.logp(Level.SEVERE, callerList.get(0), callerList.get(1), msg, thrown);
	}

	private List<String> getCallerClassAndMethodNames() {
		List<String> callerList = new ArrayList<String>();
		    StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		    int i = getFirstNonCommonLoggerClassIndex(stackTrace);
		    StackTraceElement caller = stackTrace[i];
		    
		    //this.logger.fine(caller.getClassName() + "." + caller.getMethodName() );
		    
		    callerList.add(caller.getClassName());
		    callerList.add(caller.getMethodName());
		    return callerList;
		  }

//	private int getSelfElementIndex(StackTraceElement[] stackTrace) {
//		int firstCommonLogerIdx = 0;
//		int lastCommonLogerIdx = 0;
//		boolean foundCommonLogerIdx = false;
//		for (int i = 0; i < stackTrace.length; i++) {
//			StackTraceElement se = stackTrace[i];
//			
//			this.logger.fine("StackTraceElement[" + i + "] = " + se.getClassName() + "." + se.getMethodName() );
//			
//			if (se.getClassName().equals(this.getClass().getName()) && 
//					(se.getMethodName().equalsIgnoreCase("finest") ||
//					 se.getMethodName().equalsIgnoreCase("finer") ||
//					 se.getMethodName().equalsIgnoreCase("fine") ||
//					 se.getMethodName().equalsIgnoreCase("config") ||
//					 se.getMethodName().equalsIgnoreCase("info") ||
//					 se.getMethodName().equalsIgnoreCase("warning") ||
//					 se.getMethodName().equalsIgnoreCase("severe") )) {
//				if(!foundCommonLogerIdx) {
//					firstCommonLogerIdx = i;
//					lastCommonLogerIdx = i;
//					foundCommonLogerIdx = true;
//				}else {
//					lastCommonLogerIdx = i;
//				}
//			}else {
//				if(foundCommonLogerIdx) {
//					return lastCommonLogerIdx + 1;
//				}
//			}
//		}
//		//found nothing
//		return 0;
//	}
	
	//if do not use commonLogger to log in itself, we can use this simplified version, otherwise use comment out version above
	private int getFirstNonCommonLoggerClassIndex(StackTraceElement[] stackTrace) {
		int lastCommonLogerIdx = 0;
		boolean foundCommonLogerIdx = false;
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement se = stackTrace[i];
			
			//this.logger.fine("StackTraceElement[" + i + "] = " + se.getClassName() + "." + se.getMethodName() );
			
			if (se.getClassName().equals(this.getClass().getName())) {
				if(!foundCommonLogerIdx) {
					lastCommonLogerIdx = i;
					foundCommonLogerIdx = true;
				}else {
					lastCommonLogerIdx = i;
				}
			}else {
				if(foundCommonLogerIdx) {
					return (lastCommonLogerIdx >= stackTrace.length-1)?(stackTrace.length-1):  (lastCommonLogerIdx + 1);
				}
			}
		}
		//found nothing
		return 0;
	}
}
