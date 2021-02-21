package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;


@Component
public class  ValidateTokenRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private String jwt;
	private String tokenIssuer;
	private boolean issueNewtoken;


	public String getJwt() {
		return jwt;
	}
	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
	
	public String getTokenIssuer() {
		return tokenIssuer;
	}
	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}


}
