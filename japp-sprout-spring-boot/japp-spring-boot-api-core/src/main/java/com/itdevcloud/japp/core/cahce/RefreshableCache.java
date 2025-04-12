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

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.DateUtils;

/**
 * All sub-classes of this class will be picked up by CacheRefreshTimerTask.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public abstract class RefreshableCache implements AppFactoryComponentI {

	private static final Logger logger = LogManager.getLogger(RefreshableCache.class);

	private static final int systemMinIntervalMin = 3;
	
	protected boolean refreshInProcess = false;
	private long lastUpdatedTS = -1;

	private int minInterval = -1;
	private int interval = -1;
	private boolean inited = false;

	public long getLastUpdatedTS() {
		return lastUpdatedTS;
	}

	public void setLastUpdatedTS(long lastUpdatedTS) {
		this.lastUpdatedTS = lastUpdatedTS;
	}

	public String getCacheSimpleName() {
		return this.getClass().getSimpleName();
	}
	
	public int getRefreshInterval() {
		return interval;
	}

	public void init() {
		if (!inited) {
			if (minInterval >= systemMinIntervalMin && interval >= minInterval) {
				return;
			}
			minInterval = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL_MIN);
			if (minInterval < systemMinIntervalMin) {
				// at least 5 mins interval
				minInterval = systemMinIntervalMin;
			}
			interval = ConfigFactory.appConfigService.getPropertyAsInteger(
					AppConfigKeys.JAPPCORE_CACHE_REFRESH_INTERVAL_MIN + "." + getCacheSimpleName());
			if (interval < minInterval) {
				// at least 5 mins interval
				interval = minInterval;
			}
			inited = true;
			logger.info(
					"initInterval() - end: " + getCacheSimpleName() + ", interval = " + interval + ", minInterval = "
							+ minInterval + ", systemMinIntervalMin = " + systemMinIntervalMin + "..........");
		}
	}

	public synchronized void refresh() {

		try {
			init();
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= (interval * 60 * 1000))) {

				logger.info(getCacheSimpleName() + ".refresh() - start......interval = " + interval + " mins. start TS = " 
				+ DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));

				refreshInProcess = true;

				refreshCache();

				refreshInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();

				lastUpdatedTS = endTS;

				String str = getCacheSimpleName() + ".refresh() - end...End TS = " + DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss.SSS") 
				+ ", total time = " + (endTS - startTS) + " millis. Result:" + createDisplayString() ;
				logger.info(str);
			} 
		} catch (Throwable t) {
			String err = getCacheSimpleName() + ".refresh() wirh error: " + t;
			logger.error(err, t);
		} finally {
			refreshInProcess = false;
		}
	}

	protected abstract void refreshCache();

	protected abstract String createDisplayString();

	// use by get method
	protected void waitForInit() {
		if (refreshInProcess) {
			for (int i = 0; i < 30; i++) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					logger.warn(e);
				}
				if (!refreshInProcess) {
					break;
				}
			} // end for
			if (refreshInProcess) {
				String str = getCacheSimpleName() + ".waitForInit() - end...TS = "+ DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss.SSS") ;
				logger.error(str);
				throw new RuntimeException(getCacheSimpleName() + " is in refresh() process, please wait!");
			}
		}
	}

}
