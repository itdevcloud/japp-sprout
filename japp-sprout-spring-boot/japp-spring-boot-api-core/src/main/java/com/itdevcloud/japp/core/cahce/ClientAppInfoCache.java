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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.se.common.util.CommonUtil;

/**
 * This cache will be refreshed daily. 
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ClientAppInfoCache extends RefreshableCache {


	private static final Logger logger = LogManager.getLogger(ClientAppInfoCache.class);

	private static Set<ClientAppInfo> clientAppInfoSet = null;

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
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL))) {
				logger.debug("ClientAppInfoCache.init()...start......");
				
				IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
				
				List<ClientAppInfo> appInfoList = helper.getClientAppInfoList();
				Set<ClientAppInfo> tmpClientAppInfoSet = new HashSet<ClientAppInfo>();
				if(appInfoList != null && !appInfoList.isEmpty()) {
					for(ClientAppInfo info:appInfoList) {
						tmpClientAppInfoSet.add(info);
					}
				}
				initInProcess = true;
				this.clientAppInfoSet = tmpClientAppInfoSet;
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "ClientAppInfoCache.init()  - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nclientAppInfoSet Size = " + clientAppInfoSet.size() + "\nclientAppInfoSet = " + clientAppInfoSet ;

				logger.info(str);
				String info = "ClientAppInfoCache.init() - total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nclientAppInfoSet size = "+ clientAppInfoSet.size() ;
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

	public ClientAppInfo getClientAppInfo(String appCode) {
		if(appCode == null || this.clientAppInfoSet == null || this.clientAppInfoSet.isEmpty()) {
			return null;
		}
		waitForInit();
		for(ClientAppInfo info: this.clientAppInfoSet) {
			if(appCode.equalsIgnoreCase(info.getAppCode()) ) {
				return info;
			}
		}
		return null;
	}
	
	public Set <ClientAppInfo> getAllClientAppInfo() {
		waitForInit();
		if(this.clientAppInfoSet == null) {
			return null;
		}
		Set<ClientAppInfo> appSet = new HashSet<ClientAppInfo>(this.clientAppInfoSet);
		return appSet;
	}



}
