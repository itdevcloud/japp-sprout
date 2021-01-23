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
package com.itdevcloud.japp.core.api.command;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.common.AppConfigKeys;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Spring restful controller for the command based (or 'RPC' style) API service
 * This style is convenient for internal communication. for external APIs, 
 * they can be published separately by using different style. e.g. real REATful style
 *
 * Command based restful api services are leverage following naming conventions:
 *  - Command can be provided by using query string or embedded in the json request string.
 *  - Request - CommandRequest.java (Capital first char, must extend BaseRequest.java)
 *  - Response - CommandResponse.java (Capital first char, must extend BaseResponse.java)
 *  - Processor - CommandProcessor.java (Capital first char, must extend RequestProcessor.java)
 *  e.g.
 *  - Command - echo
 *  - Request - EchoRequest.java
 *  - Response - EchoResponse.java
 *  - Processor - EchoProcessor.java
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT + "}")
public class CommandController extends BaseCommandController {

	private static final Logger logger = LogManager.getLogger(CommandController.class);

    @Operation(summary = "Json based RPC style API", 
 		   description = "Json based RPC style API, mainly for internal use.", 
 		   tags = { "RPC Command" },
			   security = {@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}")})
 
	@PostMapping("/rpcapi/v1/cmd")
	public String process(@RequestParam("cmd") Optional<String> command, @RequestBody(required = false) String jsonRequestString) {
		logger.debug("CommandController.process() - start......");
		String cmd = command.isPresent()?command.get():null;
		return processCommand(cmd, jsonRequestString);
	}
}
