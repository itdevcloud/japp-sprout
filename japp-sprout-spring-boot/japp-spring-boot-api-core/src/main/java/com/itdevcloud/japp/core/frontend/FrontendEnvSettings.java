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
package com.itdevcloud.japp.core.frontend;

/**
*
* @author Marvin Sun
* @since 1.0.0
*/
import com.itdevcloud.japp.se.common.util.StringUtil;


public class FrontendEnvSettings {
	private String version;
	private String build;
	private String apiURL;
	private String loginURL;
	private String logoutURL;
	private String envJsonUrl;

	public String getVersion() {
		if(StringUtil.isEmptyOrNull(version)) {
			return "n/a";
		}
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getBuild() {
		if(StringUtil.isEmptyOrNull(build)) {
			return "n/a";
		}
		return build;
	}
	
	public void setBuild(String build) {
		this.build = build;
	}
	
	public String getApiURL() {
		return apiURL;
	}
	
	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}
	

	public String getLoginURL() {
		return loginURL;
	}
	
	public void setLoginURL(String loginURL) {
		this.loginURL = loginURL;
	}
	
	public String getLogoutURL() {
		return logoutURL;
	}
	
	public void setLogoutURL(String logoutURL) {
		this.logoutURL = logoutURL;
	}
	
	
	public String getEnvJsonUrl() {
		return envJsonUrl;
	}
	
	public void setEnvJsonUrl(String envJsonUrl) {
		this.envJsonUrl = envJsonUrl;
	}

	

}