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

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.api.vo.MfaInfo;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.SessionServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * The DefaultIaaServiceHelper class is a default implementation class of the interface IaaServiceHelperI.
 * If there is no other class to implement the interface IaaServiceHelperI, the system will use this one to 
 * retrieve user information.
 *
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class DefaultSessionServiceHelper implements SessionServiceHelperI {
 
	//private static final Logger logger = LogManager.getLogger(DefaultIaaServiceHelper.class);
	private static final Logger logger = LogManager.getLogger(DefaultSessionServiceHelper.class);
	private static Map<String, String> sessionMap = null;
			
	@PostConstruct
	private void init() {
	}


	@Override
	public Class<?> getInterfaceClass() {
		return SessionServiceHelperI.class;
	}

	@Override
	public MfaInfo getMfaInfoFromSessionRepo(String userSessionId) {
		if(sessionMap == null || sessionMap.isEmpty() || StringUtil.isEmptyOrNull(userSessionId)) {
			return null;
		}
		String jsonString = sessionMap.get(userSessionId  +"-MFA");
		MfaInfo mfaInfo = AppUtil.getObjectFromJsonString(jsonString, MfaInfo.class);
		logger.info("..........mfaVOList = " + mfaInfo );
		return mfaInfo;
	}

	@Override
	public void setMfaInfoToSessionRepo(String userSessionId, MfaInfo mfaInfo) {
		if(sessionMap == null) {
			sessionMap = new HashMap<String, String>();
		}
		String jsonString = AppUtil.getJsonStringFromObject(mfaInfo);
		logger.info("..........jsonString = " + jsonString);
		
		sessionMap.put(userSessionId + "-MFA", jsonString);
	}

	@Override
	public String getValueFromSessionRepo(String userSessionId, String key) {
		Object obj = sessionMap.get(userSessionId  +"-SESSION-" + key);
		return obj == null?null:""+obj;
	}

	@Override
	public void setValueToSessionRepo(String userSessionId, String key, String value) {
		sessionMap.put(userSessionId + "-SESSION-" + key, value);
		
	}


}
