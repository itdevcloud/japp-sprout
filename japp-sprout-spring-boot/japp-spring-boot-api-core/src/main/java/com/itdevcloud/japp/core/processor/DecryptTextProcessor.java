package com.itdevcloud.japp.core.processor;

import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.bean.DecryptTextRequest;
import com.itdevcloud.japp.core.api.bean.DecryptTextResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.security.AsymmetricCrypter;
import com.itdevcloud.japp.se.common.security.EncryptedInfo;
import com.itdevcloud.japp.se.common.util.SecurityUtil;


@Component
public class DecryptTextProcessor extends RequestProcessor {

	private static final Logger logger = LogManager.getLogger(DecryptTextProcessor.class);

	@Override
	public BaseResponse processRequest(BaseRequest req) {
		TransactionContext txnCtx = AppThreadContext.getTransactionContext();
		logger.debug(this.getClass().getSimpleName() + " begin to process request...<txId = "
				+ txnCtx.getTransactionId() + ">...... ");

		DecryptTextRequest request = (DecryptTextRequest) req;
		DecryptTextResponse response = new DecryptTextResponse();
		String decryptedText = null;
		EncryptedInfo encryptedInfo = request.getEncryptedInfo();
		if(encryptedInfo == null) {
			response = AppUtil
					.createResponse(DecryptTextResponse.class, "N/A", ResponseStatus.STATUS_CODE_ERROR_VALIDATION, " encryptedInfo is not provided");
			return response;
		}
		
		if(AsymmetricCrypter.CIPHER_DEFAULT_ALGORITHM.equalsIgnoreCase(encryptedInfo.getAlgorithm())) {
			PublicKey publicKey = AppComponents.pkiKeyCache.getAppPublicKey();
			PrivateKey privateKey = AppComponents.pkiService.getAppPrivateKey();
			if(publicKey == null && privateKey == null) {
				response = AppUtil.createResponse(DecryptTextResponse.class, "N/A",
						ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR, "publickey and privatekey can't be both null, check code or configuration!");
				return response;
			}	
			decryptedText = SecurityUtil.decryptAsym(encryptedInfo.getEncryptedText(), privateKey, publicKey);
		}else {
			decryptedText = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		}
		response.setDecryptedText(decryptedText);
		response.setResponseStatus(
				AppUtil.createResponseStatus(ResponseStatus.STATUS_CODE_SUCCESS, "Successfuly Processed"));

		logger.debug(this.getClass().getSimpleName() + " end to process request...<txId = " + txnCtx.getTransactionId()
		+ ">...... ");

		return response;
	}

	

}