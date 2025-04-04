package com.itdevcloud.japp.se.common.multiInstance.v1.file;

import java.io.File;
/**
 * this class provide a simple file based support for multiple instances running environment
 * the main assumption is that to use this class, all instances can access same files stored in a central place.
 * 
 * if the assumption can not be applied to the target environment, this class can be changed/enhanced to support other type of central repositories, such as:
 * - database table, Azure storage account, Key Vault, Central Cache etc.
 * 
 * Logics in different implementations should be similar and following 3 functions are good enough for most cases:
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

 * - addEvent()
 * 		Note: 
 * 			for event, name + key are unique. key can be provided by client otherwise the key will be generated automatically
 *			default expire date for event is now + 24 hours
 * 		basic process logic: lock file --> clean up expired event --> add the event info if the is no same name and key found in the file --> close file
 * 
 * About event monitoring
 * this class implements Thread, once started, it will monitor whether the event file is changed or not, if the event file
 * is changed and there is unprocessed event identified, it will call EventProcessor.proccessEvent() to process each event one by one.
 * 
 * Note: this class will ignore all events happened before the class is started(i.e. All events before the instance started are treated as processed events).
 * 
 * Example:
 * 		FileSupportManager manager = new FileSupportManager();
 *		
 *		String lockName = "DailyReport-A";
 *		Date today = new Date();
 *		Date expiryDate = CommonUtil.addTime(today, Calendar.MINUTE, 10);
 *		String key = null;
 *		
 *		key = manager.addLock(lockName, expiryDate, key);
 *
 *		lockName = "MonthlyReport-B";
 *		key = "12345";
 *		key = manager.addLock(lockName, null, key);
 *		
 *		String eventName = "UserChangeEvent";
 *		expiryDate = CommonUtil.addTime(today, Calendar.HOUR, 24);
 *		key = null;
 *		String content = "abcde^John@ontaio.ca";
 *		key = manager.addEvent(eventName, expiryDate, key, content);
 *
 * @author Marvin Sun
 * @since 1.0.0
*
 */
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.RandomUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class MultiInstanceFileSupportManager extends Thread {
	private static final JulLogger logger = JulLogger.getLogger(MultiInstanceFileSupportManager.class.getName());

	private static List<EventLockInfo> processedEventList = null;
	private static boolean eventMonitorEnabled = true;

	private static boolean useWatchService = true;
	// only used when useWatchService is false
	private static long lastModifiedTime = -1;
	private static long fileCheckIntervalMills = 3000;

	public MultiInstanceFileSupportManager(boolean useWatchService) {
		super("FileSupportManager: Tread #" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS"));
		init(useWatchService, this.fileCheckIntervalMills);
	}

	public MultiInstanceFileSupportManager(long fileCheckIntevalMills) {
		super("FileSupportManager: Tread #" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS"));
		init(false, fileCheckIntevalMills);
	}

	private void init(boolean useWatchService, long fileCheckIntevalMills) {

		// set logger level
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.FINE);
		for (Handler h : rootLogger.getHandlers()) {
			h.setLevel(Level.FINE);
		}
		logger.fine(this.getName() + " init start.........");
		logger.fine(this.getName() + " init start.........useWatchService = " + useWatchService
				+ ", fileCheckIntevalMills = " + fileCheckIntevalMills);
		this.useWatchService = useWatchService;
		this.fileCheckIntervalMills = (fileCheckIntevalMills < 300 ? 300 : fileCheckIntevalMills);

		// do not care events sent before this instance start
		processedEventList = initProcessedEventInfoList();

		this.lastModifiedTime = System.currentTimeMillis();

		logger.info("EventLockInfo String format: " + EventLockInfo.eventLockInfoStringFormat);

		logger.info(this.getName() + " init end.");

		this.start();
	}

	// name is the unqiue identifier, key is used to release a lock
	// return null means lock failed.
	// default expire date is now + 5 mins
	public static synchronized String addLock(String name, Date expiryDate, String key) {
		if (StringUtil.isEmptyOrNull(name)) {
			return null;
		}
		// key will not be null or empty
		key = StringUtil.isEmptyOrNull(key) ? RandomUtil.generateAlphanumericString("l-", 5) : key;
		EventLockInfo eventLockInfo = new EventLockInfo(name, expiryDate, key);

		FileChannel channel = null;
		FileLock lock = null;
		List<EventLockInfo> lineInfoList = new ArrayList<EventLockInfo>();
		Date now = new Date();

		RandomAccessFile file = null;
		try {
			try {
				// logger.fine("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
				return null;
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no locks found
				newFileContent = eventLockInfo.getLineInfoString();
			} else {
				String[] fileContentArr = fileContent.split("\n");
				EventLockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						info = EventLockInfo.createLineInfoFromString(infoStr);
						// auto cleaning -- remove expired lock
						if (info.getExpiryDate().after(now)) {
							lineInfoList.add(info);
						}
					} catch (Throwable t) {
						// ignore this eventLockInfo String, continue;
						key = null;
						info = null;
						logger.severe("Failed to create EventLockInfo object, check lock file content: "
								+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
					}
				}
				// check existing lock
				int idx = lineInfoList.indexOf(eventLockInfo);
				if (idx < 0) {
					// no same lock exists
					lineInfoList.add(eventLockInfo);

				} else {
					logger.finer("same lock detected, lock failed......");
					key = null;
				}
				int i = 1;
				for (EventLockInfo newInfo : lineInfoList) {
					if (i == 1) {
						newFileContent = newInfo.getLineInfoString();
					} else {
						newFileContent = newFileContent + "\n" + newInfo.getLineInfoString();
					}
					i++;
				}
			}

			// write back to lock file for the new lock list
			try {
				FileUtil.writeStringToFile(channel, true, newFileContent);
				if (key != null) {
					logger.info("add lock successfully for lock name = " + name + ", key = " + key);
				} else {
					logger.info("add lock failed for lock name = " + name);

				}
				// for debug block file lock request from other process
				// run other thread to to apply lock while this one is sleeping, the other
				// thread should be blocked.
//				logger.fine("sleep 60 seconds.......");
//				Thread.sleep(60000);

				return key;

			} catch (Throwable t) {
				key = null;
				logger.severe("Failed to write back to lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
			}
		} catch (Throwable t) {
			key = null;
			logger.severe("Failed to apply lock: " + t);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					logger.severe("Failed to close lock file channel: " + e);
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (IOException e) {
					logger.severe("Failed to close lock file: " + e);
				}
			}
		}
		return key;
	}

	// lock releaser must know name and key
	public static synchronized boolean releaseLock(String name, String key) {
		if (StringUtil.isEmptyOrNull(name)) {
			return false;
		}
		EventLockInfo lineInfo = new EventLockInfo(name, null, key);
		FileChannel channel = null;
		FileLock lock = null;
		List<EventLockInfo> lineInfoList = new ArrayList<EventLockInfo>();
		Date now = new Date();
		RandomAccessFile file = null;
		try {
			try {
				// logger.fine("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no locks found
				return true;
			} else {
				String[] fileContentArr = fileContent.split("\n");
				EventLockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						info = EventLockInfo.createLineInfoFromString(infoStr);
						// auto cleaning -- remove expired lock
						if (info.getExpiryDate().after(now)) {
							lineInfoList.add(info);
						}
					} catch (Throwable t) {
						// ignore this lock string
						info = null;
						logger.severe("Failed to create LockInfo object, check lock file content: "
								+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
					}
				}
				// check existing lock
				int idx = lineInfoList.indexOf(lineInfo);
				if (idx >= 0) {
					String tmpKey = lineInfoList.get(idx).getKey();
					if (key != null && key.equals(tmpKey)) {
						lineInfoList.remove(idx);
					} else {
						// key is different, do nothing
						return false;
					}
				} else {
					// no same lock exists, do nothing
					return false;
				}
				int i = 1;
				for (EventLockInfo newInfo : lineInfoList) {
					if (i == 1) {
						newFileContent = newInfo.getLineInfoString();
					} else {
						newFileContent = newFileContent + "\n" + newInfo.getLineInfoString();
					}
					i++;
				}
			}

			// write back to lock file for the new lock list
			try {
				FileUtil.writeStringToFile(channel, true, newFileContent);
				logger.info("release lock successfully for lock name = " + name + ", key = " + key);

				// for debug block file lock request from other process
				// run other thread to to apply lock while this one is sleeping, the other
				// thread should be blocked.
//				logger.fine("sleep 60 seconds.......");
//				Thread.sleep(60000);

				return true;

			} catch (Throwable t) {
				logger.severe("Failed to write back to lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
			}
		} catch (Throwable t) {
			logger.severe("Failed to release lock: " + t);
		} finally {
			if (channel != null) {
				try {
					channel.close();
					channel = null;
				} catch (IOException e) {
					logger.severe("Failed to close lock file channel: " + e);
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (IOException e) {
					logger.severe("Failed to close lock file: " + e);
				}
			}
		}
		return false;
	}

	// event list can have same event name, but event name + event key must unique
	// in the list
	// default expire date is now + 24 hours
	public static synchronized String addEvent(String name, Date expiryDate, String key, String content) {
		if (StringUtil.isEmptyOrNull(name)) {
			return null;
		}

		key = StringUtil.isEmptyOrNull(key) ? RandomUtil.generateAlphanumericString("e-", 5) : key;
		EventLockInfo lineInfo = new EventLockInfo(name, expiryDate, key);
		lineInfo.setContent(content);

		FileChannel channel = null;
		FileLock lock = null;
		List<EventLockInfo> lineInfoList = new ArrayList<EventLockInfo>();
		Date now = new Date();
		RandomAccessFile file = null;
		try {
			try {
				// logger.fine("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
				return null;
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no event found
				newFileContent = lineInfo.getLineInfoString();
			} else {
				String[] fileContentArr = fileContent.split("\n");
				EventLockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						info = EventLockInfo.createLineInfoFromString(infoStr);
						// auto cleaning - remove expired lock
						if (info.getExpiryDate().after(now)) {
							lineInfoList.add(info);
						}
					} catch (Throwable t) {
						// ignore this event string
						key = null;
						info = null;
						logger.severe("Failed to create LockInfo object, check lock file content: "
								+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
					}
				}
				// check existing event
				boolean found = false;
				for (int i = 0; i < lineInfoList.size(); i++) {
					info = lineInfoList.get(i);
					// check name+key
					if (name.equalsIgnoreCase(info.getName()) && key.equalsIgnoreCase(info.getKey())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// no same event exists
					lineInfoList.add(lineInfo);

				} else {
					// logger.fine("same event detected......");
					key = null;
				}
				int i = 1;
				for (EventLockInfo newInfo : lineInfoList) {
					if (i == 1) {
						newFileContent = newInfo.getLineInfoString();
					} else {
						newFileContent = newFileContent + "\n" + newInfo.getLineInfoString();
					}
					i++;
				}
			}

			// write back to lock file for the new lock list
			try {
				FileUtil.writeStringToFile(channel, true, newFileContent);
				if (key != null) {
					logger.info("add event successfully for name = " + name + ", key = " + key);
				} else {
					logger.info("add event failed for name = " + name);

				}
				// for debug block file event request from other process
//				logger.fine("sleep 60 seconds.......");
//				Thread.sleep(60000);

				return key;

			} catch (Throwable t) {
				key = null;
				logger.severe("Failed to write back to event file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
			}
		} catch (Throwable t) {
			key = null;
			logger.severe("Failed to addevent lock: " + t);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					logger.severe("Failed to close event file channel: " + e);
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (IOException e) {
					logger.severe("Failed to close event file: " + e);
				}
			}
		}
		return key;
	}

	private static synchronized boolean addProcessedEvent(EventLockInfo info) {
		if (info == null) {
			logger.fine("Event Info is null, do nothing.........");
			return false;
		}
		boolean found = false;
		for (EventLockInfo tmpInfo : processedEventList) {
			// logger.fine("tmpInfo.getName()=" + tmpInfo.getName() + "---info.getName()="
			// +info.getName() + ", tmpInfo.getKey()=" + tmpInfo.getKey() +
			// "---info.getKey()="+info.getKey());

			if (tmpInfo.getName().equalsIgnoreCase(info.getName())
					&& tmpInfo.getKey().equalsIgnoreCase(info.getKey())) {
				found = true;
			}
		}
		if (!found) {
			processedEventList.add(info);
			logger.info("the Event Info is added to the processed list: " + info);
			return true;
		} else {
			logger.info("the Event Info can not be added to the processed list: " + info);

		}
		return false;
	}

	private static synchronized List<EventLockInfo> getUnProccessedEventInfoList() {

		FileChannel channel = null;
		FileLock lock = null;
		List<EventLockInfo> lineInfoList = new ArrayList<EventLockInfo>();
		Date now = new Date();
		RandomAccessFile file = null;
		try {
			try {
				logger.finer("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
				return null;
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no new event found
				return null;

			} else {
				String[] fileContentArr = fileContent.split("\n");
				EventLockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						info = EventLockInfo.createLineInfoFromString(infoStr);
						// only check active event
						if (info != null && info.getExpiryDate().after(now)) {
							if (!findProcessedEvent(info)) {
								lineInfoList.add(info);
							}
						}
					} catch (Throwable t) {
						// ignore this lock string
						info = null;
						logger.severe("Failed to create LineInfo object, check event file content: "
								+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
					}
				}
			}
			return lineInfoList;
		} catch (Throwable t) {
			logger.severe("Failed to check event: " + t);
			return null;
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					logger.severe("Failed to close event file channel: " + e);
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (IOException e) {
					logger.severe("Failed to close event file: " + e);
				}
			}
		}
	}

	// the instance will ignore all events sent before this instance start
	private static synchronized List<EventLockInfo> initProcessedEventInfoList() {

		FileChannel channel = null;
		FileLock lock = null;
		List<EventLockInfo> lineInfoList = new ArrayList<EventLockInfo>();
		Date now = new Date();
		RandomAccessFile file = null;
		try {
			try {
				logger.finer("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
				return null;
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no new event found
				return lineInfoList;

			} else {
				String[] fileContentArr = fileContent.split("\n");
				EventLockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						// logger.finer("info string from event file:" + infoStr);
						info = EventLockInfo.createLineInfoFromString(infoStr);
						if (info != null) {
							lineInfoList.add(info);

						}
					} catch (Throwable t) {
						// ignore this lock string
						info = null;
						logger.severe("Failed to create LineInfo object, check event file content: "
								+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME + ", Error = " + t);
					}
				}
			}
			String tmpStr = "";
			for (EventLockInfo tmpInfo : lineInfoList) {
				tmpStr = tmpStr + tmpInfo + "\n";
			}
			logger.fine("inited processedEventInfoList = " + tmpStr);
			return lineInfoList;
		} catch (Throwable t) {
			logger.severe("Failed to check event: " + t);
			return lineInfoList;
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					logger.severe("Failed to close event file channel: " + e);
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (IOException e) {
					logger.severe("Failed to close event file: " + e);
				}
			}
		}
	}

	private static boolean findProcessedEvent(EventLockInfo info) {
		if (info == null) {
			logger.fine("Event Info is null, return false.........");
			return false;
		}
		for (EventLockInfo tmpInfo : processedEventList) {
			if (tmpInfo.getName().equalsIgnoreCase(info.getName())
					&& tmpInfo.getKey().equalsIgnoreCase(info.getKey())) {
				return true;
			}
		}
		return false;
	}

	public void stopEventMonitor() {
		eventMonitorEnabled = false;
		this.interrupt();
	}

	public void run() {
		logger.info("multi-instance file support manager start to monitor event file: "
				+ MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME);
		File file = null;
		WatchService watchService = null;
		try {
			if (this.useWatchService) {

				Path path = FileSystems.getDefault().getPath(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_PATH);
				// System.out.println(path);
				watchService = FileSystems.getDefault().newWatchService();
				WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_MODIFY);
				while (eventMonitorEnabled) {
					WatchKey wk = watchService.take();
					for (WatchEvent<?> event : wk.pollEvents()) {
						final Path changed = (Path) event.context();
						if (changed.endsWith(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_NAME)) {
							logger.info("Event File '" + MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_NAME
									+ "' change is detetced......");
							List<EventLockInfo> infoList = MultiInstanceFileSupportManager
									.getUnProccessedEventInfoList();
							if (infoList == null || infoList.isEmpty()) {
								logger.info("There is no new event founded, do nothing......");
								continue;
							}
							// process event one by one
							for (EventLockInfo info : infoList) {
								new EventProcessor().processEvent(info);
								MultiInstanceFileSupportManager.addProcessedEvent(info);
							}
						}
					}
					// reset the key
					boolean valid = wk.reset();
					if (!valid) {
						logger.severe("WatchKey can not be reset...............");
					}
				}//end while
			} else {
				file = new File(MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_FULL_NAME);
				while (eventMonitorEnabled) {

					Thread.sleep(this.fileCheckIntervalMills);

					if (this.lastModifiedTime < file.lastModified()) {
						logger.info("Event File '" + MultiInstanceFileSupportConstant.INSTANCE_EVENT_FILE_NAME + "' change is detetced......");
						this.lastModifiedTime = file.lastModified();
						List<EventLockInfo> infoList = MultiInstanceFileSupportManager.getUnProccessedEventInfoList();
						if (infoList == null || infoList.isEmpty()) {
							logger.info("There is no new event founded, do nothing......");
							continue;
						}
						// process event one by one
						for (EventLockInfo info : infoList) {
							new EventProcessor().processEvent(info);
							MultiInstanceFileSupportManager.addProcessedEvent(info);
						}
					}
				}

			}

		} catch (Throwable t) {
			eventMonitorEnabled = false;
			logger.severe(this.getName() + " stopped. Will not monitor event any more........." + t.getMessage());
			t.printStackTrace();
		}finally {
			if(watchService != null) {
				try {
					watchService.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
