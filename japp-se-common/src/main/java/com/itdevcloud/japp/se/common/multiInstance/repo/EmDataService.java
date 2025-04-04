package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.ArrayList;
import java.util.List;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class EmDataService {
	
	private static final JulLogger logger = JulLogger.getLogger(EmDataService.class.getName());
	
	private RepoBaseMiEventManager manager = null;
	
	
	public EmDataService(RepoBaseMiEventManager manager) {
		if(manager == null) {
			throw new RuntimeException("EventManager is null, can not create " + this.getClass().getSimpleName() + " instance!");
		}
		this.manager = manager;
	}
	
    public String getDataRepoName(String dataEventType){
		if(StringUtil.isEmptyOrNull(dataEventType) || !dataEventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			return null;
		}
		if(dataEventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
			dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
		}
		String repoName = this.manager.getRepoName(EventManagerConstant.REPO_TYPE_CONTENT, dataEventType);
		return repoName;
    }
    
    public List<DataInfo> retrieveDataFromRepo(String dataEventType){
		Long startTS = System.currentTimeMillis();
		if(StringUtil.isEmptyOrNull(dataEventType) || !dataEventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			logger.severe("dataEventType is null or not supported: " + dataEventType);
			return null;
		}
		if(dataEventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
			dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
		}
		
		
		if (this.manager.appRepoLockService.aquireAppRepoLock(dataEventType)) {
			try {
				List<EventInfo> infoList = this.manager.repoService.getEventInfoListFromRepo(dataEventType);
				if(infoList == null || infoList.isEmpty()) {
					Long endTS = System.currentTimeMillis();
					logger.fine("broadcastEventMessage end: return null , total millis = " + (endTS - startTS));
					return null;
				}
				List<DataInfo> dataInfoList = new ArrayList<DataInfo>();
				DataInfo dataInfo = null;
				for(EventInfo info: infoList) {
					dataInfo = new DataInfo(info);
					dataInfoList.add(dataInfo);
				}
				Long endTS = System.currentTimeMillis();
				logger.fine("retrieveDataFromRepo end: return DataInfoList Size = " + dataInfoList.size() + ", total millis = " + (endTS - startTS));
				return dataInfoList;
			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(dataEventType);
			}

		} else {
			Long endTS = System.currentTimeMillis();
			logger.fine("retrieveDataFromRepo end: Can not get APP Repo Lock. return false, total millis = "
					+ (endTS - startTS));
			return null;
		}
		
    }
    public boolean saveDataToRepo(String dataEventType, List<DataInfo> dataInfoList, boolean append){
    	Long startTS = System.currentTimeMillis();
		if(StringUtil.isEmptyOrNull(dataEventType) || !dataEventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			logger.severe("dataEventType is null or not supported: " + dataEventType);
			return false;
		}
		if(dataEventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
			dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
		}
		List<EventInfo> infoList = new ArrayList<EventInfo>();;
		if(dataInfoList != null &&  !dataInfoList.isEmpty()) {
			for(DataInfo info: dataInfoList) {
				infoList.add(info);
			}
		}
		
		if (this.manager.appRepoLockService.aquireAppRepoLock(dataEventType)) {
			try {
				boolean result = this.manager.repoService.saveEventInfoListToRepo(infoList, dataEventType, append);
				Long endTS = System.currentTimeMillis();
				logger.fine("retrieveDataFromRepo end: return < " + result + ">, total millis = " + (endTS - startTS));
		   	    return result;
			} finally {
				this.manager.appRepoLockService.releaseAppRepoLock(dataEventType);
			}

		} else {
			Long endTS = System.currentTimeMillis();
			logger.fine("retrieveDataFromRepo end: Can not get APP Repo Lock. return false, total millis = "
					+ (endTS - startTS));
			return false;
		}
		
   }

}
