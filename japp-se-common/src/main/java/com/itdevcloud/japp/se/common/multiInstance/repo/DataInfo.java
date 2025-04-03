package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.Date;
import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class DataInfo extends EventInfo {

	private static final JulLogger logger = JulLogger.getLogger(DataInfo.class.getName());

	public DataInfo(String appName, String content) {
		super(appName, null, null);
		Date expiryDate = DateUtils.stringToDate("2099-12-31", "yyyy-MM-dd");
		this.setExpiryDate(expiryDate);
		this.setContent(content);
	}

	public DataInfo(EventInfo eventInfo) {
		super(eventInfo);
	}

	@Override
	public String getEventInfoString() {
		String infoStr = this.appName + EventManagerConstant.CONTENT_SEPARATOR
				+ DateUtils.dateToString(this.expiryDate, EventManagerConstant.DISPLAY_DATE_FORMAT)
				+ EventManagerConstant.CONTENT_SEPARATOR + this.content;
		return infoStr;
	}

	@Override
	public DataInfo createEventInfoFromString(String infoString) {
		if (StringUtil.isEmptyOrNull(infoString)) {
			return null;
		}
		// last one can not be null or empty
		String[] infoArr = infoString.split(EventManagerConstant.CONTENT_SEPARATOR);
		if (infoArr.length < 2
				|| (infoArr.length == 2 && !infoString.endsWith(EventManagerConstant.CONTENT_SEPARATOR))) {
			logger.severe("Invalid DataInfo String Format: '" + infoString + "'");
			return null;
		}
		String appName = infoArr[0];

		String dateStr = infoArr[1];
		Date expiryDate = DateUtils.stringToDate(dateStr, EventManagerConstant.DISPLAY_DATE_FORMAT);

		String content = null;
		if (infoArr.length == 2) {
			content = null;
		} else {
			content = infoArr[2];
		}
		content = StringUtil.isEmptyOrNull(content) ? null : content;

		DataInfo dataInfo = new DataInfo(appName, content);
		dataInfo.setExpiryDate(expiryDate);
		return dataInfo;
	}

	@Override
	public String toString() {
		return "DataInfo " + "[appName=" + this.appName + ", expiryDate="
				+ DateUtils.dateToString(this.expiryDate, EventManagerConstant.DISPLAY_DATE_FORMAT) + ", content="
				+ this.content + "]";
	}

}
