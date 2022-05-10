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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.ClientCallBackType;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.TokenTransferType;
import io.netty.util.internal.StringUtil;

public class ClientAppInfo implements Serializable,  Comparable<ClientAppInfo>{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ClientAppInfo.class);
	
	private Long id;
	private String clientAppId;
	private String name;
	private String organizationId;
	private Boolean apiAutoRenewAccessToken;
	private Boolean enforceTokenNonce;
	private Boolean enforceTokenIP;
	private CidrWhiteList cidrWhiteList;
	private ClientAuthInfo clientAuthInfo;
	private ClientPkiInfo clientPkiInfo;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClientAppId() {
		return clientAppId;
	}
	public void setClientAppId(String clientId) {
		this.clientAppId = clientId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public Boolean getApiAutoRenewAccessToken() {
		return apiAutoRenewAccessToken;
	}
	public void setApiAutoRenewAccessToken(Boolean apiAutoRenewAccessToken) {
		this.apiAutoRenewAccessToken = apiAutoRenewAccessToken;
	}
	
	
	public Boolean getEnforceTokenNonce() {
		return enforceTokenNonce;
	}
	public void setEnforceTokenNonce(Boolean enforceTokenNonce) {
		this.enforceTokenNonce = enforceTokenNonce;
	}
	public Boolean getEnforceTokenIP() {
		return enforceTokenIP;
	}
	public void setEnforceTokenIP(Boolean enforceTokenIP) {
		this.enforceTokenIP = enforceTokenIP;
	}
	public void addClientAuthProvider(ClientAuthProvider clientAuthProvider) {
		if(this.clientAuthInfo == null) {
			this.clientAuthInfo = new ClientAuthInfo();
		}
		if(clientAuthProvider != null) {
			this.clientAuthInfo.addClientAuthProvider(clientAuthProvider);
		}
		return;
	}

	public ClientAuthProvider getClientAuthProvider(String clientAuthKey) {
		if(this.clientAuthInfo == null || this.clientAuthInfo.getClientAuthProviderList() == null) {
			return null;
		}
		List<ClientAuthProvider> providerList = this.clientAuthInfo.getClientAuthProviderList();
		if(StringUtil.isNullOrEmpty(clientAuthKey)) {
			for (ClientAuthProvider provider: providerList) {
				if(provider != null && provider.getIsDefault() != null && provider.getIsDefault() == true ) {
					return provider;
				}
			}
		}else {
			for (ClientAuthProvider provider: providerList) {
				if(provider != null && clientAuthKey.equalsIgnoreCase(provider.getClientAuthKey()) ) {
					return provider;
				}
			}
		}
		return null;

	}

	
	public void addClientPKI(ClientPKI clientPKI) {
		if(this.clientPkiInfo == null) {
			this.clientPkiInfo = new ClientPkiInfo();
		}
		if(clientPKI != null) {
			this.clientPkiInfo.addClientPKI(clientPKI);
		}
		return;
	}
	

	public ClientPKI getClientPKI(String clientPkiKey) {
		if(this.clientPkiInfo == null || this.clientPkiInfo.getClientPkiList() == null) {
			return null;
		}
		List<ClientPKI> pkiList = this.clientPkiInfo.getClientPkiList();
		if(StringUtil.isNullOrEmpty(clientPkiKey)) {
			for (ClientPKI pki: pkiList) {
				if(pki != null && pki.getIsDefault() != null && pki.getIsDefault() == true ) {
					return pki;
				}
			}
		}else {
			for (ClientPKI pki: pkiList) {
				if(pki != null && clientPkiKey.equalsIgnoreCase(pki.getClientPkiKey()) ) {
					return pki;
				}
			}
		}
		return null;

	}
	
	
	public CidrWhiteList getCidrWhiteList() {
		return cidrWhiteList;
	}
	public void setCidrWhiteList(CidrWhiteList cidrWhiteList) {
		this.cidrWhiteList = cidrWhiteList;
	}
	public ClientAuthInfo getClientAuthInfo() {
		return clientAuthInfo;
	}
	public void setClientAuthInfo(ClientAuthInfo clientAuthInfo) {
		this.clientAuthInfo = clientAuthInfo;
	}
	public ClientPkiInfo getClientPkiInfo() {
		return clientPkiInfo;
	}
	public void setClientPkiInfo(ClientPkiInfo clientPkiInfo) {
		this.clientPkiInfo = clientPkiInfo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientAppId == null) ? 0 : clientAppId.hashCode());
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
		ClientAppInfo other = (ClientAppInfo) obj;
		if (clientAppId == null) {
			if (other.clientAppId != null)
				return false;
		} else if (!clientAppId.equals(other.clientAppId))
			return false;
		return true;
	}
	@Override
	public int compareTo(ClientAppInfo o) {
		if(o == null) {
			return 1;
		}
		ClientAppInfo e = (ClientAppInfo) o;
		if(getClientAppId() == null && e.getClientAppId() ==null) {
			return 0;
		}else if(getClientAppId() == null) {
			return -1;
		}else if(e.getClientAppId() ==null) {
			return 1;
		}else {
			return getClientAppId().compareTo(e.getClientAppId());
		}
	}
	
	@Override
	public String toString() {
		return "ClientAppInfo [id=" + id + ", clientAppId=" + clientAppId + ", name=" + name + ", organizationId="
				+ organizationId + ", apiAutoRenewAccessToken=" + apiAutoRenewAccessToken + ", enforceTokenNonce="
				+ enforceTokenNonce + ", enforceTokenIP=" + enforceTokenIP + ", cidrWhiteList=" + cidrWhiteList
				+ ", clientAuthInfo=" + clientAuthInfo + ", clientPkiInfo=" + clientPkiInfo + "]";
	}
	
	public static void main(String[] args) {
		
		//this is used to generate JSON string which is used as template for client-auth-info.json
		ClientAppInfo clientAppInfo = new ClientAppInfo();
		clientAppInfo.setId(1L);
		clientAppInfo.setClientAppId("clientappid-1");
		clientAppInfo.setName("Client-1");
		clientAppInfo.setOrganizationId("Org-1");
		clientAppInfo.setApiAutoRenewAccessToken(true);
		clientAppInfo.setEnforceTokenNonce(true);
		clientAppInfo.setEnforceTokenIP(false);

		CidrWhiteList cidrWhitelist = new CidrWhiteList();
		ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
		List<ClientAuthProvider> providerList = new ArrayList<ClientAuthProvider>();
		
		cidrWhitelist.addCidrIP("127.0.0.1"); 
		cidrWhitelist.addCidrIP("127.0.0.1"); 
		cidrWhitelist.addCidrIP("192.168.1.0/24"); 

		clientAppInfo.setCidrWhiteList(cidrWhitelist);
		
		ClientAuthProvider ClientAuthProvider = new ClientAuthProvider();
		ClientAuthProvider.setId(1L);
		ClientAuthProvider.setClientAuthKey("clientAuthKey-1");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setAuthProviderId("AAD-OIDC");
		ClientAuthProvider.setAuthAppCallbackUrl("https://localhost:8443/open/aadauth");
		ClientAuthProvider.setClientCallbackType(ClientCallBackType.POST);
		ClientAuthProvider.setTokenTransferType(TokenTransferType.SESSION_STORAGE);
		ClientAuthProvider.setSignoutAppRedirectUrl(null);
		ClientAuthProvider.setSignoutClientRedirectUrl(null);
		ClientAuthProvider.setIsDefault(true);
		ClientAuthProvider.addAuthProperty("aad.client.id", "c3d6299f-2aed-45be-ab4f-857f5961b13e");
		ClientAuthProvider.addAuthProperty("aad.auth.prompt", "login");
		
		
		providerList.add(ClientAuthProvider);
		
		ClientAuthProvider = new ClientAuthProvider();
		ClientAuthProvider.setId(2L);
		ClientAuthProvider.setClientAuthKey("clientAuthKey-2");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setAuthProviderId("CORE-BASIC");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setClientCallbackType(ClientCallBackType.REDIRECT);
		ClientAuthProvider.setTokenTransferType(TokenTransferType.COOKIE);
		ClientAuthProvider.setSignoutAppRedirectUrl(null);
		ClientAuthProvider.setSignoutClientRedirectUrl(null);
		ClientAuthProvider.setIsDefault(false);
		ClientAuthProvider.addAuthProperty("aad.client.id", "c3d6299f-2aed-45be-ab4f-857f5961b13e");
		ClientAuthProvider.addAuthProperty("aad.auth.prompt", "login");
		
		providerList.add(ClientAuthProvider);

		clientAuthInfo.setClientAuthProviderList(providerList); 

		clientAppInfo.setClientAuthInfo(clientAuthInfo);
		
		//pki info
		ClientPkiInfo clientPkiInfo = new ClientPkiInfo();
		List<ClientPKI> pkiList = new ArrayList<ClientPKI>();
		
		ClientPKI clientPKI = new ClientPKI();
		clientPKI.setId(1L);
		clientPKI.setClientPkiKey("clientPkiKey-1");
		clientPKI.setCertificateExpiryDate(null);
		clientPKI.setEncodedCertificate(null);
		clientPKI.setEncodedPublicKey(null);
		clientPKI.setIsDefault(true);
		
		
		pkiList.add(clientPKI);
		
		clientPKI = new ClientPKI();
		clientPKI.setId(2L);
		clientPKI.setClientPkiKey("clientPkiKey-2");
		clientPKI.setCertificateExpiryDate(null);
		clientPKI.setEncodedCertificate(null);
		clientPKI.setEncodedPublicKey(null);
		clientPKI.setIsDefault(true);
		
		pkiList.add(clientPKI);
		
		clientPkiInfo.setClientPkiList(pkiList); 
		
		clientAppInfo.setClientPkiInfo(clientPkiInfo);
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonStr = gson.toJson(clientAppInfo);
		System.out.println("clientAppInfo json Template = \n" + jsonStr);
	}



}
