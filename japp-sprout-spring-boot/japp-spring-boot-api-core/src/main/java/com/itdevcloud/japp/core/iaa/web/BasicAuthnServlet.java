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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;

import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@WebServlet(name = "basicAuthnServlet", urlPatterns = "/open/auth/basicauth")
public class BasicAuthnServlet extends jakarta.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	// private static final Logger logger =
	// LogManager.getLogger(BasicAuthServlet.class);
	private static final Logger logger = LogManager.getLogger(BasicAuthnServlet.class);

	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		AppUtil.initTransactionContext(httpRequest);
		try {
			logger.debug("BasicAuthServlet.doPost()...start.......");
			// App CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
				logger.error(
						"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = "
								+ AppUtil.getClientIp(httpRequest) + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E209");
				return;
			}

			AppIaaUser iaaUser = null;

			String[] basicInfo = AppUtil.parseHttpBasicAuthString(httpRequest);
			if (basicInfo == null || basicInfo.length != 2) {
				String errorMsg = "Authentication Failed. code E101 - can not parse basic authentication string from request....";
				logger.error(errorMsg);
				httpResponse.setStatus(401);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E101");
				return;
			}
			String id = basicInfo[0];
			String pwd = basicInfo[1];

			String userId = null;
			try {
				iaaUser = AppComponents.iaaService.loginByLoginIdPassword(id, pwd, null);
				if (iaaUser == null) {
					logger.error("Authentication Failed. code E102 - Can not retrive user by loginId '" + id
							+ "' and password.....");
					httpResponse.setStatus(401);
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E102");
					return;
				}
				userId = iaaUser.getUserIaaUID();
				AppThreadContext.setUserId(userId);

			} catch (AppException e) {
				logger.error("Authentication Failed. code E103 - " + e);
				logger.error(AppUtil.getStackTrace(e));
				httpResponse.setStatus(401);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E103");
				return;
			}

			// handle maintenance mode
			if (AppComponents.commonService.inMaintenanceMode(httpResponse, userId)) {
				return;
			}

			boolean handleMFA = AppUtil.handleMfa(httpRequest, httpResponse, iaaUser);
			if (!handleMFA) {
				// issue new JAPP JWT token;
				String token = AppComponents.jwtService.issueJappToken(iaaUser);
				if (StringUtil.isEmptyOrNull(token)) {
					logger.error(
							"BasicAuthServlet.doPost() - Authentication Failed. code E104. JAPP Token can not be created for login Id '"
									+ iaaUser.getLoginId() + "', userId = " + userId);
					httpResponse.setStatus(403);
					AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E104");
					return;
				}

				httpResponse.addHeader("Japp-Token", token);

				httpResponse.addHeader("Content-Security-Policy", "default-src 'self';");
				httpResponse.addHeader("X-XSS-Protection", "1; mode=block");
				httpResponse.setStatus(200);
				AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_SUCCESS, "succeed");
			}
			return;

		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
