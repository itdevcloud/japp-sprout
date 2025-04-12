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
package com.itdevcloud.japp.core.cahce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.core.service.customization.ReferenceCodeServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AppConfigCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(AppConfigCache.class);

	private static Map<String, String> appConfigMap = null;

	@PostConstruct
	public void initService() {
	}

	@Override
	protected String createDisplayString() {
		String str = "appConfigMap size = " + appConfigMap.size();
		return str;
	}

	@Override
	public synchronized void refreshCache() {
		ConfigServiceHelperI helper = ConfigFactory.appConfigService.getConfigServiceHelper();
		try {

			Map<String, String> tmpMap = helper.createConfigMapFromAppRepository();

			refreshInProcess = true;
			appConfigMap = (tmpMap == null ? new HashMap<String, String>()
					: Collections.unmodifiableMap(tmpMap));
			refreshInProcess = false;

		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public String getConfigProperty(String key) {
		waitForInit();
		//logger.debug("getConfigProperty() - key = " + key );
		if (appConfigMap == null || appConfigMap.isEmpty() || StringUtil.isEmptyOrNull(key)) {
			return null;
		}
		return appConfigMap.get(key);
	}


}
