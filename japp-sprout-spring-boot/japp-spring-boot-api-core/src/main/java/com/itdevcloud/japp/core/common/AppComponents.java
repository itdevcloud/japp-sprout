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

import com.itdevcloud.japp.core.cahce.AppConfigCache;
import com.itdevcloud.japp.core.cahce.AuthProviderCache;
import com.itdevcloud.japp.core.cahce.EntraIdJwksCache;
import com.itdevcloud.japp.core.cahce.IaaAppInfoCache;
import com.itdevcloud.japp.core.cahce.IaaUserCache;
import com.itdevcloud.japp.core.cahce.PkiKeyCache;
import com.itdevcloud.japp.core.cahce.ReferenceCodeCache;
import com.itdevcloud.japp.core.frontend.FrontendEnvSetupService;
import com.itdevcloud.japp.core.iaa.service.IaaService;
import com.itdevcloud.japp.core.iaa.service.JwtService;
import com.itdevcloud.japp.core.iaa.service.PkiService;
import com.itdevcloud.japp.core.iaa.service.azure.AzureJwksService;
import com.itdevcloud.japp.core.service.email.AsyncEmailService;
import com.itdevcloud.japp.core.service.email.EmailService;
import com.itdevcloud.japp.core.service.log.LogFileService;
import com.itdevcloud.japp.core.service.notification.SystemNotifyService;
import com.itdevcloud.japp.core.service.referencecode.ReferenceCodeService;
import com.itdevcloud.japp.core.service.startup.StartupService;
import com.itdevcloud.japp.core.session.SessionService;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AppComponents {

	public static CommonService commonService;
	public static JwtService jwtService;
	public static IaaService iaaService;
	public static PkiService pkiService;
	public static AzureJwksService azureJwksService;
	public static SessionService sessionService;
	
	public static FrontendEnvSetupService frontendEnvSetupService;
	public static StartupService startupService;

	public static SystemNotifyService systemNotifyService;
	
	public static EmailService emailService;
	public static AsyncEmailService asyncEmailService;

	public static HttpService httpService;
	public static ReferenceCodeService referenceCodeService;
	public static LogFileService logFileService; 
	
	public static IaaUserCache iaaUserCache;
	public static PkiKeyCache pkiKeyCache;
	public static EntraIdJwksCache aadJwksCache;
	public static ReferenceCodeCache referenceCodeCache;
	public static IaaAppInfoCache iaaAppInfoCache;
	public static AuthProviderCache authProviderCache;
	
	//appConfigCache is special - for configure
	//must be defined in the AppConfigService, not here
	//AppConfigService will be inited before this class
	//public static AppConfigCache appConfigCache;


}
