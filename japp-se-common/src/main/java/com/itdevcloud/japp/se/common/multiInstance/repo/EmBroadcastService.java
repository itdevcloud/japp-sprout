package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class EmBroadcastService {
	
	private static final JulLogger logger = JulLogger.getLogger(EmBroadcastService.class.getName());
	
	private RepoBaseMiEventManager manager = null;
	
	
	public EmBroadcastService(RepoBaseMiEventManager manager) {
		if(manager == null) {
			throw new RuntimeException("EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}

	protected List<EventInfo> getUnProcessedBroadcastEventInfoList() {

		logger.fine("getUnProcessedBroadcastEventInfoList start........");
		Long startTS = System.currentTimeMillis();

		long eventLastUpdateTime = this.manager.getEventLastUpdateTimeFromRepo(EventManagerConstant.EVENT_TYPE_BROADCAST);
		List<EventInfo> list = new ArrayList<EventInfo>();
		if (this.manager.getLastProcessTimestamp() == -1 || this.manager.getLastProcessTimestamp() < eventLastUpdateTime) {
			List<EventInfo> activeEventInfoList = this.manager.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_BROADCAST);

			for (EventInfo info : activeEventInfoList) {
				Date eventDate = info.getEventDate();
				if (eventDate != null && (eventDate.getTime() > this.manager.getMonitorStartTimestamp())
						&& !this.manager.getProcessedEventList().contains(info)) {
					list.add(info);
				}
			}
		}
		Long endTS = System.currentTimeMillis();
		logger.fine("getUnProcessedBroadcastEventInfoList end: list size = " + list.size() + ", total millis = "
				+ (endTS - startTS));
		return list;
	}

	
	protected boolean broadcastEventMessage(String eventName, String message) {
		logger.info("broadcastEventMessage start........");
		Long startTS = System.currentTimeMillis();

		if (StringUtil.isEmptyOrNull(eventName)) {
			return false;
		}
		message = StringUtil.isEmptyOrNull(message) ? EventManagerConstant.EVENT_INFO_EMPTY_CONTENT : message.trim();
		if (this.manager.appRepoLockService.aquireAppRepoLock(EventManagerConstant.EVENT_TYPE_BROADCAST)) {
			try {
				EventInfo eventInfo = new EventInfo(this.manager.getAppName(), eventName, this.manager.getMyInstanceName());
				eventInfo.setContent(message);
				List<EventInfo> infoList = this.manager.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_BROADCAST);
				if (infoList == null) {
					infoList = new ArrayList<EventInfo>();
				}
				infoList.add(eventInfo);
				if (this.manager.repoService.saveEventInfoListToRepo(infoList, EventManagerConstant.EVENT_TYPE_BROADCAST, false)) {
					
					if(!this.manager.setEventLastUpdateTimeInRepo(EventManagerConstant.EVENT_TYPE_BROADCAST)) {
						logger.fine("broadcastEventMessage broadcast success, but update EventLastUpdateTime failed.....return <true>");
					}
					Long endTS = System.currentTimeMillis();
					logger.info("broadcastEventMessage end: return <true>, total millis = " + (endTS - startTS) + ", " + eventInfo);
					return true;
				} else {
					Long endTS = System.currentTimeMillis();
					logger.info("broadcastEventMessage end: return <false>, total millis = " + (endTS - startTS));
					return false;
				}
			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(EventManagerConstant.EVENT_TYPE_BROADCAST);
			}

		} else {
			Long endTS = System.currentTimeMillis();
			logger.info("broadcastEventMessage end: Can not get APP Repo Lock. return false, total millis = "
					+ (endTS - startTS));
			return false;
		}
	}

}
