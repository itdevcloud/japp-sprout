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
package com.itdevcloud.japp.core.api.gateway;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.config.AppConfigService;
import com.itdevcloud.japp.se.common.util.StringUtil;



@Configuration
public class OpenApiConfig {
	
	//private static final Logger logger = LogManager.getLogger(OpenApiConfig.class);

	@Value("${jappcore.openapi.info.title}")
	private String infoTitle;
	@Value("${jappcore.openapi.info.description}")
	private String infoDescription;
	@Value("${jappcore.openapi.info.version}")
	private String infoVersion;
	@Value("${jappcore.openapi.info.contact.name}")
	private String infoContactName;
	@Value("${jappcore.openapi.info.contact.url}")
	private String infoContactUrl;
	@Value("${jappcore.openapi.info.contact.email}")
	private String infoContactEmail;

	@Value("${jappcore.openapi.info.license.name}")
	private String licenseName;
	@Value("${jappcore.openapi.info.license.url}")
	private String licenseUrl;

	@Value("${jappcore.openapi.externalDocs.description}")
	private String externalDocsDescription;
	@Value("${jappcore.openapi.externalDocs.url}")
	private String externalDocsUrl;


	@Bean
	public OpenAPI customOpenAPI() {
		
		AppConfigService appConfigService = ConfigFactory.appConfigService;
		String serverListStr = appConfigService.getPropertyAsString("jappcore.openapi.server.list");
		String securityRequirementName = appConfigService.getPropertyAsString("jappcore.openapi.security.requirement.name");
		
		OpenAPI openapi =  new OpenAPI()
				.components(new Components().addSecuritySchemes(securityRequirementName,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")))
				.info(new Info().title(infoTitle)
						        .version(infoVersion)
						        .description(infoDescription)
						        .contact(new Contact().name(infoContactName).url(infoContactUrl).email(infoContactEmail))
						        .license(new License().name(licenseName).url(licenseUrl)))
				.externalDocs(new ExternalDocumentation().description(externalDocsDescription).url(externalDocsUrl));

		if(!StringUtil.isEmptyOrNull(serverListStr)) {
			String[] serverArr = serverListStr.split(";");
			for (String str : serverArr) {
				str = str.trim();
				if(!StringUtil.isEmptyOrNull(str) && !str.equals(",")) {
					int idx = str.indexOf(",");
					String url = null;
					String desc = null;
					if(idx < 0) {
						url = str;
						desc = null;
					}else if(idx == 0){
						url = null;
						desc = str.substring(idx+1);
					}else {
						url = str.substring(0, idx);
						desc = str.substring(idx+1);
					}
					if(url != null) {
						openapi.addServersItem(new Server().url(url).description(desc));
					}
				}
			}
		}
		return openapi;
	}
}


