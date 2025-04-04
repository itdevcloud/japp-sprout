package com.itdevcloud.japp.se.common.multiInstance.repo.azureredis;

import java.util.List;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventInfo;
import com.itdevcloud.japp.se.common.multiInstance.repo.RepoBaseMiEventManager;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventProcessor;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class AzureRedisEventManager extends RepoBaseMiEventManager {
	private static final JulLogger logger = JulLogger.getLogger(AzureRedisEventManager.class.getName());

	private AzureRedisService azureRedisService = null;

	public AzureRedisEventManager(String appName) {
		this(appName, null, null);
	}

	public AzureRedisEventManager(String appName, String myInstanceName, EventProcessor eventProcessor) {
		super(appName, myInstanceName, eventProcessor);
		this.azureRedisService = AzureRedisService.getInstance(this.appName);
	}

	@Override
	public void close() {
		this.azureRedisService.closeRedis();
	}
	@Override
	public boolean isConnected() {
		return this.azureRedisService.isConnected();
	}
	
	@Override
	public void reset(String appName) {
		this.azureRedisService.reset(appName);
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	protected boolean useAppRepoLock() {
		return true;
	}
	@Override
	public String getRepoName(String repoType, String eventType) {

		String repoName = this.appName + "_";

		if (StringUtil.isEmptyOrNull(repoType) || StringUtil.isEmptyOrNull(eventType)) {
			return null;
		}

		if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			if (eventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
				eventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
			}
		}

		if (EventManagerConstant.REPO_TYPE_REPO_LOCK.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_LOCK_EVENT";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_BROADCAST_EVENT";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_PROCESS_STATUS_EVENT";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
				repoName = repoName + "REPO_LOCK_FOR_" + eventType.toUpperCase();
			} else {
				// event type is not supported
				repoName = null;
			}
		} else if (EventManagerConstant.REPO_TYPE_CONTENT.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				repoName = repoName + "LOCK_EVENT_CONTENT";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				repoName = repoName + "BROADCAST_EVENT_CONTENT";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				repoName = repoName + "PROCESS_STATUS_EVENT_CONTENT";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
				repoName = repoName + eventType.toUpperCase();
			} else {
				// event type is not supported
				repoName = null;
			}
		} else if (EventManagerConstant.REPO_TYPE_UPDATE_TIME.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				repoName = repoName + "LOCK_EVENT_UPDATE_TIME";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				repoName = repoName + "BROADCAST_EVENT_UPDATE_TIME";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				repoName = repoName + "PROCESS_STATUS_EVENT_UPDATE_TIME";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
				repoName = repoName + eventType.toUpperCase() + "_UPDATE_TIME";
			} else {
				// event type is not supported
				repoName = null;
			}
		} else {
			repoName = null;
		}
		return repoName;
	}


	@Override
	protected long getEventLastUpdateTimeFromRepo(String eventType) {

		logger.fine("getRepoLastUpdatedTimestamp start........");
		Long startTS = System.currentTimeMillis();

		String repoName = getRepoName(EventManagerConstant.REPO_TYPE_UPDATE_TIME, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine(
					"getRepoLastUpdatedTimestamp end: repoName is null, return -1, total millis = " + (endTS - startTS));
			return -1;
		}
		String value = this.azureRedisService.getValueFromRedis(repoName);
		long ts = -1;
		try {
			ts = Long.parseLong(value);
		} catch (Throwable t) {

		}
		Long endTS = System.currentTimeMillis();
		logger.fine("getRepoLastUpdatedTimestamp end: repoName = " + repoName + ", total millis = " + (endTS - startTS));
		return ts;
	}

	@Override
	protected boolean setEventLastUpdateTimeInRepo(String eventType) {
		logger.fine("setRepoLastUpdatedTimestamp start........");
		Long startTS = System.currentTimeMillis();
		String repoName = getRepoName(EventManagerConstant.REPO_TYPE_UPDATE_TIME, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine(
					"setEventLastUpdateTimeInRepo end: repoName is null, return false, total millis = " + (endTS - startTS));
			return false;
		}
		long ts = System.currentTimeMillis();

		boolean result = this.azureRedisService.setValueToRedis(repoName, "" + ts);
		Long endTS = System.currentTimeMillis();
		logger.fine("setEventLastUpdateTimeInRepo end: repoName = " + repoName + ", return " + result + ", total millis = "
				+ (endTS - startTS));

		return result;

	}

	@Override
	protected boolean saveContentToRepo(String repoName, List<String> contentList, boolean append) {
		return this.azureRedisService.saveContentToRedis(repoName, contentList, true, append);
	}

	@Override
	protected List<String> getContentFromRepo(String repoName) {
		return this.azureRedisService.getContentFromRedis(repoName);
	}


	public static void main(String[] args) {

		JulLogger.initJavaUtilLogger(null);
		AzureRedisEventManager azureRedisEventManager = null;
		try {
			azureRedisEventManager = new AzureRedisEventManager("FCS");

			// start monitor
		azureRedisEventManager.startEventMonitor();
		
		
		EventInfo eventInfo = new EventInfo(azureRedisEventManager.getAppName(), null, azureRedisEventManager.getMyInstanceName());
		azureRedisEventManager.broadcastEventMessage("User_Change_Event", "aaaa@hotmail.com");

		List <EventInfo> infoList = azureRedisEventManager.getBroadcastEventList();
		String infoListStr = CommonUtil.listToString(infoList);
		logger.info("Broadcast Event List, size = " + (infoList == null?0:infoList.size()) + "\n" +infoListStr);
		
//		try {
//			Thread.sleep(60000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		infoList = azureRedisEventManager.getProcessStatusList();
//		infoListStr = CommonUtil.listToString(infoList);
//		logger.info("Event Process Status List, size = " + (infoList == null?0:infoList.size()) + "\n" +infoListStr);
		

//			DataInfo dInfo = null;
//			List<DataInfo> dInfoList = new ArrayList<DataInfo>();
//			for (int i = 1; i < 10; i++) {
//				dInfo = new DataInfo("FCS", "user-" + i);
//				dInfoList.add(dInfo);
//			}
//			String dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "jwt_blacklist";
//			azureRedisEventManager.saveData(dataEventType, dInfoList, true);
//
//			dInfoList = azureRedisEventManager.retrieveData(dataEventType);
//			String dInfoListStr = CommonUtil.listToString(dInfoList);
//			logger.info("Data List, size = " + (dInfoList == null ? 0 : dInfoList.size()) + "\n" + dInfoListStr);
		} finally {
//			if (azureRedisEventManager != null) {
//				azureRedisEventManager.close();
//			}
		}
	}



}
