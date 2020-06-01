package com.itdevcloud.japp.core.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.itdevcloud.japp.core.common.TestBase;

import junit.framework.JUnit4TestAdapter;

public class AppCoreTest extends TestBase {

	private static Logger logger = LogManager.getLogger(AppCoreTest.class);

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(com.itdevcloud.japp.core.test.AppCoreTest.class);
	}

	@Before
	public void setup() {
	}

	@Ignore
	public void methodYouWantIgnored() {
	}

	@Test
	public void runBatchTest() {

		logger.info("runBatchTest() begin...............");

		List<String> failCommandList = batchTest(null);


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
		boolean result = test(urlPath, "echo",jsonRequestString, null);
		assertTrue(result);
	}

}
