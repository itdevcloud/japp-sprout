package com.itdevcloud.japp.core.api.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * Abstract super class for all Value Object for Service tier
 * 
 */
public abstract class BaseVO implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	protected boolean isFinalized;
	
	protected long pk;
	protected Date effectiveDate;
	protected Date expiryDate;
	protected String createdBy;
	protected Date createdDate;
	protected String updatedBy;
	protected Date updatedDate;
	protected Date endDate;
	protected String endDecsription;
	private List<AttributeVO> attributes;
	
	public BaseVO() {
	}

	public BaseVO(long pk) {
		this.pk = pk;
	}
	
	public void finalize() {
		this.isFinalized = true;
	}

	public boolean isFinalized() {
		return isFinalized;
	}

	public long getPk() {
		return pk;
	}

	public void setPk(long pk) {
		if(isFinalized) {
			return;
		}
		this.pk = pk;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		if(isFinalized) {
			return;
		}
		this.effectiveDate = effectiveDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		if(isFinalized) {
			return;
		}
		this.expiryDate = expiryDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		if(isFinalized) {
			return;
		}
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		if(isFinalized) {
			return;
		}
		this.createdDate = createdDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		if(isFinalized) {
			return;
		}
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		if(isFinalized) {
			return;
		}
		this.updatedDate = updatedDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		if(isFinalized) {
			return;
		}
		this.endDate = endDate;
	}

	public String getEndDecsription() {
		return endDecsription;
	}

	public void setEndDecsription(String endDecsription) {
		if(isFinalized) {
			return;
		}
		this.endDecsription = endDecsription;
	}
	public List<AttributeVO> getAttributes() {
		if(this.attributes == null) {
			this.attributes = new ArrayList<AttributeVO>();
		}
		return attributes;
	}

	public void setAttributes(List<AttributeVO> attributes) {
		if(isFinalized) {
			return;
		}
		this.attributes = attributes;
		if (this.attributes != null) {
			Collections.sort(this.attributes);
		}
	}
	
	public void addAttribute(AttributeVO attribute) {
		if(isFinalized || attribute == null) {
			return;
		}
		getAttributes().add(attribute);
		if (this.attributes != null) {
			Collections.sort(this.attributes);
		}

	}

	public abstract String getUID() ;
	
	@Override
	public String toString() {
		return "IaaVO [isFinalized=" + isFinalized + ", pk=" + pk + ", effectiveDate=" + DateUtils.dateToString(effectiveDate) + ", expiryDate="
				+ DateUtils.dateToString(expiryDate) + ", createdBy=" + createdBy + ", createdDate=" + DateUtils.dateToString(createdDate) + ", updatedBy="
				+ updatedBy + ", updatedDate=" + DateUtils.dateToString(updatedDate) + ", endDate=" + DateUtils.dateToString(endDate) + ", endDecsription="
				+ endDecsription + ", \nattributes = " + CommonUtil.listToString(attributes, 0) +  " ]";
	}
	
	@Override
	public int compareTo(Object obj) {
		if (this == obj) {
			return 0;
		}
		if (obj == null) {
			return 1;
		}
		if (getClass() != obj.getClass()) {
			return StringUtil.compareTo(this.getClass().getSimpleName(), obj.getClass().getSimpleName());
		}
		BaseVO other = (BaseVO) obj;
		int result = StringUtil.compareTo(this.getUID(), other.getUID());
		return result;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUID() == null) ? 0 : getUID().hashCode());
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
		BaseVO other = (BaseVO) obj;
		if (getUID() == null) {
			if (other.getUID() != null)
				return false;
		} else if (!getUID().equalsIgnoreCase(other.getUID()))
			return false;
		return true;
	}
	
	
}
