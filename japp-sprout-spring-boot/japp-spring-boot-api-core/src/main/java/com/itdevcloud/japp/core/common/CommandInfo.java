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

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.processor.RequestProcessor;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class CommandInfo {
	
	private String command;
	private BaseRequest request;
	private BaseResponse response;
	private RequestProcessor processor;

	public Class<?> getRequestClass(){
		return request==null?null:request.getClass();
	}
	public Class<?> getResponseClass(){
		return response==null?null:response.getClass();
	}
	public Class<?> getProcessorClass(){
		return processor==null?null:processor.getClass();
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public BaseRequest getRequest() {
		return request;
	}
	public void setRequest(BaseRequest request) {
		this.request = request;
	}
	public BaseResponse getResponse() {
		return response;
	}
	public void setResponse(BaseResponse response) {
		this.response = response;
	}
	public RequestProcessor getProcessor() {
		return processor;
	}
	public void setProcessor(RequestProcessor processor) {
		this.processor = processor;
	}
	

}
