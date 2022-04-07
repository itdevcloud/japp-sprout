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

@Component
public class SignedBasicAuthRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private String loginId;
	private String password;
	private String tokenNonce;
	private String clientAppCode;
	private String clientSiteCode;
	private String signature;
	
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
	public String getTokenNonce() {
		return tokenNonce;
	}
	public void setTokenNonce(String tokenNonce) {
		this.tokenNonce = tokenNonce;
	}
	
	public String getClientAppCode() {
		return clientAppCode;
	}
	public void setClientAppCode(String clientAppCode) {
		this.clientAppCode = clientAppCode;
	}
	public String getClientSiteCode() {
		return clientSiteCode;
	}
	public void setClientSiteCode(String clientSiteCode) {
		this.clientSiteCode = clientSiteCode;
	}





}
