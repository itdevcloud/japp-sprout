package com.itdevcloud.japp.se.common.multiInstance.socket;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.RandomUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class LockFileManager {
	private static final CommonLogger logger = CommonLogger.getLogger(LockFileManager.class.getName());

	public static synchronized String addLock(String lockName, String requester, Date expiryDate, String key) {
		if (StringUtil.isEmptyOrNull(lockName) || StringUtil.isEmptyOrNull(requester)) {
			return null;
		}
		LockInfo lockInfo = new LockInfo(lockName, requester, expiryDate);
		FileChannel channel = null;
		FileLock lock = null;
		List<LockInfo> lockInfoList = new ArrayList<LockInfo>();
		Date now = new Date();
		key = StringUtil.isEmptyOrNull(key)?RandomUtil.generateUniqueID("l-", 3): key;
		RandomAccessFile file = null;
		try {
			try {
				logger.finer("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
				return null;
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no locks found
				lockInfo.setLockDate(now);
				lockInfo.setKey(key);
				lockInfo.setStatus(LockInfo.LOCK_STATUS_LOCKED);
				newFileContent = lockInfo.getLockInfoString();
			} else {
				String[] fileContentArr = fileContent.split("\n");
				LockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						//logger.finer("info string from lock file:" + infoStr);
						info = LockInfo.createLockInfoFromString(infoStr);
						// remove expired lock
						if (info.getExpiryDate().after(now)) {
							lockInfoList.add(info);
						}
					} catch (Throwable t) {
						// ignore this lock string
						key = null;
						info = null;
						logger.severe("Failed to create LockInfo object, check lock file content: "
								+ MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
					}
				}
				// check existing lock
				int idx = lockInfoList.indexOf(lockInfo);
				if (idx < 0) {
					// no same lock exists
					lockInfo.setLockDate(now);
					lockInfo.setKey(key);
					lockInfo.setStatus(LockInfo.LOCK_STATUS_LOCKED);
					lockInfoList.add(lockInfo);
					//logger.finer("add this lock: " + lockInfo);

				}else {
					logger.finer("same lock with active status detected......");
					key = null;
				}
				int i = 1;
				for (LockInfo newInfo : lockInfoList) {
					if (i == 1) {
						newFileContent = newInfo.getLockInfoString();
					} else {
						newFileContent = newFileContent + "\n" + newInfo.getLockInfoString();
					}
					i++;
				}
			}

			// write back to lock file for the new lock list
			try {
				FileUtil.writeStringToFile(channel, true, newFileContent);
				if(key != null) {
					logger.fine("add lock successfully for lock name = " + lockName + ", requster = " + requester);
				}else {
					logger.fine("add lock failed for lock name = " + lockName + ", requster = " + requester);
					
				}
				//for debug block file lock request from other process
//				logger.fine("sleep 60 seconds.......");
//				Thread.sleep(60000);
				
				return key;

			} catch (Throwable t) {
				key = null;
				logger.severe("Failed to write back to lock file: " + MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME
						+ ", Error = " + t);
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

	public static synchronized boolean releaseLock(String lockName, String requester, String key) {
		if (StringUtil.isEmptyOrNull(lockName) || StringUtil.isEmptyOrNull(requester)) {
			return false;
		}
		LockInfo lockInfo = new LockInfo(lockName, requester);
		FileChannel channel = null;
		FileLock lock = null;
		List<LockInfo> lockInfoList = new ArrayList<LockInfo>();
		Date now = new Date();
		RandomAccessFile file = null;
		try {
			try {
				logger.finer("Acquire exclusive lock of the lock file......");
				file = new RandomAccessFile(MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME, "rw");
				channel = file.getChannel();
				lock = channel.lock();
			} catch (Throwable t) {
				logger.info("Failed to open and lock Instance lock file: "
						+ MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
			}
			// read current lock info
			String fileContent = FileUtil.getFileContentAsString(channel);
			String newFileContent = "";
			if (StringUtil.isEmptyOrNull(fileContent)) {
				// no locks found
				return true;
			} else {
				String[] fileContentArr = fileContent.split("\n");
				LockInfo info = null;
				for (String infoStr : fileContentArr) {
					try {
						info = LockInfo.createLockInfoFromString(infoStr);
						// remove expired lock
						if (info.getExpiryDate().after(now)) {
							lockInfoList.add(info);
						}
					} catch (Throwable t) {
						// ignore this lock string
						info = null;
						logger.severe("Failed to create LockInfo object, check lock file content: "
								+ MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME + ", Error = " + t);
					}
				}
				// check existing lock
				int idx = lockInfoList.indexOf(lockInfo);
				if (idx >= 0) {
					String tmpKey = lockInfoList.get(idx).getKey();
					if(key != null && key.equals(tmpKey)) {
						lockInfoList.remove(idx);
					}else {
						//key is different, do nothing
						return false;
					}
				} else {
					// no same lock exists, do nothing
					return false;
				}
				int i = 1;
				for (LockInfo newInfo : lockInfoList) {
					if (i == 1) {
						newFileContent = newInfo.getLockInfoString();
					} else {
						newFileContent = newFileContent + "\n" + newInfo.getLockInfoString();
					}
					i++;
				}
			}

			// write back to lock file for the new lock list
			try {
				FileUtil.writeStringToFile(channel, true, newFileContent);
				logger.fine("release lock successfully for lock name = " + lockName + ", requster = " + requester);
				
				//for debug block file lock request from other process
//				logger.fine("sleep 60 seconds.......");
//				Thread.sleep(60000);
				
				return true;

			} catch (Throwable t) {
				logger.severe("Failed to write back to lock file: " + MultiInstanceSocketSupportConstant.INSTANCE_lOCK_FILE_NAME
						+ ", Error = " + t);
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


}
