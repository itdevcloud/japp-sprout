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

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.azure.AzureJwksKey;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This cache will be refreshed daily.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class EntraIdJwksCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(EntraIdJwksCache.class);

	private static List<AzureJwksKey> aadJwksKeys = null;
	private static String aadAuthUri = null;
	private static String aadAuthLogoutUri = null;
	private static String aadClientId = null;

	@PostConstruct
	public void initService() {
	}

//	@Override
//	public void refreshCache() {
//		if (lastUpdatedTS == -1) {
//			initCache();
//		}else {
//			logger.info("AadJwksCache.refreshCache() - only daily referesh is requried, do nothing...");
//		}
//	}

	@Override
	protected String createDisplayString() {
		String str = "aadAuthUri = " + aadAuthUri + "\n Entra ID JWKS Keys = " + CommonUtil.listToString(aadJwksKeys)
				+ "\naadJwksKeys size = " + (aadJwksKeys == null?0:aadJwksKeys.size());
		return str;
	}

	@Override
	protected void refreshCache() {
		if (!AppConstant.AUTH_PROVIDER_ENTRAID_OPENID.equalsIgnoreCase(ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER))) {
			String info = "EntraIdJwksCache.refreshCache()...... Authentication Provider is not "
					+ AppConstant.AUTH_PROVIDER_ENTRAID_OPENID + ", no cache is needed, do nothing......";
			logger.info(info);
			// AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE,
			// info);
			return;
		}
		try {
			List<AzureJwksKey> tmpJwksKeys = AppComponents.azureJwksService.getJwksKeys();
			if (tmpJwksKeys == null || tmpJwksKeys.isEmpty()) {
				String info = "EntraIdJwksCache.refreshCache()....cannot retrieve AAD JWKS Keys, will not change these values in current cache!";
				logger.error(info);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
				return;
			}
			String tmpAuthUri = AppComponents.azureJwksService.getAadAuthUri();
			String tmpAuthLogoutUri = AppComponents.azureJwksService.getAadAuthLogoutUri();
			String tmpClientId = AppComponents.azureJwksService.getAadClientId();
			if (StringUtil.isEmptyOrNull(tmpAuthUri) || StringUtil.isEmptyOrNull(tmpClientId)
					|| StringUtil.isEmptyOrNull(tmpAuthLogoutUri)) {
				String info = "EntraIdJwksCache.refreshCache()....cannot retrieveAAD Auth Uri, AuthLogoutUri, or clientId, will not change these values in current cache!";
				logger.error(info);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE, info);
				return;
			}
			aadJwksKeys = tmpJwksKeys;
			aadAuthUri = tmpAuthUri;
			aadAuthLogoutUri = tmpAuthLogoutUri;
			aadClientId = tmpClientId;

		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
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
