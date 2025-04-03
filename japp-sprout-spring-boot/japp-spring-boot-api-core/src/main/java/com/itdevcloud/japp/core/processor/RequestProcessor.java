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
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;

public abstract class RequestProcessor implements AppFactoryComponentI {

	//private static final Logger logger = LogManager.getLogger(RequestProcessor.class);
	private static final Logger logger = LogManager.getLogger(RequestProcessor.class);


	public abstract BaseResponse processRequest(BaseRequest request);

	public BaseResponse process(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		BaseResponse response = null;
		try {
			logger.info("RequestProcessor before process request,  TransactionContext = " + txnCtx + "......");
			response = processRequest(request);
			if(response == null) {
				String simpleName = this.getClass().getSimpleName();
				BaseResponse commandResponse = AppUtil
						.createBaseResponse(request.getCommand(), ResponseStatus.STATUS_CODE_NA, simpleName + " returns null response object");
				response = commandResponse;
			}

		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(t.getMessage(), t);
			BaseResponse commandResponse = AppUtil
					.createBaseResponse(request.getCommand(), ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, t.getMessage());
			response = commandResponse;
		}
		return response;
	}

}
