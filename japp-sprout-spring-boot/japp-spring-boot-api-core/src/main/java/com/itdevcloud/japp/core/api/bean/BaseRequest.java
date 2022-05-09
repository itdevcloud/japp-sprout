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
 * Base Request Class.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.io.Serializable;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class BaseRequest implements Serializable, AppFactoryComponentI {

	private static final long serialVersionUID = 1L;

	private String command = AppUtil.getCorrespondingCommand(this);
	private String clientAppId;
	private String clientTxId;
	private String tokenNonce;
	
	public String getCommand() {
		if(StringUtil.isEmptyOrNull(this.command )) {
			this.command = AppUtil.getCorrespondingCommand(this);
		}
		return command;
	}

	public void setCommand(String command) {
		if(StringUtil.isEmptyOrNull(command )) {
			command = AppUtil.getCorrespondingCommand(this);
		}
		this.command = command;
	}

	public String getClientAppId() {
		return clientAppId;
	}

	public void setClientAppId(String clientAppId) {
		this.clientAppId = clientAppId;
	}

	public String getClientTxId() {
		return clientTxId;
	}

	public void setClientTxId(String clientTxId) {
		this.clientTxId = clientTxId;
	}

	public String getTokenNonce() {
		return tokenNonce;
	}

	public void setTokenNonce(String tokenNonce) {
		this.tokenNonce = tokenNonce;
	}


	@Override
	public String toString() {
		return "BaseRequest [command=" + command + ", clientAppId=" + clientAppId + ", clientTxId=" + clientTxId
				+ ", tokenNonce=" + tokenNonce + "]";
	}



}
