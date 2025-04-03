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
package com.itdevcloud.japp.core.service.log;

import java.io.File;
import java.io.FilenameFilter;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.ConfigFactory;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class LogFileNameFilter implements FilenameFilter {


	@PostConstruct
	public void init() {
	}

	@Override
	public boolean accept(File directory, String fileName) {
		return fileName.startsWith(ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_LOG_FILE_PREFIX)) && fileName.endsWith(".log");
	}

}
