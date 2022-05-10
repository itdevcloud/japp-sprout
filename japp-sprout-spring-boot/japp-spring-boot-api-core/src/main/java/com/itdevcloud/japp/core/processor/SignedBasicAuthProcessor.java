package com.itdevcloud.japp.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthRequest;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthResponse;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ClientPKI;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;

@Component
public class SignedBasicAuthProcessor extends RequestProcessor {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(SignedBasicAuthProcessor.class);

	@Override
	protected BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();

		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");


		// =====create response object =====
		SignedBasicAuthRequest request = (SignedBasicAuthRequest) req;
		SignedBasicAuthResponse response = new SignedBasicAuthResponse();
		ResponseStatus responseStatus = null;

		// ====== validate request ======
		if (StringUtil.isEmptyOrNull(request.getLoginId()) || StringUtil.isEmptyOrNull(request.getPassword())) {
			responseStatus = AppUtil.createResponseStatus(Status.ERROR_VALIDATION,
					"Authentication Failed. LoginId and/or password is null.");
			response.setResponseStatus(responseStatus);
			return response;
		}

		String loginId = null;
		try {
			// ====== business logic starts ======
			loginId = request.getLoginId();
			String password = request.getPassword();
			String clientPkiKey = request.getClientPkiKey();
			String tokenType = request.getTokenType();

			String clientAppId = apiAuthInfo.clientAppId;
			String clientAuthKey = apiAuthInfo.clientAuthKey;
			String tokenNonce = apiAuthInfo.tokenNonce;
			String uip = apiAuthInfo.clientIP;
			String signature = request.getSignature();
			
			//handle token nonce and ip 
			AppUtil.checkTokenIpAndNonceRequirement(uip, tokenNonce);

			String signedMessage = clientAppId + loginId + password + tokenNonce;
			logger.info("signatureText=" + signedMessage + ", signature=" + signature);
			ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(clientAppId);
			ClientPKI clientPKI = clientAppInfo.getClientPKI(clientPkiKey);
			if(clientPKI == null) {
				String errMsg = "Authorization Failed. Error: clientAuthProvider is null, check code! Client App Id = " + clientAppId + ", clientPkiKey = " + clientPkiKey;
				responseStatus = AppUtil.createResponseStatus(Status.ERROR_SECURITY, errMsg);
				response.setResponseStatus(responseStatus);
				return response;
			}

			if (!SecurityUtil.verifySignature(clientPKI.getPublicKey(), signature, signedMessage)) {
				String errMsg = "Authentication Failed. Signature verification failed: loginId = '" + loginId
						+ "'.....";
				logger.error(errMsg);
				responseStatus = AppUtil.createResponseStatus(Status.ERROR_VALIDATION, errMsg);
				response.setResponseStatus(responseStatus);
				return response;
			}

			// ====== login starts ======
			IaaUserI iaaUser = null;
			iaaUser = AppComponents.iaaService.login(loginId, password, null);
			if (iaaUser == null) {
				logger.error("Authentication Failed. Can not retrive user by loginId '" + loginId
						+ "' and/or password.....");
				responseStatus = AppUtil.createResponseStatus(Status.ERROR_SECURITY_AUTHENTICATION,
						"Authentication Failed. Can not retrive user by loginId '" + loginId
								+ "' and/or password.....");
				response.setResponseStatus(responseStatus);
				return response;
			}

			// issue new JWT token;
			String hashedNonce = StringUtil.isEmptyOrNull(tokenNonce) ? null : Hasher.hashPassword(tokenNonce);
			String hashedUip = StringUtil.isEmptyOrNull(uip) ? null : Hasher.hashPassword(uip);
			
			iaaUser.setClientAppId(clientAppId);
			iaaUser.setClientAuthKey(null);

			iaaUser.setHashedNonce(hashedNonce);
			iaaUser.setHashedUserIp(hashedUip);
			
			if(!(TokenHandlerI.TYPE_ACCESS_TOKEN.equalsIgnoreCase(tokenType) || 
					TokenHandlerI.TYPE_ID_TOKEN.equalsIgnoreCase(tokenType) ||
					TokenHandlerI.TYPE_REFRESH_TOKEN.equalsIgnoreCase(tokenType))) {
				tokenType = TokenHandlerI.TYPE_ACCESS_TOKEN;
			}
			
			String token = AppComponents.jwtService.issueToken(iaaUser, tokenType, null);
			if (StringUtil.isEmptyOrNull(token)) {
				logger.error("JWT Token can not be created for login Id '" + loginId);
				responseStatus = AppUtil.createResponseStatus(Status.ERROR_SYSTEM_ERROR,
						"JWT Token can not be created for login Id '" + iaaUser.getLoginId() + "', username = "
								+ loginId);
				response.setResponseStatus(responseStatus);
				return response;
			}
			responseStatus = AppUtil.createResponseStatus(Status.SUCCESS, "Login Process Success.");

			response.setResponseStatus(responseStatus);
			response.setToken(token);

			logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = "
					+ txnCtx.getTransactionId() + ">...... ");
			return response;
		}catch (AppException ae) {
			throw ae;
		}catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
			responseStatus = AppUtil.createResponseStatus(Status.ERROR_SYSTEM_ERROR,
					t.getMessage());
			response.setResponseStatus(responseStatus);
			return response;
		}
	}
}