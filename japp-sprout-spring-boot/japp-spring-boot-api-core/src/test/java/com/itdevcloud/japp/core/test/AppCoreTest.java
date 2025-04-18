package com.itdevcloud.japp.core.test;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.core.common.TestBase;


public class AppCoreTest extends TestBase {

	private static Logger logger = LogManager.getLogger(AppCoreTest.class);


	@BeforeEach
	public void setup() {
	}

	@Disabled
	public void methodYouWantIgnored() {
	}

	@Test
	public void runBatchTest() {

		logger.info("runBatchTest() begin...............");

		List<String> failCommandList = batchTestRpcAPI(null);


		if (failCommandList.isEmpty()) {
			logger.info("runBatchTest() end........successfully......");
			assertTrue(true);
		} else {
			logger.error("runBatchTest() end........failed command count = " + failCommandList.size()
			+ ", failed command List = \n" + failCommandList);
			assertTrue(false);
		}
	}
	@Test
	public void runTest() {

		String urlPath = "http://localhost:8080/api/cmd";
		String jsonRequestString = "{\"message\":\"hello\"}";
		boolean result = testRpcAPI(urlPath, "echo",jsonRequestString, null, null, null, null);
		assertTrue(result);
	}

}
