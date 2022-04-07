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

public class AppSiteInfo implements Serializable{

	private Long siteId;
	private String siteCode;
	private String authCallbackUrl;
	private PublicKey publicKey;
	private Certificate certificate;
	private Date certificateExpiryDate;
	

	public Long getSiteId() {
		return siteId;
	}
	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}
	
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = (siteCode==null?null:siteCode.trim());
	}
	public String getAuthCallbackUrl() {
		return authCallbackUrl;
	}
	public void setAuthCallbackUrl(String authCallbackUrl) {
		this.authCallbackUrl = (authCallbackUrl==null?null:authCallbackUrl.trim());
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
	public void setCertificateExpiryDate(Date certificateExpiryTs) {
		this.certificateExpiryDate = certificateExpiryTs;
	}
	@Override
	public String toString() {
		return "AppSiteInfo [siteId=" + siteId + ", siteCode=" + siteCode + ", authCallbackUrl=" + authCallbackUrl
				+ "]";
	}
	



}
