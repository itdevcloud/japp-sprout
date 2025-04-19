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

import jakarta.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.common.AppConstant;

/**
 * This servlet is designed as a call back class for Azure AD authentication provider. It 
 * provides the authentication and authorization services.
 * <p>
 * First, it receives a JWT issued by Azure AD, then this servlet will 
 * verify if this JWT is valid. After check if this is an authorized user, 
 * an application specific JWT will be issued to the client.
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "aadAuthCallbackServlet", urlPatterns = "/open/aadauth")
public class AadAuthCallbackServlet extends AuthnCallbackServletBase {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AadAuthCallbackServlet.class);



	@Override
	protected String getAuthnProvider() {
		return AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID;
	}

}
