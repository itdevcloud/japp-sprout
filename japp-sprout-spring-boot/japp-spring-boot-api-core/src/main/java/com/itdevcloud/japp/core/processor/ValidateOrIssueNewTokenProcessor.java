package com.itdevcloud.japp.core.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.ValidateOrIssueNewTokenRequest;
import com.itdevcloud.japp.core.api.bean.ValidateOrIssueNewTokenResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public class ValidateOrIssueNewTokenProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(ValidateOrIssueNewTokenProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest request) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		ValidateOrIssueNewTokenRequest req = (ValidateOrIssueNewTokenRequest) request;
		ValidateOrIssueNewTokenResponse response = new ValidateOrIssueNewTokenResponse();
		
		
		// ====== business logic starts ======
		String currentToken = req.getCurrentToken();
		String currentTokenNonce = req.getCurrentTokenNonce();
		//validate Toke will be called by system client only, and validate individual token, 
		//so it has to provides token user's IP, the IP in http header is the caller ip, not token user's ip
		//the ip should come from request object, not txnCtx
		String currentTokenUserIp = req.getCurrentTokenUserIP();
		String newTokenType = req.getNewTokenType();
		String currentTokenIssuer = req.getCurrentTokenIssuer();
		
		Map<String, String> expectedClaims = new HashMap<String, String>();
		if (!StringUtil.isEmptyOrNull(currentTokenUserIp)) {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, Hasher.hashPassword(currentTokenUserIp));
		}else {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, null);
		}
		if (!StringUtil.isEmptyOrNull(currentTokenNonce)) {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, Hasher.hashPassword(currentTokenNonce));
		}else {
			expectedClaims.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, null);
		}

		String[] args = null;
		
		boolean isValid = AppComponents.jwtService.isValidToken(currentToken, expectedClaims, true, args);
		
		if(isValid) {
			response.setIsValidToken(true);
		}else {
			response.setIsValidToken(false);
		}
		String newToken = null;
		if(TokenHandlerI.TYPE_ACCESS_TOKEN.equalsIgnoreCase(newTokenType) || 
				TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(newTokenType) ||
				TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(newTokenType)) {
			//issue new token
			IaaUserI iaaUser = AppComponents.jwtService.getIaaUser(currentToken);
					newToken = AppComponents.jwtService.issueToken(iaaUser, newTokenType, null);
		}
		response.setNewToken(newToken);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Command Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

	

}