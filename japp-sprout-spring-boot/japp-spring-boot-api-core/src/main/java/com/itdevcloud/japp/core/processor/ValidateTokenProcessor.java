package com.itdevcloud.japp.core.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.ValidateTokenRequest;
import com.itdevcloud.japp.core.api.bean.ValidateTokenResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public class ValidateTokenProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(ValidateTokenProcessor.class);
	@Override
	public String getTargetRole() {
		return null;
	}

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		ValidateTokenRequest req = (ValidateTokenRequest) request;
		ValidateTokenResponse response = new ValidateTokenResponse();
		
		
		// ====== business logic starts ======
		String jwt = req.getJwt();
		String nonce = req.getTokenNonce();
		String userIp = txnCtx.getClientIP();
		
		Map<String, String> expectedClaims = new HashMap<String, String>();
		if (!StringUtil.isEmptyOrNull(userIp)) {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, Hasher.hashPassword(userIp));
		}else {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, null);
		}
		if (!StringUtil.isEmptyOrNull(nonce)) {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, Hasher.hashPassword(nonce));
		}else {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, null);
		}

		String[] args = null;
		
		boolean isValid = AppComponents.jwtService.isValidToken(jwt, expectedClaims, true, args);
		
		if(isValid) {
			response.setIsValidToken(true);
		}else {
			response.setIsValidToken(false);
		}

	
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

	

}