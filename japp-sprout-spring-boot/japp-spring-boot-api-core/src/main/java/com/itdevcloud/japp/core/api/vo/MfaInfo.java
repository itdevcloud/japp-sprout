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

import java.util.ArrayList;
import java.util.List;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class MfaInfo {

	private MfaTOTP mfaTotp;;
	private MfaOTP mfaOtp;;

	public MfaInfo() {
	}

	public List<MfaVO> getMfaVOList() {
		List<MfaVO> mfaVOList = new ArrayList<MfaVO>();
		if(mfaTotp != null) {
			mfaVOList.add(mfaTotp);
		}
		if(mfaOtp != null) {
			mfaVOList.add(mfaOtp);
		}
		return mfaVOList;
	}

	public void addOrUpdateMfaVO(MfaVO mfaVO) {
		if (mfaVO == null) {
			return;
		}
		if (mfaVO instanceof MfaTOTP) {
			this.mfaTotp = (MfaTOTP) mfaVO;
			return;
		}else if (mfaVO instanceof MfaOTP) {
			this.mfaOtp = (MfaOTP)mfaVO;
			return;
		}
		return;
	}

	public MfaVO getMfaVO(String type) {
		if (StringUtil.isEmptyOrNull(type)) {
			return null;
		}
		if (type.equalsIgnoreCase(mfaTotp.getType())) {
			return mfaTotp;
		}else if (type.equalsIgnoreCase(mfaOtp.getType())) {
			return mfaOtp;
		}else {
			return null;
		}
	}

	
	public MfaTOTP getMfaTotp() {
		return mfaTotp;
	}


	public MfaOTP getMfaOtp() {
		return mfaOtp;
	}


	@Override
	public String toString() {
		return "MfaInfo [mfaTotp = " + mfaTotp + ", mfaOtp = " + mfaOtp ;
	}

	
}
