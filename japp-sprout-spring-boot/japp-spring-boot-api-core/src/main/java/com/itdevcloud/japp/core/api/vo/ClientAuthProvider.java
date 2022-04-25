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

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.ClientCallBackType;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.TokenTransferType;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientAuthProvider implements Serializable,  Comparable<ClientAuthProvider>{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String clientAuthKey;
	private String authProviderId;
	private String multiFactorType;
	private String authAppCallbackUrl;
	private String clientCallbackUrl;
	private ClientCallBackType clientCallbackType;
	private TokenTransferType tokenTransferType;
	private String signoutAppRedirectUrl;
	private String signoutClientRedirectUrl;
	private Map <String, String> authProperties;
	private Boolean isDefault;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClientAuthKey() {
		return clientAuthKey;
	}
	public void setClientAuthKey(String clientAuthKey) {
		this.clientAuthKey = clientAuthKey;
	}
	
	public String getAuthProviderId() {
		return authProviderId;
	}
	public void setAuthProviderId(String authProviderId) {
		this.authProviderId = authProviderId;
	}
	
	public String getMultiFactorType() {
		return multiFactorType;
	}
	public void setMultiFactorType(String multiFactorType) {
		this.multiFactorType = multiFactorType;
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
	public ClientCallBackType getClientCallbackType() {
		return clientCallbackType;
	}
	public void setClientCallbackType(ClientCallBackType clientCallbackType) {
		this.clientCallbackType = clientCallbackType;
	}
	public TokenTransferType getTokenTransferType() {
		return tokenTransferType;
	}
	public void setTokenTransferType(TokenTransferType tokenTransferType) {
		this.tokenTransferType = tokenTransferType;
	}
	
	public void addAuthProperty(String key, String value) {
		if(authProperties == null) {
			authProperties = new HashMap<String, String>();
		}
		if(StringUtil.isEmptyOrNull(key)) {
			return;
		}
		if(StringUtil.isEmptyOrNull(value)) {
			value = "";
		}
		authProperties.put(key, value);
		return;
	}
	public String getAuthProperty(String key) {
		if(StringUtil.isEmptyOrNull(key)) {
			return null;
		}
		String value = getAuthProperties().get(key);
		value = (StringUtil.isEmptyOrNull(value)?"":value.trim()) ;
		return value;
	}
	public Map<String, String> getAuthProperties() {
		if(authProperties == null) {
			authProperties = new HashMap<String, String>();
		}
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(authProperties);
		return map;
	}
	public void setAuthProperties(Map<String, String> authProperties) {
		if(authProperties == null) {
			this.authProperties = null;;
		}else {
			this.authProperties = new HashMap<String, String>();
			this.authProperties.putAll(authProperties);
		}
		return;
	}
	
	public String getSignoutAppRedirectUrl() {
		return signoutAppRedirectUrl;
	}
	public void setSignoutAppRedirectUrl(String signoutAppRedirectUrl) {
		this.signoutAppRedirectUrl = signoutAppRedirectUrl;
	}
	public String getSignoutClientRedirectUrl() {
		return signoutClientRedirectUrl;
	}
	public void setSignoutClientRedirectUrl(String signoutClientRedirectUrl) {
		this.signoutClientRedirectUrl = signoutClientRedirectUrl;
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
		result = prime * result + ((clientAuthKey == null) ? 0 : clientAuthKey.hashCode());
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
		ClientAuthProvider other = (ClientAuthProvider) obj;
		if (clientAuthKey == null) {
			if (other.clientAuthKey != null)
				return false;
		} else if (!clientAuthKey.equals(other.clientAuthKey))
			return false;
		return true;
	}
	

	@Override
	public String toString() {
		return "ClientAuthProvider [id=" + id + ", clientAuthKey=" + clientAuthKey + ", authProviderId="
				+ authProviderId + ", authAppCallbackUrl=" + authAppCallbackUrl + ", clientCallbackUrl="
				+ clientCallbackUrl + ", clientCallbackType=" + clientCallbackType + ", tokenTransferType="
				+ tokenTransferType + ", signoutAppRedirectUrl=" + signoutAppRedirectUrl + ", signoutClientRedirectUrl="
				+ signoutClientRedirectUrl + ", authProperties=" + authProperties + ", isDefault=" + isDefault + "]";
	}
	@Override
	public int compareTo(ClientAuthProvider o) {
		if(o == null) {
			return 1;
		}
		ClientAuthProvider e = (ClientAuthProvider) o;
		if(getClientAuthKey() == null && e.getClientAuthKey() ==null) {
			return 0;
		}else if(getClientAuthKey() == null) {
			return -1;
		}else if(e.getClientAuthKey() ==null) {
			return 1;
		}else {
			return getClientAuthKey().compareTo(e.getClientAuthKey());
		}
	}

	
	public static void main(String[] args) {
		ClientAuthProvider ClientAuthProvider = new ClientAuthProvider();
		ClientAuthProvider.setId(1L);
		ClientAuthProvider.setClientAuthKey("clientAuthKey-1");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setAuthProviderId("AAD-OIDC");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setClientCallbackType(ClientCallBackType.POST);
		ClientAuthProvider.setTokenTransferType(TokenTransferType.SESSION_STORAGE);
		ClientAuthProvider.setIsDefault(true);
		ClientAuthProvider.addAuthProperty("aad.client.id", "c3d6299f-2aed-45be-ab4f-857f5961b13e");
		ClientAuthProvider.addAuthProperty("aad.auth.prompt", "login");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonStr = gson.toJson(ClientAuthProvider);
		System.out.println("jsonStr = \n" + jsonStr);
	}
	



}
