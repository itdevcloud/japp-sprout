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

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ProcessorTargetRoleUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;

public abstract class RequestProcessor implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(RequestProcessor.class);


	protected abstract BaseResponse processRequest(BaseRequest request);
	
	protected String getLoginId() {
		return getIaaUser().getLoginId();
	}
	protected IaaUserI getIaaUser() {
		IaaUserI iaaUser = 	AppThreadContext.getIaaUser();
		if (iaaUser == null) {
			throw new RuntimeException("IaaUser is not set in the AppThreadContext, check code!");
		}
		return iaaUser;
	}
	
	public <T extends BaseResponse> String getTargetRole(Class<T> responseClass) {
		if(responseClass == null) {
			return null;
		}
		String command = AppUtil.getCorrespondingCommand(responseClass.getSimpleName());
		String processorName = command + AppUtil.PROCESSOR_POSTFIX;
		return ProcessorTargetRoleUtil.getTargetRole(processorName);
	}

	public <T extends BaseResponse> T process(BaseRequest request, Class<T> responseClass) {
		long startTS = System.currentTimeMillis();
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		T response = null;
		try {
			logger.info("RequestProcessor process begin,  LoginId = '" + getLoginId() + "', TransactionContext = " + txnCtx + "......");
			if (request == null) {
				response = AppUtil.createResponse(responseClass, "N/A",
						ResponseStatus.STATUS_CODE_ERROR_VALIDATION, " request parameter is null!");
				return response;
			}
			//check role
			if(!AppComponents.iaaService.isAccessAllowed(getTargetRole(responseClass))) {
				String simpleName = this.getClass().getSimpleName();
				T commandResponse = AppUtil
						.createResponse(responseClass, request.getCommand(), ResponseStatus.STATUS_CODE_ERROR_SECURITY_NOT_AUTHORIZED, getLoginId() + " not authorized: " + simpleName);
				response = commandResponse;
				return response;
		    }

			response = (T) processRequest(request);
			if(response == null) {
				String simpleName = this.getClass().getSimpleName();
				T commandResponse = AppUtil
						.createResponse(responseClass, request.getCommand(), ResponseStatus.STATUS_CODE_NA, simpleName + " returns null response object");
				response = commandResponse;
			}
			response.populateReuqstInfo(request);
			
			long endTS = System.currentTimeMillis();
			logger.info("RequestProcessor process end - total time = " + (endTS - startTS) + " millis. LoginId = '" + getLoginId() + "', TransactionContext = " + txnCtx + "......");

		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(t.getMessage(), t);
			T commandResponse = AppUtil
					.createResponse(responseClass, request.getCommand(), ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, t.getMessage());
			response = commandResponse;
			if(request != null) {
				response.populateReuqstInfo(request);
			}
		}
		return response;
	}

}
