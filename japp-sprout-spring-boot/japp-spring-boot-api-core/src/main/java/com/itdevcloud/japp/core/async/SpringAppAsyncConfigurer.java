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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class SpringAppAsyncConfigurer implements AsyncConfigurer {

	private Logger logger = LogManager.getLogger(SpringAppAsyncConfigurer.class);

	@Value("${" + AppConfigKeys.JAPPCORE_ASYNC_EXECUTOR_CORE_POOL_SIZE + ":2}")
	private int corePoolSize;

	@Value("${" + AppConfigKeys.JAPPCORE_ASYNC_EXECUTOR_QUEUE_CAPACITY + ":4}")
	private int queueCapacity;

	@Value("${" + AppConfigKeys.JAPPCORE_ASYNC_EXECUTOR_MAX_POOL_SIZE + ":15}")
	private int maxPoolSize;

	@Value("${" + AppConfigKeys.JAPPCORE_ASYNC_EXECUTOR_REJECT_POLICY + ":default}")
	private String rejectPolicy;

	/**
	 * Override the Spring default Async Executor.
	 */
	@Bean("jappTaskExecutor")
	@Override
	public ThreadPoolTaskExecutor getAsyncExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		logger.info("getAsyncExecutor() - corePoolSize = " + corePoolSize + ", queueCapacity = " + queueCapacity + ", maxPoolSize = " + maxPoolSize);
		taskExecutor.setCorePoolSize(corePoolSize);
		taskExecutor.setQueueCapacity(queueCapacity);
		taskExecutor.setMaxPoolSize(maxPoolSize);
		taskExecutor.setThreadNamePrefix("japp-async-");
		if(AppConstant.ASYNC_EXECUTOR_REJECT_POLICY_CALLERRUN.equalsIgnoreCase(rejectPolicy)) {
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		}else if(AppConstant.ASYNC_EXECUTOR_REJECT_POLICY_REQUEUE.equalsIgnoreCase(rejectPolicy)) {
			taskExecutor.setRejectedExecutionHandler(new ReQueueRejectedExecutionHandler());
		}

		taskExecutor.initialize();
		return taskExecutor;
	}

}