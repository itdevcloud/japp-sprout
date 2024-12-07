package com.itdevcloud.japp.se.common.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.multiInstance.socket.LockFileManager;

class LockFileManagerTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLock() {
		String lockName = "Lock-1" ;
		String requester = "Instance-1"; 
		String key = LockFileManager.addLock(lockName, requester, null, null);
		System.out.println("Lock-1 , Instance-1 Key = " + key);

		lockName = "Lock-2" ;
		requester = "Instance-2"; 
		key = LockFileManager.addLock(lockName, requester, null, "12345");
		System.out.println("Lock-2, Instance-2 Key = " + key);

		lockName = "Lock-2" ;
		requester = "Instance-1"; 
		key = LockFileManager.addLock(lockName, requester, null, null);
		System.out.println("Lock-2, Instance-1 Key = " + key);

		lockName = "Lock-2" ;
		requester = "Instance-3"; 
		key = LockFileManager.addLock(lockName, requester, null, null);
		System.out.println("Lock-2, Instance-3 Key = " + key);

		lockName = "Lock-3" ;
		requester = "Instance-3"; 
		key = LockFileManager.addLock(lockName, requester, null, null);
		System.out.println("Lock-3, Instance-3 Key = " + key);
		assertTrue(true);
	}
	@Test
	void testRelease() {
		
		
		String lockName = "Lock-1" ;
		String requester = "Instance-1"; 
		String key = "l-pc2L3120648qgp";
		boolean result = LockFileManager.releaseLock(lockName, requester, key);
		System.out.println("Release Lock-1 , Instance-1 result = " + result);

		lockName = "Lock-2" ;
		requester = "Instance-2"; 
		key = "l2345";
		result = LockFileManager.releaseLock(lockName, requester, key);
		System.out.println("Release Lock-2 , Instance-2 result = " + result);
		assertTrue(true);

	}

}
