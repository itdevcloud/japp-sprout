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
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaUser;
import com.itdevcloud.japp.core.iaa.token.AppLocalTokenHandler;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface TokenHandlerI extends AppFactoryComponentI {

	public static final String TYPE_ACCESS_TOKEN = "Access_Token";
	public static final String TYPE_ID_TOKEN = "Id_Token";
	public static final String TYPE_REFRESH_TOKEN = "Refresh_Token";

	public static final String JWT_CLAIM_KEY_ISSUER = "iss";
	public static final String JWT_CLAIM_KEY_TOKEN_TYPE = "type";
//	public static final String JWT_CLAIM_KEY_IDENTITY_PROVIDER = "idp";
	public static final String JWT_CLAIM_KEY_APP_ID = "appid";
	public static final String JWT_CLAIM_KEY_ISSUE_AT = "iat";
	public static final String JWT_CLAIM_KEY_IAT_LOCAL = "iatLocal";
	public static final String JWT_CLAIM_KEY_SUBJECT = "sub";
	public static final String JWT_CLAIM_KEY_AUDIENCE = "aud";
	public static final String JWT_CLAIM_KEY_AUTH_KEY = "authKey";
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
	public static final String JWT_CLAIM_KEY_HASHED_USERIP = "uip";
	public static final String JWT_CLAIM_KEY_HASHED_NONCE = "nonce";
	public static final String JWT_CLAIM_KEY_AAD_NONCE = "nonce";
	
	static final Logger logger = LogManager.getLogger(AppLocalTokenHandler.class);

	public boolean isValidToken(String token, Map<String, String> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) ;
	public IaaUserI getIaaUser(String token);
	public String issueToken(IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap);
	public String getAccessToken(String token);

	//beside dates etc., only check values in claimEqualMatchMap
	public static boolean isValidTokenDefault(String token, Map<String, String> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return false;
			}
			PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
			boolean isValid = AppComponents.jwtService.isValidTokenByPublicKey(token, publicKey);

			if (!isValid) {
				logger.error("token can not be validated by publickey..........");
				return false;
			}
			
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);
			
			String tokenType = (claims.get(JWT_CLAIM_KEY_TOKEN_TYPE)==null?null:"" + claims.get(JWT_CLAIM_KEY_TOKEN_TYPE));
			if (!TokenHandlerI.TYPE_ACCESS_TOKEN.equalsIgnoreCase(tokenType)
					&& !TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType)
					&& !TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType)) {
				logger.error("token is not Valid, missing token type..........");
				return false;
			}
	
			long nbf = (claims.get(JWT_CLAIM_KEY_NOT_BEFORE) == null? -1: Long.parseLong(""+claims.get(JWT_CLAIM_KEY_NOT_BEFORE)));
			long exp = (claims.get(JWT_CLAIM_KEY_EXPIRE) == null? -1: Long.parseLong(""+claims.get(JWT_CLAIM_KEY_EXPIRE)));
			
		
			LocalDateTime now = LocalDateTime.now().withNano(0);
			LocalDateTime expDateTime = DateUtil.epochSecondToLocalDateTime(exp, null);
			LocalDateTime nbfDateTime = DateUtil.epochSecondToLocalDateTime(nbf, null);
			if (exp < 0 || now.isAfter(expDateTime)) {
				logger.error("token exp claim is not valid......");
				return false;
  		    }
			if (nbf < 0 || now.isBefore(nbfDateTime)) {
				logger.error("token nbf claim is not valid.....");
				return false;
		    }
			if(claimEqualMatchMap == null || claimEqualMatchMap.isEmpty()) {
				//no claims need to be verified
				return true;
			}else {
				//this could include nonce and UIP
				Set<String> keySet = claimEqualMatchMap.keySet();
				for(String key: keySet) {
					String value = claimEqualMatchMap.get(key);
					String tokenValue = (claims.get(key)==null?null:""+claims.get(key));
					if(ingoreNullInToken) {
						if (StringUtil.isEmptyOrNull(tokenValue)) {
							continue;
						}
						if (tokenValue.equals(value)) {
							continue;
						}else {
							logger.error("token claim '" + key + "'is not valid, value - '" + tokenValue+"' doesn't match claimEqualMatchMap value - '"+ value + "'.....");
							return false;
						}
					}else {
						if (StringUtil.isEmptyOrNull(value)) {
							if(StringUtil.isEmptyOrNull(tokenValue)) {
								continue;
							}else {
								//matchMap value is null, token value is not null
								logger.error("claimEqualMatchMap value is null, token value is not null. token claim '" + key + "' is not valid.....");
								return false;
							}
						} else {
							if (!value.equals(tokenValue)) {
								logger.error("token claim '" + key + "' is not valid, claimEqualMatchMap value.....");
								return false;
							}else {
								continue;
							}
						}
					}
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return false;
		}
	}
	
	public static String issueTokenDefault (IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap) {

		if (iaaUser == null || privateKey == null) {
			return null;
		}
		try {
			
			Map<String, Object> claims = TokenHandlerI.getDefaultTokenClaims(iaaUser, tokenType, expireMinutes);
			if(customClaimMap != null) {
				customClaimMap.putAll(claims);
			}else {
				customClaimMap = claims;
			}

			// setClaims first
			String token = Jwts.builder().setClaims(customClaimMap)
			.signWith(SignatureAlgorithm.RS256, privateKey).compact();
			return token;
		} catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
			return null;
		}
	}

	public static Map<String, Object> getDefaultTokenClaims(IaaUserI iaaUser, String tokenType, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		if (TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType)) {
			if (expireMinutes <= 0) {
				expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_REFRESH_EXPIRATION_LENGTH);
			}
			return getDefaultRefreshTokenClaims(iaaUser, expireMinutes);
		}else if (TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType)) {
			if (expireMinutes <= 0) {
				expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_IDTOKEN_EXPIRATION_LENGTH);
			}
			return getDefaultIdTokenClaims(iaaUser, expireMinutes);
		}else {
			if (expireMinutes <= 0) {
				expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_ACCESS_EXPIRATION_LENGTH);
			}
			return getDefaultAccessTokenClaims(iaaUser, expireMinutes);
		}
	}
	private static Map<String, Object> getDefaultClaimMap(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		LocalDateTime now = LocalDateTime.now().withNano(0);
		ZonedDateTime nowZonedDateTime = DateUtil.getZonedDateTime(now, null);
		Long iat = DateUtil.LocalDateTimeToEpochSecond(now, null);
		String iatLocal = nowZonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
		
		LocalDateTime expireDateTime = now.plusMinutes(expireMinutes);
		ZonedDateTime expireZonedDateTime = DateUtil.getZonedDateTime(expireDateTime, null);
		Long exp = DateUtil.LocalDateTimeToEpochSecond(expireDateTime, null);
		
		String expLocal = expireZonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);

		String iss = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TOKEN_ISSUE_ISS);
		String hashedUip = iaaUser.getHashedUserIp();
		String hashedNonce = iaaUser.getHashedNonce();
		
		
		//logger.info("hashedUip = " + hashedUip + ", hashednonce = "+ hashedNonce);

		Map<String, Object> claims = new HashMap<String, Object>();
		
		claims.put(JWT_CLAIM_KEY_ISSUER, iss);
		claims.put(JWT_CLAIM_KEY_ISSUE_AT, iat);
		claims.put(JWT_CLAIM_KEY_IAT_LOCAL, iatLocal);
		claims.put(JWT_CLAIM_KEY_NOT_BEFORE, iat);
		claims.put(JWT_CLAIM_KEY_EXPIRE, exp);
		claims.put(JWT_CLAIM_KEY_EXPIRE_LOCAL, expLocal);
		claims.put(JWT_CLAIM_KEY_AUDIENCE, iaaUser.getClientAppId());
		claims.put(JWT_CLAIM_KEY_AUTH_KEY, iaaUser.getClientAuthKey());
		claims.put(JWT_CLAIM_KEY_APP_ID, iaaUser.getApplicationId());
		claims.put(JWT_CLAIM_KEY_SUBJECT, iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_MFA_STATUS, iaaUser.getMfaStatus());
		claims.put(JWT_CLAIM_KEY_UID, iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_LOGINID, iaaUser.getLoginId());
		claims.put(JWT_CLAIM_KEY_EMAIL, iaaUser.getEmail());
		claims.put(JWT_CLAIM_KEY_NAME, iaaUser.getName());
		claims.put(JWT_CLAIM_KEY_PHONE, iaaUser.getPhone());
		claims.put(JWT_CLAIM_KEY_HASHED_USERIP, hashedUip);
		claims.put(JWT_CLAIM_KEY_HASHED_NONCE, hashedNonce);
		
		return claims;
	}

	private static Map<String, Object> getDefaultAccessTokenClaims(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
	
		Map<String, Object> claims = getDefaultClaimMap(iaaUser, expireMinutes);
		
		claims.put(JWT_CLAIM_KEY_TOKEN_TYPE, TYPE_ACCESS_TOKEN);
		claims.put(JWT_CLAIM_KEY_BUS_ROLES, iaaUser.getBusinessRoles());
		claims.put(JWT_CLAIM_KEY_APP_ROLES, iaaUser.getApplicationRoles());
		claims.put(JWT_CLAIM_KEY_AUTH_GROUPS, iaaUser.getAuthGroups());
		
		return claims;
	}
	
	private static Map<String, Object> getDefaultIdTokenClaims(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		Map<String, Object> claims = getDefaultClaimMap(iaaUser, expireMinutes);
		
		claims.put(JWT_CLAIM_KEY_TOKEN_TYPE, TYPE_ID_TOKEN);
		
		return claims;
	}

	private static Map<String, Object> getDefaultRefreshTokenClaims(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		Map<String, Object> claims = getDefaultClaimMap(iaaUser, expireMinutes);
		
		claims.put(JWT_CLAIM_KEY_TOKEN_TYPE, TYPE_REFRESH_TOKEN);
		return claims;
	}
	
	public static IaaUserI getIaaUserFromeTokenDefault(String token) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return null;
			}
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);

			String uid = "" + claims.get(JWT_CLAIM_KEY_UID);
			//Because token is issued locally, try to get IaaUser from repository first
			IaaUserI iaaUser = AppComponents.iaaService.getIaaUserBySystemUid(uid);
			
			if(iaaUser == null){

				String nbf = claims.get(JWT_CLAIM_KEY_NOT_BEFORE)==null?null:"" + claims.get(JWT_CLAIM_KEY_NOT_BEFORE);
				String exp = claims.get(JWT_CLAIM_KEY_EXPIRE)==null?null:"" + claims.get(JWT_CLAIM_KEY_EXPIRE);
				String name = claims.get(JWT_CLAIM_KEY_NAME)==null?null:"" + claims.get(JWT_CLAIM_KEY_NAME);
				String email = claims.get(JWT_CLAIM_KEY_EMAIL)==null?null:"" + claims.get(JWT_CLAIM_KEY_EMAIL);
				String phone = claims.get(JWT_CLAIM_KEY_PHONE)==null?null:"" + claims.get(JWT_CLAIM_KEY_PHONE);
				String loginId = claims.get(JWT_CLAIM_KEY_LOGINID)==null?null:"" + claims.get(JWT_CLAIM_KEY_LOGINID);
				String busRoles = claims.get(JWT_CLAIM_KEY_BUS_ROLES)==null?null:"" + claims.get(JWT_CLAIM_KEY_BUS_ROLES);
				String appRoles = claims.get(JWT_CLAIM_KEY_APP_ROLES)==null?null:"" + claims.get(JWT_CLAIM_KEY_APP_ROLES);
				String authGroups = claims.get(JWT_CLAIM_KEY_AUTH_GROUPS)==null?null:"" + claims.get(JWT_CLAIM_KEY_AUTH_GROUPS);
	
				iaaUser = new DefaultIaaUser();
				iaaUser.setEmail(email);
				iaaUser.setName(name);
				iaaUser.setBusinessRoles(CommonUtil.getSetFromString(busRoles));
				iaaUser.setApplicationRoles(CommonUtil.getSetFromString(appRoles));
				iaaUser.setAuthGroups(CommonUtil.getSetFromString(authGroups));
				iaaUser.setPhone(phone);
				iaaUser.setLoginId(loginId);
				iaaUser.setSystemUid(uid);
				iaaUser.setAuthGroups(CommonUtil.getSetFromString(authGroups));
			}
			
			String iss = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TOKEN_ISSUE_ISS);
			String aud = claims.get(JWT_CLAIM_KEY_AUDIENCE)==null?null:"" + claims.get(JWT_CLAIM_KEY_AUDIENCE);
			String authKey = claims.get(JWT_CLAIM_KEY_AUTH_KEY)==null?null:"" + claims.get(JWT_CLAIM_KEY_AUTH_KEY);
			String mfaStatus = claims.get(JWT_CLAIM_KEY_MFA_STATUS)==null?null:"" + claims.get(JWT_CLAIM_KEY_MFA_STATUS);
			String hashedUip = claims.get(JWT_CLAIM_KEY_HASHED_USERIP)==null?null:"" + claims.get(JWT_CLAIM_KEY_HASHED_USERIP);
			String hashedNonce = claims.get(JWT_CLAIM_KEY_HASHED_NONCE)==null?null:"" + claims.get(JWT_CLAIM_KEY_HASHED_NONCE);
			
			iaaUser.setApplicationId(iss);
			iaaUser.setClientAppId(aud);
			iaaUser.setClientAuthKey(authKey);
			iaaUser.setMfaStatus(mfaStatus);
			iaaUser.setHashedUserIp(hashedUip);
			iaaUser.setHashedNonce(hashedNonce);
			
			return iaaUser;

		} catch (SignatureException e) {
			logger.error(e);
			return null;
		}
	}


}
