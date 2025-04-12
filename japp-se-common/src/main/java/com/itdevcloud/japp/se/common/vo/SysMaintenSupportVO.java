package com.itdevcloud.japp.se.common.vo;

public class SysMaintenSupportVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	private String smsId;
	private String smsDomain;
	private String smsKey;
	private int smsKeySequence;
	private String smsValue;

	
	public SysMaintenSupportVO() {
		super();
	}

	public String getUID() {
		return "" + this.smsId + "-" + this.smsDomain + "-" + this.smsKey + "-" + this.smsKeySequence;
	}

	public String getSmsId() {
		return smsId;
	}

	public void setSmsId(String smsId) {
		this.smsId = smsId;
	}

	public String getSmsDomain() {
		return smsDomain;
	}

	public void setSmsDomain(String smsDomain) {
		this.smsDomain = smsDomain;
	}

	public String getSmsKey() {
		return smsKey;
	}

	public void setSmsKey(String smsKey) {
		this.smsKey = smsKey;
	}

	public int getSmsKeySequence() {
		return smsKeySequence;
	}

	public void setSmsKeySequence(int smsKeySequence) {
		this.smsKeySequence = smsKeySequence;
	}

	public String getSmsValue() {
		return smsValue;
	}

	public void setSmsValue(String smsValue) {
		this.smsValue = smsValue;
	}

	@Override
	public String toString() {
		return "SysMaintenanceSupportVO [smsId=" + smsId + ", smsDomain=" + smsDomain + ", smsKey=" + smsKey
				+ ", smsKeySequence=" + smsKeySequence + ", smsValue=" + smsValue + ", Base=" + super.toString()
				+ "]";
	}


}
