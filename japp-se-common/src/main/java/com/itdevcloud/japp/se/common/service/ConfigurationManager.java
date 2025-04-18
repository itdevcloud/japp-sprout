package com.itdevcloud.japp.se.common.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * 
 * @author SunMa
 *
 *         This class is an implementation of IConfigurationManager interface.
 * 
 *         The configuration loading process:
 * 
 *         1. load system and environment properties(variables) 2. 2.1)load
 *         commonConfig.properties from classpath, if fail, throws
 *         RuntimeException 2.2)load included file(s) if has any 3. 3.1)load
 *         external property file defined in commonConfig.properties file (key =
 *         japp.common.config.external.file), if fail, loading process stop.
 *         3.2)load included file(s) if has any
 * 
 *         Configuration loading Features:
 * 
 *         1. Support include zero or more property files. (use prefix
 *         "japp.config.includes." for key name will cause loading process
 *         search and loading properties from another file. Support two search
 *         machenisiam. e.g. #load from classpath
 *         jee.config.includes.a=classpath:sample-application-external-common-config.properties
 *         #load from path (use variables)
 *         jee.config.includes.b=${jee.common.config.unittest.dummy.config.root}/${jee.common.config.unittest.dummy.config.file}
 * 
 *         2. Support using variables (system or user defined) e.g.
 *         common_root="c:/common
 *         jee.config.includes.c=${common_root}/aaa.properties
 * 
 *         3. support prefix (for property key name) check. (just give warning
 *         information)
 * 
 */
public class ConfigurationManager {

	private static JulLogger logger = JulLogger.getLogger(ConfigurationManager.class.getName());

	public static final String JAPP_COMMON_CONFIG_FILE_NAME = "commonConfig.properties";
	public static final String JAPP_COMMON_EXTERNAL_CONFIG_FILE = "japp.common.config.external.file";
	public static final String JAPP_CONFIG_PROPERTY_NAME_CHECK_PREFIX_KEY = "japp.config.property.name.check.prefix";

	public static final String JAPP_CONFIG_INCLUDES_PREFIX = "japp.common.includes.";

	private static Properties tempConfigProperties = null;

	private static Map<String, String> commonConfigMap = null;

	private static ConfigurationManager instance = null;

	static {
		tempConfigProperties = null;
		commonConfigMap = null;
		loadConfigProperties();
	}

	private ConfigurationManager() {
		loadConfigProperties();
	}

	public static ConfigurationManager getInstance() {
		if (instance == null) {
			instance = new ConfigurationManager();
		}
		return instance;
	}

	/**
	 * Searches for the property with the specified prefix in this property list. If
	 * the key is not found in this property list, a empty property is returned.
	 * 
	 * @param prefix - the prefix of the property sought after
	 * @return A list of properties matching the given prefix. However, the given
	 *         prefix is stripped from the name of the returned properties.
	 */
	public Map<String, String> getProperties(String prefix) {
		if (commonConfigMap == null || commonConfigMap.isEmpty()) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		for (String key : commonConfigMap.keySet()) {
			if (StringUtil.isEmptyOrNull(prefix) || key.startsWith(prefix)) {
				map.put(key, commonConfigMap.get(key));
			}
		}
		return map.isEmpty() ? null : Collections.unmodifiableMap(map);
	}


	public String getPropertyAsString(String propertyName, String defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not find or get empty value for the property( " + propertyName
					+ " ) from configration file, use default value (" + defaultValue + ").");
			return defaultValue;
		}
		return value;
	}

	public String getRequiredPropertyAsString(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.severe("Can not find or get empty value for the property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not find or get empty value for the property( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		return value;
	}

	public int getPropertyAsInt(String propertyName, int defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value ("
					+ defaultValue + ").");
			return defaultValue;
		}
		int retValue = defaultValue;
		try {
			retValue = Integer.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = "
					+ value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public int getRequiredPropertyAsInt(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.severe("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		int retValue = 0;
		try {
			retValue = Integer.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = "
					+ value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public double getPropertyAsDouble(String propertyName, double defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value ("
					+ defaultValue + ").");
			return defaultValue;
		}
		double retValue = defaultValue;
		try {
			retValue = Double.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into double (property = " + propertyName + ", value = "
					+ value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public double getRequiredPropertyAsDouble(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.severe("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		double retValue = 0;
		try {
			retValue = Double.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into double (property = " + propertyName + ", value = "
					+ value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public BigDecimal getPropertyAsBigDecimal(String propertyName, BigDecimal defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value ("
					+ defaultValue + ").");
			return defaultValue;
		}
		BigDecimal retValue = defaultValue;
		try {
			retValue = new BigDecimal(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into BigDecimal (property = " + propertyName
					+ ", value = " + value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public BigDecimal getRequiredPropertyAsBigDecimal(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.severe("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		BigDecimal retValue = new BigDecimal(0);
		try {
			retValue = new BigDecimal(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into BigDecimal (property = " + propertyName
					+ ", value = " + value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value ("
					+ defaultValue + ").");
			return defaultValue;
		}
		boolean retValue = false;
		try {
			retValue = Boolean.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = "
					+ value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public boolean getRequiredPropertyAsBoolean(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.severe("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not get property ( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		boolean retValue = false;
		try {
			retValue = Boolean.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into boolean (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into boolean (property = " + propertyName
					+ ", value = " + value + " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	private String getCommonConfigProperty(String propertyName) {
		if (propertyName == null || (propertyName = propertyName.trim()).equals("") || commonConfigMap == null) {
			return null;
		}
		return commonConfigMap.get(propertyName);
	}

	public String printConfiguration() {
		return printCommonConfigProperties();
	}

	public void resetConfiguration() {
		commonConfigMap = null;
		tempConfigProperties = null;
		loadConfigProperties();
	}

	private static void loadConfigProperties() {
		if (commonConfigMap != null) {
			return;
		}
		logger.finer("Loading configuration properties start........");
		// load system properties
		tempConfigProperties = new Properties(System.getProperties());

		// load system environment variables
		Map<String, String> env = System.getenv();
		for (String key : env.keySet()) {
			tempConfigProperties.setProperty(key, env.get(key));
		}

		Properties tmpProperties = new Properties();
		InputStream in = null;
		try {
			in = ConfigurationManager.class.getResourceAsStream("/" + JAPP_COMMON_CONFIG_FILE_NAME);
			logger.info("loading property file '/" + JAPP_COMMON_CONFIG_FILE_NAME + "' from classpath...");
			tempConfigProperties.load(in);
			in.close();

			// just check and warn
			tmpProperties.clear();
			in = ConfigurationManager.class.getResourceAsStream("/" + JAPP_COMMON_CONFIG_FILE_NAME);
			tmpProperties.load(in);
			in.close();
			in = null;
			checkPropertyPrefix(tmpProperties);
		} catch (Exception e) {
			throw new RuntimeException(
					"can not load property file '/" + JAPP_COMMON_CONFIG_FILE_NAME + "' from classpath.", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.warning("Cannot close InputStream." + e);
				}
				;
			}
		}
		resolveVariables();
		resolveIncludes();
		String externalFile = tempConfigProperties.getProperty(JAPP_COMMON_EXTERNAL_CONFIG_FILE);
		if (externalFile != null && !(externalFile = externalFile.trim()).equals("")) {
			if (externalFile.equalsIgnoreCase(JAPP_COMMON_CONFIG_FILE_NAME)) {
				throw new RuntimeException("external property file name '" + externalFile + "' should not be same as "
						+ JAPP_COMMON_CONFIG_FILE_NAME);
			}
			// load external properties
			try {
				in = CommonFactory.class.getResourceAsStream("/" + externalFile);
				logger.info("loading property file '/" + externalFile + "' from classpath...");
				if (in == null) {
					logger.warning("can not find property file '/" + externalFile + "' from classpath.");
				} else {
					tempConfigProperties.load(in);
					in.close();

					// just check and warn
					tmpProperties.clear();
					in = CommonFactory.class.getResourceAsStream("/" + externalFile);
					tmpProperties.load(in);
					in.close();
					in = null;
					checkPropertyPrefix(tmpProperties);
				}
			} catch (Exception e) {
				throw new RuntimeException("can not load property file '/" + externalFile + "' from classpath.", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						logger.warning("Cannot close InputStream." + e);
					}
					;
				}
			}
			resolveVariables();
			resolveIncludes();
		} else {
			logger.info("There is no external configuration file defined. Check property '"
					+ JAPP_COMMON_EXTERNAL_CONFIG_FILE + "'...");
		}
		logger.finer("\n" + printCommonConfigProperties());
		logger.finer("Loading configuration properties end........");

		commonConfigMap = CommonUtil.propertiesToMap(tempConfigProperties, true);
		tempConfigProperties = null;
		return;
	}

	private static String printCommonConfigProperties() {
		// logger.fine("printCommonConfigProperties() begins...");
		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
			return "Configuration Properties = []";
		}
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Configuration Properties = ");
		Set e = commonConfigMap.keySet();
		List<String> keyList = new ArrayList(e);
		Collections.sort(keyList);
		for (String key : keyList) {
			strBuffer.append("\n****" + key + " = " + commonConfigMap.get(key));
		}
		// logger.fine("printCommonConfigProperties() ends...");
		return strBuffer.toString();
	}

	private static void checkPropertyPrefix(Properties properties) {
		// logger.fine("checkPropertyPrefix() begin...");
		if (properties == null) {
			return;
		}
		String prefix = properties.getProperty(JAPP_CONFIG_PROPERTY_NAME_CHECK_PREFIX_KEY);
		if (prefix == null || (prefix = prefix.trim()).equals("")) {
			return;
		}
		Set<?> e = properties.keySet();
		List<String> keyList = new ArrayList(e);
		String value = null;
		for (String key : keyList) {
			if (!key.startsWith(prefix)) {
				logger.warning("checkPropertyPrefix() - property name (" + key + ") should have prefix (" + prefix
						+ "), please change your property name...");
			}
		}
		return;
	}

	private static void resolveVariables() {
		Set<?> e = tempConfigProperties.keySet();
		List<String> keyList = new ArrayList(e);
		String value = null;
		for (String key : keyList) {
			value = tempConfigProperties.getProperty(key);
			value = resolveVariable(value);
			tempConfigProperties.put(key, value);
		}
		return;
	}

	private static String resolveVariable(String value) {
		String variableName = getVariableName(value);
		if (variableName == null) {
			// no variables in the String
			return value;
		}
		logger.fine("resolve variable ${" + variableName + "}...");
		int idx1 = value.indexOf("${" + variableName + "}");
		if (idx1 < 0) {
			throw new RuntimeException("There are code defects in resolveVariable() and/or getVariableName() methods.");
		}
		String str1 = getVariableValue(variableName);
		String str2 = value.substring(0, idx1) + str1 + value.substring(idx1 + variableName.length() + 3);
		String retStr = resolveVariable(str2);
		return retStr;
	}

	private static String getVariableName(String value) {
		if (value == null) {
			return null;
		}
		int idx1 = value.indexOf("${");
		if (idx1 < 0) {
			return null;
		}
		if (idx1 == 0 || !value.substring(idx1 - 1, idx1).equals("\\")) {
			int idx2 = value.indexOf("}", idx1);
			if (idx2 < 0 || (idx2 == (idx1 + 2))) {
				// ${} or ${...
				return null;
			}
			value = value.substring(idx1 + 2, idx2);
			return value;
		}
		// case \${
		value = value.substring(idx1 + 1);
		return getVariableName(value);
	}

	private static String getVariableValue(String variableName) {
		String value = "";
		if (variableName == null) {
			logger.fine("variable =${" + variableName + "}, value = " + value);
			return value;
		}
		value = tempConfigProperties.getProperty(variableName);
		if (value != null) {
			logger.fine("variable =${" + variableName + "}, value (come from configuration file)= '" + value + "'");
			return value;
		} else {
			try {
				value = System.getProperty(variableName);
			} catch (Exception e) {
				logger.warning("error when getting system envrionment variable (" + variableName + "..." + e);
				e.printStackTrace();
			}
		}
		value = (value == null ? "" : value);
		logger.fine("variable =${" + variableName + "}, value (come from system envrionment)= '" + value + "'");
		return value;
	}

	private static void resolveIncludes() {
		Set<?> e = tempConfigProperties.keySet();
		List<String> keyList = new ArrayList(e);
		Collections.sort(keyList);
		String fileName = null;
		String key = null;
		int size = keyList.size();

		for (int j = 0; j < size; j++) {
			key = keyList.get(j);
			if (key.startsWith(JAPP_CONFIG_INCLUDES_PREFIX)) {
				fileName = tempConfigProperties.getProperty(key);
				tempConfigProperties.remove(key);
				keyList.remove(key);
				size--;
				logger.fine("loading included configuration file, key = '" + key + "' file = " + fileName + "...");
				loadProperty(fileName);
			}
		}
		return;
	}

	private static void loadProperty(String fileName) {
		if (fileName == null || (fileName = fileName.trim()).equals("")) {
			return;
		}
		InputStream in = null;
		Properties tmpProperties = new Properties();
		String originalName = fileName;
		try {
			if (originalName.startsWith("classpath:")) {
				fileName = originalName.substring(10);
				logger.info("loading file '" + fileName + "' from classpath...");
				in = ConfigurationManager.class.getResourceAsStream("/" + fileName);
			} else {
				logger.info("loading property file '" + fileName + "' from path...");
				in = new FileInputStream(new File(originalName));
			}
			if (in == null) {
				throw new RuntimeException("cannot find property file " + fileName);
			}
			tempConfigProperties.load(in);
			in.close();
			tmpProperties.clear();
			if (originalName.startsWith("classpath:")) {
				fileName = originalName.substring(10);
				in = ConfigurationManager.class.getResourceAsStream("/" + fileName);
			} else {
				in = new FileInputStream(new File(originalName));
			}
			tmpProperties.load(in);
			in.close();
			in = null;
			checkPropertyPrefix(tmpProperties);
			resolveVariables();
			resolveIncludes();
		} catch (Exception e) {
			logger.severe("can not load property file '" + fileName + "'." + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.warning("Cannot close InputStream." + e);
				}
			}
		}
		return;
	}

//	public String getRequiredPropertyAsString(String prefix, String propertyName) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//		if (prefix == null) {
//			return getRequiredPropertyAsString(propertyName);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getRequiredPropertyAsString(newPropertyName);
//		} else {
//			return getRequiredPropertyAsString(propertyName);
//		}
//	}
//
//	public String getPropertyAsString(String prefix, String propertyName, String defaultValue) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//		if (prefix == null) {
//			return getPropertyAsString(propertyName, defaultValue);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getPropertyAsString(newPropertyName, defaultValue);
//		} else {
//			return getPropertyAsString(propertyName, defaultValue);
//		}
//	}
//
//	public int getRequiredPropertyAsInt(String prefix, String propertyName) {
//
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//
//		if (prefix == null) {
//			return getRequiredPropertyAsInt(propertyName);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getRequiredPropertyAsInt(newPropertyName);
//		} else {
//			return getRequiredPropertyAsInt(propertyName);
//		}
//
//	}
//
//	public int getPropertyAsInt(String prefix, String propertyName, int defaultValue) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//
//		if (prefix == null) {
//			return getPropertyAsInt(propertyName, defaultValue);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getPropertyAsInt(newPropertyName, defaultValue);
//		} else {
//			return getPropertyAsInt(propertyName, defaultValue);
//		}
//	}
//
//	public BigDecimal getPropertyAsBigDecimal(String prefix, String propertyName, BigDecimal defaultValue) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//		if (prefix == null) {
//			return getPropertyAsBigDecimal(propertyName, defaultValue);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getPropertyAsBigDecimal(newPropertyName, defaultValue);
//		} else {
//			return getPropertyAsBigDecimal(propertyName, defaultValue);
//		}
//	}
//
//	public BigDecimal getRequiredPropertyAsBigDecimal(String prefix, String propertyName) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return null;
//		}
//		if (prefix == null) {
//			return getRequiredPropertyAsBigDecimal(propertyName);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getRequiredPropertyAsBigDecimal(newPropertyName);
//		} else {
//			return getRequiredPropertyAsBigDecimal(propertyName);
//		}
//	}
//
//	public boolean getPropertyAsBoolean(String prefix, String propertyName, boolean defaultValue) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return defaultValue;
//		}
//		if (prefix == null) {
//			return getPropertyAsBoolean(propertyName, defaultValue);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getPropertyAsBoolean(newPropertyName, defaultValue);
//		} else {
//			return getPropertyAsBoolean(propertyName, defaultValue);
//		}
//	}
//
//	public boolean getRequiredPropertyAsBoolean(String prefix, String propertyName) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return false;
//		}
//		if (prefix == null) {
//			return getRequiredPropertyAsBoolean(propertyName);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getRequiredPropertyAsBoolean(newPropertyName);
//		} else {
//			return getRequiredPropertyAsBoolean(propertyName);
//		}
//	}
//
//	public double getPropertyAsDouble(String prefix, String propertyName, double defaultValue) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return defaultValue;
//		}
//		if (prefix == null) {
//			return getPropertyAsDouble(propertyName, defaultValue);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getPropertyAsDouble(newPropertyName, defaultValue);
//		} else {
//			return getPropertyAsDouble(propertyName, defaultValue);
//		}
//	}
//
//	public double getRequiredPropertyAsDouble(String prefix, String propertyName) {
//		logger.fine("prefix: " + prefix);
//		if(commonConfigMap == null || commonConfigMap.isEmpty()) {
//			return 0;
//		}
//		if (prefix == null) {
//			return getRequiredPropertyAsDouble(propertyName);
//		}
//
//		prefix = prefix.trim();
//		String newPropertyName = prefix + "." + propertyName;
//
//		if (commonConfigMap.containsKey(newPropertyName)) {
//			return getRequiredPropertyAsDouble(newPropertyName);
//		} else {
//			return getRequiredPropertyAsDouble(propertyName);
//		}
//	}

}
