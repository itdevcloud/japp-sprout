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
import java.util.Enumeration;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * The pair of keys are stored in Azure Key Vault or in a private key store.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class PkiService implements AppFactoryComponentI {

	//private static final Logger logger = LogManager.getLogger(PkiService.class);
	private static final Logger logger = LogManager.getLogger(PkiService.class);

	private Key jappPrivateKey;
	private PublicKey jappPublicKey;
	private Certificate jappCertificate;
	private boolean comeFromKeyVault = false;

	//key name in Azure Key Vault
	@Value("${JappKeys:}")
	private String azureKeyVaultKeys;

	public String getAzureKeyVaultKeys() {
		return azureKeyVaultKeys;
	}

	public void setAzureKeyVaultKeys(String azureKeyVaultKeys) {
		this.azureKeyVaultKeys = azureKeyVaultKeys;
	}

	@PostConstruct
	private void init() {
		//try to avoid using AppConfig Service, AppComponents.appConfigCache may be not fully initiated yet
	}
	
	public boolean isComeFromKeyVault() {
		return comeFromKeyVault;
	}
	
	public Key getJappPrivateKey() {
		return jappPrivateKey;
	}

	public PublicKey getJappPublicKey() {
		return jappPublicKey;
	}

	public void retrieveJappKeyPair() {

		InputStream in = null;
		FileInputStream is = null;
		try {

			if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.AZURE_KEYVAULT_ENABLED)) {
				logger.info(
						"retrieveJappKeyPair() try to load JAPP private and public key from Azure Key Vault............");
				comeFromKeyVault = true;
				String encodedPkcs12Str = getAzureKeyVaultKeys();
				if(StringUtil.isEmptyOrNull(encodedPkcs12Str)) {
					logger.error("retrieveJappKeyPair() ..............no PKCS12 read from Azure Key Vault.......................");
					return;
				}
				byte[] decodedPkcs12Bytes = Base64.getDecoder().decode(encodedPkcs12Str);
				//log.info("retrieveJappKeyPair() .....Pkcs12Str...." + encodedPkcs12Str);
				String pkcs12Password = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AZURE_KEYVAULT_JAPPCORE_PKCS12_PASSWORD);
				if(StringUtil.isEmptyOrNull(encodedPkcs12Str)) {
					logger.error("retrieveJappKeyPair() ..............no key store password read from Azure Key Vault.......................");
					return;
				}
				
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
					jappPrivateKey = null;
					jappPublicKey = null;
					jappCertificate = null;
					String err = "There is no alisses found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
					logger.error(err);
					//throw new RuntimeException(err);
					return;
				}
				if (StringUtil.isEmptyOrNull(keyAlias)) {
					if (aliasList.size() == 1) {
						String alias = aliasList.get(0);
						logger.warn("retrieveJappKeyPair() - There is no keyAlias defined in property file, use only alias defined in pkcs12 stream, the alias name = " + alias);
						jappPrivateKey = keystore.getKey(alias, kepPassword.toCharArray());
						jappCertificate = keystore.getCertificate(alias);
						jappPublicKey = jappCertificate.getPublicKey();

					} else {
						jappPrivateKey = null;
						jappPublicKey = null;
						jappCertificate = null;
						String err = "There is no keyAlias defined in property file, but there are more than one alisses found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
						logger.error(err);
						//throw new RuntimeException(err);
						return;
					}
				} else {
					boolean foundAlias = false;
					for (String alias : aliasList) {
						if (keyAlias.equalsIgnoreCase(alias)) {
							logger.info("retrieveJappKeyPair() .....alias name = " + alias);
							jappPrivateKey = keystore.getKey(alias, kepPassword.toCharArray());
							jappCertificate = keystore.getCertificate(alias);
							jappPublicKey = jappCertificate.getPublicKey();
							foundAlias = true;
							break;
						}
					}
					if (!foundAlias) {
						jappPrivateKey = null;
						jappPublicKey = null;
						jappCertificate = null;
						String err = "There is no alisses<" + keyAlias
								+ "> found in PKCS12 file, Can't get public and private key from PKCS12 file!!!";
						logger.error(err);
						//throw new RuntimeException(err);
						return;
					}
				}
				//log.info("retrieveJappKeyPair()...Azure Key Vault...jappPublicKey=" + jappPublicKey);
			} else {
				String privateKeyStore = getPrivateKeyStore();
				logger.info("retrieveJappKeyPair() try to load JAPP private and public key in " + privateKeyStore
						+ " from classpath.....");
				comeFromKeyVault = false;
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
					//throw new RuntimeException(err);
					return;
				}
				File file = new File(resource.toURI());
				is = new FileInputStream(file);

				//logger.info("retrieveJappKeyPair().....FileInputStream =" + is);
				keystore = KeyStore.getInstance(KeyStore.getDefaultType());
				keystore.load(is, ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_STORE_PASSWORD).toCharArray());
				jappPrivateKey = keystore.getKey(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_ALIAS),
						ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_PASSWORD).toCharArray());
				// log.info("retrieveJappKeyPair().....jappPrivateKey==" + jappPrivateKey);
				jappCertificate = keystore.getCertificate(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.SERVER_SSL_KEY_ALIAS));
//				logger.info("PkiKeyService.retrieveJappKeyPair().....jappCertificate =" + jappCertificate);
				jappPublicKey = jappCertificate.getPublicKey();
//				logger.info("PkiKeyService.retrieveJappKeyPair().....jappPublicKey =" + jappPublicKey.getClass());
			}
		} catch (Exception e) {
			jappPrivateKey = null;
			jappPublicKey = null;
			logger.error(e, e);
			//throw AppUtil.throwRuntimeException(e);
			return;
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

	public Certificate getJappCertificate() {
		return jappCertificate;
	}

	public void setJappCertificate(Certificate jappCertificate) {
		this.jappCertificate = jappCertificate;
	}

}
