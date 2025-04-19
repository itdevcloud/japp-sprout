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
package com.itdevcloud.japp.core.cahce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.AuthProviderVO;
import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.KeySecretVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AuthProviderCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(AuthProviderCache.class);

	private static Map<String, AuthProviderVO> authProviderMap;

	@PostConstruct
	private void initService() {
	}

	
	@Override
	protected String createDisplayString() {
		String str = CommonUtil.mapToString(authProviderMap, 0);
		return str;
	}

	@Override
	protected void refreshCache() {
		try {
			Map<String, AuthProviderVO> tmpMap = new HashMap<String, AuthProviderVO>();
			List <AuthProviderVO> tmpList = AppComponents.iaaService.getAuthProviderInfo();
			
			//logger.info("...........tmpList......." + CommonUtil.listToString(tmpList));
			
			if(tmpList == null ) {
				logger.warn("getAuthProviderInfo List is null or empty, will not update exisitng cache value.......");
				tmpList = new ArrayList <AuthProviderVO>();
				//continue to add EntraId and My_app
			}
		
			for (AuthProviderVO vo: tmpList) {
				tmpMap.put(vo.getName().toUpperCase(), vo);
			}
			//make sure EntraID and My_App is in the map
			AuthProviderVO vo = null;
			if(tmpMap.get(AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID) == null) {
				vo = new AuthProviderVO();
				vo.setName(AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID);
				tmpMap.put(vo.getName().toUpperCase(), vo);
			}
			if(tmpMap.get(AppConstant.AUTH_PROVIDER_NAME_MY_APP) == null) {
				vo = new AuthProviderVO();
				vo.setName(AppConstant.AUTH_PROVIDER_NAME_MY_APP);
				tmpMap.put(vo.getName().toUpperCase(), vo);
			}
			authProviderMap = Collections.unmodifiableMap(tmpMap);


		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public AuthProviderVO getAuthProviderInfo(String name) {
		if(StringUtil.isEmptyOrNull(name)) {
			return null;
		}
		waitForInit();
		return authProviderMap.get(name.toUpperCase());
	}


}
