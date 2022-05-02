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
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.provider.BaseAuthProviderHandler;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "coreLoginServlet", urlPatterns = "/open/core/login")
public class CoreLoginServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CoreLoginServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("login doGet =======begin==================");
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		logger.debug("login doPost =======begin==================");
		//AppUtil.initTransactionContext(request);

		// App CIDR white list check begin
		if (!AppComponents.commonService.matchClientAppIpWhiteList(request)) {
			logger.error("Authorization Failed. Request IP is not on the App's IP white list, user IP = "
					+ AppUtil.getClientIp(request) + ".....");
			AppUtil.setHttpResponse(response, 403, Status.ERROR_SECURITY_AUTHENTICATION, "Authorization Failed.");
			return;
		}

		
		String origin = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN);

		logger.debug("set login Header with CORS ==========================");
		response.addHeader("Access-Control-Allow-Origin", origin);
		response.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
		response.addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization");


		BaseAuthProviderHandler handler = BaseAuthProviderHandler.getHandler(request, response);
		if(handler != null) {
			handler.handleUiLogin(request, response);
		}
		
		return;
	}

}
