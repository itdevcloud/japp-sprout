package com.itdevcloud.japp.core.processor;

import java.security.PublicKey;
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


@Component
public class ValidateTokenProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(ValidateTokenProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		ValidateTokenRequest req = (ValidateTokenRequest) request;
		ValidateTokenResponse response = new ValidateTokenResponse();
		
		
		// ====== business logic starts ======
//		PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
//		if(publicKey == null) {
//			response = AppUtil.createResponse(ValidateTokenResponse.class, "N/A",
//					ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "Can't get publickey for token validation, check code or configuration!");
//			return response;
//		}
		String jwt = req.getJwt();
		Map<String, String> expectedClaims = new HashMap<String, String>();
		String[] args = null;
		boolean isValid = AppComponents.jwtService.isValidToken(jwt, expectedClaims, args);
		if(isValid) {
			response.setValidJwt(true);
		}else {
			response.setValidJwt(false);
		}

	
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

	

}