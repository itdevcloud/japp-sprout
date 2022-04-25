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

import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.BasicCredential;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.core.iaa.provider.AuthProviderHandlerInfo;
import com.itdevcloud.japp.core.iaa.provider.BaseAuthProviderHandler;
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
@WebServlet(name = "coreBasicAuthServlet", urlPatterns = "/open/core/basicauth")
public class CoreBasicAuthServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CoreBasicAuthServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

//		AppUtil.initTransactionContext(request);
		try {
			
			logger.debug("coreBasicAuthServlet.doPost()...start.......");
			
			TransactionContext txContext = AppThreadContext.getTransactionContext();
			ApiAuthInfo apiAuthInfo = txContext.getApiAuthInfo();
			
			String errMsg = null;
			AuthProviderHandlerInfo handlerInfo = BaseAuthProviderHandler.getValidatedHandlerInfo(request, response);			
			if (handlerInfo == null) {
				errMsg = "doPost() - can get AuthProviderHandlerInfo from request!" ;
				logger.error(errMsg);
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMsg);
				return ;
			}
			
			BasicCredential basicCredential = AppUtil.getBasicCredential(request);
			if(basicCredential == null) {
				String msg = "Can not get username/loginId information from request......";
				logger.error(msg);
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, msg);
					return;
			}
			
			//String userIp = AppUtil.getClientIp(request);
			String userIp = apiAuthInfo.clientIP;
			
			// App CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(request)) {
				logger.error(
						"Authorization Failed. Request IP is not in the APP's IP white list, user IP = "
								+ userIp + ".....");
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. ");
				return;
			}

			IaaUserI iaaUser = null;
			try {
				iaaUser = AppComponents.iaaService.getIaaUserFromRepositoryByLoginId(basicCredential.getLoginId(), basicCredential.getPassword());
				if (iaaUser == null) {
					errMsg = "Authentication Failed. code E102 - Can not retrive user by loginId '" + basicCredential.getLoginId()
							+ "' and password.....";
					logger.error(errMsg);
					response.setStatus(401);
					AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMsg);
					return;
				}

			} catch (AppException e) {
				errMsg = "Authentication Failed - " + e;
				logger.error(errMsg);
				logger.error(CommonUtil.getStackTrace(e));
				response.setStatus(401);
				AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMsg);
				return;
			}

			// handle maintenance mode
			if (AppComponents.commonService.inMaintenanceMode(response, basicCredential.getLoginId())) {
				return;
			}
			
			//Force to check nonce and ip if token has them as claims
			if (!StringUtil.isEmptyOrNull(userIp)) {
				iaaUser.setHashedUserIp(Hasher.hashPassword(userIp));
			}else {
				iaaUser.setHashedUserIp(null);
			}
			if (!StringUtil.isEmptyOrNull(handlerInfo.apiAuthInfo.tokenNonce)) {
				iaaUser.setHashedNonce(Hasher.hashPassword(handlerInfo.apiAuthInfo.tokenNonce));
			}else {
				iaaUser.setHashedNonce(null);
			}
			// issue Core JWT token;
			String token = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN, null);
			
			if (StringUtil.isEmptyOrNull(token)) {
				errMsg = "doPost() - Authentication Failed. Token can not be created for login Id: "
						+ basicCredential.getLoginId();
				logger.error(errMsg);
				response.setStatus(403);
				AppUtil.setHttpResponse(response, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMsg);
				return;
			}
			apiAuthInfo.token = token;
			AppComponents.commonService.handleClientAuthCallbackResponse(response, apiAuthInfo);

			return;

		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
