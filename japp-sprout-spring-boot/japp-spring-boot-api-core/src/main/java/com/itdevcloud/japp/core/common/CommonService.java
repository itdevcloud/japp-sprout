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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.LoginStateInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ServerInstanceInfo;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class CommonService implements AppFactoryComponentI {
	// private static final Logger logger =
	// LogManager.getLogger(CommonService.class);
	private static final Logger logger = LogManager.getLogger(CommonService.class);

	@PostConstruct
	public void init() {
		// try to avoid using AppConfig Service, AppComponents.appConfigCache may be not
		// fully initiated yet
	}

	/**
	 * Check if this application is running in a maintenance mode.
	 */
	public boolean inMaintenanceMode(HttpServletResponse httpResponse, String loginId) throws IOException {
		logger.debug("handleMaintenanceMode() - start...");
		if (httpResponse == null) {
			logger.error(
					"handleMaintenanceMode() - httpResponse is null, return as in maintenance mode, check code!...");
			return true;
		}
		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_APP_MAINTENANCE_MODE_ENABLED)) {
//			if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_APP_MAINTENANCE_MODE_ENABLED)
//					&& !AppComponents.iaaService.isAccessAllowed(AppConstant.BUSINESS_ROLE_IT_SUPPORT)) {

			httpResponse.addHeader("MaitainenaceMode", "true");
			httpResponse.addHeader("Access-Control-Expose-Headers", "MaitainenaceMode");

			String maitenanceUrl = ConfigFactory.appConfigService
					.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_MAINTENANCE_PAGE);
			if (!StringUtil.isEmptyOrNull(maitenanceUrl)) {
				httpResponse.addHeader("Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Accept, Authorization");
				httpResponse.sendRedirect(maitenanceUrl);
				return true;
			} else {
				logger.info("Authentication Failed. code E901. User '" + loginId
						+ "' can't access the application due to maintenance mode.......");
				httpResponse.setStatus(403);
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_MAINTENANCE_MODE,
						"Authorization Failed. code E901");
				return true;
			}
		} else {
			return false;
		}

	}

	public List<String> getApplicationCidrWhiteList() {
		String whitelist = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST);
		if (StringUtil.isEmptyOrNull(whitelist)) {
			return null;
		}
		String[] wlArr = whitelist.split(";");
		List<String> wlist = new ArrayList<String>();
		for (String cidr : wlArr) {
			if (!StringUtil.isEmptyOrNull(cidr)) {
				wlist.add(cidr.trim());
			}
		}
		return (wlist.isEmpty() ? null : wlist);
	}

	public boolean matchUserIpWhiteList(HttpServletRequest httpRequest, AppIaaUser iaaUser) {
		if(iaaUser == null) {
			logger.error("matchUserIpWhiteList() - iaaUser is null, return false.....");
			return false;
		}
		List<String> cidrList = CommonUtil.stringToList(iaaUser.getCidrWhitelist(), ",");
		return matchCidrWhitelist(httpRequest, cidrList);
	}

	public boolean matchAppCidrWhitelist(HttpServletRequest httpRequest, IaaAppVO iaaAppVO) {
		List<String> cidrList = null;
		if(iaaAppVO == null) {
			//logger.error("matchUserIpWhiteList() - iaaAppVO is null, return false.....");
			cidrList = getApplicationCidrWhiteList();
			//return false;
		}else {
			cidrList = iaaAppVO.getClientCidrWhitelist();
		}
//		List<String> cidrList = getApplicationCidrWhiteList();
		return matchCidrWhitelist(httpRequest, cidrList);
	}

	public boolean matchCidrWhitelist(HttpServletRequest httpRequest, List<String> cidrList) {
		// CIDR white list check begin
		if (httpRequest == null) {
			logger.error("matchCidrWhitelist() - httpRequest is null, return false.....");
			return false;
		}
		if (cidrList == null || cidrList.isEmpty()) {
			return true;
		}
		boolean isIpValid = false;
		for (String entry : cidrList) {
			if (new IpAddressMatcher(entry).matches(httpRequest)) {
				isIpValid = true;
				break;
			}
		}
		if (!isIpValid) {
			logger.error("requester's IP is not on the CIDR white list, request IP = "
					+ AppUtil.getClientIp(httpRequest) + ", CIDR whiteList = " + cidrList + ".....");
			return false;
		}
		return true;
	}
	
	public String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookieList = request.getCookies();
		if (cookieList == null || cookieName == null) {
			return null;
		}
		String retValue = null;
		for (int i = 0; i < cookieList.length; i++) {
			logger.debug("retrieve token from TRACS ......cookie name= " + cookieList[i].getName() + ", value=" + cookieList[i].getValue());
			if (cookieList[i].getName().equalsIgnoreCase(cookieName)) {
				retValue = cookieList[i].getValue();
				break;
			}
		}
		return retValue;
	}

	public boolean matchAppRoleList(AppIaaUser iaaUser) {
		if (iaaUser == null) {
			logger.error("matchAppRoleList() - iaaUser is null, return false.....");
			return false;
		}
//		if (ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_APPLICATION_ROLECHECK_ENABLED)) {
//			Set<String> roleList =  iaaUser.getBusinessRoles();
//			roleList.addAll(iaaUser.getApplicationRoles());
//			
//			List<String> appRoles = getApplicationRoleList();
//			boolean isRoleIncluded = false;
//			if (appRoles == null || appRoles.isEmpty()) {
//				isRoleIncluded = true;
//			} else {
//				for (String entry : appRoles) {
//					for (String role: roleList) {
//						if (entry.equalsIgnoreCase(role)) {
//							isRoleIncluded = true;
//							break;
//						}
//					}
//				}
//			}
//			if (!isRoleIncluded) {
//				logger.error(
//						"requester's role is not on the Applicaion's role list"
//						+ ".....");
//				return false;
//			}
//		}
		return true;
	}

	public List<String> getApplicationRoleList() {
		String roles = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_APPLICATION_ROLE_LIST);
		if (StringUtil.isEmptyOrNull(roles)) {
			return null;
		}
		String[] roleList = roles.split(";");
		List<String> rlist = new ArrayList<String>();
		for (String r : roleList) {
			if (!StringUtil.isEmptyOrNull(r)) {
				rlist.add(r.trim());
			}
		}
		return (rlist.isEmpty() ? null : rlist);
	}

	public List<String> getSystemUserCIDRWhiteList(String userId) {
		String whitelist = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_CIDR_SYSTEMUSER_WHITELIST + "." + userId);
		if (StringUtil.isEmptyOrNull(whitelist)) {
			return null;
		}
		String[] wlArr = whitelist.split(";");
		List<String> wlist = new ArrayList<String>();
		for (String cidr : wlArr) {
			if (!StringUtil.isEmptyOrNull(cidr)) {
				wlist.add(cidr.trim());
			}
		}
		return (wlist.isEmpty() ? null : wlist);
	}

	public ServerInstanceInfo getSeverInstanceInfo() {
		ServerInstanceInfo severInstanceInfo = new ServerInstanceInfo();
		InetAddress ip = null;
		String hostIP = null;
		String hostname = null;
		String applicationId = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		try {
			ip = InetAddress.getLocalHost();
			hostIP = ip.getHostAddress();
			hostname = ip.getHostName();
			logger.debug("Server Instance Info:  Hostname: " + hostname + ", IP address : " + hostIP);

		} catch (Exception e) {
			logger.error(AppUtil.getStackTrace(e));
			hostIP = (StringUtil.isEmptyOrNull(hostIP) ? "0.0.0.0" : hostIP);
			hostname = (StringUtil.isEmptyOrNull(hostname) ? "unknown.hostname" : hostname);
		}
		severInstanceInfo.setLocalIP(hostIP);
		severInstanceInfo.setLocalHostName(hostname);
		severInstanceInfo.setActiveProfileName(AppUtil.getSpringActiveProfile());
		severInstanceInfo.setApplicationId(applicationId);
		severInstanceInfo.setStartupDate(AppUtil.getStartupDate());
		return severInstanceInfo;
	}

	public void handleCookieResponse(HttpServletResponse response, String token) throws IOException {

		String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE);
		boolean secureCookieEnabled = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_FRONTEND_UI_SECURE_COOKIE_ENABLED);
		String origin = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_TOKEN_PAGE, "http://localhost:4200");

		Cookie tokenCookie = new Cookie(AppConstant.HTTP_AUTHORIZATION_COOKIE_NAME, token);
		tokenCookie.setPath("/");
		if (secureCookieEnabled) {
			tokenCookie.setSecure(true);
		}
		tokenCookie.setMaxAge(300);

		response.addCookie(tokenCookie);

		response.addHeader("Access-Control-Allow-Origin", origin);
		response.addHeader("Content-Security-Policy", "default-src 'self';");
		response.addHeader("X-XSS-Protection", "1; mode=block");

//		response.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
//		response.addHeader("Access-Control-Allow-Headers",
//				"Origin, X-Requested-With, Content-Type, Accept, Authorization");
		// response.addHeader("Access-Control-Expose-Headers", "Authorization");
		// response.addHeader("Access-Control-Expose-Headers", "APP_ROLES");

		logger.info("redirect back to front-end UI token page============= ");

		/*
		 * //get all headers Collection<String> headers =
		 * response.getHeaders("Access-Control-Allow-Origin"); for (String header :
		 * headers) { System.out.println("    Value : " + header); }
		 */

		response.sendRedirect(url);

		return;
	}

	public void handlePostResponse(HttpServletResponse response, String postUrl, String token) throws IOException {

		// ===load token page===
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inputStream = this.getClass().getResourceAsStream("/token_loader.html");
			if (inputStream == null) {
				throw new RuntimeException("can not load token_loader html file.......");
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
		htmlText = htmlText.replaceAll("@action@", postUrl);
		response.setContentType("text/html");
		response.setStatus(200);
		PrintWriter out = response.getWriter();
		out.println(htmlText);
		out.flush();
		out.close();

		return;
	}

//	public String retrieveAuthCallBackUrl(ConfigServiceHelperI jappConfigService, String appId) {
//		String propertyName = "tracs.application.callback.url." + appId.toLowerCase();
//		String url = ConfigFactory.appConfigService.getPropertyAsString(propertyName);
//
//		return url;
//
//	}

	public String getAuthnProviderURL(HttpServletRequest httpRequest, IaaAppVO iaaAppVO, String stateString) {
		if (httpRequest == null || iaaAppVO == null) {
			logger.error("getAuthnProviderURL() - httpRequest and/or iaaAppVO is null, return null.....");
			return null;
		}
	    String provider = iaaAppVO.getAuthnProvider();
		if (StringUtil.isEmptyOrNull(provider)) {
			logger.error("getAuthnProviderURL() - provider is not defined in IaaAppInfo, return null.....AppId = " + iaaAppVO.getAppId());
			return null;
		} 
		LoginStateInfo stateInfo = LoginStateInfo.parseStateString(stateString);
		if(stateInfo == null) {
			stateInfo = new LoginStateInfo();
			stateInfo.setAppId(iaaAppVO.getAppId());
			stateString = stateInfo.createStateString();
		}
		String thisAppId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);;
		UUID uuid = UUID.randomUUID();
		if (AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equals(provider)) {
			//just for prompt only, actual logged user may be changed
			String loginId = stateInfo==null?null:stateInfo.getLoginId();
			if(StringUtil.isEmptyOrNull(loginId)) {
				loginId = AppComponents.commonService.getCookieValue(httpRequest, "jappapicore-loginid");
				if(StringUtil.isEmptyOrNull(loginId)) {
					loginId = httpRequest.getParameter("loginid");
				}
			}
			String clientId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_CLIENT_ID);
			String prompt = getAadAuthPrompt();
			String url = AppComponents.aadJwksCache.getAadAuthUri() + "?client_id=" + clientId
					+ "&response_type=id_token" + "&response_mode=form_post" + "&scope=openid" + "&state=" + stateString
					+ prompt + "&nonce=" + uuid.toString();
			if(!StringUtil.isEmptyOrNull(loginId) ) {
				url = url + "&login_hint="+loginId;
			}
			return url;
		} else if (thisAppId.equalsIgnoreCase(provider)) {
			// BASIC AUTH URL
			String url = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_BASIC_AUTHENTICATION_URL);			
			url = url + "?state="+stateString;
			return url;
		}else {
			String url = iaaAppVO.getAuthnProviderURL() ;
			url = url + "?state="+stateString;
			return url;
		}
	}
	
	private String getAadAuthPrompt() {
		String aadAuthPrompt =  ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.AAD_AUTH_PROMPT);
		if ("login".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=login";
		} else if ("none".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=none";
		} else if ("consent".equalsIgnoreCase(aadAuthPrompt)) {
			return "&prompt=consent";
		} else {
			return "";
		}
	}
	
	
}
