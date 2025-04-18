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

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.LoginStateInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.cahce.EntraIdJwksCache;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * The LogoutServlet redirects client's logout requests to the predefined logout endpoint.
 * <p>
 * We support three authentication types.
 * <ul>
 * 		<li>Azure Active Directory(AAD) - using Azure AD as the authentication provider.
 * 		<li>SdcSTS - using SdcSTS as the authentication provider. SdcSTS is an implementation of STS provided by .NET SDC.
 * 		<li>Basic authentication - check the username and password in an application's repository.
 * </ul>
 * @author Ling Yang
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "logoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends jakarta.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	//private static final Logger logger = LogManager.getLogger(LogoutServlet.class);
	private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("logout get service =======111==================");

		String state = request.getParameter("state");
		LoginStateInfo stateInfo = LoginStateInfo.parseStateString(state);
		String clientAppId = stateInfo==null?null:stateInfo.getAppId();
		String thisAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);;
		if(StringUtil.isEmptyOrNull(clientAppId)) {
			logger.debug("clientAppId is not provided as request parameter, use this server's appId: " + thisAppId);
			clientAppId = thisAppId;
		}
		IaaAppVO iaaAppVO = AppComponents.iaaAppInfoCache.getIaaAppInfo(clientAppId);
		if(iaaAppVO == null) {
			logger.error(
					"Authenitcaton Failed - can not retrieve Client APP configuration, clientAppId = " + clientAppId + ".....");
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authenitcaton Failed. code E1001");
			return;
		}

		HttpSession session = request.getSession();

		// clean user reference in Cache
		String id = (String) session.getAttribute("userid");

		// invalidate session
		session.invalidate();

		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
		String url = retrieveLogoutUrl();
		response.addHeader("Access-Control-Allow-Origin", origin);
		response.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
		response.addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization");

		logger.info("redirect url============= " + url);
		if (url == null) {
			logger.error("url == null......can't redirect to provider's logout page...... ");
			response.setStatus(401);
			return;

		}
		response.sendRedirect(url);
	}

	public String retrieveLogoutUrl() {
		String provider = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER);
		if (provider == null || provider.trim().equals("")) {
			// default
			provider = AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID;
		} else {
			provider = provider.trim();
		}
		String jappPostSignOutUri = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_POST_SIGNOUT_PAGE);
		if (AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equals(provider)) {
			jappPostSignOutUri = jappPostSignOutUri.replace("/#/", "/%23/");
			String url = AppComponents.aadJwksCache.getAadAuthLogoutUri();
			if(!StringUtil.isEmptyOrNull(jappPostSignOutUri)){
				url = url + "?post_logout_redirect_uri=" + jappPostSignOutUri;
			}
			return url;
		}  else {
			return jappPostSignOutUri;
		}

	}

}


