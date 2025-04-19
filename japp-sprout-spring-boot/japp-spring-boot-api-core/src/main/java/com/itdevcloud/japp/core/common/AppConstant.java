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

	public static final String AUTH_PROVIDER_TYPE_AUTHZ = "PROVIDER_TYPE_AUTHZ";
	public static final String AUTH_PROVIDER_TYPE_AUTHN = "PROVIDER_TYPE_AUTHN";
	public static final String AUTH_PROVIDER_TYPE_AUTHN_AUTHZ = "PROVIDER_TYPE_AUTHN_AUTHZ";
	
	public static final String AUTH_PROVIDER_NAME_ENTRAID_OPENID = "ENTRAID_OPENID";
	public static final String AUTH_PROVIDER_NAME_MY_APP = "MY_APP";
	public static final String AUTH_PROVIDER_NAME_JAPP_IAA = "JAPP_IAA";
	
	public static final String AUTHN_TYPE_BASIC = "MY_APP_BASIC";
	public static final String AUTHN_TYPE_BASIC_TOTP = "MY_APP_BASIC_TOTP";
	public static final String AUTHN_TYPE_BASIC_TOTP_CERT = "MY_APP_BASIC_TOTP_CERT";
	

	public static final String FRONTEND_UI_FRAMEWORK_NONE = "none";
	public static final String FRONTEND_UI_FRAMEWORK_ANGULAR = "angular";

	public static final String JWT_TOKEN_NAME = "Japp_Jwt";
	public static final String JWT_TOKEN_ISSUE_BY = "Japp_ApiServer";

	public static final String JWT_CLAIM_KEY_ISSUE_APPID = "iss";
	public static final String JWT_CLAIM_KEY_TARGET_APPID = "aud";
	
	public static final String JWT_CLAIM_KEY_CONTEXT_ID = "ctxId";
	public static final String JWT_CLAIM_KEY_TARGET_IP = "targetIp";
	public static final String JWT_CLAIM_KEY_TIMEOUT_AT = "timeoutAt";
	public static final String JWT_CLAIM_KEY_2NDFACTOR_VERIFIED = "secondFactorVerified";
	public static final String JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT = "secondFactorRetryCount";
	public static final String JWT_CLAIM_KEY_2NDFACTOR_TYPE = "secondFactorType";
	public static final String JWT_CLAIM_KEY_2NDFACTOR_VALUE = "secondFactorValue";

	public static final String IAA_2NDFACTOR_TYPE_NONE = "none";
	public static final String IAA_2NDFACTOR_TYPE_VERIFICATION_CODE = "verificationCode";
	public static final String IAA_2NDFACTOR_TYPE_TOTP = "TOTP";

	public static final String HTTP_AUTHORIZATION_HEADER_NAME = "Authorization";
	public static final String HTTP_AUTHORIZATION_COOKIE_NAME = "Japp_Token_Cookie";

	public static final String BUSINESS_ROLE_IT_SUPPORT = "JAPP.BR.IT.SUPPORT";


	public static final String JAPPCORE_TX_ID = "JAPPCORE_TX_ID";
	public static final String JAPPCORE_SPRING_ACTIVE_PROFILE_PROD = "PROD";

	public static final String ASYNC_EXECUTOR_REJECT_POLICY_DEFAULT = "default";
	public static final String ASYNC_EXECUTOR_REJECT_POLICY_CALLERRUN = "callerrun";
	public static final String ASYNC_EXECUTOR_REJECT_POLICY_REQUEUE = "requeue";


	public static final String STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT = "FrontendUiEnvironment";
	public static final String STARTUP_NOTIFY_KEY_AAD_JWKS_CACHE = "AadJwksCache";
	public static final String STARTUP_NOTIFY_KEY_JAPPCORE_KEY_CACHE = "PkiKeyCache";
	public static final String STARTUP_NOTIFY_KEY_REFERENCE_CODE_CACHE = "ReferenceCodeCache";

	public static final String SMS_DOMAIN_SESSION = "SESSION";
	public static final String SMS_DOMAIN_MFA = "SESSION_MFA";
	public static final String SMS_DOMAIN_JWT_BLACKLIST = "JWT_BLACKLIST";
	public static final String SMS_DOMAIN_EVENT = "EVENT";
	
	public static final String SMS_KEY_MFA_TOTP = "MFA_TOTP";
	public static final String SMS_KEY_MFA_OTP = "MFA_OTP";
	public static final String SMS_KEY_USER_IAA_UID = "USER_IAA_UID";
	public static final String SMS_KEY_USER_PROFILE_UID = "USER_PROFILE_UID";

}
