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

import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class MfaInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private long verificationId;
	private String type;
	private String configuration;
	private int retryCount = 0;
	private boolean verified = false;

	public String getType() {
		if(StringUtil.isEmptyOrNull(type)) {
			type = AppConstant.IAA_2NDFACTOR_TYPE_NONE;
		}
		return type;
	}
	public void setType(String type) {
		if(StringUtil.isEmptyOrNull(type)) {
			type = AppConstant.IAA_2NDFACTOR_TYPE_NONE;
		}
		this.type = type;
	}
	
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	public int getRetryCount() {
		if(retryCount < 0) {
			retryCount = 0;
		}
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		if(retryCount < 0) {
			retryCount = 0;
		}
		this.retryCount = retryCount;
	}
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	public long getVerificationId() {
		return verificationId;
	}
	public void setVerificationId(long verificationId) {
		this.verificationId = verificationId;
	}



}
