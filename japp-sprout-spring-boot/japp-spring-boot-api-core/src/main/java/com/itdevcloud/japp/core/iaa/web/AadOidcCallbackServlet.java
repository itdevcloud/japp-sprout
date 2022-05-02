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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.azure.AadIdTokenHandler;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This servlet is designed as a call back class for Azure AD authentication
 * provider. It provides the authentication and authorization services.
 * <p>
 * First, it receives a JWT issued by Azure AD, then this servlet will verify if
 * this JWT is valid. After check if this is an authorized user, an application
 * specific JWT will be issued to the client.
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "aadOidcCallbackServlet", urlPatterns = "/open/aadauth")
public class AadOidcCallbackServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AadOidcCallbackServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		AppUtil.initTransactionContext(request);
		try {
			logger.debug("aadOidcCallbackServlet.doPost().....start......");

			String idToken = request.getParameter("id_token");
			// log.debug("id_token=========" + idToken);

			if (idToken == null || idToken.equals("")) {
				logger.error(
						"aadOidcCallbackServlet.doPost() - Authorization Failed. code E501. can not receive id_token from AAD response....");
				AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION,
						"Authorization Failed. ");
				return;
			}
			// verify JWT from AAD;
			logger.debug("aadOidcCallbackServlet.doPost() - verify idToken token=========" );
			TokenHandlerI aadIdTokenHandler = AppFactory.getTokenHandler(AadIdTokenHandler.class.getSimpleName());
			if (aadIdTokenHandler.isValidToken(idToken, null, true, null) == null) {
				logger.error(
						"aadOidcCallbackServlet.doPost() - Authorization Failed. id_token from AAD is not valid....");
				AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION,
						"Authorization Failed. ");
				return;
			}
			
			String state = (String) request.getParameter("state");
			String[] arr = state.split(";");
			String clientAppId = null;
			String clientIP = null;
			String clientAuthKey = null;
			if (arr != null) {
				if (arr.length > 0) {
					clientAppId = arr[0];
				}
				if (arr.length > 1) {
					clientIP = arr[1];
				}
				if (arr.length > 2) {
					clientAuthKey = arr[2];
				}
			}

			
			// retrieve IaaUser for Authorization
			logger.debug("aadOidcCallbackServlet.doPost() - retrieve userInfo for Authorization==============");
			IaaUserI iaaUser = null;
			try {
				iaaUser = aadIdTokenHandler.getIaaUserBasedOnToken(idToken);
				logger.debug("aadOidcCallbackServlet.doPost() - IaaUser: " + iaaUser);
				if (iaaUser == null) {
					logger.error("Authentication / Authorization Failed - can't retrieve user ......");
					AppUtil.setHttpResponse(response, 403, Status.ERROR_SECURITY_AUTHENTICATION,
							"Authentication / Authorization Failed.");
					return;
				}
			} catch (Throwable t) {
				logger.error("Authentication / Authorization Failed - can't retrieve user ......" 
						+ "\n" + CommonUtil.getStackTrace(t));
				AppUtil.setHttpResponse(response, 403, Status.ERROR_SECURITY_AUTHENTICATION,
						"Authorization Failed. ");
				return;
			}
			String loginId = iaaUser.getLoginId();
			// Application role list check
			if (!AppComponents.commonService.matchAppRoleList(iaaUser)) {
				logger.error("Authorization Failed - requestor's is not on the APP's role list" + ".....");
				AppUtil.setHttpResponse(response, 403, Status.ERROR_SECURITY_AUTHENTICATION,
						"Authorization Failed. ");
				return;
			}

			// handle maintenance mode
			if (AppComponents.commonService.inMaintenanceMode(response, loginId)) {
				return;
			}

			// issue new JAPP JWT token;
			if(!StringUtil.isEmptyOrNull(clientIP)) {
				iaaUser.setHashedUserIp(Hasher.hashPassword(clientIP));
			}
			
			String token = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN, null);
			if (StringUtil.isEmptyOrNull(token)) {
				logger.error(
						"AadAuthCallbackServlet.doPost() - Authorization Failed. Token can not be created for login Id '"
								+ loginId + "'......");
				AppUtil.setHttpResponse(response, 403, Status.ERROR_SECURITY_AUTHENTICATION,
						"Authorization Failed. ");
				return;
			}
			ApiAuthInfo apiAuthInfo = new ApiAuthInfo();
			apiAuthInfo.clientAppId = clientAppId;
			apiAuthInfo.clientAuthKey = clientAuthKey;
			apiAuthInfo.token = token;
			AppComponents.commonService.handleClientAuthCallbackResponse(response, apiAuthInfo);

		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
