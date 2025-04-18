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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.KeyVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class IaaAppInfoCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(IaaAppInfoCache.class);

	private static Map<String, IaaAppVO> appInfoMap;

	@PostConstruct
	private void initService() {
	}

	
	@Override
	protected String createDisplayString() {
		String str = CommonUtil.mapToString(appInfoMap, 0);
		return str;
	}

	@Override
	protected void refreshCache() {
		try {
			Map<String, IaaAppVO> tmpMap = new HashMap<String, IaaAppVO>();
			List <IaaAppVO> tmpList = AppComponents.iaaService.getIaaAppInfo();
			
			//logger.info("...........tmpList......." + CommonUtil.listToString(tmpList));
			
			if(tmpList == null || tmpList.isEmpty()) {
				logger.warn("AppInfo List is null or empty, will not update exisitng cache value.......");
				return;
			}
			for (IaaAppVO appvo: tmpList) {
				tmpMap.put(appvo.getAppId().toUpperCase(), appvo);
			}
			
			appInfoMap = Collections.unmodifiableMap(tmpMap);


		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public IaaAppVO getIaaAppInfo(String appId) {
		if(StringUtil.isEmptyOrNull(appId)) {
			return null;
		}
		waitForInit();
		return appInfoMap.get(appId.toUpperCase());
	}


}
