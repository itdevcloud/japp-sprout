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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class ConfigServiceHelperFactory {

	private static final Logger logger = LogManager.getLogger(ConfigServiceHelperFactory.class);

	private static ConfigServiceHelperI configServiceHelper = null;

	@Autowired
	public ConfigServiceHelperFactory(List<ConfigServiceHelperI> componentList) {
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

	public static ConfigServiceHelperI getConfigServiceHelper() {
		return configServiceHelper;
	}

}
