package com.sampleapiapp.api.vo;

public class ResponseHeader {

	private static final long serialVersionUID = 1L;

	private String clientTransactionId;
	private String ServerTransactionId;

	public void populateRequestHeaderInfo(RequestHeader reuqestHeader) {
		if(reuqestHeader == null) {
			return;
		}
		this.clientTransactionId = reuqestHeader.getClientTransactionId();
	}

	public String getClientTransactionId() {
		return clientTransactionId;
	}

	public void setClientTransactionId(String clientTransactionId) {
		this.clientTransactionId = clientTransactionId;
	}

	public String getServerTransactionId() {
		return ServerTransactionId;
	}

	public void setServerTransactionId(String serverTransactionId) {
		ServerTransactionId = serverTransactionId;
	}



}
