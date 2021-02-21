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

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;

/**
 * The pair of keys are stored in Azure Key Vault or in a private key store.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class PkiService implements AppFactoryComponentI {

	//key name in Azure Key Vault
	@Value("${AppPKCS12Key:}")
	private String appKeyFromKeyVault;

	public String getAppKeyFromKeyVault() {
		return appKeyFromKeyVault;
	}

	@PostConstruct
	private void init() {
	}
	
	
	public Key getAppPrivateKey() {
		return AppComponents.pkiKeyCache.getAppPrivateKey();
	}

	public PublicKey getAppPublicKey() {
		return AppComponents.pkiKeyCache.getAppPublicKey();
	}


	public Certificate getAppCertificate() {
		return AppComponents.pkiKeyCache.getAppCertificate();
	}


}
