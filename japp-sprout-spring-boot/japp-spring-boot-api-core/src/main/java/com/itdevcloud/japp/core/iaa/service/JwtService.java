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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
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
	}

	public boolean isValidAadIdToken(String idToken) {
		try {
			if (idToken == null || idToken.trim().isEmpty()) {
				return false;
			}
			int idx = idToken.lastIndexOf('.');
			String tokenWithoutSignature = idToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
			Header header = jwtWithoutSignature.getHeader();
			Claims claims = jwtWithoutSignature.getBody();

			String kid = (header.containsKey("kid") ? (String) header.get("kid") : null);
			String x5t = (header.containsKey("x5t") ? (String) header.get("xt5") : null);
			PublicKey publicKey = AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
			boolean isValid = isValidTokenByPublicKey(idToken, publicKey);

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

	public boolean isValidTokenByPublicKey(String token, PublicKey publicKey) {
		logger.debug("isValidTokenByPublicKey.............begin....");
		if (token == null || publicKey == null) {
			return false;
		}
		try {
			Jws<Claims> jwts = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
			Claims claims = jwts.getBody();
			String subject = claims.getSubject();
			logger.info("subject ====== " + subject);
			if (claims.containsKey("upn")) {
				String upn = (String) claims.get("upn");
				logger.info("convert subject to upn ====== " + upn);
				subject = (String) claims.get("upn");
			}
			AppThreadContext.setTokenSubject(subject);
			return true;
		} catch (SignatureException e) {
			logger.error(e);
			return false;
		}

	}

	public boolean isValidTokenByCertificate(String token, InputStream certificate) {
		logger.debug("isValidTokenByCertificate.............begin....");
		if (token == null || certificate == null) {
			return false;
		}
		BufferedInputStream bis = new BufferedInputStream(certificate);
		CertificateFactory cf = null;
		Certificate cert = null;
		
		try {
			while (bis.available() <= 0) {
				logger.error("invalid x509 certificate..............");
				return false;
			}
			cf = CertificateFactory.getInstance("X.509");
			cert = cf.generateCertificate(bis);

			bis.close();
			bis = null;
			certificate.close();
			certificate = null;

		} catch (IOException e1) {
			logger.error(e1);
			return false;
		} catch (CertificateException e) {
			logger.error(e);
			return false;
		}
		
//		logger.debug("Certificate===========" + cert.toString());
		PublicKey publicKey = cert.getPublicKey();
//		logger.debug("pubic key===========" + publicKey.toString());

		return isValidTokenByPublicKey(token, publicKey);

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
	 * Check if it is a valid application specific JWT, and the content of JWT is correct as well.
	 */
	public boolean isValidPiscesJappToken(String token, PublicKey publicKey, InputStream certificate) {
		try {
			boolean isValid = isValidToken(token, publicKey, certificate);
			if (!isValid) {
				return false;
			}
			// check 2nd factor
			return validatePiscesJappToken(token);
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return false;
		}
	}

	public boolean validatePiscesJappToken(String token) {
		try {
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);
			if (claims == null || claims.isEmpty()) {
				logger.error("validatePiscesJappToken() - can not parse token claims, return false.......");
				return false;
			}
			//check target appid
			String appIdInClaims = ""+claims.get(AppConstant.JWT_CLAIM_KEY_TARGET_APPID);
			String appIdInConfig = ""+ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			if (!appIdInClaims.equalsIgnoreCase(appIdInConfig)) {
				logger.error("validatePiscesJappToken() - target appid '" + appIdInClaims + "' is different from configured appid '" + appIdInConfig +"', return false.......");
				return false;
			}
			//check target IP
			boolean validateTokenIP = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_TOKEN_VALIDATE_IP_ENABLED);
			if (validateTokenIP) {
				TransactionContext txnCtx = AppThreadContext.getTransactionContext();
				String clientIP = txnCtx.getClientIP();
				String ipInClaims = ""+claims.get(AppConstant.JWT_CLAIM_KEY_TARGET_IP);
				if (!ipInClaims.equalsIgnoreCase(clientIP)) {
					logger.error("validatePiscesJappToken() - target ip '" + ipInClaims + "' is different from client request ip '" + clientIP +"', return false.......");
					return false;
				}
			}

			// check 2nd factor
			SecondFactorInfo secondFactorInfo = AppUtil.getSecondFactorInfoFromToken(token);
			logger.debug("validatePiscesJappToken() - secondFactorInfo = " + secondFactorInfo);
			boolean isVerified = secondFactorInfo.isVerified();
			String type = secondFactorInfo.getType();
			String value = secondFactorInfo.getValue();
			if (StringUtil.isEmptyOrNull(type) || type.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE)
					|| isVerified) {
				logger.debug("validatePiscesJappToken() - no 2nd factor type in token or token has been verified, return true");
				return true;
			} else {
				logger.error("validatePiscesJappToken() - 2nd factor value is not verified, return false...3....");
				return false;
			}
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return false;
		}
	}

	/**
	 * Create a new token by updating the expire time, and second factor authentication information in a existing token.
	 */
	public String updateToken(String token, Key privateKey, int expireMinutes, SecondFactorInfo secondFactorInfo) {
		if (StringUtil.isEmptyOrNull(token)) {
			return null;
		}
		Map<String, Object> claims = AppUtil.parseJwtClaims(token);
		if (claims == null || claims.isEmpty()) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
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
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID, ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			claims = add2ndFactorClaims(claims, secondFactorInfo);

			// setClaims first
			String newToken = Jwts.builder().setClaims(claims).setIssuedAt(new Date()).setExpiration(expiryDate)
					.signWith(SignatureAlgorithm.RS256, privateKey).compact();
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
			expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
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
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID, ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			Key privateKey = AppComponents.pkiKeyCache.getPiscesJappPrivateKey();
			// setClaims first
			String newToken = Jwts.builder().setClaims(claims).setIssuedAt(new Date()).setExpiration(expiryDate)
					.signWith(SignatureAlgorithm.RS256, privateKey).compact();
			return newToken;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}

	/**
	 * Issue a PISCESJAPP JWT token
	 */ 
	public String issuePiscesJappToken(IaaUser piscesjappIaaUser) {
		logger.info("issuePiscesJappToken() - begin......");
		if (piscesjappIaaUser == null) {
			logger.info("issuePiscesJappToken() - piscesjappIaaUser is null, return null...");
		}
		String token = null;
		int expireMins = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH);
		try {
			String secondFactorType = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_2NDFACTOR_TYPE);
			SecondFactorInfo secondFactorInfo = new SecondFactorInfo();
			String user2ndFactorType = piscesjappIaaUser.getTwoFactorAuthType();
			
			if (user2ndFactorType.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE)){
				if ((secondFactorType.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE))){
					secondFactorInfo.setType(secondFactorType);
					secondFactorInfo.setVerified(false);
					secondFactorInfo.setValue(null);
					expireMins = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
				}else {
					secondFactorInfo.setType(secondFactorType);
					secondFactorInfo.setVerified(false);
					String tmpV = AppComponents.iaaService.getAndSend2ndfactorValue(piscesjappIaaUser, secondFactorType);
					secondFactorInfo.setValue(Hasher.hashPassword(tmpV));
				}
			} else {
				secondFactorInfo.setType(user2ndFactorType);
				secondFactorInfo.setVerified(false);
				String tmpV = AppComponents.iaaService.getAndSend2ndfactorValue(piscesjappIaaUser, user2ndFactorType);
				secondFactorInfo.setValue(Hasher.hashPassword(tmpV));
			}
			Key key = AppComponents.pkiKeyCache.getPiscesJappPrivateKey();
			token = issueToken(piscesjappIaaUser, key, expireMins, secondFactorInfo);
			logger.debug("issuePiscesJappToken() - Issue PISCESJAPP token...end....secondFactorInfo = " + secondFactorInfo);
			return token;
		} catch (Throwable t) {
			logger.info("issuePiscesJappToken()  - failed - \n " + AppUtil.getStackTrace(t));
			return null;
		}

	}

	/**
	 * Issue a PISCESJAPP JWT token
	 */
	public String issueToken(IaaUser piscesjappIaaUser, Key privateKey, int expireMinutes,
			SecondFactorInfo secondFactorInfo) {

		if (piscesjappIaaUser == null || privateKey == null) {
			return null;
		}
		if (expireMinutes <= 0) {
			expireMinutes = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
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
			Map<String, Object> claims = AppComponents.iaaService.getTokenClaims(piscesjappIaaUser);
			if (claims == null || claims.isEmpty()) {
				return null;
			}
			claims.put(AppConstant.JWT_CLAIM_KEY_TIMEOUT_AT, timeoutAt);
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_APPID, ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID));
			claims.put(AppConstant.JWT_CLAIM_KEY_TARGET_IP, clientIP);

			claims = add2ndFactorClaims(claims, secondFactorInfo);

			// setClaims first
			String token = Jwts.builder().setClaims(claims).setIssuer(AppConstant.JWT_TOKEN_ISSUE_BY)
					.setSubject(piscesjappIaaUser.getUserId()).setIssuedAt(new Date()).setExpiration(expiryDate)
					.signWith(SignatureAlgorithm.RS256, privateKey).compact();
			return token;
		} catch (Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}

	private Map<String, Object> add2ndFactorClaims(Map<String, Object> claimMap, SecondFactorInfo secondFactorInfo) {
		if (secondFactorInfo == null) {
			secondFactorInfo = new SecondFactorInfo();
			secondFactorInfo.setType(AppConstant.IAA_2NDFACTOR_TYPE_NONE);
			secondFactorInfo.setVerified(false);
		}
		boolean verified = secondFactorInfo.isVerified();
		String type = secondFactorInfo.getType();
		String value = secondFactorInfo.getValue();
		//logger.error("hashed 2nd factor value in token (2) - " + value);

		int retryCount = secondFactorInfo.getRetryCount();
		if (claimMap == null) {
			claimMap = new HashMap<String, Object>();
		}
		if (StringUtil.isEmptyOrNull(type) || type.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_NONE)) {
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_TYPE, AppConstant.IAA_2NDFACTOR_TYPE_NONE);
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VERIFIED, false);
		} else if (type.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_VERIFICATION_CODE)){
			if (!StringUtil.isEmptyOrNull(value)) {
				claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VERIFIED, verified);
				claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_TYPE, type);
				claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VALUE, value);
				claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT, retryCount);
			} else {
				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR,
						"no verification code provided to create the token!");
			}
		} else if (type.equalsIgnoreCase(AppConstant.IAA_2NDFACTOR_TYPE_TOTP)){
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VERIFIED, verified);
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_TYPE, type);
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VALUE, null);
			claimMap.put(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT, retryCount);
		}else {
			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR,
					"2 factor type is not supportrf! type = " + type);
		}
		return claimMap;
	}

}
