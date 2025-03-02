package com.itdevcloud.japp.se.common.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.multiInstance.v1.socket.SocketAdaptorManager;
import com.itdevcloud.japp.se.common.multiInstance.v1.socket.SocketAdaptorMessage;

class SocketAdaptorTest {

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
	void test() {
		SocketAdaptorManager socketAdaptorManager = new SocketAdaptorManager(8080);
		
		socketAdaptorManager.startReceive();
		SocketAdaptorMessage requestMessage = new SocketAdaptorMessage();
		requestMessage.setContent("this is request message...8080...");
		
		socketAdaptorManager.send(requestMessage);

		requestMessage.setContent("this is request message...l1...\n........l2......");

		socketAdaptorManager.send(requestMessage);

		SocketAdaptorManager manager2 = new SocketAdaptorManager(8081);
		manager2.startReceive();
		requestMessage.setContent("this is request message...8081...");
		
		manager2.send(requestMessage);
		
		assertTrue(true);
	}

}
