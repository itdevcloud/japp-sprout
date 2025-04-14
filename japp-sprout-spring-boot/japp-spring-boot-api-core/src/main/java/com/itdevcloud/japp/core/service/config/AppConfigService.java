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
package com.itdevcloud.japp.core.service.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.cahce.AppConfigCache;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.se.common.service.ConfigurationManager;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class AppConfigService {

	private static final Logger logger = LogManager.getLogger(AppConfigService.class);

	private static Properties systemProperties = null;
	private static Map<String, String> osEnv = null;
	//private static Map<String, String> configMapFromAppRepository = null;
	private static ConfigurationManager configurationManager = null;

	//env includes spring boot property files
	@Autowired
	private Environment env;

	private ConfigServiceHelperI configServiceHelper = null;
	public AppConfigCache appConfigCache = null;
	
	@Autowired
	public AppConfigService(List<ConfigServiceHelperI> componentList, AppConfigCache appConfigCache) {
		if (componentList == null || componentList.isEmpty()) {
			throw new RuntimeException(
					"There is no ConfigServiceHelperI implemetation class detected, please check code ==> \n");
		}
		if (componentList.size() > 2) {
			throw new RuntimeException(
					"There is more than one custom service implemetation class detected for: ConfigServiceHelperI, please check code !");
		} else if (componentList.size() == 1) {
			configServiceHelper = componentList.get(0);
		} else {
			if (!(componentList.get(0).getClass().getSimpleName()).startsWith("Default")) {
				configServiceHelper = componentList.get(0);
			} else {
				configServiceHelper = componentList.get(1);
			}
		}
		this.appConfigCache = appConfigCache;
	}

	@PostConstruct
	public void init() {
		//configMapFromAppRepository = configServiceHelper.createConfigMapFromAppRepository();
		osEnv = System.getenv();
		systemProperties = System.getProperties();
		configurationManager = ConfigurationManager.getInstance();
	}
    public ConfigServiceHelperI getConfigServiceHelper() {
    	return this.configServiceHelper;
    }
    
	private String getProperty(String key) {
		if (StringUtil.isEmptyOrNull(key)) {
			logger.error("getProperty() - '" + key + "' failed: key is empty or NULL, check code!" );
			return null;
		}
		String valueStr = null;
		//1. get from repository first
		if(this.appConfigCache == null) {
//		if(AppComponents.appConfigCache == null) {
			logger.warn("getProperty() - appConfigCache hasn't been initiated, ignore it, need to enhance code!.......");
		}else {
			valueStr = this.appConfigCache.getConfigProperty(key);
//			valueStr = AppComponents.appConfigCache.getConfigProperty(key);
//			String valueStr = (configMapFromAppRepository==null?null:configMapFromAppRepository.get(key));
		}
		//do not use empty at here, maybe user like to use ""
		//2. system property: -D)
		if (valueStr == null ) {
			valueStr = (systemProperties==null?null:systemProperties.getProperty(key));
		}
		//3. OS ENV
		if (valueStr == null ) {
			valueStr = (osEnv==null?null:osEnv.get(key));
		}
		//somehow spring env not working based on the doc
		//4. Spring ENV (includes application.properties
		if (valueStr == null ) {
			valueStr = env.getProperty(key);
		}
		//5. Common property file
		if (valueStr == null ) {
			valueStr = configurationManager.getPropertyAsString(key, null);
		}
		return valueStr;
	}
	public String getPropertyAsString(String key) {
		return getPropertyAsString(key, null);
	}
	public String getPropertyAsString(String key, String defaultValue) {
		String valueStr = getProperty(key);
		if (valueStr == null) {
			return defaultValue;
		}else {
			return valueStr;
		}
	}
	public Integer getPropertyAsInteger(String key) {
		return getPropertyAsInteger(key, 0);
	}
	public Integer getPropertyAsInteger(String key, Integer defaultValue) {
		String valueStr = getProperty(key);
		if (valueStr == null) {
			return defaultValue;
		}
		try {
			Integer value = Integer.valueOf(valueStr);
			return value;
		} catch (Exception e) {
			logger.error("get property '" + key + "' failed with exception: " + AppUtil.getStackTrace(e));
			return defaultValue;
		}
	}
	public Long getPropertyAsLong(String key) {
		return getPropertyAsLong(key, 0L);
	}
	public Long getPropertyAsLong(String key, Long defaultValue) {
		String valueStr = getProperty(key);
		if (valueStr == null) {
			return defaultValue;
		}
		try {
			Long value = Long.valueOf(valueStr);
			return value;
		} catch (Exception e) {
			logger.error("get property '" + key + "' failed with exception: " + AppUtil.getStackTrace(e));
			return defaultValue;
		}
	}

	public Double getPropertyAsDouble(String key) {
		return getPropertyAsDouble(key, 0.0);
	}
	public Double getPropertyAsDouble(String key, Double defaultValue) {
		String valueStr = getProperty(key);
		if (valueStr == null) {
			return defaultValue;
		}
		try {
			Double value = Double.valueOf(valueStr);
			return value;
		} catch (Exception e) {
			logger.error("get property '" + key + "' failed with exception: " + AppUtil.getStackTrace(e));
			return defaultValue;
		}
	}
	public Boolean getPropertyAsBoolean(String key) {
		return getPropertyAsBoolean(key, false);
	}
	public Boolean getPropertyAsBoolean(String key, Boolean defaultValue) {
		String valueStr = getProperty(key);
		if (valueStr == null) {
			return defaultValue;
		}
		try {
			Boolean value = Boolean.valueOf(valueStr);
			return value;
		} catch (Exception e) {
			logger.error("get property '" + key + "' failed with exception: " + AppUtil.getStackTrace(e));
			return defaultValue;
		}
	}
}
