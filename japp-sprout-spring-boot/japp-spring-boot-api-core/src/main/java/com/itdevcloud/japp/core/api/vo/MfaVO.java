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
public abstract class MfaVO  {

	public static final String MFA_TYPE_TOTP = "TOTP";
	public static final String MFA_TYPE_OTP = "OTP";
	
	private int retryCount;
	private boolean verified;
	private Date expiryDate;
	private Date lastRetryDate;

	public MfaVO() {
	}

	public abstract String getType();

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Date getLastRetryDate() {
		return lastRetryDate;
	}

	public void setLastRetryDate(Date lastRetryDate) {
		this.lastRetryDate = lastRetryDate;
	}

	@Override
	public String toString() {
		return "MfaVO [type=" + getType() + ", verified=" + verified + ", expiryDate=" + expiryDate + ", retryCount="
				+ retryCount + ", lastRetryDate=" + lastRetryDate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
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
		MfaVO other = (MfaVO) obj;
		if (getType() == null) {
			if (other.getType() != null)
				return false;
		} else if (!getType().equals(other.getType()))
			return false;
		return true;
	}
	



}
