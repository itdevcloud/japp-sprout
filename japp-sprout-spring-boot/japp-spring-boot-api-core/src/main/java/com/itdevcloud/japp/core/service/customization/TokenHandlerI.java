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
package com.itdevcloud.japp.core.service.customization;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.se.common.util.DateUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface TokenHandlerI extends AppFactoryComponentI {

	public static final String JWT_CLAIM_KEY_ISSUER = "iss";
	public static final String JWT_CLAIM_KEY_IDENTITY_PROVIDER = "idp";
	public static final String JWT_CLAIM_KEY_APP_ID = "appid";
	public static final String JWT_CLAIM_KEY_ISSUE_AT = "iat";
	public static final String JWT_CLAIM_KEY_SUBJECT = "sub";
	public static final String JWT_CLAIM_KEY_AUDIENCE = "aud";
	public static final String JWT_CLAIM_KEY_EXPIRE = "exp";
	public static final String JWT_CLAIM_KEY_EXPIRE_LOCAL = "expLocal";
//	public static final String JWT_CLAIM_KEY_2NDFACTOR_VERIFIED = "2fVerified";
//	public static final String JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT = "2fRetry";
//	public static final String JWT_CLAIM_KEY_2NDFACTOR_TYPE = "2fType";
//	public static final String JWT_CLAIM_KEY_2NDFACTOR_VALUE = "2fValue";
	public static final String JWT_CLAIM_KEY_MFA_STATUS = "mfa";
	public static final String JWT_CLAIM_KEY_UID = "uid";
	public static final String JWT_CLAIM_KEY_LOGINID = "loginId";
	public static final String JWT_CLAIM_KEY_EMAIL = "email";
	public static final String JWT_CLAIM_KEY_NAME = "name";
	public static final String JWT_CLAIM_KEY_BUS_ROLES = "busRoles";
	public static final String JWT_CLAIM_KEY_APP_ROLES = "appRoles";
	public static final String JWT_CLAIM_KEY_AUTH_GROUPS = "authGroups";
	public static final String JWT_CLAIM_KEY_NOT_BEFORE = "nbf";
	public static final String JWT_CLAIM_KEY_PHONE = "phone";
	public static final String JWT_CLAIM_KEY_AAD_USERNAME = "upn";

	public boolean isValidToken(String token, Map<String, String> claimMatchMap, String... args);

	public IaaUserI getIaaUser(String token);

	public String issueAccessToken(IaaUserI iaaUser, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap);

	public String getAccessToken(String refreshToken);
	
	public static Map<String, Object> getDefaultAccessTokenClaims(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		LocalDateTime now = LocalDateTime.now().withNano(0);
		long nbf = DateUtil.LocalDateTimeToEpochSecond(now);
		LocalDateTime expireDateTime = now.plusMinutes(expireMinutes);
		long exp = DateUtil.LocalDateTimeToEpochSecond(expireDateTime);
		ZoneOffset zoneOffSet = ZonedDateTime.now().getOffset();
		String expLocal = expireDateTime.format(DateTimeFormatter.ISO_DATE_TIME) + zoneOffSet.toString();

		String iss = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TOKEN_ISSUE_ISS);
		
		Map<String, Object> claims = new HashMap<String, Object>();
		
		claims.put(JWT_CLAIM_KEY_ISSUER, iss);
		claims.put(JWT_CLAIM_KEY_ISSUE_AT, nbf);
		claims.put(JWT_CLAIM_KEY_NOT_BEFORE, nbf);
		claims.put(JWT_CLAIM_KEY_EXPIRE, exp);
		claims.put(JWT_CLAIM_KEY_EXPIRE_LOCAL, expLocal);
		claims.put(JWT_CLAIM_KEY_IDENTITY_PROVIDER, iaaUser.getIdentityProvider());
		claims.put(JWT_CLAIM_KEY_APP_ID, iaaUser.getApplicationId());
		claims.put(JWT_CLAIM_KEY_SUBJECT, iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_MFA_STATUS, iaaUser.getMfaStatus());
		claims.put(JWT_CLAIM_KEY_UID, iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_LOGINID, iaaUser.getLoginId());
		claims.put(JWT_CLAIM_KEY_EMAIL, iaaUser.getEmail());
		claims.put(JWT_CLAIM_KEY_NAME, iaaUser.getName());
		claims.put(JWT_CLAIM_KEY_PHONE, iaaUser.getPhone());
		claims.put(JWT_CLAIM_KEY_BUS_ROLES, iaaUser.getBusinessRoles());
		claims.put(JWT_CLAIM_KEY_APP_ROLES, iaaUser.getApplicationRoles());
		claims.put(JWT_CLAIM_KEY_AUTH_GROUPS, iaaUser.getAuthGroups());
		
//		if (secondFactorInfo != null) {
//			claims.put(JWT_CLAIM_KEY_2NDFACTOR_TYPE, secondFactorInfo.getType());
//			claims.put(JWT_CLAIM_KEY_2NDFACTOR_VERIFIED, secondFactorInfo.isVerified());
//			claims.put(JWT_CLAIM_KEY_2NDFACTOR_VALUE, secondFactorInfo.getValue());
//			claims.put(JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT, secondFactorInfo.getRetryCount());
//		}

		return claims;
	}

}
