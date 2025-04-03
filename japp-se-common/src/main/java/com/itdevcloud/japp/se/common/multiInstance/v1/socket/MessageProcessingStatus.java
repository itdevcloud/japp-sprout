package com.itdevcloud.japp.se.common.multiInstance.v1.socket;

import java.util.Date;
import java.util.Objects;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class MessageProcessingStatus implements Comparable<MessageProcessingStatus> {
	private static final JulLogger logger = JulLogger.getLogger(MessageProcessingStatus.class.getName());
	private String id;
	private String type;
	private String name;
	private String direction;
	private String source;
	private String destination;
	private String transferStatus;
	private String responseStatus;
	private String processStatus;
	private Date sentDate;
	private Date receivedDate;
	private Date processedDate;

	public MessageProcessingStatus() {
	}

	public MessageProcessingStatus(String id, String source, String destination) {
		this.id = id;
		this.source = source;
		this.destination = destination;
	}

	public MessageProcessingStatus(SocketAdaptorMessage message) {
		if (message != null) {
			this.id = message.getId();
			this.type = message.getType();
			this.name = message.getName();
			this.source = message.getSource();
		}
	}

	public String getPrintString() {
		String infoStr =  id + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				type + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				name + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				direction + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				source + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				destination + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				transferStatus + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				responseStatus + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				processStatus + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(sentDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT) + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(receivedDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT) + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + 
				DateUtils.dateToString(processedDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
		
		//debug
//		MessageProcessingStatus status = getStatusFromPrintString(infoStr);
//		logger.finer("convert string to object = \n" + status);
		
		return infoStr;
	}


	public static MessageProcessingStatus getStatusFromPrintString(String statusStr) {
		if(StringUtil.isEmptyOrNull(statusStr)) {
			return null;
		}
		
		MessageProcessingStatus status = new MessageProcessingStatus();
		
		String[] statusArr = statusStr.split(MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR);
		if (statusArr.length < 12) {
			throw new RuntimeException("Invalid Status String Format!");
		}
		String id = statusArr[0];
		String type = statusArr[1];
		String name = statusArr[2];
		String direction = statusArr[3];
		String source = statusArr[4];
		String destination = statusArr[5];
		String transferStatus = statusArr[6];
		String responseStatus = statusArr[7];
		String processStatus = statusArr[8];
		
		String dateStr = statusArr[9];
		Date sentDate = DateUtils.stringToDate(dateStr, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
		dateStr = statusArr[10];
		Date receivedDate = DateUtils.stringToDate(dateStr, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
		dateStr = statusArr[11];
		Date processedDate = DateUtils.stringToDate(dateStr, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);

		status.setId(id);
		status.setType(type);
		status.setName(name);
		status.setDirection(direction);
		status.setSource(source);
		status.setDestination(destination);
		status.setTransferStatus(transferStatus);
		status.setResponseStatus(responseStatus);
		status.setProcessStatus(processStatus, processedDate);
		status.setSentDate(sentDate);
		status.setReceivedDate(receivedDate);
		
		return status;
	}


	@Override
	public String toString() {
		return "MessageProcessingStatus [id=" + id + ", type=" + type + ", name=" + name + ", direction=" + direction
				+ ", source=" + source + ", destination=" + destination + ", transferStatus=" + transferStatus
				+ ", responseStatus=" + responseStatus + ", processStatus=" + processStatus + 
				", sentDate=" + DateUtils.dateToString(sentDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT)
				+ ", receivedDate=" + DateUtils.dateToString(receivedDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT) + 
				", processedDate=" + DateUtils.dateToString(processedDate, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT) + "]";
	}

	@Override
	public int compareTo(MessageProcessingStatus that) {
		if (that == null) {
			return 1;
		}
		if (this.id == null && that.getId() == null) {
			return 0;
		} else if (this.id == null) {
			return -1;
		} else if (that.getId() == null) {
			return 1;
		} else {
			return this.id.compareTo(that.getId());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(destination, id, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageProcessingStatus other = (MessageProcessingStatus) obj;
		return Objects.equals(destination, other.destination) && Objects.equals(id, other.id)
				&& Objects.equals(source, other.source);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
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

	public String getTransferStatus() {
		return transferStatus;
	}

	public void setTransferStatus(String transferStatus) {
		this.transferStatus = transferStatus;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
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

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Date getProcessedDate() {
		return processedDate;
	}

//	public void setProcessedDate(Date processedDate) {
//		this.processedDate = processedDate;
//	}

}
