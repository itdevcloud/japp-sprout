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
package com.itdevcloud.japp.core.iaa.service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class IaaUser <T> implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userId;
	private String currentLoginId;
	private String currentLoginSpType;
	private String currentAppId;
	private String currentHashedPassword;
	private String currentHashedAlgorithm;
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String phone;
	private String twoFactorAuthType;
	private String totpSecret;
	private List<String> cidrWhiteList;
	private Set<String> applicationRoles;
	private Set<String> businessRoles;
	private Set<String> authGroups;
	private String userType;
	
	private T applicationUser;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCurrentLoginId() {
		return currentLoginId;
	}
	public void setCurrentLoginId(String currentLoginId) {
		this.currentLoginId = currentLoginId;
	}
	public String getCurrentLoginSpType() {
		return currentLoginSpType;
	}
	public void setCurrentLoginSpType(String currentLoginSpType) {
		this.currentLoginSpType = currentLoginSpType;
	}
	public String getCurrentAppId() {
		return currentAppId;
	}
	public void setCurrentAppId(String currentAppId) {
		this.currentAppId = currentAppId;
	}
	public String getCurrentHashedPassword() {
		return currentHashedPassword;
	}
	public void setCurrentHashedPassword(String currentHashedPassword) {
		this.currentHashedPassword = currentHashedPassword;
	}
	public String getCurrentHashedAlgorithm() {
		return currentHashedAlgorithm;
	}
	public void setCurrentHashedAlgorithm(String currentHashedAlgorithm) {
		this.currentHashedAlgorithm = currentHashedAlgorithm;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName =  (firstName==null?null:firstName.trim());
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = (middleName==null?null:middleName.trim());
	}
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = (lastName==null?null:lastName.trim());
	}

	public String getFullName() {
		String fullName = null;
		if(StringUtil.isEmptyOrNull(this.middleName )) {
			fullName = StringUtil.changeFirstCharCase(this.firstName, true) + " " + StringUtil.changeFirstCharCase(this.lastName, true);
		}else {
			fullName = StringUtil.changeFirstCharCase(this.firstName, true) + " " + StringUtil.changeFirstCharCase(this.middleName, true) + " " + StringUtil.changeFirstCharCase(this.lastName, true);
		}
		return fullName;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email =  (email==null?null:email.trim());
	}

	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}



	public String getTwoFactorAuthType() {
		if(StringUtil.isEmptyOrNull(twoFactorAuthType) ) {
			twoFactorAuthType = AppConstant.IAA_2NDFACTOR_TYPE_NONE;
		}
		return twoFactorAuthType;
	}
	public void setTwoFactorAuthType(String twoFactorAuthType) {
		if(StringUtil.isEmptyOrNull(twoFactorAuthType) ) {
			twoFactorAuthType = AppConstant.IAA_2NDFACTOR_TYPE_NONE;
		}
		this.twoFactorAuthType = twoFactorAuthType.trim();
	}
	
	public String getTotpSecret() {
		return totpSecret;
	}
	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}
	public List<String> getCidrWhiteList() {
		return cidrWhiteList;
	}
	public void setCidrWhiteList(List<String> cidrWhiteList) {
		this.cidrWhiteList = cidrWhiteList;
	}

	public Set<String> getApplicationRoles() {
		if(applicationRoles==null) {
			applicationRoles = new HashSet<String>();
		}
		return new HashSet<String>(applicationRoles);
	}
	public void setApplicationRoles(Set<String> applicationRoles) {
		this.applicationRoles = (applicationRoles==null?new HashSet<String>():new HashSet<String>(applicationRoles));
	}
	public Set<String> getBusinessRoles() {
		if(businessRoles==null) {
			businessRoles = new HashSet<String>();
		}
		return new HashSet<String>(businessRoles);
	}
	public void setBusinessRoles(Set<String> businessRoles) {
		this.businessRoles = (businessRoles==null?new HashSet<String>():new HashSet<String>(businessRoles));
	}

	public Set<String> getAuthGroups() {
		if(authGroups==null) {
			authGroups = new HashSet<String>();
		}
		return new HashSet<String>(authGroups);
	}
	public void setAuthGroups(Set<String> authGroups) {
		this.authGroups = (authGroups==null?new HashSet<String>():new HashSet<String>(authGroups));
	}
	
	public T getApplicationUser() {
		return applicationUser;
	}
	public void setApplicationUser(T applicationUser) {
		this.applicationUser = applicationUser;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@Override
	public String toString() {
		return "IaaUser [userId=" + userId + ", currentLoginId=" + currentLoginId + ", currentLoginSpType="
				+ currentLoginSpType + ", currentAppId=" + currentAppId + ", currentHashedPassword="
				+ currentHashedPassword + ", currentHashedAlgorithm=" + currentHashedAlgorithm + ", firstName="
				+ firstName + ", middleName=" + middleName + ", lastName=" + lastName + ", email=" + email + ", phone="
				+ phone + ", twoFactorAuthType=" + twoFactorAuthType + ", totpSecret=" + totpSecret + ", cidrWhiteList="
				+ cidrWhiteList + ", applicationRoles=" + applicationRoles + ", businessRoles=" + businessRoles
				+ ", authGroups=" + authGroups + "]";
	}


}
