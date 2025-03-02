package com.itdevcloud.japp.se.common.multiInstance.v1.socket;

import java.util.Date;
import java.util.Objects;

import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.RandomUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class SocketAdaptorMessage {

	private static final CommonLogger logger = CommonLogger.getLogger(SocketAdaptorMessage.class.getName());


	private String id;
	private String type;
	private String name;
	private String content;
	private String source;
	private String transferStatus;
	private String responseStatus;
	private String processStatus;
	private Date date;

	public SocketAdaptorMessage() {
		this(null, null, null, null, null);
	}
	public SocketAdaptorMessage(SocketAdaptorMessage message) {
		this(null, null, null, null, null);
		if(message != null) {
			this.type = message.getType();
			this.id = message.getId();
			this.source = message.getSource();
			this.name = message.getName();
		}
	}

	public SocketAdaptorMessage(String type, String name, String source) {
		this(null, type, name, source, null);
	}

	public SocketAdaptorMessage(String type, String name, String source, String content) {
		this(null, type, name, source, content);
	}

	public SocketAdaptorMessage(String id, String type, String name, String source, String content) {
		this(id, type, name, source, content, null);
	}

	public SocketAdaptorMessage(String id, String type, String name, String source, String content, Date date) {
		super();
		if (StringUtil.isEmptyOrNull(id)) {
			id = RandomUtil.generateMessageID();
		}
		if (StringUtil.isEmptyOrNull(type)) {
			type = MultiInstanceSocketSupportConstant.TYPE_NA;
			// logger.warning("Message type, can not be empty or null, use default value: "
			// + TYPE_NA);
		}
		if (StringUtil.isEmptyOrNull(name)) {
			name = MultiInstanceSocketSupportConstant.NAME_NA;
			// logger.warning("Message name, can not be empty or null, use default value: "
			// + NAME_NA);
		}
		if (StringUtil.isEmptyOrNull(source)) {
			source = MultiInstanceSocketSupportConstant.SOURCE_NA;
			// logger.warning("Message source, can not be empty or null, use default value:
			// " + SOURCE_NA);
		}
		if (StringUtil.isEmptyOrNull(content)) {
			content = MultiInstanceSocketSupportConstant.CONTENT_EMPTY;
			// logger.warning("Message content, can not be empty or null, use default value:
			// " + CONTENT_NA);
		}
		if (date == null) {
			date = new Date();
		}
		this.id = id;
		this.type = type;
		this.name = name;
		this.content = content;
		this.source = source;
		this.date = date;
	}

	public static SocketAdaptorMessage createResponseMessage(SocketAdaptorMessage requestMessage) {
		SocketAdaptorMessage responseMessage = new SocketAdaptorMessage();
		;
		if (requestMessage == null) {
			return responseMessage;
		}
		responseMessage.setId(requestMessage.getId());
		responseMessage.setType(requestMessage.getType());
		responseMessage.setName(requestMessage.getName());
		responseMessage.setSource(requestMessage.getSource());
		return responseMessage;
	}

	// [date];[type];[name];[id][source];[content]
	public static SocketAdaptorMessage createRequestMessage(String requestMessageString) {
		if (StringUtil.isEmptyOrNull(requestMessageString)) {
			throw new RuntimeException("requestMessageString is null,  can not create request message object!");
		}
		String[] reqArr = requestMessageString.split(MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR);
		if (reqArr.length < 6) {
			throw new RuntimeException("Invalid Message Format! Correct format: [type];[name];[id][source];[content]");
		}
		String dateStr = getContentFromSegment(reqArr[0]);
		Date date = DateUtils.stringToDate(dateStr, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
		String type = getContentFromSegment(reqArr[1]);
		String name = getContentFromSegment(reqArr[2]);
		String id = getContentFromSegment(reqArr[3]);
		String source = getContentFromSegment(reqArr[4]);
		String content = getContentFromSegment(reqArr[5]);

		SocketAdaptorMessage message = new SocketAdaptorMessage(id, type, name, source, content, date);
		return message;
	}

	// [date];[type];[name];[id][source];[content]
	public static String getRequestMessageString(SocketAdaptorMessage message) {
		if (message == null) {
			throw new RuntimeException("message object is null, can not create request message!");
		}

		String requestStr = 
				createSegment(message.getDateString()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR +
				createSegment(message.getType()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getName()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR +
				createSegment(message.getId()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR +
				createSegment(message.getSource()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getContent());
		return requestStr;
	}

	// [date];[transfer_status];[processor_status];[type];[name];[id][source];[content]
	public static String getResponseMessageString(SocketAdaptorMessage message) {
		if (message == null) {
			throw new RuntimeException("message object is null, can not create response message!");
		}

		String responseStr = 
				createSegment(message.getDateString()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR	+ 
				createSegment(message.getTransferStatus()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR	+ 
				createSegment(message.getProcessStatus()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getType())+ MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getName()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getId()) + MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getSource())+ MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR + 
				createSegment(message.getContent());
		return responseStr;
	}

	// [date];[transfer_status];[processor_status];[type];[name];[id][source];[content]
	public static SocketAdaptorMessage createResponseMessage(String responseMessageString) {
		if (StringUtil.isEmptyOrNull(responseMessageString)) {
			throw new RuntimeException("requestMessageString is null,  can not create response message object!");
		}
		String[] reqArr = responseMessageString.split(MultiInstanceSocketSupportConstant.SEGMENT_SEPARATOR);
		if (reqArr.length < 8) {
			throw new RuntimeException(
					"Invalid Response Message String Format! Correct format: [socket_status];[processor_status];[type];[name];[id][source];[content]");
		}
		String dateStr = getContentFromSegment(reqArr[0]);
		Date date = DateUtils.stringToDate(dateStr, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
		String transferStatus = getContentFromSegment(reqArr[1]);
		String processorStatus = getContentFromSegment(reqArr[2]);
		String type = getContentFromSegment(reqArr[3]);
		String name = getContentFromSegment(reqArr[4]);
		String id = getContentFromSegment(reqArr[5]);
		String source = getContentFromSegment(reqArr[6]);
		String content = getContentFromSegment(reqArr[7]);

		SocketAdaptorMessage message = new SocketAdaptorMessage(id, type, name, source, content, date);
		message.setTransferStatus(transferStatus);
		message.setProcessStatus(processorStatus);

		return message;
	}

	private static String getContentFromSegment(String segment) {
		if (StringUtil.isEmptyOrNull(segment)) {
			throw new RuntimeException("segment string is null, can not get content!");
		}
		if (!segment.startsWith(MultiInstanceSocketSupportConstant.BOUNDARY_LEFT) || !segment.endsWith(MultiInstanceSocketSupportConstant.BOUNDARY_RIGHT)) {
			throw new RuntimeException(
					"Wrong format dtected, correct format: " + MultiInstanceSocketSupportConstant.BOUNDARY_LEFT + "content" + MultiInstanceSocketSupportConstant.BOUNDARY_RIGHT);
		}
		String content = segment.substring(1, segment.length() - 1);
		return content == null ? MultiInstanceSocketSupportConstant.CONTENT_EMPTY : content.trim();
	}

	private static String createSegment(String content) {
		if (StringUtil.isEmptyOrNull(content)) {
			content = MultiInstanceSocketSupportConstant.CONTENT_EMPTY;
		}
		String segment = MultiInstanceSocketSupportConstant.BOUNDARY_LEFT + content + MultiInstanceSocketSupportConstant.BOUNDARY_RIGHT;
		return segment;
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

	public String getContent() {
		if (content == null) {
			content = MultiInstanceSocketSupportConstant.CONTENT_EMPTY;
		}
		return content;
	}

	public void setContent(String content) {
		if (content == null) {
			content = MultiInstanceSocketSupportConstant.CONTENT_EMPTY;
		}
		this.content = content;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
	}

	public String getDateString() {
		if (date == null) {
			date = new Date();
		}
		return DateUtils.dateToString(date, MultiInstanceSocketSupportConstant.DISPLAY_DATE_FORMAT);
	}

	public Date getDate() {
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	public void setDate(Date date) {
		if (date == null) {
			date = new Date();
		}
		this.date = date;
	}

	@Override
	public String toString() {
		return "SocketAdaptorMessage [id=" + id + ", type=" + type + ", name=" + name + ", content=" + content
				+ ", source=" + source + ", transferStatus=" + transferStatus + ", responseStatus=" + responseStatus + ", processStatus=" + processStatus + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SocketAdaptorMessage other = (SocketAdaptorMessage) obj;
		return Objects.equals(id, other.id);
	}

}
