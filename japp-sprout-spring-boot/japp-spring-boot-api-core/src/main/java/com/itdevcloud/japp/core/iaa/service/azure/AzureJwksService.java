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
package com.itdevcloud.japp.core.iaa.service.azure;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.HttpResponse;
import com.itdevcloud.japp.core.iaa.service.PkiService;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.config.AppConfigService;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AzureJwksService implements AppFactoryComponentI{

	//private static final Logger logger = LogManager.getLogger(AzureJwksService.class);
	private static final Logger logger = LogManager.getLogger(AzureJwksService.class);

	// private String tenant = "common";
	// private String openIdMetaDataUrl = "https://login.microsoftonline.com/" +
	// tenant
	// + "/.well-known/openid-configuration";

	private String aadOpenIdMetaDataUrl;
	private String aadClientId;
	private String aadAuthUrl;
	private String aadAuthLogoutUrl;
    
	
	@PostConstruct
	private void init() {
		logger.info("AzureJwksService.init().....begin........");
		aadOpenIdMetaDataUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_OPEN_ID_METADATA_URL);
		aadClientId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_CLIENT_ID);
		aadAuthUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_AUTH_URL);
		aadAuthLogoutUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_AUTH_LOGOUT_URL);
	}

	/**
	 * Get this application's client ID which was registered in Azure AD.
	 * @return client id
	 */
	public String getAadClientId() {
		return aadClientId;
	}

	/**
	 * Get the AAD authorization endpoint url for this application. 
	 * @return authorization endpoint url
	 */
	public String getAadAuthUri() {

		logger.info("getAadAuthUri() begin............");
		if (!StringUtil.isEmptyOrNull(aadAuthUrl)) {
			logger.info("getAadAuthUri() get ...aadAuthUrl from property file = " + aadAuthUrl);
			return aadAuthUrl;
		}
		logger.info("getAadAuthUri() read ...aadAuthUrl from aadOpenIdMetaDataUrl = " + aadOpenIdMetaDataUrl);

		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		//List<AzureJwksKey> keys = null;
		try {
			if (StringUtil.isEmptyOrNull(aadOpenIdMetaDataUrl)) {
				logger.info("getAadAuthUri() aadOpenIdMetaDataUrl is null or empty, do nothing...........");
				return null;
			}

			// get meta data for the tenant
			HttpResponse httpResponse = AppComponents.httpService.doGet(aadOpenIdMetaDataUrl, null, true);
			if (httpResponse == null) {
				logger.info("getAadAuthUri() httpResponse is null or empty, do nothing...........");
				return null;
			}
			
			String openIdMetaData = httpResponse.getResposebody();
			if (StringUtil.isEmptyOrNull(openIdMetaData)) {
				logger.info("getAadAuthUri() openIdMetaData is null or empty, do nothing...........");
				return null;
			}

			JsonObject obj = JsonParser.parseString(openIdMetaData).getAsJsonObject();
			if (obj == null) {
				logger.info("getAadAuthUri() JsonObject is null or empty, do nothing...........");
				return null;
			}

			// get authUri
			aadAuthUrl = obj.get("authorization_endpoint").getAsString();
			logger.info("\ngetAadAuthUri() .....from Entra ID response......... aadAuthUrl = " + aadAuthUrl);

			logger.info("getAadAuthUri() end...");
			return aadAuthUrl;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			return null;
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			} else {
//				throw new RuntimeException(e);
//			}
//
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

	}

	/**
	 * Get the AAD logout endpoint url for this application
	 * @return logout endpoint url
	 */
	public String getAadAuthLogoutUri() {

		logger.info("getAadAuthLogoutUri() begin...");
		if (!StringUtil.isEmptyOrNull(aadAuthLogoutUrl)) {
			logger.info("getAadAuthLogoutUri() get ...aadAuthLogoutUrl from property file = " + aadAuthLogoutUrl);
			return aadAuthLogoutUrl;
		}
		logger.info("getAadAuthLogoutUri() read ...aadAuthLogoutUrl from aadOpenIdMetaDataUrl = " + aadOpenIdMetaDataUrl);

		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		//List<AzureJwksKey> keys = null;
		try {
			if (StringUtil.isEmptyOrNull(aadOpenIdMetaDataUrl)) {
				logger.info("getAadAuthLogoutUri() -  Entra ID OpenIdMetaDataUrl is null or empty, do nothing...........");
				return null;
			}
			// get meta data for the tenant
			HttpResponse httpResponse = AppComponents.httpService.doGet(aadOpenIdMetaDataUrl, null, true);
			if (httpResponse == null) {
				logger.info("getAadAuthLogoutUri() -  httpResponse is null or empty, do nothing...........");
				return null;
			}
			String openIdMetaData = httpResponse.getResposebody();
			if (StringUtil.isEmptyOrNull(openIdMetaData)) {
				logger.info("getAadAuthLogoutUri() -  openIdMetaData is null or empty, do nothing...........");
				return null;
			}

			JsonObject obj = JsonParser.parseString(openIdMetaData).getAsJsonObject();
			if (obj == null) {
				logger.info("getAadAuthLogoutUri() -  JsonObject is null or empty, do nothing...........");
				return null;
			}

			// get authUri
			aadAuthLogoutUrl = obj.get("end_session_endpoint").getAsString();
			logger.info("\ngetAadAuthLogoutUri() ..... aadAuthLogoutUrl = " + aadAuthLogoutUrl);

			logger.info("getAadAuthLogoutUri() end...");
			return aadAuthLogoutUrl;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			return null;
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			} else {
//				throw new RuntimeException(e);
//			}

		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

	}

	/**
	 * Retrieve public keys that is contained in the tenant's OpenID Connect discovery document
	 * @return List of AzureJwksKey
	 */
	public List<AzureJwksKey> getJwksKeys() {

		logger.info("getJwksKeys() begin...");
		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		List<AzureJwksKey> keys = null;
		try {
			if (StringUtil.isEmptyOrNull(aadOpenIdMetaDataUrl)) {
				logger.info("getJwksKeys() -  EntraID OpenIdMetaDataUrl is null or empty, do nothing...........");
				return null;
			}
			// get meta data for the tenant
			logger.info("\ngetJwksKeys() ..... aadOpenIdMetaDataUrl = " + aadOpenIdMetaDataUrl);
			
			HttpResponse httpResponse = AppComponents.httpService.doGet(aadOpenIdMetaDataUrl, null, true);
			if (httpResponse == null ) {
				logger.info("getJwksKeys() -  httpResponse is null or empty, do nothing...........");
				return null;
			}
			String openIdMetaData = httpResponse.getResposebody();
			if (StringUtil.isEmptyOrNull(openIdMetaData)) {
				logger.info("getJwksKeys() -  openIdMetaData is null or empty, do nothing...........");
				return null;
			}
			JsonObject obj = JsonParser.parseString(openIdMetaData).getAsJsonObject();
			if (obj == null) {
				logger.info("getJwksKeys() -  JsonObject from httpresponse is null or empty, do nothing...........");
				return null;
			}
			// get jwks key uri
			String jwksUrl = obj.get("jwks_uri").getAsString();
			logger.info("\ngetJwksKeys() ..... jwksUrl = " + jwksUrl);
			if (StringUtil.isEmptyOrNull(jwksUrl)) {
				logger.info("getJwksKeys() -  jwksUrl is null or empty, do nothing...........");
				return null;
			}

			// get key json
			HttpResponse jwksResponse = AppComponents.httpService.doGet(jwksUrl, null, true);
			if (jwksResponse == null) {
				logger.info("getJwksKeys() -  jwksResponse is null or empty, do nothing...........");
				return null;
			}
			String jwks = jwksResponse.getResposebody();
			if (StringUtil.isEmptyOrNull(jwks)) {
				logger.info("getJwksKeys() -  jwks is null or empty, do nothing...........");
				return null;
			}
			Gson gson = new GsonBuilder().serializeNulls().create();

			AzureJwksKeys jwksKeys = gson.fromJson(jwks, AzureJwksKeys.class);
			logger.info("getJwksKeys() ..... jwksKeys = \n" + jwksKeys);

			if (jwksKeys == null) {
				String err = "Can't retrive AAD JWKS keys........!!!!!!!!!!!!!!";
				logger.error(err);
				//throw new RuntimeException(err);
				return null;
			}
			keys = jwksKeys.getKeys();
			if (keys == null || keys.isEmpty()) {
				String err = "AAD JWKS keys has empty list.......!!!!!!!!!!!!!!";
				logger.error(err);
				//throw new RuntimeException(err);
				return null;
			}
			// process each key - to get certificate and public key
			for (AzureJwksKey key : keys) {
				// get singing certificate string (the first one only, others are chain)
				String certStr = (key.getX5c() == null ? null : key.getX5c().get(0));
				if (certStr == null || certStr.trim().equals("")) {
					String err = "Can't Parse AzureJwksKey: ...Stop....!!!!!!!!!!!!!!" + key;
					logger.error(err);
					//throw new RuntimeException(err);
					return null;
				}
				Certificate cert = AppUtil.getCertificateFromString(certStr);
				PublicKey publicKey = AppUtil.getPublicKeyFromCertificate(cert);

				key.setCertificate(cert);
				key.setPublicKey(publicKey);
			} // end for
			logger.info("getJwksKeys() end.............");
			return keys;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
			return null;
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			} else {
//				throw new RuntimeException(e);
//			}

		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

	}

}
