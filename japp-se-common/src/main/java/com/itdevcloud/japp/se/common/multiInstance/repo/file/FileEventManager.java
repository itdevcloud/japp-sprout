package com.itdevcloud.japp.se.common.multiInstance.repo.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.multiInstance.repo.EventInfo;
import com.itdevcloud.japp.se.common.multiInstance.repo.RepoBaseMiEventManager;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventProcessor;
import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class FileEventManager extends RepoBaseMiEventManager {
	
	private static final Logger logger = Logger.getLogger(FileEventManager.class.getName());
	
	private String fileRootPath = null;
	private boolean isFileAccessible = false;

	public FileEventManager(String appName) {
		this(appName, null, null);
	}
	public FileEventManager(String appName, String myInstanceName, EventProcessor eventProcessor) {
		super(appName, myInstanceName, eventProcessor);
		setFileRootPath(null);
	}

	public void setFileRootPath(String fileRootPath) {
		this.fileRootPath = StringUtil.isEmptyOrNull(fileRootPath)?System.getProperty("user.home"):fileRootPath.trim();
		if(StringUtil.isEmptyOrNull(this.fileRootPath)) {
			this.fileRootPath = File.separator;
		}
		if(this.fileRootPath.endsWith("\\") || this.fileRootPath.endsWith("/") || this.fileRootPath.endsWith(File.separator)){
			this.fileRootPath = this.fileRootPath + "fileEventManager";
		}else {
			this.fileRootPath = this.fileRootPath + File.separator + "fileEventManager";
		}
		String fullFileName = getRepoName(EventManagerConstant.REPO_TYPE_REPO_LOCK, EventManagerConstant.EVENT_TYPE_BROADCAST);
		if(StringUtil.isEmptyOrNull(fullFileName)) {
			this.isFileAccessible = false;
		}else {
			this.isFileAccessible = true;
		}
	}
	
	@Override
	public String getRepoName(String repoType, String eventType) {

		String fileName = getFileName(repoType, eventType);
		String fullFileName = (fileName == null?null:this.fileRootPath + File.separator + fileName);
		
		File dir = new File(this.fileRootPath);
		if(!dir.exists()) {
			try {
				dir.mkdirs();
			} catch (Throwable t) {
				logger.severe("can not create root path: " + this.fileRootPath + "......" + t);
				return null;
			}
		}
		File file = new File(fullFileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (Throwable t) {
				logger.severe("can not create a new file: " + fullFileName + "......" + t);
				return null;
			}
		}
		return fullFileName;
	}
	
	public String getFileName(String repoType, String eventType) {

		if(StringUtil.isEmptyOrNull(repoType) || StringUtil.isEmptyOrNull(eventType)) {
			return null;
		}
		if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			if(eventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
				eventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
			}
		}
		String fileName = this.appName + "_";

		if(EventManagerConstant.REPO_TYPE_REPO_LOCK.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				fileName = fileName + "REPO_LOCK_FOR_LOCK_EVENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				fileName = fileName + "REPO_LOCK_FOR_BROADCAST_EVENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				fileName = fileName + "REPO_LOCK_FOR_PROCESS_STATUS_EVENT.txt";
			} else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA) ) {
				fileName = fileName + "REPO_LOCK_FOR_" + eventType.toUpperCase() + ".txt";
			} else {
				// event type is not supported
				fileName = null;
			}
		}else if(EventManagerConstant.REPO_TYPE_CONTENT.equalsIgnoreCase(repoType)) {
			if (EventManagerConstant.EVENT_TYPE_LOCK.equalsIgnoreCase(eventType)) {
				fileName = fileName + "LOCK_EVENT_CONTENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_BROADCAST.equalsIgnoreCase(eventType)) {
				fileName = fileName + "BROADCAST_EVENT_CONTENT.txt";
			} else if (EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
				fileName = fileName + "PROCESS_STATUS_EVENT_CONTENT.txt";
			}  else if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA) ) {
				fileName = fileName + eventType.toUpperCase() + ".txt";
			}else {
				// event type is not supported
				fileName = null;
			}
		}else {
			fileName = null;
		}
		return fileName;
	}
	
	@Override
	public String toString() {
		String str =  super.toString() + " [File Root Path = " + this.fileRootPath + "] ";
		return str;
	}
	
	@Override
	protected boolean useAppRepoLock() {
		return true;
	}
	
	@Override
	protected boolean isConnected() {
		return this.isFileAccessible;
	}
	@Override
	protected void close() {
	}
	@Override
	protected void reset(String appName) {
	}

	@Override
	protected long getEventLastUpdateTimeFromRepo(String eventType) {

		logger.fine("getEventLastUpdateTimeFromRepo start........");
		Long startTS = System.currentTimeMillis();

		String repoName = getRepoName(EventManagerConstant.REPO_TYPE_CONTENT, eventType);
		if (repoName == null) {
			// event type is not supported
			Long endTS = System.currentTimeMillis();
			logger.info("getRepoLastUpdatedTimestamp end: blobName is null, return -1, total millis = "
					+ (endTS - startTS));
			return -1;
		}
		File contentFile = new File(repoName);
		if(contentFile == null || !contentFile.exists()) {
			Long endTS = System.currentTimeMillis();
			logger.fine("getEventLastUpdateTimeFromRepo end: fullFileName doesn't exist, return -1, total millis = " + (endTS - startTS));
			return -1;
		}
		long ts = contentFile.lastModified();
		Long endTS = System.currentTimeMillis();
		logger.fine(
				"getEventLastUpdateTimeFromRepo end: repoName = " + repoName + ", total millis = " + (endTS - startTS));
		return ts;
	}

	@Override
	protected boolean setEventLastUpdateTimeInRepo(String eventType) {
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
		String content = FileUtil.getFileContentAsString(repoName);
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
		File contentFile = new File(repoName);
		if(contentFile != null && contentFile.length() > 0 && append) {
			content = "\n" + content;
		}
		content = StringUtil.isEmptyOrNull(content)?EventManagerConstant.EVENT_INFO_EMPTY_CONTENT:content;
		
		boolean result = FileUtil.writeStringToFile(repoName, append, content);
		Long endTS = System.currentTimeMillis();
		
		logger.fine("saveContentToRepo end: repoName = " + repoName + ", return " + result + ", total millis = "
				+ (endTS - startTS));
		return result;
	}

	

	public static void main(String[] args) {
		
		CommonLogger.initJavaUtilLogger(null);
		
		FileEventManager fileEventManager = new FileEventManager("FCS");
		
		//start monitor
		fileEventManager.startEventMonitor();
		
		
		EventInfo eventInfo = new EventInfo(fileEventManager.getAppName(), null, fileEventManager.getMyInstanceName());
		fileEventManager.broadcastEventMessage("User_Change_Event", "c1@hotmail.com");

		List <EventInfo> infoList = fileEventManager.getBroadcastEventList();
		String infoListStr = CommonUtil.listToString(infoList);
		logger.info("Broadcast Event List, size = " + (infoList==null?0:infoList.size()) + "\n" +infoListStr);
		
		infoList = fileEventManager.getProcessStatusList();
		infoListStr = CommonUtil.listToString(infoList);
		logger.info("Event Process Status List, size = " + (infoList==null?0:infoList.size()) + "\n" +infoListStr);
		
		
	}

}
