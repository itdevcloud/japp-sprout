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
package com.itdevcloud.japp.core.iaa.provider;



/**
 * Base Request Class.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public abstract class BaseAuthProviderHandler implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(BaseAuthProviderHandler.class);
    protected AuthProviderHandlerInfo handlerInfo;
    
	private static AuthProviderHandlerInfo getValidatedHandlerInfo(HttpServletRequest request, HttpServletResponse response)  {

		if (request == null || response == null) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "Request and/or Respone Parameters can not be null!");
			return null;
		}
		
		String clientAppId = request.getParameter("clientAppId");
		String clientAuthKey = request.getParameter("clientAuthKey");
		String tokenNonce = request.getParameter("tokenNonce");
		String loginUserEmail = request.getParameter("loginUserEmail");

		if (StringUtil.isEmptyOrNull(clientAppId) || StringUtil.isEmptyOrNull(tokenNonce)) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "clientAppId and tokenNonce must be provided in the request! ");
			return null;
		}
		ClientAuthProvider authProvider = AppComponents.iaaService.getClientAuthProvider(clientAppId, clientAuthKey);

		if (authProvider == null) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "Can not get Auth provider !, check code or configuration! clientAppId = " + clientAppId + ", clientAuthKey = " + clientAuthKey);
			return null;
		}
		String authProviderId = authProvider.getAuthProviderId();
		String appCallbackUrl = authProvider.getAuthAppCallbackUrl();
		
		if (StringUtil.isEmptyOrNull(authProviderId) || StringUtil.isEmptyOrNull(appCallbackUrl)) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "authProviderId and appCallbackUrl must be provided in the ClientAuthProvider! Check Provider configuration.");
			return null;
		}
		
		AuthProviderHandlerInfo handlerInfo = new AuthProviderHandlerInfo();
		handlerInfo.clientAppId = clientAppId;
		handlerInfo.clientAuthKey = clientAuthKey;
		handlerInfo.tokenNonce = tokenNonce;
		handlerInfo.loginUserEmail = loginUserEmail;
		handlerInfo.clientIP = AppUtil.getClientIp(request);
		handlerInfo.authProvider = authProvider;
		
		return handlerInfo;
	}
	
	public static BaseAuthProviderHandler getHandler(HttpServletRequest request, HttpServletResponse response)  {
		AuthProviderHandlerInfo handlerInfo = getValidatedHandlerInfo(request, response);
		if(handlerInfo == null) {
			return null;
		}
		BaseAuthProviderHandler handler = null;
		String authProviderId = handlerInfo.authProvider.getAuthProviderId();
		if(AppConstant.IDENTITY_PROVIDER_CORE_AAD_OIDC.equalsIgnoreCase(authProviderId)) {
			handler =  AppFactory.getInstance(CoreAadOidcAuthProviderHandler.class);
		}else if (AppConstant.IDENTITY_PROVIDER_CORE_BASIC.equalsIgnoreCase(authProviderId)) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "AuthProviderId is not supported at this time, check code or configuration! authProviderId = " + authProviderId );
			return null;
		}else {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "AuthProviderId is not supported at this time, check code or configuration! authProviderId = " + authProviderId );
			return null;
		}
		handler.handlerInfo = handlerInfo;
		return handler;
	}
	
	public abstract void handleUiLogin(HttpServletRequest request, HttpServletResponse response) ;
	public abstract void handleUiLogout(HttpServletRequest request, HttpServletResponse response) ;
	
}
