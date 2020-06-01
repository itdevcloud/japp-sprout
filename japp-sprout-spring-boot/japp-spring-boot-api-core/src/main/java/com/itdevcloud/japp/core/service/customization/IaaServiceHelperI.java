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
package com.itdevcloud.japp.core.service.customization;

import java.util.List;
import java.util.Map;

import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.UserAppSpMap;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface IaaServiceHelperI extends CustomizableComponentI {

	public Map<String, Object> getPiscesJappTokenClaims(IaaUser piscesjappIaaUser);
	
	public IaaUser getIaaUserFromRepositoryByUserId(String userId);
	
	public IaaUser getIaaUserFromRepositoryByLoginId(String loginId, String... args);
	
	public String getIaaUserIdByLoginId(String loginId, String... args);
	
	public List<UserAppSpMap> getAuthenticationSpType(String loginId, String... args);
	
	public List<String> getUpdatedIaaUsers(long lastCheckTimestamp);

	public String getAndSend2ndfactorValue(IaaUser piscesjappIaaUser, String SecondFactorType);

	public String getHashed2ndFactorValueFromRepositoryByUserId(String userId);

	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRoles);
	
	public IaaUser getDummyIaaUserByUserId(String userId);

}
