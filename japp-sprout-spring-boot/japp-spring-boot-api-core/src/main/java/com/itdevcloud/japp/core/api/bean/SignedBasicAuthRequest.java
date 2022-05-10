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
public class SignedBasicAuthRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private String loginId;
	private String password;
	private String clientPkiKey;
	private String signature;
	private String tokenType;
	
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getClientPkiKey() {
		return clientPkiKey;
	}
	public void setClientPkiKey(String clientPkiKey) {
		this.clientPkiKey = clientPkiKey;
	}
	
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	@Override
	public String toString() {
		return "SignedBasicAuthRequest [loginId=" + loginId + ", password=" + (StringUtil.isEmptyOrNull(password)?null:"*") + ", clientPkiKey="
				+ clientPkiKey + ", signature=" + signature + ", tokenType=" + tokenType + ", Super =" + super.toString() + "] ";
	}
	





}
