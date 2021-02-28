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
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;


/**
 * The ReQueueRejectedExecutionHandler class is a pre-defined rejection handler.
 * This handler just simply put the rejected runnable back to the queue.
 * 
 * Keep in mind - this may not be the recommend way but sometimes it is useful. Please investigate a better way.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class ReQueueRejectedExecutionHandler implements RejectedExecutionHandler {

	private static Logger logger = LogManager.getLogger(ReQueueRejectedExecutionHandler.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if (executor != null && !executor.isShutdown()) {
			try {
				executor.getQueue().put(r);
			} catch (Throwable t) {
				logger.error(CommonUtil.getStackTrace(t));
				CommonUtil.throwRuntimeException(t);
			}
		}
	}

}
