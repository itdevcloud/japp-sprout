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

import java.util.List;
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

	@PostConstruct
	public void init() {
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
	
	public IaaUserI loginByToken(String token) {
		if (StringUtil.isEmptyOrNull(token)) {
			String err = "loginByToken() - The token is null or empty. check code!";
			logger.error(err);
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, err);
		}
		IaaUserI iaaUser = AppComponents.iaaService.validateTokenAndRetrieveIaaUser(token);;
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

	public IaaUserI validateTokenAndRetrieveIaaUser(String token) {
		
		
		if (StringUtil.isEmptyOrNull(token)) {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "E204: invlad token!");
		}
		TokenHandlerI tokenHandler = AppComponents.jwtService.getAccessTokenHandler();
		IaaUserI iaaUser = null;
		boolean isValidToken = tokenHandler.isValidToken(token, null);
		if(!isValidToken) {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "E204: invlad token!");
		}
		iaaUser = tokenHandler.getIaaUser(token);
		
		return iaaUser;
	}

//	public String issueToken(IaaUserI iaaUser, SecondFactorInfo secondFactorInfo) {
//
//		String token = null;
//		int expireMins = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH);
//		try {
//			String secondFactorType = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_2NDFACTOR_TYPE);
//			SecondFactorInfo secondFactorInfo = new SecondFactorInfo();
//			String user2ndFactorType = iaaUser.getMfaType();
//			
//			if (user2ndFactorType.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE)){
//				if ((secondFactorType.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE))){
//					secondFactorInfo.setType(secondFactorType);
//					secondFactorInfo.setVerified(false);
//					secondFactorInfo.setValue(null);
//					expireMins = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
//				}else {
//					secondFactorInfo.setType(secondFactorType);
//					secondFactorInfo.setVerified(false);
//					String tmpV = AppComponents.iaaService.getAndSend2factorValue(iaaUser, secondFactorType);
//					secondFactorInfo.setValue(Hasher.hashPassword(tmpV));
//				}
//			} else {
//				secondFactorInfo.setType(user2ndFactorType);
//				secondFactorInfo.setVerified(false);
//				String tmpV = AppComponents.iaaService.getAndSend2factorValue(iaaUser, user2ndFactorType);
//				secondFactorInfo.setValue(Hasher.hashPassword(tmpV));
//			}
//			Key key = AppComponents.pkiKeyCache.getJappPrivateKey();
//			TokenHandlerI tokenHandler = AppComponents.jwtService.getAccessTokenHandler();
//			token = tokenHandler.issueToken(iaaUser, key, expireMins, secondFactorInfo);
//			return token;
//		} catch (Throwable t) {
//			logger.error(CommonUtil.getStackTrace(t));
//			return null;
//		}
//	}
	
	//target role could be business role or application role
	public boolean isAccessAllowed(String targetRole) {
		IaaUserI user = AppThreadContext.getIaaUser();
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
	
//	public String getAndSend2factorValue(IaaUserI iaaUser, String SecondFactorType) {
//		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
//		return helper.getAndSend2factorValue(iaaUser, SecondFactorType);
//	}
//	
//	public String getHashed2FactorVerificationCodeFromRepositoryByUid(String uid) {
//		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
//		return helper.getHashed2FactorVerificationCodeFromRepositoryByUid(uid);
//	}
	
	public IaaUserI getDummyIaaUserByLoginId(String loginId, String... args) {
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		IaaUserI user =  helper.getDummyIaaUserByLoginId(loginId, args);
		AppThreadContext.setIaaUser(user);
		return user;

	}
	

}
