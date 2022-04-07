package com.itdevcloud.japp.core.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthRequest;
import com.itdevcloud.japp.core.api.bean.SignedBasicAuthResponse;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.core.api.vo.AppSiteInfo;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;

@Component
public class SignedBasicAuthProcessor extends RequestProcessor {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(SignedBasicAuthProcessor.class);

	@Override
	public String getTargetRole() {
		return null;
	}

	@Override
	protected BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		// =====create response object =====
		SignedBasicAuthRequest request = (SignedBasicAuthRequest) req;
		SignedBasicAuthResponse response = new SignedBasicAuthResponse();
		ResponseStatus responseStatus = null;

		// ====== validate request ======
		if (StringUtil.isEmptyOrNull(request.getLoginId()) || StringUtil.isEmptyOrNull(request.getPassword())) {
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Authentication Failed. LoginId and/or password is null.");
			response.setResponseStatus(responseStatus);
			return response;
		}

		String loginId = null;
		try {
			// ====== business logic starts ======
			loginId = request.getLoginId();
			String password = request.getPassword();
			String appCode = request.getClientAppCode();
			String siteCode = request.getClientSiteCode();
			String tokenNonce = request.getTokenNonce();
			String signature = request.getSignature();
			
			String signedMessage = appCode+siteCode+loginId+password+tokenNonce;
			logger.info("signatureText=" + signedMessage + ", signature="+signature);
			ClientAppInfo clientAppInfo = AppComponents.iaaService.getClientAppInfo(appCode);
			AppSiteInfo siteInfo =  (clientAppInfo == null? null:clientAppInfo.getAppSiteInfo(siteCode));
			if (clientAppInfo == null || siteInfo == null) {
				String errMsg = "Application/Site was not found: appCode = '" + appCode + "', siteCode='" + siteCode +"'.....";
				logger.error(errMsg);
				responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, errMsg);
				response.setResponseStatus(responseStatus);
				return response;
			}
			logger.info("AppComponents.pkiService.getAppPublicKey()=" + AppComponents.pkiService.getAppPublicKey());
			logger.info("siteInfo.getPublicKey()=" + siteInfo.getPublicKey());
			if	(!SecurityUtil.verifySignature(siteInfo.getPublicKey(), signature, signedMessage)) {
				String errMsg = "Authentication Failed. Signature verification failed: loginId = '" + loginId + "'.....";
				logger.error(errMsg);
				responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, errMsg);
				response.setResponseStatus(responseStatus);
				return response;
			}
			
			// ====== login starts ======
			IaaUserI iaaUser = null;
				iaaUser = AppComponents.iaaService.login(loginId, password, null);
				if (iaaUser == null) {
					logger.error(
							"Authentication Failed. Can not retrive user by loginId '" + loginId + "' and/or password.....");
					responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SECURITY, "Authentication Failed. Can not retrive user by loginId '" + loginId + "' and/or password.....");
					response.setResponseStatus(responseStatus);
					return response;
				}

			// issue new JWT token;
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
		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, e.getMessage());
			response.setResponseStatus(responseStatus);
			return response;
		}
	}
}