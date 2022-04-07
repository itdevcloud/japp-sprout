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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.core.service.notification.SystemNotification;
import com.itdevcloud.japp.core.service.notification.SystemNotifyService;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This Servlet Filter provides the support for verifying that each incoming
 * http request includes a valid JWT and it is a valid request.
 * <p>
 * First, this filter checks CIDR whitelist of this application. Then it checks
 * whether the JWT in each http request's header is valid or not. If this is a
 * valid token, then it will check whether this is an authorized user or not.
 * Finally, this filter will check CIDR whitelist of the user. If this request
 * passes all checks, that means this user is an authorized user. the filter
 * will continue to process the request, otherwise, it will return 401 or 403
 * Error to the client.
 * <p>
 * This Filter's url pattern is "/${apiroot}/api/*". All requests with url
 * beginning with "api" will trigger this filter. If you need a api service to
 * bypass this filter, define this api's url not starting with "api".
 * <p>
 * This Filter logs the total processing time. If it is too long, a performance
 * alert/warning will be sent.
 * 
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

//NOTE:
//if update urlPattern, also need to update web.xml
//
@WebFilter(filterName = "JappApiAuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);
	private List<String> excludePathList = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// move to SpecialCharacterFilter
		AppUtil.initTransactionContext(httpRequest);

		try {
			logger.debug("AuthenticationFilter.doFilter() start ========>");
			String errMessage = null;
			// App CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
				errMessage = "Authorization Failed. Request IP is not on the APP's IP white list, user IP = "
						+ AppUtil.getClientIp(httpRequest) + ".....";
				logger.error(errMessage);
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMessage);
				return;
			}
			long startTS = System.currentTimeMillis();

			String requestURI = httpRequest.getRequestURI();
			String queryStr = httpRequest.getQueryString();

			logger.info("AuthenticationFilter.doFilter () ====> method=" + httpRequest.getMethod());
			String origin = ConfigFactory.appConfigService
					.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);

			// control for CORS flow
			if (httpRequest.getMethod().equals("OPTIONS")) {
				httpResponse.addHeader("Access-Control-Allow-Origin", origin);
				httpResponse.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
				httpResponse.addHeader("Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Accept, Authorization, Token, Token-Nonce");
				httpResponse.setStatus(200);
				return;
			}
			String activeProfile = AppUtil.getSpringActiveProfile();
			logger.debug("AuthenticationFilter activeProfile = " + activeProfile);

			if (httpRequest.getMethod().equals("GET")) {
				httpResponse.addHeader("Cache-Control", "no-cache");
			}

			IaaUserI iaaUser = null;

			// check skip auth
			boolean enableAuth = ConfigFactory.appConfigService
					.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_API_AUTH_ENABLED);
			boolean isExcluded = isExcluded(httpRequest);

			if (!(isExcluded || (!enableAuth
					&& !AppConstant.JAPPCORE_SPRING_ACTIVE_PROFILE_PROD.equalsIgnoreCase(activeProfile)))) {
				// verify JWT from a header,
				String token = AppUtil.getJwtTokenFromRequest(httpRequest);
				try {
					if (StringUtil.isEmptyOrNull(token)) {
						errMessage = "Authorization Failed. Token is null.....";
						logger.error(errMessage);
						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMessage);
						return;
					}
					
					String nonce = httpRequest.getHeader(AppConstant.HTTP_TOKEN_NONCE_HEADER_NAME);
					String userIp = AppUtil.getClientIp(httpRequest);
					
					Map<String, String> claimEqualMatchMap = new HashMap<String,String>();
					//must be access token
					claimEqualMatchMap.put(TokenHandlerI.JWT_CLAIM_KEY_TOKEN_TYPE, TokenHandlerI.TYPE_ACCESS_TOKEN);
					
					//Force to check nonce and ip if token has them as claims
					if (!StringUtil.isEmptyOrNull(userIp)) {
						claimEqualMatchMap.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, Hasher.hashPassword(userIp));
					}else {
						claimEqualMatchMap.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_USERIP, null);
					}
					if (!StringUtil.isEmptyOrNull(nonce)) {
						claimEqualMatchMap.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, Hasher.hashPassword(nonce));
					}else {
						claimEqualMatchMap.put(TokenHandlerI.JWT_CLAIM_KEY_HASHED_NONCE, null);
					}
					//if token doesn't has nonce or IP, ignore them
					iaaUser = AppComponents.iaaService.validateTokenAndRetrieveIaaUser(token, claimEqualMatchMap, true);
					if (iaaUser == null) {
						errMessage = "Authorization Failed. Can not retrieve iaaUser";
						logger.error(errMessage);
						httpResponse.setStatus(403);
						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMessage);
						return;
					}
					

					// Application role list check
					if (!AppComponents.commonService.matchAppRoleList(iaaUser)) {
						errMessage = "Authorization Failed. Requestor's role does not match APP role list";
						logger.error(errMessage);
						httpResponse.setStatus(403);
						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMessage);
						return;
					}
					// CIDR white list check begin
					if (!AppComponents.commonService.matchUserIpWhiteList(httpRequest, iaaUser)) {
						errMessage = "Authorization Failed. Requestor's IP does not on user's IP white list. user loginId: '" 
									+ iaaUser.getLoginId() + "', IP = " + AppUtil.getClientIp(httpRequest);
						logger.error(errMessage);
						httpResponse.setStatus(403);
						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errMessage);
						return;
					}
					// handle maintenance mode
					if (AppComponents.commonService.inMaintenanceMode(httpResponse, iaaUser.getLoginId())) {
						return;
					}
				} catch (AppException e) {
					String errStr = e.getMessage();
					logger.error(errStr);
					AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, errStr);
					return;
				}
				// SecondFactorInfo secondFactorInfo =
				// AppUtil.getSecondFactorInfoFromToken(token);
				// String newToken = AppComponents.iaaService.issueToken(iaaUser,
				// secondFactorInfo);
				// httpResponse.addHeader("Token", newToken);
//				 httpResponse.addHeader("Access-Control-Allow-Origin",
//				 authParameters.getAngularOrigin());
				httpResponse.addHeader("Access-Control-Allow-Headers",
						"X-Requested-With,Origin,Content-Type, Accept, Token");
				httpResponse.addHeader("Access-Control-Expose-Headers", "Token");

				logger.info("AuthenticationFilter - Issue new token........");
				String newToken = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN);
				httpResponse.addHeader("Token", newToken);

			} else {

				// skip auth
				String loginId = request.getParameter("loginId");

				iaaUser = AppComponents.iaaService.getAnonymousIaaUserByLoginId(loginId);

				// handle maintenance mode
				if (AppComponents.commonService.inMaintenanceMode(httpResponse, loginId)) {
					return;
				}
				logger.warn("AuthenticationFilter - back-end skip auth.......!!!!!!!, get Anonymous user with loginId = "
						+ loginId);

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
				infoStr = "JappAuthenticationFilter.... end...Request URI = <" + requestURI + ">, Query String = <"
						+ queryStr + ">, total time = " + totalTS + " millis. \n";
			} else if (totalTS > warningSeconds * 1000 && totalTS <= alertSeconds * 1000) {
				infoStr = "JappAuthenticationFilter...PERFORMANCE WARNING - Request URI = <" + requestURI
						+ ">, Query String = <" + queryStr + ">, total time = " + totalTS + " millis. \n";

				Date scheduledDate = null;
				SystemNotification sn = new SystemNotification(SystemNotifyService.CATEGORY_PERFORMANCE_WARNING,
						scheduledDate, infoStr);
				AppComponents.systemNotifyService.addNotification(sn);
			} else {
				infoStr = "JappAuthenticationFilter...PERFORMANCE ALERT - Request URI = <" + requestURI
						+ ">, Query String = <" + queryStr + ">, total time = " + totalTS + " millis. \n";

				Date scheduledDate = null;
				SystemNotification sn = new SystemNotification(SystemNotifyService.CATEGORY_PERFORMANCE_ALERT,
						scheduledDate, infoStr);
				AppComponents.systemNotifyService.addNotification(sn);
			}
			logger.info(infoStr);
		} finally{
			AppUtil.clearTransactionContext();
		}
	}
	private boolean isExcluded(HttpServletRequest request) {
		if(excludePathList == null) {
			String authExcludePaths = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_AUTH_EXCLUDE_PATHS);
            logger.info("authExcludePaths------"+authExcludePaths);
			if(StringUtil.isEmptyOrNull(authExcludePaths)) {
				excludePathList = new ArrayList<String>();
				return false;
			}
			excludePathList = Arrays.asList(authExcludePaths.split(";"));
		}
		if(excludePathList.isEmpty()) {
			return false;
		}
		for(String str: excludePathList) {
			String path = request.getRequestURI();
            int idx = path.indexOf(str);
            logger.debug("isExcluded() - request URI: "+  path + ", exclude string: "+str);
            if(idx >= 0) {
            	return true;
            }
		}
        return false;
    }


	@Override
	public void destroy() {
		AppUtil.clearTransactionContext();
	}
}
