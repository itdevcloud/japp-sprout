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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.JJwtTokenUtil;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;

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
	public Map<String, Object> isValidToken(String token, Map<String, Object> claimEqualMatchMap, boolean ingoreNullInToken, String... args) {
		try {
			Header header = JJwtTokenUtil.parseJJwtHeaders(token);
			if (header == null) {
				return null;
			}
			String kid = (header.containsKey("kid") ? (String) header.get("kid") : null);
			String x5t = (header.containsKey("x5t") ? (String) header.get("xt5") : null);
			PublicKey publicKey = AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
			
			Claims claims = JJwtTokenUtil.isValidJJwtTokenByPublicKey(token, publicKey);

			if (claims == null) {
				logger.error("token is not Valid..........");
				return null;
			}
			String aud = claims.getAudience();
			Date nbf = claims.getNotBefore();
			Date exp = claims.getExpiration();
			String clientId = AppComponents.aadJwksCache.getAadClientId();
			Date now = new Date();

			if (clientId == null || !clientId.equals(aud)) {
				logger.error("idToken aud claim is not valid.....");
				return null;
			}
			if (exp == null || now.after(exp)) {
				logger.error("idToken exp claim is not valid......");
				return null;
			}
			if (nbf == null || now.before(nbf)) {
				logger.error("idToken nbf claim is not valid.....");
				return null;
			}
			Set<String> keySet = claims.keySet();
			if (keySet == null || keySet.isEmpty()) {
				logger.error("isValidTokenByPublicKey()......no claim found in the token!");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keySet) {
				map.put(key, claims.get(key));
			}
			return map;

		} catch (Exception e) {
			logger.error("token is not Valid......Error: " + e);
			logger.error(e);
			return null;
		}

	}

	@Override
	public IaaUserI getIaaUserBasedOnToken(String token) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return null;
			}
			Map<String, Object> claims = JJwtTokenUtil.parseJwtClaims(token);
			String email = "" + claims.get(JWT_CLAIM_KEY_EMAIL);
			String sub = "" + claims.get(JWT_CLAIM_KEY_SUBJECT);
			String upn = "" + claims.get(JWT_CLAIM_KEY_AAD_USERNAME);
			String nonce = "" + claims.get(JWT_CLAIM_KEY_AAD_NONCE);

			String loginId = null;
			logger.info("subject: " + sub + ", upn=" + upn + "......");
			if (!StringUtil.isEmptyOrNull(upn) && (upn.indexOf("@") != -1)) {
				loginId = upn;
			}else if (!StringUtil.isEmptyOrNull(sub) && (sub.indexOf("@") != -1)){
				loginId = sub;
			}else {
				loginId = email;
			}
			logger.info("retrieving user by loginid = " + loginId );
			IaaUserI iaaUser = AppComponents.iaaService.getIaaUserFromRepositoryByLoginId(loginId);
			
			if(!StringUtil.isEmptyOrNull(nonce)) {
				iaaUser.setHashedNonce(Hasher.hashPassword(nonce));
			}
			return iaaUser;

		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


	@Override
	public String getAccessToken(String token) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String issueToken(IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes,
			Map<String, Object> customClaimMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> parseTokenHeaders(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> parseTokenClaims(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> isValidTokenByPublicKey(String token, PublicKey publicKey) {
		// TODO Auto-generated method stub
		return null;
	}


}
