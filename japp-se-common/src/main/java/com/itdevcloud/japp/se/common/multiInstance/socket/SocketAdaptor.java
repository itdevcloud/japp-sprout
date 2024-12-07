package com.itdevcloud.japp.se.common.multiInstance.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itdevcloud.japp.se.common.security.Crypter;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.service.CommonFactory;
import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * For String message only.
 * 
 * Example of receiver: SocketAdaptorManager socketAdaptorManager = new
 * SocketAdaptorManager(8080); socketAdaptorManager.startReceive(); }
 * 
 * Example of sender: socketAdaptorManager.send("this is request
 * message......");
 * 
 * @author Marvin Sun
 */
class SocketAdaptor {
	private static final CommonLogger logger = CommonLogger.getLogger(SocketAdaptor.class.getName());

//	public static final String ERROR_PREFIX = "[ERROR]";
//	public static final String SUCCESS_PREFIX = "[SUCCESS]";
//	public static final int DEFAULT_PORT = 8800;
//	public static final int DEFAULT_TIMEOUT = 30000; // 30 seconds

	private static final String NEW_LINE_REPLACEMENT = "[NLnl]";
	private static final int MIN_TIMEOUT = 10000; // 10 seconds
	private static final int ACCEPT_RETRY_WAIT = 5000; // 5 seconds
	private static final int BACKLOG = 200; // The maximum length of the queue
	private static final int DEFAULT_ACCEPT_RETRY = 3;
	private static final String DEFAULT_ENCRYPTION_SALT = "Salt123456";

	private static long receiverThreadCount = 0;

	private int timeout = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT;
	private int numOfRetries = DEFAULT_ACCEPT_RETRY;

	// a little bit tricky here
	// the SocketAdaptor can send or receive message.
	// send: use host _ port
	// receive: use port only, do not separate send port and receive port,
	// recommend to create a different sender or receiver adaptor instead
	private String host;
	private int port;

	private ServerSocket serverSocket = null;
//	private String encryptionSecret = null;
//	private String encryptionSalt = null;
	
	private Crypter crypter = null;

	// package access only
	SocketAdaptor() {
		this(null, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PORT, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}

	SocketAdaptor(int port) {
		this(null, port, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}

	SocketAdaptor(int port, int timeoutMills) {
		this(null, port, timeoutMills);
	}

	SocketAdaptor(String host, int port) {
		this(host, port, MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT);
	}

	SocketAdaptor(String host, int port, int timeoutMills) {

		if (StringUtil.isEmptyOrNull(host)) {
			host = CommonUtil.getMyFirstLocalIp(null);
		}
		this.host = host;

		if (port <= 1024 || port > 65535) {
			port = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PORT;
		}
		this.port = port;

		if (timeoutMills < MIN_TIMEOUT) {
			this.timeout = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_TIMEOUT;
		} else {
			this.timeout = timeoutMills;
		}

		String secret = MultiInstanceSocketSupportManager.getSocketMessageProcessor().getEncryptionSecret();
		if (StringUtil.isEmptyOrNull(secret)) {
			this.crypter = null;
		} else {
			String salt = MultiInstanceSocketSupportManager.getSocketMessageProcessor().getEncryptionSalt();
			if (StringUtil.isEmptyOrNull(salt)) {
				salt = DEFAULT_ENCRYPTION_SALT;
			}
			String encodedKey = SecurityUtil.generateKeyFromPassword(secret, salt, null);
			this.crypter = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		}
		//logger.fine("New instance is created. Host=" + host + ", port=" + port);
	}

	String getHost() {
		return host;
	}

	int getPort() {
		return port;
	}

	int getTimeout() {
		return timeout;
	}
	
	void receive() {
		
		//logger.fine("Creating Server Socket on port #" + port);
		Socket socket = null;

		// Init ServerSocket
		if (serverSocket == null) {
			try {
				serverSocket = new ServerSocket(port, BACKLOG);
			} catch (Throwable t) {
				logger.severe("SocketAdaptor(Receiver) can not listen on port # " + port + ", Error: " + t, t);
				close(serverSocket);
				throw new RuntimeException(t);
			}
		}
		// no exception from this point
		// only create server socket fail will throw exception to let manager know
		// something wrong on server side
		// Accept
		logger.fine("Listening on port #" + port);
		boolean acceptSuccess = false;
		for (int i = 1; socket == null && i <= this.numOfRetries; i++) {
			try {
				socket = serverSocket.accept();
				acceptSuccess = true;
			} catch (Throwable t) {
				acceptSuccess = false;
				if (i > this.numOfRetries) {
					// can not accept exception may caused by client side, not server side, do not
					// throw exception
					close(socket);
					logger.severe("Max number of accept retries(" + this.numOfRetries + ") reached. stop receiving....Error: " + t, t);
				} else {
					// Sleep
					logger.severe("Can't accept connection. Going to wait for "
							+ ACCEPT_RETRY_WAIT + "ms, retry count = " + i);
					i++;
					close(socket);
					try {
						Thread.sleep(ACCEPT_RETRY_WAIT);
					} catch (InterruptedException e1) {
						logger.severe("Receiving retry sleep is interupted: " + e1.getMessage());
					}
				}
			}
		}
		if (acceptSuccess) {
			acceptSuccess = false;
			try {
				// may caused by client side, not server side, do not throw exception
				socket.setSoTimeout(this.timeout);
				acceptSuccess = true;
			} catch (SocketException e) {
				acceptSuccess = false;
				close(socket);
				logger.severe("Can not set timeout. Error: " + e, e);
				// throw new RuntimeException(e);
			}
		}
		if (acceptSuccess) {
			try {
				String myInstanceName = InstanceInfo.createInstanceName(this.port, this.host);
				//crypter is not thread safe
				new SocketAdaptorReceiver(socket, new Crypter(this.crypter), myInstanceName).start();
			} catch (Throwable t) {
				// do not through exception to let manager continue to receive
				logger.severe("Can't receive and process the message, error = " + t, t);
			}
		}
	}

	SocketAdaptorMessage send(SocketAdaptorMessage message) {
		logger.finer("Sending message to host=" + host + ", port = " + port);
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			socket = new Socket(this.host, this.port);
			socket.setSoTimeout(this.timeout);
		}catch (Throwable t) {	
			String err = "Can not establish connection to host=" + this.host + ", port = " + this.port + ", Error: " + t;
			close(socket);
			logger.severe(err);
			throw new RuntimeException(t);
		}
		//no exception after this point
		SocketAdaptorMessage responseMessage = null;
		if (message == null) {
			
			responseMessage = new SocketAdaptorMessage();
			String err = "Message is null, do nothing...";
			responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			responseMessage.setResponseStatus(MultiInstanceSocketSupportConstant.RESPONSE_STATUS_ERROR);
			responseMessage.setContent(err);
			
			logger.warning(err);
			return responseMessage;
		}
		
		String requestMessageStr = null;
		String messageReceived = null;
		boolean sendSuccss = false;
		try {
			
			requestMessageStr = SocketAdaptorMessage.getRequestMessageString(message);
			if(MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(message.getType())) {
				logger.finer("message to be sent:" + requestMessageStr);
			}else {
				logger.info("message to be sent:" + requestMessageStr);
			}
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Send
			//hash
			String messageToSend = addHash(requestMessageStr);
			// encrypt if required
			
			//crypter is not thread safe, create a new instance every time
			Crypter crypter = this.crypter == null?null:new Crypter(this.crypter);
			
			messageToSend = crypter == null ? messageToSend : crypter.encryptText(messageToSend);
			// Make sure we have no "\n" in the message.
			messageToSend = messageToSend.replace("\n", NEW_LINE_REPLACEMENT);
			messageToSend = addLength(messageToSend);

			logger.finer("final message to be sent:" + messageToSend);
			out.println(messageToSend);
			sendSuccss = true;
			
			// Receive
			messageReceived = in.readLine();
			messageReceived= RemoveAndCheckLength(messageReceived);
			messageReceived = messageReceived.replace(NEW_LINE_REPLACEMENT, "\n");
			messageReceived = this.crypter == null ? messageReceived : this.crypter.decryptText(messageReceived);
			//check hash
			messageReceived = RemoveAndCheckHash(messageReceived);

			if(MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(message.getType())) {
				logger.finer("Received response message string=\n" + messageReceived);
			}else {
				logger.info("Received response message string=\n" + messageReceived);
			}
			
			responseMessage = SocketAdaptorMessage.createResponseMessage(messageReceived);
			responseMessage.setResponseStatus(MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS);
			
			logger.finer("Received response message Object=\n" + responseMessage);
			if(message.getId() == null || responseMessage.getId() == null || !message.getId().equalsIgnoreCase(responseMessage.getId())) {
				logger.warning("Request message Id is different from response message id!......");
			}
			
			//save send info into cache
			if(!MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(message.getType())) {
				MessageProcessingStatus status = new MessageProcessingStatus(message);
				status.setSentDate(message.getDate());
				status.setDestination(InstanceInfo.createInstanceName(this.port,  this.host));
				status.setDirection(MultiInstanceSocketSupportConstant.DIRECTION_OUTBOUND);
				status.setTransferStatus(responseMessage.getTransferStatus());
				status.setResponseStatus(responseMessage.getResponseStatus());
				status.setProcessStatus(responseMessage.getProcessStatus());
				status.setReceivedDate(responseMessage.getDate());
				
				MessageProcessingStatusCache.addOrUpdate(status);
			}
			
			return responseMessage;
			
		}catch(Throwable t) {
			String err = (sendSuccss?"Send Suceeded, Receive Failed.":"Send Failed.");
			err = err + ", Error:" + t;
			responseMessage = new SocketAdaptorMessage(message);
			responseMessage.setContent(err);
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			responseMessage.setTransferStatus(sendSuccss?MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS:MultiInstanceSocketSupportConstant.TRANSFER_STATUS_ERROR);
			responseMessage.setResponseStatus(MultiInstanceSocketSupportConstant.RESPONSE_STATUS_ERROR);
			
			
			//save send info into cache
			if(!MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(message.getType())) {
				MessageProcessingStatus status = new MessageProcessingStatus(message);
				status.setSentDate(message.getDate());
				status.setDestination(InstanceInfo.createInstanceName(this.port,  this.host));
				status.setDirection(MultiInstanceSocketSupportConstant.DIRECTION_OUTBOUND);
				status.setTransferStatus(responseMessage.getTransferStatus());
				status.setResponseStatus(responseMessage.getResponseStatus());
				status.setProcessStatus(responseMessage.getProcessStatus());
				status.setReceivedDate(responseMessage.getDate());
				
				MessageProcessingStatusCache.addOrUpdate(status);
			}

			
			return responseMessage; 
			
		} finally {
			close(out);
			close(in);
			close(socket);
			logger.finer("Send message end. Destinatin host=" + host + ", port = " + port);
		}
	}

	private String addLength(String rawMessage) {
		return "<length>" + rawMessage.length() + "</length>" + rawMessage;
	}
	private String addHash(String rawMessage) {
		String hashed = Hasher.getHash(rawMessage);
		return "<hash>" + hashed + "</hash>" + rawMessage ;
	}

	private String RemoveAndCheckLength(String rawMessage)  {

		if (StringUtil.isEmptyOrNull(rawMessage)) {
			throw new RuntimeException("message is null...");
		}

		// TODO: do this compile once only.
		Pattern pattern = Pattern.compile("<length>([0-9]+)</length>(.*)");
		Matcher matcher = pattern.matcher(rawMessage);
		if (!matcher.matches())
			throw new RuntimeException("message has missing or wrong length prefix:" + rawMessage.substring(0, 30)+ "...");

		int expectedLength = Integer.parseInt(matcher.group(1));
		rawMessage = matcher.group(2);
		if (expectedLength != rawMessage.length()) {
			throw new RuntimeException("message length check failed; expected " + expectedLength + " bytes, received "
					+ rawMessage.length());
		}
		return rawMessage;
	}
	private String RemoveAndCheckHash(String rawMessage)  {

		if (StringUtil.isEmptyOrNull(rawMessage)) {
			throw new RuntimeException("message is null...");
		}
		//(?s).* includes '\n'
		Pattern pattern = Pattern.compile("<hash>(.+)</hash>((?s).*)");
		Matcher matcher = pattern.matcher(rawMessage);
		if (!matcher.matches())
			throw new RuntimeException("message has missing or wrong hash prefix:" + rawMessage);

		String hashStr = matcher.group(1);
		rawMessage = matcher.group(2);
		if(!Hasher.getHash(rawMessage).equals(hashStr)) {
			logger.finer("Hash Parser Result:\nhashStr=" + hashStr + "\nrawMessage=" + rawMessage);
			throw new RuntimeException("message hash check failed! ");
		}
		return rawMessage;
	}

	void stop() {
		close(serverSocket);
	}

	private class SocketAdaptorReceiver extends Thread {
		private final CommonLogger logger = CommonLogger.getLogger(SocketAdaptorReceiver.class.getName());

		private Socket socket = null;
		private Crypter crypter = null;
		private String myInstanceName = null;
		
		public SocketAdaptorReceiver(Socket socket, Crypter crypter, String myInstanceName) {
			super("SocketAdaptorReceiver-Thread_Count#" + (receiverThreadCount++) + "-"
					+ DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS"));
			this.socket = socket;
			this.crypter = crypter;
			this.myInstanceName = myInstanceName;
		}

		public void run() {
			logger.finer(this.getName() + " receiving and processing message......");
			
			// no exception throwed by this method
			PrintWriter out = null;
			BufferedReader in = null;
			String strRequestMessage = null;
			String strResponseMessage = null;
			SocketAdaptorMessage requestMessage = null;
			SocketAdaptorMessage responseMessage = null;
			boolean transferSuccess = false;
			boolean readSuccess = false;
			

			try {
				try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					// step 1. Read
					strRequestMessage = in.readLine();
					transferSuccess = true;
					strRequestMessage = RemoveAndCheckLength(strRequestMessage);
					strRequestMessage = strRequestMessage.replace(NEW_LINE_REPLACEMENT, "\n");

					String decryptedMessage = this.crypter == null ? strRequestMessage
							: this.crypter.decryptText(strRequestMessage);
					

					strRequestMessage = (StringUtil.isEmptyOrNull(decryptedMessage))?strRequestMessage:decryptedMessage;
					
					//uncomment for hash testing
					//strRequestMessage = "<hash>9kr0Fku0i8srQGugvbq2DZBIVhHVo0DoWowsbuPZ0VCOJgVjTA9bNI2X91z/OYQiBynyPdczyAY55DyaIPh9aw==</hash>[E001];test message - need to comement it out";

					
					
					//check hash
					logger.finer("Received decrypted orignal string=\n" + strRequestMessage);
					strRequestMessage = RemoveAndCheckHash(strRequestMessage);
					
					logger.finer("Received decrypted message string=\n" + strRequestMessage);
					requestMessage = SocketAdaptorMessage.createRequestMessage(strRequestMessage);
					logger.finer("Received message object=\n" + requestMessage);
					
					readSuccess = true;

				} catch(Throwable t) {
					readSuccess = false;
					responseMessage = new SocketAdaptorMessage();
					String err = "can not receive message or can not parse received message. Error: " + t;
					responseMessage.setContent(err);
					responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
					if(transferSuccess) {
						responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
					}else {
						responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_ERROR);
					}
					logger.severe(err, t);
				} 
				// step 2. Process
				//String processedMsg = null;
				if (readSuccess) {
					try {
						logger.finer("Processing received message=\n"
								+ strRequestMessage);
						
						//process the message
						MultiInstanceSocketSupportManager multipleInstanceSocketManager = MultiInstanceSocketSupportManager
								.getSingletonInstance();
						responseMessage = multipleInstanceSocketManager.processMessage(requestMessage);
						
						if(responseMessage == null) {
							responseMessage = new SocketAdaptorMessage(requestMessage);
							String err = "failed to process received message. Porcessor returns null response object ";
							responseMessage.setContent(err);
							responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
							responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
							responseMessage.setContent(err);
							logger.severe(err);
						}else {
							responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
						}
					} catch (Throwable t) {
						// process failed
						responseMessage = new SocketAdaptorMessage(requestMessage);
						String err = "failed to process received message. Error: " + t;
						responseMessage.setContent(err);
						responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
						responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
						responseMessage.setContent(err);
						logger.severe(err, t);
					}
				}
				// step 3. response back
				out = new PrintWriter(socket.getOutputStream(), true);
				String responseMessageStr = null;
				try {
					responseMessageStr = SocketAdaptorMessage.getResponseMessageString(responseMessage);
					//add hash
					strResponseMessage = addHash(responseMessageStr);
					//logger.finer("will response back this message: " + strResponseMessage);
					
					// encrypt if required
					strResponseMessage = this.crypter == null ? strResponseMessage
							: this.crypter.encryptText(strResponseMessage);
					// Make sure we have no "\n" in the message
					strResponseMessage = strResponseMessage.replace("\n", NEW_LINE_REPLACEMENT);
					strResponseMessage = addLength(strResponseMessage);
					
				} catch (Throwable t) {
					strResponseMessage = responseMessageStr;
					logger.severe(strResponseMessage + "\ncan not process response message object: \n" + responseMessage, t);
				}

				// Write
				out.println(strResponseMessage);

				//save send info into cache
				if(requestMessage != null && !MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(requestMessage.getType())) {
					MessageProcessingStatus status = new MessageProcessingStatus(requestMessage);
					status.setSentDate(requestMessage.getDate());
					status.setDestination(this.myInstanceName);
					status.setDirection(MultiInstanceSocketSupportConstant.DIRECTION_INBOUND);
					status.setTransferStatus(responseMessage.getTransferStatus());
					status.setResponseStatus(MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS);
					status.setProcessStatus(responseMessage.getProcessStatus());
					status.setReceivedDate(responseMessage.getDate());
					
					MessageProcessingStatusCache.addOrUpdate(status);
				}

			} catch (Throwable t) {
				//only out operation error can go into here.
				//may casued by client side, do not throw exception
				//save send info into cache
				if(requestMessage != null && !MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(requestMessage.getType())) {
					MessageProcessingStatus status = new MessageProcessingStatus(requestMessage);
					status.setSentDate(requestMessage.getDate());
					status.setDestination(this.myInstanceName);
					status.setDirection(MultiInstanceSocketSupportConstant.DIRECTION_INBOUND);
					status.setTransferStatus(responseMessage.getTransferStatus());
					status.setResponseStatus(MultiInstanceSocketSupportConstant.RESPONSE_STATUS_ERROR);
					status.setProcessStatus(responseMessage.getProcessStatus());
					status.setReceivedDate(responseMessage.getDate());
					
					MessageProcessingStatusCache.addOrUpdate(status);
				}

				logger.severe("can not responsne back message. " + t, t);
			} finally {
				close(out);
				close(in);
				close(socket);
				logger.finer(this.getName() + "...run().....end.........");
			}
		}
	}

	private static void close(Object res) {
		if (res == null) {
			return;
		}
		String type = "Object";
		try {
			if (res instanceof InputStream) {
				type = "InputStream";
				((InputStream) res).close();
			} else if (res instanceof OutputStream) {
				type = "OutputStream";
				((OutputStream) res).close();
			} else if (res instanceof Socket) {
				type = "Socket";
				((Socket) res).close();
			} else if (res instanceof ServerSocket) {
				type = "ServerSocket";
				((ServerSocket) res).close();
			} else if (res instanceof Writer) {
				type = "Writer";
				((Writer) res).close();
			} else if (res instanceof Reader) {
				type = "Reader";
				((Reader) res).close();
			}
			// close success
			res = null;
		} catch (Exception e) {
			logger.severe("SocketAdaptor(Receiver) can not close <" + type + ">. Error: " + e);
		}
	}
}
