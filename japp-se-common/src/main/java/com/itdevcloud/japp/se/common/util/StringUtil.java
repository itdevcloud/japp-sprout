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
package com.itdevcloud.japp.se.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class StringUtil {

	public static final String BEGIN_MARK = "^^^^^";
	public static final String END_MARK = "^^^^^";

	public static String replaceAllWhiteSpacesWithSpace(String original) {
		if (original == null) {
			return null;
		}
		return original.replaceAll("\\s+", " ");
	}

	public static boolean isEmptyOrNull(String s) {
		if (s != null && !(s = s.trim()).equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public static String appendChar(String str, int number, char c) {
		StringBuffer sb = new StringBuffer(str);
		for(int i = 0; i < number; i++){
			sb.append(c);
		}
		return sb.toString();
	}
	public static String appendCharToExtendLength(String str, char c, int targetLength) {
		StringBuffer sb = null;
		if(isEmptyOrNull(str)){
			sb = new StringBuffer();
		}else{
			sb = new StringBuffer(str);
		}
		if(sb.length() >= targetLength){
			return sb.toString();
		}
		int len = targetLength - sb.length();
		for(int i = 0; i < len; i++){
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String escapeDoubleQuote(String original) {
		if (original == null) {
			return null;
		}
		//direct change not working
		String str1 = original.replaceAll("\"", "@@@");
		String str2 = str1.replaceAll("@@@", "\\\\\"");
		return str2;
	}

	public static String mask(String str, int begin, int len, char mask) {
		if(isEmptyOrNull(str)){
			return str;
		}
		int size = str.length();
		begin = (begin < 0? 0:begin);
		if(begin >= size || len < 0){
			return str;
		}
		len = (begin + len > size? 0: len);
		StringBuffer sb = new StringBuffer(str.substring(0, begin));
		for(int i = begin; i < begin+len; i++){
			sb.append(mask);
		}
		if(begin + len < size){
			sb.append(str.substring(begin+len));
		}
		return sb.toString();
	}
	
	public static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static String changeFirstCharCase(String str, boolean toUpper){
		if(isEmptyOrNull(str)) {
			return str;
		}
		if(toUpper){
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}else{
			return str.substring(0, 1).toLowerCase() + str.substring(1);
		}
	}
	
	public static String replaceStringByRegex(String oldStr,
			String chFromRegex, String chTo) {
		if (StringUtil.isEmptyOrNull(oldStr)) {
			return "";
		}
		String newStr = oldStr.replaceAll(chFromRegex, chTo);
		return newStr;
	}

	public static String replaceStringByWordRegex(String oldStr, String chFromRegex, String chTo) {
		if (StringUtil.isEmptyOrNull(oldStr)) {
			return "";
		}
		String newStr = oldStr.replaceAll("\\b" + chFromRegex + "\\b", chTo);
		return newStr;
	}

	public static String insertStringByRegex(boolean insertBefore, String oldStr, String chRegex, String chTo) {
		if (StringUtil.isEmptyOrNull(oldStr) ) {
			return "";
		}
		if (StringUtil.isEmptyOrNull(chRegex) ) {
			return oldStr;
		}
		Pattern pattern = Pattern.compile(chRegex);
		String workingStr = oldStr;
		Matcher matcher = pattern.matcher(workingStr);

		StringBuffer newStr = new StringBuffer();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			newStr.append(workingStr.substring(0, start));
			if(insertBefore) {
				newStr.append(chTo);
				newStr.append(workingStr.substring(start, end));
			}else {
				newStr.append(workingStr.substring(start, end));
				newStr.append(chTo);
			}
			workingStr = workingStr.substring(end);
			matcher = pattern.matcher(workingStr);
		}
		newStr.append(workingStr);
		return newStr.length()==0?oldStr:newStr.toString();
	}

	public static String removeStringByRegex(String oldStr, String startRegex, String endRegex) {
		if (StringUtil.isEmptyOrNull(oldStr) ) {
			return "";
		}
		boolean startFromBegin = false;
		boolean endUntilEnd = false;
		if (StringUtil.isEmptyOrNull(startRegex) || startRegex.equals(StringUtil.BEGIN_MARK)) {
			startFromBegin = true;
		}
		if (StringUtil.isEmptyOrNull(endRegex) || endRegex.equals(StringUtil.END_MARK)) {
			endUntilEnd = true;
		}
		Pattern startPattern = startFromBegin?null:Pattern.compile(startRegex);
		Pattern endPattern = endUntilEnd?null:Pattern.compile(endRegex);
		Matcher startMatcher = startFromBegin?null:startPattern.matcher(oldStr);
		Matcher endMatcher = endUntilEnd?null:endPattern.matcher(oldStr);

		if(startFromBegin && endUntilEnd) {
			return "";
		}else if(startFromBegin) {
			if(!endMatcher.find()) {
				return oldStr;
			}
			return oldStr.substring(endMatcher.start());
		}else if(endUntilEnd) {
			if(!startMatcher.find()) {
				return oldStr;
			}
			return oldStr.substring(0, startMatcher.end());
		}

		//come to here means remove in middle
		String workingStr = oldStr;

		StringBuffer newStr = new StringBuffer();
		while (startMatcher.find()) {
			if(!endMatcher.find()) {
				//no match found
				break;
			}
			int startMatcherEnd = startMatcher.end();
			int endMatcherStart = endMatcher.start();
			if(startMatcherEnd >= endMatcherStart) {
				while(endMatcher.find()) {
					if(startMatcherEnd < endMatcher.start()) {
						endMatcherStart = endMatcher.start();
						break;
					}
				}
				if(startMatcherEnd >= endMatcherStart) {
					//no match found
					break;
				}
			}
			//check if the found match is the closest match or not
			while(startMatcher.find()) {
				if(startMatcher.end() < endMatcherStart) {
					startMatcherEnd = startMatcher.end();
				}else {
					break;
				}
			}
			newStr.append(workingStr.substring(0, startMatcherEnd) + endMatcher.group());
			workingStr = workingStr.substring(endMatcher.end());
			startMatcher = startPattern.matcher(workingStr);
			endMatcher = endPattern.matcher(workingStr);
		}
		newStr.append(workingStr);
		return newStr.length()==0?oldStr:newStr.toString();
	}

	public static String replaceString(String oldStr, String chFrom, String chTo) {
		if (StringUtil.isEmptyOrNull(oldStr)) {
			return "";
		}
		int beg = 0;
		int end = 0;
		StringBuffer newStr = null;
		if ((end = oldStr.indexOf(chFrom)) == -1) {
			return oldStr;
		} else {
			newStr = new StringBuffer();
		}
		while (end != -1) {
			newStr.append(oldStr.substring(beg, end) + chTo);
			beg = end + chFrom.length();
			end = oldStr.indexOf(chFrom, beg);
		}
		newStr.append(oldStr.substring(beg));
		return newStr.toString();
	}

	public static String insertString(boolean insertBefore, String oldStr, String chKey, String chTo) {
		if (StringUtil.isEmptyOrNull(oldStr)) {
			return "";
		}
		int beg = 0;
		int end = 0;
		StringBuffer newStr = null;
		if ((end = oldStr.indexOf(chKey)) == -1) {
			return oldStr;
		} else {
			newStr = new StringBuffer();
		}
		while (end != -1) {
			if(insertBefore) {
				newStr.append(oldStr.substring(beg, end) + chTo + chKey);
			}else {
				newStr.append(oldStr.substring(beg, end) + chKey + chTo);
			}
			beg = end + chKey.length();
			end = oldStr.indexOf(chKey, beg);
		}
		newStr.append(oldStr.substring(beg));
		return newStr.toString();
	}

	public static String replaceString(String oldStr, String chTo,
			int startIdx, int endIdx) {
		if (oldStr == null || startIdx < 0 || endIdx < startIdx
				|| startIdx >= oldStr.length() || endIdx >= oldStr.length()) {
			return oldStr;
		}
		String replaceStr = oldStr.substring(0, startIdx)
				+ (chTo == null ? "" : chTo) + oldStr.substring(endIdx + 1);
		return replaceStr;
	}

	public static String convertEncode(String in, String oldEncode,
			String newEncode) {
		if (in == null || in.equals("") || oldEncode == null
				|| newEncode == null) {
			return in;
		}
		try {
			String str = new String(in.getBytes(oldEncode), newEncode);
			return str;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] getBytes(String s)  {
		try {
			return s.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getString(byte[] b)  {
		try {
			return new String(b, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encodeBase64(byte[] bytes)  {
		String encoded = Base64.getEncoder().encodeToString(bytes);
		return encoded;
	}

	public static String encodeBase64(String s)  {
		String encoded = Base64.getEncoder().encodeToString(getBytes(s));
		return encoded;
	}
	
	public static byte[] decodeBase64(String encoded)  {
		byte[] decoded = Base64.getDecoder().decode(encoded);		
		return decoded;
	}

	public static int compareTo(String s1, String s2)  {
		if(s1 == null || s2 == null) {
			if(s1 == null) {
				return s2 == null? 0: -1;
			}else {
				return 1;
			}
		}
		//both not null
		return s1.compareTo(s2);
	}


}
