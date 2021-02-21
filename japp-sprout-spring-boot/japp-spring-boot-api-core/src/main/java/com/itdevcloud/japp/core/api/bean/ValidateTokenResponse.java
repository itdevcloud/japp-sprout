package com.itdevcloud.japp.core.api.bean;


import org.springframework.stereotype.Component;


@Component
public class ValidateTokenResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;

	private boolean isValidJwt;
	private String jwt;

	public boolean isValidJwt() {
		return isValidJwt;
	}

	public void setValidJwt(boolean isValidJwt) {
		this.isValidJwt = isValidJwt;
	}


}
