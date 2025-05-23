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

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class PkiKeyCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(PkiKeyCache.class);

	private static Key jappPrivateKey;
	private static PublicKey jappPublicKey;
	private static Certificate jappCertificate;

	@PostConstruct
	private void initService() {
	}

	@Override
	protected String createDisplayString() {
		String str = "jappCertificate=" + (jappCertificate==null?null:"***") + ", jappPublicKey=" + (jappPublicKey==null?null:"***") + 
				", jappPrivateKey=" + (jappPrivateKey==null?null:"***")  ;
		return str;
	}

	@Override
	public synchronized void refreshCache() {
		try {

			AppComponents.pkiService.retrieveJappKeyPair();
			Key tmpJappPrivateKey = AppComponents.pkiService.getJappPrivateKey();
			PublicKey tempJappPublicKey = AppComponents.pkiService.getJappPublicKey();
			Certificate tempJappCertificate = AppComponents.pkiService.getJappCertificate();
			boolean comeFromKeyVault = AppComponents.pkiService.isComeFromKeyVault();
			if (tmpJappPrivateKey == null || tempJappPublicKey == null) {
				String info = "JappKeyCache.init() - cannot retrieve JappPrivateKey, JappsPublicKey, does not change current Japp Key Cache.......!!!";
				logger.error(info);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE,
						info);
				return;
			}
			jappPrivateKey = tmpJappPrivateKey;
			jappPublicKey = tempJappPublicKey;
			jappCertificate = tempJappCertificate;
		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public Key getJappPrivateKey() {
		waitForInit();
		return jappPrivateKey;
	}

	public PublicKey getJappPublicKey() {
		waitForInit();
		return jappPublicKey;
	}

	public Certificate getJappCertificate() {
		return jappCertificate;
	}

	public void setJappCertificate(Certificate jappCertificate) {
		PkiKeyCache.jappCertificate = jappCertificate;
	}

}
