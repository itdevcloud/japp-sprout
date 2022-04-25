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
package com.itdevcloud.japp.core.cahce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;
import com.itdevcloud.japp.core.api.vo.ClientPKI;
import com.itdevcloud.japp.core.api.vo.ClientPkiInfo;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This cache will be refreshed daily. 
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ClientAppInfoCache extends RefreshableCache {


	private static final Logger logger = LogManager.getLogger(ClientAppInfoCache.class);

	private static List<ClientAppInfo> clientAppInfoList = null;

	@PostConstruct
	public void initService() {
	}

	@Override
	public String getInitOrder() {
		return "05";
	}

	@Override
	public void refreshCache() {
		if (lastUpdatedTS == -1) {
			initCache();
		}else {
			logger.info("ClientAppInfoCache.refreshCache() - only daily referesh is requried, do nothing...");
		}
	}
	
	@Override
	public synchronized void initCache() {
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 ) {
				logger.debug("ClientAppInfoCache.init()...start......");
				
				IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
				
				List<ClientAppInfo> appInfoList = helper.getClientAppInfoList();
				if(appInfoList == null) {
					appInfoList = new ArrayList<ClientAppInfo>();
				}
				ClientAppInfo coreAppInfo = AppComponents.commonService.getCoreAppInfo();
				appInfoList.add(coreAppInfo);
				Collections.sort(appInfoList);
				
				String errorStr = validateAppInfo(appInfoList);
				if(!StringUtil.isEmptyOrNull(errorStr)) {
					logger.warn(errorStr);
				}
				
				initInProcess = true;
				this.clientAppInfoList = appInfoList;
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "ClientAppInfoCache.init()  - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nclientAppInfoList Size = " + clientAppInfoList.size() + "\nclientAppInfoList = " + clientAppInfoList ;

				logger.info(str);
				String info = "ClientAppInfoCache.init() - total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nclientAppInfoList size = "+ clientAppInfoList.size() ;
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_CLIENT_APPINFO_CACHE, str);
			}
		} catch (Exception e) {
			String errStr = CommonUtil.getStackTrace(e);
			logger.error(errStr);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_REFERENCE_CODE_CACHE, errStr);
		} finally {
			initInProcess = false;
		}
	}

	private String validateAppInfo(List<ClientAppInfo> appInfoList) {
		if(appInfoList == null || appInfoList.isEmpty()) {
			return "appInfoList is null or empty, check code!";
		}
		int i = 0;
		String errorStr = "";
		for (ClientAppInfo appInfo: appInfoList) {
			String clientAppId = appInfo.getClientAppId();
			ClientAuthInfo clientAuthInfo = appInfo.getClientAuthInfo();
			ClientPkiInfo clientPkiInfo = appInfo.getClientPkiInfo();
			if(StringUtil.isEmptyOrNull(clientAppId) || clientAuthInfo == null || clientPkiInfo == null) {
				errorStr = errorStr + "clientAppId, clientAuthInfo and/or clientPkiInfo is null or empty for List("+ i + ")! \n";
			}
			List<ClientAuthProvider> providerList = clientAuthInfo.getClientAuthProviderList();
			if(providerList == null || providerList.isEmpty()) {
				errorStr = errorStr + "clientAppId, clientAuthInfo and/or clientPkiInfo is null or empty for List("+ i + ")! \n";
				return errorStr;
			}
			for (ClientAuthProvider provider: providerList) {
				if(!AppComponents.commonService.isSupportedAuthProvider(provider.getAuthProviderId())) {
					errorStr = errorStr + "authProviderId is not supported for List("+ i + ")! \n";
					return errorStr;
				}
			}
			i++;
		}
		if(StringUtil.isEmptyOrNull(errorStr)) {
			return null;
		}else {
			return errorStr;
		}
	}
	
	
	public ClientAppInfo getClientAppInfo(String clientAppId) {
		if(this.clientAppInfoList == null || this.clientAppInfoList.isEmpty()) {
			return null;
		}
		if (StringUtil.isEmptyOrNull(clientAppId)) {
			//default to core app, refer to CommonService.getCoreAppInfo()
			clientAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		}
		waitForInit();
		for(ClientAppInfo info: this.clientAppInfoList) {
			if(clientAppId.equalsIgnoreCase(info.getClientAppId()) ) {
				return info;
			}
		}
		return null;
	}
	
	public ClientAuthProvider getClientAuthProvider(String clientAppId, String clientAuthKey) {
		ClientAppInfo clientAppInfo = getClientAppInfo(clientAppId);
		if(clientAppInfo == null ) {
			return null;
		}
		ClientAuthProvider provider = clientAppInfo.getClientAuthProvider(clientAuthKey);
		return provider;
	}
	
	public ClientPKI getClientPKI(String clientAppId, String clientPkiKey) {
		ClientAppInfo clientAppInfo = getClientAppInfo(clientAppId);
		if(clientAppInfo == null ) {
			return null;
		}
		ClientPKI clientPKI = clientAppInfo.getClientPKI(clientPkiKey);
		return clientPKI;
	}
	
	public Set <ClientAppInfo> getAllClientAppInfo() {
		waitForInit();
		if(this.clientAppInfoList == null) {
			return null;
		}
		Set<ClientAppInfo> appSet = new HashSet<ClientAppInfo>(this.clientAppInfoList);
		return appSet;
	}



}
