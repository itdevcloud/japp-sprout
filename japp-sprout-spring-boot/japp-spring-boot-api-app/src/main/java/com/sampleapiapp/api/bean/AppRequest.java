package com.sampleapiapp.api.bean;

/**
 * Generated request class.
 *
 *
 * @since 1.0.0
 */

import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.sampleapiapp.api.vo.RequestHeader;


@Component
public class AppRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private RequestHeader header;

	public RequestHeader getHeader() {
		return header;
	}
	public void setHeader(RequestHeader header) {
		this.header = header;
	}

}
