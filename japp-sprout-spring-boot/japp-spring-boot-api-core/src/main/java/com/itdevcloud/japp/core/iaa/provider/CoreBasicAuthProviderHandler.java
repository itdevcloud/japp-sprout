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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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

import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.ClientCallBackType;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.TokenTransferType;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class CoreBasicAuthProviderHandler extends BaseAuthProviderHandler {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CoreBasicAuthProviderHandler.class);

	public void handleUiLogin(HttpServletRequest request, HttpServletResponse response)  {

		String appCallbackUrl = "/open/core/basicauth";
		String coreLoginId = handlerInfo.coreLoginId;
		String tokenNonce = handlerInfo.apiAuthInfo.tokenNonce;
		String clientAppId = handlerInfo.apiAuthInfo.clientAppId;
		String clientAuthKey = handlerInfo.apiAuthInfo.clientAuthKey;

//		ClientAppInfo clientAppInfo = AppComponents.iaaService.getClientAppInfo(clientAppId);
//		if(clientAppInfo == null) {
//			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//					"Authorization Failed. Error: clientAppInfo is null, check code! Client App Id = " + clientAppId + ", clientAuthKey = " + clientAuthKey);
//			return;
//		}

		ClientAuthProvider clientAuthProvider = handlerInfo.authProvider;
		
		String clientCallbackUrl = clientAuthProvider.getClientCallbackUrl();
		clientCallbackUrl = StringUtil.isEmptyOrNull(clientCallbackUrl)?"/none":clientCallbackUrl.trim();
		
		ClientCallBackType clientCallBackType = clientAuthProvider.getClientCallbackType();
		String clientCallBackTypeStr = (clientCallBackType==null?"":clientCallBackType.name());
		
		TokenTransferType tokenTrasferType = clientAuthProvider.getTokenTransferType();
		String tokenTrasferTypeStr = (tokenTrasferType==null?"":tokenTrasferType.name());

		
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			inputStream = CommonService.class.getResourceAsStream("/page/core_basic_login.html");
			if (inputStream == null) {
				throw new RuntimeException("can not load core_basic_login.html file, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} catch (Exception e){
			e.printStackTrace();
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: " + e);
		}finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		String htmlText = sb.toString();
		htmlText = htmlText.replaceAll("@username@", (coreLoginId==null?"":coreLoginId));
		htmlText = htmlText.replaceAll("@action@", appCallbackUrl);
		htmlText = htmlText.replaceAll("@client_app_id@", clientAppId);
		htmlText = htmlText.replaceAll("@client_auth_key@", clientAuthKey);
		htmlText = htmlText.replaceAll("@token_nonce@", tokenNonce);
		htmlText = htmlText.replaceAll("@callback_type@", clientCallBackTypeStr);
		htmlText = htmlText.replaceAll("@token_transfer@", tokenTrasferTypeStr);
//		htmlText = htmlText.replaceAll("@script@", transferTokenJsUrl);
//		htmlText = htmlText.replaceAll("@style@", transferTokenCssUrl);
		response.setContentType("text/html");
		response.setStatus(200);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.println(htmlText);
			out.flush();
			out.close();
			out = null;
		} catch (Exception e1) {
			e1.printStackTrace();
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: " + e1);
		} finally {
			if(out != null) {
				out.close();
			}
		}
		logger.debug(htmlText);

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
			logger.error("Can't redirect to AAD login page!", e);
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, e.getMessage());
			return;
		}

	}

}
