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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;

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
	public static final String CONFIG_KEY_JAPPCORE_API_ACCESS_TOKEN = "jappcore.test.api.accesstoken";

	private String JAPPCORE_API_TEST_URL = null;
	private String JAPPCORE_API_ACCESS_TOKEN = null;
	private Map<String, String> commandRequestMap = null;
	private Map<String, String> commandResultMap = null;
	private Properties testConfigProperties = null;

	private Verbosity verbosity = Verbosity.PAYLOAD_ANY;;
	private String logLevel = LoggingFeature.DEFAULT_LOGGER_LEVEL;

	private void init(String testPropertiesFileName) {
		logger.info("TestBase.init() - start........");
		if (StringUtil.isEmptyOrNull(testPropertiesFileName)) {
			testPropertiesFileName = JAPPCORE_BATCH_TEST_FILE_NAME;
		} else {
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
			throw new RuntimeException("can not load property file '/" + testPropertiesFileName + "' from classpath.",
					e);
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
				// only one url path entry in the property file.
				if (CONFIG_KEY_JAPPCORE_TEST_URL_PATH.equalsIgnoreCase(key)) {
					JAPPCORE_API_TEST_URL = testConfigProperties.getProperty(key);
					continue;
				}
				if (CONFIG_KEY_JAPPCORE_API_ACCESS_TOKEN.equalsIgnoreCase(key)) {
					JAPPCORE_API_ACCESS_TOKEN = testConfigProperties.getProperty(key);
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
		if (errorCommandList.size() > 0) {
			info = info + ", total error command detected: " + errorCommandList.size() + ", error command list: "
					+ errorCommandList;
		} else {
			info = info + ", total error command detected: 0 ";
		}
		logger.info(info);
	}

	public void setJappApiTestUrl(String url) {
		JAPPCORE_API_TEST_URL = url;
	}

	public Verbosity getVerbosity() {
		if (verbosity == null) {
			verbosity = Verbosity.PAYLOAD_ANY;
		}
		return verbosity;
	}

	public void setVerbosity(Verbosity verbosity) {
		if (verbosity == null) {
			verbosity = Verbosity.PAYLOAD_ANY;
		}
		this.verbosity = verbosity;
	}

	public String getLogLevel() {
		if (logLevel == null) {
			logLevel = LoggingFeature.DEFAULT_LOGGER_LEVEL;
		}
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		if (logLevel == null) {
			logLevel = LoggingFeature.DEFAULT_LOGGER_LEVEL;
		}
		this.logLevel = logLevel;
	}

	public List<String> batchTestRpcAPI(String testPropertiesFileName) {

		logger.info("batchTestRpcAPI()-  begin..........");
		init(testPropertiesFileName);

		if (StringUtil.isEmptyOrNull(JAPPCORE_API_TEST_URL)) {
			logger.info("batchTest() - JAPPCORE_API_TEST_URL is null or empty, do nothing......");
			throw new RuntimeException("batchTest() - JAPPCORE_API_TEST_URL is null or empty, do nothing......");
		}
		logger.info("batchTestRpcAPI() - JAPPCORE_API_TEST_URL  = " + JAPPCORE_API_TEST_URL);

		List<String> failCommandList = new ArrayList<String>();
		String rpcUrlPath = JAPPCORE_API_TEST_URL;

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

			logger.info("batchTest() - test order = " + order + ", test command = '" + command + "' jsonRequest:\n"
					+ jsonRequest);
			boolean result = testRpcAPI(rpcUrlPath, command, jsonRequest, assertresult, JAPPCORE_API_ACCESS_TOKEN, null, null);

			if (!result) {
				failCommandList.add(key);
			}
		} // end for

		if (failCommandList.isEmpty()) {
			logger.info("batchTest() - end........successfully......");
		} else {
			logger.error("batchTest() - end........failed command count = " + failCommandList.size()
					+ ", failed command List = \n" + failCommandList);
		}
		return failCommandList;
	}

	public boolean testRpcAPI(String rpcUrlPath, String command, String jsonRequestString,
			String assertionString, String accessToken,	String username, String password) {

		logger.info("testRpcAPI() - begin..........");
		if (StringUtil.isEmptyOrNull(rpcUrlPath)) {
			logger.info("testRpcAPI() - rpcUrlPath is null or empty, do nothing......");
			return false;
		}
		String targetUrl = rpcUrlPath;
		if(StringUtil.isEmptyOrNull(command)) {
			try {
				targetUrl =  UriBuilder.fromUri(targetUrl).queryParam("cmd", command).build().toURL().toString();
			} catch (Throwable t) {
				logger.info("testRpcAPI() - can not append cmd parameter, return fasle......");
				return false;
			}
		}
		if (StringUtil.isEmptyOrNull(assertionString)) {
			assertionString = "\"cmdStatusCode\":\"SUCCESS\"";
		}
		boolean result = testPost(targetUrl, MediaType.APPLICATION_JSON, jsonRequestString, 
				assertionString, accessToken, username, password);
		return result;
	}

	public boolean testPost(String targetURL, String mediaType, String requestString, String assertionString, String accessToken,
			String username, String password) {

		logger.info("testPost() - start...targetURL = " + targetURL + ".........");
		if (StringUtil.isEmptyOrNull(targetURL)) {
			logger.info("test() - targetURL is null or empty, do nothing......");
			return false;
		}
		if (mediaType == null) {
			mediaType = MediaType.APPLICATION_JSON;
		}

		Client client = ClientBuilder.newBuilder().property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, verbosity)
				.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, logLevel).build();

		WebTarget webTarget = client.target(targetURL);

		Invocation.Builder invocationBuilder = webTarget.request(mediaType);
		if (!StringUtil.isEmptyOrNull(accessToken)) {
			invocationBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		} else if (!StringUtil.isEmptyOrNull(username)) {
			invocationBuilder.header(HttpHeaders.AUTHORIZATION, AppUtil.getBasicAuthnHeader(username, password));
		}

		Response response = null;
		String stringResponse = null;
		if(StringUtil.isEmptyOrNull(requestString)) {
			if (mediaType.equals(MediaType.APPLICATION_JSON)) {
				response = invocationBuilder.post(Entity.json(requestString));
				stringResponse = response.readEntity(String.class);
			} else if (mediaType.equals(MediaType.APPLICATION_XML) ||
					mediaType.equals(MediaType.TEXT_XML) ) {
				response = invocationBuilder.post(Entity.xml(requestString));
				stringResponse = response.readEntity(String.class);
			}else if (mediaType.equals(MediaType.TEXT_HTML)) {
				response = invocationBuilder.post(Entity.html(requestString));
				stringResponse = response.readEntity(String.class);
			}else if (mediaType.equals(MediaType.TEXT_PLAIN)) {
				response = invocationBuilder.post(Entity.text(requestString));
				stringResponse = response.readEntity(String.class);
			}else {
				response = invocationBuilder.post(Entity.text(requestString));
				stringResponse = response.readEntity(String.class);
			}
		}else {
			logger.debug("testPost() - there is no request body provided.......");
			response = invocationBuilder.post(null);
			stringResponse = response.readEntity(String.class);
		}

		logger.info("testPost() - end......requestString = \n" + requestString
				+ ", stringResponse = \n" + stringResponse);
		if (StringUtil.isEmptyOrNull(stringResponse)) {
			return false;
		} else {
			if (StringUtil.isEmptyOrNull(assertionString)) {
				//no need to check assertion string, always return true
				return true;
			}else if(stringResponse.indexOf(assertionString) == -1) {
				return false;
			}
		}
		return true;
	}
	public boolean testGet(String targetURL, String mediaType, String assertionString, String accessToken,
			String username, String password) {

		logger.info("testPost() - start...targetURL = " + targetURL + ".........");
		if (StringUtil.isEmptyOrNull(targetURL)) {
			logger.info("test() - targetURL is null or empty, do nothing......");
			return false;
		}
		if (mediaType == null) {
			mediaType = MediaType.APPLICATION_JSON;
		}

		Client client = ClientBuilder.newBuilder().property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, verbosity)
				.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, logLevel).build();

		WebTarget webTarget = client.target(targetURL);

		Invocation.Builder invocationBuilder = webTarget.request(mediaType);
		if (!StringUtil.isEmptyOrNull(accessToken)) {
			invocationBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		} else if (!StringUtil.isEmptyOrNull(username)) {
			invocationBuilder.header(HttpHeaders.AUTHORIZATION, AppUtil.getBasicAuthnHeader(username, password));
		}

		Response response = null;
		String stringResponse = null;
		response = invocationBuilder.get();
		stringResponse = response.readEntity(String.class);

		logger.info("testGet() - end......stringResponse = \n" + stringResponse);
		if (StringUtil.isEmptyOrNull(stringResponse)) {
			return false;
		} else {
			if (StringUtil.isEmptyOrNull(assertionString)) {
				//no need to check assertion string, always return true
				return true;
			}else if(stringResponse.indexOf(assertionString) == -1) {
				return false;
			}
		}
		return true;
	}

}
