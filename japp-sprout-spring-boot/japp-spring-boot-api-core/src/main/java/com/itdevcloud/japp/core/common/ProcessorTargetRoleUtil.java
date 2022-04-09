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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.se.common.service.PropertyManager;
import com.itdevcloud.japp.se.common.util.StringUtil;


public class ProcessorTargetRoleUtil implements Serializable {

	private static final Logger logger = LogManager.getLogger(ProcessorTargetRoleUtil.class);

	public static final String ROLE_ANY = "AnyRole";

	private static final long serialVersionUID = 1L;
	private static Map<String, String> targetRoleMap;
	private static final String TARGET_ROLE_PREFIX = "processor.name.";
	
	public static void init() {
		logger.info("ProcessorTargetRoleUtil.init() - begin........");
		
		targetRoleMap = new HashMap<String, String>();
		PropertyManager pm = new PropertyManager("/target-role.properties");
		Properties properties = pm.getProperties(TARGET_ROLE_PREFIX);
		if(properties == null) {
			return;
		}
		Set <?> keySet = properties.keySet();
		for(Object key: keySet) {
			String processorName = (String)key;
			String targetRole = properties.getProperty(processorName);
			targetRole = (targetRole==null?"":targetRole.trim());
			processorName = processorName.trim().toUpperCase();
			if(StringUtil.isEmptyOrNull(targetRole)) {
				targetRole = "";
			}else if(targetRole.equals("*")) {
				targetRole = ROLE_ANY;
			}else {
				targetRole = targetRole.trim();
			}
			targetRoleMap.put(processorName.toUpperCase(), targetRole);
		}
		logger.info("ProcessorTargetRoleUtil.init() - end........");
	}


	public static String getTargetRole(String processorSimpleName) {
		if(targetRoleMap == null || targetRoleMap.isEmpty() || StringUtil.isEmptyOrNull(processorSimpleName)) {
			return null;
		}
		String role = targetRoleMap.get(processorSimpleName.trim().toUpperCase());
		return role;
	}
	public static Set<String> getProcessorNameSet() {
		if(targetRoleMap == null || targetRoleMap.isEmpty() ) {
			return new HashSet<String>();
		}
		return targetRoleMap.keySet();
	}


}
