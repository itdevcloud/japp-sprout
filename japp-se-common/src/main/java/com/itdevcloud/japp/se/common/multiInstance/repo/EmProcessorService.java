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

public class EmProcessorService {

	private static final Logger logger = Logger.getLogger(EmProcessorService.class.getName());

	private RepoBaseMiEventManager manager = null;

	public EmProcessorService(RepoBaseMiEventManager manager) {
		if (manager == null) {
			throw new RuntimeException(
					"EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}

	protected boolean sendEventProcessStatusToRepo(EventProcessStatus eventProcessStatus) {
		logger.fine("sendEventProcessStatusToRepo start........");
		Long startTS = System.currentTimeMillis();
		if (this.manager.appRepoLockService.aquireAppRepoLock(EventManagerConstant.EVENT_TYPE_PROCESS_STATUS)) {
			try {
				List<EventInfo> infoList = this.manager
						.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_PROCESS_STATUS);
				if (infoList == null) {
					infoList = new ArrayList<EventInfo>();
				}
				infoList.add(eventProcessStatus);
				if (this.manager.repoService.saveEventInfoListToRepo(infoList, EventManagerConstant.EVENT_TYPE_PROCESS_STATUS,
						false)) {
					Long endTS = System.currentTimeMillis();
					logger.info("sendEventProcessStatusToRepo end: return <true>, total millis = " + (endTS - startTS));
					return true;
				} else {
					Long endTS = System.currentTimeMillis();
					logger.info(
							"sendEventProcessStatusToRepo end: return <false>, total millis = " + (endTS - startTS));
					return false;
				}
			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(EventManagerConstant.EVENT_TYPE_PROCESS_STATUS);
			}

		} else {
			Long endTS = System.currentTimeMillis();
			logger.fine("sendEventProcessStatusToRepo end: Can not get APP Repo Lock, return <false>, total millis = "
					+ (endTS - startTS));
			return false;
		}
	}

	public void process() {
		try {
			Long startTS = System.currentTimeMillis();
			List<EventInfo> list = null;
			// only process non lock event
			list = this.manager.broadcastService.getUnProcessedBroadcastEventInfoList();

			if (list != null && !list.isEmpty()) {
				logger.info("Event Monitor [" + this.manager.getClass().getSimpleName()
						+ "] find un-processed event, event count = " + list.size() + "......");

				this.manager.setLastProcessTimestamp(System.currentTimeMillis());

				// process event one by one
				EventProcessStatus eventProcessStatus = null;
				Date now = new Date();
				Date expiryDate = DateUtils.addTime(now, Calendar.MINUTE,
						EventManagerConstant.DEFAULT_PROCESS_STATUS_RETENTION_DAY * 24 * 60);

				String contentAbstract = null;
				for (EventInfo info : list) {
					logger.info("Event Monitor: before process " + info);

					for (int i = 0; i <= this.manager.getProcessRetryCount(); i++) {
						try {
							eventProcessStatus = this.manager.getEventProcessor().processEvent(info);
						} catch (Throwable t) {
							logger.severe("EventManager process error: " + t + "\n" + info);
							t.printStackTrace();
						}
						if (info != null && !StringUtil.isEmptyOrNull(info.getContent())) {
							contentAbstract = info.getContent().length() > 10 ? info.getContent().substring(0, 9)
									: info.getContent();
						} else {
							contentAbstract = EventManagerConstant.EVENT_INFO_EMPTY_CONTENT;
						}
						if (eventProcessStatus == null) {

							eventProcessStatus = new EventProcessStatus();
							eventProcessStatus.setProcessStatus(EventManagerConstant.EVENT_PROCESS_STATUS_FAIL);
						}
						// make sure value are right
						eventProcessStatus.setAppName(info.getAppName());
						eventProcessStatus.setUid(null);
						eventProcessStatus.setName(info.getName());
						eventProcessStatus.setSource(info.getSource());
						eventProcessStatus.setEventDate(now);
						eventProcessStatus.setExpiryDate(expiryDate);
						eventProcessStatus.setContent(contentAbstract);
						eventProcessStatus.setProcessor(this.manager.getMyInstanceName());
						eventProcessStatus.setProccessedEventUid(info.getUid());
						eventProcessStatus.setProcessedDate(new Date());

						if (EventManagerConstant.EVENT_PROCESS_STATUS_SUCCESS
								.equalsIgnoreCase(eventProcessStatus.getProcessStatus())) {
							this.manager.getProcessedEventList().add(info);
							break;
						} else {
							eventProcessStatus.setProcessStatus(EventManagerConstant.EVENT_PROCESS_STATUS_FAIL);
							if (i >= this.manager.getProcessRetryCount()) {
								this.manager.getProcessedEventList().add(info);
								break;
							}
							try {
								// wait for retry
								Thread.sleep(this.manager.getProcessRetryIntervalMillis());
							} catch (Throwable t) {
							}
						}
					} // end retry for
					this.sendEventProcessStatusToRepo(eventProcessStatus);
					logger.info("EventManager Monitor: process status: " + eventProcessStatus);
				} // end for
				Long endTS = System.currentTimeMillis();
				logger.info("process() end.....total millis = " + (endTS - startTS));
			}
		} catch (Throwable t) {
			logger.severe("Error Detected: " + t.getMessage());
			t.printStackTrace();
		} finally {
		}
	}

}
