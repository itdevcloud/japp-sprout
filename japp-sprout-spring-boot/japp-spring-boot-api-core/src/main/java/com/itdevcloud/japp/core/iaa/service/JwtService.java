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

import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.iaa.token.AppLocalTokenHandler;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class JwtService implements AppFactoryComponentI {
	
	private static final Logger logger = LogManager.getLogger(JwtService.class);
	private TokenHandlerI coreTokenHandler = null;

	@PostConstruct
	public void init() {
	}

	public TokenHandlerI getTokenHandler() {
		if(coreTokenHandler == null) {
			String handlerName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_ACCESSTOKEN_HANDLER_NAME);
			coreTokenHandler = AppFactory.getTokenHandler(handlerName);
			if(coreTokenHandler == null) {
				logger.warn("Can't find access token handler by name: " + handlerName + ", use AppLocalTokenHandler instead......... ");
				coreTokenHandler = AppFactory.getTokenHandler(AppLocalTokenHandler.class.getSimpleName());
			}
		}
		return coreTokenHandler;
	}
	
	public Map<String, Object> parseTokenHeaders(String token) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.parseTokenHeaders(token);
	}
	public Map<String, Object> parseTokenClaims(String token) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.parseTokenClaims(token);
	}
	
	public Map<String, Object> isValidToken(String token, Map<String, Object> claimEqualMatchMap, boolean ingoreNullInToken, String... args ) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.isValidToken(token, claimEqualMatchMap, ingoreNullInToken, args );
	}
	
	public IaaUserI getIaaUserBasedOnToken(String token) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.getIaaUserBasedOnToken(token);
	}

	public String issueToken(IaaUserI iaaUser, String tokenType, Map<String, Object> customClaimMap) {
		if(iaaUser == null) {
			String errMsg = "issueToken()......iaaUser is null in the request!";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
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
		//clear nonce and uip if not enforced
		if(!AppUtil.isEnforceTokenIp()) {
			iaaUser.setHashedUserIp(null);
		}
		if(!AppUtil.isEnforceTokenNonce()) {
			iaaUser.setHashedNonce(null);
		}
		return tokenHandler.issueToken(iaaUser, tokenType, privateKey, -1, customClaimMap);
	}
	
	public Map<String, Object>  isValidTokenByPublicKey(String token, PublicKey publicKey) {
		TokenHandlerI tokenHandler = getTokenHandler();
		return tokenHandler.isValidTokenByPublicKey(token, publicKey);

	}

	public Map<String, Object> isValidTokenByCertificate(String token, InputStream certificate) {
		if (token == null || certificate == null) {
			String errMsg = "isValidTokenByCertificate()......token or certificate is null/empty in the request!";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		BufferedInputStream bis = new BufferedInputStream(certificate);
		CertificateFactory cf = null;
		Certificate cert = null;
		
		try {
			while (bis.available() <= 0) {
				logger.error("invalid x509 certificate..............");
				return null;
			}
			cf = CertificateFactory.getInstance("X.509");
			cert = cf.generateCertificate(bis);

			bis.close();
			bis = null;
			certificate.close();
			certificate = null;

		} catch (IOException e1) {
			logger.error("can not load x509 certificate.....Error: " + e1);
			logger.error(e1);
			return null;
		} catch (CertificateException e) {
			logger.error("can not load x509 certificate......Error: " + e);
			logger.error(e);
			return null;
		}
		
//		logger.debug("Certificate===========" + cert.toString());
		PublicKey publicKey = cert.getPublicKey();
//		logger.debug("pubic key===========" + publicKey.toString());
		return isValidTokenByPublicKey(token, publicKey);

	}
	
}
