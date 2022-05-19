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
package com.itdevcloud.japp.core.common;

import java.io.BufferedReader;
/**
*
* @author Marvin Sun
* @since 1.0.0
*/
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ClientPkiInfo;
import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.CidrWhiteList;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;
import com.itdevcloud.japp.core.api.vo.ClientPKI;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.ClientCallBackType;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.TokenTransferType;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.api.vo.ServerInstanceInfo;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public class CommonService implements AppFactoryComponentI {
	private static final Logger logger = LogManager.getLogger(CommonService.class);

	@Value("${" + AppConfigKeys.JAPPCORE_APP_CORE_CONTROLLER_ENABLED_COMMANDS + ":none}")
	private String enabledCommands;
	private Set<String> enabledCommandSet = null;

	@Value("${" + AppConfigKeys.JAPPCORE_IAA_AUTH_SUPPORTED_PROVIDERS + "}")
	private String supportedProviderIds;
	private Set<String> supportedProviderIdSet = null;

	@PostConstruct
	public void init() {
		//enabled command set
		this.enabledCommandSet = new HashSet<String>();
		if(StringUtil.isEmptyOrNull(enabledCommands) || "none".equalsIgnoreCase(enabledCommands)) {
			return;
		}
		String[] cmdArr = enabledCommands.split(",");
		for(String cmd: cmdArr) {
			enabledCommandSet.add(cmd.trim().toLowerCase());
		}
		//supported auth provider set
		this.supportedProviderIdSet = new HashSet<String>();
		if(StringUtil.isEmptyOrNull(supportedProviderIds) ) {
			return;
		}
		String[] idArr = enabledCommands.split(",");
		for(String id: idArr) {
			supportedProviderIdSet.add(id.trim().toLowerCase());
		}

	}
	
	//return null means it is enabled
	//return a response object means the command is not enabled
	//this way could simplify controller's code
//	public <T extends BaseResponse> T checkIsEnabledCommand(Class<T> responseClass) {
//		if (responseClass == null) {
//			String errMsg = "checkIsEnabledCommand()......responseClass is null.";
//			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
//		}
//		String command = AppUtil.getCorrespondingCommand(responseClass.getSimpleName());
//		if(StringUtil.isEmptyOrNull(command) || !enabledCommandSet.contains(command)) {
//			if(enabledCommandSet.contains("all") || enabledCommandSet.contains("any") || enabledCommandSet.contains("*")) {
//				return null;
//			}
//			T response = AppUtil.createResponse(responseClass, "N/A", Status.ERROR_VALIDATION,
//					"the command [" + command + "] is not enabled!");
//			return response;
//		}
//		return null;
//	}
	
	public boolean isCommandEnabled(String classSimpleName) {
		if (StringUtil.isEmptyOrNull(classSimpleName)) {
			String errMsg = "isEnabledCommand()......classSimpleName can not be null ot empty.";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		String command = AppUtil.getCorrespondingCommand(classSimpleName);
		if(StringUtil.isEmptyOrNull(command)) {
			return false;
		}
		//use lower case
		if(!enabledCommandSet.contains(command.trim().toLowerCase())) {
			if(enabledCommandSet.contains("all") || enabledCommandSet.contains("any") || enabledCommandSet.contains("*")) {
				return true;
			}else {
				return false;
			}
		}
		return true;
	}
	
	public boolean isSupportedAuthProvider(String authProviderId) {
		if(StringUtil.isEmptyOrNull(authProviderId)) {
			return false;
		}
		//use lower case
		if(!supportedProviderIdSet.contains(authProviderId.trim().toLowerCase())) {
			return false;
		}
		return true;

	}

	/**
	 * Check if this application is running in a maintenance mode.
	 */
	public boolean inMaintenanceMode(HttpServletResponse httpResponse, String loginId) throws IOException{
		logger.debug("handleMaintenanceMode() - start...");
		if(httpResponse == null) {
			logger.error("handleMaintenanceMode() - httpResponse is null, return as in maintenance mode, check code!...");
			return true;
		}
		String roleAllowed = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_MAINTENANCE_MODE_ROLE_ALLOWED);
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_APP_MAINTENANCE_MODE_ENABLED)
				&& !AppComponents.iaaService.isAccessAllowed(roleAllowed)) {

			httpResponse.addHeader("MaitainenaceMode", "true");
			httpResponse.addHeader("Access-Control-Expose-Headers", "MaitainenaceMode");

			String maitenanceUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_MAINTENANCE_PAGE);
			if (!StringUtil.isEmptyOrNull(maitenanceUrl)) {
				httpResponse.addHeader("Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Accept, Authorization");
				httpResponse.sendRedirect(maitenanceUrl);
				return true;
			} else {
				logger.info("Authentication Failed. code E901. User '" + loginId + "' can't access the application due to maintenance mode.......");
				httpResponse.setStatus(403);
				AppUtil.setHttpResponse(httpResponse, 403, Status.ERROR_SECURITY_AUTHORIZATION,
						"In Maintenance Mode");
				return true;
			}
		}else {
			return false;
		}

	}
	public boolean matchUserIpWhiteList(HttpServletRequest httpRequest, IaaUserI iaaUser) {
		if(httpRequest == null || iaaUser == null) {
			logger.error("matchUserIpWhiteList() - httpRequest and/or iaaUser is null, return false.....");
			return false;
		}
		
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_CIDR_USER_WHITELIST_ENABLED)) {
			List<String> whiteList = iaaUser.getCidrWhiteList();
			return validateIp(httpRequest, whiteList);
		}
		return true;
	}


	public boolean matchClientAppIpWhiteList(HttpServletRequest request) {
		// CIDR white list check begin
		if(request == null ) {
			logger.error("matchClientAppIpWhiteList() - httpRequest is null, return false.....");
			return false;
		}
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();
		ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(apiAuthInfo.clientAppId);
		if(clientAppInfo == null) {
			logger.error("matchClientAppIpWhiteList() - can not get ClientAppInfo, clientAppId = " + apiAuthInfo.clientAppId);
			return false;
			
		}
		List<String> whiteList = (clientAppInfo.getCidrWhiteList()==null?null:clientAppInfo.getCidrWhiteList().getIpWhiteList());
		return validateIp(request, whiteList);
	}

	public boolean validateIp(HttpServletRequest request, List<String> whiteList) {
		// CIDR white list check begin
		if(request == null ) {
			logger.error("validateIp() - httpRequest is null, return false.....");
			return false;
		}
		if(whiteList == null || whiteList.isEmpty()) {
			logger.error("validateIp() - whiteList is null or empty, return false.....");
			return false;
		}
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();
		String clientIP = apiAuthInfo.clientIP;
		boolean isIpValid = false;
		for (String entry : whiteList) {
			if(StringUtil.isEmptyOrNull(entry)) {
				continue;
			}else if(entry.trim().equalsIgnoreCase("any") || entry.trim().equalsIgnoreCase("*") ||
					 new IpAddressMatcher(entry).matches(request) || entry.equals(clientIP)) {
				isIpValid = true;
				break;
			}
		}
		if (!isIpValid) {
			logger.error(
					"requester's IP is not in the Applicaion's IP white list, request IP = " + clientIP + ", APP whiteList = " + whiteList
					+ ".....");
			return false;
		}
		return true;
	}
	

	public boolean matchAppRoleList(IaaUserI iaaUser) {
		if(iaaUser == null ) {
			logger.error("matchAppRoleList() - iaaUser is null, return false.....");
			return false;
		}
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_APPLICATION_ROLECHECK_ENABLED)) {
			Set<String> roleList =  iaaUser.getBusinessRoles();
			roleList.addAll(iaaUser.getApplicationRoles());
			
			List<String> appRoles = getApplicationRoleList();
			boolean isRoleIncluded = false;
			if (appRoles == null || appRoles.isEmpty()) {
				isRoleIncluded = true;
			} else {
				for (String entry : appRoles) {
					for (String role: roleList) {
						if (entry.equalsIgnoreCase(role)) {
							isRoleIncluded = true;
							break;
						}
					}
				}
			}
			if (!isRoleIncluded) {
				logger.error(
						"requester's role is not on the Applicaion's role list"
						+ ".....");
				return false;
			}
		}
		return true;
	}

	public List<String> getApplicationRoleList() {
		String roles = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_APPLICATION_ROLE_LIST);
		if(StringUtil.isEmptyOrNull(roles)) {
			return null;
		}
		String[]  roleList = roles.split(";");
		List<String> rlist = new ArrayList<String>();
		for(String r: roleList) {
			if(!StringUtil.isEmptyOrNull(r)) {
				rlist.add(r.trim());
			}
		}
		return (rlist.isEmpty()?null:rlist);
	}

	public List<String> getSystemUserCIDRWhiteList(String userId) {
		String whitelist = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_SYSTEMUSER_WHITELIST + "." + userId);
		if(StringUtil.isEmptyOrNull(whitelist)) {
			return null;
		}
		String[]  wlArr = whitelist.split(";");
		List<String> wlist = new ArrayList<String>();
		for(String cidr: wlArr) {
			if(!StringUtil.isEmptyOrNull(cidr)) {
				wlist.add(cidr.trim());
			}
		}
		return (wlist.isEmpty()?null:wlist);
	}

	public ServerInstanceInfo getSeverInstanceInfo() {
		ServerInstanceInfo severInstanceInfo = new ServerInstanceInfo();
		InetAddress ip = null;
		String hostIP = null;
		String hostname = null;
		String applicationId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		try {
			ip = InetAddress.getLocalHost();
			hostIP = ip.getHostAddress();
			hostname = ip.getHostName();
			logger.debug("Server Instance Info:  Hostname: " + hostname + ", IP address : " + hostIP);

		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			hostIP = (StringUtil.isEmptyOrNull(hostIP)?"0.0.0.0": hostIP);
			hostname = (StringUtil.isEmptyOrNull(hostname)?"unknown.hostname": hostname);
		}
//		severInstanceInfo.setLocalIP(hostIP);
//		severInstanceInfo.setLocalHostName(hostname);
		severInstanceInfo.setActiveProfileName(AppUtil.getSpringActiveProfile());
		severInstanceInfo.setApplicationId(applicationId);
		severInstanceInfo.setStartupDate(AppUtil.getStartupDate());
		return severInstanceInfo;
	}


	public ClientAppInfo getCoreAppInfo(){
		
		String clientAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		String clientAppName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_NAME);
		String clientOrgName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ORGANIZATION_ID);
		String clientCidrWhiteListStr = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST);
		boolean autoRenewToken = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_API_AUTO_RENEW_ACCESS_TOKEN_ENABLED, true);
		boolean enforceTokenNonce = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_TOKEN_ENFORCE_TOKEN_NONCE, true);
		boolean enforceTokenIp = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_TOKEN_ENFORCE_TOKEN_IP, true);
		Certificate appCertificate = AppComponents.pkiService.getAppCertificate();
		PublicKey appPublicKey = AppComponents.pkiService.getAppPublicKey();
		
		List<String> cidrWhiteList = new ArrayList<String>();
		String[] strArr = StringUtil.isEmptyOrNull(clientCidrWhiteListStr)?null:clientCidrWhiteListStr.split(";");
		if(strArr != null) {
			for (String str:strArr) {
				str = StringUtil.isEmptyOrNull(str)?null:str.trim();
				if(str != null) {
					cidrWhiteList.add(str);
				}
			}
		}
		CidrWhiteList cidrWL = new CidrWhiteList();
		cidrWL.setIpWhiteList(cidrWhiteList);
		
		ClientAppInfo clientAppInfo = new ClientAppInfo();
		clientAppInfo.setId(1L);
		clientAppInfo.setClientAppId(clientAppId);
		clientAppInfo.setName(clientAppName);
		clientAppInfo.setOrganizationId(clientOrgName);
		clientAppInfo.setCidrWhiteList(cidrWL);
		clientAppInfo.setApiAutoRenewAccessToken(autoRenewToken);
		clientAppInfo.setEnforceTokenIP(enforceTokenIp);
		clientAppInfo.setEnforceTokenNonce(enforceTokenNonce);
		
		//this is used to generate JSON string which is used as template for client-auth-info.json
		ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
		List<ClientAuthProvider> providerList = new ArrayList<ClientAuthProvider>();
		
		ClientAuthProvider ClientAuthProvider = new ClientAuthProvider();
		
		String clientAuthKey = clientAppId + "-ak-1";
		
		ClientAuthProvider = new ClientAuthProvider();
		ClientAuthProvider.setId(2L);
		ClientAuthProvider.setClientAuthKey(clientAuthKey);
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setAuthProviderId(AppConstant.IDENTITY_PROVIDER_CORE_BASIC);
		ClientAuthProvider.setMultiFactorType(AppConstant.IAA_MULTI_FACTOR_TYPE_NONE);
		ClientAuthProvider.setAuthAppCallbackUrl(null);
		ClientAuthProvider.setClientCallbackType(ClientCallBackType.REDIRECT);
		ClientAuthProvider.setTokenTransferType(TokenTransferType.COOKIE);
		ClientAuthProvider.setIsDefault(true);
		
		providerList.add(ClientAuthProvider);

		clientAuthInfo.setClientAuthProviderList(providerList); 

		clientAppInfo.setClientAuthInfo(clientAuthInfo);
		
		//pki info
		ClientPkiInfo clientPkiInfo = new ClientPkiInfo();
		List<ClientPKI> pkiList = new ArrayList<ClientPKI>();
		String clientPkiKey = clientAppId + "-pki-1";
		
		ClientPKI clientPKI = new ClientPKI();
		clientPKI.setId(1L);
		clientPKI.setClientPkiKey(clientPkiKey);
		clientPKI.setCertificateExpiryDate(null);
		clientPKI.setCertificate(appCertificate);
		clientPKI.setPublicKey(appPublicKey);
		clientPKI.setIsDefault(true);
		
		
		pkiList.add(clientPKI);
		
		clientPkiInfo.setClientPkiList(pkiList); 
		
		clientAppInfo.setClientPkiInfo(clientPkiInfo);
		
		return clientAppInfo;
	}

	public void handleClientAuthCallbackResponse(HttpServletResponse response, ApiAuthInfo apiAuthInfo) throws IOException {

		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
		logger.info("handleClientAuthCallbackResponse(), origin is: " + origin);

		String transferTokenJsUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_JS_PATH);
		String transferTokenCssUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_CSS_PATH);
		
		String queryStr = "?" + AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_APP_ID + "=" + apiAuthInfo.clientAppId + "&"  
				+ AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_AUTH_KEY + "=" + apiAuthInfo.clientAuthKey;
		
		transferTokenJsUrl = transferTokenJsUrl + queryStr;
		transferTokenCssUrl = transferTokenCssUrl + queryStr;
		
		response.addHeader("Access-Control-Allow-Origin", origin);
		//owasp - restriction source download
		response.addHeader("Content-Security-Policy", "default-src 'self';");
		response.addHeader("X-XSS-Protection", "1; mode=block");

		ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(apiAuthInfo.clientAppId);
		if(clientAppInfo == null) {
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY,
					"clientAppInfo is null, check code! Client App Id = " + apiAuthInfo.clientAppId + ", clientAuthKey = " + apiAuthInfo.clientAuthKey);
			return;
		}
		ClientAuthProvider clientAuthProvider = clientAppInfo.getClientAuthProvider(apiAuthInfo.clientAuthKey);
		if(clientAuthProvider == null) {
			AppUtil.setHttpResponse(response, 401, Status.ERROR_SECURITY,
					"clientAuthProvider is null, check code! Client App Id = " + apiAuthInfo.clientAppId + ", clientAuthKey = " + apiAuthInfo.clientAuthKey);
			return;
		}
		
		String clientCallbackUrl = clientAuthProvider.getClientCallbackUrl();
		clientCallbackUrl = StringUtil.isEmptyOrNull(clientCallbackUrl)?"/none":clientCallbackUrl.trim();
		
		ClientCallBackType clientCallBackType = clientAuthProvider.getClientCallbackType();
		String clientCallBackTypeStr = (clientCallBackType==null?"":clientCallBackType.name());
		
		TokenTransferType tokenTrasferType = clientAuthProvider.getTokenTransferType();
		String tokenTrasferTypeStr = (tokenTrasferType==null?"":tokenTrasferType.name());
		
		// ===load token page===
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			inputStream = CommonService.class.getResourceAsStream("/page/transfer_token_to_client.html");
			if (inputStream == null) {
				throw new RuntimeException("can not load transfer_token_to_client.html file, check code!.......");
			}
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();
			inputStream = null;
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
		String htmlText = sb.toString();
		htmlText = htmlText.replaceAll("@token@", apiAuthInfo.token);
		htmlText = htmlText.replaceAll("@action@", clientCallbackUrl);
		htmlText = htmlText.replaceAll("@callback_type@", clientCallBackTypeStr);
		htmlText = htmlText.replaceAll("@token_transfer@", tokenTrasferTypeStr);
		htmlText = htmlText.replaceAll("@script@", transferTokenJsUrl);
		htmlText = htmlText.replaceAll("@style@", transferTokenCssUrl);
		response.setContentType("text/html");
		response.setStatus(200);
		PrintWriter out = response.getWriter();
		logger.debug(htmlText);
		out.println(htmlText);
		out.flush();
		out.close();

		return;
	}

	
	
	public void setValidatedAuthTokenClaimsAndApiAuthInfoContext(HttpServletRequest request)  {

		String errMsg = null;
		if (request == null ) {
			errMsg = "setValidatedAuthTokenClaimsAndApiAuthInfoContext() - request can not be null!" ;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		String clientAppId = null;
		String jsonRequestClientAppId = null;
		String tokenClientAppId = null;
		String paraClientAppId = null;

		String clientAuthKey = null;
		String jsonRequestClientAuthKey = null;
		String tokenClientAuthKey = null;
		String paraClientAuthKey = null;
		
		String tokenNonce =null;
		String jsonRequestTokenNonce =null;
		String paraTokenNonce =null;

		boolean useCoreAppIdAsClientAppId = false;
		
		String requestBodyStr = AppUtil.getHttpRequestJsonBody(request);
		
		//set  claims context first
		Map<String, Object> tokenClaims = null;
		String token = AppUtil.getJwtTokenFromRequest(request);
		if(!StringUtil.isEmptyOrNull(token)) {
			tokenClaims = AppComponents.jwtService.parseTokenClaims(token);
			if(tokenClaims == null) {
				//token is not valid
				token = null;
				AppThreadContext.setAuthTokenClaims(null);
				tokenClientAppId = null;
				tokenClientAuthKey = null;
			}else {
				AppThreadContext.setAuthTokenClaims(tokenClaims);
				tokenClientAppId = (tokenClaims.get(TokenHandlerI.JWT_CLAIM_KEY_AUDIENCE)==null?null:""+tokenClaims.get(TokenHandlerI.JWT_CLAIM_KEY_AUDIENCE));
				tokenClientAuthKey = (tokenClaims.get(TokenHandlerI.JWT_CLAIM_KEY_AUTH_KEY)==null?null:""+tokenClaims.get(TokenHandlerI.JWT_CLAIM_KEY_AUTH_KEY));
			}
		}else {
			token = null;
			AppThreadContext.setAuthTokenClaims(null);
			tokenClientAppId = null;
			tokenClientAuthKey = null;
		}
		//get from parameter/query string/cookie etc.
		paraClientAppId = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_APP_ID);
		paraClientAuthKey = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_AUTH_KEY);
		paraTokenNonce = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_TOKEN_NONCE);

		//try to get from request body json string
		if (!StringUtil.isEmptyOrNull(requestBodyStr)) {
			jsonRequestClientAppId = AppUtil.getValueFromJsonString(requestBodyStr, "clientAppId");
			jsonRequestClientAuthKey = AppUtil.getValueFromJsonString(requestBodyStr, "clientAuthKey");
			jsonRequestTokenNonce = AppUtil.getValueFromJsonString(requestBodyStr, "tokenNonce");
		}
		
		if(!CommonUtil.haveSameValue(true, true, jsonRequestClientAppId, tokenClientAppId, paraClientAppId)) {
			errMsg = "setValidatedAuthTokenClaimsAndApiAuthInfoContext() - CleintAppId come from request body, request header/cookie/query and token are different! " 
			+ "tokenClientAppId = " + tokenClientAppId
			+ ", paraClientAppId = " + paraClientAppId
			+ ", jsonRequestClientAppId = " + jsonRequestClientAppId;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
		}
		
		if(!CommonUtil.haveSameValue(true, true, jsonRequestClientAuthKey, tokenClientAuthKey, paraClientAuthKey)) {
			errMsg = "setValidatedAuthTokenClaimsAndApiAuthInfoContext() - clientAuthKey come from request body, request header/cookie/query and token are different! " 
			+ "tokenClientAuthKey = " + tokenClientAuthKey
			+ ", paraClientAuthKey = " + paraClientAuthKey
			+ ", jsonRequestClientAuthKey = " + jsonRequestClientAuthKey;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
		}
		
		if(!CommonUtil.haveSameValue(true, true, jsonRequestTokenNonce, paraTokenNonce)) {
			errMsg = "setValidatedAuthTokenClaimsAndApiAuthInfoContext() - tokenNonce come from request body, request header/cookie/query and token are different! " 
			+ "paraTokenNonce = " + paraTokenNonce
			+ ", jsonRequestTokenNonce = " + jsonRequestTokenNonce;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
		}
		
		clientAppId = StringUtil.isEmptyOrNull(jsonRequestClientAppId)? (StringUtil.isEmptyOrNull(tokenClientAppId)?paraClientAppId:tokenClientAppId):jsonRequestClientAppId;
		clientAuthKey = StringUtil.isEmptyOrNull(jsonRequestClientAuthKey)? (StringUtil.isEmptyOrNull(tokenClientAuthKey)?paraClientAuthKey:tokenClientAuthKey):jsonRequestClientAuthKey;
		tokenNonce = StringUtil.isEmptyOrNull(jsonRequestTokenNonce)? paraTokenNonce :jsonRequestTokenNonce;
		
		
		if (StringUtil.isEmptyOrNull(clientAppId)) {
			//default to core app, refer to CommonService.getCoreAppInfo()
			clientAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			clientAuthKey = AppComponents.clientAppInfoCache.getDefaultClientAuthKey(clientAppId);
			useCoreAppIdAsClientAppId = true;
		}
		
		ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(clientAppId);
		if(clientAppInfo == null) {
			errMsg = "setValidatedAuthTokenClaimsAndApiAuthInfoContext() - clientAppId (" + clientAppId + ") is not supported!" ;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
			
		}

		//clientAppId and ClientAuthKey won't be null
		//token and token nonce could be null
		
		String host = AppUtil.getClientHost(request);
		host = (StringUtil.isEmptyOrNull(host) ? "n/a" : host);
		
		String ip = AppUtil.getClientIp(request);
		ip = (StringUtil.isEmptyOrNull(ip) ? "n/a" : ip);

		ApiAuthInfo authInfo = new ApiAuthInfo();
		authInfo.clientAppId = clientAppId;
		authInfo.clientAuthKey = clientAuthKey;
		authInfo.token = token;
		authInfo.tokenNonce = tokenNonce;
		authInfo.clientIP = ip;
		authInfo.clientHost = host;
		authInfo.useCoreAppIdAsClientAppId = useCoreAppIdAsClientAppId;
		AppThreadContext.setApiAuthInfo(authInfo); 
		return;
	}


}

	
