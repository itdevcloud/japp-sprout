package com.itdevcloud.japp.core.processor;

import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.EncryptTextRequest;
import com.itdevcloud.japp.core.api.bean.EncryptTextResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.security.AsymmetricCrypter;
import com.itdevcloud.japp.se.common.security.EncryptedInfo;
import com.itdevcloud.japp.se.common.util.SecurityUtil;


@Component
public class EncryptTextProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(EncryptTextProcessor.class);

	@Override
	public String getTargetRole() {
		return null;
	}

	@Override
	public BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		EncryptTextRequest request = (EncryptTextRequest) req;
		EncryptTextResponse response = new EncryptTextResponse();
		String encryptedText = null;
		EncryptedInfo encryptedInfo = null;
		
		if(request.isSymmetric()) {
			logger.debug("Symmetric Encryption...... ");
			encryptedInfo = SecurityUtil.encrypt(request.getClearText(), null);
		}else {
			logger.debug("Asymmetric Encryption...... ");
			PublicKey publicKey = AppComponents.pkiService.getAppPublicKey();
			PrivateKey privateKey = AppComponents.pkiService.getAppPrivateKey();
			if(publicKey == null && privateKey == null) {
				response = AppUtil.createResponse(EncryptTextResponse.class, "N/A",
						ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "publickey and privatekey can't be both null, check code or configuration!");
				return response;
			}	
			//encrypt by public key
			encryptedText = SecurityUtil.encryptAsym(request.getClearText(), null, publicKey);
			
			encryptedInfo = new EncryptedInfo();
			encryptedInfo.setAlgorithm(AsymmetricCrypter.CIPHER_DEFAULT_ALGORITHM);
			encryptedInfo.setEncryptedText(encryptedText);
		}
		response.setEncryptedInfo(encryptedInfo);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Successfuly Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

	

}