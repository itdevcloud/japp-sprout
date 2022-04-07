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

import java.io.Serializable;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.util.ArrayList;
import java.util.List;

import com.itdevcloud.japp.se.common.util.StringUtil;

public class ClientAppInfo implements Serializable{

	@Override
	public String toString() {
		return "ClientAppInfo [appId=" + appId + ", appCode=" + appCode + ", name=" + name + ", appSiteInfoList="
				+ appSiteInfoList + "]";
	}

	private static final long serialVersionUID = 1L;
	
	private Long appId;
	private String appCode;
	
	private String name;
	private List<AppSiteInfo> appSiteInfoList;
	
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = (appId==null?null:appId);
	}
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String code) {
		this.appCode = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void addAppSite(AppSiteInfo appSiteInfo) {
		if(this.appSiteInfoList == null) {
			this.appSiteInfoList = new ArrayList<AppSiteInfo>();
		}
		this.appSiteInfoList.add(appSiteInfo);
	}
	public AppSiteInfo getAppSiteInfo(String siteCode) {
		if(this.appSiteInfoList == null || StringUtil.isEmptyOrNull(siteCode)) {
			return null;
		}
		for(AppSiteInfo info: this.appSiteInfoList) {
			if(siteCode.equalsIgnoreCase(info.getSiteCode()) ) {
				return info;
			}
		}
		return null;
	}

	public List<AppSiteInfo> getAppSiteInfoList() {
		if(this.appSiteInfoList == null) {
			this.appSiteInfoList = new ArrayList<AppSiteInfo>();
		}
		return appSiteInfoList;
	}
	public void setAppSiteList(List<AppSiteInfo> appSiteList) {
		this.appSiteInfoList = appSiteList;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appCode == null) ? 0 : appCode.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientAppInfo other = (ClientAppInfo) obj;
		if (appCode == null) {
			if (other.appCode != null)
				return false;
		} else if (!appCode.equalsIgnoreCase(other.appCode)) {
			return false;
		}
		return true;
	}
	




}
