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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.itdevcloud.japp.se.common.service.PropertyManager;
import com.itdevcloud.japp.se.common.util.CommonUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class ResponseStatusItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String itemCode;
	private String itemMessage;
	private Map<String, String> itemProperties;

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemMessage() {
		return itemMessage;
	}

	public void setItemMessage(String itemMessage) {
		this.itemMessage = itemMessage;
	}

	public String getItemProperty(String key) {
		if(key == null || (key = key.trim().toLowerCase()).equals("")) {
			return null;
		}
		if(this.itemProperties == null) {
			this.itemProperties = new HashMap<String, String>();
		}
		return itemProperties.get(key);
	}
	
	public Map<String, String> getItemProperties() {
		return itemProperties;
	}

	public void setItemProperties(Map<String, String> itemProperties) {
		this.itemProperties = itemProperties;
	}
	public void addItemPropertty(String key, String value) {
		if(key == null || (key = key.trim().toLowerCase()).equals("")) {
			return ;
		}
		if(this.itemProperties == null) {
			this.itemProperties = new HashMap<String, String>();
		}
		this.itemProperties.put(key, value);
	}

	@Override
	public String toString() {
		return "ResponseStatusItem [itemCode=" + itemCode + ", itemMessage=" + itemMessage + CommonUtil.mapToStringForPrint(itemProperties, 0) + "]";
	}

}
