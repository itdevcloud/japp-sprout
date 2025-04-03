package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.Date;
import java.util.Objects;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class EventProcessStatus extends EventInfo {

	private static final JulLogger logger = JulLogger.getLogger(EventProcessStatus.class.getName());
	private String proccessedEventUid;
	private String processor;
	private String processStatus;
	private Date processedDate;

	public EventProcessStatus() {
		this(null, null, null, null,null);
	}

	public EventProcessStatus(EventInfo eventInfo, String proccessedEventUid, String processor, String processStatus,
			Date processedDate) {
		super(eventInfo);

		this.proccessedEventUid = proccessedEventUid;
		this.processor = processor;
		this.processStatus = processStatus;
		this.processedDate = processedDate == null?new Date():processedDate;
	}

	@Override
	public String getEventInfoString() {
		//last one can not be null or empty
		String infoStr = super.getEventInfoString() + EventManagerConstant.CONTENT_SEPARATOR + this.proccessedEventUid
				+ EventManagerConstant.CONTENT_SEPARATOR + this.processor + EventManagerConstant.CONTENT_SEPARATOR
				+ this.processStatus + EventManagerConstant.CONTENT_SEPARATOR
				+ DateUtils.dateToString(this.processedDate, EventManagerConstant.DISPLAY_DATE_FORMAT);
		return infoStr;
	}

	@Override
	public EventProcessStatus createEventInfoFromString(String infoString) {
		if (StringUtil.isEmptyOrNull(infoString)) {
			return null;
		}
		//last one can not be null or empty
		String[] infoArr = infoString.split(EventManagerConstant.CONTENT_SEPARATOR);
		if (infoArr.length < 10
				|| (infoArr.length == 10 && !infoString.endsWith(EventManagerConstant.CONTENT_SEPARATOR))) {
			logger.severe("Invalid EventProcessStatus String Format: '" + infoString +"'");
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

		String content = infoArr[6];
		content = StringUtil.isEmptyOrNull(content) ? null : content;

		String proccessedEventUid = infoArr[7];
		String processor = infoArr[8];
		String processStatus = infoArr[9];

		dateStr = null;
		if (infoArr.length == 10) {
			dateStr = null;
		} else {
			dateStr = infoArr[10];
		}
		Date processedDate = DateUtils.stringToDate(dateStr, EventManagerConstant.DISPLAY_DATE_FORMAT);

		EventInfo info = new EventInfo(appName, uid, name, source, eventDate, expiryDate);
		info.setContent(content);

		EventProcessStatus eventProcessStatus = new EventProcessStatus(info, proccessedEventUid, processor,
				processStatus, processedDate);
		return eventProcessStatus;
	}

	@Override
	public String toString() {
		return "EventProcessStatus - " + super.toString() ;
	}

	public String getProccessedEventUid() {
		return proccessedEventUid;
	}

	public void setProccessedEventUid(String proccessedEventUid) {
		this.proccessedEventUid = proccessedEventUid;
	}

	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
		setProcessedDate(new Date());
	}

	public void setProcessStatus(String processStatus, Date processedDate) {
		this.processStatus = processStatus;
		setProcessedDate(processedDate == null?new Date():processedDate);
	}

	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate == null ? new Date() : processedDate;
	}

}
