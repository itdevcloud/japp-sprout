package com.itdevcloud.japp.core.api.vo;

import com.itdevcloud.japp.se.common.vo.BaseVO;

public class ReferenceCode extends BaseVO {

	private static final long serialVersionUID = 1L;

	private String codeDomain;
	private String codeType;
	private String codeName;
	private String nameI18nKey;
	private String description;
	private long parentCodeId;


	public ReferenceCode() {
		super();
	}


	public ReferenceCode(long pk, String codeDomain, String codeType, String codeName, String nameI18nKey, String description,
			long parentCodeId) {
		super();
		this.pk = pk;
		this.codeDomain = codeDomain;
		this.codeType = codeType;
		this.codeName = codeName;
		this.nameI18nKey = nameI18nKey;
		this.description = description;
		this.parentCodeId = parentCodeId;
	}


	public String getCodeDomain() {
		return codeDomain;
	}


	public void setCodeDomain(String codeDomain) {
		if(isFinalized) {
			return;
		}
		this.codeDomain = codeDomain;
	}


	public String getCodeType() {
		return codeType;
	}


	public void setCodeType(String codeType) {
		if(isFinalized) {
			return;
		}
		this.codeType = codeType;
	}


	public String getCodeName() {
		return codeName;
	}


	public void setCodeName(String codeName) {
		if(isFinalized) {
			return;
		}
		this.codeName = codeName;
	}


	public String getNameI18nKey() {
		return nameI18nKey;
	}


	public void setNameI18nKey(String nameI18nKey) {
		if(isFinalized) {
			return;
		}
		this.nameI18nKey = nameI18nKey;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		if(isFinalized) {
			return;
		}
		this.description = description;
	}


	public long getParentCodeId() {
		return parentCodeId;
	}


	public void setParentCodeId(long parentCodeId) {
		if(isFinalized) {
			return;
		}
		this.parentCodeId = parentCodeId;
	}

	@Override
	public String getUID() {
		return this.codeDomain + "-" + this.codeType + "-" + this.codeName;
	}


	@Override
	public String toString() {
		return "IaaReferenceCode [codeDomain=" + codeDomain + ", codeType=" + codeType + ", codeName=" + codeName
				+ ", nameI18nKey=" + nameI18nKey + ", description=" + description + ", parentCodeId=" + parentCodeId
				+ ", Base=" + super.toString() + "]";
	}

}
