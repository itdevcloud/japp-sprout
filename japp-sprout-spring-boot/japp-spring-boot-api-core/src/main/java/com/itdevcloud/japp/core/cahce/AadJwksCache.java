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
package com.itdevcloud.japp.core.cahce;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.azure.AzureJwksKey;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This cache will be refreshed daily. 
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AadJwksCache extends RefreshableCache {


	private static final Logger logger = LogManager.getLogger(AadJwksCache.class);

	private static List<AzureJwksKey> aadJwksKeys = null;
	private static String aadAuthUri = null;
	private static String aadAuthLogoutUri = null;
	private static String aadClientId = null;


	@PostConstruct
	public void initService() {
	}

	@Override
	public void refreshCache() {
		if (lastUpdatedTS == -1) {
			initCache();
		}else {
			logger.info("AadJwksCache.refreshCache() - only daily referesh is requried, do nothing...");
		}
	}

	@Override
	public synchronized void initCache() {
		if (!AppConstant.IDENTITY_PROVIDER_AAD_OIDC.equalsIgnoreCase(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER))) {
			String info = "AadJwksCache.init()...... Authentication Provider is not " + AppConstant.IDENTITY_PROVIDER_AAD_OIDC
					+ ", no cache is needed, do nothing......";
			logger.info(info);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
			return;
		}
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL))) {
				logger.debug("AadJwksCache.init()...start......");

				List<AzureJwksKey> tmpJwksKeys = AppComponents.azureJwksService.getJwksKeys();
				if (tmpJwksKeys == null || tmpJwksKeys.isEmpty()) {
					String info = "AadJwksCache.init()....cannot retrieve AAD JWKS Keys, does not change current key Cache.......!!!";
					logger.error(info);
					AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
					return;
				}
				String tmpAuthUri = AppComponents.azureJwksService.getAadAuthUri();
				String tmpAuthLogoutUri = AppComponents.azureJwksService.getAadAuthLogoutUri();
				String tmpClientId = AppComponents.azureJwksService.getAadClientId();
				if (StringUtil.isEmptyOrNull(tmpAuthUri) || StringUtil.isEmptyOrNull(tmpClientId)||StringUtil.isEmptyOrNull(tmpAuthLogoutUri)) {
					String info = "AadJwksCache.init()....cannot retrieveAAD Auth Uri, AuthLogoutUri,  or clientId, does not change these values in memeory.......!!!";
					logger.error(info);
					AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
					return;
				}
				initInProcess = true;
				aadJwksKeys = tmpJwksKeys;
				aadAuthUri = tmpAuthUri;
				aadAuthLogoutUri = tmpAuthLogoutUri;
				aadClientId = tmpClientId;
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "AadJwksCache.init()  - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\naadAuthUri = " + aadAuthUri + "\n AAD JWKS Keys = " + aadJwksKeys + "\naadJwksKeys size = "
						+ aadJwksKeys.size();

				logger.info(str);
				String info = "AadJwksCache.init() - total time = " + (endTS - startTS) + " millis. Result:"
						+ "\naadJwksKeys size = "+ aadJwksKeys.size() ;
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
			}
		} catch (Exception e) {
			String errStr = CommonUtil.getStackTrace(e);
			logger.error(errStr);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, errStr);
		} finally {
			initInProcess = false;
		}
	}

	public String getAadAuthUri() {
		waitForInit();
		return aadAuthUri;
	}
	
	public String getAadAuthLogoutUri() {
		waitForInit();
		return aadAuthLogoutUri;
	}

	public String getAadClientId() {
		return aadClientId;
	}

	public PublicKey getAadPublicKey(String kid, String x5t) {
		waitForInit();
		if ((kid == null || kid.trim().equals("")) && (x5t == null || x5t.trim().equals(""))) {
			logger.error("getAadPublicKey() - kid or x5t is null / empty, return null.....");
			return null;
		}
		if (aadJwksKeys == null || aadJwksKeys.isEmpty()) {
			logger.error("getAadPublicKey() - aadJwksKeys is null / empty, return null.....");
			return null;
		}
		for (AzureJwksKey key : aadJwksKeys) {
			if (kid != null) {
				if (kid.equals(key.getKid())) {
					return key.getPublicKey();
				}
			} else {
				if (x5t.equals(key.getX5t())) {
					return key.getPublicKey();
				}
			}
		}
		logger.error("getAadPublicKey() - can't find keys defined for kid = " + kid + ", x5t = " + x5t);
		return null;
	}



}
