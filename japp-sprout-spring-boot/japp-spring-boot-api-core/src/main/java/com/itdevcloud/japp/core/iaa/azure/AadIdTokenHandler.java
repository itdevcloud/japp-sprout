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
package com.itdevcloud.japp.core.iaa.azure;

import java.security.Key;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class AadIdTokenHandler implements TokenHandlerI {
	private static final Logger logger = LogManager.getLogger(AadIdTokenHandler.class);

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean isValidToken(String token, Map<String, String> claimMatchMap, String... args) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return false;
			}
			int idx = token.lastIndexOf('.');
			String tokenWithoutSignature = token.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
			Header header = jwtWithoutSignature.getHeader();
			Claims claims = jwtWithoutSignature.getBody();

			String kid = (header.containsKey("kid") ? (String) header.get("kid") : null);
			String x5t = (header.containsKey("x5t") ? (String) header.get("xt5") : null);
			PublicKey publicKey = AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
			boolean isValid = AppComponents.jwtService.isValidTokenByPublicKey(token, publicKey);

			if (!isValid) {
				logger.error("idToken is not Valid..........");
				return false;
			}
			String aud = claims.getAudience();
			Date nbf = claims.getNotBefore();
			Date exp = claims.getExpiration();
			String clientId = AppComponents.aadJwksCache.getAadClientId();
			Date now = new Date();

			if (clientId == null || !clientId.equals(aud)) {
				logger.error("idToken aud claim is not valid.....");
				return false;
			}
			if (exp == null || now.after(exp)) {
				logger.error("idToken exp claim is not valid......");
				return false;
			}
			if (nbf == null || now.before(nbf)) {
				logger.error("idToken nbf claim is not valid.....");
				return false;
			}
			return true;

		} catch (SignatureException e) {
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
//			IaaUserI iaaUser = new DefaultIaaUser();
//
//			String aud = "" + claims.get(JWT_CLAIM_KEY_AUDIENCE);
//			String nbf = "" + claims.get(JWT_CLAIM_KEY_NOT_BEFORE);
//			String exp = "" + claims.get(JWT_CLAIM_KEY_EXPIRE);
//			String name = "" + claims.get(JWT_CLAIM_KEY_NAME);
//			String email = "" + claims.get(JWT_CLAIM_KEY_EMAIL);
//			String phone = "" + claims.get(JWT_CLAIM_KEY_PHONE);
//			String iss = "" + claims.get(JWT_CLAIM_KEY_ISSUER);
//			String busRoles = "" + claims.get(JWT_CLAIM_KEY_BUS_ROLES);
//			String appRoles = "" + claims.get(JWT_CLAIM_KEY_APP_ROLES);
//			String authGroups = "" + claims.get(JWT_CLAIM_KEY_AUTH_GROUPS);
//			String mfaStatus = "" + claims.get(JWT_CLAIM_KEY_MFA_STATUS);
//			String idp = "" + claims.get(JWT_CLAIM_KEY_IDENTITY_PROVIDER);
			String sub = "" + claims.get(JWT_CLAIM_KEY_SUBJECT);
			String upn = "" + claims.get(JWT_CLAIM_KEY_AAD_USERNAME);
//			String uid = "" + claims.get(JWT_CLAIM_KEY_UID);

//			iaaUser.setEmail(email);
//			iaaUser.setName(name);
//			iaaUser.setBusinessRoles(null);
//			iaaUser.setApplicationRoles(null);
//			iaaUser.setAuthGroups(null);
//			iaaUser.setPhone(phone);
//			iaaUser.setApplicationId(aud);
//			iaaUser.setIdentityProvider(idp);
//			iaaUser.setSystemUid(uid);
//			iaaUser.setUserType(authGroups);
//			iaaUser.setMfaStatus(mfaStatus);
			
			String loginId = null;
			logger.info("subject: " + sub + ", upn=" + upn + "......");
			if (!StringUtil.isEmptyOrNull(upn)) {
				loginId = sub;
			}else {
				loginId = upn;
			}
			logger.error("retrieve user by loginid = " + loginId + ", Auth provider =  " + AppConstant.IDENTITY_PROVIDER_AAD_OIDC);
			IaaUserI iaaUser = AppComponents.iaaService.getIaaUserFromRepositoryByLoginId(loginId,
					AppConstant.IDENTITY_PROVIDER_AAD_OIDC);

			return iaaUser;

		} catch (SignatureException e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	public String issueAccessToken(IaaUserI iaaUser, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap) {
		return null;
	}

	@Override
	public String getAccessToken(String refreshToken) {
		// TODO Auto-generated method stub
		return null;
	}

}
