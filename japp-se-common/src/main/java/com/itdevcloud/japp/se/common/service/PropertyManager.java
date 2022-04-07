/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.se.common.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * 
 * This class is used to load a property file from class path 
 * Support using variables (system or user defined in the file)
 * support prefix (for property key name) check. 
 * 
 * @author Marvin Sun
 * @since 1.0.0
 *
 * 
 */
public class PropertyManager {

	private static final Logger logger = Logger.getLogger(PropertyManager.class.getName());
	
	private  Properties properties = null;

	
	public PropertyManager(String... propertyFileNames) {
		super();
		if (propertyFileNames == null ) {
			throw new RuntimeException("propertyFileNames can't be null or empty! check code!");
		}
		properties = new Properties();
		int order = 0;
		for (String fileName : propertyFileNames) {
			order++;
			if (StringUtil.isEmptyOrNull(fileName) ) {
				logger.severe("fileName (order = " + order +") can't be null or empty! check code!");
				continue;
			}
			if(!fileName.startsWith("/")) {
				fileName = "/" + fileName;
			}
			logger.info("PropertyManager load property begin...... file = " + fileName);
			InputStream in = null;
			try {
				in = PropertyManager.class.getResourceAsStream(fileName);
				properties.load(in);
				in.close();
				in = null;
			} catch (Exception e) {
				throw new RuntimeException("can not load property file '" + fileName + "' from classpath.", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						logger.warning("Cannot close InputStream." + CommonUtil.getStackTrace(e));
					}
					;
				}
			}
		}
		resolveVariables();
		logger.info("PropertyManager load property end......");
	}


	public Properties getProperties(String prefix) {
		Properties props = new Properties();
		Enumeration<?> keys = properties.propertyNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(prefix)) {
				String cnxKey = key.substring(prefix.length());
				String value = properties.getProperty(key);
				props.put(cnxKey, value);
			}
		}
		return props;
	}


	public String getPropertyAsString(String propertyName, String defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not find or get empty value for the property( " + propertyName + " ) from configration file, use default value ("
					+ defaultValue + ").");
			return defaultValue;
		}
		return value.trim();
	}

	public String getRequiredPropertyAsString(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			throw new RuntimeException("Can not find or get empty value for the property( " + propertyName
					+ " ) from configration file, please check configuration file.");
		}
		return value.trim();
	}

	public int getPropertyAsInt(String propertyName, int defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value (" + defaultValue + ").");
			return defaultValue;
		}
		int retValue = defaultValue;
		try {
			retValue = Integer.valueOf(value);
		} catch (Exception e) {
			logger.severe("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public int getRequiredPropertyAsInt(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			throw new RuntimeException("Can not get property ( " + propertyName + " ) from configration file, please check configuration file.");
		}
		int retValue = 0;
		try {
			retValue = Integer.valueOf(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public double getPropertyAsDouble(String propertyName, double defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value (" + defaultValue + ").");
			return defaultValue;
		}
		double retValue = defaultValue;
		try {
			retValue = Double.valueOf(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public double getRequiredPropertyAsDouble(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			throw new RuntimeException("Can not get property ( " + propertyName + " ) from configration file, please check configuration file.");
		}
		double retValue = 0;
		try {
			retValue = Double.valueOf(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into double (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public BigDecimal getPropertyAsBigDecimal(String propertyName, BigDecimal defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value (" + defaultValue + ").");
			return defaultValue;
		}
		BigDecimal retValue = defaultValue;
		try {
			retValue = new BigDecimal(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into BigDecimal (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public BigDecimal getRequiredPropertyAsBigDecimal(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			throw new RuntimeException("Can not get property ( " + propertyName + " ) from configration file, please check configuration file.");
		}
		BigDecimal retValue = new BigDecimal(0);
		try {
			retValue = new BigDecimal(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into BigDecimal (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			logger.info("Can not get property ( " + propertyName + " ) from configration file, use default value (" + defaultValue + ").");
			return defaultValue;
		}
		boolean retValue = false;
		try {
			retValue = Boolean.valueOf(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into int (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	public boolean getRequiredPropertyAsBoolean(String propertyName) {
		String value = getCommonConfigProperty(propertyName);
		if (value == null || (value = value.trim()).equals("")) {
			throw new RuntimeException("Can not get property ( " + propertyName + " ) from configration file, please check configuration file.");
		}
		boolean retValue = false;
		try {
			retValue = Boolean.valueOf(value);
		} catch (Exception e) {
			throw new RuntimeException("Can not convert property into boolean (property = " + propertyName + ", value = " + value
					+ " ) from configration file, please check configuration file.");
		}
		return retValue;
	}

	private String getCommonConfigProperty(String propertyName) {
		if (propertyName == null || (propertyName = propertyName.trim()).equals("")) {
			return null;
		}
		return properties.getProperty(propertyName);
	}


	public String printProperties() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Properties = ");
		if (properties == null) {
			strBuffer.append(" null");
			return strBuffer.toString();
		}
		Set e = properties.keySet();
		List<String> keyList = new ArrayList<String>(e);
		Collections.sort(keyList);
		for (String key : keyList) {
			strBuffer.append("\n****" + key + " = " + properties.getProperty(key));
		}
		return strBuffer.toString();
	}


	private void resolveVariables() {
		Set<?> e = properties.keySet();
		List<String> keyList = new ArrayList(e);
		String value = null;
		for (String key : keyList) {
			value = properties.getProperty(key);
			value = resolveVariable(value);
			properties.put(key, value);
		}
		return;
	}

	private String resolveVariable(String value) {
		String variableName = getVariableName(value);
		if (variableName == null) {
			//no variables in the String
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

	private String getVariableName(String value) {
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
				//${} or ${...
				return null;
			}
			value = value.substring(idx1 + 2, idx2);
			return value;
		}
		//case \${
		value = value.substring(idx1 + 1);
		return getVariableName(value);
	}

	private String getVariableValue(String variableName) {
		String value = "";
		if (variableName == null) {
			return value;
		}
		value = properties.getProperty(variableName);
		if (value != null) {
			logger.fine("variable =${" + variableName + "}, value (come from configuration file)= '" + value + "'");
			return value;
		} else {
			try {
				value = System.getProperty(variableName);
			} catch (Exception e) {
				logger.warning("error when getting system envrionment variable (" + variableName + "...\n" + CommonUtil.getStackTrace(e) );
				e.printStackTrace();
				value = null;
			}
			if(value == null) {
				try {
					value = System.getenv(variableName);
				} catch (Exception e) {
					logger.warning("error when getting system envrionment variable (" + variableName + "...\n" + CommonUtil.getStackTrace(e));
					e.printStackTrace();
					value = null;
				}
			}
		}
		value = (value == null ? "" : value);
		logger.severe("variable =${" + variableName + "}, value (come from system envrionment)= '" + value + "'");
		return value;
	}


	public String getRequiredPropertyAsString(String prefix, String propertyName) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getRequiredPropertyAsString(propertyName);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getRequiredPropertyAsString(newPropertyName);
		} else {
			return getRequiredPropertyAsString(propertyName);
		}
	}

	public String getPropertyAsString(String prefix, String propertyName, String defaultValue) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getPropertyAsString(propertyName, defaultValue);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getPropertyAsString(newPropertyName, defaultValue);
		} else {
			return getPropertyAsString(propertyName, defaultValue);
		}
	}

	public int getRequiredPropertyAsInt(String prefix, String propertyName) {

		//logger.debug("prefix: " + prefix);

		if (prefix == null) {
			return getRequiredPropertyAsInt(propertyName);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getRequiredPropertyAsInt(newPropertyName);
		} else {
			return getRequiredPropertyAsInt(propertyName);
		}

	}

	public int getPropertyAsInt(String prefix, String propertyName, int defaultValue) {
		//logger.debug("prefix: " + prefix);

		if (prefix == null) {
			return getPropertyAsInt(propertyName, defaultValue);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getPropertyAsInt(newPropertyName, defaultValue);
		} else {
			return getPropertyAsInt(propertyName, defaultValue);
		}
	}

	public BigDecimal getPropertyAsBigDecimal(String prefix, String propertyName, BigDecimal defaultValue) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getPropertyAsBigDecimal(propertyName, defaultValue);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getPropertyAsBigDecimal(newPropertyName, defaultValue);
		} else {
			return getPropertyAsBigDecimal(propertyName, defaultValue);
		}
	}

	public BigDecimal getRequiredPropertyAsBigDecimal(String prefix, String propertyName) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getRequiredPropertyAsBigDecimal(propertyName);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getRequiredPropertyAsBigDecimal(newPropertyName);
		} else {
			return getRequiredPropertyAsBigDecimal(propertyName);
		}
	}

	public boolean getPropertyAsBoolean(String prefix, String propertyName, boolean defaultValue) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getPropertyAsBoolean(propertyName, defaultValue);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getPropertyAsBoolean(newPropertyName, defaultValue);
		} else {
			return getPropertyAsBoolean(propertyName, defaultValue);
		}
	}

	public boolean getRequiredPropertyAsBoolean(String prefix, String propertyName) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getRequiredPropertyAsBoolean(propertyName);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getRequiredPropertyAsBoolean(newPropertyName);
		} else {
			return getRequiredPropertyAsBoolean(propertyName);
		}
	}

	public double getPropertyAsDouble(String prefix, String propertyName, double defaultValue) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getPropertyAsDouble(propertyName, defaultValue);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getPropertyAsDouble(newPropertyName, defaultValue);
		} else {
			return getPropertyAsDouble(propertyName, defaultValue);
		}
	}

	public double getRequiredPropertyAsDouble(String prefix, String propertyName) {
		//logger.debug("prefix: " + prefix);
		if (prefix == null) {
			return getRequiredPropertyAsDouble(propertyName);
		}

		prefix = prefix.trim();
		String newPropertyName = prefix + "." + propertyName;

		if (properties.containsKey(newPropertyName)) {
			return getRequiredPropertyAsDouble(newPropertyName);
		} else {
			return getRequiredPropertyAsDouble(propertyName);
		}
	}

}
