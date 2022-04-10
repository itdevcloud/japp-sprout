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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.common.ProcessorTargetRoleUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
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

	private static Boolean allowEmptyTargetRole = null;
	
	@PostConstruct
	public void init() {
		if(allowEmptyTargetRole == null) {
			allowEmptyTargetRole = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_PROCESSOR_EMPTY_TARGET_ROLE_ALLOWED, false);
		}
	}
	
	public List<ClientAppInfo> getClientAppInfoList(){
		Set<ClientAppInfo> clientAppInfoSet = AppComponents.clientAppInfoCache.getAllClientAppInfo();
		List<ClientAppInfo> clientAppInfoList = new ArrayList<ClientAppInfo>(clientAppInfoSet);
		return clientAppInfoList;
	}
	public ClientAppInfo getClientAppInfo(String clientId){
		return AppComponents.clientAppInfoCache.getClientAppInfo(clientId);
	}

	public IaaUserI login(String loginId, String password, String authProvider) {
		if (StringUtil.isEmptyOrNull(loginId) || StringUtil.isEmptyOrNull(password)) {
			String err = "loginByLoginIdPassword() - The login and/or password is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);

		//cache should not be used for login 
		String hashedPwd = Hasher.hashPassword(password);;
		IaaUserI user = helper.getIaaUserFromRepositoryByLoginId(loginId, authProvider);
		if (user == null) {
			logger.info("loginByLoginIdPassword() - The user loginId = '" + loginId + "' is not found in the repository.....");
			return null;
		}
		if (!hashedPwd.equals(user.getHashedPassword())) {
			logger.info("loginByLoginIdPassword() - password does not match for login user '" + loginId
					+ "' from repository.....");
			return null;
		} else {
			AppComponents.iaaUserCache.addIaaUser(user);
			AppThreadContext.setIaaUser(user);
			return user;
		}
	}
	
	public IaaUserI loginByToken(String token, Map<String, String> claimEqualMatchMap, boolean ignoreNullInToken, String... args) {
		if (StringUtil.isEmptyOrNull(token)) {
			String err = "loginByToken() - The token is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaUserI iaaUser = AppComponents.iaaService.validateTokenAndRetrieveIaaUser(token, claimEqualMatchMap, ignoreNullInToken, args);
		return iaaUser;
	}

	public IaaUserI getIaaUserBySystemUid(String uid) {
		if (StringUtil.isEmptyOrNull(uid)) {
			String err = "getIaaUserBySystemUid() - The user (uid: " + uid + ") is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUserI user = AppComponents.iaaUserCache.getIaaUserBySystemUid(uid);
		if (user == null) {
			user = helper.getIaaUserFromRepositoryBySystemUid(uid);
		}
		if (user == null) {
			logger.info("getIaaUser() - The user '" + uid + "' is not found.");
			return null;
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setIaaUser(user);
		return user;
	}
	
	public IaaUserI getIaaUserFromRepositoryByLoginId(String loginId, String... args) {
		if (StringUtil.isEmptyOrNull(loginId)) {
			String err = "getIaaUserFromRepositoryByLoginId() - The user (loginId: " + loginId + ") is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUserI user = helper.getIaaUserFromRepositoryByLoginId(loginId, args);
		if (user == null) {
			logger.info("getIaaUser() - The user '" + loginId + "' is not found.");
			return null;
		}
		AppComponents.iaaUserCache.addIaaUser(user);
		AppThreadContext.setIaaUser(user);
		return user;
		
	}

	public IaaUserI validateTokenAndRetrieveIaaUser(String token, Map<String, String> claimEqualMatchMap, boolean ignoreNullInToken, String... args) {
		
		if (StringUtil.isEmptyOrNull(token)) {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "token is null!");
		}
		IaaUserI iaaUser = null;
		boolean isValidToken = AppComponents.jwtService.isValidToken(token, claimEqualMatchMap, ignoreNullInToken, args);
		if(!isValidToken) {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "Invalid Token!");
		}
		iaaUser = AppComponents.jwtService.getIaaUser(token);
		
		AppComponents.iaaUserCache.addIaaUser(iaaUser);
		AppThreadContext.setIaaUser(iaaUser);
		
		return iaaUser;
	}


	//target role could be business role or application role
	public boolean isAccessAllowed(String targetRole) {
		IaaUserI user = AppThreadContext.getIaaUser();
		if (user == null) {
			logger.info("isAccessAllowed() - The user can't be retrieved from ThreadContext, return false.");
			return false;
		}
		if (StringUtil.isEmptyOrNull(targetRole)) {
			if(allowEmptyTargetRole) {
				logger.debug("isAccessAllowed() - allowEmptyTargetRole is set to 'true', The target role is empty or null, return true.");
				return true;
			}else {
				logger.debug("isAccessAllowed() - allowEmptyTargetRole is set to 'false', The target role is empty or null, return false.");
				return false;
			}
		}
		if (targetRole.equalsIgnoreCase(ProcessorTargetRoleUtil.ROLE_ANY)) {
			return true;
		}
		Set<String> brSet = user.getBusinessRoles();
		if (! (brSet == null) && ! brSet.isEmpty()) {
			if (brSet.contains(targetRole)) {
				return true;
			}
		}
		Set<String> arSet = user.getApplicationRoles();
		if (! (arSet == null) && ! arSet.isEmpty()) {
			if (arSet.contains(targetRole)) {
				return true;
			}
		}
		Set<String> agSet = user.getAuthGroups();
		if (! (agSet == null) && ! agSet.isEmpty()) {
			if (agSet.contains(targetRole)) {
				return true;
			}
		}
		logger.info("isAccessAllowed() -  The user is not assigned to required Auth Group, Business Role or Application Role: " + targetRole + ", User = " + user.getLoginId());
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
	
	
	public IaaUserI getAnonymousIaaUserByLoginId(String loginId, String... args) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUserI user =  helper.getAnonymousIaaUserByLoginId(loginId, args);
		AppThreadContext.setIaaUser(user);
		return user;

	}
	

}
