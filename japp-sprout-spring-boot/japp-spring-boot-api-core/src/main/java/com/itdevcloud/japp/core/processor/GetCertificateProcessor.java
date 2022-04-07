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

import java.security.cert.Certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.GetCertificateRequest;
import com.itdevcloud.japp.core.api.bean.GetCertificateResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.core.common.AppUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class GetCertificateProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(GetCertificateProcessor.class);

	@Override
	public String getTargetRole() {
		return null;
	}
	@Override
	protected BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		GetCertificateRequest req = (GetCertificateRequest) request;
		GetCertificateResponse response = new GetCertificateResponse();
		Certificate certificate = AppComponents.pkiService.getAppCertificate();
		if(certificate == null) {
			return null;
		}
		response.setCertificate(SecurityUtil.getCertificatePemString(certificate, req.getInsertLineSeparator()));
		response.setType(certificate.getType());
		
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

}