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

public class EmTaskLockService {
	
	private static final Logger logger = Logger.getLogger(EmTaskLockService.class.getName());
	
	private RepoBaseMiEventManager manager = null;
	
	
	public EmTaskLockService(RepoBaseMiEventManager manager) {
		if(manager == null) {
			throw new RuntimeException("EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}

	public EventInfo addTaskLock(String lockName, int expireMinutes) {
		logger.info("addTaskLock start........");
		Long startTS = System.currentTimeMillis();

		Date now = new Date();
		if (expireMinutes <= 0) {
			expireMinutes = 5;
		}
		lockName = StringUtil.isEmptyOrNull(lockName) ? ("LOCK-" + this.manager.getAppName().trim().toUpperCase())
				: lockName.trim().toUpperCase();
		Date expireDate = DateUtils.addTime(now, Calendar.MINUTE, expireMinutes);

		EventInfo lockEventInfo = new EventInfo(this.manager.getAppName(), null, EventManagerConstant.EVENT_NAME_LOCK,
				this.manager.getMyInstanceName(), now, expireDate);
		lockEventInfo.setContent(lockName);
		
		logger.info("addTaskLock() - " + lockEventInfo);

		EventInfo lockEvent = null;
		EventInfo myLockEvent = null;
		List<EventInfo> newInfoList = new ArrayList<EventInfo>();
		if (this.manager.appRepoLockService.aquireAppRepoLock(EventManagerConstant.EVENT_TYPE_LOCK)) {
			try {
				List<EventInfo> infoList = this.manager.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_LOCK);
				if (infoList != null && !infoList.isEmpty()) {
					for (EventInfo info : infoList) {
						if (lockName.equalsIgnoreCase(info.getContent())) {
							if (this.manager.getMyInstanceName().equalsIgnoreCase(info.getSource())) {
								if (lockEvent == null) {
									// update my lock
									if (info.getExpiryDate().before(lockEventInfo.getExpiryDate())) {
										myLockEvent = lockEventInfo;
									} else {
										myLockEvent = info;
									}
									lockEvent = myLockEvent;
									newInfoList.add(info);
								} else {
									// duplicate detected, remove this info when save to Repo
								}

							} else {
								if (lockEvent == null) {
									myLockEvent = null;
									lockEvent = info;
									newInfoList.add(info);
								} else {
									// duplicate detected, remove this info when save to Repo
								}
							}
						} else {
							// other lock, keep it
							newInfoList.add(info);
						}
					} // end for
				}
				if (lockEvent == null) {
					newInfoList.add(lockEventInfo);
				}
				// save and clean locks
				this.manager.repoService.saveEventInfoListToRepo(newInfoList, EventManagerConstant.EVENT_TYPE_LOCK, false);
				// double check
				infoList = this.manager.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_LOCK);
				if (infoList != null && !infoList.isEmpty()) {
					for (EventInfo info : infoList) {
						if (lockName.equalsIgnoreCase(info.getContent())
								&& this.manager.getMyInstanceName().equalsIgnoreCase(info.getSource())) {
							Long endTS = System.currentTimeMillis();
							logger.info("addLock end: <success>, total millis = " + (endTS - startTS));
							return info;
						}
					}
				}
				Long endTS = System.currentTimeMillis();
				logger.info("addTaskLock end: <fail>, total millis = " + (endTS - startTS));
				return null;

			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(EventManagerConstant.EVENT_TYPE_LOCK);
			}

		} else {
			Long endTS = System.currentTimeMillis();
			logger.fine("addTaskLock end: Can not get APP Repo Lock. <fail>, total millis = " + (endTS - startTS));
			return null;
		}
	}

	public boolean releaseTaskLock(EventInfo lockEventInfo) {
		logger.info("releaseTaskLock start........" + lockEventInfo);
		Long startTS = System.currentTimeMillis();

		if (lockEventInfo == null) {
			Long endTS = System.currentTimeMillis();
			logger.info("releaseTaskLock end:lockEventInfo is null. return <true>, total millis = " + (endTS - startTS));
			return true;
		}
		EventInfo lockEvent = null;
		EventInfo myLockEvent = null;
		List<EventInfo> newInfoList = new ArrayList<EventInfo>();
		if (this.manager.appRepoLockService.aquireAppRepoLock(EventManagerConstant.EVENT_TYPE_LOCK)) {
			try {
				List<EventInfo> infoList = this.manager.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_LOCK);
				if (infoList != null && !infoList.isEmpty()) {
					for (EventInfo info : infoList) {
						if (lockEventInfo.getContent() != null
								&& lockEventInfo.getContent().equalsIgnoreCase(info.getContent())) {
							if (this.manager.getMyInstanceName().equalsIgnoreCase(info.getSource())) {
								// this is my lock, remove it when save to repo
							} else {
								newInfoList.add(info);
							}
						} else {
							// other owns the lock, keep it
							newInfoList.add(info);
						}
					} // end for
				}
				if (this.manager.repoService.saveEventInfoListToRepo(newInfoList, EventManagerConstant.EVENT_TYPE_LOCK, false)) {
					Long endTS = System.currentTimeMillis();
					logger.info("releaseTaskLock end: return <true>, total millis = " + (endTS - startTS));
					return true;
				} else {
					Long endTS = System.currentTimeMillis();
					logger.info("releaseTaskLock end: return <false>, total millis = " + (endTS - startTS));
					return false;
				}

			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(EventManagerConstant.EVENT_TYPE_LOCK);
			}
		} else {
			Long endTS = System.currentTimeMillis();
			logger.fine("releaseTaskLock end: Can not get APP Repo Lock. return <false>, total millis = " + (endTS - startTS));
			return false;
		}
	}

}
