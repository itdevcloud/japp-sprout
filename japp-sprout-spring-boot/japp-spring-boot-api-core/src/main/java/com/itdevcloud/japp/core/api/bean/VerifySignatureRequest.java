package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;

@Component
public class  VerifySignatureRequest extends BaseRequest {
	private static final long serialVersionUID = 1L;
	
	private String text;
	private String signature;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	

}