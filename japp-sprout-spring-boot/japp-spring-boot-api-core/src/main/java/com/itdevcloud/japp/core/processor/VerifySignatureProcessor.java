package com.itdevcloud.japp.core.processor;

import java.security.PublicKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.VerifySignatureRequest;
import com.itdevcloud.japp.core.api.bean.VerifySignatureResponse;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;

@Component
public class VerifySignatureProcessor extends RequestProcessor {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(VerifySignatureProcessor.class);

	@Override
	protected BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		String loginId = getLoginId();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...loginId = " + loginId + ", <txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		VerifySignatureRequest request = (VerifySignatureRequest) req;
		VerifySignatureResponse response = new VerifySignatureResponse();
		ResponseStatus responseStatus = null;

		// ====== business logic starts ======
		PublicKey publicKey = AppComponents.pkiService.getAppPublicKey();
		//PrivateKey privateKey = AppComponents.pkiService.getAppPrivateKey();

		String text = request.getText();
		String signature = request.getSignature();
		boolean isValid = SecurityUtil.verifySignature(publicKey, signature, text);
		response.setIsValid(isValid);

		responseStatus = AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "PKI Verify Signature Process Success.");

		response.setResponseStatus(responseStatus);

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
				+ ">...... ");
		return response;
	}
}