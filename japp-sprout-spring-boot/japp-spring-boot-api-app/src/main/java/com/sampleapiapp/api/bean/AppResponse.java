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
public class AppResponse extends BaseResponse {

	private static final long serialVersionUID = 1L;

	private ResponseHeader header;


	public ResponseHeader getHeader() {
		return header;
	}

	public void setHeader(ResponseHeader header) {
		this.header = header;
	}


}
