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
import java.util.Set;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface IaaUserI  extends CustomizableComponentI {
	
	public static enum MFA_STATUS {NOT_REQUIRED, ENABLED, VERIFIED};

	public String getSystemUid();
	public void setSystemUid(String systemUid);
	
	public String getLoginId() ;
	public void setLoginId(String loginId) ;
	
	public String getIdentityProvider() ;
	public void setIdentityProvider(String idp);
	
	public String getUserType() ;
	public void setUserType(String userType) ;
	
	public String getApplicationId() ;
	public void setApplicationId(String applicationId) ;
	
	public String getHashedPassword() ;
	public void setHashedPassword(String hashedPassword) ;
	
	public String getHashAlgorithm() ;
	public void setHashAlgorithm(String hashAlgorithm) ;
	
	public String getName() ;
	public void setName(String name) ;
	
	public String getEmail() ;
	public void setEmail(String email) ;
	
	public String getPhone() ;
	public void setPhone(String phone) ;
	
	public String getMfaStatus() ;
	public void setMfaStatus(String mfaStatus) ;
	
	public String getTotpSecret() ;
	public void setTotpSecret(String totpSecret) ;
	
	public List<String> getCidrWhiteList() ;
	public void setCidrWhiteList(List<String> cidrWhiteList) ;
	
	public Set<String> getApplicationRoles() ;
	public void setApplicationRoles(Set<String> applicationRoles) ;
	
	public Set<String> getBusinessRoles() ;
	public void setBusinessRoles(Set<String> businessRoles);
	
	public Set<String> getAuthGroups() ;
	public void setAuthGroups(Set<String> authGroups) ;
	

}
