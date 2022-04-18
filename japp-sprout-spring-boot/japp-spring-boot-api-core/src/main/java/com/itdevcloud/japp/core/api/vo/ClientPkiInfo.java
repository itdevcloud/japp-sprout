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

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientPkiInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private static String clientPkiJsonStr;
	private List<ClientPKI> clientPkiList;
	
	static {
		init();
	}
	private static void init() {
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = ClientAuthInfo.class.getResourceAsStream("/client-pki-info.json");
			if (inputStream == null) {
				throw new Exception("can not load client-pki-info.json, check code!.......");
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
		clientPkiJsonStr =  sb.toString();
		//System.out.println("clientPkiJsonStr = \n" + clientPkiJsonStr);
		return;
	}
	
	private List<ClientPKI> loadClientPkiList() {
		List<ClientPKI> pkiList = null;
		Gson gson = new GsonBuilder().serializeNulls().create();
		ClientPkiInfo pkiInfo = null;
		try {
			pkiInfo = gson.fromJson(clientPkiJsonStr, ClientPkiInfo.class);
		}catch (Throwable t) {
			t.printStackTrace();
		}
		pkiList = (pkiInfo == null?null:pkiInfo.getClientPkiList());
		if(pkiList!= null) {
			Collections.sort(pkiList);
		}
		return pkiList;
	}
	public void addClientPKI(ClientPKI clientPki) {
		if(this.clientPkiList == null) {
			this.clientPkiList = loadClientPkiList();
		}
		if(clientPki == null) {
			return;
		}
		this.clientPkiList.add(clientPki);
		return;
	}
	public ClientPKI getClientPKI(String clientPkiKey) {
		if(clientPkiList == null || StringUtil.isEmptyOrNull(clientPkiKey)) {
			return null;
		}
		ClientPKI tmpPKI = new ClientPKI();
		tmpPKI.setClientPkiKey(clientPkiKey);
		ClientPKI provider = clientPkiList.stream().filter(tmpPKI::equals).findAny().orElse(null);
		return provider;
		
	}
	public List<ClientPKI> getClientPkiList() {
		if(this.clientPkiList == null) {
			this.clientPkiList = loadClientPkiList();
		}
		List<ClientPKI> list = new ArrayList<ClientPKI>();
		list.addAll(this.clientPkiList);
		return list;
	}

	public void setClientPkiList(List<ClientPKI> providerList) {
		if(providerList == null) {
			this.clientPkiList = loadClientPkiList();;
		}else {
			this.clientPkiList = new ArrayList<ClientPKI>();
			this.clientPkiList.addAll(providerList);
			Collections.sort(this.clientPkiList);

		}
	}

	@Override
	public String toString() {
		return "ClientPkiInfo [clientPkiList=" + clientPkiList + "]";
	}

	public static void main(String[] args) {
		
		//this is used to generate JSON string which is used as template for client-auth-info.json
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
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonStr = gson.toJson(clientPkiInfo);
		System.out.println("clientPkiInfo json = \n" + jsonStr);
	}



}
