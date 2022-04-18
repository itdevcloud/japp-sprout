/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.core.api.vo;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientPKI implements Serializable,  Comparable<ClientPKI>{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String clientPkiKey;
	private String encodedPublicKey;
	private String publicKeyAlgorithm;
	private String encodedCertificate;
	private transient PublicKey publicKey;
	private transient Certificate certificate;
	private Date certificateExpiryDate;
	private Boolean isDefault;
	
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getClientPkiKey() {
		return clientPkiKey;
	}


	public void setClientPkiKey(String clientPkiKey) {
		this.clientPkiKey = clientPkiKey;
	}


	public String getEncodedPublicKey() {
		if(encodedPublicKey == null && publicKey != null) {
			encodedPublicKey = SecurityUtil.getPublicKeyString(publicKey);
		}
		return encodedPublicKey;
	}


	public void setEncodedPublicKey(String encodedPublicKey) {
		this.encodedPublicKey = encodedPublicKey;
	}


	public String getPublicKeyAlgorithm() {
		return publicKeyAlgorithm;
	}


	public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
		this.publicKeyAlgorithm = publicKeyAlgorithm;
	}


	public String getEncodedCertificate() {
		if(encodedCertificate == null && certificate != null) {
			encodedCertificate = SecurityUtil.getCertificatePemString(certificate, false);
		}
		return encodedCertificate;
	}


	public void setEncodedCertificate(String encodedCertificate) {
		this.encodedCertificate = encodedCertificate;
	}


	public PublicKey getPublicKey() {
		if(publicKey == null && !StringUtil.isEmptyOrNull(encodedPublicKey)) {
			publicKey = SecurityUtil.getPublicKeyFromString(encodedPublicKey, this.publicKeyAlgorithm);
		}
		return publicKey;
	}


	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}


	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}


	public Certificate getCertificate() {
		if(certificate == null && !StringUtil.isEmptyOrNull(encodedCertificate)) {
			certificate = SecurityUtil.getCertificateFromString(encodedCertificate);
		}
		return certificate;
	}



	public Date getCertificateExpiryDate() {
		return certificateExpiryDate;
	}


	public void setCertificateExpiryDate(Date certificateExpiryDate) {
		this.certificateExpiryDate = certificateExpiryDate;
	}


	public Boolean getIsDefault() {
		return isDefault;
	}


	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientPkiKey == null) ? 0 : clientPkiKey.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientPKI other = (ClientPKI) obj;
		if (clientPkiKey == null) {
			if (other.clientPkiKey != null)
				return false;
		} else if (!clientPkiKey.equals(other.clientPkiKey))
			return false;
		return true;
	}


	@Override
	public int compareTo(ClientPKI o) {
		if(o == null) {
			return 1;
		}
		ClientPKI e = (ClientPKI) o;
		if(getClientPkiKey() == null && e.getClientPkiKey() ==null) {
			return 0;
		}else if(getClientPkiKey() == null) {
			return -1;
		}else if(e.getClientPkiKey() ==null) {
			return 1;
		}else {
			return getClientPkiKey().compareTo(e.getClientPkiKey());
		}
	}

	
	@Override
	public String toString() {
		return "ClientPKI [id=" + id + ", clientPkiKey=" + clientPkiKey 
				+ ", encodedPublicKey=" + encodedPublicKey + ", publicKeyAlgorithm=" + publicKeyAlgorithm
				+ ", encodedCertificate=" + encodedCertificate + ", publicKey=" + (publicKey==null?null:"*") + ", certificate="
				+ (certificate==null?null:"*") + ", certificateExpiryDate=" + certificateExpiryDate + ", isDefault=" + isDefault + "]";
	}


	public static void main(String[] args) {
		ClientPKI clientPKI = new ClientPKI();
		clientPKI.setId(1L);
		clientPKI.setClientPkiKey("clientPkiKey-1");
		clientPKI.setCertificateExpiryDate(null);
		clientPKI.setEncodedCertificate(null);
		clientPKI.setEncodedPublicKey(null);
		clientPKI.setIsDefault(true);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonStr = gson.toJson(clientPKI);
		System.out.println("jsonStr = \n" + jsonStr);
	}
	



}
