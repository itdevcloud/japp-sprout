package com.itdevcloud.japp.core.api.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.bean.EchoRequest;
import com.itdevcloud.japp.core.api.bean.EchoResponse;
import com.itdevcloud.japp.core.common.AppConfigKeys;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Spring restful controller.
 *
 *  e.g.
 *  - Request - EchoRequest.java
 *  - Response - EchoResponse.java
 *  - Processor - EchoProcessor.java
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT + "}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultRestController extends BaseRestController {

	private static final Logger logger = LogManager.getLogger(DefaultRestController.class);

    @Operation(summary = "Echo a request", 
    		   description = "Echo back a request. This service could be used as health check porpuse", 
    		   tags = { "Echo" },
   			   security = {@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}")})
    
	@PostMapping("/api/v1/core/echo")
	EchoResponse echo(@RequestBody EchoRequest request) {
		EchoResponse response = (EchoResponse) processRequest(request);
		return response;
	}
	

}
