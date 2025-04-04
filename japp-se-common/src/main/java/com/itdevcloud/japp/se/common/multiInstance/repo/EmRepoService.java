package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.itdevcloud.japp.se.common.service.JulLogger;

public class EmRepoService {
	
	private static final JulLogger logger = JulLogger.getLogger(EmRepoService.class.getName());
	
	private RepoBaseMiEventManager manager = null;
	
	
	public EmRepoService(RepoBaseMiEventManager manager) {
		if(manager == null) {
			throw new RuntimeException("EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}


	protected List<EventInfo> getEventInfoListFromRepo(String eventType) {
		logger.fine("getEventInfoListFromRepo start........");
		Long startTS = System.currentTimeMillis();

		String repoName = this.manager.getRepoName(EventManagerConstant.REPO_TYPE_CONTENT, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine("getEventInfoListFromRepo end: repoName is null, return <null>, total millis = " + (endTS - startTS));
			return null;
		}
		List <String> contentList = this.manager.getContentFromRepo(repoName);
		if (contentList == null || contentList.isEmpty()) {
			Long endTS = System.currentTimeMillis();
			logger.fine("getEventInfoListFromRepo end: repoName = " + repoName + ", Content List Size = 0, total millis = " + (endTS - startTS));
			return null;
		}
		EventInfo eventInfo = this.manager.createEventInfo(eventType);
		EventInfo redisEventInfo = null;
		List<EventInfo> infoList = new ArrayList<EventInfo>();
		Date now = new Date();
		for (int i = 0; i < contentList.size(); i++) {
			
			//logger.info("content ("+i+") = "+ contentList.get(i));
			
			redisEventInfo = eventInfo.createEventInfoFromString(contentList.get(i));
			if (redisEventInfo != null && now.before(redisEventInfo.getExpiryDate())) {
				infoList.add(redisEventInfo);
			}
		}
		Long endTS = System.currentTimeMillis();
		logger.fine("getEventInfoListFromRepo end: repoName = " + repoName + ", Content List Size = " + contentList.size() + ", EverntInfo List Size = " + infoList.size()
				+ ", total millis = " + (endTS - startTS));
		return infoList;
	}

	public boolean saveEventInfoListToRepo(List<EventInfo> infoList, String eventType, boolean append) {
		logger.fine("saveEventInfoListToRepo start........");
		Long startTS = System.currentTimeMillis();
		String repoName = this.manager.getRepoName(EventManagerConstant.REPO_TYPE_CONTENT, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine(
					"saveEventInfoListToRepo end: blobName is null, return false, total millis = " + (endTS - startTS));
			return false;
		}
		List<String> contentList = new ArrayList<String>();
		EventInfo eventInfo = null;
		String value = null;
		Date now = new Date();
		if (infoList != null && !infoList.isEmpty()) {
			for (int i = 0; i < infoList.size(); i++) {
				eventInfo = infoList.get(i);
				if (eventInfo != null && now.before(eventInfo.getExpiryDate())) {
					value = eventInfo.getEventInfoString();
					if (value != null) {
						contentList.add(value);
					}
				}
			}
		}
		boolean result = this.manager.saveContentToRepo(repoName, contentList, append);
		Long endTS = System.currentTimeMillis();
		logger.fine("saveEventInfoListToRepo end: repoName = " + repoName + ", EventInfo List Size = " + infoList.size() + ", Content List Size = " + contentList.size()
				+ ", return " + result + ", total millis = " + (endTS - startTS));
		return result;
	}

}
