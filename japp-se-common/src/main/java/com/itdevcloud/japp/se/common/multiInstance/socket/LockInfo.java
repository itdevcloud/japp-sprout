package com.itdevcloud.japp.se.common.multiInstance.socket;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class LockInfo {
	public static final String LOCK_STATUS_LOCKED = "LOCKED";
    public static final String LOCK_STATUS_RELEASED = "RELEASED";
	public static final String LOCK_STATUS_REJECTED = "REJECTED";

	//public static final String KEY_NA = "n/a";

	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private String name;
	private String requester;
	private String status;
	private Date lockDate;
	private Date expiryDate;
	private String key;

	public LockInfo(String name, String requester) {
		this(name, requester, null);
	}

	public LockInfo(String name, String requester, Date expiryDate) {
		this(name, requester, expiryDate, null);
	}
	public LockInfo(String name, String requester, Date expiryDate, String key) {
		if (StringUtil.isEmptyOrNull(name) || StringUtil.isEmptyOrNull(requester)) {
			throw new RuntimeException(
					"Can not create new LockInfo object becasue name and/or requester are null or empty!");
		}

		this.name = name;
		this.requester = requester;
		this.key = key;
		if (expiryDate == null) {
			expiryDate = DateUtils.addTime(new Date(), Calendar.MINUTE, 5);
		}
		this.expiryDate = expiryDate;
		this.status = null;
		this.lockDate = null;
	}

	public static LockInfo createLockInfoFromString(String infoString) {
		if(StringUtil.isEmptyOrNull(infoString)) {
			return null;
		}
		String[] infoArr = infoString.split(MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR);
		if (infoArr.length < 6) {
			throw new RuntimeException("Invalid Lock Info String Format!");
		}
		String name = infoArr[0];
		String requester = infoArr[1];
		String status = infoArr[2];
		String lockDateStr = infoArr[3];
		Date lockDate = DateUtils.stringToDate(lockDateStr, DISPLAY_DATE_FORMAT);
		String expiryDateStr = infoArr[4];
		Date expiryDate = DateUtils.stringToDate(expiryDateStr, DISPLAY_DATE_FORMAT);
		String key = infoArr[5];
		
		LockInfo info = new LockInfo(name, requester, expiryDate);
		
		info.setStatus(status);
		info.setLockDate(lockDate);
		info.setKey(key);
		
		return info;

	}

	public String getLockInfoString() {
		String infoStr =  name + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				requester + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				status + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(lockDate, DISPLAY_DATE_FORMAT) + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(expiryDate, DISPLAY_DATE_FORMAT) + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				key ;
		return infoStr;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LockInfo other = (LockInfo) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "LockInfo [name=" + name + ", requester=" + requester + ", status=" + status + ", lockDate=" + lockDate
				+ ", expiryDate=" + expiryDate + ", key=" + key + "]";
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRequester() {
		return requester;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		if (status != null && !status.equalsIgnoreCase(LOCK_STATUS_LOCKED)
				&& !status.equalsIgnoreCase(LOCK_STATUS_RELEASED) && !status.equalsIgnoreCase(LOCK_STATUS_REJECTED)) {
			throw new RuntimeException(
					"status can only be one of these values: LOCKED, RELEASED, REJECTED!");
		}
		this.status = status;
	}

	public Date getLockDate() {
		return lockDate;
	}

	public void setLockDate(Date lockDate) {
		this.lockDate = lockDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expireDate) {
		this.expiryDate = expireDate;
	}

}
