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

import java.io.InputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.MfaVO;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.multiInstance.repo.DataInfo;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.multiInstance.repo.azureblob.AzureBlobEventManager;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.PkiUtil;
import com.itdevcloud.japp.se.common.util.RandomUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class JwtService implements AppFactoryComponentI {
	private static final Logger logger = LogManager.getLogger(JwtService.class);

	@PostConstruct
	public void init() {
		// try to avoid using AppConfig Service, AppComponents.appConfigCache may be not
		// fully initiated yet
	}

	public Claims getClaimsWithoutVerification(String token) {
		try {
			logger.debug("getClaimsWithoutVerification.............begin....");
			if (token == null || token.trim().isEmpty()) {
				return null;
			}
			//logger.debug("........0.......token=" + token);

			int idx = token.lastIndexOf('.');
			String tokenWithoutSignature = token.substring(0, idx + 1);
			//logger.debug("........1...idx=" + idx + "....tokenWithoutSignature=" + tokenWithoutSignature);

			idx = tokenWithoutSignature.indexOf('.');
			String tokenWithoutHeader = tokenWithoutSignature.substring(idx);
			//logger.debug("........2....idx=" + idx + "...tokenWithoutHeader=" + tokenWithoutHeader);
			String tokenWithNoAlgHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0" + tokenWithoutHeader;

			//logger.debug("......3......token without alg header =" + tokenWithNoAlgHeader);

			Jwt<Header, Claims> jwtWithoutVerification = Jwts.parser().unsecured().build().parseUnsecuredClaims(tokenWithNoAlgHeader);

			Claims claims = jwtWithoutVerification==null?null:jwtWithoutVerification.getPayload();
			
			logger.debug("getClaimsWithoutVerification.............end....");
			return claims;

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			return null;
		}

	}

//	public boolean isValidEntraIdToken(String idToken) {
//		try {
//			logger.debug("isValidAadIdToken.............begin....");
//			if (idToken == null || idToken.trim().isEmpty()) {
//				return false;
//			}
//			logger.debug("id_token=" + idToken);
//			
//			int idx = idToken.lastIndexOf('.');
//			String tokenWithoutSignature = idToken.substring(0, idx + 1);
//
//			logger.debug(".......1.....");
//			
//			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().build().parseUnsecuredClaims(tokenWithoutSignature);
//			
//			logger.debug(".......2.....");
//			jwtWithoutSignature = Jwts.parser().build().parseUnsecuredClaims(idToken);
//			logger.debug(".......3.....");
//			
//			Header header = jwtWithoutSignature.getHeader();
//			Claims claims = jwtWithoutSignature.getPayload();
//
//			String kid = (header.containsKey("kid") ? (String) header.get("kid") : null);
//			String x5t = (header.containsKey("x5t") ? (String) header.get("xt5") : null);
//			PublicKey publicKey = AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
//			
//			logger.debug("publicKey=" + publicKey);
//			
//			boolean isValid = isValidTokenByPublicKey(idToken, publicKey);
//
//			if (!isValid) {
//				logger.error("Entra idToken is not Valid..........");
//				return false;
//			}
//			Set<String> audSet = claims.getAudience();
//			Date nbf = claims.getNotBefore();
//			Date exp = claims.getExpiration();
//			String clientId = AppComponents.aadJwksCache.getAadClientId();
//			Date now = new Date();
//
//			if (clientId == null || (audSet != null && !audSet.isEmpty() && !audSet.contains(clientId))) {
//				logger.error("idToken aud claim is not valid.....");
//				return false;
//			}
//			if (exp == null || now.after(exp)) {
//				logger.error("idToken exp claim is not valid......");
//				return false;
//			}
//			if (nbf == null || now.before(nbf)) {
//				logger.error("idToken nbf claim is not valid.....");
//				return false;
//			}
//			return true;
//
//		} catch (Throwable t) {
//			logger.error(t);
//			return false;
//		}
//
//	}

//	public boolean isValidEntraIdToken(String idToken) {
//		logger.debug("isValidAadIdToken.............begin....");
//		return isValidToken(idToken, "ENTRA_ID");
//
//	}

	public boolean isValidToken(String token, String authnProvider) {
		try {
			logger.debug("isValidToken.............begin....authnProvider = " + authnProvider);
			if (StringUtil.isEmptyOrNull(token)) {
				return false;
			}

			logger.debug("id token = " + token);

			JwtKeyLocator keyLocator = new JwtKeyLocator(authnProvider);
			Jws<Claims> jws = Jwts.parser().keyLocator(keyLocator).build().parseSignedClaims(token);
			boolean result = validateTokenClaims(jws);
			
			
			logger.debug("isValidToken.............end....authnProvider = " + authnProvider);
			return result;

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			return false;
		}

	}

	private boolean validateTokenClaims(Jws<Claims> jws) {
		if (jws == null) {
			return false;
		}
		try {
			Claims claims = jws.getPayload();
			String subject = claims.getSubject();
			logger.info("subject ====== " + subject);
			if (claims.containsKey("upn")) {
				String upn = (String) claims.get("upn");
				logger.info("convert subject to upn ====== " + upn);
				subject = (String) claims.get("upn");
			}
			Set<String> audSet = claims.getAudience();
			Date nbf = claims.getNotBefore();
			Date exp = claims.getExpiration();
			Date now = new Date();

			if (exp == null || now.after(exp)) {
				logger.error("idToken exp claim is not valid......");
				return false;
			}
			if (nbf == null || now.before(nbf)) {
				logger.error("idToken nbf claim is not valid.....");
				return false;
			}

			AppThreadContext.setTokenSubject(subject);
			return true;
		} catch (Throwable t) {
			logger.error(t);
			return false;
		}

	}

	public boolean isValidTokenByPublicKey(String token, PublicKey publicKey) {
		logger.debug("isValidTokenByPublicKey.............begin....");
		if (token == null || publicKey == null) {
			return false;
		}
		try {
			Jws<Claims> jws = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
			Claims claims = jws.getPayload();
			String subject = claims.getSubject();
			logger.info("subject ====== " + subject);
			if (claims.containsKey("upn")) {
				String upn = (String) claims.get("upn");
				logger.info("convert subject to upn ====== " + upn);
				subject = (String) claims.get("upn");
			}
			AppThreadContext.setTokenSubject(subject);
			return true;
		} catch (Throwable t) {
			logger.error(t);
			return false;
		}

	}

	public boolean isValidTokenByCertificate(String token, Certificate certificate) {
		logger.debug("isValidTokenByCertificate.............begin....");
		if (token == null || certificate == null) {
			return false;
		}
		return isValidTokenByPublicKey(token, certificate.getPublicKey());
	}

	public boolean isValidTokenByCertificate(String token, InputStream certificateIS) {
		logger.debug("isValidTokenByCertificate.............begin....");
		if (token == null || certificateIS == null) {
			return false;
		}
		Certificate certificate = PkiUtil.getCertificateFromInputStream(certificateIS);
		return isValidTokenByCertificate(token, certificate);
	}

	/**
	 * Check if a JWT token is a valid.
	 */
	public boolean isValidToken(String token, PublicKey publicKey, InputStream certificate) {
		if (token == null) {
			logger.error("token is null, return false and check code!");
			return false;
		}
		if (publicKey == null && certificate == null) {
			logger.error("both publicKey and certificate are null, return false and check code!");
			return false;
		}
		boolean isValid = false;
		try {
			if (publicKey != null) {
				isValid = isValidTokenByPublicKey(token, publicKey);
			} else {
				isValid = isValidTokenByCertificate(token, certificate);
			}
			if (!isValid) {
				logger.error("token is not valid by public key or certificate, return false.......");
				return false;
			}
			return true;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return false;
		}
	}

	/**
	 * Check if it is a valid application specific JWT, and the content of JWT is
	 * correct as well.
	 */
	public boolean isValidJappToken(String token, PublicKey publicKey, InputStream certificate) {
		try {
			boolean isValid = isValidToken(token, publicKey, certificate);
			if (!isValid) {
				return false;
			}
			// check 2nd factor
			return validateJappToken(token);
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return false;
		}
	}

	public boolean validateJappToken(String token) {
		try {
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);
			if (claims == null || claims.isEmpty()) {
				logger.error("validateJappToken() - can not parse token claims, return false.......");
				return false;
			}
			// check target appid
			String appIdInClaims = "" + claims.get(AppConstant.JWT_CLAIM_KEY_TARGET_APPID);
			String appIdInConfig = ""
					+ ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			if (!appIdInClaims.equalsIgnoreCase(appIdInConfig)) {
				logger.error("validateJappToken() - target appid '" + appIdInClaims
						+ "' is different from configured appid '" + appIdInConfig + "', return false.......");
				return false;
			}
			// check target IP
			boolean validateTokenIP = ConfigFactory.appConfigService
					.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_TOKEN_VALIDATE_IP_ENABLED);
			if (validateTokenIP) {
				TransactionContext txnCtx = AppThreadContext.getTransactionContext();
				String clientIP = txnCtx.getClientIP();
				String ipInClaims = "" + claims.get(AppConstant.JWT_CLAIM_KEY_TARGET_IP);
				if (!ipInClaims.equalsIgnoreCase(clientIP)) {
					logger.error("validateJappToken() - target ip '" + ipInClaims
							+ "' is different from client request ip '" + clientIP + "', return false.......");
					return false;
				}
			}

			// check 2nd factor
//			MfaInfo secondFactorInfo = AppUtil.getSecondFactorInfoFromToken(token);
//			logger.debug("validateJappToken() - secondFactorInfo = " + secondFactorInfo);
//			boolean isVerified = secondFactorInfo.isVerified();
//			String type = secondFactorInfo.getType();
//			String value = secondFactorInfo.getValue();
//			if (StringUtil.isEmptyOrNull(type) || type.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE)
//					|| isVerified) {
//				logger.debug("validateJappToken() - no 2nd factor type in token or token has been verified, return true");
//				return true;
//			} else {
//				logger.error("validateJappToken() - 2nd factor value is not verified, return false...3....");
//				return false;
//			}
			return true;

		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return false;
		}
	}

	/**
	 * Create a new token by updating the expire time, and second factor
	 * authentication information in a existing token.
	 */
	public String updateToken(String token, Key privateKey, int expireMinutes, MfaVO secondFactorInfo) {
		if (StringUtil.isEmptyOrNull(token)) {
			return null;
		}
		Map<String, Object> claims = AppUtil.parseJwtClaims(token);
		if (claims == null || claims.isEmpty()) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
		}
		try {
			TransactionContext txnCtx = AppThreadContext.getTransactionContext();
			String clientIP = txnCtx.getClientIP();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime expire = now.plusMinutes(expireMinutes);
			Date expiryDate = java.util.Date.from(expire.atZone(ZoneId.systemDefault()).toInstant());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			String timeoutAt = expire.format(formatter);
			logger.debug("updateToken======update JWT expiry date==============" + timeoutAt);

			claims.put(AppConstant.JWT_CLAIM_KEY_TIMEOUT_AT, timeoutAt);
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID,
					ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			// claims = add2ndFactorClaims(claims, secondFactorInfo);

			// setClaims first
			String newToken = Jwts.builder().claims(claims).issuedAt(new Date()).expiration(expiryDate)
					.signWith(privateKey).compact();
			return newToken;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}

	/**
	 * Create a new token by extending the expire time in a existing token.
	 */
	public String extendToken(String token, int expireMinutes) {
		if (StringUtil.isEmptyOrNull(token)) {
			return null;
		}
		Map<String, Object> claims = AppUtil.parseJwtClaims(token);
		if (claims == null || claims.isEmpty()) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
		}
		try {
			TransactionContext txnCtx = AppThreadContext.getTransactionContext();
			String clientIP = txnCtx.getClientIP();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime expire = now.plusMinutes(expireMinutes);
			Date expiryDate = java.util.Date.from(expire.atZone(ZoneId.systemDefault()).toInstant());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			String timeoutAt = expire.format(formatter);
			logger.debug("extendToken======extend JWT expiry date==============" + timeoutAt);

			claims.put(AppConstant.JWT_CLAIM_KEY_TIMEOUT_AT, timeoutAt);
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID,
					ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			Key privateKey = AppComponents.pkiKeyCache.getJappPrivateKey();
			// setClaims first
			String newToken = Jwts.builder().claims(claims).issuedAt(new Date()).expiration(expiryDate)
					.signWith(privateKey).compact();
			return newToken;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}

	/**
	 * Issue a JAPP JWT token
	 */
	public String issueJappToken(AppIaaUser iaaUser) {
		logger.info("issueJappToken() - begin......");
		if (iaaUser == null) {
			logger.info("issueJappToken() - iaaUser is null, return null...");
		}
		String token = null;
		int expireMins = ConfigFactory.appConfigService
				.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH);
		try {
			Key key = AppComponents.pkiKeyCache.getJappPrivateKey();
			token = issueToken(iaaUser, key, expireMins);
			logger.debug("issueJappToken() - Issue JAPP token...end....");
			return token;
		} catch (Throwable t) {
			logger.info("issueJappToken()  - failed - \n " + AppUtil.getStackTrace(t));
			return null;
		}

	}

	/**
	 * Issue a JAPP JWT token
	 */
	public String issueToken(AppIaaUser iaaUser, Key privateKey, int expireMinutes) {

		if (iaaUser == null || privateKey == null) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
		}
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime expire = now.plusMinutes(expireMinutes);
			Date expiryDate = java.util.Date.from(expire.atZone(ZoneId.systemDefault()).toInstant());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			String timeoutAt = expire.format(formatter);
			logger.debug("issueToken======JWT expiry date==============" + timeoutAt);

			TransactionContext txnCtx = AppThreadContext.getTransactionContext();
			String clientIP = txnCtx.getClientIP();
			Map<String, Object> claims = AppComponents.iaaService.getTokenClaims(iaaUser);
			if (claims == null || claims.isEmpty()) {
				return null;
			}
			claims.put(AppConstant.JWT_CLAIM_KEY_TIMEOUT_AT, timeoutAt);
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID,
					ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			String sessionId = AppUtil.getSessionId(iaaUser);
			claims.put(AppConstant.JWT_CLAIM_KEY_CONTEXT_ID, sessionId);

			// setClaims first
			String token = Jwts.builder().claims(claims).issuer(AppConstant.JWT_TOKEN_ISSUE_BY)
					.subject(iaaUser.getUserIaaUID()).issuedAt(new Date()).expiration(expiryDate).signWith(privateKey)
					.compact();
			return token;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}


//	public static void main(String[] args) {
//		JwtService jwtService = new JwtService();
//		Claims claims = jwtService.getClaimsWithoutVerification("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSIsImtpZCI6IkNOdjBPSTNSd3FsSEZFVm5hb01Bc2hDSDJYRSJ9.eyJhdWQiOiJmYWZkNWUwYi1hZTE2LTRlOTgtODk0YS00MGRkMTkwYTFiNDEiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9jZGRjMTIyOS1hYzJhLTRiOTctYjc4YS0wZTVjYWNiNTg2NWMvIiwiaWF0IjoxNzQ0NzY1ODE2LCJuYmYiOjE3NDQ3NjU4MTYsImV4cCI6MTc0NDc2OTcxNiwiYWlvIjoiQVVRQXUvOFpBQUFBSlNsaEE4c3ZuaXFaeU5DZjdzRk8ySHJGYzk5WVpaenhxSS9UQVNKQTFPQVBSQWgxL3NLd0VUMzNOVGdvTWtveitoUElwcFBnaS9CaHE5QWk3V2lsTlE9PSIsImFtciI6WyJwd2QiXSwiZmFtaWx5X25hbWUiOiJTdW4iLCJnaXZlbl9uYW1lIjoiTWFydmluIiwiaXBhZGRyIjoiMTY1Ljg1LjE2Ni4yMzkiLCJuYW1lIjoiU3VuLCBNYXJ2aW4gKE1QQlNEUCkiLCJub25jZSI6Ijc4NDYzMjM5LWE3ZWQtNDhmMC1hMGIwLTA5NDU4N2FlMTNiOCIsIm9pZCI6IjM3N2Y5MzhiLWJkNTQtNDA2ZS04YTYzLTRlYzQyNGYxMjdlNyIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0zMzY1OTYxOTc1LTI3NTI1MDM3MjEtMzU1OTIyODMxNC0yNDA2MyIsInJoIjoiMS5BUndBS1JMY3pTcXNsMHUzaWc1Y3JMV0dYQXRlX2ZvV3JwaE9pVXBBM1JrS0cwRWNBRVVjQUEuIiwic2lkIjoiMDAzZjVhZDktNjc4MC00NjIwLTY1YWMtZDA1NzhjNzEzNDJiIiwic3ViIjoiT2xGS3hjbXBLRFpZUzNNMEYxSGlmUGtBZ2JwVXhaQjI3WjZnWXJLanJhayIsInRpZCI6ImNkZGMxMjI5LWFjMmEtNGI5Ny1iNzhhLTBlNWNhY2I1ODY1YyIsInVuaXF1ZV9uYW1lIjoiTWFydmluLlN1bkBvbnRhcmlvLmNhIiwidXBuIjoiTWFydmluLlN1bkBvbnRhcmlvLmNhIiwidXRpIjoiRWh6TzJuVHQyVUtsMXdLZHNpcVJBQSIsInZlciI6IjEuMCJ9.PZPMecqibliDhJrC-7GMjepg5gwPj-JYcG0lea48In74yWiO7zRwH0j706y9u8OMfOG6hhmav3dtH1lWQHcThup-4l7ND1muSltvpi2GLbbcF_x675YWqZRHnhBxoRHgUQe-S6oj_5zYYGQ3yx2f5_0-ELya5RDJCFEWLFZrMLecZxvD3zX6e65jjvnYlPlBZDz_GERCg7pyOOOLQtViwonEGnuzP4rv7V44XM0qZJK9IIXPtKM8KdhA-r18Nsoj9OmMQPO3APaWUKy22Zt1vfreqlInVwQa_mB21GX5U3zkKhWuxupb-Utesygj87MqPrCdAOsrSy_cYrwIgXJ-wg");
//		logger.debug("...........claims = " + claims);
//		
//	}
	
}
