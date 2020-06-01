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
package com.itdevcloud.japp.core.service.notification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ScheduledSystemNotificationTimerTask {

	private static final Logger logger = LogManager.getLogger(ScheduledSystemNotificationTimerTask.class);



	/**
	 * It is a scheduled task to send out SystemNotification information at 15:00PM every day.
	 */
	@Scheduled(cron = "${jappcore.system.scheduled.notification.cron:0 0 15 * * ?}")
	public void run() {
		logger.info("SystemInfoNotificationTimerTask - run started .........");
		long start = System.currentTimeMillis();
		AppComponents.systemNotifyService.sendNotification(true);
		long end1 = System.currentTimeMillis();
		logger.info("SystemInfoNotificationTimerTask End...... took " + (end1 - start) + " ms.");

	}
}