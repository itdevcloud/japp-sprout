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
package com.itdevcloud.japp.core.api.rest;

import java.security.KeyRep;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.cahce.PkiKeyCache;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/open")
public class PkiController {

	private static final Logger logger = LogManager.getLogger(PkiController.class);
	

	@GetMapping(value="/key")
	public PublicKey getPublicKey() {
		logger.debug("PkiController.getPublicKey() - start......");
		PublicKey key = AppComponents.pkiKeyCache.getJappPublicKey();
		return key;
	}
	
	@GetMapping(value="/keyrep")
	public KeyRep getKeyRep() {
		logger.debug("PkiController.getKeyRep() - start......");
		PublicKey key = AppComponents.pkiKeyCache.getJappPublicKey();
		KeyRep keyRep = new KeyRep(KeyRep.Type.PUBLIC, key.getAlgorithm(), key.getFormat(), key.getEncoded());
		return keyRep;
	}
	
	@GetMapping(value="/certificate")
	public Certificate getCertificate() {
		logger.debug("PkiController.getCertificate() - start......");
		return AppComponents.pkiKeyCache.getJappCertificate();
	}

}
