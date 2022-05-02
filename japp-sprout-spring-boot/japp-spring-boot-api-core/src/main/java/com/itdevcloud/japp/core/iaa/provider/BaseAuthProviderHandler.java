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

import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.common.TransactionContext;
import com.itdevcloud.japp.se.common.util.StringUtil;

public abstract class BaseAuthProviderHandler implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(BaseAuthProviderHandler.class);
    protected AuthProviderHandlerInfo handlerInfo;
    
	public static AuthProviderHandlerInfo getValidatedHandlerInfo(HttpServletRequest request, HttpServletResponse response)  {

		String errMsg = null;
		if (request == null || response == null) {
			errMsg = "getValidatedHandlerInfo() - request and/or response can not be null!" ;
			logger.error(errMsg);
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
			return null;
		}
		//TransactionContext txContext = AppThreadContext.getTransactionContext();
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();
		
		String clientAppId = apiAuthInfo.clientAppId;
		String clientAuthKey = apiAuthInfo.clientAuthKey;
		String tokenNonce = apiAuthInfo.tokenNonce;
		String coreLoginId = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_LOGIN_ID);
		
		if (StringUtil.isEmptyOrNull(tokenNonce)) {
			errMsg = "getValidatedHandlerInfo() - tokenNonce must be provided!";
			logger.error(errMsg);
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
			return null;
		}
		ClientAuthProvider authProvider = AppComponents.clientAppInfoCache.getClientAuthProvider(clientAppId, clientAuthKey);

		if (authProvider == null) {
			errMsg = "getValidatedHandlerInfo() - can not retrieve ClientAuthProvider! clientAppId = " + clientAppId + ", clientAuthKey = " + clientAuthKey;
			logger.error(errMsg);
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
			return null;
		}
		
		AuthProviderHandlerInfo handlerInfo = new AuthProviderHandlerInfo();
		handlerInfo.apiAuthInfo = apiAuthInfo;
		handlerInfo.apiAuthInfo.clientAppId = clientAppId;
		handlerInfo.apiAuthInfo.clientAuthKey = clientAuthKey;
		handlerInfo.apiAuthInfo.tokenNonce = tokenNonce;
		handlerInfo.coreLoginId = coreLoginId;
		handlerInfo.apiAuthInfo.clientIP = AppUtil.getClientIp(request);
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
			handler =  AppFactory.getInstance(CoreBasicAuthProviderHandler.class);
		}else {
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY_AUTHENTICATION, "AuthProviderId is not supported at this time, check code or configuration! authProviderId = " + authProviderId );
			return null;
		}
		handler.handlerInfo = handlerInfo;
		return handler;
	}
	
	public abstract void handleUiLogin(HttpServletRequest request, HttpServletResponse response) ;
	public abstract void handleUiLogout(HttpServletRequest request, HttpServletResponse response) ;
	
}
