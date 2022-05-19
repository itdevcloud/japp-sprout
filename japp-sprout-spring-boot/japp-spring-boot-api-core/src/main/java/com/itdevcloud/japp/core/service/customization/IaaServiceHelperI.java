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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ClientAuthInfo;
import com.itdevcloud.japp.core.common.AppConstant.LogicOperation;
import com.itdevcloud.japp.core.common.AppThreadContext;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ProcessorTargetRoleUtil;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaUser;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface IaaServiceHelperI extends CustomizableComponentI {

	static final Logger logger = LogManager.getLogger(IaaServiceHelperI.class);

	default Class<?> getInterfaceClass() {
		return IaaServiceHelperI.class;
	}

	default IaaUserI getIaaUserBySystemUid(String uid) {
		IaaUserI iaaUser = getAnonymousIaaUserByLoginId(uid);
		return iaaUser;
	}
	default IaaUserI getIaaUserByLoginId(String loginId, String authProviderId, String... args) {
		IaaUserI iaaUser = getAnonymousIaaUserByLoginId(loginId);
		return iaaUser;
	}
	default IaaUserI getAnonymousIaaUserByLoginId(String loginId, String... args) {
		IaaUserI iaaUser = new DefaultIaaUser();
		if (StringUtil.isEmptyOrNull(loginId)) {
			iaaUser.setSystemUid("Anonymous");
			iaaUser.setLoginId("Anonymous");
		} else {
			iaaUser.setSystemUid(loginId);
			iaaUser.setLoginId(loginId);
		}
		//password is 12345
		iaaUser.setHashedPassword(
				"NieQminDE4Ggcewn98nKl3Jhgq7Smn3dLlQ1MyLPswq7njpt8qwsIP4jQ2MR1nhWTQyNMFkwV19g4tPQSBhNeQ==");
		iaaUser.setName("Anonymous");
		
		Set<String> agSet = new HashSet<String>();
		agSet.add("ag.dummy.group1");
		agSet.add("ag.dummy.group2");
		iaaUser.setAuthGroups(agSet);

		Set<String> brSet = new HashSet<String>();
		brSet.add("br.dummy.role1");
		brSet.add("br.dummy.role2");
		iaaUser.setBusinessRoles(brSet);

		Set<String> arSet = new HashSet<String>();
		arSet.add("ar.dummy.role1");
		arSet.add("ar.dummy.role2");
		iaaUser.setApplicationRoles(arSet);

		return iaaUser;
		
	}

	default List<String> getUpdatedSystemUids(long lastCheckTimestamp) {
		if (lastCheckTimestamp == -1) {
			return null;
		}
		ArrayList<String> idList = new ArrayList<>();
		return idList;
	}

	default boolean isAccessAllowed(String targetRole) {
		return isAccessAllowed(targetRole, null);
	}

	default boolean isAccessAllowed(String targetRole, String targetNodeId) {
		if (StringUtil.isEmptyOrNull(targetRole)) {
			logger.info("isAccessAllowed() - targetRole can not be null or empty, return false.");
			return false;
		}
		Set<String> targetRoleSet = new HashSet<String>();
		targetRoleSet.add(targetRole);
		return isAccessAllowed(targetRoleSet, LogicOperation.AND, targetNodeId);
	}
	
	default boolean isAccessAllowed(Set<String> targetRoleSet, LogicOperation logicOperation, String targetNodeId, String... args) {
		IaaUserI iaaUser = AppThreadContext.getIaaUser();
		if (iaaUser == null || targetRoleSet == null || targetRoleSet.isEmpty()) {
			logger.debug("isAccessAllowed() - The user, targetRoleSet can not be null or empty, return false.");
			return false;
		}
		Set<String> requiredSet = CommonUtil.changeStringSetCase(targetRoleSet, false);
		if (requiredSet.contains(ProcessorTargetRoleUtil.ROLE_ANY.toLowerCase())) {
			logger.debug("isAccessAllowed() - targetRoleSet contains AnyRole, return true.");
			return true;
		}
		if(logicOperation == null) {
			logicOperation = LogicOperation.AND;
		}
		Set<String> assignedTmpSet = getAssignedRoleSet(targetNodeId);
		Set<String> assignedSet = CommonUtil.changeStringSetCase(assignedTmpSet, false);
		
		if(logicOperation == LogicOperation.AND) {
			Set<String> intersectSet = new HashSet<String>(assignedSet);
			if(intersectSet.containsAll(requiredSet)) {
				return true;
			}
		}else if(logicOperation == LogicOperation.OR) {
			Set<String> intersectSet = new HashSet<String>(assignedSet);
			intersectSet.retainAll(requiredSet);
			if(!intersectSet.isEmpty()) {
				return true;
			}
		}else {
			//must be "not"
			Set<String> intersectSet = new HashSet<String>(assignedSet);
			intersectSet.retainAll(requiredSet);
			if(intersectSet.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	default Set<String> getAssignedRoleSet(String targetNodeId, String... args) {
		IaaUserI iaaUser = AppThreadContext.getIaaUser();
		if (iaaUser == null)  {
			logger.info("getAssignedRoleSet() - Can not get the user from App thread Context, return false.");
			return null;
		}
		Set<String> assignedSet = new HashSet<String>();
		assignedSet.addAll(iaaUser.getAuthGroups());
		assignedSet.addAll(iaaUser.getBusinessRoles());
		assignedSet.addAll(iaaUser.getApplicationRoles());
		return assignedSet;
	}

	default List<ClientAppInfo> getClientAppInfoList() {
		List<ClientAppInfo> appInfoList = new ArrayList<ClientAppInfo>();
		String env = AppUtil.getSpringActiveProfile();
		String path = "client/" + env;
		Map<String, String> fnMap = FileUtil.getFileListingInClassPath(FileUtil.class, path, false, null);
		Set<String> simpleNameSet = fnMap.keySet();
		
		for(String fn: simpleNameSet) {
			logger.info("getClientAppInfoList() load file:" + fn +"......");
			InputStream inputStream = null;
			StringBuilder sb = new StringBuilder();
			try {
				inputStream = ClientAuthInfo.class.getResourceAsStream("/" + path + "/" + fn);
				if (inputStream == null) {
					throw new Exception("can not load " + fn + ", check code!.......");
				}
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				inputStream.close();
				inputStream = null;
				
				Gson gson = new GsonBuilder().serializeNulls().create();
				ClientAppInfo appInfo = null;
				try {
					String jsonStr = sb.toString();
					appInfo = gson.fromJson(jsonStr, ClientAppInfo.class);
					appInfoList.add(appInfo);
				}catch (Throwable t) {
					t.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				sb = new StringBuilder(e.getMessage());
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputStream = null;
				}
			}

		}//end for
		return appInfoList;
	}

}
