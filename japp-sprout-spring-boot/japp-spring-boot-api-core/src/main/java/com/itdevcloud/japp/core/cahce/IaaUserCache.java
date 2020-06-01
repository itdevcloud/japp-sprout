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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class IaaUserCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(IaaUserCache.class);
	// key login ID
	private static Map<String, IaaUser> IaaUserMap = null;

	@PostConstruct
	private void initService() {
	}

	@Override
	public synchronized void initCache() {
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL))) {
				logger.debug("IaaUserCache.init() - begin...........");

				initInProcess = true;
				IaaUserMap = new HashMap<String, IaaUser>();
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "IaaUserCache.init() - end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nIaaUserMap size = " + IaaUserMap.size() + "\n";

				logger.info(str);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			initInProcess = false;
		}
	}

	@Override
	public void refreshCache() {
		logger.info("refreshCache() - begin .........");
		if (lastUpdatedTS != -1) {
			List<String> iaaUserIdList = AppComponents.iaaService.getUpdatedIaaUsers(IaaUserCache.lastUpdatedTS);
			if (iaaUserIdList != null && iaaUserIdList.size() > 0) {
				logger.info("IaaUserCache - refresh UserInfoCache .........");

				for (String id : iaaUserIdList) {
					// remove changed login id from cache
					removeIaaUserByUserId(id);
				}
				lastUpdatedTS = new Date().getTime();
			}
		}else {
			initCache();
		}
		logger.info("refreshCache() - end .........");

	}

	public void addIaaUser(IaaUser user) {
		if (user == null) {
			return;
		}
		String userId = user.getUserId();
		if (StringUtils.isEmpty(userId)) {
			return;
		}
		waitForInit();
		IaaUserMap.put(userId.toLowerCase(), user);
	}
	
	public IaaUser getIaaUserByUserId(String userId) {
		if (StringUtils.isEmpty(userId)) {
			return null;
		}
		waitForInit();
		IaaUser user = IaaUserMap.get(userId.toLowerCase());
		return user;
	}

	public void removeIaaUserByUserId(String userId) {
		if (StringUtils.isEmpty(userId)) {
			return;
		}
		waitForInit();
		IaaUserMap.remove(userId.toLowerCase());
	}

}
