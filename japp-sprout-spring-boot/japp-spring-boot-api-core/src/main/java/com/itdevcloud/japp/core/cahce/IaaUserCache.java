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

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class IaaUserCache extends RefreshableCache {

	// private static final Logger logger =
	// LogManager.getLogger(IaaUserCache.class);
	private static final Logger logger = LogManager.getLogger(IaaUserCache.class);

	// key login ID
	private static Map<String, AppIaaUser> iaaUserMap = null;

	@PostConstruct
	private void initService() {
	}

	@Override
	protected String createDisplayString() {
		String str = "IaaUserMap size = " + iaaUserMap.size();
		return str;
	}

	@Override
	protected void refreshCache() {
		try {
			iaaUserMap = new HashMap<String, AppIaaUser>();
		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

//	@Override
//	public void refreshCache() {
//		logger.info("refreshCache() - begin .........");
//		if (lastUpdatedTS != -1) {
//			List<String> iaaUserIdList = AppComponents.iaaService.getUpdatedIaaUsers(IaaUserCache.lastUpdatedTS);
//			if (iaaUserIdList != null && iaaUserIdList.size() > 0) {
//				logger.info("IaaUserCache - refresh UserInfoCache .........");
//
//				for (String id : iaaUserIdList) {
//					// remove changed login id from cache
//					removeIaaUserByUserId(id);
//				}
//				lastUpdatedTS = new Date().getTime();
//			}
//		}else {
//			initCache();
//		}
//		logger.info("refreshCache() - end .........");
//
//	}

	public void addIaaUser(AppIaaUser user) {
		if (user == null) {
			return;
		}
		String userId = user.getUserIaaUID();
		if (StringUtil.isEmptyOrNull(userId)) {
			return;
		}
		waitForInit();
		iaaUserMap.put(userId.toLowerCase(), user);
	}

	public AppIaaUser getIaaUserByUserId(String userId) {
		if (StringUtil.isEmptyOrNull(userId)) {
			return null;
		}
		waitForInit();
		AppIaaUser user = iaaUserMap.get(userId.toLowerCase());
		return user;
	}

	public void removeIaaUserByUserId(String userId) {
		if (StringUtil.isEmptyOrNull(userId)) {
			return;
		}
		waitForInit();
		iaaUserMap.remove(userId.toLowerCase());
	}

}
