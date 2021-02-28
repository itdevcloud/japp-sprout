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

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 * The SpecialCharacterFilter provides the support for checking if a incoming request contains some special characters
 * which may cause cross-site scripts attack, or some special characters which may cause 400 Bad Request error.
 * <p>
 * This Filter's url pattern is "/*", which means this filter will check all incoming requests.
 * @author Ling Yang
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebFilter(filterName = "SpecialCharacterFilter", urlPatterns = "/*")
public class SpecialCharacterFilter implements Filter {
	private static final Logger logger = LogManager.getLogger(SpecialCharacterFilter.class);


	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("SpecialCharacterFilter.doFilter() - start......");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		AppUtil.initTransactionContext(httpRequest);

		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);
				
		// control for CORS flow
		if (httpRequest.getMethod().equals("OPTIONS")) {
			httpResponse.addHeader("Access-Control-Allow-Origin", origin);
			httpResponse.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
			httpResponse.addHeader("Access-Control-Allow-Headers",
					"Origin, X-Requested-With, Content-Type, Accept, Authorization, JAPP-Token, CallingApp-Token");
			//httpResponse.addHeader("Access-Control-Allow-Credentials","true");
			httpResponse.setStatus(200);
			return;
		}
		//every call will go through this filter, so this is the plcae to add CORS header	
		httpResponse.addHeader("Access-Control-Allow-Origin", origin);
		//httpResponse.addHeader("Access-Control-Allow-Credentials","true");
		//httpResponse.addHeader("Access-Control-Allow-Headers",
		//		"Origin, X-Requested-With, Content-Type, Accept, Authorization");
		httpResponse.addHeader("Access-Control-Expose-Headers", "JAPP-Token");
		
		if (httpRequest.getMethod().equals("GET")) {
			httpResponse.addHeader("Cache-Control", "no-cache");
		}

		//check for Special Characters in query string.
		String queryStr = httpRequest.getQueryString();
		if (queryStr!=null) {
			if(SpecialCharacterUtil.checkSpecialBlockCharacters(queryStr)||SpecialCharacterUtil.checkCrossSiteScriptCharacters(queryStr)) {
				logger.error("The input has invalid character.");
				httpResponse.sendError(400, "request is not acceptable, invalid input.");
				return;
			}
		}
		
		chain.doFilter(request, httpResponse);// sends request to next resource
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
