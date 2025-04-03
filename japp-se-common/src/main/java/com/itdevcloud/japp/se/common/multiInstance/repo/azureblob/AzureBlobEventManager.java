package com.itdevcloud.japp.se.common.multiInstance.repo.azureblob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.multiInstance.repo.DataInfo;
import com.itdevcloud.japp.se.common.multiInstance.repo.RepoBaseMiEventManager;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventProcessor;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class AzureBlobEventManager extends RepoBaseMiEventManager {
	private static final Logger logger = Logger.getLogger(AzureBlobEventManager.class.getName());

//	public static final String BLOB_TYPE_REPO_LOCK = "BLOB_TYPE_REPO_LOCK";
//	public static final String BLOB_TYPE_CONTENT = "BLOB_TYPE_CONTENT";

	private AzureBlobService azureBlobService = null;
	private String containerName = null;

	public AzureBlobEventManager(String appName) {
		this(appName, null, null, null);
	}

	public AzureBlobEventManager(String appName, String containerName) {
		this(appName, containerName, null, null);
	}

	public AzureBlobEventManager(String appName, String containerName, String myInstanceName,
			EventProcessor eventProcessor) {
		super(appName, myInstanceName, eventProcessor);
		this.azureBlobService = AzureBlobService.getInstance(containerName);
		this.containerName = this.azureBlobService.getBlobContainerName();
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isConnected() {
		return this.azureBlobService.isInitSuccess();
	}

	@Override
	protected void reset(String appName) {
		this.azureBlobService = AzureBlobService.getInstance(this.containerName);
		this.azureBlobService.init(this.containerName);
	}

	@Override
	protected boolean useAppRepoLock() {
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + ", [Blob Container Name = "
				+ (this.azureBlobService == null ? this.containerName : this.azureBlobService.getBlobContainerName())
				+ "] ";
	}

	@Override
	public String getRepoName(String repoType, String eventType) {

		if (StringUtil.isEmptyOrNull(repoType) || StringUtil.isEmptyOrNull(eventType)) {
			return null;
		}

		if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			if (eventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
				eventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
			}
		}
		String repoName = this.appName + "_";

		if (EventManagerConstant.REPO_TYPE_REPO_LOCK.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_LOCK_EVENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_BROADCAST_EVENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				repoName = repoName + "REPO_LOCK_FOR_PROCESS_STATUS_EVENT.txt";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
				repoName = repoName + "REPO_LOCK_FOR_" + eventType.toUpperCase() + ".txt";
			} else {
				// event type is not supported
				repoName = null;
			}
		} else if (EventManagerConstant.REPO_TYPE_CONTENT.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				repoName = repoName + "LOCK_EVENT_CONTENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				repoName = repoName + "BROADCAST_EVENT_CONTENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				repoName = repoName + "PROCESS_STATUS_EVENT_CONTENT.txt";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
				repoName = repoName + eventType.toUpperCase() + ".txt";
			} else {
				// event type is not supported
				repoName = null;
			}
		} else {
			// blob type is not supported
			repoName = null;
		}
		return repoName;
	}

	@Override
	protected long getEventLastUpdateTimeFromRepo(String eventType) {
		logger.fine("getRepoLastUpdatedTimestamp start........");
		Long startTS = System.currentTimeMillis();

		String repoName = getRepoName(EventManagerConstant.REPO_TYPE_CONTENT, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.info("getRepoLastUpdatedTimestamp end: blobName is null, return -1, total millis = "
					+ (endTS - startTS));
			return -1;
		}
		long ts = this.azureBlobService.getBlobLastUpdatedTimestamp(repoName);
		Long endTS = System.currentTimeMillis();
		logger.fine(
				"getRepoLastUpdatedTimestamp end: repoName = " + repoName + ", total millis = " + (endTS - startTS));
		return ts;
	}

	@Override
	protected boolean setEventLastUpdateTimeInRepo(String eventType) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return false;
		}
		// blob updated time will be set automatically
		return true;
	}

	@Override
	protected List<String> getContentFromRepo(String repoName) {
		logger.fine("getContentFromRepo start........");
		Long startTS = System.currentTimeMillis();
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine("getContentFromRepo end: key is null, return null, total millis = " + (endTS - startTS));
			return null;
		}
		String content = this.azureBlobService.getContentFromBlob(repoName);
		String[] contentArr = content == null ? null : content.split("\n");

		List<String> contentList = (contentArr == null || contentArr.length == 0) ? null : Arrays.asList(contentArr);
		Long endTS = System.currentTimeMillis();
		logger.fine("getContentFromRepo end: repoName = " + repoName + ", content list size = "
				+ (contentList == null ? 0 : contentList.size()) + ", total millis = " + (endTS - startTS));
		return contentList;
	}

	@Override
	protected boolean saveContentToRepo(String repoName, List<String> contentList, boolean append) {
		logger.fine("saveContentToRepo start........");
		Long startTS = System.currentTimeMillis();
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.fine("saveContentToRepo end: repoName is null, return <false>, total millis = " + (endTS - startTS));
			return false;
		}
		String content = null;
		String value = null;
		if (contentList != null && !contentList.isEmpty()) {
			for (int i = 0; i < contentList.size(); i++) {
				value = contentList.get(i);
				if (value != null) {
					if (i == 0) {
						content = value;
					} else {
						content = content + "\n" + value;
					}
				}
			}
		}
		boolean result = this.azureBlobService.saveContentToBlob(repoName, content, append);
		Long endTS = System.currentTimeMillis();
		logger.fine("saveContentToRepo end: repoName = " + repoName + ", return " + result + ", total millis = "
				+ (endTS - startTS));
		return result;
	}

	public void listAllBlobs() {
		this.azureBlobService.listAllBlobs(true);
	}

	public static void main(String[] args) {

		JulLogger.initJavaUtilLogger(null);
		AzureBlobEventManager azureBlobEventManager = null;
		try {
			azureBlobEventManager = new AzureBlobEventManager("FCS", "MyTestContainer1");

			// start monitor
			azureBlobEventManager.startEventMonitor();
//		
//		
//			EventInfo eventInfo = new EventInfo(azureBlobEventManager.getAppName(), null,
//					azureBlobEventManager.getMyInstanceName());
//			azureBlobEventManager.broadcastEventMessage("User_Change_Event", "aaa@hotmail.com");
//
//			List<EventInfo> infoList = azureBlobEventManager.getBroadcastEventList();
//			String infoListStr = CommonUtil.listToString(infoList);
//			logger.info("Broadcast Event List, size = " + infoList.size() + "\n" + infoListStr);
//
//			infoList = azureBlobEventManager.getProcessStatusList();
//			infoListStr = CommonUtil.listToString(infoList);
//			logger.info("Event Process Status List, size = " + (infoList == null ? 0 : infoList.size()) + "\n"
//					+ infoListStr);
//
			DataInfo dInfo = null;
			List<DataInfo> dInfoList = new ArrayList<DataInfo>();
			for (int i = 1; i < 10; i++) {
				dInfo = new DataInfo("FCS", "user-" + i);
				dInfoList.add(dInfo);
			}
			String dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "jwt_blacklist";
			azureBlobEventManager.saveData(dataEventType, dInfoList, false);

			dInfoList = azureBlobEventManager.retrieveData(dataEventType);
			String dInfoListStr = CommonUtil.listToString(dInfoList);
			logger.info("Data List, size = " + (dInfoList == null ? 0 : dInfoList.size()) + "\n" + dInfoListStr);

			//azureBlobEventManager.listAllBlobs();

		} finally {
			if (azureBlobEventManager != null) {
				azureBlobEventManager.close();
			}
		}
	}

}
