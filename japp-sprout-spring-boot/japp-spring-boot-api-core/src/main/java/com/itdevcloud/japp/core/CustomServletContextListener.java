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
package com.itdevcloud.japp.core;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.itdevcloud.japp.core.async.AsyncExecutorFactory;

/**
 * A custom ServletContextListener class which defines servlet context initialize and destroy tasks.
 * e.g. shutdown All async Executors when the servlet context is destroyed.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Configuration
public class CustomServletContextListener implements ServletContextListener {

	@Autowired
	private ThreadPoolTaskExecutor  jappTaskExecutor;

	@Override
	public void contextInitialized(ServletContextEvent context) {
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent context) {
		jappTaskExecutor.destroy();
		AsyncExecutorFactory.shutdownAllPoolExecutors(true);
	}

}
