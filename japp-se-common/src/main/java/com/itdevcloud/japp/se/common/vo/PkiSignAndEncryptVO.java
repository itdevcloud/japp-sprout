package com.itdevcloud.japp.se.common.vo;

public class PkiSignAndEncryptVO {

	private String message;
	private String signature;
	private String encryptedMessage;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getEncryptedMessage() {
		return encryptedMessage;
	}
	public void setEncryptedMessage(String encryptedMessage) {
		this.encryptedMessage = encryptedMessage;
	}
	@Override
	public String toString() {
		
		return "PkiSignAndEncryptVO [message=" + (message==null?null:"***") + ", signature=" + (signature==null?null:"***") + ", encryptedMessage="
				+  (encryptedMessage==null?null:"***") + "]";
	}


}
