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

public class ReferenceCode implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String parentId;
	private boolean isFinalized;
	private String typeId;
	private String code;
	private String name;
	private String i18nKey;
	private String description;
	private ReferenceCode parent;

	public ReferenceCode() {
	}

	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		if (isFinalized) {
			return;
		}
		this.id = id;
	}


	public String getParentId() {
		return parentId;
	}


	public void setParentId(String parentId) {
		if (isFinalized) {
			return;
		}
		this.parentId = parentId;
	}


	public String getTypeId() {
		return typeId;
	}


	public void setTypeId(String typeId) {
		if (isFinalized) {
			return;
		}
		this.typeId = typeId;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		if (isFinalized) {
			return;
		}
		this.description = description;
	}


	public boolean isFinalized() {
		return isFinalized;
	}

	public void setFinalized(boolean isFinalized) {
		this.isFinalized = isFinalized;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		if (isFinalized) {
			return;
		}
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (isFinalized) {
			return;
		}
		this.name = name;
	}


	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String i18nKey) {
		if (isFinalized) {
			return;
		}
		this.i18nKey = i18nKey;
	}

	public ReferenceCode getParent() {
		return parent;
	}

	public void setParent(ReferenceCode parent) {
//		if (isFinalized) {
//			return;
//		}
		this.parent = parent;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ReferenceCode other = (ReferenceCode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}
