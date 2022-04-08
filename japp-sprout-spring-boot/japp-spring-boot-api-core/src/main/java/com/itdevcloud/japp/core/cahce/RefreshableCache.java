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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;

/**
 * All sub-classes of this class will be picked up by CacheRefreshTimerTask.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public abstract class RefreshableCache implements AppFactoryComponentI{

	private static final Logger logger = LogManager.getLogger(RefreshableCache.class);

	protected  boolean initInProcess = false;
	protected  long lastUpdatedTS = -1;
	
	public  long getLastUpdatedTS() {
		return lastUpdatedTS;
	}

	public  void setLastUpdatedTS(long lastUpdatedTS) {
		this.lastUpdatedTS = lastUpdatedTS;
	}

	public String getCacheSimpleName() {
		return this.getClass().getSimpleName() + ", init order: " + getInitOrder();
	}

	public abstract void initCache();
	public abstract void refreshCache();
	public abstract String getInitOrder();

	protected void waitForInit() {
		if (initInProcess) {
			for (int i = 0; i < 5; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.warn(e);
				}
				if (!initInProcess) {
					break;
				}
			} // end for
			if (initInProcess) {
				throw new RuntimeException(getCacheSimpleName() + " is in init() process, please wait!");
			}
		}
	}

}
