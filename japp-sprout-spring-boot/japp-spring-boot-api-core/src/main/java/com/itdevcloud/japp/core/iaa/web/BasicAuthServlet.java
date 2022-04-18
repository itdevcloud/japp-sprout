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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@WebServlet(name = "basicAuthServlet", urlPatterns = "/open/basicauth")
public class BasicAuthServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(BasicAuthServlet.class);

	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		AppUtil.initTransactionContext(httpRequest);
		try {
			logger.debug("BasicAuthServlet.doPost()...start.......");
			String userIp = AppUtil.getClientIp(httpRequest);
			// App CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
				logger.error(
						"Authorization Failed. Request IP is not on the APP's IP white list, user IP = "
								+ userIp + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. ");
				return;
			}

			IaaUserI iaaUser = null;

			String[] basicInfo = AppUtil.parseHttpBasicAuthString(httpRequest);
			if (basicInfo == null || basicInfo.length != 2) {
				String errorMsg = "Authentication Failed. Can not parse basic authentication string from request....";
				logger.error(errorMsg);
				httpResponse.setStatus(401);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. ");
				return;
			}
			String loginId = basicInfo[0];
			String pwd = basicInfo[1];

			try {
				iaaUser = AppComponents.iaaService.getIaaUserFromRepositoryByLoginId(loginId, pwd);
				if (iaaUser == null) {
					logger.error("Authentication Failed. code E102 - Can not retrive user by loginId '" + loginId
							+ "' and password.....");
					httpResponse.setStatus(401);
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E102");
					return;
				}

			} catch (AppException e) {
				logger.error("Authentication Failed. code E103 - " + e);
				logger.error(CommonUtil.getStackTrace(e));
				httpResponse.setStatus(401);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E103");
				return;
			}

			// handle maintenance mode
			if (AppComponents.commonService.inMaintenanceMode(httpResponse, loginId)) {
				return;
			}
			
			String nonce = httpRequest.getHeader(AppConstant.HTTP_TOKEN_NONCE_HEADER_NAME);
			
			
			//Force to check nonce and ip if token has them as claims
			if (!StringUtil.isEmptyOrNull(userIp)) {
				iaaUser.setHashedUserIp(Hasher.hashPassword(userIp));
			}else {
				iaaUser.setHashedUserIp(null);
			}
			if (!StringUtil.isEmptyOrNull(nonce)) {
				iaaUser.setHashedNonce(Hasher.hashPassword(nonce));
			}else {
				iaaUser.setHashedNonce(null);
			}
			// issue new JAPP JWT token;
			String token = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN);
			
			if (StringUtil.isEmptyOrNull(token)) {
				logger.error(
						"BasicAuthServlet.doPost() - Authentication Failed. Token can not be created for login Id '"
								+ loginId);
				httpResponse.setStatus(403);
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. ");
				return;
			}

			httpResponse.addHeader("Token", token);

			httpResponse.addHeader("Content-Security-Policy", "default-src 'self';");
			httpResponse.addHeader("X-XSS-Protection", "1; mode=block");
			httpResponse.addHeader("Access-Control-Expose-Headers", "Token, MaintenanceMode");
			httpResponse.setStatus(200);
			AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_SUCCESS, "succeed");
			return;

		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
