package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;

@Component
public class VerifySignatureResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;
	private boolean isValid;

	public boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public String toString() {
		return "VerifySignatureResponse [isValid=" + isValid + ", Super =" + super.toString() + "] ";
	}
	



}
