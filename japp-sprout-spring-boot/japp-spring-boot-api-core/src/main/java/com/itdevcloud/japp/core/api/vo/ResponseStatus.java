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

import com.itdevcloud.tools.common.service.PropertyManager;
import com.itdevcloud.tools.common.util.StringUtil;


public class ResponseStatus implements Serializable {

	public static final String STATUS_CODE_SUCCESS = "SUCCESS";
	public static final String STATUS_CODE_NA = "N/A";
	public static final String STATUS_CODE_ERROR_SYSTEM_ERROR = "E0001";
	public static final String STATUS_CODE_ERROR_MAINTENANCE_MODE = "E0002";
	public static final String STATUS_CODE_ERROR_VALIDATION = "E0100";
	public static final String STATUS_CODE_ERROR_INVALID_CODE = "E0101";
	public static final String STATUS_CODE_ERROR_SECURITY = "E0300";
	public static final String STATUS_CODE_ERROR_SECURITY_INVALID_TOKEN = "E0301";
	public static final String STATUS_CODE_ERROR_SECURITY_2FACTOR = "E0302";
	public static final String STATUS_CODE_ERROR_SECURITY_NO_VERIFICATION_CODE = "E0303";
	public static final String STATUS_CODE_ERROR_SECURITY_EXCEED_RETRY_COUNT = "E0304";
	public static final String STATUS_CODE_ERROR_SECURITY_VERIFICATION_TYPE_UNSUPPORTED = "E0305";
	public static final String STATUS_CODE_ERROR_SECURITY_NOT_AUTHORIZED = "E0306";
	
	public static final String STATUS_CODE_WARN_NOACTION = "W0001";

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

	private String cmdStatusCode;
	private String cmdStatusMessage;
	private List<ResponseStatusItem> commandStatusItems;


	public ResponseStatus(String cmdStatusCode, String cmdStatusMessage) {
		super();
		if(StringUtil.isEmptyOrNull(cmdStatusCode)) {
			cmdStatusCode = "CODE-IS-NULL";
		}
		this.cmdStatusCode = cmdStatusCode.trim().toUpperCase();
		String preDefinedMsg = preDefinedStatusMessageMap.get(this.cmdStatusCode);
		if(!StringUtil.isEmptyOrNull(preDefinedMsg)) {
			cmdStatusMessage = preDefinedMsg + "; Detail:" + cmdStatusMessage;
		}
		this.cmdStatusMessage = cmdStatusMessage;
	}

	public String getCmdStatusCode() {
		return cmdStatusCode;
	}

	public String getCmdStatusMessage() {
		return cmdStatusMessage;
	}


	public List<ResponseStatusItem> getCommandStatusItems() {
		return commandStatusItems;
	}

	public void setCommandStatusItems(List<ResponseStatusItem> commandStatusItems) {
		this.commandStatusItems = commandStatusItems;
	}

	public void addCommandStatusItem(ResponseStatusItem commandStatusItem) {
		if (commandStatusItem == null) {
			return;
		}
		if (commandStatusItems == null) {
			commandStatusItems = new ArrayList<ResponseStatusItem>();
		}
		commandStatusItems.add(commandStatusItem);
	}

}
