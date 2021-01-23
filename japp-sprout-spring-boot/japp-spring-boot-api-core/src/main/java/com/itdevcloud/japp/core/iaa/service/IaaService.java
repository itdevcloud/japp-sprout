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

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
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
	}
	
	public IaaUser loginByLoginIdPassword(String loginId, String password, String... loginSpType) {
		if (StringUtil.isEmptyOrNull(loginId) || StringUtil.isEmptyOrNull(password)) {
			String err = "loginByLoginIdPassword() - The login and/or password is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);

		//cache should not be used for login 
		String hashedPwd = Hasher.hashPassword(password);;
		IaaUser user = helper.getIaaUserFromRepositoryByLoginId(loginId, loginSpType);
		if (user == null) {
			logger.info("loginByLoginIdPassword() - The user loginId = '" + loginId + "' is not found in the repository.....");
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR,
					"The user '" + loginId + "' is not found in repository....");
		}
		if (!hashedPwd.equals(user.getCurrentHashedPassword())) {
			logger.info("loginByLoginIdPassword() - password does not match for login user '" + loginId
					+ "' from repository.....");
			return null;
		} else {
			AppComponents.iaaUserCache.addIaaUser(user);
			AppThreadContext.setIaaUser(user);
			return user;
		}
	}

	public IaaUser getIaaUserByUserId(String userId) {
		if (StringUtil.isEmptyOrNull(userId)) {
			String err = "getIaaUserByUserId() - The user \" + userId + \"is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUser user = AppComponents.iaaUserCache.getIaaUserByUserId(userId);
		if (user == null) {
			user = helper.getIaaUserFromRepositoryByUserId(userId);
		}
		if (user == null) {
			logger.info("getIaaUser() - The user '" + userId + "' is not found.");
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "The user '" + userId + "' is not found.");
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setIaaUser(user);
		return user;
	}
	
	public IaaUser getIaaUserByLoginId(String loginId, String... loginProvider) {
		if (StringUtil.isEmptyOrNull(loginId)) {
			String err = "getIaaUserByLoginId() - The user \" + loginId + \"is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUser user = helper.getIaaUserFromRepositoryByLoginId(loginId, loginProvider);
		if (user == null) {
			logger.info("getIaaUser() - The user is not found. for login Id: " + loginId);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "The user is not found. for login Id: " + loginId);
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setIaaUser(user);
		return user;
	}

	public Map<String, Object> getTokenClaims(IaaUser iaaUser) {
		if (iaaUser == null) {
			return null;
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		Map<String, Object> claims = helper.getJappTokenClaims(iaaUser);
		//add JAPP token mandatory claims
		if(claims == null) {
			claims = new HashMap<>();
		}
		if(!claims.containsKey("userId")) {
			claims.put("userId", iaaUser.getUserId());
		}
		if(!claims.containsKey("user")) {
			claims.put("user", iaaUser.getCurrentLoginId());
		}
		if(!claims.containsKey("email")) {
			claims.put("email", iaaUser.getEmail());
		}
		if(!claims.containsKey("firstName")) {
			claims.put("firstName", iaaUser.getFirstName());
		}
		if(!claims.containsKey("lastName")) {
			claims.put("lastName", iaaUser.getLastName());
		}
		if(!claims.containsKey("busRole")) {
			claims.put("busRole", iaaUser.getBusinessRoles());
		}
		if(!claims.containsKey("appRole")) {
			claims.put("appRole", iaaUser.getApplicationRoles());
		}
		return claims;
	}
	//target role could be business role or application role
	public boolean isAccessAllowed(String targetRole) {
		IaaUser user = AppThreadContext.getIaaUser();
		if (user == null) {
			logger.info("isAccessAllowed() - The user can't be retrieved from ThreadContext, return false.");
			return false;
		}
		Set<String> brSet = user.getBusinessRoles();
		if (brSet == null || brSet.isEmpty()) {
			logger.info("isAccessAllowed() -  The user is not assigned to any business role........" + user);
			return false;
		}
		if (brSet.contains(targetRole)) {
			return true;
		}
		Set<String> arSet = user.getApplicationRoles();
		if (arSet == null || arSet.isEmpty()) {
			logger.info("isAccessAllowed() -  The user is not assigned to any application role........" + user);
			return false;
		}
		if (arSet.contains(targetRole)) {
			return true;
		}
		return false;
	}
	
	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRole) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.isAccessAllowed(userId, targetNodeId, targetRole);
	}

	
	public List<String> getUpdatedIaaUsers(long lastCheckTimestamp){
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getUpdatedIaaUsers(lastCheckTimestamp);
	}
	
	public String getAndSend2ndfactorValue(IaaUser iaaUser, String SecondFactorType) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getAndSend2ndfactorValue(iaaUser, SecondFactorType);
	}
	
	public IaaUser getDummyIaaUserByUserId(String userId) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getDummyIaaUserByUserId(userId);
	}
	
	public String getHashed2ndFactorValueFromRepositoryByUserId(String userId) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		return helper.getHashed2ndFactorValueFromRepositoryByUserId(userId);
	}

}
