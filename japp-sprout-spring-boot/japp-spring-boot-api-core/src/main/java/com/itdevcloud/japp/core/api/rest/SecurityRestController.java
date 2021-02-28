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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.DecryptTextRequest;
import com.itdevcloud.japp.core.api.bean.DecryptTextResponse;
import com.itdevcloud.japp.core.api.bean.EncryptTextRequest;
import com.itdevcloud.japp.core.api.bean.EncryptTextResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT + "}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
public class SecurityRestController extends BaseRestController {

	//private static final Logger logger = LogManager.getLogger(DefaultRestController.class);
	
	@Value("${" + AppConfigKeys.JAPPCORE_APP_SECURITY_CONTROLLER_ENABLED + ":false}")
	private boolean defaultControllerEnabled;

	private <T extends BaseResponse> T checkIsEnabled(Class<T> responseClass) {
    	if(!defaultControllerEnabled) {
			T response = AppUtil.createResponse(responseClass, "N/A",
					ResponseStatus.STATUS_CODE_WARN_NOACTION, "PKI controller is not enabled!");
			return response;
		}else {
			return null;
		}
	}

    @Operation(summary = "Encrypt Text Request", 
    		   description = "Encrypt Text", 
    		   tags = { "Core-Security" },
   			   security = {@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}")})
    
	@PostMapping("/api/core/encryption")
    EncryptTextResponse echo(@RequestBody EncryptTextRequest request) {
    	EncryptTextResponse response = null;
		if( (response = checkIsEnabled(EncryptTextResponse.class)) != null) {
    		return response;
    	}
		response = processRequest(request, EncryptTextResponse.class);
		return response;
	}
	
    @Operation(summary = "Decrypt Text Request", 
 		   description = "Decrypt Text", 
 		   tags = { "Core-Security" },
			   security = {@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}")})
 
	@PostMapping("/api/core/decryption")
    DecryptTextResponse basicAuth(@RequestBody DecryptTextRequest request) {
    	DecryptTextResponse response = null;
		if( (response = checkIsEnabled(DecryptTextResponse.class)) != null) {
 		return response;
 	}
		response = processRequest(request, DecryptTextResponse.class);
		return response;
	}
	
}
