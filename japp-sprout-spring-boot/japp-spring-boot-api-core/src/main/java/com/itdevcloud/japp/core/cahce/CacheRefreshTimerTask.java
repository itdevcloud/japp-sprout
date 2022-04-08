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

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.ConfigFactory;
/**
 * This class is used to refresh various caches based on pre-defined schedules.
 *
 * It provides the support for daily cache refresh (reload all data) and 
 * regular cache refresh (only load the latest updated data since lastUpdateTimeStamp).
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class CacheRefreshTimerTask {

	private static final Logger logger = LogManager.getLogger(CacheRefreshTimerTask.class);
	private static String dailyRefreshDate = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


	//private List<RefreshableCache> cacheList;

//	@Autowired
//	public CacheRefreshTimerTask(List<RefreshableCache> cacheList) {
//		cacheList = cacheList;
//	}

	@PostConstruct
	public void init() {
	}

	@Scheduled(fixedRateString = "${jappcore.cache.refresh.interval:600000}")
	public void run() {
		
		List<RefreshableCache> cacheList = AppFactory.getRefreshableCacheList();
		
		logger.info("CacheRefreshTimerTask.run() - started......");
		if(cacheList == null || cacheList.isEmpty()) {
			logger.debug("CacheRefreshTimerTask - cacheList is null or empty, do nothing...");
			return;
		}else {
			String str = "CacheRefreshTimerTask.run() - cacheList size = " + cacheList.size();
			for (RefreshableCache cache : cacheList) {
				str = str + "\n - "+cache.getCacheSimpleName();
			}
			logger.info(str);
		}
		boolean enableCacheDailyRefresh = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_CACHE_DAILY_REFRESH_ENABLED);
		long start = System.currentTimeMillis();

		Date now = new Date();
		long nowTS = now.getTime();
		String today = sdf.format(now);
		int refreshDateInt = (dailyRefreshDate == null ? 0 : Integer.parseInt(dailyRefreshDate));
		int todayInt = Integer.parseInt(today);

		if (enableCacheDailyRefresh && (dailyRefreshDate == null || refreshDateInt < todayInt)) {
			logger.info("CacheRefreshTimerTask.run() - Daily refresh start............");

			for (RefreshableCache cache : cacheList) {
				// force to refresh anyway by set lastUpdatedTS = -1
				cache.setLastUpdatedTS(-1);
				
				logger.debug(cache.getCacheSimpleName() + ".init()......begin......order="+ cache.getInitOrder()+"......");

				cache.initCache();

				Date end = new Date();
				long endTS = end.getTime();
				cache.setLastUpdatedTS(endTS);

				logger.debug(cache.getCacheSimpleName() + ".init()......end...........");
			}

			dailyRefreshDate = today;

			long end1 = System.currentTimeMillis();
			logger.info("CacheRefreshTimerTask.run() - Daily refresh End...... took " + (end1 - start) + " ms.");
		} else {
			for (RefreshableCache cache : cacheList) {
				long lastUpdatedTS = cache.getLastUpdatedTS();
				if (((nowTS - lastUpdatedTS) >= (ConfigFactory.appConfigService
						.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL)*1000*60))) {
					logger.debug(cache.getCacheSimpleName() + ".refresh()......begin...........");
					cache.refreshCache();
					Date end = new Date();
					long endTS = end.getTime();
					cache.setLastUpdatedTS(endTS);
					logger.debug(cache.getCacheSimpleName() + ".refresh()......end...........");
				}
			}
		}
		logger.info("CacheRefreshTimerTask.run() - end.........");

	}
	
}