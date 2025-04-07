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
import com.itdevcloud.japp.core.api.bean.GetChildrenReferenceCodeRequest;
import com.itdevcloud.japp.core.api.bean.GetChildrenReferenceCodeResponse;
import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class GetChildrenReferenceCodeProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(GetChildrenReferenceCodeProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		 GetChildrenReferenceCodeResponse response = new GetChildrenReferenceCodeResponse();
		List<ReferenceCode> codeList = new ArrayList<ReferenceCode>();

		if (request == null ) {
			response.setReferenceCodeList(null);
			response.setResponseStatus(
					AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Request is null"));
			return response;
		}

		 GetChildrenReferenceCodeRequest req = ( GetChildrenReferenceCodeRequest) request;

		if (!StringUtil.isEmptyOrNull(req.getCodeDomain()) && !StringUtil.isEmptyOrNull(req.getCodeType())
				&& !StringUtil.isEmptyOrNull(req.getCodeName())) {
			codeList = AppComponents.referenceCodeService.getChildren(req.getCodeDomain(), req.getCodeType(), req.getCodeName());
		} else {
			codeList = AppComponents.referenceCodeService.getChildren(req.getId());
		}
		response.setReferenceCodeList(codeList);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));
		return response;
	}

}