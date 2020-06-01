package com.itdevcloud.japp.core.api.gateway;

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
import com.itdevcloud.tools.common.util.StringUtil;


//import ca.on.gov.ltc.startkit.common.SkConfigKeys;
 
@Configuration
public class OpenApiConfig {
	
	private static final Logger logger = LogManager.getLogger(OpenApiConfig.class);

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

//	@Value("${startkit.openapi.server.list}")
//	private String serverListStr;

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


