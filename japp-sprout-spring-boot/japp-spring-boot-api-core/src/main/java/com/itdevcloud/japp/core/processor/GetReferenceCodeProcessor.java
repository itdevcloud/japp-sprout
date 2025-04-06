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
import com.itdevcloud.japp.core.api.bean.GetReferenceCodeRequest;
import com.itdevcloud.japp.core.api.bean.GetReferenceCodeResponse;
import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.common.AppUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class GetReferenceCodeProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(GetReferenceCodeProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		GetReferenceCodeResponse response = new GetReferenceCodeResponse();
		ReferenceCode code = null;

		if (request == null) {
			response.setReferenceCode(null);
			response.setResponseStatus(
					AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Request is null"));
			return response;
		}

		GetReferenceCodeRequest req = (GetReferenceCodeRequest) request;
		if (!StringUtil.isEmptyOrNull(req.getCodeDomain()) && !StringUtil.isEmptyOrNull(req.getCodeType())
				&& !StringUtil.isEmptyOrNull(req.getCodeName())) {
			code = AppComponents.referenceCodeService.getReferenceCode(req.getCodeDomain(), req.getCodeType(),
					req.getCodeName());
		} else {
			code = AppComponents.referenceCodeService.getReferenceCode(req.getId());
		}
		response.setReferenceCode(code);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));
		return response;
	}

}