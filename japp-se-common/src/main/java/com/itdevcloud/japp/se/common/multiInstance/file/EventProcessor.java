package com.itdevcloud.japp.se.common.multiInstance.file;

import java.util.logging.Logger;

public class EventProcessor {
	private static final Logger logger = Logger.getLogger(EventProcessor.class.getName());

	public boolean processEvent(EventLockInfo eventLockInfo) {
		if(eventLockInfo == null) {
			logger.info("eventLockInfo object is null, do nothing......" );
			return false;
		}
		try {
			logger.info("processing the event: " + eventLockInfo);
		}catch(Throwable t) {
			logger.severe("can not process the event successfully, error: " + t);
			t.printStackTrace();
		}
		return true;
	}
}
