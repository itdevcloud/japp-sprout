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
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class DefaultIaaUser implements IaaUserI, Serializable {

	private static final long serialVersionUID = 1L;
	private String systemUid;
	private String loginId;
	private String identityProvider;
	private String mfaStatus;
	private String userType;
	private String applicationId;
	private String hashedPassword;
	private String hashAlgorithm;
	private String name;
	private String email;
	private String phone;
	private String mfaType;
	private String totpSecret;
	private List<String> cidrWhiteList;
	private Set<String> applicationRoles;
	private Set<String> businessRoles;
	private Set<String> authGroups;
	
	@Override
	public String getSystemUid() {
		return systemUid;
	}
	@Override
	public void setSystemUid(String systemUid) {
		this.systemUid = systemUid;
	}
	@Override
	public String getLoginId() {
		return loginId;
	}
	@Override
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	@Override
	public String getApplicationId() {
		return applicationId;
	}
	@Override
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	@Override
	public String getHashedPassword() {
		return hashedPassword;
	}
	@Override
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	@Override
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	@Override
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getEmail() {
		return email;
	}
	@Override
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String getPhone() {
		return phone;
	}
	@Override
	public void setPhone(String phone) {
		this.phone = phone;
	}
	

	@Override
	public String getTotpSecret() {
		return totpSecret;
	}
	@Override
	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}
	@Override
	public List<String> getCidrWhiteList() {
		return cidrWhiteList;
	}
	@Override
	public void setCidrWhiteList(List<String> cidrWhiteList) {
		this.cidrWhiteList = cidrWhiteList;
	}
	
	@Override
	public Set<String> getApplicationRoles() {
		if(applicationRoles==null) {
			applicationRoles = new HashSet<String>();
		}
		return new HashSet<String>(applicationRoles);
	}
	@Override
	public void setApplicationRoles(Set<String> applicationRoles) {
		this.applicationRoles = (applicationRoles==null?new HashSet<String>():new HashSet<String>(applicationRoles));
	}
	
	@Override
	public Set<String> getBusinessRoles() {
		if(businessRoles==null) {
			businessRoles = new HashSet<String>();
		}
		return new HashSet<String>(businessRoles);
	}
	@Override
	public void setBusinessRoles(Set<String> businessRoles) {
		this.businessRoles = (businessRoles==null?new HashSet<String>():new HashSet<String>(businessRoles));
	}

	@Override
	public Set<String> getAuthGroups() {
		if(authGroups==null) {
			authGroups = new HashSet<String>();
		}
		return new HashSet<String>(authGroups);
	}
	@Override
	public void setAuthGroups(Set<String> authGroups) {
		this.authGroups = (authGroups==null?new HashSet<String>():new HashSet<String>(authGroups));
	}
	
	@Override
	public String getUserType() {
		return userType;
	}
	@Override
	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	@Override
	public Class<?> getInterfaceClass() {
		return IaaUserI.class;
	}
	@Override
	public String getIdentityProvider() {
		return this.identityProvider;
	}
	@Override
	public void setIdentityProvider(String idp) {
		this.identityProvider = idp;
		
	}
	@Override
	public String getMfaStatus() {
		return this.mfaStatus;
	}
	@Override
	public void setMfaStatus(String mfaStatus) {
		this.mfaStatus = mfaStatus;
		
	}
	

}
