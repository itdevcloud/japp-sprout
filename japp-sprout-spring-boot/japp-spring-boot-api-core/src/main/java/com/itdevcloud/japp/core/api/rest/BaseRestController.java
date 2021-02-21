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


	public <O extends BaseResponse, I extends BaseRequest> O processRequest(I request, Class<O> responseClass) {
		
		logger.debug("processRequest() - start ===>");
		if (request == null) {
			O response = AppUtil.createResponse(responseClass, "N/A",
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION, " request parameter is null!");
			return response;
		}
		String requestSimpleName = request.getClass().getSimpleName();

		RequestProcessor requestProcessor = AppFactory.getRequestProcessor(requestSimpleName);
		if (requestProcessor == null) {
			O response = AppUtil.createResponse(responseClass, "N/A",
					ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "processor not found for request: '" + requestSimpleName
						+ "'....");
			return response;
		}

		O response = requestProcessor.process(request, responseClass);

		logger.debug("processRequest() - end <=== request = '" + requestSimpleName + "'");
		return response;
	}

}