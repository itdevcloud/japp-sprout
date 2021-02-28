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
package com.itdevcloud.japp.core.iaa.token;

import java.security.Key;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaUser;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class AppLocalTokenHandler implements TokenHandlerI {
	private static final Logger logger = LogManager.getLogger(AppLocalTokenHandler.class);

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean isValidToken(String token, Map<String, String> claimMatchMap, String... args ) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return false;
			}
			PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
			boolean isValid = AppComponents.jwtService.isValidTokenByPublicKey(token, publicKey);

			if (!isValid) {
				logger.error("Japp Api token is not Valid..........");
				return false;
			}
			
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);
			
			long nbf = (claims.get(JWT_CLAIM_KEY_NOT_BEFORE) == null? -1: (Long) claims.get(JWT_CLAIM_KEY_NOT_BEFORE));
			long exp = (claims.get(JWT_CLAIM_KEY_EXPIRE) == null? -1: (Long) claims.get(JWT_CLAIM_KEY_EXPIRE));
			
			LocalDateTime now = LocalDateTime.now().withNano(0);
			LocalDateTime expDateTime = DateUtil.epochSecondToLocalDateTime(exp);
			LocalDateTime nbfDateTime = DateUtil.epochSecondToLocalDateTime(nbf);
			if (exp < 0 || now.isAfter(expDateTime)) {
				logger.error("token exp claim is not valid......");
				return false;
  		    }
			if (nbf < 0 || now.isBefore(nbfDateTime)) {
				logger.error("token nbf claim is not valid.....");
				return false;
		    }
			if(claimMatchMap == null || claimMatchMap.isEmpty()) {
				//no claims need to be verified
				return true;
			}else {
				Set<String> keySet = claimMatchMap.keySet();
				for(String key: keySet) {
					String value = claimMatchMap.get(key);
					String tokenValue = ""+claims.get(key);
					if (value == null) {
						continue;
					}else {
						if (!value.equalsIgnoreCase(tokenValue)) {
							logger.error("idToken aud claim is not valid.....");
							return false;
						}
						continue;
					}
				}
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

	}

	@Override
	public IaaUserI getIaaUser(String token) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return null;
			}
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);

			String uid = "" + claims.get(JWT_CLAIM_KEY_UID);
			//Because token is issued locally, try to get IaaUser from repository first
			IaaUserI iaaUser = AppComponents.iaaService.getIaaUserBySystemUid(uid);
			
			if(iaaUser == null){

				String aud = "" + claims.get(JWT_CLAIM_KEY_AUDIENCE);
				String nbf = "" + claims.get(JWT_CLAIM_KEY_NOT_BEFORE);
				String exp = "" + claims.get(JWT_CLAIM_KEY_EXPIRE);
				String name = "" + claims.get(JWT_CLAIM_KEY_NAME);
				String email = "" + claims.get(JWT_CLAIM_KEY_EMAIL);
				String phone = "" + claims.get(JWT_CLAIM_KEY_PHONE);
				String loginId = "" + claims.get(JWT_CLAIM_KEY_LOGINID);
				String iss = "" + claims.get(JWT_CLAIM_KEY_ISSUER);
				String busRoles = "" + claims.get(JWT_CLAIM_KEY_BUS_ROLES);
				String appRoles = "" + claims.get(JWT_CLAIM_KEY_APP_ROLES);
				String authGroups = "" + claims.get(JWT_CLAIM_KEY_AUTH_GROUPS);
				String mfaStatus = "" + claims.get(JWT_CLAIM_KEY_MFA_STATUS);
				String idp = "" + claims.get(JWT_CLAIM_KEY_IDENTITY_PROVIDER);
	
				iaaUser = new DefaultIaaUser();
				iaaUser.setEmail(email);
				iaaUser.setName(name);
				iaaUser.setBusinessRoles(CommonUtil.getSetFromString(busRoles));
				iaaUser.setApplicationRoles(CommonUtil.getSetFromString(appRoles));
				iaaUser.setAuthGroups(CommonUtil.getSetFromString(authGroups));
				iaaUser.setPhone(phone);
				iaaUser.setApplicationId(aud);
				iaaUser.setIdentityProvider(idp);
				iaaUser.setLoginId(loginId);
				iaaUser.setSystemUid(uid);
				iaaUser.setUserType(authGroups);
				iaaUser.setMfaStatus(mfaStatus);
			}

			return iaaUser;

		} catch (SignatureException e) {
			logger.error(e);
			return null;
		}
	}
	
	@Override
	public String issueAccessToken(IaaUserI iaaUser, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap) {

		if (iaaUser == null || privateKey == null) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
		}
//		SecondFactorInfo secondFactorInfo = ((obj == null || !(obj instanceof SecondFactorInfo))?null: (SecondFactorInfo)obj);
		try {
			
			String iss = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TOKEN_ISSUE_ISS);

			Map<String, Object> claims = TokenHandlerI.getDefaultAccessTokenClaims(iaaUser, expireMinutes);
			if(customClaimMap != null) {
				customClaimMap.putAll(claims);
			}else {
				customClaimMap = claims;
			}

			// setClaims first
//			String token = Jwts.builder().setClaims(claims).setIssuer(iss)
//					.setSubject(iaaUser.getSystemUid()).setIssuedAt(new Date()).setExpiration(expiryDate)
//					.signWith(SignatureAlgorithm.RS256, privateKey).compact();
			String token = Jwts.builder().setClaims(customClaimMap)
			.signWith(SignatureAlgorithm.RS256, privateKey).compact();
			return token;
		} catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
			return null;
		}
	}



	@Override
	public String getAccessToken(String refreshToken) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
