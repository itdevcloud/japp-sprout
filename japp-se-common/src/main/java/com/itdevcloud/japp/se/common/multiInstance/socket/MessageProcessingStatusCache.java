package com.itdevcloud.japp.se.common.multiInstance.socket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.itdevcloud.japp.se.common.service.CommonLogger;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class MessageProcessingStatusCache {
	private static final CommonLogger logger = CommonLogger.getLogger(MessageProcessingStatusCache.class.getName());
	
	private static final String PURGE_DATE_FORMAT = "yyyy-MM-dd";
	
	private static List<MessageProcessingStatus> messageStatusList = null;
	private static String lastPurgeDate = null;
	static {
		init();
	}
	private static void init() {
		messageStatusList = new ArrayList<MessageProcessingStatus>();
		lastPurgeDate = DateUtils.dateToString(new Date(), PURGE_DATE_FORMAT);
	}
	
	public static List<MessageProcessingStatus> getCacheList(String id){
		//assume no one will change list element
		List<MessageProcessingStatus> copy = new ArrayList<>();
		if(messageStatusList == null || messageStatusList.isEmpty()) {
			return copy;
		}
		
		if(StringUtil.isEmptyOrNull(id)) {
			copy.addAll(messageStatusList);
		}else {
			for (MessageProcessingStatus status: messageStatusList) {
				if(status != null && status.getId().equalsIgnoreCase(id)) {
					copy.add(status);
				}
			}
		}
		Collections.sort(copy);
		return copy;
	}
	
	public static void addOrUpdate(MessageProcessingStatus status){
		
		String today = DateUtils.dateToString(new Date(), PURGE_DATE_FORMAT);
//		if(today.compareTo(lastPurgeDate) > 0) {
			cleanCache();
//		}
		if(status == null || StringUtil.isEmptyOrNull(status.getId()) || StringUtil.isEmptyOrNull(status.getSource()) || StringUtil.isEmptyOrNull(status.getDestination())) {
			logger.warning("Invalid MessageProcessingStatus object, do nothing!");
			return;
		}
		boolean updated = false;
		for (MessageProcessingStatus tmpStatus: messageStatusList) {
			if(tmpStatus != null && tmpStatus.getId().equalsIgnoreCase(status.getId()) && 
					tmpStatus.getSource().equalsIgnoreCase(status.getSource()) && 
					tmpStatus.getDestination().equalsIgnoreCase(status.getDestination())) {
				logger.finer("update status cache for the message: id = " + tmpStatus.getId()  + ", source = " + tmpStatus.getDestination() + ", destination = " + tmpStatus.getDestination());
				if(!StringUtil.isEmptyOrNull(status.getType())){
					tmpStatus.setType(status.getType());
				}
				if(!StringUtil.isEmptyOrNull(status.getName())){
					tmpStatus.setName(status.getName());
				}
				if(!StringUtil.isEmptyOrNull(status.getDirection())){
					tmpStatus.setDirection(status.getDirection());
				}
				if(!StringUtil.isEmptyOrNull(status.getTransferStatus())){
					tmpStatus.setTransferStatus(status.getTransferStatus());
				}
				if(!StringUtil.isEmptyOrNull(status.getResponseStatus())){
					tmpStatus.setResponseStatus(status.getResponseStatus());
				}
				if(!StringUtil.isEmptyOrNull(status.getProcessStatus())){
					//note: another thread may set status to success already
					if(!MultiInstanceSocketSupportConstant.PROCESS_STATUS_SUCCESS.equalsIgnoreCase(tmpStatus.getProcessStatus())) {
						tmpStatus.setProcessStatus(status.getProcessStatus());
					}
				}
				if(status.getSentDate() != null){
					tmpStatus.setSentDate(status.getSentDate());
				}
				if(status.getReceivedDate() != null){
					tmpStatus.setReceivedDate(status.getReceivedDate());
				}
				updated = true;
			}
		}
		if(!updated) {
			logger.finer("add new entry in status cache for the message: id = " + status.getId()  + ", source = " + status.getDestination() + ", destination = " + status.getDestination());
			messageStatusList.add(status);
		}
	}

	private static synchronized void cleanCache(){
		logger.finer("clean cache based on retntion day: " + MultiInstanceSocketSupportConstant.MESSAGE_STATUS_CACHE_RETENTION_DAY);
		Date now = new Date();
		String today = DateUtils.dateToString(now, PURGE_DATE_FORMAT);
		if(today.compareTo(lastPurgeDate) <=0) {
			//done by other thread.
			return;
		}
		List<MessageProcessingStatus> tmpStatusList = new ArrayList<MessageProcessingStatus>();

		for (MessageProcessingStatus tmpStatus: messageStatusList) {
			Date retationDate = DateUtils.addTime(tmpStatus.getReceivedDate(), Calendar.HOUR, 24*7);
			if(now.after(retationDate)) {
				continue;
			}else {
				tmpStatusList.add(tmpStatus);
			}
		}
		messageStatusList = tmpStatusList;
		lastPurgeDate = today;
	}

	//note: event source may have multiple entrys with same id and source but different destination
	public static List<MessageProcessingStatus> getCacheedStatusList(String id) {
		if(messageStatusList == null || messageStatusList.isEmpty()) {
			return null;
		}
		List<MessageProcessingStatus> copy = new ArrayList<>();
		if(StringUtil.isEmptyOrNull(id)) {
			copy.addAll(messageStatusList);
		}else {
			for (MessageProcessingStatus status: messageStatusList) {
				if(status != null && status.getId().equalsIgnoreCase(id)) {
					copy.add(status);
				}
			}
		}
		return copy;
		
	}
	//note: event source may have multiple entrys with same id and source but different destination
	public static String getCacheInfoString(String id) {
		String cacheInfo = null;
		if(messageStatusList == null || messageStatusList.isEmpty()) {
			return null;
		}
		List<MessageProcessingStatus> copy = new ArrayList<>();
		if(StringUtil.isEmptyOrNull(id)) {
			copy.addAll(messageStatusList);
		}else {
			for (MessageProcessingStatus status: messageStatusList) {
				if(status != null && status.getId().equalsIgnoreCase(id)) {
					copy.add(status);
				}
			}
		}
		Collections.sort(copy);
		for (MessageProcessingStatus status: copy) {
			if(cacheInfo == null ) {
				cacheInfo = status.getPrintString() ;
			}else {
				cacheInfo = cacheInfo + "\n" + status.getPrintString() ;
			}
		}

		return cacheInfo;
		
	}

}
