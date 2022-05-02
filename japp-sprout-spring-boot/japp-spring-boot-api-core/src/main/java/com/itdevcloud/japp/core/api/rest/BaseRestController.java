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
/**
 * provides pre-defined, centralized RESTful controller.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.processor.RequestProcessor;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public abstract class BaseRestController {


	private static final Logger logger = LogManager.getLogger(BaseRestController.class);

	@Value("${" + AppConfigKeys.JAPPCORE_APP_CORE_CONTROLLER_ENABLED_COMMANDS + ":none}")
	private String enabledCommands;
	private Set<String> enabledCommandSet = null;
	
	@PostConstruct
	public void init() {
		this.enabledCommandSet = new HashSet<String>();
		if(StringUtil.isEmptyOrNull(enabledCommands) || "none".equalsIgnoreCase(enabledCommands)) {
			return;
		}
		String[] cmdArr = enabledCommands.split(",");
		for(String cmd: cmdArr) {
			enabledCommandSet.add(cmd.trim());
		}
	}
	
	protected <T extends BaseResponse> T checkIsEnabled(Class<T> responseClass) {
		if (responseClass == null) {
			throw new RuntimeException("BaseRestController.checkIsEnabled() - responseClass is null, check code!");
		}
		String classSimpleName = responseClass.getSimpleName();
		int idx = classSimpleName.indexOf(AppUtil.RESPONSE_POSTFIX);
		if (idx <= 0) {
			throw new RuntimeException("BaseRestController.checkIsEnabled() - responseClass name is not correct, check code!");
		}
		String command = classSimpleName.substring(0, idx);
		if(StringUtil.isEmptyOrNull(command) || !enabledCommandSet.contains(command)) {
			if(enabledCommandSet.contains("all")) {
				return null;
			}
			T response = AppUtil.createResponse(responseClass, "N/A", Status.NA,
					"command '" + command + "' is not enabled!");
			return response;
		}
		return null;
	}
	
	public <O extends BaseResponse, I extends BaseRequest> O processRequest(I request, Class<O> responseClass) {
		try {
			logger.debug("processRequest() - start ===>");
			if (request == null) {
				O response = AppUtil.createResponse(responseClass, "N/A",
						Status.ERROR_VALIDATION, " request parameter is null!");
				return response;
			}
			String requestSimpleName = request.getClass().getSimpleName();
	
			RequestProcessor requestProcessor = AppFactory.getRequestProcessor(requestSimpleName);
			if (requestProcessor == null) {
				O response = AppUtil.createResponse(responseClass, "N/A",
						Status.ERROR_VALIDATION, "processor not found for request: '" + requestSimpleName
							+ "'....");
				return response;
			}
	
			O response = requestProcessor.process(request, responseClass);
	
			logger.debug("processRequest() - end <=== request = '" + requestSimpleName + "'");
			return response;
		} catch (AppException ae) {
			ae.printStackTrace();
			logger.error(ae);
			O response = AppUtil.createResponse(responseClass, "N/A",
					ae.getStatus(), ae.getMessage());
			return response;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(t);
			O response = AppUtil.createResponse(responseClass, "N/A",
					Status.ERROR_SYSTEM_ERROR, t.getMessage());
			return response;
		}
	}

}