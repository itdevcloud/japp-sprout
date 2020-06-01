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
package com.itdevcloud.japp.core.api.vo;

import java.util.Date;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

public class ServerInstanceInfo {

	private String activeProfileName;
	private String applicationId;
	private String localHostName;
	private String localIP;
	private Date startupDate;

	public String getActiveProfileName() {
		return activeProfileName;
	}
	public void setActiveProfileName(String activeProfileName) {
		this.activeProfileName = activeProfileName;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getLocalHostName() {
		return localHostName;
	}
	public void setLocalHostName(String localHostName) {
		this.localHostName = localHostName;
	}
	public String getLocalIP() {
		return localIP;
	}
	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}
	public Date getStartupDate() {
		return startupDate;
	}
	public void setStartupDate(Date startupDateTime) {
		this.startupDate = startupDateTime;
	}
	@Override
	public String toString() {
		return "ServerInstanceInfo [activeProfileName=" + activeProfileName + ", applicationId=" + applicationId
				+ ", localHostName=" + localHostName + ", localIP=" + localIP + ", startupDateTime=" + startupDate
				+ "]";
	}




}
