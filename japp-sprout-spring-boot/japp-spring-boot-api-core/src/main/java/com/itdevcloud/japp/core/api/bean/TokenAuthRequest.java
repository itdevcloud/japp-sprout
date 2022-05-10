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
package com.itdevcloud.japp.core.api.bean;
/**
  *
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import org.springframework.stereotype.Component;

import com.itdevcloud.japp.se.common.util.StringUtil;

@Component
public class TokenAuthRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;
	private String token;
	private String newTokenType;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getNewTokenType() {
		return newTokenType;
	}
	public void setNewTokenType(String newTokenType) {
		this.newTokenType = newTokenType;
	}
	@Override
	public String toString() {
		return "TokenAuthRequest [token=" + (StringUtil.isEmptyOrNull(token)?null:"*") + ", newTokenType = " + newTokenType + ", Super = " + super.toString() + "]";
	}



}
