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
package com.itdevcloud.japp.core.common;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class TestBase {

	private static Logger logger = LogManager.getLogger(TestBase.class);
	public static final String JAPPCORE_BATCH_TEST_FILE_NAME = "japp-batch-test.properties";
	public static final String CONFIG_KEY_JAPPCORE_TEST_URL_PATH = "jappcore.test.url.path";

	private String JAPPCORE_API_TEST_URL = null;
	private Map<String, String> commandRequestMap = null;
	private Map<String, String> commandResultMap = null;
	private Properties testConfigProperties = null;


	private void init(String testPropertiesFileName) {
		logger.info("TestBase.init() - start........");
		if(StringUtil.isEmptyOrNull(testPropertiesFileName)) {
			testPropertiesFileName = JAPPCORE_BATCH_TEST_FILE_NAME;
		}else {
			testPropertiesFileName = testPropertiesFileName.trim();
		}
		commandRequestMap = new HashMap<String, String>();
		commandResultMap = new HashMap<String, String>();
		testConfigProperties = new Properties();

		InputStream in = null;
		try {
			in = TestBase.class.getResourceAsStream("/" + testPropertiesFileName);
			logger.debug("loading test property file '/" + testPropertiesFileName + "' from classpath...");
			testConfigProperties.load(in);
			in.close();
			in = null;
		} catch (Exception e) {
			throw new RuntimeException(
					"can not load property file '/" + testPropertiesFileName + "' from classpath.", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.warn("Cannot close InputStream.", e);
				}
				;
			}
		}

		List<String> errorCommandList = new ArrayList<String>();

		Set<Object> keySet = testConfigProperties.keySet();
		if (keySet != null && !keySet.isEmpty()) {
			for (Object keyObj : keySet) {
				String key = "" + keyObj;
				key = key.trim();
				if(CONFIG_KEY_JAPPCORE_TEST_URL_PATH.equalsIgnoreCase(key)) {
					JAPPCORE_API_TEST_URL = testConfigProperties.getProperty(key);
					continue;
				}
				String[] keyArray = key.split("\\.");
				if (keyArray == null || keyArray.length < 2) {
					errorCommandList.add(key);
					continue;
				}
				if (key.endsWith(".assertion")) {
					commandResultMap.put(key, testConfigProperties.getProperty(key));
				} else {
					commandRequestMap.put(key, testConfigProperties.getProperty(key));

				}
			}
		} // end if
		if (commandRequestMap.size() != commandResultMap.size()) {
			logger.warn(
					"----WARNING ---- size of commandRequestMap and commandResultMap are different!....commandRequestMap size = "
							+ commandRequestMap.size() + "..., commandResultMap size = " + commandResultMap.size());
		}
		String info = "init()....end....total loaded command count = " + commandRequestMap.size();
		if(errorCommandList.size() > 0) {
			info = info + ", total error command detected: " + errorCommandList.size() + ", error command list: " + errorCommandList;
		}else {
			info = info + ", total error command detected: 0 " ;
		}
		logger.info(info);
	}

	public void setPiscesJappApiTestUrl(String url) {
		JAPPCORE_API_TEST_URL = url;
	}

	public List<String> batchTest(String testPropertiesFileName) {

		logger.info("batchTest()-  begin..........");
		init(testPropertiesFileName) ;

		if(StringUtil.isEmptyOrNull(JAPPCORE_API_TEST_URL)) {
			logger.info("batchTest() - JAPPCORE_API_TEST_URL is null or empty, do nothing......");
			throw new RuntimeException("batchTest() - JAPPCORE_API_TEST_URL is null or empty, do nothing......");
		}
		logger.info("batchTest() - JAPPCORE_API_TEST_URL  = " + JAPPCORE_API_TEST_URL);
		List<String> failCommandList = new ArrayList<String>();
		String appUrlPath = JAPPCORE_API_TEST_URL;

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(appUrlPath);

		Set<String> keySet = commandRequestMap.keySet();
		List<String> keyList = new ArrayList<String>();
		keyList.addAll(keySet);

		Collections.sort(keyList);

		for (String key : keyList) {
			String[] keyArr = key.split("\\.");
			String order = keyArr[0];
			String command = keyArr[1];
			String jsonRequest = commandRequestMap.get(key);
			String assertresult = commandResultMap.get(key + ".assertion");
			if (StringUtil.isEmptyOrNull(assertresult)) {
				assertresult = "\"cmdStatusCode\":\"SUCCESS\"";
			}

			logger.info("batchTest() - test order = " + order + ", test command = '" + command
					+ "' jsonRequest:\n" + jsonRequest);


			WebTarget commandWebTarget = webTarget;
			if(!StringUtil.isEmptyOrNull(command)) {
				commandWebTarget = webTarget.queryParam("cmd", command);
			}

			Invocation.Builder invocationBuilder = commandWebTarget.request(MediaType.APPLICATION_JSON);

			Response response = invocationBuilder.post(Entity.json(jsonRequest));
			String jsonResponse = response.readEntity(String.class);

			logger.info("batchTest() ... test order = " + order + ", test command = '" + command
					+ "' jsonResponse:\n" + jsonResponse + "\nAssertion String = " + assertresult);
			if (StringUtil.isEmptyOrNull(jsonResponse)) {
				failCommandList.add(key);
			} else {
				if (jsonResponse.indexOf(assertresult) == -1) {
					failCommandList.add(key);
				}
			}
		}

		if (failCommandList.isEmpty()) {
			logger.info("batchTest() - end........successfully......");
		} else {
			logger.error("batchTest() - end........failed command count = " + failCommandList.size()
			+ ", failed command List = \n" + failCommandList);
		}
		return failCommandList;
	}

	public boolean test(String piscesjappUrlPath, String command, String jsonRequestString, String assertionString) {

		logger.info("test() - begin..........");
		if(StringUtil.isEmptyOrNull(piscesjappUrlPath)) {
			piscesjappUrlPath = JAPPCORE_API_TEST_URL;
		}
		if(StringUtil.isEmptyOrNull(piscesjappUrlPath)) {
			logger.info("test() - piscesjappUrlPath and JAPPCORE_DEFAULT_TEST_URL_PATH are null or empty, do nothing......");
			return false;
		}
		if(StringUtil.isEmptyOrNull(jsonRequestString)) {
			logger.info("test() - jsonRequestString is null or empty, do nothing......");
			return false;
		}
		if (StringUtil.isEmptyOrNull(assertionString)) {
			assertionString = "\"cmdStatusCode\":\"SUCCESS\"";
		}
		String appUrlPath = piscesjappUrlPath;

		logger.info("test() - start...piscesjappUrlPath = '" + piscesjappUrlPath + "', test command = '" + command
				+ "' jsonRequest:\n" + jsonRequestString);

		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(appUrlPath);

		WebTarget commandWebTarget = webTarget;
		if(!StringUtil.isEmptyOrNull(command)) {
			commandWebTarget = webTarget.queryParam("cmd", command);
		}

		Invocation.Builder invocationBuilder = commandWebTarget.request(MediaType.APPLICATION_JSON);

		Response response = invocationBuilder.post(Entity.json(jsonRequestString));
		String jsonResponse = response.readEntity(String.class);

		logger.info("test() - end...test command = '" + command
				+ "' jsonResponse:\n" + jsonResponse + "\nAssertion String = " + assertionString);
		if (StringUtil.isEmptyOrNull(jsonResponse)) {
			return false;
		} else {
			if (jsonResponse.indexOf(assertionString) == -1) {
				return false;
			}
		}
		return true;
	}

}
