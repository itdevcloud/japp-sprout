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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class SystemNotifyService implements AppFactoryComponentI{

	//private static final Logger logger = LogManager.getLogger(SystemNotifyService.class);
	private static final Logger logger = LogManager.getLogger(SystemNotifyService.class);

	public static final DateFormat notificationDateTimeStringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final String CATEGORY_NONE = "Category (n/a)";
	public static final String CATEGORY_PERFORMANCE_WARNING = "PERFORMANCE WARNING";
	public static final String CATEGORY_PERFORMANCE_ALERT = "PERFORMANCE ALERT";

	private static Map<String, List<SystemNotification>> notificationMap = null;

	
	@PostConstruct
	public void init() {
	}

	/**
	 * Add a SystemNotification object into the map notificationMap property.
	 * @param notification SystemNotification
	 * @return true if a notification is added into the notificationMap successfully
	 */
	public boolean addNotification(SystemNotification notification) {
		if (notification == null) {
			logger.warn("addNotification()...notification parameter is null, do nothing......");
			return false;
		}
		String category = notification.getCategory();
		String content = notification.getContent();
		Date dateTime = notification.getNotifyDateTime();
		if (StringUtil.isEmptyOrNull(content)) {
			logger.warn("addNotification()...notification content is null or empty, do nothing......");
			return false;
		}
		
		if (notificationMap == null) {
			notificationMap = new HashMap<String, List<SystemNotification>>();
		}
		List<SystemNotification> list = notificationMap.get(category);
		if (list == null) {
			list = new ArrayList<SystemNotification>();
		}
		list.add(notification);
		notificationMap.put(category, list);
		return true;
	}

	public void sendNotification(boolean isScheduled) {
		Set<String> keySet = null;
		if (notificationMap == null || ((keySet = notificationMap.keySet()).isEmpty()) ) {
			logger.debug("notificationMap or keySet is null or empty, do nothing...");
			return;
		}
		String appName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		Date now = new Date();
		String today = notificationDateTimeStringFormat.format(now);
		String env = AppUtil.getSpringActiveProfile();
		String subject = appName + " - System Notification - " + env + " - " + today;
		if(isScheduled) {
			subject = appName + " - Scheduled System Notification - " + env + " - " + today;
		}else {
			subject = appName + " - Adhoc System Notification - " + env + " - " + today;
		}
		boolean emailEnabled = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_APP_SYSTEM_NOTIFICATION_EMAIL_ENABLED);
		StringBuffer notificationSb = new StringBuffer(subject + ": \r\n");

		boolean foundContent = false;

		for (String key : keySet) {
			List<SystemNotification> list = notificationMap.get(key);
			if (list == null || list.isEmpty()) {
				continue;
			}
			notificationSb.append("\r\n" + key + " - total = " + list.size() + "\r\n");
			List<SystemNotification> tmpList = new ArrayList<SystemNotification>();

			for (SystemNotification notification : list) {
				String category = notification.getCategory();
				Date notifyDateTime = notification.getNotifyDateTime();
				String content = notification.getContent();
				if (StringUtil.isEmptyOrNull(content)) {
					continue;
				}
				if(isScheduled && notifyDateTime == null) {
					notificationSb.append(content + "\r\n");
					foundContent = true;
					continue;
				}else if(!isScheduled && notifyDateTime != null && !now.before(notifyDateTime)) {
					notificationSb.append(content + "\r\n");
					foundContent = true;
					continue;
				}else {
					//wait for next run
					tmpList.add(notification);
					continue;
				}
			}
			if(!tmpList.isEmpty()) {
				notificationMap.put(key, tmpList);
			}
		}
		if(!foundContent) {
			return;
		}
		logger.info("System notification - content = " + notificationSb.toString());
		try {
			if (emailEnabled) {
				AppComponents.emailService.sendITNotification(subject, notificationSb.toString());
				notificationMap = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(AppUtil.getStackTrace(e));
		}
		return;

	}
}
