package com.itdevcloud.japp.se.common.multiInstance.repo.file;

import com.itdevcloud.japp.se.common.service.JulLogger;

public class FileEventManagerTest  {
	private static final JulLogger logger = JulLogger.getLogger(FileEventManagerTest.class.getName());


	public static void main(String[] args) {
		
		logger.info( "Test CommonLogger formatter.......0....");
		//JulLogger.initJavaUtilLogger(null);
		logger.info( "Test CommonLogger formatter.....1......");

//		FileEventManager fileEventManager = new FileEventManager("FCS");
//		
//		
//		fileEventManager.broadcastEventMessage("User_Change_Event", "c1@hotmail.com");
//		
//		EventInfo eventInfo = new EventInfo(fileEventManager.getAppName(), null, fileEventManager.getMyInstanceName());
//	
//		EventInfo eventInfoA = fileEventManager.addTaskLock("run-report-a", 10);
//		EventInfo eventInfoB = fileEventManager.addTaskLock("run-report-b", 2);
//		EventInfo eventInfoC = fileEventManager.addTaskLock("run-report-c", 2);
//
//		fileEventManager.releaseTaskLock(eventInfoB);
//		
//	
//		List <EventInfo> infoList = fileEventManager.getTaskLockList();
//		
//		String infoListStr = CommonUtil.listToString(infoList);
//		logger.info("Lock Event List, size = " + (infoList==null?0:infoList.size()) + "\n" +infoListStr);
		

//		DataInfo dInfo = null;
//		List<DataInfo> dInfoList = new ArrayList<DataInfo>();
//		String dataEventType = EventManagerConstant.EVENT_TYPE_DATA + "jwt_blacklist";
//
//		
//		for(int i = 1; i < 10; i++) {
//			dInfo = new DataInfo("FCS", "user-"+ i);
//			dInfoList.add(dInfo);
//		}
//		fileEventManager.saveData(dataEventType, dInfoList, false);
//		String message = fileEventManager.getDataRepoName(dataEventType);
//		fileEventManager.broadcastEventMessage("JWT_BLACKLIST", message);
//		
//		dInfoList = fileEventManager.retrieveData(dataEventType);
//		String dInfoListStr = CommonUtil.listToString(dInfoList);
//		logger.info("Data List, size = " + (dInfoList == null?0:dInfoList.size()) + "\n" +dInfoListStr);

		
	}

}
