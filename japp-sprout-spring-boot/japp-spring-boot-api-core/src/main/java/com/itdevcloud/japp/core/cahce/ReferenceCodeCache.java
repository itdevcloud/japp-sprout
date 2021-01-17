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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.service.customization.ReferenceCodeServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ReferenceCodeCache extends RefreshableCache{


	private static final Logger logger = LogManager.getLogger(ReferenceCodeCache.class);
	// key = reference identifier,
	private static Map<String, List<ReferenceCode>> referenceCodeMap = null;


	@PostConstruct
	public void init() {
	}

	@Override
	public void refreshCache() {
		ReferenceCodeServiceHelperI helper = AppFactory.getComponent(ReferenceCodeServiceHelperI.class);	
		if (lastUpdatedTS == -1
				|| helper.hasReferenceCodeChanged(lastUpdatedTS)) {
			logger.info("ReferenceCodeCache - force to init ReferenceCodeCache .........");
			// force to init()
			lastUpdatedTS = -1;
			initCache();
		}else {
			logger.info("ReferenceCodeCache.refreshCache() - only daily referesh is requried, do nothing...");
		}


	}

	@Override
	public synchronized void initCache() {
		ReferenceCodeServiceHelperI helper = AppFactory.getComponent(ReferenceCodeServiceHelperI.class);	
		try {
			long startTS = System.currentTimeMillis();
			if (lastUpdatedTS == -1 || ((startTS - lastUpdatedTS) >= 1000 * 60 * 5)) {
				logger.debug("ReferenceCodeCache.init()......begin...........");

				Map<String, List<ReferenceCode>> codeMap = helper.getReferenceCodeMapFromRepository();
				codeMap = setParent(codeMap);

				initInProcess = true;
				referenceCodeMap = (codeMap==null?new HashMap<String, List<ReferenceCode>>():codeMap);
				initInProcess = false;

				Date end = new Date();
				long endTS = end.getTime();
				lastUpdatedTS = endTS;

				String str = "ReferenceCodeCache.init() end. total time = " + (endTS - startTS) + " millis. Result:"
						+ "\nreferenceCodeMap size = " + referenceCodeMap.size() + "\n";
				//						+ "\n Key Set = " + referenceCodeMap.keySet();

				logger.info(str);
				AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_REFERENCE_CODE_CACHE, str);

			}
		} catch (Exception e) {
			String errStr = AppUtil.getStackTrace(e);
			logger.error(errStr);
			AppComponents.startupService.addNotificationInfo(AppConstant.STARTUP_NOTIFY_KEY_REFERENCE_CODE_CACHE, errStr);
		} finally {
			initInProcess = false;
		}
	}

	private Map<String, List<ReferenceCode>> setParent(Map<String, List<ReferenceCode>> codeMap) {
		//note: should be only one level
		if (codeMap == null || codeMap.isEmpty()) {
			return null;
		}
		ArrayList<ReferenceCode> fullList = new ArrayList<ReferenceCode>();
		ArrayList<ReferenceCode> tmpList = null;

		for (String type : codeMap.keySet()) {
			fullList.addAll(codeMap.get(type));
		}
		if (fullList == null || fullList.isEmpty()) {
			return null;
		}

		for (String type : codeMap.keySet()) {
			tmpList = new ArrayList<ReferenceCode>();
			tmpList.addAll(codeMap.get(type));
			for(ReferenceCode rc: tmpList) {
				if(rc != null && rc.getParentId() != null) {
					for(ReferenceCode tmprc: fullList) {
						String parentId = tmprc.getId();
						if(parentId != null && parentId.equalsIgnoreCase(rc.getId())) {
							rc.setParent(tmprc);
						}
					}
				}
			}
			codeMap.put(type, tmpList);
		}
		return codeMap;
	}

	public ArrayList<ReferenceCode> getReferenceCodeListByEntityType(String type) {
		waitForInit();
		if (StringUtil.isEmptyOrNull(type)) {
			// return all.
			ArrayList<ReferenceCode> crsReferenceCodeList = new ArrayList<ReferenceCode>();
			for (String typeStr : referenceCodeMap.keySet()) {
				crsReferenceCodeList.addAll(referenceCodeMap.get(typeStr));
			}
			return crsReferenceCodeList;
		}
		List<ReferenceCode> codeList = referenceCodeMap.get(type);

		if (codeList == null || codeList.isEmpty()) {
			return null;
		}
		return new ArrayList<ReferenceCode>(codeList);
	}

	public ReferenceCode getReferenceCodeByCode(String type, String code) {
		waitForInit();
		logger.info("Type=" + type + " Code=" + code);
		if (StringUtils.isEmpty(type) || referenceCodeMap == null || StringUtils.isEmpty(code)) {
			return null;
		}
		List<ReferenceCode> codeList = referenceCodeMap.get(type);
		if (codeList == null || codeList.isEmpty()) {
			return null;
		}
		for (ReferenceCode rc : codeList) {
			if (code.equalsIgnoreCase(rc.getCode())) {
				return rc;
			}
		}
		return null;
	}

	public ReferenceCode getReferenceCodeById(String id) {
		waitForInit();
		if (referenceCodeMap == null) {
			return null;
		}
		for (String type : referenceCodeMap.keySet()) {
			List<ReferenceCode> codeList = referenceCodeMap.get(type);
			if (codeList == null || codeList.isEmpty()) {
				continue;
			}
			for (ReferenceCode rc : codeList) {
				if (id == rc.getId()) {
					return rc;
				}
			}
		}
		return null;
	}

	public List<ReferenceCode> getChildrenReferenceCodeListByParentId(String parentId) {
		waitForInit();
		if (referenceCodeMap == null || parentId == null) {
			return null;
		}
		List<ReferenceCode> tmpList = new ArrayList<ReferenceCode>();
		for (String type : referenceCodeMap.keySet()) {
			List<ReferenceCode> codeList = referenceCodeMap.get(type);
			if (codeList == null || codeList.isEmpty()) {
				continue;
			}
			for (ReferenceCode rc : codeList) {
				if (parentId.equalsIgnoreCase(rc.getParentId())) {
					tmpList.add(rc);
				}
			}
		}

		return (tmpList.isEmpty()?null:tmpList);
	}

	public List<ReferenceCode> getChildrenReferenceCodeListByParentCode(String parentType, String parentCode) {
		waitForInit();
		if (referenceCodeMap == null || StringUtil.isEmptyOrNull(parentType)) {
			return null;
		}
		List<ReferenceCode> tmpList = new ArrayList<ReferenceCode>();
		for (String type : referenceCodeMap.keySet()) {
			List<ReferenceCode> codeList = referenceCodeMap.get(type);
			if ( codeList == null || codeList.isEmpty()) {
				continue;
			}
			for (ReferenceCode rc : codeList) {
				ReferenceCode parent = rc.getParent();
				if (parent != null && parentType.equalsIgnoreCase(parent.getTypeId())) {
					if(StringUtil.isEmptyOrNull(parentCode) || parentCode.equalsIgnoreCase(parent.getCode())) {
						tmpList.add(rc);
					}
					continue;
				}else {
					continue;
				}
			}
		}

		return (tmpList.isEmpty()?null:tmpList);
	}




}
