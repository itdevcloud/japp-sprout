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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.PkiService;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class PkiKeyCache extends RefreshableCache{

	private static final Logger logger = LogManager.getLogger(PkiKeyCache.class);

	private static Key appPrivateKey;
	private static PublicKey appPublicKey;
	private static Certificate appCertificate;
	private static boolean comeFromKeyVault;
	
	private static Key tempAppPrivateKey ;
	private static PublicKey tempAppPublicKey;
	private static Certificate tempAppCertificate ;
	private static boolean tempComeFromKeyVault;


	@PostConstruct
	private void initService() {
	}
	@Override
	public String getInitOrder() {
		return "01";
	}

	@Override
	public void refreshCache() {
		if (lastUpdatedTS == -1) {
			initCache();
		}else {
			logger.info("PkiKeyCache.refreshCache() - only daily referesh is requried, do nothing...");
		}
	}

	@Override
	public synchronized void initCache() {
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 ) {
				logger.debug("PkiKeyCache.init() - begin...........");

				retrieveJappKeyPair();
				if (tempAppPrivateKey == null || tempAppPublicKey == null || tempAppCertificate == null) {
					String info = "JappKeyCache.init() - cannot retrieve JappPrivateKey, JappsPublicKey, tempAppCertificate, does not change current App Key Cache.......!!!";
					logger.error(info);
					AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, info);
					return;
				}
				initInProcess = true;
				appPrivateKey = tempAppPrivateKey;
				appPublicKey = tempAppPublicKey;
				appCertificate = tempAppCertificate;
				comeFromKeyVault = tempComeFromKeyVault;
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "PkiKeyCache.init() - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nJappPublicKey = " + (appPublicKey==null?null:"...");
				String info = "JappKeyCache.init() - JAPP Key come from Azure Key Vault = " + comeFromKeyVault + ", total time = " + (endTS - startTS) + " millis. \n";

				logger.info(str);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, info);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String errStr = CommonUtil.getStackTrace(e);
			logger.error(errStr);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE, errStr);
		} finally {
			initInProcess = false;
		}
	}

	public Key getAppPrivateKey() {
		waitForInit();
		return appPrivateKey;
	}

	public PublicKey getAppPublicKey() {
		waitForInit();
		return appPublicKey;
	}


	public Certificate getAppCertificate() {
		return appCertificate;
	}


//	public void setJappCertificate(Certificate jappCertificate) {
//		PkiKeyCache.appCertificate = jappCertificate;
//	}
	
	public void retrieveJappKeyPair() {

		InputStream in = null;
		FileInputStream is = null;
		tempAppPrivateKey = null;
		tempAppPublicKey = null;
		tempAppCertificate = null;
		tempComeFromKeyVault = false;
		try {

			if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.AZURE_KEYVAULT_ENABLED)) {
				logger.info(
						"retrieveJappKeyPair() try to load JAPP private and public key from Azure Key Vault............");
				tempComeFromKeyVault = true;
				String encodedPkcs12Str = AppComponents.pkiService.getAppKeyFromKeyVault();
				byte[] decodedPkcs12Bytes = Base64.getDecoder().decode(encodedPkcs12Str);
				//log.info("retrieveJappKeyPair() .....Pkcs12Str...." + encodedPkcs12Str);
				String pkcs12Password = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AZURE_KEYVAULT_JAPPCORE_PKCS12_PASSWORD);
				KeyStore keystore = KeyStore.getInstance("PKCS12");
				in = new ByteArrayInputStream(decodedPkcs12Bytes);
				keystore.load(in, pkcs12Password.toCharArray());
				//log.info("get key====save to file============>" );
				//FileOutputStream out = new FileOutputStream("c:\\temp\\myks.p12");
				//keystore.store(out, "12345".toCharArray());
				//out.close();

				Enumeration<String> enumeration = keystore.aliases();
				List<String> aliasList = new ArrayList<String>();
				while (enumeration != null && enumeration.hasMoreElements()) {
					String alias = (String) enumeration.nextElement();
					aliasList.add(alias);
					logger.debug("keystore alias name in Key Vault. alias=" + alias);
				}
				String keyAlias = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AZURE_KEYVAULT_CECRET_KEY_ALIAS);
				String kepPassword = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AZURE_KEYVAULT_CECRET_KEY_PASSWORD);
				if (aliasList.isEmpty()) {
					tempAppPrivateKey = null;
					tempAppPublicKey = null;
					tempAppCertificate = null;
					String err = "There is no alisses found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
					logger.error(err);
					throw new RuntimeException(err);
				}
				if (StringUtil.isEmptyOrNull(keyAlias)) {
					if (aliasList.size() == 1) {
						String alias = aliasList.get(0);
						logger.warn("retrieveJappKeyPair() - There is no keyAlias defined in property file, use only alias defined in pkcs12 stream, the alias name = " + alias);
						tempAppPrivateKey = keystore.getKey(alias, kepPassword.toCharArray());
						tempAppCertificate = keystore.getCertificate(alias);
						tempAppPublicKey = tempAppCertificate.getPublicKey();

					} else {
						tempAppPrivateKey = null;
						tempAppPublicKey = null;
						tempAppCertificate = null;
						String err = "There is no keyAlias defined in property file, but there are more than one alisses found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
						logger.error(err);
						throw new RuntimeException(err);
					}
				} else {
					boolean foundAlias = false;
					for (String alias : aliasList) {
						if (keyAlias.equalsIgnoreCase(alias)) {
							logger.info("retrieveJappKeyPair() .....alias name = " + alias);
							tempAppPrivateKey = keystore.getKey(alias, kepPassword.toCharArray());
							tempAppCertificate = keystore.getCertificate(alias);
							tempAppPublicKey = tempAppCertificate.getPublicKey();
							foundAlias = true;
							break;
						}
					}
					if (!foundAlias) {
						tempAppPrivateKey = null;
						tempAppPublicKey = null;
						tempAppCertificate = null;
						String err = "There is no alisses<" + keyAlias
								+ "> found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
						logger.error(err);
						throw new RuntimeException(err);
					}
				}
				//log.info("retrieveJappKeyPair()...Azure Key Vault...jappPublicKey=" + jappPublicKey);
			} else {
				String privateKeyStore = getPrivateKeyStore();
				logger.info("retrieveJappKeyPair() try to load JAPP private and public key in " + privateKeyStore
						+ " from classpath.....");
				tempComeFromKeyVault = false;
				KeyStore keystore = null;
				URL resource = PkiService.class.getClassLoader().getResource(privateKeyStore);
				if (resource == null) {
					String springPrivateKS = privateKeyStore.substring(1);
					resource = PkiService.class.getClassLoader().getResource(springPrivateKS);
					logger.info("retrieveJappKeyPair().......privateKeyStore=" + springPrivateKS);
				}
				if (resource == null) {
					String err ="retrieveJappKeyPair() can not get keystore resource, check code! privateKeyStore=" + privateKeyStore;
					logger.error(err);
					throw new RuntimeException(err);
				}
				File file = new File(resource.toURI());
				is = new FileInputStream(file);

				//logger.info("retrieveJappKeyPair().....FileInputStream =" + is);
				keystore = KeyStore.getInstance(KeyStore.getDefaultType());
				keystore.load(is, ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_STORE_PASSWORD).toCharArray());
				tempAppPrivateKey = keystore.getKey(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_ALIAS),
						ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_PASSWORD).toCharArray());
				// log.info("retrieveJappKeyPair().....jappPrivateKey==" + jappPrivateKey);
				tempAppCertificate = keystore.getCertificate(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_ALIAS));
//				logger.info("PkiKeyService.retrieveJappKeyPair().....jappCertificate =" + jappCertificate);
				tempAppPublicKey = tempAppCertificate.getPublicKey();
//				logger.info("PkiKeyService.retrieveJappKeyPair().....jappPublicKey =" + jappPublicKey.getClass());
			}
		} catch (Exception e) {
			tempAppPrivateKey = null;
			tempAppPublicKey = null;
			tempAppCertificate = null;
			tempComeFromKeyVault = false;
			logger.error(CommonUtil.getStackTrace(e));
			CommonUtil.throwRuntimeException(e);
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				in = null;
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				is = null;
			}
		}

	}
	
	private String getPrivateKeyStore() {
		String privateKeyStore = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_STORE);
		if (privateKeyStore != null && privateKeyStore.startsWith("classpath:")) {
			privateKeyStore = "/" + privateKeyStore.substring(10);
		}
		return privateKeyStore;
	}


}
