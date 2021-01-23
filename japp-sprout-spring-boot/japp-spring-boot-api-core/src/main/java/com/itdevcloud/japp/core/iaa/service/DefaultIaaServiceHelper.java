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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
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
public class DefaultIaaServiceHelper implements IaaServiceHelperI {

	private static final Logger logger = LogManager.getLogger(DefaultIaaServiceHelper.class);

	@PostConstruct
	private void init() {
	}

	@Override
	public String getIaaUserIdByLoginId(String loginId, String... authSpType) {
		//default login Id = user Id
		if(StringUtil.isEmptyOrNull(loginId) || authSpType == null) {
			logger.error("getIaaUserIdByLoginId() loginId and/or loginProvider is null / empty ...");
			return null;
		}
		return loginId;

	}
	
	@Override
	public IaaUser getIaaUserFromRepositoryByUserId(String userId) {
		logger.info("getIaaUserFromRepository() begins ...");
		long start = System.currentTimeMillis();

		IaaUser iaaUser = getDummyIaaUserByUserId(userId);

		long end1 = System.currentTimeMillis();
		logger.info("getIaaUserFromRepository() end........ took " + (end1 - start) + " ms. " + userId);
		return iaaUser;
	}

	@Override
	public List<String> getUpdatedIaaUsers(long lastCheckTimestamp) {
		if (lastCheckTimestamp == -1) {
			return null;
		}
		ArrayList<String> idList = new ArrayList<>();
		return idList;
	}

	@Override
	public String getAndSend2ndfactorValue(IaaUser iaaUser, String secondFactorType) {
		// TODO Auto-generated method stub
		if (AppConstant.IAA_2NDFACTOR_TYPE_VERIFICATION_CODE.equalsIgnoreCase(secondFactorType)) {
			String email = (iaaUser == null ? null : iaaUser.getEmail());
			if (StringUtil.isEmptyOrNull(email)) {
				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR,
						"can't get email address to send the verification code!");
			}
			String subject = "verification code from JAPP";
			int length = 6;
			boolean useLetters = false;
			boolean useNumbers = true;
			String content = RandomStringUtils.random(length, useLetters, useNumbers);
			String toAddresses = email;
			try {
				AppComponents.emailService.sendEmail(subject, content, toAddresses);
				return content;
			} catch (Exception e) {
				logger.error(AppUtil.getStackTrace(e));
				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, "can't send the verification code!", e);
			}
		}else if (AppConstant.IAA_2NDFACTOR_TYPE_TOTP.equalsIgnoreCase(secondFactorType)) {
			return null;
		}else {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, " 2 factor auth type is not supported! secondFactorType = " + secondFactorType);
		}
	}

	@Override
	public IaaUser getDummyIaaUserByUserId(String userId) {
		IaaUser iaaUser = new IaaUser();
		if (StringUtil.isEmptyOrNull(userId)) {
			iaaUser.setUserId("userId");
			iaaUser.setCurrentLoginId("loginId");
		} else {
			iaaUser.setUserId(userId);
			iaaUser.setCurrentLoginId(userId);
		}
		iaaUser.setCurrentHashedPassword(
				"NieQminDE4Ggcewn98nKl3Jhgq7Smn3dLlQ1MyLPswq7njpt8qwsIP4jQ2MR1nhWTQyNMFkwV19g4tPQSBhNeQ==");
		iaaUser.setFirstName("DummyFirstName");
		iaaUser.setLastName("DummyLastName");
		iaaUser.setEmail("DummyEmail@JappApp.ca");
		//Base32.random();
		iaaUser.setTotpSecret("E47CWVVTI7BAXDD3");
		return iaaUser;
	}

	@Override
	public String getHashed2ndFactorValueFromRepositoryByUserId(String userId) {
		// return null means use hashed value in token
		return null;
	}

	@Override
	public Map<String, Object> getJappTokenClaims(IaaUser iaaUser) {
		if (iaaUser == null) {
			return null;
		}
		Map<String, Object> claims = new HashMap<>();
		claims.put("user", iaaUser.getCurrentLoginId());
		claims.put("userId", iaaUser.getUserId());
		claims.put("email", iaaUser.getEmail());
		claims.put("firstName", iaaUser.getFirstName());
		claims.put("lastName", iaaUser.getLastName());
		claims.put("busRole", iaaUser.getBusinessRoles());
		claims.put("appRole", iaaUser.getApplicationRoles());
		return claims;
	}

	@Override
	public List<UserAppSpMap> getAuthenticationSpType(String loginId, String... appId) {
		List<UserAppSpMap> mapList= new ArrayList<UserAppSpMap>();
		
		String spType = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER);
		if(!StringUtil.isEmptyOrNull(spType)) {
			UserAppSpMap map = new UserAppSpMap();
			map.setAppId(appId[0]);
			map.setLoginId(loginId);
			map.setSpType(spType);
			mapList.add(map);
		}
		return mapList;
	}

	@Override
	public IaaUser getIaaUserFromRepositoryByLoginId(String loginId, String... loginSpType) {
		return getIaaUserFromRepositoryByUserId(loginId);
	}

	@Override
	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRoles) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Class<?> getInterfaceClass() {
		return IaaServiceHelperI.class;
	}

}
