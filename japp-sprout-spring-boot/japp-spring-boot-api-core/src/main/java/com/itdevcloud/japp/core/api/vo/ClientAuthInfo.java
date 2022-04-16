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

public class ClientAuthInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	public static enum ClientCallBackType {REDIRECT, POST};
	public static enum TokenTransferType {COOKIE, SESSION_STORAGE, QUERY_PARAMETER};

	private Long id;
	private String clientId;
	private String authKey;
	private String authProviderId;
	private String authAppCallbackUrl;
	private String clientCallbackUrl;
	private ClientCallBackType clientCallbackType;
	private TokenTransferType tokenTransferType;
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
	public String getAuthKey() {
		return authKey;
	}
	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}
	public String getAuthProviderId() {
		return authProviderId;
	}
	public void setAuthProviderId(String authProviderId) {
		this.authProviderId = authProviderId;
	}
	public String getAuthAppCallbackUrl() {
		return authAppCallbackUrl;
	}
	public void setAuthAppCallbackUrl(String authAppCallbackUrl) {
		this.authAppCallbackUrl = authAppCallbackUrl;
	}
	public String getClientCallbackUrl() {
		return clientCallbackUrl;
	}
	public void setClientCallbackUrl(String clientCallbackUrl) {
		this.clientCallbackUrl = clientCallbackUrl;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	public ClientCallBackType getClientCallbackType() {
		if(ClientCallBackType.POST == this.clientCallbackType || 
				ClientCallBackType.REDIRECT == this.clientCallbackType  ) {
			return this.clientCallbackType;
		}else {
			this.clientCallbackType = ClientCallBackType.POST;
		}
		return clientCallbackType;
	}
	public void setClientCallbackType(ClientCallBackType clientCallbackType) {
		this.clientCallbackType = clientCallbackType;
	}
	
	public TokenTransferType getTokenTransferType() {
		if(TokenTransferType.COOKIE == this.tokenTransferType || 
				TokenTransferType.QUERY_PARAMETER == this.tokenTransferType ||
						TokenTransferType.SESSION_STORAGE == this.tokenTransferType ) {
			return this.tokenTransferType;
		}else {
			this.tokenTransferType = TokenTransferType.SESSION_STORAGE;
		}
		return this.tokenTransferType;
	}
	public void setTokenTransferType(TokenTransferType tokenTrasferType) {
		this.tokenTransferType = tokenTrasferType;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authKey == null) ? 0 : authKey.hashCode());
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
		ClientAuthInfo other = (ClientAuthInfo) obj;
		if (authKey == null) {
			if (other.authKey != null)
				return false;
		} else if (!authKey.equals(other.authKey))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ClientAuthInfo [id=" + id + ", clientId=" + clientId + ", authKey=" + authKey + ", authProviderId="
				+ authProviderId + ", authAppCallbackUrl=" + authAppCallbackUrl + ", clientCallbackUrl="
				+ clientCallbackUrl + ", clientCallbackType=" + clientCallbackType + ", tokenTrasferType="
				+ tokenTransferType + ", isDefault=" + isDefault + "]";
	}
	
	



}
