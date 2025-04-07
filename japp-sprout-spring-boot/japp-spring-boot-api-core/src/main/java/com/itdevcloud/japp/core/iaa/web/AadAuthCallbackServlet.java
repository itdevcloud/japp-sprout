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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;

import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.azure.AzureJwksService;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This servlet is designed as a call back class for Azure AD authentication provider. It 
 * provides the authentication and authorization services.
 * <p>
 * First, it receives a JWT issued by Azure AD, then this servlet will 
 * verify if this JWT is valid. After check if this is an authorized user, 
 * an application specific JWT will be issued to the client.
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "aadAuthCallbackServlet", urlPatterns = "/aadauth")
public class AadAuthCallbackServlet extends jakarta.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	//private static final Logger logger = LogManager.getLogger(AadAuthCallbackServlet.class);
	private static final Logger logger = LogManager.getLogger(AadAuthCallbackServlet.class);


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		AppUtil.initTransactionContext(request);
		try {
			logger.debug("AadAuthCallbackServlet.doPost().....start......");


			String idToken = request.getParameter("id_token");
			// log.debug("id_token=========" + idToken);

			if (idToken == null || idToken.equals("")) {
				logger.error(
						"AadAuthCallbackServlet.doPost() - Authorization Failed. code E501. can not receive id_token from AAD response....");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E501");
				return;
			}
			// verify JWT from AAD;
			logger.debug("AadAuthCallbackServlet.doPost() - verify idToken token=========");
			if (!AppComponents.jwtService.isValidAadIdToken(idToken)) {
				logger.error(
						"AadAuthCallbackServlet.doPost() - Authorization Failed. code E502. id_token from AAD is not valid....");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E502");
				return;
			}
			// retrieve IaaUser for Authorization
			logger.debug("AadAuthCallbackServlet.doPost() - retrieve userInfo for Authorization==============");
			IaaUser iaaUser = null;
			String loginId = AppThreadContext.getTokenSubject();
			if (loginId == null) {
				// set by isValidTokenByPublicKey()
				logger.error(
						"AadAuthCallbackServlet.doPost() - Authorization Failed. code E503. Login Id was not retrieved from AAD JWT Token. ");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E503");
				return;
			}
			try {
				iaaUser = AppComponents.iaaService.getIaaUserByLoginId(loginId, AppConstant.AUTH_PROVIDER_AAD_OPENID);
				logger.debug("AadAuthCallbackServlet.doPost() - " + iaaUser.toString());
			} catch (AppException e1) {
				logger.error("Authorization Failed. code E504 - can't retrieve user by loginid = " + loginId + " - \n"
						+ AppUtil.getStackTrace(e1));
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E504");
				return;
			} catch (Throwable t) {
				logger.error("Authorization Failed. code E505 - can't retrieve user by loginid = " + loginId + " - \n"
						+ AppUtil.getStackTrace(t));
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E505");
				return;
			}

			// Application role list check
			if (!AppComponents.commonService.matchAppRoleList(iaaUser)) {
				logger.error(
						"Authorization Failed. code E508 - requestor's is not on the APP's role list" + ".....");
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E508");
				return;
			}

			// handle maintenance mode
			if (AppComponents.commonService.inMaintenanceMode(response, loginId)) {
				return;
			}

			// issue new JAPP JWT token;
			String token = AppComponents.jwtService.issueJappToken(iaaUser);
			if (StringUtil.isEmptyOrNull(token)) {
				logger.error(
						"AadAuthCallbackServlet.doPost() - Authorization Failed. code E507. JAPP Token can not be created for login Id '"
								+ loginId + "'......");
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E507");
				return;
			}

			String state = (String) request.getParameter("state");
			String[] arr = state.split(":");
			String appId = null;
			String ip = null;
			if (arr != null) {
				if (arr.length > 0) {
					appId = arr[0];
				}
				if (arr.length > 1) {
					ip = arr[1];
				}
			}
			String currentappId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			if(StringUtil.isEmptyOrNull(appId) || appId.equalsIgnoreCase(currentappId) ){
				//if same app, we can use send-redirect
				AppComponents.commonService.handleResponse(response, token);		
			} else {
				//redirect to application's call back url
				//HTTP GET 
				String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTH_APP_CALLBACK_URL);
				if (StringUtil.isEmptyOrNull(url)) {
					logger.error(
							"AadAuthCallbackServlet.doPost() - Authorization Failed. code E509. Can't find application " + appId + " call back url from the property file.");
					AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authorization Failed. code E509");
					return;
				}
				
				if (url.toLowerCase().contains("localhost")) {
					url.replaceAll("localhost", ip);
				}
				
				//produce a post form page
			    response.setContentType("text/html");
			    response.setStatus(200);
			    PrintWriter out = response.getWriter();
			    out.println("<!DOCTYPE html>\r\n" + 
			    		"<html>\r\n" + 
			    		"<head> \r\n" + 
			    		"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n" + 
			    		"    <script type=\"text/javascript\">\r\n" + 
			    		"        function submitpage() {\r\n" + 
			    		"			document.forms['token_redirect'].submit();" +
			    		" 		}\r\n" + 
			    		"    </script>\r\n" + 
			    		"</head>\r\n" + 
			    		"<body onLoad=\"submitpage()\">\r\n" + 
			    		"<form name=\"token_redirect\" action=\"");
			    out.print(url);
			    out.print("\" method=\"post\">\r\n" + 
			    		"    <input type=\"hidden\" name=\"CallingApp-Token" +"\" value=\"");
			    out.print(token);
			    out.println("\" />\r\n" + 
			    		"</form>\r\n" + 
			    		"</body>\r\n" + 
			    		"</html>");
			    out.flush();
			    out.close();
			}
									
		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
