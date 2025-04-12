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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.ReferenceCodeServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class ReferenceCodeCache extends RefreshableCache {

	private static final Logger logger = LogManager.getLogger(ReferenceCodeCache.class);

	// key = refence PK string
	private static Map<String, ReferenceCode> referenceCodeMap = null;

	@PostConstruct
	public void initService() {
	}

	@Override
	protected String createDisplayString() {
		String str = "referenceCodeMap size = " + referenceCodeMap.size();
		return str;
	}

	@Override
	public synchronized void refreshCache() {
		ReferenceCodeServiceHelperI helper = AppFactory.getComponent(ReferenceCodeServiceHelperI.class);
		try {

			Map<String, ReferenceCode> rcMap = helper.getReferenceCodeMapFromRepository();

			refreshInProcess = true;
			referenceCodeMap = (rcMap == null ? new HashMap<String, ReferenceCode>()
					: Collections.unmodifiableMap(rcMap));
			refreshInProcess = false;

		} catch (Throwable t) {
			String err = "refreshCache() wirh error: " + t;
			logger.error(err, t);
		} finally {
		}
	}

	public List<ReferenceCode> getReferenceCodeList(String codeDomain, String codeType) {
		waitForInit();

		logger.debug("getReferenceCodeList() - codeDomain = " + codeDomain + ", codeType =" + codeType);

		if (referenceCodeMap == null || referenceCodeMap.isEmpty()) {
			return new ArrayList<ReferenceCode>();
		}
		List<ReferenceCode> rcList = new ArrayList<ReferenceCode>();
		if (StringUtil.isEmptyOrNull(codeDomain) && StringUtil.isEmptyOrNull(codeType)) {
			// return all.
			rcList = new ArrayList<ReferenceCode>(referenceCodeMap.values());
		} else if (!StringUtil.isEmptyOrNull(codeDomain) && StringUtil.isEmptyOrNull(codeType)) {
			for (ReferenceCode rc : referenceCodeMap.values()) {
				if (codeDomain.equalsIgnoreCase(rc.getCodeDomain())) {
					rcList.add(rc);
				}
			}
		} else if (StringUtil.isEmptyOrNull(codeDomain) && !StringUtil.isEmptyOrNull(codeType)) {
			for (ReferenceCode rc : referenceCodeMap.values()) {
				if (codeType.equalsIgnoreCase(rc.getCodeType())) {
					rcList.add(rc);
				}
			}
		} else {
			for (ReferenceCode rc : referenceCodeMap.values()) {
				if (codeDomain.equalsIgnoreCase(rc.getCodeDomain()) && codeType.equalsIgnoreCase(rc.getCodeType())) {
					rcList.add(rc);
				}
			}
		}
		return rcList;
	}

	public ReferenceCode getReferenceCode(String codeDomain, String codeType, String codeName) {
		waitForInit();
		logger.debug("getReferenceCode() - codeDomain = " + codeDomain + ", codeType =" + codeType + ", codeName = "
				+ codeName);
		if (StringUtil.isEmptyOrNull(codeDomain) && StringUtil.isEmptyOrNull(codeType)
				&& StringUtil.isEmptyOrNull(codeName)) {
			logger.error(
					"getReferenceCode() - must provide codeDomain, codeType and codeName to get a reference code object!");
			return null;
		}
		for (ReferenceCode rc : referenceCodeMap.values()) {
			if (codeDomain.equalsIgnoreCase(rc.getCodeDomain()) && codeType.equalsIgnoreCase(rc.getCodeType())
					&& codeName.equalsIgnoreCase(rc.getCodeName())) {
				return rc;
			}
		}
		return null;
	}

	public ReferenceCode getReferenceCode(long pk) {
		waitForInit();

		logger.debug("getReferenceCode() - pk = " + pk);

		if (referenceCodeMap == null || referenceCodeMap.isEmpty()) {
			return null;
		}
		ReferenceCode rc = referenceCodeMap.get("" + pk);
		return rc;
	}

	public ReferenceCode getParent(String codeDomain, String codeType, String codeName) {
		logger.debug(
				"getParent() - codeDomain = " + codeDomain + ", codeType =" + codeType + ", codeName = " + codeName);

		ReferenceCode rc = getReferenceCode(codeDomain, codeType, codeName);
		if (rc == null) {
			return null;
		}
		return getReferenceCode(rc.getParentCodeId());
	}

	public ReferenceCode getParent(long pk) {
		logger.debug("getParent() - pk = " + pk);

		ReferenceCode rc = getReferenceCode(pk);
		if (rc == null) {
			return null;
		}
		return getReferenceCode(rc.getParentCodeId());
	}

	public List<ReferenceCode> getChildren(long parentCodeId) {
		waitForInit();
		logger.debug("getChildren() - parentCodeId = " + parentCodeId);

		if (referenceCodeMap == null || referenceCodeMap.isEmpty()) {
			return new ArrayList<ReferenceCode>();
		}
		List<ReferenceCode> rcList = new ArrayList<ReferenceCode>();
		for (ReferenceCode rc : referenceCodeMap.values()) {
			if (rc.getParentCodeId() == parentCodeId) {
				rcList.add(rc);
			}
		}
		return rcList;
	}

	public List<ReferenceCode> getChildren(String codeDomain, String codeType, String codeName) {

		logger.debug(
				"getChildren() - codeDomain = " + codeDomain + ", codeType =" + codeType + ", codeName = " + codeName);

		ReferenceCode rc = getReferenceCode(codeDomain, codeType, codeName);
		if (rc == null) {
			return null;
		}
		return getChildren(rc.getPk());
	}

}
