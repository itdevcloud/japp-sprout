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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.tools.common.util.StringUtil;

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
	private static Map<String, String> configMapFromAppRepository = null;

	//env includes string property files
	@Autowired
	private Environment env;

	private ConfigServiceHelperI configServiceHelper = null;

	@Autowired
	public AppConfigService(List<ConfigServiceHelperI> componentList) {
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
	}

	@PostConstruct
	public void init() {
		configMapFromAppRepository = configServiceHelper.createConfigMapFromAppRepository();
		osEnv = System.getenv();
		systemProperties = System.getProperties();
	}

	private String getProperty(String key) {
		if (StringUtil.isEmptyOrNull(key)) {
			logger.error("getProperty() - '" + key + "' failed: key is NULL, check code!" );
			return null;
		}
		String valueStr = (configMapFromAppRepository==null?null:configMapFromAppRepository.get(key));
		//somehow spring env not working based on the doc
		if (valueStr == null ) {
			valueStr = (systemProperties==null?null:systemProperties.getProperty(key));
		}
		if (valueStr == null ) {
			valueStr = (osEnv==null?null:osEnv.get(key));
		}
		//do not use empty at here, maybe user like to use ""
		if (valueStr == null ) {
			valueStr = env.getProperty(key);
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
