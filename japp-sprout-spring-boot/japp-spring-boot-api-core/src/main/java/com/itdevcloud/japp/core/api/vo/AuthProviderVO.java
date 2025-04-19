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

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.KeySecretVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AuthProviderVO  {
	
	//private static final Logger logger = LogManager.getLogger(AuthProviderVO.class);

	private String name;
	private String type;
	private String authnURL;
	private String authnLogoutURL;
	private String issueAccessTokenURL;
	private String renewAccessTokenURL;
	private PublicKey publicKey;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthnURL() {
		if(AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equalsIgnoreCase(this.name)) {
			return AppComponents.aadJwksCache.getAadAuthUri();
		}
		return authnURL;
	}
	public void setAuthnURL(String authnURL) {
		this.authnURL = authnURL;
	}
	
	public String getAuthnLogoutURL() {
		if(AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equalsIgnoreCase(this.name)) {
			return AppComponents.aadJwksCache.getAadAuthLogoutUri();
		}
		return authnLogoutURL;
	}
	public void setAuthnLogoutURL(String authnLogoutURL) {
		this.authnLogoutURL = authnLogoutURL;
	}
	public String getIssueAccessTokenURL() {
		return issueAccessTokenURL;
	}
	public void setIssueAccessTokenURL(String issueAccessTokenURL) {
		this.issueAccessTokenURL = issueAccessTokenURL;
	}
	public String getRenewAccessTokenURL() {
		return renewAccessTokenURL;
	}
	public void setRenewAccessTokenURL(String renewAccessTokenURL) {
		this.renewAccessTokenURL = renewAccessTokenURL;
	}
	
	public PublicKey getPublicKey(String kid, String x5t) {
		if(AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equalsIgnoreCase(this.name)) {
			return AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
		}
		return null;
	}
	public PublicKey getPublicKey() {
		if(AppConstant.AUTH_PROVIDER_NAME_MY_APP.equalsIgnoreCase(this.name)) {
			return AppComponents.pkiKeyCache.getJappPublicKey();
		}
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	@Override
	public String toString() {
		return "AuthProviderVO [name=" + name + ", type=" + type + ", authnURL=" + authnURL + ", issueAccessTokenURL="
				+ issueAccessTokenURL + ", renewAccessTokenURL=" + renewAccessTokenURL + ", publicKey=" + (publicKey==null?null:"***")
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		AuthProviderVO other = (AuthProviderVO) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


}
