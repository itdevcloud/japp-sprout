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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.KeyVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class IaaAppVO  {
	
	private static final Logger logger = LogManager.getLogger(IaaAppVO.class);

	private String appId;
	private List<KeyVO> AppKeys ;
	private String authnProvider;
	private String authnProviderURL;
	private List<String> authnCallbackURLs;
	private List<String> clientCidrWhitelist;
	

	public IaaAppVO() {
	}


	public String getAppId() {
		return appId;
	}


	public void setAppId(String appId) {
		this.appId = appId;
	}


	public List<KeyVO> getAppkeys() {
		if(this.AppKeys == null) {
			this.AppKeys = new ArrayList<KeyVO>();
		}
		return this.AppKeys;
	}


	public void setAppkeys(List<KeyVO> appkeys) {
		AppKeys = appkeys;
	}

	public void addAppkey(KeyVO keyVO) {
		if(this.AppKeys == null) {
			this.AppKeys = new ArrayList<KeyVO>();
		}
		if(keyVO != null) {
			this.AppKeys.add(keyVO);
		}
	}
	public List<KeyVO> getPkikeys(String appId) {
		if(this.AppKeys == null || this.AppKeys.isEmpty() || StringUtil.isEmptyOrNull(appId)) {
			logger.warn("Appkeys and/or appId is empty or null, return null..........");
			return null;
		}
		List<KeyVO> keys  = new ArrayList<KeyVO>();
		KeyVO tmpKey = null;
		for (KeyVO key : this.AppKeys) {
			if(appId.equalsIgnoreCase(key.getAppId()) && (key.getCertificate() != null || key.getPrivateKey() != null || key.getPublicKey() != null)) {
				tmpKey = new KeyVO();
				tmpKey.setKeyId(key.getKeyId());
				tmpKey.setAppId(key.getAppId());
				tmpKey.setSequence(key.getSequence());
				tmpKey.setJwtKid(key.getJwtKid());
				tmpKey.setJwtX5t(key.getJwtX5t());
				tmpKey.setPkiKeyAlgorithm(key.getPkiKeyAlgorithm());
				tmpKey.setPkiSignAlgorithm(key.getPkiSignAlgorithm());
				tmpKey.setPrivateKey(key.getPrivateKey());
				tmpKey.setPublicKey(key.getPublicKey());
				tmpKey.setCertificate(key.getCertificate());
				keys.add(tmpKey);
			}
		}
		Collections.sort(keys);
		return keys;
	}
	public List<KeyVO> getCipherkeys(String appId) {
		if(this.AppKeys == null || this.AppKeys.isEmpty() || StringUtil.isEmptyOrNull(appId)) {
			logger.warn("Appkeys and/or appId is empty or null, return null..........");
			return null;
		}
		List<KeyVO> keys  = new ArrayList<KeyVO>();
		KeyVO tmpKey = null;
		for (KeyVO key : this.AppKeys) {
			if(appId.equalsIgnoreCase(key.getAppId()) && key.getCipherSecretKey() != null ) {
				tmpKey = new KeyVO();
				tmpKey.setKeyId(key.getKeyId());
				tmpKey.setAppId(key.getAppId());
				tmpKey.setSequence(key.getSequence());
				tmpKey.setCipherSecretKey(key.getCipherSecretKey());
				tmpKey.setCipherTransformation(key.getCipherTransformation());
				
				keys.add(tmpKey);
			}
		}
		Collections.sort(keys);
		
		return keys;
	}
	
	public List<KeyVO> getCipherKeys(String appId) {
		if(this.AppKeys == null || this.AppKeys.isEmpty() || StringUtil.isEmptyOrNull(appId)) {
			logger.warn("Appkeys and/or appId is empty or null, return null..........");
			return null;
		}
		List<KeyVO> keys  = new ArrayList<KeyVO>();
		KeyVO tmpKey = null;
		for (KeyVO key : this.AppKeys) {
			if(appId.equalsIgnoreCase(key.getAppId()) && key.getCipherSecretKey() != null ) {
				tmpKey = new KeyVO();
				tmpKey.setKeyId(key.getKeyId());
				tmpKey.setAppId(key.getAppId());
				tmpKey.setSequence(key.getSequence());
				tmpKey.setCipherSecretKey(key.getCipherSecretKey());
				tmpKey.setCipherTransformation(key.getCipherTransformation());
				
				keys.add(tmpKey);
			}
		}
		Collections.sort(keys);
		
		return keys;
	}
	public List<KeyVO> getTotpSecrets(String appId) {
		if(this.AppKeys == null || this.AppKeys.isEmpty() || StringUtil.isEmptyOrNull(appId)) {
			logger.warn("Appkeys and/or appId is empty or null, return null..........");
			return null;
		}
		List<KeyVO> keys  = new ArrayList<KeyVO>();
		KeyVO tmpKey = null;
		for (KeyVO key : this.AppKeys) {
			if(appId.equalsIgnoreCase(key.getAppId()) && key.getCipherSecretKey() != null ) {
				tmpKey = new KeyVO();
				tmpKey.setKeyId(key.getKeyId());
				tmpKey.setAppId(key.getAppId());
				tmpKey.setSequence(key.getSequence());
				tmpKey.setTotpSecret(key.getTotpSecret());
				
				keys.add(tmpKey);
			}
		}
		Collections.sort(keys);
		
		return keys;
	}

	
	public String getAuthnProvider() {
		return authnProvider;
	}


	public void setAuthnProvider(String authnProvider) {
		this.authnProvider = authnProvider;
	}


	public String getAuthnProviderURL() {
		return authnProviderURL;
	}


	public void setAuthnProviderURL(String authnProviderURL) {
		this.authnProviderURL = authnProviderURL;
	}


	public List<String> getAuthnCallbackURLs() {
		return authnCallbackURLs;
	}


	public void setAuthnCallbackURLs(List<String> authnProviderCallbackURLs) {
		this.authnCallbackURLs = authnProviderCallbackURLs;
	}
	
	public void addAuthnCallbackURL(String callbackURL) {
		if(this.authnCallbackURLs == null) {
			this.authnCallbackURLs = new ArrayList<String>();
		}
		if(callbackURL != null) {
			this.authnCallbackURLs.add(callbackURL);
		}
	}



	public List<String> getClientCidrWhitelist() {
		return clientCidrWhitelist;
	}


	public void setClientCidrWhitelist(List<String> clientCidrWhitelist) {
		this.clientCidrWhitelist = clientCidrWhitelist;
	}

	public void addClientCidrWhitelistIP(String cidr) {
		if(this.clientCidrWhitelist == null) {
			this.clientCidrWhitelist = new ArrayList<String>();
		}
		if(cidr != null) {
			this.clientCidrWhitelist.add(cidr);
		}
	}



	@Override
	public String toString() {
		return "AppVO [appId=" + appId + ", AppKeys=" + CommonUtil.listToString(AppKeys) + ", authnProvider=" + authnProvider
				+ ", authnCallbackURLs=" + authnCallbackURLs + ", clientWhitelistIPs="
				+ clientCidrWhitelist + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
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
		IaaAppVO other = (IaaAppVO) obj;
		if (appId == null) {
			if (other.appId != null)
				return false;
		} else if (!appId.equals(other.appId))
			return false;
		return true;
	}




}
