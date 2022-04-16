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

import org.springframework.stereotype.Component;

/**
 * Defines all keys used in the property file.
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class AppConfigKeys {

//	public static final String OAUTH2_AUTHROIZATION_URL = "sdcsts.authorization.url";
//	public static final String OAUTH2_TOKEN_URL = "sdcsts.token.url";
//	public static final String OAUTH2_CLIENT_ID = "sdcsts.clientid";
//	public static final String OAUTH2_RESOURCE = "sdcsts.resource";
//	public static final String OAUTH2_SECRET = "sdcsts.secret";
//	public static final String OAUTH2_REDIRECT_URI = "sdcsts.redirecturi";
//	public static final String OAUTH2_METADATA = "sdcsts.metadata";

	public static final String SERVER_SSL_KEY_STORE = "server.ssl.key-store";
	public static final String SERVER_SSL_KEY_STORE_PASSWORD = "server.ssl.key-store-password";
	public static final String SERVER_SSL_KEY_ALIAS = "server.ssl.key-alias";
	public static final String SERVER_SSL_KEY_PASSWORD = "server.ssl.key-password";
	public static final String SERVER_SSL_ENABLED = "server.ssl.enabled";
	public static final String SERVER_DESKTOP_LOGIN_CALLBACK_URL = "server.ssl.enabled";
	
	public static final String JAPPCORE_HTTP_PROXY_SERVER = "jappcore.http.proxy.server";
	public static final String JAPPCORE_HTTP_PROXY_PORT = "jappcore.http.proxy.port";

	public static final String JAPPCORE_APP_APPLICATION_ID = "jappcore.app.application.id";
	public static final String JAPPCORE_APP_APPLICATION_VERSION = "jappcore.app.application.version";
	public static final String JAPPCORE_APP_SPRING_SCAN_BASE_PACKAGE = "jappcore.app.spring.scan.base.package";
	public static final String JAPPCORE_APP_API_CONTROLLER_PATH_ROOT = "jappcore.app.api.controller.path.root";
	public static final String JAPPCORE_APP_MAINTENANCE_MODE_ENABLED = "jappcore.app.maintenance.mode.enabled";
	public static final String JAPPCORE_APP_MAINTENANCE_MODE_ROLE_ALLOWED = "jappcore.app.maintenance.mode.role.allowed";
	public static final String JAPPCORE_APP_SYSTEM_NOTIFICATION_EMAIL_ENABLED = "jappcore.app.system.notification.email.enabled";
	public static final String JAPPCORE_APP_SYSTEM_NOTIFICATION_SCHEDULE_CRON = "jappcore.app.system.notification.schedule.cron";
	public static final String JAPPCORE_APP_SYSTEM_MAINTENANCE_CRON = "jappcore.app.system.maintenance.cron";
	public static final String JAPPCORE_APP_SYSTEM_PERFORMANCE_WARNING_THRESHOLD_SECONDS = "jappcore.app.system.performance.warning.threshold.seconds";
	public static final String JAPPCORE_APP_SYSTEM_PERFORMANCE_ALERT_THRESHOLD_SECONDS = "jappcore.app.system.performance.alert.threshold.seconds";
	public static final String JAPPCORE_APP_LOG_DIR = "jappcore.app.log.dir";
	public static final String JAPPCORE_APP_LOG_FILE_PREFIX = "jappcore.app.log.file.prefix";
	public static final String JAPPCORE_APP_LOG_CURRENT_LOG_FILENAME = "jappcore.app.log.current.log.filename";
	public static final String JAPPCORE_APP_STARTUP_NOTIFICATION_WAITING_SECONDS = "jappcore.app.startup.notification.waiting.seconds";
	public static final String JAPPCORE_APP_COMMAND_CONTROLLER_ENABLED = "jappcore.app.command.controller.enabled";
//	public static final String JAPPCORE_APP_PKI_CONTROLLER_ENABLED = "jappcore.app.pki.controller.enabled";
//	public static final String JAPPCORE_APP_DEFAULT_CONTROLLER_ENABLED = "jappcore.app.default.controller.enabled";
//	public static final String JAPPCORE_APP_SECURITY_CONTROLLER_ENABLED = "jappcore.app.security.controller.enabled";
//	public static final String JAPPCORE_APP_AUTH_CONTROLLER_ENABLED = "jappcore.app.auth.controller.enabled";
	public static final String JAPPCORE_APP_CORE_CONTROLLER_ENABLED_COMMANDS = "jappcore.app.core.controller.enabled.commands";
	
	public static final String JAPPCORE_IAA_TOKEN_VALIDATE_IP_ENABLED = "jappcore.iaa.token.validate.ip.enabled";
	public static final String JAPPCORE_IAA_2NDFACTOR_TYPE = "jappcore.iaa.2ndfactor.type";
	public static final String JAPPCORE_IAA_CIDR_USER_WHITELIST_ENABLED = "jappcore.iaa.cidr.user.whitelist.enabled";
	public static final String JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST_ENABLED = "jappcore.iaa.cidr.application.whitelist.enabled";
	public static final String JAPPCORE_IAA_CIDR_APPLICATION_WHITELIST = "jappcore.iaa.cidr.application.whitelist";
	public static final String JAPPCORE_IAA_CIDR_SYSTEMUSER_WHITELIST = "jappcore.iaa.cidr.whitelist.systemuser";
	public static final String JAPPCORE_IAA_API_AUTH_ENABLED = "jappcore.iaa.japp.api.auth.enabled";
	public static final String JAPPCORE_IAA_TOKEN_ACCESS_EXPIRATION_LENGTH = "jappcore.iaa.token.access.expiration.length.mins";
	public static final String JAPPCORE_IAA_TOKEN_REFRESH_EXPIRATION_LENGTH = "jappcore.iaa.token.refresh.expiration.length.mins";
	public static final String JAPPCORE_IAA_TOKEN_IDTOKEN_EXPIRATION_LENGTH = "jappcore.iaa.token.idtoken.expiration.length.mins";
	public static final String JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH = "jappcore.iaa.token.verify.expiration.length";
	public static final String JAPPCORE_IAA_TOKEN_ISSUE_ISS = "jappcore.iaa.token.issue.iss";
	public static final String JAPPCORE_IAA_TOKEN_VALIDATE_AUD = "jappcore.iaa.token.validate.aud";
	public static final String JAPPCORE_IAA_AUTHENTICATION_PROVIDER = "jappcore.iaa.authentication.provider";
	public static final String JAPPCORE_IAA_APPLICATION_ROLECHECK_ENABLED = "jappcore.iaa.application.rolecheck.enabled";
	public static final String JAPPCORE_IAA_APPLICATION_ROLE_LIST = "jappcore.iaa.application.rolelist";
	public static final String JAPPCORE_IAA_AUTH_APP_CALLBACK_URL = "jappcore.iaa.auth.app.callback.url";
	//public static final String JAPPCORE_IAA_USE_THIRDPARTY_TOKEN = "jappcore.iaa.use.thirdparty.token";
	public static final String JAPPCORE_IAA_ACCESSTOKEN_HANDLER_NAME = "jappcore.iaa.accessstoken.handler.name";
	public static final String JAPPCORE_IAA_AUTH_EXCLUDE_PATHS = "jappcore.iaa.auth.exclude.paths";
	public static final String JAPPCORE_IAA_PROCESSOR_EMPTY_TARGET_ROLE_ALLOWED = "jappcore.iaa.processor.empty.target.role.allowed";
	public static final String JAPPCORE_IAA_DESKTOP_LOGIN_CALLBACK_URL = "jappcore.iaa.auth.desktop.callback.url";
	public static final String JAPPCORE_IAA_AAD_REDIRECT_URL =  "jappcore.iaa.aad.redirect.url";
	
	public static final String JAPPCORE_FRONTEND_UI_FRAMEWORK = "jappcore.frontend.ui.framework";
	public static final String JAPPCORE_FRONTEND_UI_ORIGIN = "jappcore.frontend.ui.origin";
	public static final String JAPPCORE_FRONTEND_UI_POST_SIGNOUT_PAGE = "jappcore.frontend.ui.post.signout.page";
	public static final String JAPPCORE_FRONTEND_UI_HOME_PAGE = "jappcore.frontend.ui.home.page";
	public static final String JAPPCORE_FRONTEND_UI_TOKEN_PAGE = "jappcore.frontend.ui.token.page";
	public static final String JAPPCORE_FRONTEND_UI_MAINTENANCE_PAGE = "jappcore.frontend.ui.maintenance.page";
	public static final String JAPPCORE_FRONTEND_UI_SECURE_COOKIE_ENABLED = "jappcore.frontend.ui.secure.cookie.enabled";

	public static final String JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_JS_PATH = "jappcore.iaa.transfer.toekn.to.client.js.path";
	public static final String JAPPCORE_IAA_TRANSFER_TOKEN_TO_CLIENT_CSS_PATH = "jappcore.iaa.transfer.toekn.to.client.css.path";

//	public static final String JAPPCORE_FRONTEND_ONLINE_TOKEN_LOADER_JS_PATH = "jappcore.frontend.onlinetokenloader.js.path";
//	public static final String JAPPCORE_FRONTEND_DESKTOP_TOKEN_LOADER_JS_PATH = "jappcore.frontend.desktoptokenloader.js.path";
//	public static final String JAPPCORE_FRONTEND_ONLINE_HANDLE_REDIRECT_JS_PATH = "jappcore.frontend.onlinehandleredirect.js.path";
//	public static final String JAPPCORE_FRONTEND_ONLINE_LOGIN_ERROR_JS_PATH = "jappcore.frontend.onlineloginerror.js.path";

	public static final String JAPPCORE_CACHE_REFRESH_INTERVAL = "jappcore.cache.refresh.interval.millis";
	public static final String JAPPCORE_CACHE_REFRESH_LEAST_INTERVAL = "jappcore.cache.refresh.least.interval.mins";
	public static final String JAPPCORE_CACHE_DAILY_REFRESH_ENABLED = "jappcore.cache.daily.refresh.enabled";
	public static final String JAPPCORE_CACHE_USER_CACHE_MAX_SIZE = "jappcore.cache.user.cache.max.size";

	public static final String JAPPCORE_EMAIL_ASYNC_WAITFORRESPONSE = "jappcore.email.async.waitforresponse";
	public static final String JAPPCORE_EMAIL_DEFAULT_TO_ASYNC = "jappcore.email.default.to.async";
	public static final String JAPPCORE_EMAIL_PROVIDER = "jappcore.email.provider";
	public static final String JAPPCORE_EMAIL_BUS_TOADDRESSES = "jappcore.email.bus.toaddresses";
	public static final String JAPPCORE_EMAIL_IT_TOADDRESSES = "jappcore.email.it.toaddresses";
	public static final String JAPPCORE_EMAIL_SYSTEM_FROMADDRESS = "jappcore.email.system.fromaddress";
	public static final String JAPPCORE_EMAIL_SYSTEM_REPLYTOADDRESS = "jappcore.email.system.replytoaddress";
	public static final String JAPPCORE_EMAIL_SENDGRID_API_KEY = "jappcore.email.sendgrid.api.key";
	public static final String JAPPCORE_EMAIL_SENDASGROUP = "jappcore.email.sendasgroup";
	public static final String JAPPCORE_EMAIL_MAX_ATTACHMENT_FILE_SIZE_BYTES = "jappcore.email.max.attachment.file.size.bytes";

	public static final String JAPPCORE_ASYNC_EXECUTOR_CORE_POOL_SIZE = "jappcore.async.executor.core.pool.size";
	public static final String JAPPCORE_ASYNC_EXECUTOR_QUEUE_CAPACITY = "jappcore.async.executor.queue.capacity";
	public static final String JAPPCORE_ASYNC_EXECUTOR_MAX_POOL_SIZE = "jappcore.async.executor.max.pool.size";
	public static final String JAPPCORE_ASYNC_EXECUTOR_REJECT_POLICY = "jappcore.async.executor.reject.policy";
	
//	public static final String JAPPCORE_SWAGGER_API_BASE_PACKAGE = "jappcore.swagger.api.base.package";
//	public static final String JAPPCORE_SWAGGER_API_PATH_ANT_PATTERN = "jappcore.swagger.api.path.ant.pattern";
//	public static final String JAPPCORE_SWAGGER_API_SECURITY_REQUIREMENT_NAME = "jappcore.swagger.api.security.requirement.name";
	
	public static final String JAPPCORE_DYNAMIC_AUTHROIZATION_URL = "jappcore.dynamic.authorization.url";
	public static final String JAPPCORE_APPLICATION_ID = "jappcore.application.id";

	public static final String AAD_CLIENT_ID = "aad.clientid";
	public static final String AAD_AUTH_PROMPT = "aad.auth.prompt";
	public static final String AAD_OPEN_ID_METADATA_URL = "aad.openIdMetaDataUrl";
	public static final String AAD_AUTH_URL = "aad.auth.url";
	public static final String AAD_AUTH_LOGOUT_URL = "aad.auth.logout.url";

	public static final String AZURE_KEYVAULT_ENABLED = "azure.keyvault.enabled";
	public static final String AZURE_KEYVAULT_URI = "azure.keyvault.uri";
	public static final String AZURE_KEYVAULT_CLIENT_ID = "azure.keyvault.client-id";
	public static final String AZURE_KEYVAULT_CLIENT_KEY = "azure.keyvault.client-key";
	public static final String AZURE_KEYVAULT_TOKEN_ACQUIRE_TIMEOUT_SECONDS = "azure.keyvault.token-acquire-timeout-seconds";

	public static final String AZURE_KEYVAULT_JAPPCORE_PKCS12_KEY = "JAPPCORE_PKCS12";
	public static final String AZURE_KEYVAULT_JAPPCORE_PKCS12_PASSWORD = "azure.keyvault.secret.pkcs12.password";
	public static final String AZURE_KEYVAULT_CECRET_KEY_PASSWORD = "azure.keyvault.secret.key.password";
	public static final String AZURE_KEYVAULT_CECRET_KEY_ALIAS = "aazure.keyvault.secret.key.alias";

	public static final String SPRING_JACKSON_TIMEZONE = "spring.jackson.time-zone";

	public static final String JAPPCORE_IAA_BASIC_AUTHROIZATION_URL = "jappcore.iaa.basic.authorization.url";
	//public static final String JAPPCORE_IAA_BASIC_RESOURCE = "jappcore.iaa.basic.resource";	
	//public static final String JAPPCORE_IAA_BASIC_REDIRECT_URI = "jappcore.iaa.basic.redirecturi";
	
	

}
