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
import com.itdevcloud.japp.core.api.bean.BasicAuthRequest;
import com.itdevcloud.japp.core.api.bean.BasicAuthResponse;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthRequest;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthResponse;
import com.itdevcloud.japp.core.api.bean.TokenAuthRequest;
import com.itdevcloud.japp.core.api.bean.TokenAuthResponse;
import com.itdevcloud.japp.core.api.bean.ValidateOrIssueNewTokenRequest;
import com.itdevcloud.japp.core.api.bean.ValidateOrIssueNewTokenResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT
		+ "}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = AppConstant.JAPPCORE_OPENAPI_CORE_SECURITY_SCHEMA_NAME)
public class AuthRestController extends BaseRestController {

	@Operation(summary = "Basic Auth Service", description = "Autheticate User by LoginId and Passwrod", tags = {
			"Core-Auth" })

	@PostMapping("/open/core/basicauth")
	BasicAuthResponse basicAuth(@RequestBody BasicAuthRequest request) {
		BasicAuthResponse response = null;
//		if ((response = AppComponents.commonService.checkIsEnabledCommand(BasicAuthResponse.class)) != null) {
//			return response;
//		}
		response = processRequest(request, BasicAuthResponse.class);
		return response;
	}

	@Operation(summary = "Signed Basic Auth Service", description = "2-factor authentication: basic + certificate", tags = {
			"Core-Auth" }, security = { @SecurityRequirement(name = "core-bear-jwt") })

	@PostMapping("/open/core/signedauth")
	SignedBasicAuthResponse signedBasicAuth(@RequestBody SignedBasicAuthRequest request) {

		SignedBasicAuthResponse response = null;
//		if ((response = AppComponents.commonService.checkIsEnabledCommand(SignedBasicAuthResponse.class)) != null) {
//			return response;
//		}
		response = (SignedBasicAuthResponse) processRequest(request, SignedBasicAuthResponse.class);

		return response;
	}

	@Operation(summary = "Token Based Auth Service", description = "Validate Token (refresh, ID or access token issued by this application and Issue Access Token", tags = {
			"Core-Auth" })

	@PostMapping("/open/core/tokenauth")
	TokenAuthResponse tokenAuth(@RequestBody TokenAuthRequest request) {
		TokenAuthResponse response = null;
//		if ((response = AppComponents.commonService.checkIsEnabledCommand(TokenAuthResponse.class)) != null) {
//			return response;
//		}
		response = processRequest(request, TokenAuthResponse.class);
		return response;
	}

	@Operation(summary = "Validate or Issue New Token", description = "Validate Token issued by this application or partners, Generate new Token if newToken Tyoe is not null", tags = {
			"Core-Auth" })

	@PostMapping("/api/core/tokenvalidation")
	ValidateOrIssueNewTokenResponse validateOrIssueNewToken(@RequestBody ValidateOrIssueNewTokenRequest request) {
		ValidateOrIssueNewTokenResponse response = null;
//		if ((response = AppComponents.commonService.checkIsEnabledCommand(ValidateOrIssueNewTokenResponse.class)) != null) {
//			return response;
//		}
		response = processRequest(request, ValidateOrIssueNewTokenResponse.class);
		return response;
	}
}
