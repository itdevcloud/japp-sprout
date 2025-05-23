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

/**
 * provides centralized command process controller.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.CommandInfo;
import com.itdevcloud.japp.core.common.AppFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.processor.RequestProcessor;
import com.itdevcloud.japp.se.common.util.StringUtil;

@Component
public abstract class BaseCommandController {

	private static final Logger logger = LogManager.getLogger(BaseCommandController.class);

	public String processCommand(String command, String jsonRequest) {

		if (StringUtil.isEmptyOrNull(command) && StringUtil.isEmptyOrNull(jsonRequest)) {
			BaseResponse response = AppUtil.createBaseResponse("N/A",
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "command and jsonRequest can not be both null or empty!");
			return toJsonResponse(response);
		}

		BaseRequest request = null;

		if (StringUtil.isEmptyOrNull(jsonRequest)) {
			//allow submit a command without submitting a request json string: 
			//e.g. command <getserverinfo>
			//but must have a request bean defined for the command
			//command can not be null in this block
			request = getNewRequestInstanceFromCommand(command);
			if (request == null) {
				BaseResponse response = AppUtil.createBaseResponse(command,
						ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
						"processCommand() - there is no request bean defined for command <" + command + ">");
				return toJsonResponse(response);
			}	// call processor
			logger.info("processCommand() - start ===>>> command = '" + command + "'" + ", jsonRequest = \n" + jsonRequest);
			BaseResponse response = processRequest(request);
			logger.info("processCommand() - end <<<=== command = '" + command + "'");

			return toJsonResponse(response);

		}
		//json string is not null from here
		jsonRequest = handleNullForDeserialization(jsonRequest);
		String cmd = getCommandFromJsonString(jsonRequest);
		if(StringUtil.isEmptyOrNull(command)) {
			command = cmd;
		}else {
			if(cmd != null && !cmd.equalsIgnoreCase(command)) {
				BaseResponse response = AppUtil.createBaseResponse(command,
						ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Command provided in query Parameter <" + command + "> does not match command provided in JSON request <" + cmd + "> !");
				return toJsonResponse(response);

			}
		}

		logger.info("processCommand() - start ===>>> command = '" + command + "'" + ", jsonRequest = \n" + jsonRequest);
		try {
			if (StringUtil.isEmptyOrNull(command) ) {
				BaseResponse response = AppUtil.createBaseResponse("N/A",
						ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Request must be Json String and command must be provided through query paraneter or inside JSON request!");

				return toJsonResponse(response);
			}
			//command is not null from here
			request = toRequest(command, jsonRequest);

			if (request == null) {
				BaseResponse response = AppUtil.createBaseResponse(command,
						ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
						"can't initialize request instance for command: '" + command + "', check command  or json request string. ");

				return toJsonResponse(response);
			}

			// call processor
			BaseResponse response = processRequest(request);
			//make sure command is set
			response.setCommand(command);
			
			String jsonResponse = toJsonResponse(response);
			logger.info("processCommand() - end <<<=== command = '" + command + "'");
			//logger.debug("processCommand().....process end <==  command = '" + command + "'" + ", jsonResponse = \n" + jsonResponse);

			return jsonResponse;

		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(t);
			BaseResponse response = AppUtil
					.createBaseResponse(command, ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, t.getMessage());
			return toJsonResponse(response);
		}
	}

	private BaseRequest getNewRequestInstanceFromCommand(String command) {
		logger.debug("getEmptyRequestInstanceFromCommand() start ...");
		if (StringUtil.isEmptyOrNull(command)) {
			return null;
		}
		CommandInfo commandInfo = AppFactory.getCommandInfo(command, null);
		if (commandInfo == null) {
			return null;
		}
		BaseRequest request = commandInfo.getRequest();
		//create a new instance
		return AppFactory.getInstance(request.getClass());
	}
	
	private BaseResponse processRequest(BaseRequest request) {
		logger.debug("processRequest() - start ...");
		if (request == null) {
			//should not goes into here anyway
			BaseResponse response = AppUtil.createBaseResponse("N/A",
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
					"processRequest() - request parameter is null, check code!");
			return response;
		}
		String requestSimpleName = request.getClass().getSimpleName();

		BaseResponse response = null;
		CommandInfo commandInfo = AppFactory.getCommandInfo(null, requestSimpleName);
		if (commandInfo == null) {
			response = AppUtil.createBaseResponse(request.getCommand(),
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
					"processRequest() - processor not found for request: '" + requestSimpleName
					+ "'....");
			return response;
		}
		RequestProcessor requestProcessor = commandInfo.getProcessor();
		if (requestProcessor == null) {
			response = AppUtil.createBaseResponse(request.getCommand(),
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
					"processRequest() - processor not found for request: '" + requestSimpleName
					+ "'....");
			return response;
		}

		response = requestProcessor.process(request);

		logger.debug("processRequest() - end ... request = '" + requestSimpleName + "'");
		return response;
	}

	private BaseRequest toRequest(String command, String jsonRequest) {
		if (StringUtil.isEmptyOrNull(command) || StringUtil.isEmptyOrNull(jsonRequest)) {
			return null;
		}
		CommandInfo commandInfo = AppFactory.getCommandInfo(command, null);
		if (commandInfo == null) {
			return null;
		}
		Class<?> requestClass = commandInfo.getRequestClass();
		if (requestClass == null) {
			return null;
		}
		Object request = null;

		Gson gson = new GsonBuilder().serializeNulls().create();
		request = gson.fromJson(jsonRequest, requestClass);
		return (BaseRequest) request;
	}

	private String getCommandFromJsonString(String jsonRequest) {
		String cmd = null;
		try {
			JsonObject jsonObject = JsonParser.parseString(jsonRequest).getAsJsonObject();
			JsonElement jsonElement = jsonObject.get("command");
			cmd = (jsonElement==null?null:jsonElement.getAsString());
			return cmd;
		}catch(Throwable t) {
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}

	}

	private String toJsonResponse(BaseResponse response) {
		if (response == null) {
			ResponseStatus commandResponseStatus = new ResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "response can not be null !");
			BaseResponse tmpResponse = new BaseResponse();

			tmpResponse.setResponseStatus(commandResponseStatus);
			response = tmpResponse;
		}

		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(response);
	}

	private String handleNullForDeserialization(String jsonString) {
		if (StringUtil.isEmptyOrNull(jsonString)) {
			return jsonString;
		}
		String pattern = "(:)(\\s*)([,\\}])";
		String output = jsonString.replaceAll(pattern, "$1\"\"$3");
		return output;
	}

}