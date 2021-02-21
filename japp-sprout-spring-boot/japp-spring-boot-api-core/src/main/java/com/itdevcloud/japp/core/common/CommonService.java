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


import java.io.IOException;
import java.net.InetAddress;
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

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ServerInstanceInfo;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

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
	public void handleResponse(HttpServletResponse response, String token)
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

	public String retrieveAuthCallBackUrl(ConfigServiceHelperI jappConfigService, String appId) {
		String propertyName = "tracs.application.callback.url." + appId.toLowerCase();
		String url = ConfigFactory.appConfigService.getPropertyAsString(propertyName);

		return url;

	}

}
