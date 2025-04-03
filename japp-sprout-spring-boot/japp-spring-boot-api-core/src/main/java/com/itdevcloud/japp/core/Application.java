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
package com.itdevcloud.japp.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 * Spring Boot Application startup class.
 * <p>
 * To deploy war to Tomcat, make the @SpringBootApplication class 
 * extends SpringBootServletInitializer
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@ServletComponentScan
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableScheduling
public class Application extends SpringBootServletInitializer {

	//must init ConfigFactory before init AppFactory
	@Autowired
	ConfigFactory configFactory;
	
	//must init AppFactory during the startup process
	@Autowired
	private AppFactory jappFactory;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/**
	 * PropertySourcesPlaceHolderConfigurer Bean only required for @Value("{}")
	 * annotations. Remove this bean if you are not using @Value annotations for
	 * injecting properties.
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Configure cross origin requests settings.
	 * @return WebMvcConfigurer
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {

			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedMethods("*").allowedOrigins(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN));
			}

		};
	}
}