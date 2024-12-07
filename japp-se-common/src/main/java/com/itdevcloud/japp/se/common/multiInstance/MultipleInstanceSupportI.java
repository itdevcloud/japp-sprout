package com.itdevcloud.japp.se.common.multiInstance;

import java.util.List;

public interface MultipleInstanceSupportI {

	
	String addLock(String taskName, String key);
	boolean releaseLock(String taskName, String key);
	String broadcastEventMessage(String eventName, String key, String message);
	List<EventStatus> inquiryEventStatus(String eventMessageId);
	
}
