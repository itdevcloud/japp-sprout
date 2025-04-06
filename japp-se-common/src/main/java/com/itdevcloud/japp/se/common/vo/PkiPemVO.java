package com.itdevcloud.japp.se.common.vo;

public class PkiPemVO {
 
	private String certificate ;
	private String publicKey ;
	private String algorithm;
	private String keyFormat;
	private String certificateType;
	
	public String getCertificate() {
		return certificate;
	}
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	
	public String getKeyFormat() {
		return keyFormat;
	}
	public void setKeyFormat(String keyFormat) {
		this.keyFormat = keyFormat;
	}
	public String getCertificateType() {
		return certificateType;
	}
	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}
	@Override
	public String toString() {
		return "PkiPemVO [certificate=" + (certificate==null?null:"***") + ", publicKey=" + (publicKey==null?null:"***") + ", algorithm=" + algorithm
				+ ", keyFormat=" + keyFormat + ", certificateType=" + certificateType + "]";
	}
	
	
}