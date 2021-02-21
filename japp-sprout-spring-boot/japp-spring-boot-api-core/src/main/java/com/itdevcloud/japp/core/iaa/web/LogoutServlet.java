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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.cahce.AadJwksCache;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
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
public class LogoutServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("logout get service =======111==================");

		// App CIDR white list check begin
		if (!AppComponents.commonService.matchAppIpWhiteList(request)) {
			logger.error(
					"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = " + AppUtil.getClientIp(request) + ".....");
			AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. code E209");
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
			provider = AppConstant.IDENTITY_PROVIDER_AAD_OIDC;
		} else {
			provider = provider.trim();
		}
		String jappPostSignOutUri = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_POST_SIGNOUT_PAGE);
		if (AppConstant.IDENTITY_PROVIDER_AAD_OIDC.equals(provider)) {
			jappPostSignOutUri = jappPostSignOutUri.replace("/#/", "/%23/");
			String url = AppComponents.aadJwksCache.getAadAuthLogoutUri();
			if(!StringUtil.isEmptyOrNull(jappPostSignOutUri)){
				url = url + "?post_logout_redirect_uri=" + jappPostSignOutUri;
			}
			return url;
		} 
//		else if (AppConstant.AUTH_PROVIDER_GENERAL_OAUTH2.equals(provider)) {
//			// SDC STS URL
//			return jappPostSignOutUri;
//		} 
		else {
			return jappPostSignOutUri;
		}

	}

}


