package com.itdevcloud.japp.core.async;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;


/**
 * The ReQueueRejectedExecutionHandler class is a pre-defined rejection handler.
 * This handler just simply put the rejected runnable back to the queue.
 * <p>
 * Keep in mind - this may not be the recommend way but sometimes it is useful. Please investigate a better way.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class ReQueueRejectedExecutionHandler implements RejectedExecutionHandler {

	private static final Logger logger = LogManager.getLogger(ReQueueRejectedExecutionHandler.class);

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		if (executor != null && !executor.isShutdown()) {
			try {
				executor.getQueue().put(r);
			} catch (Throwable t) {
				logger.error("ReQueueRejectedExecutionHandler(0 reQueue failed with error: " + t, t);
				//throw AppUtil.throwRuntimeException(t);
			}
		}
	}

}
