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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.FindReferenceCodeRequest;
import com.itdevcloud.japp.core.api.bean.FindReferenceCodeResponse;
import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class FindReferenceCodeProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(FindReferenceCodeProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		FindReferenceCodeResponse response = new FindReferenceCodeResponse();
		List<ReferenceCode> codeList = new ArrayList<ReferenceCode>();

		if (request == null ) {
			codeList = AppComponents.referenceCodeService.getReferenceCodeList(null, null);
			response.setReferenceCodeList(codeList);
			response.setResponseStatus(
					AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));
			return response;
		}

		FindReferenceCodeRequest req = (FindReferenceCodeRequest) request;
		codeList = AppComponents.referenceCodeService.getReferenceCodeList(req.getCodeDomain(), req.getCodeType());
		response.setReferenceCodeList(codeList);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));
		return response;
	}

}