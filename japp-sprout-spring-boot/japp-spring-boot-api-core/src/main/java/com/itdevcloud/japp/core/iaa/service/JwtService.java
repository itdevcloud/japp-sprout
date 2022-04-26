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
import java.util.Map;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.iaa.token.AppLocalTokenHandler;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
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

	public TokenHandlerI getTokenHandler() {

		String handlerName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_ACCESSTOKEN_HANDLER_NAME);
		TokenHandlerI tokenHandler = AppFactory.getTokenHandler(handlerName);
		if(tokenHandler == null) {
			logger.error("Can't find access token handler by name: " + handlerName + ", check configuration file! JappApiLocalTokenHandler instead......... ");
			tokenHandler = AppFactory.getTokenHandler(AppLocalTokenHandler.class.getSimpleName());
		}
		return tokenHandler;
	}
	
	public boolean isValidToken(String token, Map<String, String> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.isValidToken(token, claimEqualMatchMap, ingoreNullInToken, args );
	}
	
	public IaaUserI getIaaUser(String token) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.getIaaUser(token);
	}

	public String issueToken(IaaUserI iaaUser, String tokenType, Map<String, Object> customClaimMap) {
		TokenHandlerI tokenHandler = getTokenHandler();
		Key privateKey = AppComponents.pkiService.getAppPrivateKey();
		//default is refresh token
		if(StringUtil.isEmptyOrNull(tokenType)) {
			tokenType = TokenHandlerI.TYPE_REFRESH_TOKEN;
		}else if (!TokenHandlerI.TYPE_ACCESS_TOKEN.equalsIgnoreCase(tokenType) &&
				  !TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType) &&
				  !TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType)) {
			tokenType = TokenHandlerI.TYPE_REFRESH_TOKEN;
		}
		return tokenHandler.issueToken(iaaUser, tokenType, privateKey, -1, customClaimMap);
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
	


}
