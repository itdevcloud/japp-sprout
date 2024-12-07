package com.itdevcloud.japp.se.common.service;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * 
 * @author SunMa
 *
 * <p>This is a common factory class used to create an instance.</p>
 * 
 * <p>
 * The CommonFactory.java class is a simplified object / instance factory utility class. 
 * The main purpose of this class is to make any common service fully API based without 
 * using complicated third party framework such as spring framework.  
 * </p>
 * 
 * <p>
 * For an example, one organization has more than 20 Java/J2EE applications share an 
 * internal common component which provides a configuration management service. 
 * It�s a normal case that one particular application sometimes has a special 
 * requirement for the configuration management service. In order to not change 
 * common code just for one application, the best practice is that define configuration 
 * management service interface and use it in common component. The common component 
 * provides a default implement of it. At run time, an application can provide its 
 * own implementation if necessary. 
 * </p>
 * 
 * <p>
 * In jeeCommon.jar file, we provide configuration, logging and email services by 
 * defining three interfaces: 
 * 1) IConfigurationManager
 * 2) ICommonLogger
 * 3) ISimpleEmailService.
 * </p>
 * 
 * Default implementations of these interfaces are provided in the common Jar file. 
 * These implementations can be replaced at run time.
 *
 * 
 * <p>
 * There is an IConfigurationManager interface defined and used in jeeCommon.jar. 
 * For an example, inside jeeCommon.jar, code will always use following statement 
 * to get implementation of IConfigurationManager interface before using it:
 * </p>
 * IConfigurationManager configManager = 
 *            CommonFactory.getInstance ("IConfigurationManager");
 * 
 * <p>
 * If a client application would like to use it�s own implementation, just simply 
 * create an implementation class of IConfigurationManager and define it in 
 * jeeCommonFactory-external.properties file (file name is fixed for now)
  * E.g. put this line in jeeCommonFactory-external.properties file.
 * IConfigurationManager=com.abc.myConfigurationManagerImpl *
 * Make sure put jeeCommonFactory-external.properties in classpath 
 * (e.g. in APP-INF/lib, APP-INF/classes or WEB-INF/lib, 
 * WEB-INF/classes directory. We do not suggest to put it in system class path)
 * </p>
 * 
 * <p>
 * usage example (use configuration management service as an example):
 * <p>
 * &nbsp;&nbsp; 
 *    IConfigurationManager configurationManager = 
 *       CommonFactory.getInstance("IConfigurationManager");
 * @see org.sm.jee.common.util.impl.configuration.ConfigurationManager
 * 
 * the factory class will try to invoke target class' getInstance() method, this give a chance to
 * the target class to implement singlton pattern
 * 
 */



public class CommonFactory {
	public static final String COMMON_FACTORY_CONFIG_FILE_NAME = "commonFactory.properties";
	public static final String COMMON_FACTORY_EXTERNAL_CONFIG_FILE = "japp.common.factory.external.file";
	private static Properties jappCommonFactoryProperties = null;
	private static final Logger logger = Logger.getLogger(CommonFactory.class.getName());
	private static Map<String, Class<?>> classesCache = new HashMap<String, Class<?>>();

	public static <T> T getInstance(String implClassName) {
		//logger.debug("CommonFactory.getInstance() begin, original implementation class name = '" + implClassName + "'...");
		T t = (T) getInstance(implClassName, (Class[]) null, (Object[]) null);
		//logger.debug("CommonFactory.getInstance() end, invoke default constructor, returned class type = " + t.getClass().getName());
		return t;
	}

	public static <T> T getInstance(String implClassName, Class<?>[] parameterTypes, Object[] initargs) {
		logger.finest("CommonFactory.getInstance(...) begin, original implementation class name = '" + implClassName + "'...");
		if (implClassName == null || (implClassName = implClassName.trim()).equals("")) {
			throw new RuntimeException("Must provide 'implClassName' when using CommonFactory to get an implementation instance");
		}
		//if there is a entry in configuration file, use it
		String value = getConfigProperty(implClassName);
		if (!StringUtil.isEmptyOrNull(value) && !value.equals(implClassName)) {
			implClassName = value;
			logger.finest("implementation class name change to = '" + implClassName + "'...");
		}
		Class<?> c1 = null;
		Method m = null;
		T t = null;
		try {
			c1 = getClass(implClassName);
		} catch (Exception e) {
			throw new RuntimeException("can not find '" + implClassName + "'.", e);
		}
		try {
			m = c1.getMethod("getInstance", parameterTypes);
			t = (T) m.invoke(null, initargs);
			t.getClass().cast(t);
			logger.finest("CommonFactory.getInstance(...) end, call getInstance(), returned class type = " + t.getClass().getName());
			return t;
		} catch (ClassCastException e1) {
			//will not get here
			throw new RuntimeException("'" + implClassName + "' is not a type of return type. ", e1);
		} catch (Exception e) {
			//there is no static getInstance() method, try default constructor
			logger.finest("Can not find static getInstance(...) method in class '" + implClassName
					+ "', try to use constructor(...) to create an instance.");
			try {
				Constructor<?> construtor = c1.getConstructor(parameterTypes);
				t = (T) construtor.newInstance(initargs);
			} catch (NoSuchMethodException e0) {
				try {
					logger.finest("Can not find static getInstance(...) or constructor(...) method in class '" + implClassName
							+ "', try to use getInstance() or default construtor to create an instance.");
					return getInstance((Class<T>) c1);
				} catch (Exception ee) {
					throw new RuntimeException(
							"can not get an instance for class '"
									+ implClassName
									+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
							ee);
				}
			} catch (Exception e2) {
				throw new RuntimeException(
						"can not get an instance for class '"
								+ implClassName
								+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
						e2);
			}
			logger.finest("CommonFactory.getInstance(...) end, invoke constructor(...), returned class type = " + t.getClass().getName());
			return t;
		}
	}

	public static <T> T getInstance(Class<T> interfaceImpl, Class<?>[] parameterTypes, Object[] initargs) {
		if (interfaceImpl == null) {
			throw new RuntimeException("Must provide 'interfaceImpl' when using CommonFactory to get an implementation instance");
		}
		String implClassName = interfaceImpl.getName();
		logger.finest("CommonFactory.getInstance(...) begin, interface implementation class name = '" + implClassName + "'...");
		Method m = null;
		T t = null;
		try {
			m = interfaceImpl.getMethod("getInstance", parameterTypes);
			t = (T) m.invoke(null, initargs);
			logger.finest("CommonFactory.getInstance(...) end, call getInstance(), returned class type = " + t.getClass().getName());
			return t;
		} catch (Exception e) {
			//there is no static getInstance() method, try default constructor
			logger.finest("Can not find static getInstance(...) method in class '" + implClassName
					+ "', try to use default constructor to create an instance. " + e);
			try {
				Constructor<?> construtor = interfaceImpl.getConstructor(parameterTypes);
				t = (T) construtor.newInstance(initargs);
			} catch (NoSuchMethodException e0) {
				try {
					logger.finest("Can not find static getInstance(...) or constructor(...) method in class '" + implClassName
							+ "', try to use getInstance() or default construtor to create an instance. " + e0);
					return getInstance(interfaceImpl);
				} catch (Exception ee) {
					throw new RuntimeException(
							"can not get an instance for class '"
									+ implClassName
									+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
							ee);
				}
			} catch (Exception e1) {
				throw new RuntimeException(
						"can not get an instance for class '"
								+ implClassName
								+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
						e1);
			}
			logger.finest("CommonFactory.getInstance() end, invoke default constructor, returned class type = " + t.getClass().getName());
			return t;
		}
	}

	public static <T> T getInstance(Class<T> interfaceImpl) {
		logger.finest("CommonFactory.getInstance() begin, interface implementation class name = '" + interfaceImpl + "'...");
		T t = (T) getInstance(interfaceImpl, (Class[]) null, (Object[]) null);
		logger.finest("CommonFactory.getInstance() end, invoke default constructor, returned class type = " + t.getClass().getName());
		return t;
	}

	private static Properties getConfigProperties() {
		if (jappCommonFactoryProperties != null) {
			return new Properties(jappCommonFactoryProperties);
		}
		logger.info("CommonFactory.getConfigProperties() begin...");
		jappCommonFactoryProperties = new Properties();
		try {
			InputStream in = CommonFactory.class.getResourceAsStream("/" + COMMON_FACTORY_CONFIG_FILE_NAME);
			jappCommonFactoryProperties.load(in);
		} catch (Exception e) {
			throw new RuntimeException("can not load property file '/" + COMMON_FACTORY_CONFIG_FILE_NAME + "'.", e);
		}
		String externalFile = jappCommonFactoryProperties.getProperty(COMMON_FACTORY_EXTERNAL_CONFIG_FILE);
		if (!StringUtil.isEmptyOrNull(externalFile) && !externalFile.equalsIgnoreCase(COMMON_FACTORY_CONFIG_FILE_NAME)) {
			//defined a different external property file
			//load external properties
			try {
				InputStream in = CommonFactory.class.getResourceAsStream("/" + externalFile);
				if (in == null) {
					logger.warning("can not find property file '/" + externalFile + "' from classpath.");
				} else {
					jappCommonFactoryProperties.load(in);
				}
			} catch (Exception e) {
				throw new RuntimeException("can not load property file '/" + externalFile + "'.", e);
			}
		}else {
			logger.info("there is no seperate external proprty file defined for common factory......");
		}
		logger.info("\n" + printConfigProperty());
		logger.info("CommonFactory.getConfigProperties() end...\n");
		return jappCommonFactoryProperties;
	}

	public static void resetConfigProperty() {
		jappCommonFactoryProperties = null;
	}

	private static String getConfigProperty(String key) {
		if (jappCommonFactoryProperties == null) {
			getConfigProperties();
		}
		String value = jappCommonFactoryProperties.getProperty(key);
		if (value != null) {
			value = value.trim();
		}

		return value;
	}

	private static String printConfigProperty() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("CommonFactory " +
				"Properties = ");
		if (jappCommonFactoryProperties == null) {
			strBuffer.append(" null");
			return strBuffer.toString();
		}
		Set e = jappCommonFactoryProperties.keySet();
		List<String> keyList = new ArrayList(e);
		Collections.sort(keyList);
		for (String value : keyList) {
			strBuffer.append("\n    " + value + " = " + jappCommonFactoryProperties.getProperty(value));
		}
		return strBuffer.toString();
	}

	private static Class<?> getClass(String className) throws ClassNotFoundException {
		if (className == null || className.trim().length() == 0) {
			throw new RuntimeException("Must provide 'className'");
		}
		String trimmed = className.trim();
		Class<?> c = classesCache.get(trimmed);
		if (c == null) {
			c = Class.forName(trimmed);
			classesCache.put(trimmed, c);
		}
		return c;
	}
}
