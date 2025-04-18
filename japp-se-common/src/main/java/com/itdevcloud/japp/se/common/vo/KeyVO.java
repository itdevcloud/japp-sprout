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
package com.itdevcloud.japp.se.common.vo;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

import javax.crypto.SecretKey;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class KeyVO  implements  Comparable {
	
	private String keyId;
	private String appId;
	private String jwtKid;
	private String jwtX5t;
	private int sequence;
	private Key privateKey;
	private PublicKey publicKey;
	private Certificate certificate;
	private String pkiKeyAlgorithm;
	private String pkiSignAlgorithm;
	
	private String totpSecret;
	private String cipherTransformation;
	private SecretKey cipherSecretKey;

	public KeyVO() {
	}


	public String getKeyId() {
		if (StringUtil.isEmptyOrNull(keyId)) {
			keyId = appId + "-" + sequence;
		}
		return keyId;
	}


	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}


	public String getAppId() {
		return appId;
	}


	public void setAppId(String appId) {
		this.appId = appId;
	}


	public String getJwtKid() {
		return jwtKid;
	}


	public void setJwtKid(String jwtKid) {
		this.jwtKid = jwtKid;
	}


	public String getJwtX5t() {
		return jwtX5t;
	}


	public void setJwtX5t(String jwtX5t) {
		this.jwtX5t = jwtX5t;
	}


	public int getSequence() {
		return sequence;
	}


	public void setSequence(int sequence) {
		this.sequence = sequence;
	}


	public Key getPrivateKey() {
		return privateKey;
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
		return certificate;
	}


	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}


	public String getPkiKeyAlgorithm() {
		return pkiKeyAlgorithm;
	}


	public void setPkiKeyAlgorithm(String pkiKeyAlgorithm) {
		this.pkiKeyAlgorithm = pkiKeyAlgorithm;
	}


	public String getPkiSignAlgorithm() {
		return pkiSignAlgorithm;
	}


	public void setPkiSignAlgorithm(String pkiSignAlgorithm) {
		this.pkiSignAlgorithm = pkiSignAlgorithm;
	}


	public String getTotpSecret() {
		return totpSecret;
	}


	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}


	public String getCipherTransformation() {
		return cipherTransformation;
	}


	public void setCipherTransformation(String cipherTransformation) {
		this.cipherTransformation = cipherTransformation;
	}


	public SecretKey getCipherSecretKey() {
		return cipherSecretKey;
	}


	public void setCipherSecretKey(SecretKey cipherSecretKey) {
		this.cipherSecretKey = cipherSecretKey;
	}


	@Override
	public String toString() {
		return "KeyVO [keyId=" + keyId + ", appId=" + appId + ", jwtKid=" + jwtKid + ", jwtX5t=" + jwtX5t
				+ ", sequence=" + sequence + ", privateKey=" + (privateKey==null?null:"***") + ", publicKey=" + (publicKey==null?null:"***")
				+ ", certificate=" + (certificate==null?null:"***") + ", pkiKeyAlgorithm=" + pkiKeyAlgorithm + ", pkiSignAlgorithm="
				+ pkiSignAlgorithm + ", totpSecret=" + (totpSecret==null?null:"***") + ", cipherTransformation=" + cipherTransformation
				+ ", cipherSecretKey=" + (cipherSecretKey==null?null:"***") + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
		result = prime * result + sequence;
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
		KeyVO other = (KeyVO) obj;
		if (appId == null) {
			if (other.appId != null)
				return false;
		} else if (!appId.equals(other.appId))
			return false;
		if (sequence != other.sequence)
			return false;
		return true;
	}


	@Override
	public int compareTo(Object obj) {
		if (this == obj) {
			return 0;
		}
		if (obj == null) {
			return 1;
		}
		if (getClass() != obj.getClass()) {
			return StringUtil.compareTo(this.getClass().getSimpleName(), obj.getClass().getSimpleName());
		}
		KeyVO other = (KeyVO) obj;
		int result = StringUtil.compareTo(this.getAppId(), other.getAppId());
		if(result != 0) {
			return result;
		}
		result = this.getSequence()==other.getSequence()?0: (this.getSequence()>other.getSequence()?1:-1);
		
		return result;
	}





}
