package com.itdevcloud.japp.se.common.vo;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;

public class PkiVO {

	private Key privateKey;
	private PublicKey publicKey;
	private Certificate certificate;
	
	public Key getPrivateKey() {
		return this.privateKey;
	}
	public void setPrivateKey(Key privateKey) {
		this.privateKey = privateKey;
	}
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	public Certificate getCertificate() {
		return this.certificate;
	}
	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}
	
	@Override
	public String toString() {
		return "PkiVO [ certificate=" + (certificate==null?null:"***") + ", publicKey=" + (publicKey==null?null:"***") + 
				", privateKey=" + (privateKey==null?null:"***")  + "] ";
	}

	
}
