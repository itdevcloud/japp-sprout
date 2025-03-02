package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.Date;
import java.util.logging.Logger;

public class EventProcessor {
	private static final Logger logger = Logger.getLogger(EventProcessor.class.getName());

	public EventProcessStatus processEvent(EventInfo eventInfo) {
		if(eventInfo == null) {
			logger.info("eventLockInfo object is null, do nothing......" );
			return null;
		}
		try {
			logger.info("processing start...... the event: " + eventInfo);
			EventProcessStatus eventProcessStatus = new EventProcessStatus();
			eventProcessStatus.setProcessStatus(EventManagerConstant.EVENT_PROCESS_STATUS_SUCCESS);
			logger.info("processing end...... the event: " + eventInfo);
			return eventProcessStatus;
		// make sure value are right
		}catch(Throwable t) {
			logger.severe("can not process the event successfully, error: " + t);
			t.printStackTrace();
		}
		return null;
	}
}
