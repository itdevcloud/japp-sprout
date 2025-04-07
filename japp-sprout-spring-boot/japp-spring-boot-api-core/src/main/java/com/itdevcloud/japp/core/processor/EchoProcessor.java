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
package com.itdevcloud.japp.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.EchoRequest;
import com.itdevcloud.japp.core.api.bean.EchoResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.iaa.web.Verify2ndFactorServlet;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class EchoProcessor extends RequestProcessor {

	//private static final Logger logger = LogManager.getLogger(EchoProcessor.class);
	private static final Logger logger = LogManager.getLogger(EchoProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		EchoRequest req = (EchoRequest) request;
		EchoResponse response = new EchoResponse();

		response.setMessage(req.getMessage());
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

}