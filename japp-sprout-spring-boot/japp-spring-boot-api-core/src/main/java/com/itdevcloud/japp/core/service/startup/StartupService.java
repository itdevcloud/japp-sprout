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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.StartupServiceHelperI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class StartupService implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(StartupService.class);
	private static Map<String, String> infoMap = new HashMap<String, String>();
	private static long lastUpdatedTS = -1;

	//must init AppFactory before startup

	/**
	 * Handle an application event. This event is executed as late as conceivably possible to indicate 
	 * that the application is ready to service requests.
	 * 
	 */
	@EventListener
	public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
		StartupServiceHelperI startupServiceHelper =  AppFactory.getComponent(StartupServiceHelperI.class);
		startupServiceHelper.init(event);
	}

	public void addNotificationInfo(String key, String value) {
		infoMap.put(key, value);
		lastUpdatedTS = System.currentTimeMillis();
	}

	@Async
	public void sendStartupNotification() {
		int waitingSeconds =  ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_APP_STARTUP_NOTIFICATION_WAITING_SECONDS);
		long nowTS = System.currentTimeMillis();
		if(lastUpdatedTS  ==-1) {
			lastUpdatedTS = nowTS;
		}
		while ((nowTS - lastUpdatedTS) < waitingSeconds*1000) {
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e);
				break;
			}
			nowTS = System.currentTimeMillis();
		}
		String info = getNotification();
		String subject = "APP Startup Infomation - Active Profile: " + AppUtil.getSpringActiveProfile() + " - "
				+ new Date();
		try {
			logger.info("sendStartupNotification() - email subject = \n" + subject);
			logger.info("sendStartupNotification() - email content = \n" + info);
			AppComponents.emailService.sendITNotification(subject, info);
			logger.info("sendStartupNotification() - end....");
		} catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
		}
	}
	
	private String getNotification() {
		String str = "\r\nStartup Notification Info:\r\n\r\n";
		Set<String> keySet = infoMap.keySet();
		for (String key : keySet) {
			str = str + key + ":\r\n" + infoMap.get(key) + "\r\n\r\n";
		}
		return str;
	}


}
