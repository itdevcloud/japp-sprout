package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class EmAppRepoLockService {
	
	private static final Logger logger = Logger.getLogger(EmAppRepoLockService.class.getName());
	
	private RepoBaseMiEventManager manager = null;
	
	
	public EmAppRepoLockService(RepoBaseMiEventManager manager) {
		if(manager == null) {
			throw new RuntimeException("EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}

	/**
	 * Aquire Lock to manage corresponding (eventType related) Repo Content
	 * only one instance can get the lock (one line/eventInfo string in the repo only)
	 * @param eventType
	 * @return
	 */
	boolean aquireAppRepoLock(String eventType) {
		
		if(!this.manager.useAppRepoLock()) {
			logger.fine("Use AppRepoLock is not required for " + this.manager.getClass().getSimpleName() + " - AppName = " + this.manager.getAppName() + "........");
			return true;
		}
		logger.fine("aquireAppRepoLock start........");
		Long startTS = System.currentTimeMillis();

		EventInfo myLockEventInfo = new EventInfo(this.manager.getAppName(), EventManagerConstant.EVENT_NAME_REPO_LOCK,
				this.manager.getMyInstanceName());
		EventInfo appRepoEventInfo = null;

		int wait = this.manager.getAppRepoLockRetryIntervalMillis();
		int retryCount = this.manager.getAppRepoLockTimeoutMillis() / wait;
		retryCount = (retryCount <= 0 ? 1 : retryCount);
		Date now = new Date();

		logger.fine("aquireAppRepoLock RetryCount = " + retryCount);

		for (int i = 0; i < retryCount; i++) {
			if(i != 0) {
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
				}
			}
			appRepoEventInfo = this.getAppRepoLockEventInfo(eventType);
			if (appRepoEventInfo == null || appRepoEventInfo.getExpiryDate().before(now)) {
				// no repo lock find or the lock is expired, try to
				// add my lock
				this.setAppRepoLockEventInfo(eventType, myLockEventInfo);
				try {
					Thread.sleep(wait);
				} catch (Throwable t) {
				}
				appRepoEventInfo = this.getAppRepoLockEventInfo(eventType);
				if (appRepoEventInfo == null) {
					// failed, retry
					continue;
				} else {
					if (myLockEventInfo.getSource().equalsIgnoreCase(appRepoEventInfo.getSource())) {
						Long endTS = System.currentTimeMillis();
						logger.fine("aquireAppRepoLock end: <Success>.....retry count=" + i + ", total millis = "
								+ (endTS - startTS));
						return true;
					} else {
						// try to wait the lock is released by other instance
						continue;
					}
				}
			} else {
				if (myLockEventInfo.getSource().equalsIgnoreCase(appRepoEventInfo.getSource())) {
					Long endTS = System.currentTimeMillis();
					logger.fine("getAppRepoLock end: <Success>.....retry count=" + i + ", total millis = "
							+ (endTS - startTS));
					return true;
				} else {
					//logger.fine("getAppRepoLock wait for lock release by other instance....retry count=" + i );
					continue;
				}
			}
		}
		Long endTS = System.currentTimeMillis();
		logger.info(
				"aquireAppRepoLock end: <Fail>.....retry count=" + retryCount + ", total millis = " + (endTS - startTS));
		return false;
	}

	boolean releaseAppRepoLock(String eventType) {

		if(!this.manager.useAppRepoLock()) {
			logger.fine("useAppRepoLock is not required for " + this.getClass().getSimpleName() + " - AppName = " + this.manager.getAppName() + "........");
			return true;
		}
		logger.fine("releaseAppRepoLock start........");
		Long startTS = System.currentTimeMillis();

		EventInfo myLockEventInfo = new EventInfo(this.manager.getAppName(), EventManagerConstant.EVENT_NAME_REPO_LOCK,
				this.manager.getMyInstanceName());
		EventInfo appRepoEventInfo = this.getAppRepoLockEventInfo(eventType);
		Date now = new Date();
		if (appRepoEventInfo == null || appRepoEventInfo.getExpiryDate().before(now)) {
			// no repo lock find or key format is not correct or the lock is expired, try to
			// add my lock
			Long endTS = System.currentTimeMillis();
			logger.fine(
					"releaseAppRepoLock end: no active lock find, return <true>, total millis = " + (endTS - startTS));
			return true;
		} else {
			if (myLockEventInfo.getSource().equalsIgnoreCase(appRepoEventInfo.getSource())) {
				boolean result = this.setAppRepoLockEventInfo(eventType, null);
				Long endTS = System.currentTimeMillis();
				logger.fine("releaseAppRepoLock end: return <" + result + ">, total millis = " + (endTS - startTS));
				return result;
			} else {
				// not locked by me
				Long endTS = System.currentTimeMillis();
				logger.fine("releaseAppRepoLock end: not my lock, return <true>, total millis = " + (endTS - startTS));
				return true;
			}
		}
	}

	private EventInfo getAppRepoLockEventInfo(String eventType) {
		logger.fine("getAppRepoLockEventInfo start........");
		Long startTS = System.currentTimeMillis();
		
		String repoName = this.manager.getRepoName(EventManagerConstant.REPO_TYPE_REPO_LOCK, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.info("getAppRepoLockEventInfo end: repoName is null, return <null>, total millis = " + (endTS - startTS));
			return null;
		}

		List<String> repoContentList = this.manager.getContentFromRepo(repoName);
		String repoContent = (repoContentList == null || repoContentList.isEmpty())?null:repoContentList.get(0);
		
		EventInfo eventInfo = new EventInfo("");
		EventInfo fileEventInfo = eventInfo.createEventInfoFromString(repoContent);
		
		logger.fine("getAppRepoLockEventInfo() repo lock = " + fileEventInfo +"........");
		return fileEventInfo;
	}

	private boolean setAppRepoLockEventInfo(String eventType, EventInfo eventInfo) {
		logger.fine("setAppRepoLockEventInfo start........");
		Long startTS = System.currentTimeMillis();
		String repoContent = null;
		if(eventInfo == null) {
			repoContent = " ";
		}else {
			repoContent = eventInfo.getEventInfoString();
		}
		List<String> contentList = new ArrayList<String>();
		contentList.add(repoContent);
		
		String repoName = this.manager.getRepoName(EventManagerConstant.REPO_TYPE_REPO_LOCK, eventType);
		if (repoName == null) {
			// repo type and/or event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine("setAppRepoLockEventInfo end: repoName is null, return <false>, total millis = " + (endTS - startTS));
			return false;
		}
		boolean result = this.manager.saveContentToRepo(repoName, contentList, false);
		
		logger.fine("setAppRepoLockEventInfo() repo lock = " + eventInfo + ", result = <" + result +">........");
		return result;
	}


}
