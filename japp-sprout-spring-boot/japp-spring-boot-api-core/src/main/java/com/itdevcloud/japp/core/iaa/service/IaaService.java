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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.AuthProviderVO;
import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;

import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;
 
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class IaaService implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(IaaService.class);

	@PostConstruct
	public void init() {
		//try to avoid using AppConfig Service, AppComponents.appConfigCache may be not fully initiated yet
	}
	
	public AppIaaUser loginByLoginIdPassword(String loginId, String password, String authnProvider) {
		if (StringUtil.isEmptyOrNull(loginId) || StringUtil.isEmptyOrNull(password)) {
			String err = "loginByLoginIdPassword() - The login and/or password is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);

		//cache should not be used for login 
		AppIaaUser iaaUser = helper.getIaaUserFromRepositoryByLoginId(loginId, authnProvider);
		if (iaaUser == null) {
			logger.info("loginByLoginIdPassword() - The user loginId = '" + loginId + "' is not found in the repository.....");
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR,
					"The user '" + loginId + "' is not found in repository....");
		}
		String hashedPwd = Hasher.getHash(password, iaaUser.getHashedAlgorithm());
		if (!hashedPwd.equals(iaaUser.getHashedPassword())) {
			logger.info("loginByLoginIdPassword() - password does not match for login user '" + loginId
					+ "' from repository.....");
			return null;
		} else {
			AppComponents.iaaUserCache.addIaaUser(iaaUser);
			AppThreadContext.setAppIaaUser(iaaUser);
			return iaaUser;
		}
	}

	public AppIaaUser getIaaUserByUserId(String userId) {
		if (StringUtil.isEmptyOrNull(userId)) {
			String err = "getIaaUserByUserId() - The user \" + userId + \"is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		AppIaaUser user = AppComponents.iaaUserCache.getIaaUserByUserId(userId);
		if (user == null) {
			user = helper.getIaaUserFromRepositoryByUserIaaUID(userId);
		}
		if (user == null) {
			logger.info("getIaaUser() - The user '" + userId + "' is not found.");
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "The user '" + userId + "' is not found.");
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setAppIaaUser(user);
		return user;
	}
	
	public AppIaaUser getIaaUserByLoginId(String loginId, String authnProvider) {
		if (StringUtil.isEmptyOrNull(loginId)) {
			String err = "getIaaUserByLoginId() - The user \" + loginId + \"is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		AppIaaUser user = helper.getIaaUserFromRepositoryByLoginId(loginId, authnProvider);
		if (user == null) {
			logger.info("getIaaUser() - The user is not found. for login Id: " + loginId);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "The user is not found. for login Id: " + loginId);
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setAppIaaUser(user);
		return user;
	}

	public Map<String, Object> getTokenClaims(AppIaaUser iaaUser) {
		if (iaaUser == null) {
			return null;
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		Map<String, Object> claims = helper.getJappTokenClaims(iaaUser);
		//add JAPP token mandatory claims
		if(claims == null) {
			claims = new HashMap<>();
		}
		if(!claims.containsKey("jti")) {
			claims.put("jti", UUID.randomUUID().toString());
		}
		if(!claims.containsKey("userId")) {
			claims.put("userId", iaaUser.getUserIaaUID());
		}
		if(!claims.containsKey("user")) {
			claims.put("user", iaaUser.getLoginId());
		}
		if(!claims.containsKey("email")) {
			claims.put("email", iaaUser.getEmail());
		}
		if(!claims.containsKey("name")) {
			claims.put("name", iaaUser.getName());
		}

//		if(!claims.containsKey("busRole")) {
//			claims.put("busRole", iaaUser.getBusinessRoles());
//		}
//		if(!claims.containsKey("appRole")) {
//			claims.put("appRole", iaaUser.getApplicationRoles());
//		}
		return claims;
	}
	
	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRole) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.isAccessAllowed(userId, targetNodeId, targetRole);
	}

	
	public AppIaaUser getDummyIaaUserByUserId(String userId) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getDummyIaaUserByUserId(userId);
	}
	
	public List<IaaAppVO> getIaaAppInfo() {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getIaaAppInfo();
	}
	
	public List<AuthProviderVO> getAuthProviderInfo() {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getAuthProviderInfo();
	}

	public String getAuthnProviderURL(HttpServletRequest httpRequest, IaaAppVO iaaAppVO, String stateString) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getAuthnProviderURL(httpRequest, iaaAppVO, stateString);
	}

}
