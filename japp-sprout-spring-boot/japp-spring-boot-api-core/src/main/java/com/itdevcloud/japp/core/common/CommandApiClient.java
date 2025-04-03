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
package com.itdevcloud.japp.core.common;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is a generic command-based api client.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class CommandApiClient {

	//private static Logger logger = LogManager.getLogger(CommandApiClient.class);
	private static final Logger logger = LogManager.getLogger(CommandApiClient.class);

	public <T> T invoke(String apiUrlPath, String command, Object reqeustObj, Class<T> responseClass) {
		if(reqeustObj == null ) {
			logger.info("invoke() - reqeustObj is null, do nothing......");
			return null;
		}
		Gson gson = new GsonBuilder().serializeNulls().create();
		String jsonRequestString =  gson.toJson(reqeustObj);
		return invoke(apiUrlPath, command, jsonRequestString,  responseClass);

	}
	
	public <T> T invoke(String apiUrlPath, String command, String jsonRequestString, Class<T> responseClass) {

		logger.debug("invoke() - begin..........");
		if(apiUrlPath == null || apiUrlPath.trim().equals("")) {
			logger.info("invoke() - apiUrlPath is null or empty, do nothing......");
			return null;
		}
		if(jsonRequestString == null || jsonRequestString.trim().equals("")) {
			logger.info("invoke() - jsonRequestString is null or empty, do nothing......");
			return null;
		}
		if (responseClass == null) {
			logger.info("invoke() - responseClass is null, do nothing......");
			return null;
		}

		long start = System.currentTimeMillis();
		logger.info("invoke() - start...apiUrlPath = '" + apiUrlPath + "', command = '" + command
				+ "' jsonRequest:\n" + jsonRequestString);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(apiUrlPath);

		WebTarget commandWebTarget = webTarget;
		if(command != null && !command.trim().equals("")) {
			commandWebTarget = webTarget.queryParam("cmd", command);
		}

		Invocation.Builder invocationBuilder = commandWebTarget.request(MediaType.APPLICATION_JSON);

		Response response = invocationBuilder.post(Entity.json(jsonRequestString));
		T responseObj = response.readEntity(responseClass);

		long end = System.currentTimeMillis();
		logger.info("invoke() - end, command = '" + command + "', took " + (end - start) + " ms.");

		return responseObj;
	}

}
