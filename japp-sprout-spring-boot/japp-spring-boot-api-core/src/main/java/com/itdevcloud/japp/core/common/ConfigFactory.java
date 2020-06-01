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
package com.itdevcloud.japp.core.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.service.config.AppConfigService;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ConfigFactory {

	private static final Logger logger = LogManager.getLogger(ConfigFactory.class);

	public static AppConfigService appConfigService;
	
	@Autowired
	public ConfigFactory(AppConfigService configService) {
		logger.info("init ConfigFactory begin......");
		if (configService == null ) {
			throw new RuntimeException(
					"There is no AppConfigService class initiated, please check code ==> \n");
		}
		appConfigService = configService;
		logger.info("init ConfigFactory end......");
	}

}
