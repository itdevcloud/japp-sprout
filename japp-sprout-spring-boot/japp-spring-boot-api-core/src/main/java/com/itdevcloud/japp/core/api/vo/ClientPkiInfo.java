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

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientPkiInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String clientId;
	private String pkiCode;
	private PublicKey publicKey;
	private Certificate certificate;
	private Date certificateExpiryDate;
	private Boolean isDefault;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getPkiCode() {
		return pkiCode;
	}
	public void setPkiCode(String pkiCode) {
		this.pkiCode = pkiCode;
	}
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	public Certificate getCertificate() {
		return certificate;
	}
	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
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
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((pkiCode == null) ? 0 : pkiCode.hashCode());
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
		ClientPkiInfo other = (ClientPkiInfo) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (pkiCode == null) {
			if (other.pkiCode != null)
				return false;
		} else if (!pkiCode.equals(other.pkiCode))
			return false;
		return true;
	}
	@Override
	public String toString() {
		String certStr = (certificate != null? "*":null);
		String keyStr = (publicKey != null? "*":null);
		return "ClientPkiInfo [id=" + id + ", clientId=" + clientId + ", pkiCode=" + pkiCode + ", publicKey="
				+ keyStr + ", certificate=" + certStr + ", certificateExpiryDate=" + certificateExpiryDate
				+ ", isDefault=" + isDefault + "]";
	}
	




}
