package com.itdevcloud.japp.se.common.multiInstance.socket;

import com.itdevcloud.japp.se.common.service.CommonLogger;

public class DefaultSocketEventProcessor implements SocketEventProcessorI {

	private static final CommonLogger logger = CommonLogger.getLogger(DefaultSocketEventProcessor.class.getName());


	@Override
	public SocketAdaptorMessage process(SocketAdaptorMessage message) {
		logger.finer("processing message start......message =\n" + message);

		SocketAdaptorMessage responseMessage = null;
		if (message == null) {
			responseMessage = new SocketAdaptorMessage();
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
			return responseMessage;
		}
		responseMessage = SocketAdaptorMessage.createResponseMessage(message);
		responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);

		// process each command
		if (MultiInstanceSocketSupportConstant.TYPE_EVENT.equalsIgnoreCase(message.getType())) {
			logger.info("processing event [" + message.getName() + "], content = " + message.getContent());
			//add code here
			
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
		} else {
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			String err = "Processor for message type [" + message.getType() + "] is not defined, do nothing for the message!";
			responseMessage.setContent(err);
			logger.severe(err + " Received message object: " + message);
		}
		logger.finer("processing message end......responsed message = \n" + responseMessage);
		return responseMessage;

	}

	@Override
	public String getEncryptionSecret() {
		// get from a central place, all instance must use same secret
		return "abcde54321";
	}

	@Override
	public String getEncryptionSalt() {
		// get from a central place, all instance must use same secret
		return "salt123";
	}

}
