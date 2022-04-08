package com.itdevcloud.japp.core.processor;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.BasicAuthRequest;
import com.itdevcloud.japp.core.api.bean.BasicAuthResponse;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;

@Component
public class BasicAuthProcessor extends RequestProcessor {


	@PostConstruct
	private void init() {
	}

	private static final Logger logger = LogManager.getLogger(BasicAuthProcessor.class);
	
	@Override
	public String getTargetRole() {
		return null;
	}

	@Override
	protected BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		BasicAuthRequest request = (BasicAuthRequest) req;
		BasicAuthResponse response = new BasicAuthResponse();
		ResponseStatus responseStatus = null;
		
		// ====== validate request ======
		if (StringUtil.isEmptyOrNull(request.getLoginId()) || StringUtil.isEmptyOrNull(request.getPassword())) {
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Authentication Failed. LoginId and/or password is null.");
			response.setResponseStatus(responseStatus);
			return response;
		}


		// ====== business logic starts ======
		String loginId = request.getLoginId();
		String password = request.getPassword();
		String tokenNonce = request.getTokenNonce();
		String uip = txnCtx.getClientIP();
		IaaUserI iaaUser = null;
		try {
			iaaUser = AppComponents.iaaService.login(loginId, password, null);
			if (iaaUser == null) {
				logger.error(
						"Authentication Failed. Can not retrive user by loginId '" + loginId + "' and/or password.....");
				responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SECURITY, "Authentication Failed. Can not retrive user by loginId '" + loginId + "' and/or password.....");
				response.setResponseStatus(responseStatus);
				return response;
			}
		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, e.getMessage());
			response.setResponseStatus(responseStatus);
			return response;
		}

		// issue new JWT token;
		String hashedNonce = StringUtil.isEmptyOrNull(tokenNonce)?null:Hasher.hashPassword(tokenNonce);
		String hashedUip = StringUtil.isEmptyOrNull(uip)?null:Hasher.hashPassword(uip);
		
		iaaUser.setHashedNonce(hashedNonce);
		iaaUser.setHashedUserIp(hashedUip);
		
		String token = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN);
		
		if (StringUtil.isEmptyOrNull(token)) {
			logger.error("JWT Token can not be created for login Id '" + loginId);
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "JWT Token can not be created for login Id '" + iaaUser.getLoginId() + "', username = "
					+ loginId);
			response.setResponseStatus(responseStatus);
			return response;
		}
		responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Login Process Success.");

		response.setResponseStatus(responseStatus);
		response.setJwt(token);

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");
		return response;
	}




}