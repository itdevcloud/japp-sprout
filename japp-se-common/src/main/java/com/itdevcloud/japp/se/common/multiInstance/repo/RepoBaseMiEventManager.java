package com.itdevcloud.japp.se.common.multiInstance.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public abstract class RepoBaseMiEventManager extends Thread {
	private static final Logger logger = Logger.getLogger(RepoBaseMiEventManager.class.getName());

	protected static boolean eventMonitorEnabled = false;

	protected String appName = null;
	protected String myInstanceName = null;

	private int processRetryCount = EventManagerConstant.DEFAULT_PROCESSOR_RETRY_COUNT;
	private int processRetryIntervalMillis = EventManagerConstant.DEFAULT_PROCESSOR_RETRY_INTERVAL_MILLIS;
	private int processStatusRetetionDay = EventManagerConstant.DEFAULT_PROCESS_STATUS_RETENTION_DAY;
	private int monitorIntervalMills = EventManagerConstant.DEFAULT_MONITOR_CHECK_INTERVAL_MILLIS;
	protected int appRepoLockTimeoutMillis = EventManagerConstant.DEFAULT_REPO_LOCK_TIMEOUT_MILLIS;
	protected int appRepoLockRetryIntervalMillis = EventManagerConstant.DEFAULT_REPO_LOCK_RETRY_INTERVAL_MILLIS;

	private long lastProcessTimestamp = -1;
	private long monitorStartTimestamp = -1;

	private EventProcessor eventProcessor = null;
	private List<EventInfo> processedEventList = null;

	protected EmAppRepoLockService appRepoLockService = null;
	protected EmTaskLockService taskLockService = null;
	protected EmBroadcastService broadcastService = null;
	protected EmProcessorService processorService = null;
	protected EmDataService dataService = null;
	protected EmRepoService repoService = null;

	public RepoBaseMiEventManager(String appName) {
		init(appName, null, null);
	}

	public RepoBaseMiEventManager(String appName, String myInstanceName, EventProcessor eventProcessor) {
		init(appName, myInstanceName, eventProcessor);
	}

	private void init(String appName, String myInstanceName, EventProcessor eventProcessor) {

		logger.info(this.getClass().getSimpleName() + " init start........");
		Long startTS = System.currentTimeMillis();

		appName = StringUtil.isEmptyOrNull(appName) ? "ALLAPPS" : appName.trim().toUpperCase();
		this.appName = appName;

		setMyInstanceName(myInstanceName);
		setEventProcessor(eventProcessor);
		setProcessRetryCount(-1);
		setProcessStatusRetetionDay(-1);
		setProcessRetryIntervalMillis(-1);
		setAppRepoLockTimeoutMillis(-1);
		setAppRepoLockRetryIntervalMillis(-1);
		setMonitorIntervalMills(-1);

		// thread name
		String nowStr = DateUtils.dateToString(new Date(), EventManagerConstant.ID_DATE_FORMAT);
		String tName = this.getClass().getSimpleName() + " Monitor Tread [" + this.appName + "-" + nowStr + "]";
		this.setName(tName);

		this.eventMonitorEnabled = false;
		this.monitorStartTimestamp = System.currentTimeMillis();
		this.lastProcessTimestamp = this.monitorStartTimestamp;
		this.processedEventList = new ArrayList<EventInfo>();

		this.appRepoLockService = new EmAppRepoLockService(this);
		this.taskLockService = new EmTaskLockService(this);
		this.broadcastService = new EmBroadcastService(this);
		this.processorService = new EmProcessorService(this);
		this.dataService = new EmDataService(this);
		this.repoService = new EmRepoService(this);

		stopEventMonitor();

		logger.info("EventInfo String format: " + EventManagerConstant.EVENT_INFO_STRING_FORMAT);
		logger.info("EventInfo: <EventProcessStatus> String format: "
				+ EventManagerConstant.EVENT_INFO_PROCESS_STATUS_STRING_FORMAT);
		logger.info("EventInfo: <DataInfo> String format: " + EventManagerConstant.DATA_INFO_STRING_FORMAT);

		Long endTS = System.currentTimeMillis();
		logger.info(this.getClass().getSimpleName() + " init  end.....total millis = " + (endTS - startTS));

	}

	@Override
	public String toString() {
		String tsString = DateUtils.timestampToDateString(monitorStartTimestamp, "yyyy-MM-dd HH:mm:ss.SSS");
		return this.getClass().getSimpleName() + " [appName=" + appName + ", myInstanceName=" + myInstanceName
				+ ", processRetryCount=" + processRetryCount + ", processRetryIntervalMillis="
				+ processRetryIntervalMillis + ", processStatusRetetionDay=" + processStatusRetetionDay
				+ ", appRepoLockTimeoutMillis=" + appRepoLockTimeoutMillis + ", monitorIntervalMills="
				+ monitorIntervalMills + ", monitoStartTimestamp=" + monitorStartTimestamp + "(" + tsString + ") ]";
	}

	protected abstract boolean isConnected();

	protected abstract void close();

	protected abstract void reset(String appName);

	protected abstract boolean useAppRepoLock();

	protected abstract List<String> getContentFromRepo(String repoName);

	protected abstract boolean saveContentToRepo(String repoName, List<String> contentList, boolean append);

	protected abstract long getEventLastUpdateTimeFromRepo(String eventType);

	protected abstract boolean setEventLastUpdateTimeInRepo(String eventType);

	protected abstract String getRepoName(String repoType, String eventType);

	public void stopEventMonitor() {
		eventMonitorEnabled = false;
	}

	public void startEventMonitor() {
		eventMonitorEnabled = true;
		this.start();
	}

	public void run() {

		logger.info(this.getClass().getSimpleName() + " Monitor started, thread name = " + this.getName());

		this.monitorStartTimestamp = System.currentTimeMillis();
		this.lastProcessTimestamp = this.monitorStartTimestamp;

		try {
			List<EventInfo> list = null;
			while (eventMonitorEnabled) {
				try {
					// check manager is in good state or not, if not, retry connection
					while (eventMonitorEnabled && !this.isConnected()) {
						try {
							logger.info("Connectiviy is not established, waiting for "
									+ (EventManagerConstant.DEFAULT_CONNECTION_RETRY_INTERVAL_MINS )
									+ " minutes to retry...");
							Thread.sleep(EventManagerConstant.DEFAULT_CONNECTION_RETRY_INTERVAL_MINS*60*1000);
							this.reset(this.appName);
						} catch (Throwable t) {
						}
					}
					// try to process (find and process)
					this.processorService.process();
					try {
						Thread.sleep(this.monitorIntervalMills);
					} catch (Throwable t) {
					}

				} catch (Throwable t) {
					logger.info(this.getClass().getSimpleName() + " Monitor process event ERROR: " + t);
				}
			} // end while
			logger.info(this.getClass().getSimpleName() + " Monitor stopped, thread name = " + this.getName());

		} catch (Throwable t) {
			logger.severe(this.getName() + "  Error Detected: " + t.getMessage());
			t.printStackTrace();
		} finally {
			this.close();
		}
	}

	// === task lock service ===
	public EventInfo addTaskLock(String lockName, int expireMinutes) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return null;
		}
		return this.taskLockService.addTaskLock(lockName, expireMinutes);
	}

	public boolean releaseTaskLock(EventInfo lockEventInfo) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return false;
		}
		return this.taskLockService.releaseTaskLock(lockEventInfo);
	}

	// === Broadcaset service ===
	public boolean broadcastEventMessage(String eventName, String message) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return false;
		}
		return this.broadcastService.broadcastEventMessage(eventName, message);
	}

	// === Data service ===
	protected String getDataRepoName(String dataEventType) {
		return this.dataService.getDataRepoName(dataEventType);
	}

	public List<DataInfo> retrieveData(String dataEventType) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return null;
		}
		return this.dataService.retrieveDataFromRepo(dataEventType);
	}

	public boolean saveData(String dataEventType, List<DataInfo> dataInfoList, boolean append) {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return false;
		}
		return this.dataService.saveDataToRepo(dataEventType, dataInfoList, append);
	}

	public EventInfo createEventInfo(String eventType) {
		if (eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			if (eventType.length() <= EventManagerConstant.EVENT_TYPE_DATA.length()) {
				eventType = EventManagerConstant.EVENT_TYPE_DATA + "ALL";
			}
		}
		EventInfo eventInfo = null;
		if (eventType != null && EventManagerConstant.EVENT_TYPE_PROCESS_STATUS.equalsIgnoreCase(eventType)) {
			eventInfo = new EventProcessStatus();
		} else if (eventType != null && eventType.toUpperCase().startsWith(EventManagerConstant.EVENT_TYPE_DATA)) {
			eventInfo = new DataInfo(this.appName, null);
		} else {
			eventInfo = new EventInfo(this.appName, null, this.myInstanceName);
		}
		return eventInfo;
	}

	public List<EventInfo> getTaskLockList() {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return null;
		}
		return this.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_LOCK);
	}

	public List<EventInfo> getBroadcastEventList() {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return null;
		}
		return this.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_BROADCAST);
	}

	public List<EventInfo> getProcessStatusList() {
		if(!this.isConnected()) {
			logger.warning(this.getClass().getSimpleName() + " is not conntected, do nothing!");
			return null;
		}
		return this.repoService.getEventInfoListFromRepo(EventManagerConstant.EVENT_TYPE_PROCESS_STATUS);
	}

	public String getAppName() {
		return appName;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor == null ? new EventProcessor() : eventProcessor;
	}

	public String getMyInstanceName() {
		return myInstanceName;
	}

	public void setMyInstanceName(String myInstanceName) {
		String nowStr = DateUtils.dateToString(new Date(), EventManagerConstant.ID_DATE_FORMAT);
		myInstanceName = StringUtil.isEmptyOrNull(myInstanceName) ? (CommonUtil.getMyFirstLocalIp(null) + "-" + nowStr)
				: myInstanceName.trim().toUpperCase();
		this.myInstanceName = myInstanceName;
	}

	public void setProcessRetryIntervalMillis(int processRetryIntervalMillis) {
		this.processRetryIntervalMillis = processRetryIntervalMillis < 0
				? EventManagerConstant.DEFAULT_PROCESSOR_RETRY_INTERVAL_MILLIS
				: (processRetryIntervalMillis < 500 ? 500 : processRetryIntervalMillis);
	}

	public void setMonitorIntervalMills(int monitorIntervalMills) {
		this.monitorIntervalMills = monitorIntervalMills < 0
				? EventManagerConstant.DEFAULT_MONITOR_CHECK_INTERVAL_MILLIS
				: (monitorIntervalMills < 500 ? 500 : monitorIntervalMills);
	}

	public void setProcessRetryCount(int processRetryCount) {
		processRetryCount = processRetryCount < 0 ? EventManagerConstant.DEFAULT_PROCESSOR_RETRY_COUNT
				: processRetryCount;
		if (processRetryCount > EventManagerConstant.DEFAULT_PROCESSOR_RETRY_MAX_COUNT) {
			processRetryCount = EventManagerConstant.DEFAULT_PROCESSOR_RETRY_MAX_COUNT;
		}
		this.processRetryCount = processRetryCount;
	}

	public void setProcessStatusRetetionDay(int processStatusRetetionDay) {
		this.processStatusRetetionDay = processStatusRetetionDay <= 0
				? EventManagerConstant.DEFAULT_PROCESS_STATUS_RETENTION_DAY
				: processStatusRetetionDay;
	}

	public void setAppRepoLockTimeoutMillis(int appRepoLockTimeoutMillis) {
		if (appRepoLockTimeoutMillis <= 0) {
			appRepoLockTimeoutMillis = EventManagerConstant.DEFAULT_REPO_LOCK_TIMEOUT_MILLIS;
		}
		this.appRepoLockTimeoutMillis = appRepoLockTimeoutMillis;
	}

	public void setAppRepoLockRetryIntervalMillis(int appRepoLockRetryIntervalMillis) {
		if (appRepoLockRetryIntervalMillis <= 0) {
			appRepoLockRetryIntervalMillis = EventManagerConstant.DEFAULT_REPO_LOCK_RETRY_INTERVAL_MILLIS;
		}
		this.appRepoLockRetryIntervalMillis = appRepoLockRetryIntervalMillis;
	}

	protected long getLastProcessTimestamp() {
		return lastProcessTimestamp;
	}

	protected void setLastProcessTimestamp(long lastProcessTimestamp) {
		this.lastProcessTimestamp = lastProcessTimestamp;
	}

	public List<EventInfo> getProcessedEventList() {
		return processedEventList;
	}

	protected void setProcessedEventList(List<EventInfo> processedEventList) {
		this.processedEventList = processedEventList;
	}

	protected int getProcessRetryCount() {
		return processRetryCount;
	}

	protected int getProcessRetryIntervalMillis() {
		return processRetryIntervalMillis;
	}

	protected int getProcessStatusRetetionDay() {
		return processStatusRetetionDay;
	}

	protected int getMonitorIntervalMills() {
		return monitorIntervalMills;
	}

	protected int getAppRepoLockTimeoutMillis() {
		return appRepoLockTimeoutMillis;
	}

	protected int getAppRepoLockRetryIntervalMillis() {
		return appRepoLockRetryIntervalMillis;
	}

	protected EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	protected long getMonitorStartTimestamp() {
		return monitorStartTimestamp;
	}

}
