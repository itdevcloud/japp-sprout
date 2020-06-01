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
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.cahce.PkiKeyCache;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.IaaService;
import com.itdevcloud.japp.core.iaa.service.JwtService;
import com.itdevcloud.japp.core.iaa.service.SecondFactorInfo;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.core.service.notification.SystemNotification;
import com.itdevcloud.japp.core.service.notification.SystemNotifyService;
import com.itdevcloud.tools.common.util.StringUtil;

/**
 * This Servlet Filter provides the support for verifying that each incoming
 * http request includes a valid JWT and it is a valid request.
 * <p>
 * First, this filter checks CIDR whitelist of this application. Then it checks
 * if the JWT in each http request's header is valid. If this is a valid token,
 * then it will check if this is an authorized user. Finally, this filter will
 * check CIDR whitelist of the user. If this request passes all checks, that
 * means this user is an authorized user. Then the filter will continue to
 * process the request, otherwise, it will return 401 or 403 Error to the
 * client.
 * <p>
 * This Filter's url pattern is "/api/*". All requests with url beginning with
 * "api" will trigger this filter. If you need a api service to bypass this
 * filter, define this api's url not starting with "api".
 * <p>
 * This Filter logs the total processing time. If it is too long, a performance
 * alert/warning will be sent.
 * 
 * @author Ling Yang
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebFilter(filterName = "PiscesJappAuthenticationFilter", urlPatterns = "/api/*")
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		AppUtil.initTransactionContext(httpRequest);
		try {
			logger.debug("PiscesJappAuthenticationFilter.doFilter() start ========>");

			// App CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
				logger.error(
						"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = "
								+ AppUtil.getClientIp(httpRequest) + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E209");
				return;
			}
			long startTS = System.currentTimeMillis();

			String requestURI = httpRequest.getRequestURI();
			String queryStr = httpRequest.getQueryString();

			logger.info("PiscesJappAuthenticationFilter.doFilter====method=" + httpRequest.getMethod());
			String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);

			// control for CORS flow
			if (httpRequest.getMethod().equals("OPTIONS")) {
				httpResponse.addHeader("Access-Control-Allow-Origin", origin);
				httpResponse.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
				httpResponse.addHeader("Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Accept, Authorization");
				httpResponse.setStatus(200);
				return;
			}
			String activeProfile = AppUtil.getSpringActiveProfile();
			logger.debug("PiscesJappAuthenticationFilter activeProfile = " + activeProfile);

			if (httpRequest.getMethod().equals("GET")) {
				httpResponse.addHeader("Cache-Control", "no-cache");
			}

			IaaUser iaaUser = null;
			// PiscesJappIaaService iaaService = piscesjappFactory.getIaaService();

			// check piscesjappip authentication
			boolean piscesjappipAuth = ConfigFactory.appConfigService
					.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_SKIP_AUTH_ENABLED);
			if (!piscesjappipAuth || AppConstant.JAPPCORE_SPRING_ACTIVE_PROFILE_PROD.equalsIgnoreCase(activeProfile)) {
				// verify JWT from a header,
				String token = AppUtil.getJwtTokenFromRequest(httpRequest);
				if (StringUtil.isEmptyOrNull(token)) {
					// jwt token is null return 401
					logger.error("Authentication Failed. code E204 - token is not validated, throw 401 error====");
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E204");
					return;

				}
				if (!AppComponents.jwtService.isValidPiscesJappToken(token, AppComponents.pkiKeyCache.getPiscesJappPublicKey(),
						null)) {
					// jwt token is not valid, return 401
					logger.error("Authentication Failed. code E205 - token is not validated, throw 401 error====");
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY_INVALID_TOKEN,
							"Authentication Failed. code E205");
					return;
				}

				String newToken = null;
				String provider = ConfigFactory.appConfigService
						.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTHENTICATION_PROVIDER);
				// retrieve userInfo for Authorization
				// token subject must be userId!!!
				String userId = AppThreadContext.getTokenSubject();
				if (StringUtil.isEmptyOrNull(userId)) {
					logger.error(
							"Authentication Failed. code E206 - PiscesJappThreadContext.getLoginId() returns null, PiscesJappThreadContext have no login ID set.");
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E206");
					return;
				}
				try {
					iaaUser = AppComponents.iaaService.getIaaUserByUserId(userId);
					if (iaaUser == null) {
						logger.error("Authorization Failed. code E207 - Can not retrive user by userId '" + userId
								+ "'.....");
						httpResponse.setStatus(403);
						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
								"Authorization Failed. code E207");
						return;
					}
				} catch (AppException e) {
					logger.error("Authorization Failed. code E208 - " + e);
					logger.error(AppUtil.getStackTrace(e));
					AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authorization Failed. code E208");
					return;
				}
				// Application role list check
				if (!AppComponents.commonService.matchAppRoleList(iaaUser)) {
					logger.error(
							"Authorization Failed. code E508 - requestor's is not on the APP's role list" + ".....");
					AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authorization Failed. code E508");
					return;
				}
				// CIDR white list check begin
				if (!AppComponents.commonService.matchUserIpWhiteList(httpRequest, iaaUser)) {
					logger.error(
							"Authorization Failed. code E210 - request IP is not on the user's IP white list, user userId '"
									+ iaaUser.getUserId() + "', IP = " + AppUtil.getClientIp(httpRequest) + ".....");
					AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authorization Failed. code E210");
					return;
				}
				// handle maintenance mode
				if (AppComponents.commonService.inMaintenanceMode(httpResponse, userId)) {
					return;
				}

				// generate new JWT token.
				SecondFactorInfo secondFactorInfo = AppUtil.getSecondFactorInfoFromToken(token);
				newToken = AppComponents.jwtService.issueToken(iaaUser,
						AppComponents.pkiKeyCache.getPiscesJappPrivateKey(), ConfigFactory.appConfigService
								.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH),
						secondFactorInfo);

				httpResponse.addHeader("Token", newToken);
				// httpResponse.addHeader("Access-Control-Allow-Origin",
				// authParameters.getAngularOrigin());
				// httpResponse.addHeader("Access-Control-Allow-Headers",
				// "X-Requested-With,Origin,Content-Type, Accept, Token");
				httpResponse.addHeader("Access-Control-Expose-Headers", "Token");

				logger.info("PiscesJappAuthenticationFilter - Issue new token........");

			} else {

				// piscesjappip auth
				String userId = request.getParameter("loginId");

				iaaUser = AppComponents.iaaService.getDummyIaaUserByUserId(userId);

				// handle maintenance mode
				if (AppComponents.commonService.inMaintenanceMode(httpResponse, userId)) {
					return;
				}
				logger.warn(
						"PiscesJappAuthenticationFilter - back-end piscesjappip auth.......!!!!!!!, get dummy user with userId = "
								+ userId);

				AppThreadContext.setIaaUser(iaaUser);
			}

			chain.doFilter(request, httpResponse);// sends request to next resource

			Date end = new Date();
			long endTS = end.getTime();
			long totalTS = (endTS - startTS);
			String infoStr = null;
			int warningSeconds = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_APP_SYSTEM_PERFORMANCE_WARNING_THRESHOLD_SECONDS);
			int alertSeconds = ConfigFactory.appConfigService
					.getPropertyAsInteger(AppConfigKeys.JAPPCORE_APP_SYSTEM_PERFORMANCE_ALERT_THRESHOLD_SECONDS);
			if (totalTS <= warningSeconds * 1000) {
				infoStr = "PiscesJappAuthenticationFilter.... end...Request URI = <" + requestURI + ">, Query String = <"
						+ queryStr + ">, total time = " + totalTS + " millis. \n";
			} else if (totalTS > warningSeconds * 1000 && totalTS <= alertSeconds * 1000) {
				infoStr = "PiscesJappAuthenticationFilter...PERFORMANCE WARNING - Request URI = <" + requestURI
						+ ">, Query String = <" + queryStr + ">, total time = " + totalTS + " millis. \n";

				Date scheduledDate = null;
				SystemNotification sn = new SystemNotification(SystemNotifyService.CATEGORY_PERFORMANCE_WARNING,
						scheduledDate, infoStr);
				AppComponents.systemNotifyService.addNotification(sn);
			} else {
				infoStr = "PiscesJappAuthenticationFilter...PERFORMANCE ALERT - Request URI = <" + requestURI
						+ ">, Query String = <" + queryStr + ">, total time = " + totalTS + " millis. \n";

				Date scheduledDate = null;
				SystemNotification sn = new SystemNotification(SystemNotifyService.CATEGORY_PERFORMANCE_ALERT,
						scheduledDate, infoStr);
				AppComponents.systemNotifyService.addNotification(sn);
			}
			logger.info(infoStr);
		} finally {
			AppUtil.clearTransactionContext();
		}
	}

	@Override
	public void destroy() {
	}
}
