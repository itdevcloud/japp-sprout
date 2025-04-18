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

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.IaaAppVO;
import com.itdevcloud.japp.core.api.vo.MfaVO;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.UserAppSpMap;

import jakarta.servlet.http.HttpServletRequest;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface IaaServiceHelperI extends CustomizableComponentI {

	public Map<String, Object> getJappTokenClaims(AppIaaUser iaaUser);
	
	public AppIaaUser getIaaUserFromRepositoryByUserIaaUID(String userIaaUID);
	public AppIaaUser getIaaUserFromRepositoryByLoginId(String loginId, String authnProvider);
	public boolean isAccessAllowed(String userId, String targetNodeId, String targetRoles);
	public AppIaaUser getDummyIaaUserByUserId(String userId);
	public List<MfaVO> getMfaInfoFromSessionRepository(String userSessionId);
	public void addOrUpdateMfaInfoToSessionRepository(String userSessionId, MfaVO mfaVO);
	public String getUserIaaUIDFromSessionRepository(String userSessionId);
	public void setUserIaaUIDToSessionRepository(String userIaaUID);
	public List<IaaAppVO> getIaaAppInfo();
	public String getAuthnProviderURL(HttpServletRequest httpRequest, IaaAppVO iaaAppVO, String stateString);

}
