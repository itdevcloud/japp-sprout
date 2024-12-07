package com.itdevcloud.japp.se.common.multiInstance.socket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.itdevcloud.japp.se.common.multiInstance.EventStatus;
import com.itdevcloud.japp.se.common.multiInstance.MultipleInstanceSupportI;
import com.itdevcloud.japp.se.common.service.CommonFactory;
import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
/**
 * this class provide a simple socket based support for multiple instances running environment
 * this class also rely on a central file to handle main instance info 
 * the main assumption is that to use this class, all instances can access same main instance file stored in a central place.
 * 
 * if the assumption can not be applied to the target environment, this class can be changed/enhanced to support other type of central repositories, such as:
 * - database table, Azure storage account, Key Vault, Central Cache etc.
 * 
 * Logics in different implementations should be similar and following 3 functions are provided by the class:
 * - addLock()
 * 		Note: 
 * 			for Lock, name is unique ID. the key is used to release lock. key can be provided by client otherwise the key will be generated automatically
 * 			return an empty key means lock failed.
 * 			default expire date for lock is now  +5 mins
 * 		basic process logic: lock file --> clean up expired lock --> add lock info if there is no same lock name exists into file --> close file
 * 
 * - releaseLock()
 * 		Note: 
 * 			for releasing a  Lock, name and key must be provided.
 * 		basic process logic: lock file --> clean up expired lock --> remove the lock info from lock file if lock name and key are matched --> close file

 * - broadcastEvent()
 * 		Note: 
 * 			for event, this class will send event message through socket 
 *			default expire date for event is now + 24 hours
 * 		basic process logic: send the event info through socket to each instance
 * 
 * unlike File based multiple instance support, this class provide a function to check event handling status across instance:
 *  - inquiryEventStatus()
 *  
 * How to manage instances. 
 *  - each instance will get main instance info (port-IP) during startup process and call pingOrRegier to pin main instance and register itself
 *  - each instance will ping main instance regularly, if can not ping main instance, if the error is not network error, wait until next ping, it is network error, it will create main instance 
 *  	response request file to see whether main instance is alive or not. if main instance responded, then do nothing, wait for next ping, 
 *  	if not, it try to register itself as main instance.
 *  	main instance will return latest instance list for each ping request.
 *  - main instance ping function: it will check whether there is a response request file created or not, if there is one, it will update main instance file to set last ping time.
 *  	it also clean up instance cache list if there is instance not ping for a while.
 *  - this instance will process PING, AddLock, Release Lock and Inquiry Event Status request, Event processing will be done in SocketEventMessageProcessor
 *  - this class will try / choose available ports one by one based on the configuration.
 *  - the message send through socket will be encrypted and hashed based on the encryption secret provided by SocketEventMessageProcessor.
 *  - startup: this class (init())--> SocketAdaptorManager --> two SocketerAdaptors(sender/receiver) --> receiver adaptor start to listen
 * 
 * Example:
 * 		MultiInstanceSocketSupportManager manager = MultiInstanceSocketSupportManager.getSingletonInstance();
 *		String lockName = "Lock-1" ;
 *		String key = "12345-1";
 *
 *		String tmpKey = manager.addLock(lockName, key);
 *		System.out.println("returned key = " + tmpKey);
 *
 *		lockName = "Lock-1" ;
 *		key = "12345-1";
 *		result = manager.releaseLock(lockName, key);
 *		System.out.println("Release Lock-1 , Instance-1 result = " + result);
 *		
 *		String mId1 = manager.broadcastEventMessage("user change event", null, "event 1 testing message from Test3.....");
 *		
 *		List<EventStatus> statusList = manager.inquiryEventStatus(mId1);
 *
 *
 */

public class MultiInstanceSocketSupportManager extends Thread implements MultipleInstanceSupportI {

	private static final CommonLogger logger = CommonLogger
			.getLogger(MultiInstanceSocketSupportManager.class.getName());

	private static final long MAX_PING_INACTIVE_INTERVAL_MILLISECOND = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND
			+ 10000;

	private static SocketEventProcessorI socketMessageProcessor = null;

	private InstanceInfo myInstanceInfo = null;
	private InstanceInfo mainInstanceInfo = null;
	private boolean pingEnabled = true;
	private int pingIntervalMillisecond = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND;

	private List<InstanceInfo> instanceInfoList = null;

	private static MultiInstanceSocketSupportManager instance = null;

	public static SocketEventProcessorI getSocketMessageProcessor() {
		if (socketMessageProcessor == null) {
			socketMessageProcessor = CommonFactory.getInstance("SocketMessageProcessorI");
			if (socketMessageProcessor == null) {
				socketMessageProcessor = new DefaultSocketEventProcessor();
				logger.warning(
						"can not get socketMessageProcessor instance, check CommonFacotry configuration file. Use DefaultSocketMessageProcessor instead....");
				logger.info("Socket Encryption Secret and Salt are provided by DefaultSocketMessageProcessor.....");
			} else {
				logger.info("Socket Encryption Secret and Salt are provided by "
						+ socketMessageProcessor.getClass().getSimpleName() + "......");
			}

		}
		return socketMessageProcessor;
	}

	public static MultiInstanceSocketSupportManager getSingletonInstance() {
		if (instance == null) {
			instance = new MultiInstanceSocketSupportManager(MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_START_PORT,
					MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_END_PORT,
					MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND);
		}
		return instance;
	}

	public static MultiInstanceSocketSupportManager getSingletonInstance(int startPort, int endPort,
			int pingIntervalMillisecond) {
		if (instance == null) {
			instance = new MultiInstanceSocketSupportManager(startPort, endPort, pingIntervalMillisecond);
		}
		return instance;
	}

	private MultiInstanceSocketSupportManager(int startPort, int endPort, int pingIntervalMillisecond) {
		super("MultiInstanceSocketSupportManager: Tread #" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS"));
		init(startPort, endPort, pingIntervalMillisecond);
	}

	private void init(int startPort, int endPort, int pingIntervalMillisecond) {
		logger.finer("Init start.........");
		if (startPort <= 1024 || startPort > 65535) {
			String err = "Wwrong start port provided: " + startPort + ", used default start port instead:"
					+ MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_START_PORT;
			logger.warning(err);
			startPort = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_START_PORT;
		}
		if (endPort <= 1024 || endPort > 65535) {
			String err = "Wrong end port provided: " + endPort + ", used default start port instead:"
					+ MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_END_PORT;
			logger.warning(err);
			endPort = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_END_PORT;
		}
		if (endPort < startPort) {
			String err = "End port provided: " + endPort + " less than start port:" + startPort
					+ ", set end port equal to start port.";
			logger.warning(err);
			endPort = startPort;
		}
		// init processor
		MultiInstanceSocketSupportManager.getSocketMessageProcessor();

		int myPort = CommonUtil.getNextAvailablePort(startPort, endPort);

		if (myPort < 0) {
			pingEnabled = false;
			throw new RuntimeException("Init Failed: Can not get avaible listen/receiving port for " + this.getName());
		}
		if (pingIntervalMillisecond <= 100) {
			pingIntervalMillisecond = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_PING_INTERVAL_MILLISECOND;
		}
		if (pingIntervalMillisecond > MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND) {
			pingIntervalMillisecond = MultiInstanceSocketSupportConstant.DEFAULT_SOCKET_MAX_PING_INTERVAL_MILLISECOND;
		}
		this.pingIntervalMillisecond = pingIntervalMillisecond;

		List<InstanceInfo> instanceInfoList = new ArrayList<InstanceInfo>();
		String myIp = CommonUtil.getMyFirstLocalIp(null);

		InstanceInfo myInstanceInfo = new InstanceInfo(myIp, myPort, this.pingIntervalMillisecond, -1);
		myInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_SELF);
		this.myInstanceInfo = myInstanceInfo;

		InstanceInfo mainInstanceInfo = getMainInstanceInfo();

		if (this.myInstanceInfo.equals(mainInstanceInfo)) {
			mainInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
			myInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
			// do not add self instance in this case;
		} else {
			mainInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN);
			instanceInfoList.add(this.myInstanceInfo);
		}
		instanceInfoList.add(mainInstanceInfo);

		this.mainInstanceInfo = mainInstanceInfo;
		this.instanceInfoList = instanceInfoList;

		logger.info("Socket Encryption Secret and Salt are provided by "
				+ socketMessageProcessor.getClass().getSimpleName() + "......");

		logger.info("Init end. Latest InstanceInfoList = " + this.getInstanceInfoListPrintString());

		myInstanceInfo.getSocketAdaptorManager().startReceive();

	}

	private InstanceInfo getMainInstanceInfo() {
		// get from a central place
		// currently use a file
		// to deal with current access, retry 3 times
		int i = 1;
		String mainInstanceInfoStr = null;
		while (i <= 3) {
			mainInstanceInfoStr = FileUtil.getFileContentAsString(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME);
			if (StringUtil.isEmptyOrNull(mainInstanceInfoStr)) {
				logger.finer("Can not read main instance info from file: "
						+ MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME);
				// can not find main instance info, it is the first instance running
				// set this instance as main instance
				logger.finer("Try to set this instance as main instance, count: " + i);
				String infoStr = this.myInstanceInfo.getInstanceInfoString();
				if (FileUtil.writeStringToFile(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME, false, infoStr)) {
					mainInstanceInfoStr = infoStr;
					break;
				}
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.finest("sleep interupted" + e);
				}
			} else {
				break;
			}
		}
		if (StringUtil.isEmptyOrNull(mainInstanceInfoStr)) {
			throw new RuntimeException("Init Failed: Can not get or set main instance info......" + this.getName());
		}
		logger.finer("MainInstanceInfoStr=" + mainInstanceInfoStr);
		InstanceInfo instanceInfo = new InstanceInfo(mainInstanceInfoStr);
		return instanceInfo;
	}

	private String getInstanceInfoListPrintString() {
		if (instanceInfoList == null || instanceInfoList.isEmpty()) {
			return null;
		}
		String infoListStr = "Size:" + instanceInfoList.size();
		for (InstanceInfo info : instanceInfoList) {
			infoListStr = infoListStr + "\n" + info.getInstanceInfoPrintString();
		}
		return infoListStr;
	}

	private String getPingResponseInstanceInfoString() {
		if (instanceInfoList == null || instanceInfoList.isEmpty()) {
			return null;
		}
		String infoListStr = "";
		for (InstanceInfo info : instanceInfoList) {
			String str = info.getPort() + "-" + info.getIp() + "-" + info.getPingIntervalMillis() + "-"
					+ info.getPingTimestamp();
			infoListStr = infoListStr.equals("") ? str : (infoListStr + MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR + str);
		}
		return infoListStr;
	}

	private void cleanInstanceList() {
		if (this.instanceInfoList == null || this.instanceInfoList.isEmpty()) {
			return;
		}
		long ts = System.currentTimeMillis();
		List<InstanceInfo> infoList = new ArrayList<InstanceInfo>();
		for (InstanceInfo info : this.instanceInfoList) {
			if (info != null) {
				long maxInactiveInterval = MAX_PING_INACTIVE_INTERVAL_MILLISECOND;
				if ((ts - info.getPingTimestamp()) <= maxInactiveInterval) {
					infoList.add(info);
				} else {
					logger.fine("Remove this instance due to not received PING message more than "
							+ (MAX_PING_INACTIVE_INTERVAL_MILLISECOND / 1000) + " Seconds....Instance="
							+ info.getInstanceInfoPrintString());
				}
			}
		}
		if (infoList.isEmpty()) {
			infoList.add(this.mainInstanceInfo);
		}
		this.instanceInfoList = infoList;
	}

	private List<InstanceInfo> pingOrRegister() {
		// this method should not throw exception

		long pingTS = System.currentTimeMillis();
		if (myInstanceInfo.equals(mainInstanceInfo)) {
			logger.fine("This instance is the main instance, update ping timestamp only....."
					+ myInstanceInfo.getInstanceInfoPrintString());
			myInstanceInfo.setPingTimestamp(pingTS);
			mainInstanceInfo.setPingTimestamp(pingTS);
			int idx = -1;
			if ((idx = this.instanceInfoList.indexOf(mainInstanceInfo)) != -1) {
				InstanceInfo oldInfo = this.instanceInfoList.get(idx);
				oldInfo.setPingTimestamp(pingTS);
			} else {
				this.instanceInfoList.add(mainInstanceInfo);
			}
			String checkRequestInfoStr = FileUtil
					.getFileContentAsString(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_LIVE_CHECK_FILE_NAME);
			if (StringUtil.isEmptyOrNull(checkRequestInfoStr)) {
				// no body request.
				// delete the file if exists
				FileUtil.delete(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_LIVE_CHECK_FILE_NAME);
			} else {
				// there is main instance check request
				logger.info("There is main instance check request created by: " + checkRequestInfoStr);
				// update ping time stamp in the file
				// try 3 times
				String mainInfoStr = this.myInstanceInfo.getInstanceInfoString();
				int i = 0;
				boolean writeSucceed = false;
				while (i < 3) {
					try {
						logger.info("updateing main instance file......");
						FileUtil.writeStringToFile(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME, false,
								mainInfoStr);
						writeSucceed = true;
						logger.info("deleting main instance check request file......");
						FileUtil.delete(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_LIVE_CHECK_FILE_NAME);
						break;
					} catch (Throwable t) {
						i++;
						logger.severe("Can not update main instance file, will try again. try count: " + i, t);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							logger.finer("sleep interupted" + e);
						}
					}
				}
				if (!writeSucceed) {
					logger.severe("Can not write to main instance file, wait for next run...........");
				}
			}
			cleanInstanceList();
			logger.fine("Ping completed. Latest Instance List = " + getInstanceInfoListPrintString());
			// logger.fine("Status Cache = \n" +
			// MessageProcessingStatusCache.getCacheInfo(null));
			return this.instanceInfoList;
		}

		// this is not a main instance
		List<InstanceInfo> instanceList = new ArrayList<InstanceInfo>();
		myInstanceInfo.setPingTimestamp(pingTS);
		instanceList.add(myInstanceInfo);

		// create ping message
		SocketAdaptorMessage pingRequestMessage = null;
		SocketAdaptorMessage pingResponseMessage = null;
		pingRequestMessage = new SocketAdaptorMessage(MultiInstanceSocketSupportConstant.TYPE_PING, MultiInstanceSocketSupportConstant.TYPE_PING,
				myInstanceInfo.getInstanceName(), myInstanceInfo.getInstanceInfoString());

		// String mainInstanceResponseStr = null;
		// becasue this method will be run every defined interval, no need to add retry
		// in this method
		boolean successSend = false;
		try {
			pingResponseMessage = mainInstanceInfo.getSocketAdaptorManager().send(pingRequestMessage);
			if (pingResponseMessage != null) {
				successSend = true;
			}
			logger.finer("Received message object: " + pingResponseMessage);
		} catch (Throwable t) {
			logger.severe("Failed to establish connection to main instance ("
					+ mainInstanceInfo.getInstanceInfoPrintString() + "), Error: " + t, t);
		}
		if (successSend) {
			// process response
			if (!MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS
					.equalsIgnoreCase(pingResponseMessage.getTransferStatus())) {
				// message sent, but no good response, may have connection issue
				// do nothing, wait for next ping
				logger.severe("Error response message received, do nothing, wait for next ping. Messsage Received:\n"
						+ pingResponseMessage);
			} else {
				if (!pingResponseMessage.getProcessStatus()
						.equalsIgnoreCase(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS)) {
					// main instance can not process PING command successfully, it returns wrong
					// response
					// do not change infoList
					logger.severe(
							"main instance can not process PING successfully, wait for next ping. receive ping response message:\n"
									+ pingResponseMessage);
					instanceList.add(mainInstanceInfo);
				} else {
					// ping message has been processed successfully by main instance
					String[] contentArray = pingResponseMessage.getContent()
							.split(MultiInstanceSocketSupportConstant.CONTENT_SEPARATOR);
					for (int i = 0; i < contentArray.length; i++) {
						String infoStr = contentArray[i];
						InstanceInfo instanceInfo = new InstanceInfo(infoStr);
						if (mainInstanceInfo.equals(instanceInfo)) {
							instanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN);
						}
						if (!myInstanceInfo.equals(instanceInfo)) {
							instanceList.add(instanceInfo);
						}
					}
				}
			}
			logger.fine("Ping completed, Latest Instance List = " + getInstanceInfoListPrintString());
			// logger.fine("Ping completed. Status Cache = \n" +
			// MessageProcessingStatusCache.getCacheInfo(null));
		} else {
			// may need to change mainInstance.
			// main instance info from file
			String mainInstanceInfoStr = FileUtil
					.getFileContentAsString(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME);
			InstanceInfo instanceInfo = StringUtil.isEmptyOrNull(mainInstanceInfoStr) ? this.mainInstanceInfo
					: new InstanceInfo(mainInstanceInfoStr);

			if (this.mainInstanceInfo.equals(instanceInfo)) {
				// current main instance is same as main instance in file

				// check last main instance ping time
				if ((pingTS - instanceInfo.getPingTimestamp()) >= MAX_PING_INACTIVE_INTERVAL_MILLISECOND) {

					// it seems like main instance does not write ping timestamp to file for a while
					// request main instance response
					String myInfoStr = this.myInstanceInfo.getInstanceInfoString();

					String checkRequestorInstanceInfoStr = FileUtil
							.getFileContentAsString(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_LIVE_CHECK_FILE_NAME);
					if (StringUtil.isEmptyOrNull(checkRequestorInstanceInfoStr)) {
						// no one request check, I will request
						FileUtil.writeStringToFile(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_LIVE_CHECK_FILE_NAME,
								false, myInfoStr);
						logger.info("Created Main instance check request file, requester: " + myInfoStr);
					} else {
						// request has been made
						// do nothing
						logger.info(
								"Main instance check request file exists. Requester: " + checkRequestorInstanceInfoStr);
					}

					// wait for main instance response
					try {
						Thread.sleep(MAX_PING_INACTIVE_INTERVAL_MILLISECOND);
					} catch (InterruptedException e) {
						logger.finer("wait for main instance response, sleep is interupted......" + e);
					}

					// check whether main instance has responded or not
					String mInfoStr = FileUtil
							.getFileContentAsString(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME);
					InstanceInfo mInfo = StringUtil.isEmptyOrNull(mInfoStr) ? this.mainInstanceInfo
							: new InstanceInfo(mInfoStr);

					if (!this.mainInstanceInfo.equals(mInfo)) {
						// main instance has been changed while waiting
						changeMainInstance(mInfo, instanceList);
					} else {
						if ((System.currentTimeMillis()
								- mInfo.getPingTimestamp()) >= MAX_PING_INACTIVE_INTERVAL_MILLISECOND) {
							// there is no main instance response detected, retry to set this instance as
							// main instance
							try {
								FileUtil.writeStringToFile(MultiInstanceSocketSupportConstant.MAIN_INSTANCE_FILE_NAME, false,
										myInfoStr);
							} catch (Throwable t) {
								logger.fine(
										"Can not set this instance as main instance, wait for next ping..........." + t,
										t);
								return instanceList;
							}
							this.mainInstanceInfo = new InstanceInfo(myInfoStr);
							this.mainInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
							this.myInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
						} else {
							// main instance has responded, connection failure not caused by main instance
							logger.warning(
									"Can not ping main instance. However, main instance is running, do nothing, wait for next ping to retry......");
						}
					}
				} else {
					// main instance is still actively ping, do nothing
				}
			} else {
				// found the new main instance
				changeMainInstance(instanceInfo, instanceList);
			}

		}
		return instanceList;
	}

	private void changeMainInstance(InstanceInfo newInstanceInfo, List<InstanceInfo> newInfoList) {
		this.mainInstanceInfo = newInstanceInfo;
		if (!this.myInstanceInfo.equals(this.mainInstanceInfo)) {
			this.mainInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN);
			this.myInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_SELF);
			newInfoList.add(this.mainInstanceInfo);
		} else {
			this.mainInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
			this.myInstanceInfo.setAttribute(InstanceInfo.ATTRIBUTE_MAIN_SELF);
			// no need to add main instance info in the list
		}

	}

	public void stopPing() {
		pingEnabled = false;
		this.interrupt();
	}

	public void run() {
		logger.info("multi-instance socket support manager start to run......");
		while (pingEnabled) {
			try {
				List<InstanceInfo> instanceInfoList = pingOrRegister();
				if (instanceInfoList != null && !instanceInfoList.isEmpty()) {
					this.instanceInfoList = instanceInfoList;
				}
				try {
					Thread.sleep(this.pingIntervalMillisecond);
				} catch (Exception e) {
					logger.finer("sleep is interupted. " + e);
				}
			} catch (Throwable t) {
				pingEnabled = false;
				logger.severe(
						this.getName() + " stopped. Will not ping main instance any more........." + t.getMessage());
			}
		}
	}

	@Override
	public String addLock(String taskName, String key) {
		if (StringUtil.isEmptyOrNull(taskName)) {
			return null;
		}
		SocketAdaptorMessage requestMessage = new SocketAdaptorMessage(MultiInstanceSocketSupportConstant.TYPE_ADD_LOCK, taskName,
				myInstanceInfo.getInstanceName(), key);

		SocketAdaptorMessage responseMessage = this.mainInstanceInfo.getSocketAdaptorManager().send(requestMessage);

		if (MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getResponseStatus())
				&& MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getResponseStatus())
				&& MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getProcessStatus())) {
			return responseMessage.getContent();
		}
		return null;
	}

	@Override
	public boolean releaseLock(String taskName, String key) {
		if (StringUtil.isEmptyOrNull(taskName)) {
			return false;
		}
		SocketAdaptorMessage requestMessage = new SocketAdaptorMessage(MultiInstanceSocketSupportConstant.TYPE_RELEASE_LOCK,
				taskName, myInstanceInfo.getInstanceName(), key);

		SocketAdaptorMessage responseMessage = this.mainInstanceInfo.getSocketAdaptorManager().send(requestMessage);

		if (responseMessage != null
				&& MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getResponseStatus())
				&& MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getResponseStatus())
				&& MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS.equalsIgnoreCase(responseMessage.getProcessStatus())) {
			String content = responseMessage.getContent();
			if ("true".equalsIgnoreCase(content) || "false".equalsIgnoreCase(content)) {
				return Boolean.valueOf(content);
			}
		}
		return false;
	}

	@Override
	//socket implementation does not require a event key, each socket message has a unique message id. 
	//other multiple instance support implementation may require a event key (e.g. file based multiple instance support implementation
	public String broadcastEventMessage(String eventName, String key, String message) {
		if (StringUtil.isEmptyOrNull(eventName) || StringUtil.isEmptyOrNull(message)) {
			return null;
		}
		SocketAdaptorMessage requestMessage = new SocketAdaptorMessage(MultiInstanceSocketSupportConstant.TYPE_EVENT, eventName,
				myInstanceInfo.getInstanceName(), message);
		SocketAdaptorMessage responseMessage =  null;
		for (InstanceInfo info : this.instanceInfoList) {
			if (this.myInstanceInfo.equals(info)) {
				// not send to itself
				continue;
			}
			responseMessage = info.getSocketAdaptorManager().send(requestMessage);
		}
		return requestMessage.getId();
	}

	public List<EventStatus> inquiryEventStatus(String eventMessageId) {
		if (StringUtil.isEmptyOrNull(eventMessageId)) {
			return null;
		}
		List<EventStatus> statusList = new ArrayList<EventStatus>();
		SocketAdaptorMessage requestMessage = new SocketAdaptorMessage(MultiInstanceSocketSupportConstant.TYPE_INQUIRY_EVENT_STATUS,
				MultiInstanceSocketSupportConstant.NAME_NA, myInstanceInfo.getInstanceName(), eventMessageId);

		EventStatus status = null;
		for (InstanceInfo info : this.instanceInfoList) {
//			if (this.myInstanceInfo.equals(info)) {
//				// not send to self
//				continue;
//			}
			SocketAdaptorMessage responseMessage = info.getSocketAdaptorManager().send(requestMessage);

			if (responseMessage != null
					&& MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS
							.equalsIgnoreCase(responseMessage.getResponseStatus())
					&& MultiInstanceSocketSupportConstant.RESPONSE_STATUS_SUCCESS
							.equalsIgnoreCase(responseMessage.getResponseStatus())
					&& MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS
							.equalsIgnoreCase(responseMessage.getProcessStatus())) {

				String content = responseMessage.getContent();

				if (StringUtil.isEmptyOrNull(content)
						|| content.equalsIgnoreCase(MultiInstanceSocketSupportConstant.CONTENT_EMPTY)) {
					
					continue;
//					status = new EventStatus(eventMessageId, MultiInstanceSocketSupportConstant.NAME_NA,
//							MultiInstanceSocketSupportConstant.SOURCE_NA, info.getInstanceName());
//					status.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_NA);
//					status.setProcessedDate(null);
				} else {
					status = EventStatus.getStatusFromPrintString(content);
					if (status == null) {
						continue;
//						status = new EventStatus(eventMessageId, MultiInstanceSocketSupportConstant.NAME_NA,
//								MultiInstanceSocketSupportConstant.SOURCE_NA, info.getInstanceName());
//						status.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_NA);
//						status.setProcessedDate(null);
					}
				}
			} else {
				status = new EventStatus(eventMessageId, MultiInstanceSocketSupportConstant.NAME_NA, MultiInstanceSocketSupportConstant.SOURCE_NA,
						info.getInstanceName());
				status.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_NA);
				status.setProcessedDate(null);

			}
			statusList.add(status);
		}
		return statusList;

	}

	public SocketAdaptorMessage processMessage(SocketAdaptorMessage message) {
		// no exception will be thrown
		SocketAdaptorMessage responseMessage = null;
		if (message == null) {
			responseMessage = new SocketAdaptorMessage();
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
			responseMessage.setSource(this.myInstanceInfo.getInstanceName());
			return responseMessage;
		}
		responseMessage = SocketAdaptorMessage.createResponseMessage(message);
		responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
		try {
			if (MultiInstanceSocketSupportConstant.TYPE_PING.equalsIgnoreCase(message.getType())) {
				logger.finer("Main Instance Process PING request: " + message);
				String content = message.getContent();
				if (!StringUtil.isEmptyOrNull(content)) {
					InstanceInfo pingInfo = new InstanceInfo(content);
					pingInfo.setPingTimestamp(System.currentTimeMillis());
					int idx = -1;
					if ((idx = this.instanceInfoList.indexOf(pingInfo)) != -1) {
						InstanceInfo oldInfo = this.instanceInfoList.get(idx);
						oldInfo.setPingTimestamp(System.currentTimeMillis());
					} else {
						this.instanceInfoList.add(pingInfo);
					}
				}
				cleanInstanceList();
				responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
				responseMessage.setContent(getPingResponseInstanceInfoString());
				responseMessage.setSource(this.myInstanceInfo.getInstanceName());

				logger.fine("Ping message processed. Lasted Instance List = " + getInstanceInfoListPrintString());

				return responseMessage;

			} else if (MultiInstanceSocketSupportConstant.TYPE_ADD_LOCK.equalsIgnoreCase(message.getType())) {
				logger.finer("Main Instance - Process Add Lock request: " + message);
				String lockName = message.getName();
				String requester = message.getSource();
				String key = message.getContent();

				key = LockFileManager.addLock(lockName, requester, null, key);

				responseMessage = new SocketAdaptorMessage(message);
				responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
				responseMessage.setSource(this.myInstanceInfo.getInstanceName());
				responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
				responseMessage.setContent(key);

				logger.finer("Main Instance - Add Lock message processed......");

				return responseMessage;

			} else if (MultiInstanceSocketSupportConstant.TYPE_RELEASE_LOCK.equalsIgnoreCase(message.getType())) {
				logger.fine("Main Instance - Process Release Lock request: " + message);
				String lockName = message.getName();
				String requester = message.getSource();
				String key = message.getContent();

				boolean success = LockFileManager.releaseLock(lockName, requester, key);

				responseMessage = new SocketAdaptorMessage(message);
				responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
				responseMessage.setSource(this.myInstanceInfo.getInstanceName());
				responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
				responseMessage.setContent(Boolean.toString(success));

				logger.fine("Main Instance - Release Lock message processed......");

				return responseMessage;

			} else if (MultiInstanceSocketSupportConstant.TYPE_INQUIRY_EVENT_STATUS.equalsIgnoreCase(message.getType())) {
				logger.fine("Process event status inquiry request, requested event message Id: " + message.getContent());
				
				logger.finer("Current MessageProcessingStatusCache = \n" + MessageProcessingStatusCache.getCacheInfoString(null));
				
				String messageId = message.getContent();
				
				List<MessageProcessingStatus> messageStatusList = MessageProcessingStatusCache
						.getCacheedStatusList(messageId);
				
				List<EventStatus> evenStatusList = new ArrayList<>();
				EventStatus eventStatus = null;

				responseMessage = new SocketAdaptorMessage(message);
				responseMessage.setTransferStatus(MultiInstanceSocketSupportConstant.TRANSFER_STATUS_SUCCESS);
				responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);

				if (messageStatusList == null || messageStatusList.isEmpty()) {
					// not found
					responseMessage.setSource(this.myInstanceInfo.getInstanceName());
					responseMessage.setContent(MultiInstanceSocketSupportConstant.CONTENT_EMPTY);

				} else {
					for (MessageProcessingStatus mStatus : messageStatusList) {
						if (mStatus != null
								&& mStatus.getDestination().equalsIgnoreCase(this.myInstanceInfo.getInstanceName())) {
							//this is message receiver
							eventStatus = new EventStatus();
							eventStatus.setMessageId(messageId);
							eventStatus.setDestination(mStatus.getDestination());
							eventStatus.setName(mStatus.getName());
							eventStatus.setProcessStatus(mStatus.getProcessStatus(), mStatus.getProcessedDate());
							eventStatus.setSource(mStatus.getSource());

							responseMessage.setSource(this.myInstanceInfo.getInstanceName());
							responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
							responseMessage.setContent(eventStatus.getPrintString());
							return responseMessage;
						}
					}
					for (MessageProcessingStatus mStatus : messageStatusList) {
						if (mStatus != null
								&& mStatus.getSource().equalsIgnoreCase(this.myInstanceInfo.getInstanceName())) {
							//the message sender is this instance
							eventStatus = new EventStatus();
							eventStatus.setMessageId(messageId);
							eventStatus.setDestination(mStatus.getSource());
							eventStatus.setName(mStatus.getName());
							eventStatus.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_NA);
							eventStatus.setSource(mStatus.getSource());

							responseMessage.setSource(this.myInstanceInfo.getInstanceName());
							responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS);
							responseMessage.setContent(eventStatus.getPrintString());
							return responseMessage;
						}
					}

				}

				responseMessage.setSource(this.myInstanceInfo.getInstanceName());
				responseMessage.setContent(MultiInstanceSocketSupportConstant.CONTENT_EMPTY);

				logger.fine("Process event status inquiry end. ");

				return responseMessage;

			} else {
				// start a new thread to process the message
				new MessageProcessor(this.socketMessageProcessor, message, this.myInstanceInfo.getInstanceName()).run();

				String msg = "Message received successfully, processor is processing the message.";
				responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_RECEIVED);
				responseMessage.setContent(msg);

				logger.finer(msg);

				return responseMessage;
			}

		} catch (Throwable t) {
			String err = "Failed to process message. Error: " + t;
			responseMessage.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
			responseMessage.setContent(err);
			return responseMessage;
		}
	}

	private class MessageProcessor extends Thread {
		private final CommonLogger logger = CommonLogger.getLogger(MessageProcessor.class.getName());

		private SocketEventProcessorI processor = null;
		private SocketAdaptorMessage message = null;
		private String instanceName = null;

		public MessageProcessor(SocketEventProcessorI processor, SocketAdaptorMessage message, String instanceName) {
			super("MessageProcessor-Thread_Count#" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS"));
			this.processor = processor;
			this.message = message;
			this.instanceName = instanceName;
		}

		public void run() {
			logger.finer(this.getName() + " receiving and processing message: " + message);
			MessageProcessingStatus status = new MessageProcessingStatus(message.getId(), message.getSource(),
					instanceName);
			try {
				SocketAdaptorMessage responseMessage = this.processor.process(message);
				status.setProcessStatus(responseMessage.getProcessStatus());
				MessageProcessingStatusCache.addOrUpdate(status);
				logger.finer("process message successfuly, returned message: " + responseMessage);
			} catch (Throwable t) {
				status.setProcessStatus(MultiInstanceSocketSupportConstant.PROCESS_STATUS_ERROR);
				logger.severe("process message failed: " + t, t);
			} finally {
				logger.finer(this.getName() + "...run().....end.........");
			}
		}
	}

}
