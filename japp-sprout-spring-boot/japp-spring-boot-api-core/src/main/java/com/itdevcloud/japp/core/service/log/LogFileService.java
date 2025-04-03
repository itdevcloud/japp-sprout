/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.core.service.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.email.SpringEmailProvider;
import com.itdevcloud.japp.se.common.util.StringUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class LogFileService implements AppFactoryComponentI {
	//private static final Logger logger = LogManager.getLogger(LogFileService.class);
	private static final Logger logger = LogManager.getLogger(LogFileService.class);

	private static final String JAPPCORE_DEFAULT_LOG_FOLDER = "logs/";

	@PostConstruct
	public void init() {
	}

	public String getLogFileDir() {
		String logDir = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_LOG_DIR);
		if(StringUtil.isEmptyOrNull(logDir)) {
			String deployRoot = AppUtil.getDeploymentRootDir();
			if(StringUtil.isEmptyOrNull(deployRoot)) {
				logDir = JAPPCORE_DEFAULT_LOG_FOLDER;
			}else {
				logDir = deployRoot + JAPPCORE_DEFAULT_LOG_FOLDER;
			}
		}
		if(!logDir.endsWith(File.separator)) {
			logDir = logDir + File.separator;
		}
		return logDir;

	}

	public List<String> searchCurrentLog(String searchText) {
		String logFileName = getLogFileDir() + ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_LOG_CURRENT_LOG_FILENAME);
		return searchLinesFromFile(logFileName, searchText);
	}

	public Map<String, List<String>> searchLogDir(String searchText, String startDateStr, String endDateStr) {
		String logDir = getLogFileDir();
		LogFileNameFilter filter = new LogFileNameFilter();
		if (StringUtil.isEmptyOrNull(startDateStr) && StringUtil.isEmptyOrNull(endDateStr)) {
			return searchLinesFromDir(logDir, filter, searchText);
		} else  {
			List<String> fileNameList = getLogFileNameList(startDateStr, endDateStr);
			return searchLinesFromFiles(fileNameList, searchText);
		}
	}

	private List<String> getLogFileNameList(String startDateStr, String endDateStr){
		if(StringUtil.isEmptyOrNull(startDateStr) && StringUtil.isEmptyOrNull(endDateStr) ) {
			return null;
		}
		String currentLogFile = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_LOG_CURRENT_LOG_FILENAME);
		String dir = getLogFileDir();
		File dirFile = new File(dir);
		if(!dirFile.exists() || !dirFile.isDirectory()) {
			logger.warn("getLogFileNameList() - the dir does not exist or is not a dir, do nothing:  <" + dir + ">......");
			return null;
		}
		File[] files = dirFile.listFiles();
		List<String> fileNameList = new ArrayList<String>();
		if(files != null) {
			for(File file: files) {
				String fileName = file.getName();
				if(file.exists() && file.isFile()) {
					if(!fileName.startsWith("bits") || !fileName.endsWith(".log")) {
						continue;
					}
					String dateStr = getDateString(file);
					if(StringUtil.isEmptyOrNull(dateStr)) {
						if(!fileNameList.contains(currentLogFile)) {
							fileNameList.add(currentLogFile);
						}
						continue;
					}
					if(!StringUtil.isEmptyOrNull(startDateStr) && StringUtil.isEmptyOrNull(endDateStr)) {
						if(dateStr.compareTo(startDateStr) >= 0) {
							fileNameList.add(file.getAbsolutePath());
						}
					}else if(StringUtil.isEmptyOrNull(startDateStr) && !StringUtil.isEmptyOrNull(endDateStr)) {
						if(dateStr.compareTo(endDateStr) <= 0) {
							fileNameList.add(file.getAbsolutePath());
						}
					}else {
						if(dateStr.compareTo(startDateStr) >= 0 && dateStr.compareTo(endDateStr) <= 0) {
							fileNameList.add(file.getAbsolutePath());
						}
					}
				}
			}//end for
		}
		logger.debug("getLogFileNameList().........fileNameList = " + fileNameList);
		return fileNameList;
	}

	private String getDateString(File file) {
		String fileName = file.getName();
		Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		Matcher matcher = pattern.matcher(fileName);

		if (matcher.find()) {
			return matcher.group();
		}else {
			return null;
		}
	}

	public List<String> searchLinesFromFile(String fileName, String searchText) {
		if (StringUtil.isEmptyOrNull(fileName) || StringUtil.isEmptyOrNull(searchText) || searchText.trim().length() < 5) {
			logger.error(
					"searchLinesFromFile()......fileName or searchText is empty or searchText less than 5 characters, do nothing....!!!");
			return null;
		}
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			logger.error("searchLinesFromFiles() - the file does not exist or is not a file, do nothing:  <"
					+ fileName + ">......");
			return null;
		}
		Pattern pattern = Pattern.compile("^\\[[A-Z\\s]{5}\\]\\s\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
		BufferedReader br = null;
		List<String> list = new ArrayList<String>();
		searchText = searchText.toLowerCase();
		String simpleName = file.getName();
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = null;
			String logLine = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					//this is logLine start line
					//search current logLine
					if(!StringUtil.isEmptyOrNull(logLine)) {
						if (logLine.trim().toLowerCase().contains(searchText)) {
							list.add(logLine);
						}
					}
					logLine = line;
					count++;
					continue;
				}else {
					logLine = logLine + "\r\n" + line;
					continue;
				}
			}
			if(!StringUtil.isEmptyOrNull(logLine)) {
				if (logLine.trim().toLowerCase().contains(searchText)) {
					list.add(logLine);
				}
			}
			br.close();
			//logger.debug("fileName=" + fileName + ", list = " + list);
			return list;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw AppUtil.throwRuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.warn(e);
				}
				br = null;
			}
		}
	}

	public Map<String, List<String>> searchLinesFromFiles(List<String> fileNameList, String searchText) {
		if (fileNameList == null || fileNameList.isEmpty() || StringUtil.isEmptyOrNull(searchText)
				|| searchText.trim().length() < 5) {
			logger.error(
					"searchLinesFromFiles()......fileNameList or searchText is empty or searchText less than 5 characters, do nothing....!!!");
			return null;
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (String fileName : fileNameList) {
			File file = new File(fileName);
			if (!file.exists() || !file.isFile()) {
				logger.warn("searchLinesFromFiles() - the file does not exist or is not a file, do nothing:  <"
						+ fileName + ">......");
				continue;
			}
			String simpleName = file.getName();
			List<String> list = searchLinesFromFile(fileName, searchText);
			if (list != null && !list.isEmpty()) {
				map.put(simpleName, list);
			}
		}
		return map;
	}

	public Map<String, List<String>> searchLinesFromDir(String dir, FilenameFilter filter, String searchText) {
		if (StringUtil.isEmptyOrNull(dir) || StringUtil.isEmptyOrNull(searchText) || searchText.trim().length() < 5) {
			logger.error(
					"searchLinesFromDir()......dir or searchText is empty or searchText less than 5 characters, do nothing....!!!");
			return null;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			logger.warn(
					"searchLinesFromDir() - the dir does not exist or is not a dir, do nothing:  <" + dir + ">......");
			return null;
		}
		File[] files = null;
		if (filter == null) {
			files = dirFile.listFiles();
		} else {
			files = dirFile.listFiles(filter);
		}
		List<String> fileNameList = new ArrayList<String>();
		if (files != null) {
			for (File file : files) {
				if (file.exists() && file.isFile()) {
					fileNameList.add(file.getAbsolutePath());
				}
			}
		}
		if (fileNameList.isEmpty()) {
			logger.warn("searchLinesFromDir() - the dir does contains files, do nothing:  <" + dir + ">......");
			return null;
		}

		return searchLinesFromFiles(fileNameList, searchText);
	}


}
