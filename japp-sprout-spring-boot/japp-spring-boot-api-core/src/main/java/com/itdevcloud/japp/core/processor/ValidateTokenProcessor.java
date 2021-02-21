package com.itdevcloud.japp.core.processor;

import java.security.PublicKey;
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

	private static final long serialVersionUID = 1L;


	private static final Logger logger = LogManager.getLogger(ValidateTokenProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		ValidateTokenRequest req = (ValidateTokenRequest) request;
		ValidateTokenResponse response = new ValidateTokenResponse();
		
		
		// ====== business logic starts ======
		PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
		if(publicKey == null) {
			response = AppUtil.createResponse(ValidateTokenResponse.class, "N/A",
					ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "Can't get publickey for token validation, check code or configuration!");
			return response;
		}
		String jwt = req.getJwt();
		boolean isValid = AppComponents.jwtService.isValidTokenByPublicKey(jwt, publicKey);
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