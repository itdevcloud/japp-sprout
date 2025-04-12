package com.itdevcloud.japp.se.common.vo;

public class AttributeVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	private long mainEntityId;
	private long domainId;
	private String domainCode;
	private long typeId;
	private String typeCode;
	private int sequence;
	private String value;
	private int displayOrder;
	private String description;
	private boolean requireUnique;

	
	public AttributeVO() {
		super();
	}



	public AttributeVO(long mainEntityId, long pk, long domainId, String domainCode, long typeId, String typeCode, int sequence, String value, int displayOrder, String description, boolean requireUnique) {
		super();
		this.mainEntityId = mainEntityId;
		this.pk= pk;
		this.domainId = domainId;
		this.domainCode = domainCode;
		this.typeId = typeId;
		this.typeCode = typeCode;
		this.sequence = sequence;
		this.value = value;
		this.displayOrder = displayOrder;
		this.description = description;
		this.requireUnique = requireUnique;
	}



	public long getMainEntityId() {
		return mainEntityId;
	}

	public void setMainEntityId(long mainEntityId) {
		if(isFinalized) {
			return;
		}
		this.mainEntityId = mainEntityId;
	}


	public long getDomainId() {
		return domainId;
	}



	public void setDomainId(long domainId) {
		if(isFinalized) {
			return;
		}
		this.domainId = domainId;
	}



	public String getDomainCode() {
		return domainCode;
	}



	public void setDomainCode(String domainCode) {
		if(isFinalized) {
			return;
		}
		this.domainCode = domainCode;
	}



	public long getTypeId() {
		return typeId;
	}



	public void setTypeId(long typeId) {
		if(isFinalized) {
			return;
		}
		this.typeId = typeId;
	}



	public String getTypeCode() {
		return typeCode;
	}



	public void setTypeCode(String typeCode) {
		if(isFinalized) {
			return;
		}
		this.typeCode = typeCode;
	}



	public int getSequence() {
		return sequence;
	}



	public void setSequence(int sequence) {
		if(isFinalized) {
			return;
		}
		this.sequence = sequence;
	}



	public String getValue() {
		return value;
	}



	public void setValue(String value) {
		if(isFinalized) {
			return;
		}
		this.value = value;
	}



	public int getDisplayOrder() {
		return displayOrder;
	}



	public void setDisplayOrder(int displayOrder) {
		if(isFinalized) {
			return;
		}
		this.displayOrder = displayOrder;
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

	public boolean isRequireUnique() {
		return requireUnique;
	}



	public void setRequireUnique(boolean requireUnique) {
		if(isFinalized) {
			return;
		}
		this.requireUnique = requireUnique;
	}



	public String getUID() {
		return "" + this.mainEntityId + "-" + this.typeId + "-" + this.sequence;
	}


	@Override
	public String toString() {
		return "AttributeVO [mainEntityId=" + mainEntityId + ", domainId=" + domainId + ", domainCode=" + domainCode + ", typeId=" + typeId + ", typeCode=" + typeCode + ", sequence=" + sequence + ", value="
				+ value + ", requireUnique=" + requireUnique + ", displayOrder=" + displayOrder + ", description=" + description + ", Base="
				+ super.toString() + "]";
	}


}
