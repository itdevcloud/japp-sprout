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

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.PkiVO;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AuthzProviderKeyCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(AuthzProviderKeyCache.class);

	private static Map<String, List<PkiVO>> authzProviderKeyMap;

	@PostConstruct
	private void initService() {
	}

//	@Override
//	public void refreshCache() {
//		if (lastUpdatedTS == -1) {
//			initCache();
//		}else {
//			logger.info("PkiKeyCache.refreshCache() - only daily referesh is requried, do nothing...");
//		}
//	}
	
	@Override
	protected String createDisplayString() {
		String str = CommonUtil.mapToString(authzProviderKeyMap, 0);
		return str;
	}

	@Override
	protected void refreshCache() {
		try {
			Map<String, List<PkiVO>> tmpMap = new HashMap<String, List<PkiVO>>();
			List<PkiVO> tmpList = new ArrayList<PkiVO>();
			
			PkiVO tmpVO = new PkiVO();
			tmpVO.setPrivateKey(AppComponents.pkiService.getJappPrivateKey());
			tmpVO.setPrivateKey(AppComponents.pkiService.getJappPublicKey());
			tmpVO.setCertificate(AppComponents.pkiService.getJappCertificate());
			tmpList.add(tmpVO);
			
			tmpMap.put("LOCAL", tmpList);
			
			authzProviderKeyMap = Collections.unmodifiableMap(tmpMap);


		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public List<PkiVO> getAuthzProviderKeys(String providerName) {
		if(StringUtil.isEmptyOrNull(providerName)) {
			return null;
		}
		waitForInit();
		return authzProviderKeyMap.get(providerName);
	}


}
