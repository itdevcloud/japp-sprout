package com.sampleapiapp.validator;

import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppUtil;
import com.sampleapiapp.api.bean.LoginRequest;

@Component
public class LoginValidator {


	public ResponseStatus validate(BaseRequest appRrequest){
		ResponseStatus responseStatus = null;
		if(appRrequest == null){
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "request object is null!");
			return responseStatus;
		}
		if(!(appRrequest instanceof LoginRequest)) {
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "request is not a LoginRequest!");
			return responseStatus;
		}
		LoginRequest  request = (LoginRequest) appRrequest;
		
		//=====Add Business Logic ======
		
		responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, " Validated Successfully.");
		return responseStatus;

	}

}
