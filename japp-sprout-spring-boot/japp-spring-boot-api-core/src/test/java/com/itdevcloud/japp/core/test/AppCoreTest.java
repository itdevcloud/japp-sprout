/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.core.test;

/**
*
* @author Marvin Sun
* @since 1.0.0
*/
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
