package com.itdevcloud.japp.se.common.multiInstance.v1.socket;

import com.itdevcloud.japp.se.common.service.JulLogger;
/**
 * @author Marvin Sun
 * @since 1.0.0
 * 
 */
public class SocketAdaptorManager extends Thread{

	private static final JulLogger logger = JulLogger.getLogger(SocketAdaptorManager.class.getName());
	
	private static final int DEFAULT_RECEIVE_RETRY = 3; 
	private static final int RECEIVE_RETRY_WAIT = 1000; // 1 seconds
	
	private boolean receiving = true;
	private SocketAdaptor sender;
	private SocketAdaptor receiver;

	public SocketAdaptorManager() {
		this(null, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PORT, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}
	public SocketAdaptorManager(int port) {
		this(null, port, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}

	public SocketAdaptorManager(int port, int timeoutMills) {
		this(null, port, timeoutMills);
	}
	
	public SocketAdaptorManager(String host, int port) {
		this(host, port, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}

	public SocketAdaptorManager(String host, int port, int timeoutMills) {
		//super("SocketAdaptorManager:" + port + "-" + host);
		sender = new SocketAdaptor(host, port, timeoutMills);
		receiver = new SocketAdaptor(host, port, timeoutMills);
		String threaName = "SocketAdaptorManager:" + sender.getPort() + "-" + sender.getHost();
		super.setName(threaName);
	}
	
	String getHost() {
		return sender.getHost();
	}

	int getPort() {
		return sender.getPort();
	}
	
	int getTimeout() {
		return sender.getTimeout();
	}
	
	public SocketAdaptorMessage send(SocketAdaptorMessage message) {
		return sender.send(message);
	}
	
	public void startReceive() {
		this.start();
	}

	public void run() {
		logger.info("SocketAdaptor (Receiver) Listening on port #" + receiver.getPort() + ", max restart retries=" + DEFAULT_RECEIVE_RETRY + ", Timeout = "+ receiver.getTimeout());
		//ONLY RETRY WHEN SERVER SOCKET CAN NOT BE CREATED
		int retry = 1;
		while (receiving && retry <= DEFAULT_RECEIVE_RETRY) {
			try {
				receiver.receive();
				//reset retry
				retry = 1;
			} catch (Throwable t) {
				if(retry < DEFAULT_RECEIVE_RETRY) {
					logger.severe("Receiveing failed, wait for retry. Retry count:" + retry + ". " + t, t);
					retry++;
					try {
						Thread.sleep(RECEIVE_RETRY_WAIT);
					} catch (InterruptedException e) {
						logger.finer("Receiving retry wait is interupted: " + e);
					}
				}else {
					logger.severe("SocketAdaptorManager receiveing failed and retied " + retry + " times. Stop receiveing. Error: " + t, t);
					logger.severe("SocketAdaptor Stop Listening on port #" + receiver.getPort());
					stopReceiver();
				}
			}
		}
	}

	public void stopReceiver() {
		try {
			receiving = false;
			receiver.stop();
		} catch (Throwable t) {
			receiving = false;
			logger.severe("Failed to stop receiver.... Error: "+ t, t);
		}
	}

}