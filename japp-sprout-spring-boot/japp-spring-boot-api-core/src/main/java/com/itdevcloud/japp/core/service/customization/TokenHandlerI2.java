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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.common.JJwtTokenUtil;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaUser;
import com.itdevcloud.japp.core.iaa.token.AppLocalTokenHandler;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface TokenHandlerI2 extends AppFactoryComponentI {

	public static final String TOKEN_PROVIDER_JJWT = "JJWT";
	public static final String TOKEN_PROVIDER_AUTH0 = "AUTH0";
	
	public static final String TYPE_ACCESS_TOKEN = "Access_Token";
	public static final String TYPE_ID_TOKEN = "Id_Token";
	public static final String TYPE_REFRESH_TOKEN = "Refresh_Token";

	public static final String JWT_CLAIM_KEY_TOEKN_ID = "jti";
	public static final String JWT_CLAIM_KEY_ISSUER = "iss";
	public static final String JWT_CLAIM_KEY_TOKEN_TYPE = "type";
//	public static final String JWT_CLAIM_KEY_IDENTITY_PROVIDER = "idp";
//	public static final String JWT_CLAIM_KEY_APP_ID = "appid";
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
	
	public Map<String, Object> parseTokenHeaders(String token) ;
	public Map<String, Object> parseTokenClaims(String token) ;
	public Map<String, Object> isValidTokenByPublicKey(String token, PublicKey publicKey);
	public Map<String, Object> isValidToken(String token, Map<String, Object> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) ;
	public IaaUserI getIaaUserBasedOnToken(String token);
	public String issueToken(IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap);

	
	public static Map<String, Object> parseTokenHeadersDefault(String token) {
		Map<String, Object> claims = JJwtTokenUtil.parseJwtHeaders(token);
		return claims;
	}
	
	public static Map<String, Object> parseTokenClaimsDefault(String token) {
		Map<String, Object> claims = JJwtTokenUtil.parseJwtClaims(token);
		return claims;
	}
	
	public static Map<String, Object> isValidTokenByPublicKeyDefault(String token, PublicKey publicKey){
		if (token == null || publicKey == null) {
			String errMsg = "isValidTokenByPublicKeyDefault() - token and/or publicKey is null";
			logger.error(errMsg);
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		Map<String, Object> claims = JJwtTokenUtil.isValidTokenByPublicKey(token, publicKey);
		return claims;
	}

	public static Map<String, Object> isValidTokenDefault(String token, Map<String, Object> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) {
		try {
			if (token == null || token.trim().isEmpty()) {
				String errMsg = "isValidTokenDefault() - token is null";
				logger.error(errMsg);
				throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
			}
			PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
			Map<String, Object> claims = isValidTokenByPublicKeyDefault(token, publicKey);
			if (claims == null) {
				String errMsg = "isValidTokenDefault() ......token can not be validated.";
				logger.error(errMsg);
				throw new AppException(Status.ERROR_VALIDATION, errMsg);
			}
			
			String tokenType = (claims.get(JWT_CLAIM_KEY_TOKEN_TYPE)==null?null:"" + claims.get(JWT_CLAIM_KEY_TOKEN_TYPE));
			if (!TokenHandlerI2.TYPE_ACCESS_TOKEN.equalsIgnoreCase(tokenType)
					&& !TokenHandlerI2.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType)
					&& !TokenHandlerI2.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType)) {
				String errMsg = "isValidTokenDefault() - token is not Valid, missing token type";
				logger.error(errMsg);
				throw new AppException(Status.ERROR_VALIDATION, errMsg);
			}
			Date nbfDate = claims.get(JWT_CLAIM_KEY_NOT_BEFORE)==null?null:(Date)claims.get(JWT_CLAIM_KEY_NOT_BEFORE);
			Date expDate = claims.get(JWT_CLAIM_KEY_EXPIRE)==null?null:(Date)claims.get(JWT_CLAIM_KEY_EXPIRE);
			
			long expEpochSecond = expDate==null?-1:expDate.toInstant().getEpochSecond();
			long nbfEpochSecond = nbfDate==null?-1:nbfDate.toInstant().getEpochSecond();;
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
			LocalDateTime expDateTime = DateUtil.epochSecondToLocalDateTime(expEpochSecond, null);
			LocalDateTime nbfDateTime = DateUtil.epochSecondToLocalDateTime(nbfEpochSecond, null);
			if (expEpochSecond < 0 || now.isAfter(expDateTime)) {
				String errMsg = "isValidTokenDefault()......token is not valid";
				logger.error(errMsg + "(exp is not valid)!");
				throw new AppException(Status.ERROR_VALIDATION, errMsg);
  		    }
			if (nbfEpochSecond < 0 || now.isBefore(nbfDateTime)) {
				String errMsg = "isValidTokenDefault()......token is not valid";
				logger.error(errMsg + "(nbf is not valid)!");
				throw new AppException(Status.ERROR_VALIDATION, errMsg);
		    }
			//handle tokenIp and nonce
			String mapHashedTokenIp = (claimEqualMatchMap == null || claimEqualMatchMap.isEmpty())?null:""+claimEqualMatchMap.get(JWT_CLAIM_KEY_HASHED_USERIP);
			String mapHashedTokenNonce = (claimEqualMatchMap == null || claimEqualMatchMap.isEmpty())?null:""+claimEqualMatchMap.get(JWT_CLAIM_KEY_HASHED_NONCE);
			AppUtil.checkTokenIpAndNonceRequirement(mapHashedTokenIp,  mapHashedTokenNonce);
			
			String tokenHashedTokenIp = ""+claims.get(JWT_CLAIM_KEY_HASHED_USERIP);
			String tokenHashedTokenNonce = ""+claims.get(JWT_CLAIM_KEY_HASHED_NONCE);
			AppUtil.checkTokenIpAndNonceRequirement(tokenHashedTokenIp,  tokenHashedTokenNonce);

			if(claimEqualMatchMap == null || claimEqualMatchMap.isEmpty()) {
				return claims;
			}else {
				//this could include nonce and UIP
				Set<String> keySet = claimEqualMatchMap.keySet();
				for(String key: keySet) {
					String value = (claimEqualMatchMap.get(key)==null?null:""+claimEqualMatchMap.get(key));
					String tokenValue = (claims.get(key)==null?null:""+claims.get(key));
					if(ingoreNullInToken) {
						if (StringUtil.isEmptyOrNull(tokenValue)) {
							continue;
						}
						if (tokenValue.equals(value)) {
							continue;
						}else {
							logger.error("isValidTokenDefault()......token claim '" + key + "'is not valid, value - '" + tokenValue+"' doesn't match claimEqualMatchMap value - '"+ value + "'.....");
							String errMsg = "isValidTokenDefault()......token is not valid.";
							throw new AppException(Status.ERROR_VALIDATION, errMsg);
						}
					}else {
						if (StringUtil.isEmptyOrNull(value)) {
							if(StringUtil.isEmptyOrNull(tokenValue)) {
								continue;
							}else {
								//matchMap value is null, token value is not null
								logger.error("isValidTokenDefault()......claimEqualMatchMap value is null, token value is not null. token claim '" + key + "' is not valid.....");
								String errMsg = "isValidTokenDefault()......token is not valid.";
								throw new AppException(Status.ERROR_VALIDATION, errMsg);
							}
						} else {
							if (!value.equals(tokenValue)) {
								logger.error("isValidTokenDefault()......token claim '" + key + "' is not valid, claimEqualMatchMap value.....");
								String errMsg = "isValidTokenDefault()......token is not valid.";
								throw new AppException(Status.ERROR_VALIDATION, errMsg);
							}else {
								continue;
							}
						}
					}
				}
				return claims;
			}
		} catch (AppException ae) {
			throw ae;
		} catch (Throwable t) {
			t.printStackTrace();
			String errMsg = "isValidTokenDefault() - error occurs when validating the token: " + t.getMessage();
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
		}
	}
	
	public static String issueTokenDefault (IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap) {

		if (iaaUser == null || privateKey == null) {
			logger.error("issueTokenDefault() - iaaUser and/or privateKey is null ..........");
			return null;
		}
		try {
			Map<String, Object> claims = TokenHandlerI2.getDefaultTokenClaims(iaaUser, tokenType, expireMinutes);
			if(claims == null) {
				String errMsg = "issueTokenDefault()......can not get default claims!";
				throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
			}
			if(customClaimMap != null) {
				claims.putAll(customClaimMap);
			}
			return JJwtTokenUtil.issueToken(claims, tokenType, privateKey, expireMinutes);
		}catch (AppException ae) {
			throw ae;
		}catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
			throw new AppException(Status.ERROR_SYSTEM_ERROR, t.getMessage());
		}
	}

	public static Map<String, Object> getDefaultTokenClaims(IaaUserI iaaUser, String tokenType, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
		if (TokenHandlerI2.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType)) {
			if (expireMinutes <= 0) {
				expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_REFRESH_EXPIRATION_LENGTH);
			}
			return getDefaultRefreshTokenClaims(iaaUser, expireMinutes);
		}else if (TokenHandlerI2.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType)) {
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
		
		AppUtil.checkTokenIpAndNonceRequirement(hashedUip, hashedNonce);

		Map<String, Object> claims = new HashMap<String, Object>();
		
		//some JWT library remove claim name if the value is null (e.g. jjwt)
		claims.put(JWT_CLAIM_KEY_ISSUER, iss==null?"":iss);
		claims.put(JWT_CLAIM_KEY_ISSUE_AT, iat);
		claims.put(JWT_CLAIM_KEY_IAT_LOCAL, iatLocal==null?"":iatLocal);
		claims.put(JWT_CLAIM_KEY_NOT_BEFORE, iat);
		claims.put(JWT_CLAIM_KEY_EXPIRE, exp);
		claims.put(JWT_CLAIM_KEY_EXPIRE_LOCAL, expLocal==null?"":expLocal);
		claims.put(JWT_CLAIM_KEY_AUDIENCE, iaaUser.getClientAppId()==null?"":iaaUser.getClientAppId());
		claims.put(JWT_CLAIM_KEY_AUTH_KEY, iaaUser.getClientAuthKey()==null?"":iaaUser.getClientAuthKey());
//		claims.put(JWT_CLAIM_KEY_APP_ID, iaaUser.getApplicationId()==null?"":iaaUser.getApplicationId());
		claims.put(JWT_CLAIM_KEY_SUBJECT, iaaUser.getSystemUid()==null?"":iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_MFA_STATUS, iaaUser.getMfaStatus()==null?"":iaaUser.getMfaStatus());
		claims.put(JWT_CLAIM_KEY_UID, iaaUser.getSystemUid()==null?"":iaaUser.getSystemUid());
		claims.put(JWT_CLAIM_KEY_LOGINID, iaaUser.getLoginId()==null?"":iaaUser.getLoginId());
		claims.put(JWT_CLAIM_KEY_EMAIL, iaaUser.getEmail()==null?"":iss);
		claims.put(JWT_CLAIM_KEY_NAME, iaaUser.getName()==null?"":iaaUser.getName());
		claims.put(JWT_CLAIM_KEY_PHONE, iaaUser.getPhone()==null?"":iaaUser.getPhone());
		claims.put(JWT_CLAIM_KEY_HASHED_USERIP, hashedUip==null?"":hashedUip);
		claims.put(JWT_CLAIM_KEY_HASHED_NONCE, hashedNonce==null?"":hashedNonce);
		
		return claims;
	}

	private static Map<String, Object> getDefaultAccessTokenClaims(IaaUserI iaaUser, int expireMinutes) {
		if (iaaUser == null) {
			return null;
		}
	
		Map<String, Object> claims = getDefaultClaimMap(iaaUser, expireMinutes);
		
		claims.put(JWT_CLAIM_KEY_TOKEN_TYPE, TYPE_ACCESS_TOKEN);
		claims.put(JWT_CLAIM_KEY_BUS_ROLES, iaaUser.getBusinessRoles()==null?"":iaaUser.getBusinessRoles());
		claims.put(JWT_CLAIM_KEY_APP_ROLES, iaaUser.getApplicationRoles()==null?"":iaaUser.getApplicationRoles());
		claims.put(JWT_CLAIM_KEY_AUTH_GROUPS, iaaUser.getAuthGroups()==null?"":iaaUser.getAuthGroups());
		
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
	
	public static IaaUserI getIaaUserBaseOnTokenDefault(String token) {
		if (token == null || token.trim().isEmpty()) {
			return null;
		}
		
		Map<String, Object> claims = AppComponents.jwtService.parseTokenClaims(token);
		if (claims == null) {
			logger.error("token can not be validated by publickey..........");
			return null;
		}
		return getIaaUserFromTokenClaimDefault(claims);
	}
	private static IaaUserI getIaaUserFromTokenClaimDefault(Map<String, Object> claims) {
		if (claims == null ) {
			return null;
		}
		String uid = "" + claims.get(JWT_CLAIM_KEY_UID);
		//Because token is issued locally, try to get IaaUser from repository first
		IaaUserI iaaUser = AppComponents.iaaService.getIaaUserBySystemUid(uid);
		
		if(iaaUser == null){

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

	}


}
