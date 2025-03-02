package com.itdevcloud.japp.se.common.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.multiInstance.v1.EventStatus;
import com.itdevcloud.japp.se.common.multiInstance.v1.socket.MessageProcessingStatusCache;
import com.itdevcloud.japp.se.common.multiInstance.v1.socket.MultiInstanceSocketSupportManager;
import com.itdevcloud.japp.se.common.util.CommonUtil;

class MultiInstanceSocketSupportTest {

	static MultiInstanceSocketSupportManager manager = null;
	
	@BeforeAll
	public static void setup() {
		if(manager == null) {
			manager = MultiInstanceSocketSupportManager.getSingletonInstance();
			manager.start();
		}
	}

	@BeforeEach
	public void init() {
	}

	@AfterAll
    public static void teardown() {
		//to make sure manager thread is running at least 10 mins
		System.out.println("....sleeping.......");
		int i = 0;
        while(i < 10) {
           	i++;
        	try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
         }
    }

	@Disabled
	public void methodYouWantIgnored() {
	}

	@Test
	void testLock1() {
		// init

		List<String> ipList = CommonUtil.getMyLocalIp(null);
		System.out.println("MultipleInstanceSocketManagerTest1...........My IPs = " + CommonUtil.listToString(ipList));

		String lockName = "Lock-1";
		String key = "12345-1";

		String tmpKey = manager.addLock(lockName, key);
		System.out.println("returned key = " + tmpKey);

		lockName = "Lock-2";
		key = "12345-2";

		tmpKey = manager.addLock(lockName, key);
		System.out.println("returned key = " + tmpKey);

		lockName = "Lock-3";
		key = "12345-3";

		tmpKey = manager.addLock(lockName, key);
		System.out.println("returned key = " + tmpKey);

		System.out.println("Status Cache = \n" + MessageProcessingStatusCache.getCacheInfoString(null));
		assertTrue(true);
	}
	@Test
	void testLock2() {

		String lockName = "Lock-1";
		String key = "12345-1";

		String tmpKey = manager.addLock(lockName, key);
		System.out.println("returned key = " + tmpKey);

		lockName = "Lock-4";
		key = "12345-4";

		tmpKey = manager.addLock(lockName, key);
		System.out.println("returned key = " + tmpKey);

		System.out.println("Status Cache = \n" + MessageProcessingStatusCache.getCacheInfoString(null));
		assertTrue(true);
	}

	@Test
	void testBroadcastEvent() {

		String mId1 = manager.broadcastEventMessage("user change event", null,
				"event 1 testing message from Test3.....");
		
		String mId2 = manager.broadcastEventMessage("name change event", null, "event 2 message from Test3.....");

		System.out.println("sleep 5 sencods.....");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<EventStatus> statusList1 = manager.inquiryEventStatus(mId1);
		List<EventStatus> statusList2 = manager.inquiryEventStatus(mId2);

		System.out.println("statusList1 = \n");
		for (EventStatus status : statusList1) {
			System.out.println(status.getPrintString());
		}

		System.out.println("statusList2 = \n");
		for (EventStatus status : statusList2) {
			System.out.println(status.getPrintString());
		}

		System.out.println("Status Cache = \n" + MessageProcessingStatusCache.getCacheInfoString(null));
		assertTrue(true);
	}
}
