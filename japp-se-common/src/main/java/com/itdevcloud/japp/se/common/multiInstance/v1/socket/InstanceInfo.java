package com.itdevcloud.japp.se.common.multiInstance.v1.socket;

import java.util.Objects;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class InstanceInfo {

	private static final JulLogger logger = JulLogger.getLogger(InstanceInfo.class.getName());
	
	public static final String ATTRIBUTE_SELF = "Self";
	public static final String ATTRIBUTE_MAIN = "Main";
	public static final String ATTRIBUTE_MAIN_SELF = "Main/Self";
	public static final String ATTRIBUTE_OTHER = "Other";

	private String ip;
	private int port = -1;
	private SocketAdaptorManager socketAdaptorManager;
	private int pingIntervalMillis;
	private long pingTimestamp;
	private String attribute;

	public static String createInstanceName(int port, String host) {
		return port + "-" + host;

	}

	public InstanceInfo() {
		super();
		setInstanceInfo(null, -1, -1, -1);

	}

	public InstanceInfo(String ip, int port, int pingIntervalMillis, int pingTimestamp) {
		super();
		setInstanceInfo(ip, port, pingIntervalMillis, pingTimestamp);
	}

	public InstanceInfo(String instanceInfoStr) {
		super();
		setInstanceInfo(instanceInfoStr);
	}

	// info string format: port-ip-pingIntervalMillis-pingTS
	public void setInstanceInfo(String infoStr) {
		if (StringUtil.isEmptyOrNull(infoStr)) {
			setInstanceInfo(null, -1, -1, -1);
		}
		try {
			String[] infoArr = infoStr.split("-");
			int port = Integer.parseInt(infoArr[0]);
			String ip = infoArr.length <= 1 ? null : infoArr[1];
			String pingIntervalStr = infoArr.length <= 2 ? null : infoArr[2];
			int pingInterval = -1;
			if (!StringUtil.isEmptyOrNull(pingIntervalStr)) {
				try {
					pingInterval = Integer.parseInt(pingIntervalStr.trim());
				} catch (Exception e) {
					logger.warning("wrong ping interval (" + pingIntervalStr + ") detected when creating InstanceInfo object, use default setting. Error: " + e);
					pingInterval = -1;
				}
			}
			String pingTSStr = infoArr.length <= 3 ? null : infoArr[3];
			//System.out.println("setInstanceInfo().......pingTSStr..."+pingTSStr);
			long pingTS = -1;
			if (!StringUtil.isEmptyOrNull(pingTSStr)) {
				try {
					pingTS = Long.parseLong(pingTSStr.trim());
				} catch (Exception e) {
					logger.warning("wrong ping timestamp (" + pingTSStr + ") detected when creating InstanceInfo object, use default setting. Error: " + e);
					pingTS = -1;
				}
			}
			setInstanceInfo(ip, port, pingInterval, pingTS);

		} catch (Exception e) {
			logger.fine("Error: " + e, e);
			setInstanceInfo(null, -1, -1, -1);
		}
	}
	private void setInstanceInfo(String ip, int port, int pingIntervalMillis, long pingTimestamp) {
		
		if(StringUtil.isEmptyOrNull(ip)) {
			ip = CommonUtil.getMyFirstLocalIp(null);
		}
		this.ip = ip;
		if(port < 1024 || port > 65535) {
			logger.warning("port # " + port + " is not a valid one, use default setting. Invalid port:" + port);
			port = -1;
		}
		this.port = port;
		this.attribute = ATTRIBUTE_OTHER;
		if (this.socketAdaptorManager != null) {
			this.socketAdaptorManager.interrupt();
		}

		this.socketAdaptorManager = new SocketAdaptorManager(this.ip, this.port);
		//port or host could be changed
		this.port = socketAdaptorManager.getPort();
		this.ip = socketAdaptorManager.getHost();
		
		this.pingIntervalMillis = pingIntervalMillis;
		this.pingTimestamp = pingTimestamp;
	}

	public String getInstanceName() {
		return InstanceInfo.createInstanceName(this.port, this.ip);

	}
	public String getInstanceInfoString() {
		return this.port + "-" + this.ip + "-" + this.pingIntervalMillis + "-" + this.pingTimestamp;
	}
	public String getInstanceInfoPrintString() {
		return this.attribute + "-" + this.port + "-" + this.ip + "-" + this.pingIntervalMillis + "-" + this.pingTimestamp;

	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public SocketAdaptorManager getSocketAdaptorManager() {
		return socketAdaptorManager;
	}

	public int getPingIntervalMillis() {
		return pingIntervalMillis;
	}

	public long getPingTimestamp() {
		return pingTimestamp;
	}

	public void setPingTimestamp(long pingTimestamp) {
		this.pingTimestamp = pingTimestamp;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstanceInfo other = (InstanceInfo) obj;
		return Objects.equals(ip, other.ip) && port == other.port;
	}

	@Override
	public String toString() {
		return "InstanceInfo [Attribute=" + attribute + ", ip=" + ip + ", port=" + port + ", socketAdaptorManager="
				+ (socketAdaptorManager == null ? null : socketAdaptorManager.getName()) + ", pingIntervalMillis=" + pingIntervalMillis + ", pingTimestamp="
				+ pingTimestamp + "]";
	}

}
