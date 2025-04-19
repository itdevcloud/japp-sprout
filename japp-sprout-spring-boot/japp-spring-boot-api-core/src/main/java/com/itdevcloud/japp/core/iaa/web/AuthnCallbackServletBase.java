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
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.LoginStateInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

public abstract class AuthnCallbackServletBase extends jakarta.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AuthnCallbackServletBase.class);

	abstract protected String getAuthnProvider();
	protected String getIdTokenParameterName() {
		//this is default idToken parameter name
		return "id_token";
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doPost(request, response);
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		AppUtil.initTransactionContext(request);
		try {
			logger.debug("AuthnCallbackServletBase.doPost().....start......");

			String idTokenParameterName = getIdTokenParameterName();
			if(StringUtil.isEmptyOrNull(idTokenParameterName)) {
				idTokenParameterName = "id_token";
			}
			String idToken = request.getParameter(idTokenParameterName);
			// log.debug("id_token......" + idToken);

			if (idToken == null || idToken.equals("")) {
				logger.error(
						"AuthnCallbackServletBase.doPost() - Authentication Failed. code E501. can not receive id_token from AAD response....");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1031");
				return;
			}
			// verify JWT from AAD;
			logger.debug("AuthnCallbackServletBase.doPost() - verify idToken token.........");
			if (!AppComponents.jwtService.isValidToken(idToken, getAuthnProvider())) {
				logger.error(
						"AuthnCallbackServletBase.doPost() - Authentication Failed. code E1032. id_token from AAD is not valid....");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1032");
				return;
			}
			// retrieve IaaUser for Authorization
			logger.debug("AuthnCallbackServletBase.doPost() - retrieve userInfo for Authorization........");
			AppIaaUser iaaUser = null;
			// set by isValidToken()
			String loginId = AppThreadContext.getTokenSubject();
			if (loginId == null) {
				// set by isValidTokenByPublicKey()
				logger.error(
						"AuthnCallbackServletBase.doPost() - Authentication Failed. code E1033. Login Id was not retrieved from AAD JWT Token. ");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1033");
				return;
			}
			try {
				iaaUser = AppComponents.iaaService.getIaaUserByLoginId(loginId, AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID);
				logger.debug("AuthnCallbackServletBase.doPost() - " + iaaUser.toString());
			} catch (AppException e1) {
				logger.error("Authentication Failed. code E1034 - can't retrieve user by loginid = " + loginId + " - \n"
						+ AppUtil.getStackTrace(e1));
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1034");
				return;
			} catch (Throwable t) {
				logger.error("Authentication Failed. code E1035 - can't retrieve user by loginid = " + loginId + " - \n"
						+ AppUtil.getStackTrace(t));
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1035");
				return;
			}

			// Application role list check
			if (!AppComponents.commonService.matchAppRoleList(iaaUser)) {
				logger.error(
						"Authentication Failed. code E1036 - requestor's is not on the APP's role list" + ".....");
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1036");
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
						"Authentication Failed. code E1037. JAPP Token can not be created for login Id '"
								+ loginId + "'......");
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E1037");
				return;
			}

			String state = (String) request.getParameter("state");
			LoginStateInfo stateInfo = LoginStateInfo.parseStateString(state);
			
			String appId = stateInfo==null? null:stateInfo.getAppId();
			
			IaaAppVO iaaAppVO = AppComponents.iaaAppInfoCache.getIaaAppInfo(appId);
			List<String> callbackUrlList = iaaAppVO.getAuthnCallbackURLs();
			String callbackUrl = (callbackUrlList==null||callbackUrlList.isEmpty())?null:callbackUrlList.get(0);
			
			String thisAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			if(StringUtil.isEmptyOrNull(appId) || appId.equalsIgnoreCase(thisAppId) ){
				//if same app, we can use cookie send-redirect
				//AppComponents.commonService.handleCookieResponse(response, token);		
				AppComponents.commonService.handlePostResponse(response, callbackUrl, token);	
			} else {
				//redirect to application's call back url
				//HTTP GET 
				if (StringUtil.isEmptyOrNull(callbackUrl)) {
					logger.error(
							"Authentication Failed. code E1038. Can't find application " + appId + " call back url from the property file.");
					AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E1038");
					return;
				}
				
//				if (url.toLowerCase().contains("localhost")) {
//					url.replaceAll("localhost", ip);
//				}
				AppComponents.commonService.handlePostResponse(response, callbackUrl, token);	
			}
									
		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
