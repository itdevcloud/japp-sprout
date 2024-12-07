package com.itdevcloud.japp.se.common.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.multiInstance.file.MultiInstanceFileSupportManager;
import com.itdevcloud.japp.se.common.util.DateUtils;

class MultiInstanceFileSupportTest {

	@BeforeAll
	public static void setup() {
	}

	@BeforeEach
	public void init() {
	}
	
	@AfterAll
    public void teardown() {
		//to make sure manager thread is running at least 10 mins
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
	void test() {
		MultiInstanceFileSupportManager manager = new MultiInstanceFileSupportManager();
		
		String lockName = "Report-A";
		Date today = new Date();
		Date expiryDate = DateUtils.addTime(today, Calendar.MINUTE, 10);
		String key = null;
		
		key = manager.addLock(lockName, expiryDate, key);

		lockName = "MonthlyReport-B";
		today = new Date();
		expiryDate = DateUtils.addTime(today, Calendar.MINUTE, 10);
		key = null;
		
		key = manager.addLock(lockName, expiryDate, key);
		
		String eventName = "UserChangeEvent";
		expiryDate = DateUtils.addTime(today, Calendar.HOUR, 24);
		key = null;
		String content = "John@ontaio.ca";
		key = manager.addEvent(eventName, expiryDate, key, content);

		eventName = "NameChangeEvent";
		expiryDate = DateUtils.addTime(today, Calendar.HOUR, 24);
		key = null;
		content = "Frank@ontaio.ca";
		key = manager.addEvent(eventName, expiryDate, key, content);
		
		assertTrue(true);
	}


}
