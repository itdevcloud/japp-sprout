package com.itdevcloud.japp.core.test;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.core.common.TestBase;

import jakarta.ws.rs.core.MediaType;


public class LoginTest extends TestBase {

	private static Logger logger = LogManager.getLogger(LoginTest.class);


	@BeforeEach
	public void setup() {
	}

	@Disabled
	public void methodYouWantIgnored() {
	}

	@Test
	public void runLoginTest() {

		logger.info("runTest() begin...............");
		String urlPath = "http://localhost:8080/open/login";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("App", "Japp");
		params.put("CallbackURL", "http://localhost:8080/aadauth");
		boolean result = testGet(urlPath, MediaType.TEXT_PLAIN, null, null, null, null);
		
		assertTrue(result);
	}

}
