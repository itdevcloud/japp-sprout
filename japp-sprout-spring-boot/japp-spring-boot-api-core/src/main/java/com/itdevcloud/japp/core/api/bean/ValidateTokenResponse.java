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
public class ValidateTokenResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;

	private boolean isValidToken;
	private String newToken;


	public boolean getIsValidToken() {
		return isValidToken;
	}

	public void setIsValidToken(boolean isValidToken) {
		this.isValidToken = isValidToken;
	}

	public String getNewToken() {
		return newToken;
	}

	public void setNewToken(String newToken) {
		this.newToken = newToken;
	}


}