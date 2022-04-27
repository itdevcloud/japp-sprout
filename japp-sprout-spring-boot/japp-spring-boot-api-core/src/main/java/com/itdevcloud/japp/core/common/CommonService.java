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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ServerInstanceInfo;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public class CommonService implements AppFactoryComponentI {
	private static final Logger logger = LogManager.getLogger(CommonService.class);


	@PostConstruct
	public void init() {
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
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_MAINTENANCE_MODE,
						"Authorization Failed. code E901");
				return true;
			}
		}else {
			return false;
		}

	}
	
//	public List<String> getApplicationCidrWhiteList() {
//		String whitelist = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST);
//		if(StringUtil.isEmptyOrNull(whitelist)) {
//			return null;
//		}
//		String[]  wlArr = whitelist.split(";");
//		List<String> wlist = new ArrayList<String>();
//		for(String cidr: wlArr) {
//			if(!StringUtil.isEmptyOrNull(cidr)) {
//				wlist.add(cidr.trim());
//			}
//		}
//		return (wlist.isEmpty()?null:wlist);
//	}
	

	public boolean matchUserIpWhiteList(HttpServletRequest httpRequest, IaaUserI iaaUser) {
		if(httpRequest == null || iaaUser == null) {
			logger.error("userIpWhiteListCheck() - httpRequest and/or iaaUser is null, return false.....");
			return false;
		}
		List<String> whiteList = iaaUser.getCidrWhiteList();
		return matchUserIpWhiteList (httpRequest, whiteList);
	}

	public boolean matchUserIpWhiteList(HttpServletRequest httpRequest, List<String> whiteList) {
		// CIDR white list check begin
		if(httpRequest == null) {
			logger.error("userIpWhiteListCheck() - httpRequest is null, return false.....");
			return false;
		}
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_CIDR_USER_WHITELIST_ENABLED)) {
			boolean isIpValid = false;
			if (whiteList == null || whiteList.isEmpty()) {
				isIpValid = true;
			} else {
				for (String entry : whiteList) {
					if (new IpAddressMatcher(entry).matches(httpRequest)) {
						isIpValid = true;
						break;
					}
				}
			}
			if (!isIpValid) {
				logger.error(
						"request IP is not on the IP white list, IP = " + AppUtil.getClientIp(httpRequest) + ", User whiteList = " + whiteList
								+ ".....");
				return false;
			}
		}
		return true;

	}
	

	public boolean matchAppIpWhiteList(HttpServletRequest request) {
		// CIDR white list check begin
		if(request == null ) {
			logger.error("matchAppIpWhiteList() - httpRequest is null, return false.....");
			return false;
		}
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();
		
		ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(apiAuthInfo.clientAppId);
		if(clientAppInfo == null) {
			logger.error("matchAppIpWhiteList() - can not get ClientAppInfo, clientAppId = " + apiAuthInfo.clientAppId);
			return false;
			
		}
		String clientIP = AppUtil.getClientIp(request);
		List<String> whiteList = (clientAppInfo.getCidrWhiteList()==null?null:clientAppInfo.getCidrWhiteList().getCidrWhiteList());
		boolean isIpValid = false;
		if (whiteList == null || whiteList.isEmpty()) {
			isIpValid = true;
		} else {
			for (String entry : whiteList) {
				if(StringUtil.isEmptyOrNull(entry)) {
					continue;
				}
				if (new IpAddressMatcher(entry).matches(request) || entry.equals(clientIP)) {
					isIpValid = true;
					break;
				}
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
		Boolean renewToken = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_API_RENEW_ACCESS_TOKEN_ENABLED, false);
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
		cidrWL.setCidrWhiteList(cidrWhiteList);
		
		ClientAppInfo clientAppInfo = new ClientAppInfo();
		clientAppInfo.setId(1L);
		clientAppInfo.setClientAppId(clientAppId);
		clientAppInfo.setName(clientAppName);
		clientAppInfo.setOrganizationId(clientOrgName);
		clientAppInfo.setApiRenewAccessToken(null);
		clientAppInfo.setCidrWhiteList(cidrWL);
		clientAppInfo.setApiRenewAccessToken(renewToken);
		
		//this is used to generate JSON string which is used as template for client-auth-info.json
		ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
		List<ClientAuthProvider> providerList = new ArrayList<ClientAuthProvider>();
		
		ClientAuthProvider ClientAuthProvider = new ClientAuthProvider();
		
		String clientAuthKey = clientAppId;
		
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
		String clientPkiKey = clientAppId + "-pk-1";
		
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
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: clientAppInfo is null, check code! Client App Id = " + apiAuthInfo.clientAppId + ", clientAuthKey = " + apiAuthInfo.clientAuthKey);
			return;
		}
		ClientAuthProvider clientAuthProvider = clientAppInfo.getClientAuthProvider(apiAuthInfo.clientAuthKey);
		if(clientAuthProvider == null) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: clientAuthProvider is null, check code! Client App Id = " + apiAuthInfo.clientAppId + ", clientAuthKey = " + apiAuthInfo.clientAuthKey);
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

	public boolean isSupportedAuthProvider(String authProviderId) {
		if(StringUtil.isEmptyOrNull(authProviderId)) {
			return false;
		}
		String supportedProviderIds = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_SUPPORTED_AUTH_PROVIDERS);
		String[] ids= supportedProviderIds.split(",");
		for(String id: ids) {
			if(!StringUtil.isEmptyOrNull(id)) {
				if(authProviderId.equalsIgnoreCase(id.trim())) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public ApiAuthInfo getApiAuthInfo(HttpServletRequest request)  {

		String errMsg = null;
		if (request == null ) {
			errMsg = "getApiAuthInfo() - request can not be null!" ;
			logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		String clientAppId = null;
		String clientAuthKey = null;
		String tokenNonce =null;
		boolean useCoreAppIdAsClientAppId = false;
		//get from token first
		String token = AppUtil.getJwtTokenFromRequest(request);
		if(!StringUtil.isEmptyOrNull(token)) {
			Map<String, Object> claims = AppUtil.parseJwtClaims(token);
			clientAppId = (claims.get(TokenHandlerI.JWT_CLAIM_KEY_AUDIENCE)==null?null:""+claims.get(TokenHandlerI.JWT_CLAIM_KEY_AUDIENCE));
			clientAuthKey = (claims.get(TokenHandlerI.JWT_CLAIM_KEY_AUTH_KEY)==null?null:""+claims.get(TokenHandlerI.JWT_CLAIM_KEY_AUTH_KEY));
		}
		if(StringUtil.isEmptyOrNull(clientAppId)) {
			//get from para/cookie etc.
			clientAppId = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_APP_ID);
			clientAuthKey = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_CLIENT_AUTH_KEY);
		}
		tokenNonce = AppUtil.getParaCookieHeaderValue(request, AppConstant.HTTP_AUTHORIZATION_ARG_NAME_TOKEN_NONCE);
		
		if (StringUtil.isEmptyOrNull(clientAppId)) {
			//try to get from request body json string
			String requestBodyStr = AppUtil.getHttpRequestJsonBody(request);
			if (!StringUtil.isEmptyOrNull(requestBodyStr)) {
				clientAppId = AppUtil.getValueFromJsonString(requestBodyStr, "clientAppId");
				clientAuthKey = AppUtil.getValueFromJsonString(requestBodyStr, "clientAuthKey");
			}
		}
		if (StringUtil.isEmptyOrNull(clientAppId)) {
			//default to core app, refer to CommonService.getCoreAppInfo()
			clientAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
			clientAuthKey = null;
			useCoreAppIdAsClientAppId = true;
		}

		String host = AppUtil.getClientHost(request);
		host = (StringUtil.isEmptyOrNull(host) ? "n/a" : host);
		
		String ip = AppUtil.getClientIp(request);
		ip = (StringUtil.isEmptyOrNull(ip) ? "n/a" : ip);

		ApiAuthInfo authInfo = new ApiAuthInfo();
		authInfo.clientAppId = clientAppId;
		authInfo.clientAuthKey = clientAuthKey;
		authInfo.tokenNonce = tokenNonce;
		authInfo.clientIP = ip;
		authInfo.clientHost = host;
		authInfo.useCoreAppIdAsClientAppId = useCoreAppIdAsClientAppId;
		return authInfo;
	}


}

	
