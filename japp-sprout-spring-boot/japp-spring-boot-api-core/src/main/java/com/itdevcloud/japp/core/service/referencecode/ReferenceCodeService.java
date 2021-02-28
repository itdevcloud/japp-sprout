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
package com.itdevcloud.japp.core.service.referencecode;
/**
*
* @author Marvin Sun
* @since 1.0.0
*/
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;

@Component
public class ReferenceCodeService implements AppFactoryComponentI {
	
	private Logger logger = LogManager.getLogger(ReferenceCodeService.class);

	public ArrayList<ReferenceCode> getReferenceCodeListByEntityType(String type) {
		return AppComponents.referenceCodeCache.getReferenceCodeListByEntityType(type);
	}

	public ReferenceCode getReferenceCodeByCode(String type, String code) {
		return AppComponents.referenceCodeCache.getReferenceCodeByCode(type, code);
	}

	public ReferenceCode getReferenceCodeById(String id) {
		return AppComponents.referenceCodeCache.getReferenceCodeById(id);
	}

	public List<ReferenceCode> getChildrenReferenceCodeListByParentId(String parentId) {
		return AppComponents.referenceCodeCache.getChildrenReferenceCodeListByParentId(parentId);
	}
	public List<ReferenceCode> getChildrenReferenceCodeListByParentCode(String parentType, String parentCode) {
		return AppComponents.referenceCodeCache.getChildrenReferenceCodeListByParentCode(parentType, parentCode);
	}


}
