package com.itdevcloud.japp.se.common.multiInstance.repo.azureblob;

import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.multiInstance.repo.EventInfo;
import com.itdevcloud.japp.se.common.service.CommonLogger;

public class AzureBlobEventManagerTest {
	private static final Logger logger = Logger.getLogger(AzureBlobEventManagerTest.class.getName());

	public static void main(String[] args) {

		CommonLogger.initJavaUtilLogger(null);
		AzureBlobEventManager azureBlobEventManager = null;
		try {
			azureBlobEventManager = new AzureBlobEventManager("FCS", "MyTestContainer1");
			
			// start monitor
			//azureBlobEventManager.startEventMonitor();

			EventInfo eventInfo = new EventInfo(azureBlobEventManager.getAppName(), null, azureBlobEventManager.getMyInstanceName());
			azureBlobEventManager.broadcastEventMessage("User_Change_Event", "ddd1@hotmail.com");

			eventInfo = new EventInfo(azureBlobEventManager.getAppName(), null, azureBlobEventManager.getMyInstanceName());
			azureBlobEventManager.broadcastEventMessage("User_Change_Event", "ddd2@hotmail.com");

			EventInfo eventInfoA = azureBlobEventManager.addTaskLock("run-report-a", 10);
			logger.info("addLock() eventInfoA = " + eventInfoA);
			
			EventInfo eventInfoB = azureBlobEventManager.addTaskLock("run-report-b", 10);
			logger.info("addLock() eventInfoB = " + eventInfoB);

			EventInfo eventInfoC = azureBlobEventManager.addTaskLock("run-report-c", 2);
			logger.info("addLock() eventInfoC = " + eventInfoC);
	
			boolean  result = azureBlobEventManager.releaseTaskLock(eventInfoB);
			logger.info("releaseLock() eventInfoB = " + result);

		

		} finally {
			if (azureBlobEventManager != null) {
				azureBlobEventManager.close();
			}
		}

	}

}
