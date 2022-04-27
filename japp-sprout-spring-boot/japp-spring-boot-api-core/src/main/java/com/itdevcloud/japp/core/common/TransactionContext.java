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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class TransactionContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private String transactionId;
	private Timestamp requestReceivedTimeStamp = null;
	private String serverTimezoneId = null;
	private String clientTimezoneId = null;
	
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Timestamp getRequestReceivedTimeStamp() {
		return requestReceivedTimeStamp;
	}

	public void setRequestReceivedTimeStamp(Timestamp requestTimeStamp) {
		this.requestReceivedTimeStamp = requestTimeStamp;
	}

	public String getServerTimezoneId() {
		return serverTimezoneId;
	}

	public void setServerTimezoneId(String serverTimezone) {
		this.serverTimezoneId = serverTimezone;
	}

	public String getClientTimezoneId() {
		return clientTimezoneId;
	}

	public void setClientTimezoneId(String clientTimezone) {
		this.clientTimezoneId = clientTimezone;
	}

	@Override
	public String toString() {
		return "TransactionContext [transactionId=" + transactionId + ", requestReceivedTimeStamp="
				+ requestReceivedTimeStamp + ", serverTimezoneId=" + serverTimezoneId + ", clientTimezoneId="
				+ clientTimezoneId + "]";
	}


}
