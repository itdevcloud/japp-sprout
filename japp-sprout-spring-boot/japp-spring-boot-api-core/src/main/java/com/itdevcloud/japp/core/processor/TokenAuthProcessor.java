package com.itdevcloud.japp.core.processor;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.TokenAuthRequest;
import com.itdevcloud.japp.core.api.bean.TokenAuthResponse;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;

@Component
public class TokenAuthProcessor extends RequestProcessor {


	@PostConstruct
	private void init() {
	}

	private static final Logger logger = LogManager.getLogger(TokenAuthProcessor.class);
	
	@Override
	protected BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();

		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		TokenAuthRequest request = (TokenAuthRequest) req;
		TokenAuthResponse response = new TokenAuthResponse();
		ResponseStatus responseStatus = null;
		
		// ====== validate request ======
		String token = request.getToken();
		String newTokenType = request.getNewTokenType();
		if (StringUtil.isEmptyOrNull(token)) {
			responseStatus = AppUtil.createResponseStatus(Status.ERROR_SECURITY_AUTHENTICATION, "token in the request is null or empty.");
			response.setResponseStatus(responseStatus);
			return response;
		}

		// ====== business logic starts ======
		String tokenNonce = apiAuthInfo.tokenNonce;
		String uip = apiAuthInfo.clientIP;
		String clientAppId = apiAuthInfo.clientAppId;
		String clientAuthKey = apiAuthInfo.clientAuthKey;

		//handle token nonce and ip 
		AppUtil.checkTokenIpAndNonceRequirement(uip, tokenNonce);

		
		IaaUserI iaaUser = AppComponents.iaaService.loginByToken(token, null, true);
		if (iaaUser == null) {
			String errMsg = "can not login by token";
			logger.error(errMsg);
			throw new AppException(Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
		}

		// issue new JWT token;
		String hashedNonce = StringUtil.isEmptyOrNull(tokenNonce)?null:Hasher.hashPassword(tokenNonce);
		String hashedUip = StringUtil.isEmptyOrNull(uip)?null:Hasher.hashPassword(uip);
		
		iaaUser.setHashedNonce(hashedNonce);
		iaaUser.setHashedUserIp(hashedUip);
		
		iaaUser.setClientAppId(clientAppId);
		iaaUser.setClientAuthKey(clientAuthKey);
		
		if(!(TokenHandlerI.TYPE_ACCESS_TOKEN.equalsIgnoreCase(newTokenType) || 
				TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(newTokenType) ||
				TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(newTokenType))) {
			newTokenType = TokenHandlerI.TYPE_ACCESS_TOKEN;
		}
		String newToken = AppComponents.jwtService.issueToken(iaaUser, newTokenType, null);
		
		if (StringUtil.isEmptyOrNull(newToken)) {
			String errMsg = newTokenType + "can not be created.";
			logger.error(errMsg);
			throw new AppException(Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
		}
		responseStatus = AppUtil.createResponseStatus(Status.SUCCESS, "Token Auth Process Success.");

		response.setResponseStatus(responseStatus);
		response.setToken(newToken);

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");
		return response;

	}


}