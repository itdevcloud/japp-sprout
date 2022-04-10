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

import io.netty.util.internal.StringUtil;

public class ClientAppInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String clientId;
	private String name;
	private String organizationId;
	private String organizationName;
	private String authenticationProvider;
	private String authenticationCallbackUrl;
	private List<ClientPkiInfo> clientPkiInfoList;
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
	public String getOrganizationName() {
		return organizationName;
	}
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}
	public String getAuthenticationProvider() {
		return authenticationProvider;
	}
	public void setAuthenticationProvider(String authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}
	public String getAuthenticationCallbackUrl() {
		return authenticationCallbackUrl;
	}
	public void setAuthenticationCallbackUrl(String authenticationCallbackUrl) {
		this.authenticationCallbackUrl = authenticationCallbackUrl;
	}
	
	public void addClientPkiInfo(ClientPkiInfo clientPkiInfo) {
		if(this.clientPkiInfoList == null) {
			this.clientPkiInfoList = new ArrayList<ClientPkiInfo>();
		}
		if(clientPkiInfo != null) {
			this.clientPkiInfoList.add(clientPkiInfo);
		}
		return;
	}
	
	public List<ClientPkiInfo> getClientPkiInfoList() {
		if(this.clientPkiInfoList == null) {
			this.clientPkiInfoList = new ArrayList<ClientPkiInfo>();
		}
		return this.clientPkiInfoList;
	}
	public void resetClientPkiInfoList() {
		this.clientPkiInfoList = new ArrayList<ClientPkiInfo>();
	}	
	
	public ClientPkiInfo getClientPkiInfo(String pkiCode) {
		if(this.clientPkiInfoList == null) {
			return null;
		}
		if(StringUtil.isNullOrEmpty(pkiCode)) {
			for (ClientPkiInfo clientPkiInfo: this.clientPkiInfoList) {
				if(clientPkiInfo != null && clientPkiInfo.getIsDefault() != null && clientPkiInfo.getIsDefault() == true ) {
					return clientPkiInfo;
				}
			}
		}else {
			for (ClientPkiInfo clientPkiInfo: this.clientPkiInfoList) {
				if(clientPkiInfo != null && pkiCode.equalsIgnoreCase(clientPkiInfo.getPkiCode()) ) {
					return clientPkiInfo;
				}
			}
		}
		return null;

	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
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
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ClientAppInfo [id=" + id + ", clientId=" + clientId + ", name=" + name + ", organizationId="
				+ organizationId + ", organizationName=" + organizationName + ", authenticationProvider="
				+ authenticationProvider + ", authenticationCallbackUrl=" + authenticationCallbackUrl
				+ ", clientPkiInfoList=" + clientPkiInfoList + "]";
	}


}
