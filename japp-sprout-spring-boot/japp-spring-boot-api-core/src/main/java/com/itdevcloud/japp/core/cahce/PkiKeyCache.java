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

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class PkiKeyCache extends RefreshableCache{

	private static final Logger logger = LogManager.getLogger(PkiKeyCache.class);

	private static Key piscesjappPrivateKey;
	private static PublicKey piscesjappPublicKey;
	private static Certificate piscesjappCertificate;


	@PostConstruct
	private void initService() {
	}

	@Override
	public void refreshCache() {
		if (lastUpdatedTS == -1) {
			initCache();
		}else {
			logger.info("PiscesJappKeyCache.refreshCache() - only daily referesh is requried, do nothing...");
		}
	}

	@Override
	public synchronized void initCache() {
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL))) {
				logger.debug("PiscesJappKeyCache.init() - begin...........");

				AppComponents.pkiService.retrievePiscesJappKeyPair();
				Key tmpPiscesJappPrivateKey = AppComponents.pkiService.getPiscesJappPrivateKey();
				PublicKey tempPiscesJappPublicKey = AppComponents.pkiService.getPiscesJappPublicKey();
				Certificate tempPiscesJappCertificate = AppComponents.pkiService.getPiscesJappCertificate();
				boolean comeFromKeyVault = AppComponents.pkiService.isComeFromKeyVault();
				if (tmpPiscesJappPrivateKey == null || tempPiscesJappPublicKey == null) {
					String info = "PiscesJappKeyCache.init() - cannot retrieve PiscesJappPrivateKey, PiscesJappsPublicKey, does not change current PiscesJapp Key Cache.......!!!";
					logger.error(info);
					AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, info);
					return;
				}
				initInProcess = true;
				piscesjappPrivateKey = tmpPiscesJappPrivateKey;
				piscesjappPublicKey = tempPiscesJappPublicKey;
				piscesjappCertificate = tempPiscesJappCertificate;
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "PiscesJappKeyCache.init() - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nPiscesJappPublicKey = " + (piscesjappPublicKey==null?null:"...");
				String info = "PiscesJappKeyCache.init() - PISCESJAPP Key come from Azure Key Vault = " + comeFromKeyVault + ", total time = " + (endTS - startTS) + " millis. \n";

				logger.info(str);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, info);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String errStr = AppUtil.getStackTrace(e);
			logger.error(errStr);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, errStr);
		} finally {
			initInProcess = false;
		}
	}

	public Key getPiscesJappPrivateKey() {
		waitForInit();
		return piscesjappPrivateKey;
	}

	public PublicKey getPiscesJappPublicKey() {
		waitForInit();
		return piscesjappPublicKey;
	}


	public Certificate getPiscesJappCertificate() {
		return piscesjappCertificate;
	}


	public void setPiscesJappCertificate(Certificate piscesjappCertificate) {
		PkiKeyCache.piscesjappCertificate = piscesjappCertificate;
	}


}
