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
package com.itdevcloud.japp.core.api.bean;

import org.springframework.stereotype.Component;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class GetChildrenReferenceCodeRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private long id;
	private String codeDomain;
	private String codeType;
	private String codeName;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCodeDomain() {
		return codeDomain;
	}
	public void setCodeDomain(String codeDomain) {
		this.codeDomain = codeDomain;
	}
	public String getCodeType() {
		return codeType;
	}
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
	public String getCodeName() {
		return codeName;
	}
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
	
	@Override
	public String toString() {
		return "GetChildrenReferenceCodeRequest [id=" + id + ", codeDomain=" + codeDomain + ", codeType=" + codeType
				+ ", codeName=" + codeName + ", Base=" + super.toString() + "]";
	}
	
	

}
