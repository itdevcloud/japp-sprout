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
package com.itdevcloud.japp.core.service.startup;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.StartupServiceHelperI;
import com.itdevcloud.japp.core.service.referencecode.ReferenceCodeService;
import com.itdevcloud.japp.se.common.util.StringUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class DefaultStartupServiceHelper implements StartupServiceHelperI {
	//private Logger logger = LogManager.getLogger(DefaultStartupServiceHelper.class);
	private static final Logger logger = LogManager.getLogger(DefaultStartupServiceHelper.class);


	@PostConstruct
	public void initservice() {
	}

	@Override
	public void init(ApplicationReadyEvent event) {
		logger.info("DefaultStartupServiceHelper.init() - begin......");

		//set server default timezone
		String serverDefaultTimeZoneId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SPRING_JACKSON_TIMEZONE);
		if(!StringUtil.isEmptyOrNull(serverDefaultTimeZoneId)) {
			String defaultTZId = TimeZone.getDefault().getID();
			if(!serverDefaultTimeZoneId.equalsIgnoreCase(defaultTZId)) {
				TimeZone tz = TimeZone.getTimeZone(serverDefaultTimeZoneId);
				TimeZone.setDefault(tz);
				logger.info("DefaultStartupService.init() - Change Server Default TimeZone Id from '" + defaultTZId + "' to '" + serverDefaultTimeZoneId + "' ......");
			}
		}
		logger.info("DefaultStartupService.init() - Server Default TimeZone Id = '" + TimeZone.getDefault().getID() + "' ......");

		AppComponents.frontendEnvSetupService.setupFrontendEnvironment();
		AppComponents.startupService.sendStartupNotification();

		logger.info("DefaultStartupService.init() - end......");
		return;

	}

	@Override
	public Class<?> getInterfaceClass() {
		// TODO Auto-generated method stub
		return StartupServiceHelperI.class;
	}

}
