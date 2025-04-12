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

import java.security.PublicKey;
import java.security.cert.Certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.se.common.util.PkiUtil;
import com.itdevcloud.japp.se.common.vo.PkiPemVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
//@RequestMapping(value = "/open")
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT + "}/open")
public class PkiController {

	private static final Logger logger = LogManager.getLogger(PkiController.class);
	

	@GetMapping(value="/publickey")
	public PkiPemVO getPublicKey() {
		logger.debug("PkiController.getPublicKey() - start............");
		
		PublicKey key = AppComponents.pkiKeyCache.getJappPublicKey();
		
		PkiPemVO pkiPemVO = PkiUtil.getPublicKeyPemString(key);
		logger.debug("PkiController.getPublicKey() - end............");
		
		return pkiPemVO;
	}
	
	
	@GetMapping(value="/certificate")
	public PkiPemVO getCertificate() {
		logger.debug("PkiController.getCertificate() - start......");
		Certificate certificate = AppComponents.pkiKeyCache.getJappCertificate();
		PkiPemVO pkiPemVO = PkiUtil.getCertificateAndPublicKeyPemString(certificate, false);
		logger.debug("PkiController.getCertificate() - end......");
		return pkiPemVO;
	}

}
