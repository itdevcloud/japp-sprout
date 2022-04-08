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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.bean.DecryptTextRequest;
import com.itdevcloud.japp.core.api.bean.DecryptTextResponse;
import com.itdevcloud.japp.core.api.bean.EncryptTextRequest;
import com.itdevcloud.japp.core.api.bean.EncryptTextResponse;
import com.itdevcloud.japp.core.api.bean.SignTextRequest;
import com.itdevcloud.japp.core.api.bean.SignTextResponse;
import com.itdevcloud.japp.core.api.bean.VerifySignatureRequest;
import com.itdevcloud.japp.core.api.bean.VerifySignatureResponse;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT
		+ "}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SecurityRestController extends BaseRestController {

	@Operation(summary = "Encrypt Text Request", description = "Encrypt Text", tags = { "Core-Security" }, security = {
			@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}") })

	@PostMapping("/api/core/encryption")
	EncryptTextResponse encryptText(@RequestBody EncryptTextRequest request) {
		EncryptTextResponse response = null;
		if ((response = checkIsEnabled(EncryptTextResponse.class)) != null) {
			return response;
		}
		response = processRequest(request, EncryptTextResponse.class);
		return response;
	}

	@Operation(summary = "Decrypt Text Request", description = "Decrypt Text", tags = { "Core-Security" }, security = {
			@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}") })

	@PostMapping("/api/core/decryption")
	DecryptTextResponse decryptText(@RequestBody DecryptTextRequest request) {
		DecryptTextResponse response = null;
		if ((response = checkIsEnabled(DecryptTextResponse.class)) != null) {
			return response;
		}
		response = processRequest(request, DecryptTextResponse.class);
		return response;
	}

	@Operation(summary = "Sign Text Request", description = "Sign Text", tags = { "Core-Security" }, security = {
			@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}") })

	@PostMapping("/api/core/sign")
	SignTextResponse signText(@RequestBody SignTextRequest request) {
		SignTextResponse response = null;
		if ((response = checkIsEnabled(SignTextResponse.class)) != null) {
			return response;
		}
		response = processRequest(request, SignTextResponse.class);
		return response;
	}

	@Operation(summary = "Verify Signature Request", description = "Verify Signature Text", tags = { "Core-Security" }, security = {
			@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}") })

	@PostMapping("/api/core/signatureVerification")
	VerifySignatureResponse signatureVerification(@RequestBody VerifySignatureRequest request) {
		VerifySignatureResponse response = null;
		if ((response = checkIsEnabled(VerifySignatureResponse.class)) != null) {
			return response;
		}
		response = processRequest(request, VerifySignatureResponse.class);
		return response;
	}

}
