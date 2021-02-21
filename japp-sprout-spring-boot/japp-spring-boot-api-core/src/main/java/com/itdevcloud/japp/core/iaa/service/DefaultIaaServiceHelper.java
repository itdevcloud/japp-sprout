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
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
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
	public IaaUserI getIaaUserFromRepositoryBySystemUid(String uid) {
		logger.info("getIaaUserFromRepositoryBySystemUid() begins ...");
		long start = System.currentTimeMillis();

		IaaUserI iaaUser = getDummyIaaUserByLoginId(uid);

		long end1 = System.currentTimeMillis();
		logger.info("getIaaUserFromRepositoryBySystemUid() end........ took " + (end1 - start) + " ms. " + uid);
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

//	@Override
//	public String getAndSend2factorValue(IaaUserI iaaUser, String secondFactorType) {
//		if (AppConstant.IAA_2NDFACTOR_TYPE_VERIFICATION_CODE.equalsIgnoreCase(secondFactorType)) {
//			String email = (iaaUser == null ? null : iaaUser.getEmail());
//			if (StringUtil.isEmptyOrNull(email)) {
//				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR,
//						"can't get email address to send the verification code!");
//			}
//			String subject = "verification code";
//			int length = 6;
//			boolean useLetters = false;
//			boolean useNumbers = true;
//			String content = RandomStringUtils.random(length, useLetters, useNumbers);
//			String toAddresses = email;
//			try {
//				AppComponents.emailService.sendEmail(subject, content, toAddresses);
//				return content;
//			} catch (Exception e) {
//				logger.error(CommonUtil.getStackTrace(e));
//				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, "can't send the verification code!", e);
//			}
//		}else if (AppConstant.IAA_2NDFACTOR_TYPE_TOTP.equalsIgnoreCase(secondFactorType)) {
//			return null;
//		}else {
//			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, " 2 factor auth type is not supported! secondFactorType = " + secondFactorType);
//		}
//	}

	@Override
	public IaaUserI getDummyIaaUserByLoginId(String loginId, String... args) {
		IaaUserI iaaUser = new DefaultIaaUser();
		if (StringUtil.isEmptyOrNull(loginId)) {
			iaaUser.setSystemUid("uid-1");
			iaaUser.setLoginId("loginId-1");
		} else {
			iaaUser.setSystemUid(loginId);
			iaaUser.setLoginId(loginId);
		}
		//password is 12345
		iaaUser.setHashedPassword(
				"NieQminDE4Ggcewn98nKl3Jhgq7Smn3dLlQ1MyLPswq7njpt8qwsIP4jQ2MR1nhWTQyNMFkwV19g4tPQSBhNeQ==");
		iaaUser.setName("John Smith");
		iaaUser.setEmail("john.smith@dummy.ca");
		iaaUser.setTotpSecret("E47CWVVTI7BAXDD3");
		return iaaUser;
	}


	@Override
	public IaaUserI getIaaUserFromRepositoryByLoginId(String loginId, String... args) {
		return getIaaUserFromRepositoryBySystemUid(loginId);
	}

	@Override
	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRoles) {
		return true;
	}

	@Override
	public Class<?> getInterfaceClass() {
		return IaaServiceHelperI.class;
	}






}
