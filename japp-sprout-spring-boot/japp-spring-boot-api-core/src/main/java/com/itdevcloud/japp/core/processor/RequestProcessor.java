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
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ProcessorTargetRoleUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.util.StringUtil;

public abstract class RequestProcessor implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(RequestProcessor.class);


	protected abstract BaseResponse processRequest(BaseRequest request);
	
	protected String getLoginId() {
		return getIaaUser().getLoginId();
	}
	protected IaaUserI getIaaUser() {
		IaaUserI iaaUser = 	AppThreadContext.getIaaUser();
		if (iaaUser == null) {
			String errmsg = "getIaaUser() - IaaUser is not set in the AppThreadContext.";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errmsg);
		}
		return iaaUser;
	}
	
	public String getTargetRole(String classSimpleName) {
		if(StringUtil.isEmptyOrNull(classSimpleName) ) {
			return null;
		}
		String command = AppUtil.getCorrespondingCommand(classSimpleName);
		if(StringUtil.isEmptyOrNull(command)) {
			String errMsg = "getTargetRole() - can not get command from classSimpleName = " + classSimpleName;
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		String processorName = command + AppUtil.PROCESSOR_POSTFIX;
		return ProcessorTargetRoleUtil.getTargetRole(processorName);
	}

	public <T extends BaseResponse> T process(BaseRequest request, Class<T> responseClass) {
		long startTS = System.currentTimeMillis();
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		T response = null;
		try {
			logger.info("process() - begin,  LoginId = '" + getLoginId() + "', TransactionContext = " + txnCtx + "......");
			if (request == null) {
				response = AppUtil.createResponse(responseClass, "N/A",
						Status.ERROR_SYSTEM_ERROR, " request parameter can not be null!");
				return response;
			}
			//check command is enabled or not
			String classSimpleName = request.getClass().getSimpleName();
			if(!AppComponents.commonService.isCommandEnabled(classSimpleName)) {
				String command = AppUtil.getCorrespondingCommand(classSimpleName);
				String errMsg = "process() - Command [" + command  + "] is not enabled or supported.";
				T commandResponse = AppUtil
						.createResponse(responseClass, command, Status.ERROR_SYSTEM_CMD_NOT_SUPPORTED, errMsg);
				response = commandResponse;
				logger.error(errMsg);
				return response;
			}
			//check role
			if(!AppComponents.iaaService.isAccessAllowed(getTargetRole(classSimpleName))) {
				String command = AppUtil.getCorrespondingCommand(classSimpleName);
				String errMsg = "process() - User [" + getLoginId() + "] is not authorized to invoke the function: " + command;
				T commandResponse = AppUtil
						.createResponse(responseClass, request.getCommand(), Status.ERROR_SECURITY_AUTHORIZATION, errMsg);
				response = commandResponse;
				logger.error(errMsg);
				return response;
		    }
			
			response = (T) processRequest(request);
			if(response == null) {
				String command = AppUtil.getCorrespondingCommand(classSimpleName);
				String errMsg = "process() - User [" + getLoginId() + "]. Procossor returns null reponse for the command: " + command;
				T commandResponse = AppUtil
						.createResponse(responseClass, request.getCommand(), Status.ERROR_SYSTEM_ERROR, errMsg);
				response = commandResponse;
				logger.error(errMsg);
			}
			response.populateReuqstInfo(request);
			
			long endTS = System.currentTimeMillis();
			logger.info("process() - end - total time = " + (endTS - startTS) + " millis. LoginId = " + getLoginId() + "......");

		} catch (AppException ae) {
			ae.printStackTrace();
			logger.error("process() - ERROR: " + ae.getMessage());
			T commandResponse = AppUtil
					.createResponse(responseClass, request.getCommand(), ae.getStatus(), ae.getMessage());
			response = commandResponse;
			if(request != null) {
				response.populateReuqstInfo(request);
			}
		}  catch (Throwable t) {
			t.printStackTrace();
			logger.error("process() - ERROR: " + t.getMessage(), t);
			T commandResponse = AppUtil
					.createResponse(responseClass, request.getCommand(), Status.ERROR_SYSTEM_ERROR, t.getMessage());
			response = commandResponse;
			if(request != null) {
				response.populateReuqstInfo(request);
			}
		}
		return response;
	}

}
