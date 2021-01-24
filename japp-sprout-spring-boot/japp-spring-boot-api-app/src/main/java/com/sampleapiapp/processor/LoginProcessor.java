package com.sampleapiapp.processor;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.iaa.service.IaaService;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.JwtService;
import com.itdevcloud.japp.core.processor.RequestProcessor;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.sampleapiapp.api.bean.AppRequest;
import com.sampleapiapp.api.bean.LoginRequest;
import com.sampleapiapp.api.bean.LoginResponse;
import com.sampleapiapp.api.vo.ResponseHeader;
import com.sampleapiapp.validator.LoginValidator;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;

@Component
public class LoginProcessor extends RequestProcessor {

	@Autowired
	LoginValidator validator;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private IaaService iaaService;

	@PostConstruct
	private void init() {
	}

	private static final Logger logger = LogManager.getLogger(LoginProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest appRrequest) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		// =====create response object =====
		LoginResponse response = new LoginResponse();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setServerTransactionId(txnCtx.getTransactionId());
		
		// ====== validate request ======
		ResponseStatus responseStatus = validator.validate(appRrequest);
		if (responseStatus == null || !ResponseStatus.STATUS_CODE_SUCCESS.equalsIgnoreCase(responseStatus.getStatusCode())) {
			// validation failed
			if (appRrequest != null && appRrequest instanceof AppRequest) {
				responseHeader.populateRequestHeaderInfo(((AppRequest) appRrequest).getHeader());
			}
			response.setHeader(responseHeader);
			response.setResponseStatus(responseStatus);
			return response;
		}

		LoginRequest request = (LoginRequest) appRrequest;
		responseHeader.populateRequestHeaderInfo(request.getHeader());

		// ====== business logic starts ======
		String username = request.getUsername();
		String password = request.getPassword();
		IaaUser iaaUser = null;
		try {
			iaaUser = iaaService.loginByLoginIdPassword(username, password);
			if (iaaUser == null) {
				logger.error(
						"Authentication Failed. Can not retrive user by loginId '" + username + "' and/or password.....");
				responseStatus = new ResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SECURITY, "Authentication Failed. Can not retrive user by loginId '" + username + "' and/or password.....");
				response.setHeader(responseHeader);
				response.setResponseStatus(responseStatus);
				return response;
			}
		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			responseStatus = new ResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, e.getMessage());
			response.setHeader(responseHeader);
			response.setResponseStatus(responseStatus);
			return response;
		}

		// issue new JWT token;
		String token = jwtService.issueJappToken(iaaUser);
		if (StringUtil.isEmptyOrNull(token)) {
			logger.error("JWT Token can not be created for login Id '" + iaaUser.getCurrentLoginId() + "', username = "
					+ username);
			responseStatus = new ResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "JWT Token can not be created for login Id '" + iaaUser.getCurrentLoginId() + "', username = "
					+ username);
			response.setHeader(responseHeader);
			response.setResponseStatus(responseStatus);
			return response;
		}
		responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Login Process Success.");

		response.setHeader(responseHeader);
		response.setResponseStatus(responseStatus);
		response.setJwt(token);

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");
		return response;
	}

}