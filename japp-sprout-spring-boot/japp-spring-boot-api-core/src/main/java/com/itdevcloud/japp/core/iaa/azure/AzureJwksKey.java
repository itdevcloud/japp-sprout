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
package com.itdevcloud.japp.core.iaa.azure;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

/**
 * A class represents one of Azure AD's public keys. A public key will be used to verify that a JWT token is valid 
 * and originated from the Azure AD.
 *
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class AzureJwksKey {

	private String kty;
	private String use;
	private String kid;
	private String x5t;
	private String n;
	private String e;
	private List<String> x5c;
	//not in the AAD json, drived from above fields
	private Certificate certificate;
	private PublicKey publicKey;

	public String getKty() {
		return kty;
	}
	public void setKty(String kty) {
		this.kty = kty;
	}
	public String getUse() {
		return use;
	}
	public void setUse(String use) {
		this.use = use;
	}
	public String getKid() {
		return kid;
	}
	public void setKid(String kid) {
		this.kid = kid;
	}
	public String getX5t() {
		return x5t;
	}
	public void setX5t(String x5t) {
		this.x5t = x5t;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public String getE() {
		return e;
	}
	public void setE(String e) {
		this.e = e;
	}

	public List<String> getX5c() {
		return x5c;
	}
	public void setX5c(List<String> x5c) {
		this.x5c = x5c;
	}


	public Certificate getCertificate() {
		return certificate;
	}
	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	@Override
	public String toString() {
		//		return "AzureJwksKey [kty=" + kty + ", use=" + use + ", kid=" + kid + ", x5t=" + x5t + ", n=" + n + ", e=" + e
		//				+ ", x5c=" + x5c + ", certificate=" + certificate + ", publicKey=" + publicKey + "]";
		return "AzureJwksKey [kty=" + kty + ", use=" + use + ", kid=" + kid + ", x5t=..., n=..., e=..., x5c=..., certificate=..., publicKey=...]";
	}


}
