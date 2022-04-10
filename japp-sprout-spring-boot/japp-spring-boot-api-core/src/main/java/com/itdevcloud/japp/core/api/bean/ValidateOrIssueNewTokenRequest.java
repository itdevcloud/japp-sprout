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
* @author Marvin Sun
* @since 1.0.0
*/

import org.springframework.stereotype.Component;


@Component
public class  ValidateOrIssueNewTokenRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private String currentToken;
	private String currentTokenNonce;
	private String currentTokenIssuer;
	private String currentTokenUserIP;
	private String newTokenType;
	public String getCurrentToken() {
		return currentToken;
	}
	public void setCurrentToken(String currentToken) {
		this.currentToken = currentToken;
	}
	public String getCurrentTokenNonce() {
		return currentTokenNonce;
	}
	public void setCurrentTokenNonce(String currentTokenNonce) {
		this.currentTokenNonce = currentTokenNonce;
	}
	public String getCurrentTokenIssuer() {
		return currentTokenIssuer;
	}
	public void setCurrentTokenIssuer(String currentTokenIssuer) {
		this.currentTokenIssuer = currentTokenIssuer;
	}
	public String getCurrentTokenUserIP() {
		return currentTokenUserIP;
	}
	public void setCurrentTokenUserIP(String currentTokenUserIP) {
		this.currentTokenUserIP = currentTokenUserIP;
	}
	public String getNewTokenType() {
		return newTokenType;
	}
	public void setNewTokenType(String newTokenType) {
		this.newTokenType = newTokenType;
	}




}
