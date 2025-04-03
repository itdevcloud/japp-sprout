package com.itdevcloud.japp.se.common.multiInstance.v1;

import java.util.Date;
import java.util.Objects;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class EventStatus implements Comparable<EventStatus> {
	
	public static final String CONTENT_SEPARATOR = "◄►";
	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private static final JulLogger logger = JulLogger.getLogger(EventStatus.class.getName());
	private String messageId;
	private String name;
	private String source;
	private String destination;
	private String processStatus;
	private Date processedDate;

	public EventStatus() {
	}

	public EventStatus(String messageId, String name, String source, String destination) {
		this.messageId = messageId;
		this.name = name;
		this.source = source;
		this.destination = destination;
	}


	public String getPrintString() {
		String infoStr =  messageId + CONTENT_SEPARATOR + 
				name + CONTENT_SEPARATOR + 
				source + CONTENT_SEPARATOR + 
				destination + CONTENT_SEPARATOR + 
				processStatus + CONTENT_SEPARATOR + 
				DateUtils.dateToString(processedDate, DISPLAY_DATE_FORMAT);
		
		
		return infoStr;
	}


	public static EventStatus getStatusFromPrintString(String statusStr) {
		if(StringUtil.isEmptyOrNull(statusStr)) {
			return null;
		}
		
		EventStatus status = new EventStatus();
		
		String[] statusArr = statusStr.split(CONTENT_SEPARATOR);
		if (statusArr.length < 6) {
			logger.severe("Invalid Status String Format: " + statusStr + ", return null! ");
			return null;
		}
		String messageId = statusArr[0];
		String name = statusArr[1];
		String source = statusArr[2];
		String destination = statusArr[3];
		String processStatus = statusArr[4];
		
		String dateStr = statusArr[5];
		Date processedDate = DateUtils.stringToDate(dateStr, DISPLAY_DATE_FORMAT);

		status.setMessageId(messageId);
		status.setName(name);
		status.setSource(source);
		status.setDestination(destination);
		status.setProcessStatus(processStatus, processedDate);
		status.setProcessedDate(processedDate);
		
		return status;
	}




	@Override
	public String toString() {
		return "EventStatus [messageId=" + messageId + ", name=" + name + ", source=" + source + ", destination="
				+ destination + ", processStatus=" + processStatus + ", processedDate=" + DateUtils.dateToString(processedDate, DISPLAY_DATE_FORMAT) + "]";
	}

	@Override
	public int compareTo(EventStatus that) {
		if (that == null) {
			return 1;
		}
		if (this.messageId == null && that.getMessageId() == null) {
			return 0;
		} else if (this.messageId == null) {
			return -1;
		} else if (that.getMessageId() == null) {
			return 1;
		} else {
			return this.messageId.compareTo(that.getMessageId());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(destination, messageId, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventStatus other = (EventStatus) obj;
		return Objects.equals(destination, other.destination) && Objects.equals(messageId, other.messageId)
				&& Objects.equals(source, other.source);
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}



	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
		this.processedDate = new Date();
	}
	public void setProcessStatus(String processStatus, Date processedDate) {
		this.processStatus = processStatus;
		this.processedDate = processedDate;
	}


	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}

}
