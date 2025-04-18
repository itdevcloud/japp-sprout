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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.LoginStateInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * The LoginServlet redirects individual user login requests to the authentication provider
 * end point defined for the application.
 * 
 * <p>
 * Currently support following authentication provider.
 * <ul>
 * 		<li>Azure Entra ID(AAD) 
 * 		<li>Local basic authentication - check the username and password in an application's repository.
 * </ul>
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "loginServlet", urlPatterns = "/open/login")
public class LoginServlet extends jakarta.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//logger.debug("login doGet =======begin==================");
		doPost(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("login doPost =======begin==================");

		String state = request.getParameter("state");
		LoginStateInfo stateInfo = LoginStateInfo.parseStateString(state);
		
		String clientAppId = stateInfo==null?null:stateInfo.getAppId();
		String thisAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);;
		if(StringUtil.isEmptyOrNull(clientAppId)) {
			logger.debug("clientAppId is not provided as request parameter, use this server's appId: " + thisAppId);
			clientAppId = thisAppId;
		}
		//update stateInfo (appId may be changed from null to thisAppId
		LoginStateInfo newStateInfo = new LoginStateInfo();
		newStateInfo.setAppId(clientAppId);
		newStateInfo.setLoginId(stateInfo==null?null:stateInfo.getLoginId());
		newStateInfo.setAuthnCallbackURL(stateInfo==null?null:stateInfo.getAuthnCallbackURL());
		String newStateInfoString = newStateInfo.createStateString();
		
		IaaAppVO iaaAppVO = AppComponents.iaaAppInfoCache.getIaaAppInfo(clientAppId);
		if(iaaAppVO == null) {
			logger.error(
					"Authenitcaton Failed - can not retrieve Client APP configuration, clientAppId = " + clientAppId + ".....");
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authenitcaton Failed. code E1001");
			return;
		}
		// individual user, do not check APP IP whitelist

		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
		String url = AppComponents.iaaService.getAuthnProviderURL(request, iaaAppVO, newStateInfoString);
		if (url == null) {
			logger.error(
					"Authenitcaton Failed - can not retrieve authn login redirect URL, clientAppId = " + clientAppId + ".....");
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authenitcaton Failed. code E1002");
			return;
		}

		logger.debug("set login doGet Header with CORS ==========================");
		response.addHeader("Access-Control-Allow-Origin", origin);
		response.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
		response.addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization");

		PrintWriter out = response.getWriter();
		out.print("Loading the login page...");

		logger.debug("doPost() redirect url==== " + url);
		
		//=====to be removed, for testing only=====
		if(url.indexOf("localhost:8443/open/auth/basic") > 0){
			//for testing
			handleBasicAuthTest(response) ; {
				return;
			}
		}
		//=====to be removed, for testing only=====
		response.sendRedirect(url);
	}

	
	private void handleBasicAuthTest(HttpServletResponse response) throws IOException {

		// ===load token page===
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = this.getClass().getResourceAsStream("/sample_basic_authn.html");
			if (inputStream == null) {
				throw new RuntimeException("can not load sample_basic_authn html file.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
		String htmlText = sb.toString();
//		htmlText = htmlText.replaceAll("@token@", token);
//		htmlText = htmlText.replaceAll("@action@", postUrl);
		response.setContentType("text/html");
		response.setStatus(200);
		PrintWriter out = response.getWriter();
		out.println(htmlText);
		out.flush();
		out.close();

		return;
	}
	
	
	
//	public String retrieveAuthnProviderUrl(HttpServletRequest request) {
//		String appId = request.getParameter("appId");
//		String currentAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);;
//		if(StringUtil.isEmptyOrNull(appId)) {
//			logger.debug("appId is not provided as request parameter, use default appId: " + currentAppId);
//			appId = currentAppId;
//		}
//		IaaAppVO iaaAppVO = AppComponents.iaaAppInfoCache.getIaaAppInfo(appId);
//		String provider = null;
//		if(iaaAppVO == null) {
//			provider = null;
//		}else {
//		    provider = iaaAppVO.getAuthnProvider();
//		}
//		
//		//String provider = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER);
//		if (provider == null || provider.trim().equals("")) {
//			// default
//			//provider = AppConstant.AUTH_PROVIDER_AAD_OPENID;
//			logger.debug("can not find AppInfo or AuthnProvider configuration for the AppiId: " + appId + ", use this app  ID as authn provider!");
//			provider = appId;
//		} 
//		UUID uuid = UUID.randomUUID();
//		if (AppConstant.AUTH_PROVIDER_AAD_OPENID.equals(provider)) {
//			String bitsUserEmail = getCookieValue(request, "BITS_USER_EMAIL");
//			String jappUserEmail = request.getParameter("JAPPCORE_USER_EMAIL");
//			String clientId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_CLIENT_ID);
//			//String appId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
//			String prompt = getAadAuthPrompt();
//			logger.debug("retrieveLoginUrl()......email cookie = " + jappUserEmail);
//			String url = AppComponents.aadJwksCache.getAadAuthUri() + "?client_id=" + clientId
//					+ "&response_type=id_token" + "&response_mode=form_post" + "&scope=openid" + "&state=" + appId
//					+ prompt + "&nonce=" + uuid.toString();
//			if(!StringUtil.isEmptyOrNull(jappUserEmail) ) {
//				url = url + "&login_hint="+jappUserEmail;
//			}
//			return url;
//		} 
//		else if (AppConstant.AUTH_PROVIDER_GENERAL_OAUTH2.equals(provider)) {
//			String server = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_AUTHROIZATION_URL);
//			String clientid = "?client_id=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_CLIENT_ID);
//			String redirecturi = "&redirect_uri=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_REDIRECT_URI);
//			String resource = "&resource=" +  ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.OAUTH2_RESOURCE);
//			return server + clientid + redirecturi + resource;
//		} 
//		else if (currentAppId.equalsIgnoreCase(provider)) {
//			// BASIC AUTH URL
//			String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_BASIC_AUTHENTICATION_URL);			
//			return url;
//		}
//		else if (AppConstant.AUTH_PROVIDER_JAPPCORE_DYNAMIC.equals(provider)) {
//			// JAPP-DYNAMIC URL
//			String server = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_DYNAMIC_AUTHENTICATION_URL);
//			String appid = "?appId=" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
//			return server + appid;
//		}
//		else {
//			return null;
//		}
//
//	}
//	private String getAadAuthPrompt() {
//		String aadAuthPrompt =  ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_AUTH_PROMPT);
//		if ("login".equalsIgnoreCase(aadAuthPrompt)) {
//			return "&prompt=login";
//		} else if ("none".equalsIgnoreCase(aadAuthPrompt)) {
//			return "&prompt=none";
//		} else if ("consent".equalsIgnoreCase(aadAuthPrompt)) {
//			return "&prompt=consent";
//		} else {
//			return "";
//		}
//	}

}
