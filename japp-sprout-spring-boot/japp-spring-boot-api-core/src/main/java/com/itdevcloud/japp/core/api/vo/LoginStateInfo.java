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
package com.itdevcloud.japp.core.api.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.KeySecretVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class LoginStateInfo  {
	
	private static final Logger logger = LogManager.getLogger(LoginStateInfo.class);

	private String appId;
	private String loginId;
	private String authnCallbackURL ;

	public static LoginStateInfo parseStateString(String stateString) {
		if(StringUtil.isEmptyOrNull(stateString)) {
			return null;
		}
		String decodedUrl = AppUtil.getBase64UrlDecodedUrlString(stateString);
		String[] stateStrArr = decodedUrl.split(",");
		LoginStateInfo loginStateInfo = new LoginStateInfo();
		
		if(StringUtil.isEmptyOrNull(stateStrArr[0]) || "n/a".equalsIgnoreCase(stateStrArr[0])) {
			loginStateInfo.setAppId(null);
		}else {
			loginStateInfo.setAppId(stateStrArr[0]);
		}
		if(stateStrArr.length >= 2) {
			if(StringUtil.isEmptyOrNull(stateStrArr[1]) || "n/a".equalsIgnoreCase(stateStrArr[1])) {
				loginStateInfo.setLoginId(null);
			}else {
				loginStateInfo.setLoginId(stateStrArr[1]);
			}
		}
		if(stateStrArr.length >= 3) {
			if(StringUtil.isEmptyOrNull(stateStrArr[2]) || "n/a".equalsIgnoreCase(stateStrArr[2])) {
				loginStateInfo.setAuthnCallbackURL(null);
			}else {
				loginStateInfo.setAuthnCallbackURL(stateStrArr[2]);
			}
		}
		return loginStateInfo;
		
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getAuthnCallbackURL() {
		return authnCallbackURL;
	}
	public void setAuthnCallbackURL(String authnCallbackURL) {
		this.authnCallbackURL = authnCallbackURL;
	}
	
	public String createStateString() {
		String value = (StringUtil.isEmptyOrNull(appId)?"n/a" : appId.trim()) 
				+ "," + (StringUtil.isEmptyOrNull(loginId)?"n/a":loginId.trim())
				+ "," + (StringUtil.isEmptyOrNull(authnCallbackURL)?"n/a":authnCallbackURL.trim());
		String encodedString = AppUtil.getBase64UrlEncodedString(value);
		return encodedString;
	}
	
	

	@Override
	public String toString() {
		return "LoginStateInfo [appId=" + appId + ", loginId=" + loginId + ", authnCallbackURL=" + authnCallbackURL + "]";
	}
	

}
