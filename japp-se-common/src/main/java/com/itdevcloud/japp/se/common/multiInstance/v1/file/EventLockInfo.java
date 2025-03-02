package com.itdevcloud.japp.se.common.multiInstance.v1.file;

import java.util.Calendar;
import java.util.Date;

import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;


public class EventLockInfo {

	public static  final String eventLockInfoStringFormat = "name" + MultiInstanceFileSupportConstant.CONTENT_SEPARATOR +" expiryDate" +  MultiInstanceFileSupportConstant.CONTENT_SEPARATOR +" key" +  MultiInstanceFileSupportConstant.CONTENT_SEPARATOR + "content";
	
	private String name;
	private Date expiryDate;
	private String key;
	private String content;

	public EventLockInfo(String name) {
		this(name, null, null);
	}

	public EventLockInfo(String name, Date expiryDate, String key) {

		this.name = name;
		this.key = key;
		if (expiryDate == null) {
			expiryDate = DateUtils.addTime(new Date(), Calendar.MINUTE, 5);
		}
		this.expiryDate = expiryDate;
	}

	public static EventLockInfo createLineInfoFromString(String infoString) {
		if(StringUtil.isEmptyOrNull(infoString)) {
			return null;
		}
		String[] infoArr = infoString.split(MultiInstanceFileSupportConstant.CONTENT_SEPARATOR);
		if (infoArr.length < 4) {
			throw new RuntimeException("Invalid EventLockInfo String Format: " + infoString );
		}
		String name = infoArr[0];
		String dateStr = infoArr[1];
		Date expiryDate = DateUtils.stringToDate(dateStr, MultiInstanceFileSupportConstant.DISPLAY_DATE_FORMAT);
		String key = infoArr[2];
		String content = infoArr[3];

		EventLockInfo info = new EventLockInfo(name, expiryDate, key);
		info.setContent(content);
		
		return info;

	}
	
	public String getLineInfoString() {
		String infoStr =  name + MultiInstanceFileSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(expiryDate, MultiInstanceFileSupportConstant.DISPLAY_DATE_FORMAT) + MultiInstanceFileSupportConstant.CONTENT_SEPARATOR + 
				key + MultiInstanceFileSupportConstant.CONTENT_SEPARATOR + 
				content ;
		return infoStr;
	}



	@Override
	public String toString() {
		return "LineInfo [name=" + name + ", expiryDate=" + DateUtils.dateToString(expiryDate, MultiInstanceFileSupportConstant.DISPLAY_DATE_FORMAT) + ", key=" + key + ", content=" + content + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		EventLockInfo other = (EventLockInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


}
