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
package com.itdevcloud.japp.core.iaa.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class SpecialCharacterUtil {

	private static final Logger logger = LogManager.getLogger(SpecialCharacterUtil.class);
	private static final String SPECIAL_CHAR = " ,-'.0123456789";
	private static final String PATTERN_FOR_INPUT = "[A-Za-z0-9\\'\\-\\,\\. ]*";
	private static final String PATTERN_FOR_BLOCK="[<>~;^`]";

	public static boolean checkForStringInput(String param) {
		if (param == null || param.trim().length() == 0) {
			return true;
		}

		if (!param.trim().matches(PATTERN_FOR_INPUT)) {
			logger.debug(param + " has special characters.");
			return false;
		} else if (isSingleSpec(param)) {
			logger.debug(param + " is a single special character.");
			return false;
		}
		return true;
	}
	public static boolean checkForStringInputSpecial(String param) {
		if (param == null || param.trim().length() == 0) {
			return true;
		}

		if (!param.trim().matches(PATTERN_FOR_INPUT)) {
			logger.debug(param + " has special characters.");
			return false;
		}
		return true;
	}

	private static boolean isSingleSpec(String source) {
		if (source.trim().length() > 1 || source.trim().length() == 0) {
			return false;
		}

		boolean result = false;
		if (SPECIAL_CHAR.contains(source)) {
			result = true;
		}
		return result;
	}


	/**
	 * this method disallow some special characters that may cause cross-site scripts error.
	 * param: String
	 * return: boolean
	 */
	public static boolean checkCrossSiteScriptCharacters(String str){
		boolean specialC = false;
		if(!StringUtils.isEmpty(str) && str.trim().length() > 0){
			String lowerStr = str.toLowerCase();
			if (lowerStr.indexOf("<script") >= 0
					|| lowerStr.indexOf("<object") >= 0
					|| lowerStr.indexOf("<applet") >= 0
					|| lowerStr.indexOf("<embed") >= 0
					|| lowerStr.indexOf("<form") >= 0) {
				return true;

			}
			if (lowerStr.indexOf("%3Cscript") >= 0
					|| lowerStr.indexOf("%3Cobject") >= 0
					|| lowerStr.indexOf("%3Capplet") >= 0
					|| lowerStr.indexOf("%3Cembed") >= 0
					|| lowerStr.indexOf("%3Cform") >= 0) {
				return true;

			}
		}
		return specialC;
	}

	/**
	 * check if the input string including some special characters which caused 400
	 * Bad Request error. 
	 * @param note
	 * @return boolean
	 */
	public static boolean checkSpecialBlockCharacters(String note){
		boolean specialC = false;

		if(!StringUtils.isEmpty(note) && note.trim().length() > 0){
			Pattern p = Pattern.compile(PATTERN_FOR_BLOCK);
			Matcher m = p.matcher(note);
			specialC = m.find();
			if (specialC==true) {
				return specialC;
			}

			if (note.indexOf("%3C") >= 0
					|| note.indexOf("%3E") >= 0
					|| note.indexOf("%5E") >= 0
					|| note.indexOf("%60") >= 0) {
				specialC = true;
			}
		}
		return specialC;
	}
}