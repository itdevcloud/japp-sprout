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
package com.itdevcloud.japp.core.iaa.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("login doGet =======begin==================");
		doPost(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("login doPost =======begin==================");

		// App CIDR white list check begin
		if (!AppComponents.commonService.matchAppIpWhiteList(request)) {
			logger.error(
					"Authorization Failed. Request IP is not on the App's IP white list, user IP = " + AppUtil.getClientIp(request) + ".....");
			AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed.");
			return;
		}
		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
		String url = retrieveLoginUrl(request);

		logger.debug("set login doGet Header with CORS ==========================");
		response.addHeader("Access-Control-Allow-Origin", origin);
		response.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
		response.addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization");

		PrintWriter out = response.getWriter();
		out.print("Loading the login page...");

		logger.info("doGet() redirect url============= " + url);
		if (url == null) {
			logger.error("url == null......can't redirect to provider's login page...... ");
			response.setStatus(401);
			return;

		}
		response.sendRedirect(url);
	}

	public String retrieveLoginUrl(HttpServletRequest request) {
		String provider = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER);
		if (provider == null || provider.trim().equals("")) {
			// default
			provider = AppConstant.IDENTITY_PROVIDER_AAD_OIDC;
		} else {
			provider = provider.trim();
		}
		UUID uuid = UUID.randomUUID();
		if (AppConstant.IDENTITY_PROVIDER_AAD_OIDC.equals(provider)) {
			String jappUserEmail = request.getParameter("JAPPCORE_USER_EMAIL");
			String clientId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_CLIENT_ID);
			String appId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			String prompt = getAadAuthPrompt();
			logger.debug("retrieveLoginUrl()......email cookie = " + jappUserEmail);
			String url = AppComponents.aadJwksCache.getAadAuthUri() + "?client_id=" + clientId
					+ "&response_type=id_token" + "&response_mode=form_post" + "&scope=openid" + "&state=" + appId
					+ prompt + "&nonce=" + uuid.toString();
			if(!StringUtil.isEmptyOrNull(jappUserEmail) ) {
				url = url + "&login_hint="+jappUserEmail;
			}
			return url;
		} 
//		else if (AppConstant.AUTH_PROVIDER_GENERAL_OAUTH2.equals(provider)) {
//			String server = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_AUTHROIZATION_URL);
//			String clientid = "?client_id=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_CLIENT_ID);
//			String redirecturi = "&redirect_uri=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_REDIRECT_URI);
//			String resource = "&resource=" +  ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_RESOURCE);
//			return server + clientid + redirecturi + resource;
//		} 
		else if (AppConstant.IDENTITY_PROVIDER_APP_LOCAL_BASIC.equals(provider)) {
			// SK-BASIC AUTH URL
			String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_BASIC_AUTHROIZATION_URL);			
			return url;
		}
		else if (AppConstant.IDENTITY_PROVIDER_APP_LOCAL_DYNAMIC.equals(provider)) {
			// JAPP-DYNAMIC URL
			String server = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_DYNAMIC_AUTHROIZATION_URL);
			String appid = "?appId=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			return server + appid;
		}
		else {
			return null;
		}

	}
	private String getAadAuthPrompt() {
		String aadAuthPrompt =  ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_AUTH_PROMPT);
		if ("login".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=login";
		} else if ("none".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=none";
		} else if ("consent".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=consent";
		} else {
			return "";
		}
	}

}
