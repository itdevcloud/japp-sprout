package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.Calendar;
import java.util.Date;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.RandomUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;


public class EventInfo {

	private static final JulLogger logger = JulLogger.getLogger(EventInfo.class.getName());
	//public static  final String eventLockInfoStringFormat = "name" + EventManagerConstant.CONTENT_SEPARATOR +" expiryDate" +  EventManagerConstant.CONTENT_SEPARATOR +" key" +  EventManagerConstant.CONTENT_SEPARATOR + "content";
	//private static final String SPACE = " ";
	
	protected String appName;
	protected String uid;
	protected String name;
	protected String source;
	protected Date eventDate;
	protected Date expiryDate;
	protected String content;

	public EventInfo(String name) {
		this(null, null, name, null,null, null);
	}
	public EventInfo(String appName, String name, String source) {
		this(appName, null, name, source, null, null);
	}
	
	public EventInfo(EventInfo eventInfo) {
		this(eventInfo==null?null:eventInfo.getAppName(),
				eventInfo==null?null:eventInfo.getUid(),
				eventInfo==null?null:eventInfo.getName(), 
				eventInfo==null?null:eventInfo.getSource(),
				eventInfo==null?null:eventInfo.getEventDate(),
				eventInfo==null?null:eventInfo.getExpiryDate());
		this.setContent(eventInfo==null?null:eventInfo.getContent());
	}

	public EventInfo(String appName, String uid, String name, String source, Date eventDate, Date expiryDate) {

		setAppName(appName);
		setName(name);
		setUid(uid);
		this.source = StringUtil.isEmptyOrNull(source)? CommonUtil.getMyFirstLocalIp(null):source.trim().toUpperCase();
		setEventDate(eventDate);
		setExpiryDate(expiryDate);
		//MUST NOT BE "" OR NULL
		setContent(null);
	}

	public EventInfo createEventInfoFromString(String infoString) {
		if(StringUtil.isEmptyOrNull(infoString)) {
			return null;
		}
		String[] infoArr = infoString.split(EventManagerConstant.CONTENT_SEPARATOR);
		if (infoArr.length < 6
				|| (infoArr.length == 6 && !infoString.endsWith(EventManagerConstant.CONTENT_SEPARATOR))) {
			logger.severe("Invalid EventInfo String Format: '" + infoString + "'" );
			return null;
		}
		String appName = infoArr[0];
		String uid = infoArr[1];
		String name = infoArr[2];
		String source = infoArr[3];
		
		String dateStr = infoArr[4];
		Date eventDate = DateUtils.stringToDate(dateStr, EventManagerConstant.DISPLAY_DATE_FORMAT);
		
		dateStr = infoArr[5];
		Date expiryDate = DateUtils.stringToDate(dateStr, EventManagerConstant.DISPLAY_DATE_FORMAT);
		
		String content = null;
		if (infoArr.length == 6) {
			content = null;
		} else {
			content = infoArr[6];
		}
		content = StringUtil.isEmptyOrNull(content)? null:content;
		
		EventInfo info = new EventInfo(appName, uid, name, source, eventDate, expiryDate);
		info.setContent(content);
		
		return info;

	}
	
	public String getEventInfoString() {
		String infoStr =  appName + EventManagerConstant.CONTENT_SEPARATOR + 
				uid + EventManagerConstant.CONTENT_SEPARATOR + 
				name + EventManagerConstant.CONTENT_SEPARATOR + 
				source + EventManagerConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(eventDate, EventManagerConstant.DISPLAY_DATE_FORMAT) + 
				EventManagerConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(expiryDate, EventManagerConstant.DISPLAY_DATE_FORMAT) + 
				EventManagerConstant.CONTENT_SEPARATOR +
				(StringUtil.isEmptyOrNull(content)?EventManagerConstant.EVENT_INFO_EMPTY_CONTENT:content.trim()) ;
		return infoStr;
	}



	@Override
	public String toString() {
		return "EventInfo [" + getEventInfoString() + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		EventInfo other = (EventInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = StringUtil.isEmptyOrNull(appName)? "ALLAPPS":appName.trim().toUpperCase();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = StringUtil.isEmptyOrNull(source)? CommonUtil.getMyFirstLocalIp(null):source.trim().toUpperCase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = StringUtil.isEmptyOrNull(name)? "NO-EVENT-NAME":name.trim().toUpperCase();
	}
	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = (eventDate == null?new Date():eventDate);
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		if (expiryDate == null) {
			expiryDate = DateUtils.addTime(eventDate, Calendar.MINUTE, EventManagerConstant.DEFAULT_EVENT_EXPIRE_MINUTES);
		}
		this.expiryDate = expiryDate;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = StringUtil.isEmptyOrNull(uid)? RandomUtil.generateUniqueID(null, EventManagerConstant.DEFAULT_EVENT_UID_RANDOM_LENGTH): uid.trim();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = StringUtil.isEmptyOrNull(content)? EventManagerConstant.EVENT_INFO_EMPTY_CONTENT:content.trim();
	}


}
