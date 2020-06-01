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

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.service.customization.ReferenceCodeServiceHelperI;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class DefaultReferenceCodeServiceHelper implements ReferenceCodeServiceHelperI{
	private Logger logger = LogManager.getLogger(DefaultReferenceCodeServiceHelper.class);


	@Override
	public Map<String, List<ReferenceCode>> getReferenceCodeMapFromRepository(){
		return null;
	}

	@Override
	public boolean hasReferenceCodeChanged(long lastUpdatedTS) {
		return false;
	}

	@Override
	public Class<?> getInterfaceClass() {
		return ReferenceCodeServiceHelperI.class;
	}


}
