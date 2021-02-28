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
package com.itdevcloud.japp.core.frontend;

/**
*
* @author Marvin Sun
* @since 1.0.0
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;


@Component
public class FrontendEnvSetupService implements AppFactoryComponentI {

	private static FrontendEnvironment frontendEnvironment = null;
	private static final String DEFAULT_FRONTEND_ENV = "default";
	private static final Logger logger = LogManager.getLogger(FrontendEnvSetupService.class);

	public static FrontendEnvironment getFrontendEnvironment() {
		return frontendEnvironment;
	}



	@PostConstruct
	public void init() {
	}

	public void setupFrontendEnvironment() {
		logger.info("setupFrontendEnvironment() - start...");

		if (AppConstant.FRONTEND_UI_FRAMEWORK_ANGULAR
				.equalsIgnoreCase(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_FRAMEWORK))) {
			CopyAngulerEnvJsonFile();
		}else {
			String info = "No supported front-end defined.";
			AppComponents.startupService
			.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT, info);

		}
		logger.info("setupFrontendEnvironment() - end...");

	}

	private void CopyAngulerEnvJsonFile() {

		logger.info("CopyAngulerEnvJsonFile() - start...");
		BufferedReader bufferedReader = null;
		try {
			String env = AppUtil.getSpringActiveProfile();
			String deploymentRootDir = AppUtil.getDeploymentRootDir();

			String envPath = deploymentRootDir + "assets/environments/";
			Path srcFilePath = Paths.get(envPath + "environment-" + env + ".json");
			Path destFilePath = Paths.get(envPath + "environment.json");

			if (!StringUtil.isEmptyOrNull(env) && !DEFAULT_FRONTEND_ENV.equalsIgnoreCase(env)
					&& !StringUtil.isEmptyOrNull(deploymentRootDir)) {

				logger.info("FrontendEnvSetupUtil.CopyAngulerEnvJsonFile().....srcFilePath = " + srcFilePath);
				logger.info("FrontendEnvSetupUtil.CopyAngulerEnvJsonFile().....destFilePath = " + destFilePath);

				Files.copy(srcFilePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
				String info = "FrontendEnvSetupUtil.CopyAngulerEnvJsonFile()...Success, srcFilePath = " + srcFilePath
						+ ",...destFilePath = " + destFilePath;

				bufferedReader = new BufferedReader(new FileReader(destFilePath.toFile()));
				Gson gson = new Gson();
				frontendEnvironment = gson.fromJson(bufferedReader, FrontendEnvironment.class);
				if (frontendEnvironment != null && frontendEnvironment.getSettings() != null) {
					info = info + "\r\n" + "Version: " + frontendEnvironment.getSettings().getVersion() + "\r\n"
							+ "Build: " + frontendEnvironment.getSettings().getBuild() + "\r\n\r\n";
				} else {
					info = info + "\r\n" + "Version: n/a" + "\r\n" + "Build: n/a" + "\r\n\r\n";
				}
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT, info);
			} else {
				String info = "FrontendEnvSetupUtil.CopyAngulerEnvJsonFile().....'spring.profiles.active' is not defined or 'spring.profiles.active' is 'default' or 'jappcore.deployment.root.dir' is not defined, does NOT copy environemnt JSON file......!!! ";
				bufferedReader = new BufferedReader(new FileReader(destFilePath.toFile()));
				Gson gson = new Gson();
				frontendEnvironment = gson.fromJson(bufferedReader, FrontendEnvironment.class);
				if (frontendEnvironment != null && frontendEnvironment.getSettings() != null) {
					info = info + "\r\n" + "Version: " + frontendEnvironment.getSettings().getVersion() + "\r\n"
							+ "Build: " + frontendEnvironment.getSettings().getBuild() + "\r\n\r\n";
				} else {
					info = info + "\r\n" + "Version: n/a" + "\r\n" + "Build: n/a" + "\r\n\r\n";
				}
				logger.warn(info);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT, info);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("CopyAngulerEnvJsonFile() - error = " + e);
			logger.error(e);
			logger.warn("CopyAngulerEnvJsonFile() - UI will use defult: environment.json");
			String info = "CopyAngulerEnvJsonFile() - error = " + e + "\n" + CommonUtil.getStackTrace(e);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_FRONTEND_UI_ENVIRONMENT, info);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				logger.error("closing bufferedReader - error = " + ex);
			}
		}

	}

}
