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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class HttpResponse {
	
	private Map<String, List<String>> headerMap;
	private String resposebody;
	
	public Map<String, List<String>> getHeaderMap() {
		if(headerMap == null) {
			headerMap = new HashMap<String, List<String>>();
		}
		return headerMap;
	}
	public void setHeaderMap(Map<String, List<String>> headerMap) {
		this.headerMap = headerMap;
	}
	public String getResposebody() {
		if(resposebody == null) {
			resposebody = "No Response Body";
		}
		return resposebody;
	}
	public void setResposebody(String resposebody) {
		this.resposebody = resposebody;
	}
	
	public String printHeadersString() {
		if(headerMap == null) {
			return "Response Header Map is null";
		}
		String str = "Response Header:";
		for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
			str = str + entry.getKey() + " = " + entry.getValue();
		}
		return str;

	}

}
