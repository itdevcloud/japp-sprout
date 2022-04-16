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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ClientPkiInfo;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.ClientCallBackType;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo.TokenTransferType;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ServerInstanceInfo;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
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
	
	public List<String> getApplicationCidrWhiteList() {
		String whitelist = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST);
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
	

	public boolean matchAppIpWhiteList(HttpServletRequest httpRequest) {
		// CIDR white list check begin
		if(httpRequest == null ) {
			logger.error("userIpWhiteListCheck() - httpRequest is null, return false.....");
			return false;
		}
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST_ENABLED)) {
			List<String> whiteList = getApplicationCidrWhiteList();
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
						"requester's IP is not on the Applicaion's IP white list, request IP = " + AppUtil.getClientIp(httpRequest) + ", APP whiteList = " + whiteList
						+ ".....");
				return false;
			}
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
	public void handleResponse111(HttpServletResponse response, String token)
			throws IOException {

		String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE);
		boolean secureCookieEnabled = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_FRONTEND_UI_SECURE_COOKIE_ENABLED);

		Cookie tokenCookie = new Cookie(AppConstant.HTTP_AUTHORIZATION_COOKIE_NAME, token);
		tokenCookie.setPath("/");
		if (secureCookieEnabled) {
			tokenCookie.setSecure(true);
		}
		tokenCookie.setMaxAge(300);

		response.addCookie(tokenCookie);

		response.addHeader("Content-Security-Policy", "default-src 'self';");
		response.addHeader("X-XSS-Protection", "1; mode=block");

		//response.addHeader("Access-Control-Expose-Headers", "Authorization");
		//response.addHeader("Access-Control-Expose-Headers", "APP_ROLES");

		logger.info("redirect back to front-end UI token page============= ");

		/*
		 * //get all headers Collection<String> headers =
		 * response.getHeaders("Access-Control-Allow-Origin"); for (String header :
		 * headers) { System.out.println("    Value : " + header); }
		 */

		response.sendRedirect(url);

		return;
	}


	public ClientAppInfo getCoreAppInfo(){
		
		String clientId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		String authProvider = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTH_APP_CALLBACK_URL);
		String appCallbackUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTH_APP_CALLBACK_URL);
		Certificate appCertificate = AppComponents.pkiService.getAppCertificate();
		PublicKey appPublicKey = AppComponents.pkiService.getAppPublicKey();
		
		ClientAppInfo clientAppInfo = new ClientAppInfo();
		ClientPkiInfo clientPkiInfo = new ClientPkiInfo();
		ClientAuthInfo clientAuthInfo = new ClientAuthInfo();
		
		clientAppInfo.setClientId(clientId);
		
		clientPkiInfo.setClientId(clientId);
		clientPkiInfo.setPkiCode("corePki");
		clientPkiInfo.setCertificate(appCertificate);
		clientPkiInfo.setPublicKey(appPublicKey);
		clientPkiInfo.setIsDefault(true);
		
		clientAppInfo.addClientPkiInfo(clientPkiInfo);
		
		
		clientAuthInfo.setAuthAppCallbackUrl(appCallbackUrl);
		clientAuthInfo.setAuthKey("authKey-1");
		clientAuthInfo.setAuthProviderId("AAD_ID-Token");
		clientAuthInfo.setClientCallbackType(ClientCallBackType.POST);
		clientAuthInfo.setClientCallbackUrl(null);
		clientAuthInfo.setTokenTransferType(TokenTransferType.COOKIE);
		clientAuthInfo.setIsDefault(true);
		clientAuthInfo.setClientId("client-1");
		
		clientAppInfo.addClientAuthInfo(clientAuthInfo);
		
		return clientAppInfo;
	}

	public void handleClientAuthCallbackResponse(HttpServletResponse response, String token, String clientId, String authKey) throws IOException {

//		String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE);
//		logger.info("handleClientAuthCallbackResponse(), url is: " + url);

		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
		logger.info("handleClientAuthCallbackResponse(), origin is: " + origin);

		String transferTokenJsUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_JS_PATH);
		String transferTokenCssUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_CSS_PATH);

		response.addHeader("Access-Control-Allow-Origin", origin);
		//owasp - restriction source download
		response.addHeader("Content-Security-Policy", "default-src 'self';");
		response.addHeader("X-XSS-Protection", "1; mode=block");

		ClientAppInfo clientAppInfo = AppComponents.iaaService.getClientAppInfo(clientId);
		if(clientAppInfo == null) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: clientAppInfo is null, check code! Client Id = " + clientId + ", authKey = " + authKey);
			return;
		}
		ClientAuthInfo clientAuthInfo = clientAppInfo.getClientAuthInfo(authKey);
		if(clientAuthInfo == null) {
			AppUtil.setHttpResponse(response, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"Authorization Failed. Error: clientAuthInfo is null, check code! Client Id = " + clientId + ", authKey = " + authKey);
			return;
		}
		
		String clientCallbackUrl = clientAuthInfo.getClientCallbackUrl();
		clientCallbackUrl = StringUtil.isEmptyOrNull(clientCallbackUrl)?"/none":clientCallbackUrl.trim();
		
		ClientCallBackType clientCallBackType = clientAuthInfo.getClientCallbackType();
		String clientCallBackTypeStr = (clientCallBackType==null?"":clientCallBackType.name());
		
		TokenTransferType tokenTrasferType = clientAuthInfo.getTokenTransferType();
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
		htmlText = htmlText.replaceAll("@token@", token);
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

	
//	public void handlePostTokenToClientResponse(HttpServletResponse response, String loginId, String token) throws IOException {
//
//		String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE);
//		logger.info("handleResponse(), url is: " + url);
//
//		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN, "http://localhost:4200");
//		logger.info("handleResponse(), origin is: " + origin);
//
//		response.addHeader("Access-Control-Allow-Origin", origin);
//
//		//Set login-id 
//		response.addHeader("Content-Security-Policy", "default-src 'self';");
//		response.addHeader("X-XSS-Protection", "1; mode=block");
//
//		String clientPostTokenUrl = getClientPostTokenUrl();
//		String clientPostTokenStorage = getClientPostTokenUrl();
//		
//		// ===load token page===
//		InputStream inputStream = null;
//		StringBuilder sb = new StringBuilder();
//		
//		try {
//			inputStream = CommonService.class.getResourceAsStream("/page/post_token_to_client.html");
//			if (inputStream == null) {
//				throw new RuntimeException("can not load post_token_to_client html file, check code!.......");
//			}
//			String line;
//			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//			while ((line = br.readLine()) != null) {
//				sb.append(line).append("\n");
//			}
//			inputStream.close();
//			inputStream = null;
//		} finally {
//			if (inputStream != null) {
//				inputStream.close();
//				inputStream = null;
//			}
//		}
//		String htmlText = sb.toString();
//		htmlText = htmlText.replaceAll("@token@", token);
//		htmlText = htmlText.replaceAll("@action@", clientPostTokenUrl);
//		htmlText = htmlText.replaceAll("@loginid@", loginId);
//		htmlText = htmlText.replaceAll("@token_storage@", onlineTokenLoaderJsUrl);
//		response.setContentType("text/html");
//		response.setStatus(200);
//		PrintWriter out = response.getWriter();
//		out.println(htmlText);
//		out.flush();
//		out.close();
//
//		return;
//
//		
//		return;
//	}
//	public void handlePostTokenToClientResponse(HttpServletResponse response, String loginId, String token, String desktop, String password) throws IOException {
//
//		String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE);
//		logger.info("handleResponse(), url is: " + url);
//
//		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN, "http://localhost:4200");
//		logger.info("handleResponse(), origin is: " + origin);
//
//		response.addHeader("Access-Control-Allow-Origin", origin);
//
//		//Set login-id 
//		response.addHeader("Content-Security-Policy", "default-src 'self';");
//		response.addHeader("X-XSS-Protection", "1; mode=block");
//
//		String desktopLoginCallBackURL = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_DESKTOP_LOGIN_CALLBACK_URL);
//
//		if(desktop != null && desktop.equalsIgnoreCase("true")) {
//			handleDesktopPostResponse(response, desktopLoginCallBackURL, token, loginId, password);
//		} else {
//			handleOnlinePostResponse(response, token, loginId);
//		}
//
//		return;
//	}

//	public void handleOnlinePostResponse(HttpServletResponse response, String token, String loginId) throws IOException {
//
//		// ===load token page===
//		InputStream inputStream = null;
//		StringBuilder sb = new StringBuilder();
//		String postUrl = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_HOME_PAGE);
//		
//		//defined in OpenRestController
//		String onlineTokenLoaderJsUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_ONLINE_TOKEN_LOADER_JS_PATHL);
//		try {
//			inputStream = CommonService.class.getResourceAsStream("/page/online_token_loader.html");
//			if (inputStream == null) {
//				throw new RuntimeException("can not load token_loader html file, check code!.......");
//			}
//			String line;
//			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//			while ((line = br.readLine()) != null) {
//				sb.append(line).append("\n");
//			}
//			inputStream.close();
//			inputStream = null;
//		} finally {
//			if (inputStream != null) {
//				inputStream.close();
//				inputStream = null;
//			}
//		}
//		String htmlText = sb.toString();
//		htmlText = htmlText.replaceAll("@token@", token);
//		htmlText = htmlText.replaceAll("@action@", postUrl);
//		htmlText = htmlText.replaceAll("@loginid@", loginId);
//		htmlText = htmlText.replaceAll("@script@", onlineTokenLoaderJsUrl);
//		response.setContentType("text/html");
//		response.setStatus(200);
//		PrintWriter out = response.getWriter();
//		out.println(htmlText);
//		out.flush();
//		out.close();
//
//		return;
//	}


//	public void handleDesktopPostResponse(HttpServletResponse response, String postUrl, String token, String loginId, String password) throws IOException {
//
//		// ===load token page===
//		String desktopTokenLoaderJsUrl = "/" + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT) + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_DESKTOP_TOKEN_LOADER_JS_PATH);
//		
//		InputStream inputStream = null;
//		StringBuilder sb = new StringBuilder();
//		try {
//			inputStream = CommonService.class.getResourceAsStream("/page/desktop_token_loader.html");
//			if (inputStream == null) {
//				throw new RuntimeException("can not load desktop_token_loader html file, check code!.......");
//			}
//			String line;
//			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//			while ((line = br.readLine()) != null) {
//				sb.append(line).append("\n");
//			}
//			inputStream.close();
//			inputStream = null;
//		} finally {
//			if (inputStream != null) {
//				inputStream.close();
//				inputStream = null;
//			}
//		}
//		
//		PublicKey publicKey = AppComponents.pkiService.getAppPublicKey();
//		
//		String htmlText = sb.toString();
//		htmlText = htmlText.replaceAll("@token@", token);
//		htmlText = htmlText.replaceAll("@action@", postUrl);
//		htmlText = htmlText.replaceAll("@loginid@", loginId);
//		htmlText = htmlText.replace("@publickey@", SecurityUtil.getPublicKeyString(publicKey));
//		htmlText = htmlText.replace("@algorithm@", publicKey.getAlgorithm());
//		htmlText = htmlText.replaceAll("@password@", password);
//		htmlText = htmlText.replaceAll("@script@", desktopTokenLoaderJsUrl);
//		response.setContentType("text/html");
//		response.setStatus(200);
//		PrintWriter out = response.getWriter();
//		out.println(htmlText);
//		out.flush();
//		out.close();
//
//		return;
//	}
	
//	//Land on index.html with error message in cookie
//	public void setLoginErrorHttpResponse(HttpServletRequest request, HttpServletResponse httpResponse, int httpStatus, String statusCode,
//			String message, String token) throws IOException {
//		if (CoreUtil.isEmptyOrNull(statusCode)) {
//			statusCode = "" + httpStatus;
//		}
//		BaseResponse skBaseResponse = CoreUtil.createBaseResponse(statusCode, message);
//		Gson gson = new GsonBuilder().serializeNulls().create();
//		String jsonResponseStr = gson.toJson(skBaseResponse);
//
//		String state = (String) request.getParameter("state");
//		String desktop = null;
//		if(state != null) {
//			String[] arr = state.split(":");
//			String appId = null;
//			
//			String ip = null;
//			if (arr != null) {
//				if (arr.length > 0) {
//					appId = arr[0];
//				}
//				if (arr.length > 1) {
//					desktop = arr[1];
//				}
//				if (arr.length > 2) {
//					ip = arr[2];
//				}
//			}
//		} else {
//			httpResponse.sendError(HttpStatus.BAD_REQUEST.value());
//			return;
//		}
//
//
//		String loginId = "";
//
//		if(null != token) {
//			loginId = CoreUtil.getSubjectFromJwt(token);
//		} else {
//			loginId = "";
//		}
//
//		String method = "get";
//		String postUrl = ConfigService.getInstance().getPropertyAsString(ConfigKeys.STARTKIT_FRONTEND_UI_LOGIN_ERROR_PAGE, "http://localhost:8443/rdcui/index.html/#/loginError");
//		
//		//jsonResponseStr = URLEncoder.encode(jsonResponseStr, StandardCharsets.UTF_8.toString());
//
//		if(desktop != null && desktop.equalsIgnoreCase("true")) {
//			postUrl = ConfigService.getInstance().getPropertyAsString(ConfigKeys.SERVER_DESKTOP_LOGIN_CALLBACK_URL, "https://localhost:8443/open/logincallbackservlet");
//			method = "post";
//
//
//
//			InputStream inputStream = null;
//			StringBuilder sb = new StringBuilder();
//			try {
//				inputStream = WebCoreUtil.class.getResourceAsStream("/online_loginError.html");
//				if (inputStream == null) {
//					throw new RuntimeException("can not load token_loader html file.......");
//				}
//				String line;
//				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//				while ((line = br.readLine()) != null) {
//					sb.append(line).append("\n");
//				}
//				inputStream.close();
//				inputStream = null;
//			} finally {
//				if (inputStream != null) {
//					inputStream.close();
//					inputStream = null;
//				}
//			}
//
//			
//
//			String htmlText = sb.toString();
//			htmlText = htmlText.replaceAll("@actionType@", method);
//			htmlText = htmlText.replaceAll("@actionError@", postUrl);
//			htmlText = htmlText.replaceAll("@loginId@", loginId);
//			htmlText = htmlText.replaceAll("@isError@", "true");
//			htmlText = htmlText.replaceAll("@loginError@", jsonResponseStr);
//			httpResponse.setContentType("text/html");
//			httpResponse.setStatus(200);
//			PrintWriter out = httpResponse.getWriter();
//			out.println(htmlText);
//			out.flush();
//			out.close();
//
//		} else {
//			/*String cookieName = ConfigService.getInstance().getPropertyAsString(ConfigKeys.LOGIN_ERROR_MESSAGE, "LoginErrorMessage");
//			logger.info("WebCoreUtil.setLoginErrorHttpResponse(), cookieName is: ***" + cookieName + "***");
//			logger.info("WebCoreUtil.setLoginErrorHttpResponse(), jsonResponseStr is: " + jsonResponseStr);
//			Cookie loginErrorCookie = new Cookie(cookieName, jsonResponseStr);
//			loginErrorCookie.setPath("/");
//			loginErrorCookie.setHttpOnly(false);
//			String sslEnabled = ConfigService.getInstance().getPropertyAsString(ConfigKeys.SERVER_SSL_ENABLED, "true");
//			
//			logger.info("WebCoreUtil.setLoginErrorHttpResponse(), sslEnabled is: " + sslEnabled);
//			if (Boolean.TRUE.toString().equalsIgnoreCase(sslEnabled) && !postUrl.startsWith("http://localhost:4200")) {
//				// for local if set cookie to secure then page cannot get it
//				logger.info("WebCoreUtil.setLoginErrorHttpResponse(), set secure for cookie.");
//				loginErrorCookie.setSecure(true);
//			}
//			loginErrorCookie.setMaxAge(60);
//			httpResponse.addCookie(loginErrorCookie);*/
//			
//			logger.info("WebCoreUtil.setLoginErrorHttpResponse(), Login error response is: " + jsonResponseStr);
//			
//			httpResponse.sendRedirect(postUrl);
//		}
//	}


}

	
