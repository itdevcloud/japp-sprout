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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.rest.PkiController;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to create/initiate async executors and shutdown created executors.
 * The client application should shutdown all executors when the client application is shutdown/stopped.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AsyncExecutorFactory  {

	//private static Logger logger = LogManager.getLogger(AsyncExecutorFactory.class);
	private static final Logger logger = LogManager.getLogger(AsyncExecutorFactory.class);

	private static Map<String, ThreadPoolExecutor> asyncExecutorMap;

	static {
		asyncExecutorMap = new HashMap<String, ThreadPoolExecutor>();
	}
	
	public static void initExecutors(List<ExecutorConfig> executorConfigList) {
		if(executorConfigList == null || executorConfigList.isEmpty()) {
			logger.warn("initExecutors() - executorConfigList parameter is null or empty, do nothing.......");
			return;
		}
		for(ExecutorConfig executorConfig:executorConfigList) {
			initExecutor(executorConfig);
		}
		return ;
	}
	
	public static void initExecutor(ExecutorConfig executorConfig) {
		if(executorConfig == null) {
			logger.error("initExecutor() - executorConfig parameter is null, do nothing.......");
			return;
		}
		String name = executorConfig.getName();
		if(name == null ||(name=name.trim().toLowerCase()).equals("")) {
			logger.error("initExecutor() - name is null or empty, return null......");
			return ;
		}

		int corePoolSize = executorConfig.getCorePoolSize();
		int maxPoolSize = executorConfig.getMaxPoolSize();
		int queueCapacity = executorConfig.getQueueCapacity();
		RejectedExecutionHandler handler = executorConfig.getHandler();
		boolean isScheduled = executorConfig.isScheduled();

		ThreadPoolExecutor executor = asyncExecutorMap.get(name);

		if(executor == null || executor.isShutdown()) {
			if(!isScheduled) {
				logger.info("Creating a new ThreadPoolExecutor, name = " + name);
				executor = new ThreadPoolExecutor(
						corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueCapacity)
						);
				if(handler != null) {
					executor.setRejectedExecutionHandler(handler);
				}
			}else {
				logger.info("Creating a new ScheduledThreadPoolExecutor, name = " + name);
				executor = new ScheduledThreadPoolExecutor(corePoolSize, handler);
			}
			asyncExecutorMap.put(name, executor);
		}else {
			logger.warn("a ThreadPoolExecutor (name = '" + name + "') has been inited already, do nothing......");
		}
		return ;
	}
	
	public static ExecutorService getActivePoolExecutor(String name) {
		if(asyncExecutorMap == null || asyncExecutorMap.isEmpty()) {
			logger.error("getActivePoolExecutor() - no any threadPoolExecutor has been inited, return null......");
			return null;
		}
		if(name == null ||(name=name.trim().toLowerCase()).equals("")) {
			logger.error("getActivePoolExecutor() - name is null or empty, return null......");
			return null;
		}
		ThreadPoolExecutor executor = asyncExecutorMap.get(name);
		if(executor == null || executor.isShutdown()) {
			logger.error("the ThreadPoolExecutor (name = '" + name + "') has not been inited or has been shutdown, return null......");
			asyncExecutorMap.remove(name);
			return null;
		}
		return executor;
	}

	public static void shutdownAllPoolExecutors (boolean force) {
		if(asyncExecutorMap == null || asyncExecutorMap.isEmpty()) {
			logger.error("shutdownAllPoolExecutors() - no active threadPoolExecutor, return null......");
			return;
		}
		Set<String> keySet = asyncExecutorMap.keySet();
		for(String name: keySet) {
			shutdownPoolExecutor (name, force);
		}
		return;
	}
	
	public static void shutdownPoolExecutor (List<String> nameList, boolean force) {
		if(nameList == null || nameList.isEmpty()) {
			logger.warn("shutdownPoolExecutor() - nameList parameter is null or empty, do nothing.......");
			return;
		}
		for(String name: nameList) {
			shutdownPoolExecutor (name,  force);
		}

	}
	
	public static void shutdownPoolExecutor (String name, boolean force) {
		if(name == null ||(name=name.trim().toLowerCase()).equals("")) {
			logger.error("shutdownPoolExecutor() - name is null or empty, return null......");
			return ;
		}
		ThreadPoolExecutor executor = asyncExecutorMap.get(name);
		if(executor == null || executor.isShutdown()) {
			logger.info("the ThreadPoolExecutor (name = '" + name + "') has not been inited or has been shutdown, do nothing......");
			asyncExecutorMap.remove(name);
			return;
		}
		if(force) {
			logger.info("force shutdown ThreadPoolExecutor (name = '" + name + "') ......");
			executor.shutdownNow();
		}else {
			logger.info("gracefult shutdown ThreadPoolExecutor (name = '" + name + "') ......");
			executor.shutdown();
		}
		asyncExecutorMap.remove(name);
	}

}