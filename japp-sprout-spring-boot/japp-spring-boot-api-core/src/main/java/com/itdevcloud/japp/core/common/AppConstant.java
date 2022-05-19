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

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppConstant {

	public static enum LogicOperation {NOT, AND, OR};

	public static final String IDENTITY_PROVIDER_CORE_AAD_OIDC = "CORE-AAD-OIDC";
	public static final String IDENTITY_PROVIDER_CORE_AAD_OAUTH2 = "CORE-AAD-OAUTH2";
	public static final String IDENTITY_PROVIDER_CORE_BASIC = "CORE-BASIC";
	public static final String IDENTITY_PROVIDER_CORE_OKTA_OIDC = "CORE-OKTA-OIDC";
	public static final String CONTEXT_KEY_JAPPCORE_TX_ID = "JAPPCORE_TX_ID";

	public static final String FRONTEND_UI_FRAMEWORK_NONE = "none";
	public static final String FRONTEND_UI_FRAMEWORK_ANGULAR = "angular";


	public static final String IAA_MULTI_FACTOR_TYPE_NONE = "none";
	public static final String IAA_MULTI_FACTOR_TYPE_VERIFICATION_CODE_EMAIL = "verificationCodeEmail";
	public static final String IAA_MULTI_FACTOR_TYPE_VERIFICATION_CODE_SMS = "verificationCodeSMS";
	public static final String IAA_MULTI_FACTOR_TYPE_VERIFICATION_CODE_PHONE_CALL = "verificationCodePhoneCall";
	public static final String IAA_MULTI_FACTOR_TYPE_TOTP = "TOTP";

	public static final String HTTP_AUTHORIZATION_HEADER_NAME = "Authorization";
	public static final String HTTP_AUTHORIZATION_COOKIE_NAME = "CoreTokenCookie";
	public static final String HTTP_AUTHORIZATION_ARG_NAME_CLIENT_APP_ID = "CoreClientAppId";
	public static final String HTTP_AUTHORIZATION_ARG_NAME_TOKEN_NONCE = "CoreTokenNonce";
	public static final String HTTP_AUTHORIZATION_ARG_NAME_CLIENT_AUTH_KEY = "CoreClientAuthKey";
	public static final String HTTP_AUTHORIZATION_ARG_NAME_LOGIN_ID = "CoreLoginId";


	public static final String JAPPCORE_SPRING_ACTIVE_PROFILE_PROD = "PROD";
	public static final String JAPPCORE_CLIENT_APP_ID = "CoreApp";
	public static final String JAPPCORE_CLIENT_APP_NAME = "CoreApp";
	public static final String JAPPCORE_OPENAPI_CORE_SECURITY_SCHEMA_NAME = "core-bear-jwt";

	public static final String ASYNC_EXECUTOR_REJECT_POLICY_DEFAULT = "default";
	public static final String ASYNC_EXECUTOR_REJECT_POLICY_CALLERRUN = "callerrun";
	public static final String ASYNC_EXECUTOR_REJECT_POLICY_REQUEUE = "requeue";


	public static final String STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT = "FrontendUiEnvironment";
	public static final String STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE = "AadJwksCache";
	public static final String STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE = "PkiKeyCache";
	public static final String STARTUP_NOTIFY_KEY_REFERENCE_CODE_CACHE = "ReferenceCodeCache";
	public static final String STARTUP_NOTIFY_KEY_CLIENT_APPINFO_CACHE = "ClientAppInfoCache";

}
