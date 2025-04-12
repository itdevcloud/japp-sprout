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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.BaseVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppIaaUser extends BaseVO {

	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String userIaaUID;
	private String userProfileUID;
	private String loginId;
	private String loginProvider;
	private String appId;
	private String userType;
	private String hashedPassword;
	private String hashedAlgorithm;
	private String name;
	private String email;
	private String phone;
	private String cidrWhitelist;
	private String assignedAgCodes;
	private MfaInfo mfaInfo;
	
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserIaaUID() {
		return userIaaUID;
	}
	public void setUserIaaUID(String usrIaaUID) {
		if(isFinalized) {
			return;
		}
		this.userIaaUID = usrIaaUID;
	}

	public String getUserProfileUID() {
		return userProfileUID;
	}
	public void setUserProfileUID(String userProfileUID) {
		if(isFinalized) {
			return;
		}
		this.userProfileUID = userProfileUID;
	}


	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getLoginProvider() {
		return loginProvider;
	}
	public void setLoginProvider(String loginProvider) {
		this.loginProvider = loginProvider;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getCidrWhitelist() {
		return cidrWhitelist;
	}
	public void setCidrWhitelist(String cidrWhitelist) {
		this.cidrWhitelist = cidrWhitelist;
	}
	public String getAssignedAgCodes() {
		return assignedAgCodes;
	}
	public void setAssignedAgCodes(String assignedAgCodes) {
		this.assignedAgCodes = assignedAgCodes;
	}
	
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	public String getHashedAlgorithm() {
		return hashedAlgorithm;
	}
	public void setHashedAlgorithm(String hashedAlgorithm) {
		this.hashedAlgorithm = hashedAlgorithm;
	}
	
	
	public MfaInfo getMfaInfo() {
		return mfaInfo;
	}
	public void setMfaInfo(MfaInfo mfaInfo) {
		this.mfaInfo = mfaInfo;
	}
	@Override
	public String getUID() {
		return userIaaUID;
	}
	@Override
	public String toString() {
		return "AppIaaUser [userIaaUID=" + userIaaUID + ", userProfileUID=" + userProfileUID + ", loginId=" + loginId
				+ ", loginProvider=" + loginProvider + ", appId=" + appId + ", userType=" + userType + ", name=" + name
				+ ", email=" + email + ", phone=" + phone + ", cidrWhitelist=" + cidrWhitelist + ", sessionId="
				+ assignedAgCodes + ", assignedAgCodes="
						+ sessionId + ", mfaInfo=" + mfaInfo + "]";
	}


}
