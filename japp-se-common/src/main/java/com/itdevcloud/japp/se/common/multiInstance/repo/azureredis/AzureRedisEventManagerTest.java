package com.itdevcloud.japp.se.common.multiInstance.repo.azureredis;

import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.multiInstance.repo.EventInfo;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;

public class AzureRedisEventManagerTest {
	private static final Logger logger = Logger.getLogger(AzureRedisEventManagerTest.class.getName());

	public static void main(String[] args) {

		JulLogger.initJavaUtilLogger(null);
		AzureRedisEventManager azureRedisEventManager = null;
		try {
			azureRedisEventManager = new AzureRedisEventManager("FCS");

		EventInfo eventInfo = new EventInfo(azureRedisEventManager.getAppName(), null, azureRedisEventManager.getMyInstanceName());
		azureRedisEventManager.broadcastEventMessage("User_Change_Event", "bbbbb@hotmail.com");

		EventInfo eventInfoA = azureRedisEventManager.addTaskLock("run-report-a", 10);
		EventInfo eventInfoB = azureRedisEventManager.addTaskLock("run-report-b", 2);
		EventInfo eventInfoC = azureRedisEventManager.addTaskLock("run-report-c", 2);

		azureRedisEventManager.releaseTaskLock(eventInfoB);

			List<EventInfo> infoList = azureRedisEventManager.getTaskLockList();

			String infoListStr = CommonUtil.listToString(infoList);
			logger.info("Lock Event List, size = " + (infoList == null?0:infoList.size()) + "\n" + infoListStr);

			infoList = azureRedisEventManager.getProcessStatusList();
			infoListStr = CommonUtil.listToString(infoList);
			logger.info("Event Process Status List, size = " + (infoList == null?0:infoList.size()) + "\n" +infoListStr);

			
		} finally {
			if (azureRedisEventManager != null) {
				azureRedisEventManager.close();
			}
		}

	}

}
