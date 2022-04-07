package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;

@Component
public class SignTextResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;
	private String signature;
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}


}
