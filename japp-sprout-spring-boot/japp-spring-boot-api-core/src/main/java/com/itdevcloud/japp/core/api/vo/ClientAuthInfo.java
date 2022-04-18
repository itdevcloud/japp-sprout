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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientAuthInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	public static enum ClientCallBackType {REDIRECT, POST};
	public static enum TokenTransferType {COOKIE, SESSION_STORAGE, QUERY_PARAMETER};

	private static String clientAuthProviderJsonStr;
	private List<ClientAuthProvider> clientAuthProviderList;
	
	static {
		init();
	}
	private static void init() {
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = ClientAuthInfo.class.getResourceAsStream("/client-auth-info.json");
			if (inputStream == null) {
				throw new Exception("can not load client-auth-info.json, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} catch (Exception e) {
			e.printStackTrace();
			sb = new StringBuilder(e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		clientAuthProviderJsonStr =  sb.toString();
		//System.out.println("clientAuthProviderJsonStr = \n" + clientAuthProviderJsonStr);
		return;
	}
	
	private List<ClientAuthProvider> loadClientAuthProviderList() {
		List<ClientAuthProvider> providerList = null;
		Gson gson = new GsonBuilder().serializeNulls().create();
		ClientAuthInfo authInfo = null;
		try {
			authInfo = gson.fromJson(clientAuthProviderJsonStr, ClientAuthInfo.class);
		}catch (Throwable t) {
			t.printStackTrace();
		}
		providerList = (authInfo == null?null:authInfo.getClientAuthProviderList());
		if(providerList!= null) {
			Collections.sort(providerList);
		}
		return providerList;
	}
	public void addClientAuthProvider(ClientAuthProvider clientAuthProvider) {
		if(this.clientAuthProviderList == null) {
			this.clientAuthProviderList = loadClientAuthProviderList();
		}
		if(clientAuthProvider == null) {
			return;
		}
		this.clientAuthProviderList.add(clientAuthProvider);
		return;
	}
	public ClientAuthProvider findClientAuthProvier(String clientAuthKey) {
		if(clientAuthProviderList == null || StringUtil.isEmptyOrNull(clientAuthKey)) {
			return null;
		}
		ClientAuthProvider tmpProvider = new ClientAuthProvider();
		tmpProvider.setClientAuthKey(clientAuthKey);
		ClientAuthProvider provider = clientAuthProviderList.stream().filter(tmpProvider::equals).findAny().orElse(null);
		return provider;
		
	}
	public List<ClientAuthProvider> getClientAuthProviderList() {
		if(this.clientAuthProviderList == null) {
			this.clientAuthProviderList = loadClientAuthProviderList();
		}
		List<ClientAuthProvider> list = new ArrayList<ClientAuthProvider>();
		list.addAll(this.clientAuthProviderList);
		return list;
	}

	public void setClientAuthProviderList(List<ClientAuthProvider> providerList) {
		if(providerList == null) {
			this.clientAuthProviderList = loadClientAuthProviderList();;
		}else {
			this.clientAuthProviderList = new ArrayList<ClientAuthProvider>();
			this.clientAuthProviderList.addAll(providerList);
			Collections.sort(this.clientAuthProviderList);

		}
	}

	@Override
	public String toString() {
		return "ClientAuthInfo [clientAuthProviderList=" + clientAuthProviderList + "]";
	}

	public static void main(String[] args) {
		
		//this is used to generate JSON string which is used as template for client-auth-info.json
		ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
		List<ClientAuthProvider> providerList = new ArrayList<ClientAuthProvider>();
		
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
		
		
		providerList.add(ClientAuthProvider);
		
		ClientAuthProvider = new ClientAuthProvider();
		ClientAuthProvider.setId(2L);
		ClientAuthProvider.setClientAuthKey("clientAuthKey-2");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setAuthProviderId("CORE-BASIC");
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setClientCallbackType(ClientCallBackType.REDIRECT);
		ClientAuthProvider.setTokenTransferType(TokenTransferType.COOKIE);
		ClientAuthProvider.setIsDefault(false);
		ClientAuthProvider.addAuthProperty("aad.client.id", "c3d6299f-2aed-45be-ab4f-857f5961b13e");
		ClientAuthProvider.addAuthProperty("aad.auth.prompt", "login");
		
		providerList.add(ClientAuthProvider);
		
		clientAuthInfo.setClientAuthProviderList(providerList); 
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonStr = gson.toJson(clientAuthInfo);
		System.out.println("clientAuthInfo json = \n" + jsonStr);
	}
	
}
