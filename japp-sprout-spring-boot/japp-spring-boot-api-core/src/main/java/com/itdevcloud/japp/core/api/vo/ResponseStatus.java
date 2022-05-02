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
package com.itdevcloud.japp.core.api.vo;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.itdevcloud.japp.se.common.service.PropertyManager;
import com.itdevcloud.japp.se.common.util.StringUtil;


public class ResponseStatus implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static Map<String, String> preDefinedStatusMessageMap;
	private static final String STATUS_CODE_PREFIX = "status.code.";

	static {
		init();
	}
	private static void init() {
		preDefinedStatusMessageMap = new HashMap<String, String>();
		PropertyManager pm = new PropertyManager("/status-code.properties");
		Properties properties = pm.getProperties(STATUS_CODE_PREFIX);
		if(properties == null) {
			return;
		}
		Set <?> keySet = properties.keySet();
		for(Object key: keySet) {
			String code = (String)key;
			String msg = properties.getProperty(code);
			code = code.trim().toUpperCase();
			preDefinedStatusMessageMap.put(code.toUpperCase(), msg);
		}
	}

	public static enum Status {
		SUCCESS(),
	    NA(),
	    ERROR_SYSTEM_ERROR(),
	    ERROR_VALIDATION(),
	    ERROR_SECURITY(),
	    ERROR_SECURITY_INVALID_TOKEN(),
	    ERROR_SECURITY_AUTHENTICATION(),
	    ERROR_SECURITY_AUTHORIZATION(),
	    WARN_NO_ACTION()
	    ;

//		SUCCESS("SUCCESS", "Success"),
//	    NA("NA", "N/A"),
//	    ERROR_SYSTEM_ERROR("E0001", "System Error"),
//	    ERROR_VALIDATION("E0100", "General Validation Error"),
//	    ERROR_SECURITY("E0300", "General Security Error"),
//	    ERROR_SECURITY_INVALID_TOKEN("E0301", "Validation Error"),
//	    ERROR_SECURITY_AUTHENTICATION("E0302", "Authentication Error"),
//	    ERROR_SECURITY_AUTHORIZATION("E0303", "Authorization Error"),
//	    WARN_NO_ACTION("W1000", "Warning: No Action")
//	    ;

	    public final String code;
	    public final String message;
	    
	    Status() {
	    	String tmpStr = preDefinedStatusMessageMap.get(this.name());
	    	if(StringUtil.isEmptyOrNull(tmpStr)) {
	    		throw new RuntimeException("status-code property not defined properly, no value found for: " + this.name());
	    	}
	    	String[] strArr = tmpStr.split(",");
	    	if(StringUtil.isEmptyOrNull(strArr[0])) {
	    		throw new RuntimeException("status-code property not defined properly, no error code found for: " + this.name());
	    	}
	    	
	    	this.code = strArr[0].trim().toUpperCase();
	    	if(strArr.length<2 || StringUtil.isEmptyOrNull(strArr[1])) {
	    		this.message = "n/a";
	    	}else {
	    		this.message = strArr[1].trim();
	    	}
	    }
//	    Status(final String code, final String message) {
//	        this.code = StringUtil.isEmptyOrNull(code)?"NA":code.trim().toUpperCase();
//	        this.message = StringUtil.isEmptyOrNull(code)?"N/A":code.trim();
//	    }
	    public String toString() {
	        return code + "-" + message;
	    }
	}

	
    private Status status;
	private String customizedMessage;
	private List<ResponseStatusItem> responseStatusItems;


	public ResponseStatus(Status status, String customizedMessage) {
		super();
		this.status = status==null?Status.NA:status;
		if(StringUtil.isEmptyOrNull(customizedMessage)) {
			customizedMessage = "customizaed message: n/a";
		}else {
			this.customizedMessage = customizedMessage;
		}
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getCustomizedMessage() {
		return customizedMessage;
	}

	public void setCustomizedMessage(String customizedMessage) {
		this.customizedMessage = customizedMessage;
	}


	public List<ResponseStatusItem> getResponseStatusItems() {
		return responseStatusItems;
	}

	public void setResponseStatusItems(List<ResponseStatusItem> responseStatusItems) {
		this.responseStatusItems = responseStatusItems;
	}

	public void addResponseStatusItem(ResponseStatusItem responseStatusItem) {
		if (responseStatusItem == null) {
			return;
		}
		if (responseStatusItems == null) {
			responseStatusItems = new ArrayList<ResponseStatusItem>();
		}
		responseStatusItems.add(responseStatusItem);
	}

	@Override
	public String toString() {
		return "ResponseStatus [status=" + status + ", customizedMessage=" + customizedMessage
				+ ", responseStatusItems=" + responseStatusItems + "]";
	}

	public static void main(String[] args) {
		ResponseStatus responseStatus = new ResponseStatus(null,null);
		responseStatus = new ResponseStatus(ResponseStatus.Status.ERROR_SECURITY_INVALID_TOKEN,"test.....");
		System.out.println("ResponseStatus = " + responseStatus);
		
	}
}
