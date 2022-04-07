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
public class  ValidateTokenRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private String jwt;
	private String tokenNonce;
	private String tokenIssuer;
	private boolean issueNewtoken;


	public String getJwt() {
		return jwt;
	}
	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
	
	public String getTokenIssuer() {
		return tokenIssuer;
	}
	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}
	public String getTokenNonce() {
		return tokenNonce;
	}
	public void setTokenNonce(String tokenNonce) {
		this.tokenNonce = tokenNonce;
	}
	public boolean getIssueNewtoken() {
		return issueNewtoken;
	}
	public void setIssueNewtoken(boolean issueNewtoken) {
		this.issueNewtoken = issueNewtoken;
	}


}
