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
package com.itdevcloud.japp.core.common;

import java.security.Key;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class JJwtTokenUtil {

	private static final Logger logger = LogManager.getLogger(JJwtTokenUtil.class);

	public static Header parseJJwtHeaders(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				logger.error("parseJwtHeaders()......token is null!");
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parserBuilder().build().parseClaimsJwt(tokenWithoutSignature);
			Header header = jwtWithoutSignature.getHeader();

			return header;

		} catch (JwtException e) {
			logger.error("parseJJwtHeaders()..........token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	public static Map<String, Object> parseJwtHeaders(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				logger.error("parseJwtHeaders()......token is null!");
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parserBuilder().build().parseClaimsJwt(tokenWithoutSignature);
			Header header = jwtWithoutSignature.getHeader();

			Set<String> keySet = header.keySet();
			if (keySet == null || keySet.isEmpty()) {
				logger.error("parseJwtClaims()......no claim found in the token!");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keySet) {
				map.put(key, header.get(key));
			}

			return map;

		} catch (JwtException e) {
			logger.error("parseJwtHeaders()..........token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	public static Claims parseJJwtClaims(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				logger.error("parseJJwtClaims()......token is null!");
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parserBuilder().build().parseClaimsJwt(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getBody();
			return claims;

		} catch (JwtException e) {
			logger.error("parseJJwtClaims()..........token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	public static Map<String, Object> parseJwtClaims(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				logger.error("parseJwtClaims()......token is null!");
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parserBuilder().build().parseClaimsJwt(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getBody();

			Set<String> keySet = claims.keySet();
			if (keySet == null || keySet.isEmpty()) {
				logger.error("parseJwtClaims()......no claim found in the token!");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keySet) {
				map.put(key, claims.get(key));
			}
			
			map.put(TokenHandlerI.JWT_CLAIM_KEY_EXPIRE, claims.getExpiration());
			map.put(TokenHandlerI.JWT_CLAIM_KEY_NOT_BEFORE, claims.getNotBefore());
			map.put(TokenHandlerI.JWT_CLAIM_KEY_ISSUE_AT, claims.getIssuedAt());
			
			return map;

		} catch (JwtException e) {
			logger.error("parseJwtClaims()..........token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}
	public static Claims isValidJJwtTokenByPublicKey(String token, PublicKey publicKey) {
		if (token == null || publicKey == null) {
			logger.error("isValidJJwtTokenByPublicKey()......toekn or publickey is null!");
			return null;
		}
		try {
			Jws<Claims> jwts = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
			Claims claims = jwts.getBody();
			
			return claims;
		} catch (JwtException  e) {
			logger.error("isValidJJwtTokenByPublicKey().............token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	public static Map<String, Object> isValidTokenByPublicKey(String token, PublicKey publicKey) {
		if (token == null || publicKey == null) {
			logger.error("isValidTokenByPublicKey()......toekn or publickey is null!");
			return null;
		}
		try {
			Jws<Claims> jwts = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
			Claims claims = jwts.getBody();
			
			Set<String> keySet = claims.keySet();

			if (keySet == null || keySet.isEmpty()) {
				logger.error("isValidTokenByPublicKey()......no claim found in the token!");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keySet) {
				map.put(key, claims.get(key));
			}
			map.put(TokenHandlerI.JWT_CLAIM_KEY_EXPIRE, claims.getExpiration());
			map.put(TokenHandlerI.JWT_CLAIM_KEY_NOT_BEFORE, claims.getNotBefore());
			map.put(TokenHandlerI.JWT_CLAIM_KEY_ISSUE_AT, claims.getIssuedAt());
		
			return map;
		} catch (JwtException  e) {
			logger.error("isValidTokenByPublicKey().............token is not valid!");
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	
	public static String issueToken (Map<String, Object> claims, String tokenType, Key privateKey, int expireMinutes) {

		if (claims == null || claims.isEmpty() || privateKey == null) {
			logger.error("issueToken()......claims and/or private key is null!");
			return null;
		}
		try {
			// setClaims first
			String token = Jwts.builder().setClaims(claims)
			.signWith(privateKey, SignatureAlgorithm.RS256).compact();
			return token;
		} catch (Throwable t) {
			logger.error("issueToken() ... encounter error: " + CommonUtil.getStackTrace(t));
			return null;
		}
	}

	


}