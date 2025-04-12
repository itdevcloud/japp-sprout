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
package com.itdevcloud.japp.core.session;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.MfaInfo;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.SessionServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class SessionService implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(SessionService.class);

	@PostConstruct
	public void init() {
		//try to avoid using AppConfig Service, AppComponents.appConfigCache may be not fully initiated yet
	}

	public MfaInfo getMfaInfoFromSessionRepo(String userSessionId) {
		if (StringUtil.isEmptyOrNull(userSessionId)) {
			String err = "getMfaInfoFromSessionRepo() - The luserSessionId is null or empty. check code!";
			logger.error(err);
			return null;
		}
		SessionServiceHelperI helper = AppFactory.getComponent(SessionServiceHelperI.class);
		MfaInfo mfaInfo = helper.getMfaInfoFromSessionRepo(userSessionId);
		return mfaInfo;
	}

	public void setMfaInfoFromSessionRepo(String userSessionId, MfaInfo mfaInfo) {
		if (StringUtil.isEmptyOrNull(userSessionId)) {
			String err = "setMfaInfoFromSessionRepo() - The luserSessionId is null or empty. check code!";
			logger.error(err);
			return;
		}
		SessionServiceHelperI helper = AppFactory.getComponent(SessionServiceHelperI.class);
		helper.setMfaInfoToSessionRepo(userSessionId, mfaInfo);
		return;
	}

	public String getValueFromSessionRepo(String userSessionId, String key) {
		if (StringUtil.isEmptyOrNull(userSessionId) || StringUtil.isEmptyOrNull(key)) {
			String err = "getValueFromSessionRepo() - The luserSessionId or Key is null or empty. check code!";
			logger.error(err);
			return null;
		}
		SessionServiceHelperI helper = AppFactory.getComponent(SessionServiceHelperI.class);
		String value = helper.getValueFromSessionRepo(userSessionId, key);
		return value;
	}

	public void setValueToSessionRepo(String userSessionId, String key, String value) {
		if (StringUtil.isEmptyOrNull(userSessionId) || StringUtil.isEmptyOrNull(key)) {
			String err = "setValueToSessionRepo() - The luserSessionId or Key is null or empty. check code!";
			logger.error(err);
			return;
		}
		SessionServiceHelperI helper = AppFactory.getComponent(SessionServiceHelperI.class);
		helper.setValueToSessionRepo(userSessionId, key, value);
		return;

	}

	public String getUserIaaUIDFromSessionRepo(String userSessionId) {
		String value = getValueFromSessionRepo(userSessionId, AppConstant.SMS_KEY_USER_IAA_UID);
		return value;
	}
	public void setUserIaaUIDToSessionRepo(String userSessionId, String userIaaUID) {
		setValueToSessionRepo(userSessionId, AppConstant.SMS_KEY_USER_IAA_UID, userIaaUID);
		return ;
	}
	public String getUserProfileUIDFromSessionRepo(String userSessionId) {
		String value = getValueFromSessionRepo(userSessionId, AppConstant.SMS_KEY_USER_PROFILE_UID);
		return value;
	}
	public void setUserProfileUIDToSessionRepo(String userSessionId, String userProfileUID) {
		setValueToSessionRepo(userSessionId, AppConstant.SMS_KEY_USER_PROFILE_UID, userProfileUID);
		return ;
	}

}
