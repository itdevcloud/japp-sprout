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
package com.itdevcloud.japp.core.async;

import java.util.concurrent.RejectedExecutionHandler;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class ExecutorConfig {
	private String name;
	private boolean isScheduled;
	private int corePoolSize;
	private int maxPoolSize;
	private int queueCapacity;
	RejectedExecutionHandler handler;


	public ExecutorConfig(String name) {
		if(name == null || (name=name.trim()).equals("")) {
			throw new RuntimeException("name can not be null or empty when constructing ExecutorConfig instance, check code!");
		}
		this.name = name;
		this.corePoolSize = 2;
		this.maxPoolSize = 15;
		this.queueCapacity = 4;
		this.isScheduled = false;
		this.handler = null;

	}


	public ExecutorConfig(String name, boolean isScheduled, int corePoolSize, int maxPoolSize, int queueCapacity,
			RejectedExecutionHandler handler) {
		if(name == null || (name=name.trim()).equals("")) {
			throw new RuntimeException("name can not be null or empty when constructing ExecutorConfig instance, check code!");
		}
		this.name = name;
		this.isScheduled = isScheduled;
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.queueCapacity = queueCapacity;
		this.handler = handler;
	}


	public String getName() {
		return name;
	}

	public boolean isScheduled() {
		return isScheduled;
	}
	public void setScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}
	public int getCorePoolSize() {
		if(corePoolSize <=0) {
			corePoolSize = 1;
		}
		return corePoolSize;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	public int getMaxPoolSize() {
		if( maxPoolSize < getCorePoolSize()) {
			maxPoolSize = getCorePoolSize();
		}
		return maxPoolSize;
	}
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	public int getQueueCapacity() {
		if(queueCapacity <=0) {
			queueCapacity = 2;
		}
		return queueCapacity;
	}
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	public RejectedExecutionHandler getHandler() {
		return handler;
	}
	public void setHandler(RejectedExecutionHandler handler) {
		this.handler = handler;
	}




}
