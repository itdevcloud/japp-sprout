package com.itdevcloud.japp.se.common.multiInstance.socket;


public interface SocketEventProcessorI {
	

	SocketAdaptorMessage process(SocketAdaptorMessage message);
	String getEncryptionSecret();
	String getEncryptionSalt();
}