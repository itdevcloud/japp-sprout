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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;

import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 * This class is used to refresh various caches based on pre-defined schedules.
 *
 * It provides the support for daily cache refresh (reload all data) and regular
 * cache refresh (only load the latest updated data since lastUpdateTimeStamp).
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class CacheRefreshTimerTask {

	private static final Logger logger = LogManager.getLogger(CacheRefreshTimerTask.class);

	private static String dailyRefreshDate = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	private List<RefreshableCache> cacheList;

	@Autowired
	public CacheRefreshTimerTask(List<RefreshableCache> cacheList) {
		this.cacheList = cacheList;

		// spring boot container only has one instance, otherwise the instances in the
		// list are different than the instance in AppCompont object
		// following code can approve it:
//		for (RefreshableCache cache : cacheList) {
//			RefreshableCache tmpCache = AppFactory.getComponent(cache.getClass());
//			if (tmpCache == null) {
//				logger.error("No Instance found in AppFactory - " + cache.getClass().getSimpleName()
//						+ " instance not found in AppFactory!");
//			} else if (cache == tmpCache) {
//				logger.info("RefreshableCache - " + cache.getClass().getSimpleName()
//						+ " instances are identical instances!");
//			} else {
//				logger.error(
//						"RefreshableCache - " + cache.getClass().getSimpleName() + " instances are differe instances!");
//			}
//		}

	}

	@PostConstruct
	public void init() {
	}

	private String getCacheListInfoString() {
		StringBuffer sb = new StringBuffer();
		if (cacheList == null || cacheList.isEmpty()) {
			sb.append("CacheRefreshTimerTask - cacheList is null or empty, do nothing......");
		} else {
			sb.append("CacheRefreshTimerTask.run() - cacheList size = " + cacheList.size());
			for (RefreshableCache cache : cacheList) {
				// make sure load interval configuration
				cache.init();
				sb.append("\n - " + cache.getCacheSimpleName() + ", Refresh Interval = " + cache.getRefreshInterval()
						+ " mins......");
			}
		}
		return sb.toString();
	}

	// 10 mins
	@Scheduled(fixedDelayString = "${jappcore.cache.refresh.least.interval.:10}", timeUnit = TimeUnit.MINUTES)
	public void run() {
		boolean enableCacheDailyRefresh = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_CACHE_DAILY_REFRESH_ENABLED);

		Date now = new Date();
		long nowTS = now.getTime();
		String today = sdf.format(now);
		int refreshDateInt = (dailyRefreshDate == null ? 0 : Integer.valueOf(dailyRefreshDate));
		int todayInt = Integer.valueOf(today);

		if (enableCacheDailyRefresh && (dailyRefreshDate == null || refreshDateInt < todayInt)) {
			logger.info("CacheRefreshTimerTask.run() - Daily refresh start............");
			logger.info(getCacheListInfoString());

			for (RefreshableCache cache : cacheList) {
				// force to refresh anyway by set lastUpdatedTS = -1
				cache.setLastUpdatedTS(-1);
				cache.refresh();
			}
			dailyRefreshDate = today;
			Date end = new Date();
			long endTS = end.getTime();
			logger.info("CacheRefreshTimerTask.run() - Daily refresh end........ took " + (endTS - nowTS) + " ms.");
		} else {
			for (RefreshableCache cache : cacheList) {
				cache.refreshCache();
			}
		}

	}
}