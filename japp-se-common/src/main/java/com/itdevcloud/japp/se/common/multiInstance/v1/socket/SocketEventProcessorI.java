package com.itdevcloud.japp.se.common.multiInstance.v1.socket;


public interface SocketEventProcessorI {
	

	SocketAdaptorMessage process(SocketAdaptorMessage message);
	String getEncryptionSecret();
	String getEncryptionSalt();
}