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
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.iaa.token.AppLocalTokenHandler;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
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

	public TokenHandlerI getAccessTokenHandler() {
		String handlerName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_ACCESSTOKEN_HANDLER_NAME);
		TokenHandlerI tokenHandler = AppFactory.getTokenHandler(handlerName);
		if(tokenHandler == null) {
			logger.error("Can't find access token handler by name: " + handlerName + ", check configuration file! JappApiLocalTokenHandler instead......... ");
			tokenHandler = AppFactory.getTokenHandler(AppLocalTokenHandler.class.getSimpleName());
		}
		return tokenHandler;
	}

	public String issueAccessToken(IaaUserI iaaUser) {
		TokenHandlerI tokenHandler = getAccessTokenHandler();
		Key privateKey = AppComponents.pkiService.getAppPrivateKey();
		return tokenHandler.issueAccessToken(iaaUser, privateKey, -1, null);
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
			//AppThreadContext.setTokenSubject(subject);
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
			logger.error(CommonUtil.getStackTrace(t));
			return false;
		}
	}
	



}
