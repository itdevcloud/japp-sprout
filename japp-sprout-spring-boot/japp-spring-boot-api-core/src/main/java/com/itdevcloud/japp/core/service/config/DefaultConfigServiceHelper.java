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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class DefaultConfigServiceHelper implements ConfigServiceHelperI {

	//private static final Logger logger = LogManager.getLogger(DefaultConfigServiceHelper.class);


	@Override
	public Map<String, String> createConfigMapFromAppRepository() {
		Map<String, String> configeMap = new HashMap<String, String>();
		return configeMap;
	}

}
