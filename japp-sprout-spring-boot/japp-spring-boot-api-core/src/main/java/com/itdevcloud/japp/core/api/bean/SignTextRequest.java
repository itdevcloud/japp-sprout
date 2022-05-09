package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;

@Component
public class  SignTextRequest extends BaseRequest {
	private static final long serialVersionUID = 1L;
	
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "SignTextRequest [text=" + text + ", Super =" + super.toString() + "] ";
	}
	

}