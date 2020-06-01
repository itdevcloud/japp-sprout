package com.itdevcloud.japp.core.api.rest;
/**
 * provides pre-defined, centralized process command function.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.processor.RequestProcessor;


@Component
public abstract class BaseRestController {


	private static final Logger logger = LogManager.getLogger(BaseRestController.class);


	public Object processRequest(BaseRequest request) {
		logger.debug("processRequest() - start ===>");
		if (request == null) {
			BaseResponse response = AppUtil.createBaseResponse(
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
					"processRequest() - request parameter is null");
			return response;
		}
		String requestSimpleName = request.getClass().getSimpleName();

		RequestProcessor requestProcessor = AppFactory.getRequestProcessor(requestSimpleName);
		if (requestProcessor == null) {
			BaseResponse response = AppUtil.createBaseResponse(
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
					"processRequest() - processor not found for request: '" + requestSimpleName
					+ "'....");
			return response;
		}

		Object response = requestProcessor.process(request);

		logger.debug("processRequest() - end <=== request = '" + requestSimpleName + "'");
		return response;
	}

}