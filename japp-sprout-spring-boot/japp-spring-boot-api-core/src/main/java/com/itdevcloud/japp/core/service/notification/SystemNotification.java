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

import java.io.Serializable;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class SystemNotification implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(SystemNotification.class);

	private String category = null;
	private Date notifyDateTime = null;
	private String content = null;

	public SystemNotification() {
		super();
		this.category = SystemNotifyService.CATEGORY_NONE;
		this.notifyDateTime = null;
		this.content = null;
	}


	public SystemNotification(String category, String notifyDateTimeStr, String content) {
		super();
		this.category = category;
		this.content = content;
		try {
			this.notifyDateTime = StringUtil.isEmptyOrNull(notifyDateTimeStr) ? null : SystemNotifyService.notificationDateTimeStringFormat.parse(notifyDateTimeStr);
		} catch (Exception e) {
			this.notifyDateTime = null;
			logger.error("notifyDateTimeStr must use 'yyyy-MM-dd HH:mm' format, can't parse notifyDateTimeStr, use null value!");
			logger.error(CommonUtil.getStackTrace(e));
		}
	}

	public SystemNotification(String category, Date notifyDateTime, String content) {
		super();
		this.category = category;
		this.notifyDateTime = notifyDateTime;
		this.content = content;
	}

	public String getCategory() {
		if(StringUtil.isEmptyOrNull(this.category)) {
			this.category = SystemNotifyService.CATEGORY_NONE;
		}
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}



	public Date getNotifyDateTime() {
		return notifyDateTime;
	}


	public void setNotifyDateTime(String notifyDateTimeStr) {
		try {
			this.notifyDateTime = StringUtil.isEmptyOrNull(notifyDateTimeStr) ? null : SystemNotifyService.notificationDateTimeStringFormat.parse(notifyDateTimeStr);
		} catch (Exception e) {
			this.notifyDateTime = null;
			logger.error("notifyDateTimeStr must use 'yyyy-MM-dd HH:mm' format, can't parse notifyDateTimeStr, use null value!");
			logger.error(CommonUtil.getStackTrace(e));
		}
	}
	public void setNotifyDateTime(Date notifyDateTime) {
		this.notifyDateTime = notifyDateTime;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}



}
