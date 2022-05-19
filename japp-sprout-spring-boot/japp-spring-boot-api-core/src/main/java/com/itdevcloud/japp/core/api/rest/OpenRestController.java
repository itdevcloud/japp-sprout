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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.bean.EchoRequest;
import com.itdevcloud.japp.core.api.bean.EchoResponse;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController


@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT+ "}")
@SecurityRequirement(name = AppConstant.JAPPCORE_OPENAPI_CORE_SECURITY_SCHEMA_NAME)
public class OpenRestController extends BaseRestController {

	@Operation(summary = "Echo a request", description = "Echo back a request. This service could be used as health check porpuse", tags = {
			"Core-Open" })

	@RequestMapping(value = "/open/core/echo", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	EchoResponse echo(@RequestBody EchoRequest request) {
		EchoResponse response = null;
//		if ((response = checkIsEnabled(EchoResponse.class)) != null) {
//			return response;
//		}
		response = processRequest(request, EchoResponse.class);
		return response;
	}

	@Operation(summary = "Load Transfer Token To Client Script", description = "Load Transfer Token To Client Script", tags = {
			"Core-Open" })

	@RequestMapping(value="/open/core/transfer_token_to_client_script", method=RequestMethod.GET)
	String getTransferTokenToClientScript() {
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = OpenRestController.class.getResourceAsStream("/page/transfer_token_to_client_script.js");
			if (inputStream == null) {
				throw new Exception("can not load transfer_token_to_client_script.js, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder(e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return sb.toString();
	}

	@Operation(summary = "Load Transfer Token To Client Style", description = "Load Transfer Token To Client Style", tags = {
	"Core-Open" })

	@RequestMapping(value="/open/core/transfer_token_to_client_style", method=RequestMethod.GET)
	String getTransferTokenToClientStyle() {
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = OpenRestController.class.getResourceAsStream("/page/transfer_token_to_client_style.css");
			if (inputStream == null) {
				throw new Exception("can not load transfer_token_to_client_style.css, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
			inputStream.close();
			inputStream = null;
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder(e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return sb.toString();
	}

	@Operation(summary = "Load Online Login Error Script", description = "Load Online Login Error Script", tags = {
			"Core-Open" })

	@RequestMapping(value="/open/core/online_login_error_script", method=RequestMethod.GET)
	public String getOnlineLoginErrorScript() {
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = OpenRestController.class.getResourceAsStream("/page/online_login_error_script.js");
			if (inputStream == null) {
				throw new RuntimeException("can not load online_login_error_script.js file, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder(e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return sb.toString();
	}


}
