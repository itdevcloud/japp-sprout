package com.sampleapiapp.api.bean;

/**
 * Generated request class.
 *
 *
 * @since 1.0.0
 */

import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.sampleapiapp.api.vo.ResponseHeader;


@Component
public class LoginResponse extends AppResponse {

	private static final long serialVersionUID = 1L;

	private String jwt ;


	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}


}
