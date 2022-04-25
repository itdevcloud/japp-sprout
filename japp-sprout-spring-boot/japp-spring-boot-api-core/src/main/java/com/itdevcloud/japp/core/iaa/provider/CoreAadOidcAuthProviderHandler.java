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

import java.io.IOException;

/**
 * Base Request Class.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class CoreAadOidcAuthProviderHandler extends BaseAuthProviderHandler {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CoreAadOidcAuthProviderHandler.class);

	public void handleUiLogin(HttpServletRequest request, HttpServletResponse response)  {


		String authProviderId = handlerInfo.authProvider.getAuthProviderId();
		String aadClientId = handlerInfo.authProvider.getAuthProperty("aad.client.id");
		String aadPrompt = handlerInfo.authProvider.getAuthProperty("aad.auth.prompt");
		String appCallbackUrl = handlerInfo.authProvider.getAuthAppCallbackUrl();
		String coreLoginId = handlerInfo.coreLoginId;
		String tokenNonce = handlerInfo.apiAuthInfo.tokenNonce;
		String clientAppId = handlerInfo.apiAuthInfo.clientAppId;
		String clientIP = handlerInfo.apiAuthInfo.clientIP;
		String clientAuthKey = handlerInfo.apiAuthInfo.clientAuthKey;
		
		String prompt = getAadAuthPrompt(aadPrompt);

		if (StringUtil.isEmptyOrNull(appCallbackUrl)) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "CoreAadOidcAuthProviderHandler - appCallbackUrl must be provided in the ClientAuthProvider! Check Provider configuration.");
			return ;
		}

		String url = AppComponents.aadJwksCache.getAadAuthUri() + "?client_id=" + aadClientId
				+ "&response_type=id_token" + "&response_mode=form_post" + "&scope=openid" + "&state=" + clientAppId 
				     + ";" + (clientIP==null?"":clientIP) + ";" + (clientAuthKey==null?"":clientAuthKey)
				+ prompt + "&nonce=" + tokenNonce + "&redirect_uri=" + appCallbackUrl;
		;
		if (!StringUtil.isEmptyOrNull(coreLoginId)) {
			url = url + "&login_hint=" + coreLoginId;
		}
		logger.info("AuthProvider - " + authProviderId + ", Login Redirect URL: " + url);
		
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("CoreAadOidcAuthProviderHandler - Can't redirect to AAD login page!", e);
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "CoreAadOidcAuthProviderHandler - "+ e.getMessage());
			return;
		}

	}


	private static String getAadAuthPrompt(String aadAuthPrompt) {
		if ("login".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=login";
		} else if ("none".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=none";
		} else if ("consent".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=consent";
		} else {
			return "&prompt=login";
		}
	}

	public void handleUiLogout(HttpServletRequest request, HttpServletResponse response)  {


		String authProviderId = handlerInfo.authProvider.getAuthProviderId();
		String aadClientId = handlerInfo.authProvider.getAuthProperty("aad.client.id");
		String aadPrompt = handlerInfo.authProvider.getAuthProperty("aad.auth.prompt");
		String appCallbackUrl = handlerInfo.authProvider.getAuthAppCallbackUrl();
		String coreLoginId = handlerInfo.coreLoginId;
		String tokenNonce = handlerInfo.apiAuthInfo.tokenNonce;
		String clientAppId = handlerInfo.apiAuthInfo.clientAppId;
		String clientAuthKey = handlerInfo.apiAuthInfo.clientAuthKey;
		

		String signoutAppRedirectUrl = handlerInfo.authProvider.getSignoutAppRedirectUrl();
			String url = AppComponents.aadJwksCache.getAadAuthLogoutUri();
			if(!StringUtil.isEmptyOrNull(signoutAppRedirectUrl)){
				url = url + "?post_logout_redirect_uri=" + signoutAppRedirectUrl;
			}
		
			logger.info("AuthProvider - " + authProviderId + ", Signout Redirect URL: " + url);
		
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("CoreAadOidcAuthProviderHandler - Can't redirect to AAD login page!", e);
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, "CoreAadOidcAuthProviderHandler - " + e.getMessage());
			return;
		}

	}

}
