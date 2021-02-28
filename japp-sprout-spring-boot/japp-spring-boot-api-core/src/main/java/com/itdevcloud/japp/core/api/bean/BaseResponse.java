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
 * Base Response Class.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
import java.io.Serializable;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class BaseResponse implements Serializable, AppFactoryComponentI {

	private static final long serialVersionUID = 1L;

	private ResponseStatus responseStatus = null;
	private String command;
	private String clientId;
	private String clientTxId;
	private String serverTxId;

	public void populateReuqstInfo(BaseRequest request) {
		if(request != null) {
			this.command = request.getCommand();
			this.clientId = request.getClientId();
			this.clientTxId = request.getClientTxId();
		}
	}
	
	public BaseResponse() {
		TransactionContext tcContext = AppThreadContext.getTransactionContext();
		serverTxId = (tcContext==null?null:tcContext.getTransactionId());
	}

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

	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getServerTxId() {
		if(StringUtil.isEmptyOrNull(serverTxId )) {
			TransactionContext tcContext = AppThreadContext.getTransactionContext();
			serverTxId = (tcContext==null?null:tcContext.getTransactionId());
		}
		return serverTxId;
	}

	public void setServerTxId(String serverTxId) {
		this.serverTxId = serverTxId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientTxId() {
		return clientTxId;
	}

	public void setClientTxId(String clientTxId) {
		this.clientTxId = clientTxId;
	}

}
