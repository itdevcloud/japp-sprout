package com.itdevcloud.japp.core.api.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.EchoRequest;
import com.itdevcloud.japp.core.api.bean.EchoResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;

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

	//private static final Logger logger = LogManager.getLogger(DefaultRestController.class);
	@Value("${" + AppConfigKeys.JAPPCORE_APP_DEFAULT_CONTROLLER_ENABLED + ":false}")
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

    @Operation(summary = "Echo a request", 
    		   description = "Echo back a request. This service could be used as health check porpuse", 
    		   tags = { "Echo" },
   			   security = {@SecurityRequirement(name = "${jappcore.openapi.security.requirement.name}")})
    
	@PostMapping("/api/core/echo")
	EchoResponse echo(@RequestBody EchoRequest request) {
    	EchoResponse response = null;
		if( (response = checkIsEnabled(EchoResponse.class)) != null) {
    		return response;
    	}
		response = processRequest(request, EchoResponse.class);
		return response;
	}
	
}
