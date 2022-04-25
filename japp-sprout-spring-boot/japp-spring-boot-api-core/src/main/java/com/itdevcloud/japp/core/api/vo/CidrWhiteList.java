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
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.util.List;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class CidrWhiteList implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<String> cidrWhiteList;
	
	public void addCidr(String cidr) {
		if(this.cidrWhiteList == null) {
			this.cidrWhiteList = new ArrayList<String>();
		}
		if(StringUtil.isEmptyOrNull(cidr)) {
			return;
		}
		this.cidrWhiteList.add(cidr);
		Collections.sort(this.cidrWhiteList);
		return;
	}
	public List<String> getCidrWhiteList() {
		if(this.cidrWhiteList == null) {
			this.cidrWhiteList = new ArrayList<String>();
		}
		List<String> list = new ArrayList<String>();
		list.addAll(this.cidrWhiteList);
		Collections.sort(list);
		return list;
	}

	public void setCidrWhiteList(List<String> cidrList) {
		this.cidrWhiteList =  new ArrayList<String>();
		if(cidrList != null) {
			this.cidrWhiteList.addAll(cidrList);
			Collections.sort(this.cidrWhiteList);
		}
	}

	@Override
	public String toString() {
		return "ClientAuthInfo [cidrWhiteList=" + cidrWhiteList + "]";
	}

	
}
