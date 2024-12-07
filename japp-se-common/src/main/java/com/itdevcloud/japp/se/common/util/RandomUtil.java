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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class RandomUtil {
	//no 'o', capital J and L
	private static char[] supportedChars = "0123456789abcdefghiJkmLnpqrstuvwxyz".toCharArray();

	private static Random random = new Random();
	private static SecureRandom secureRandom = new SecureRandom();

	public static String getNextSessionToken(int tokenSize) {
		return new BigInteger(tokenSize, secureRandom).toString(32);
	}
	public static int getNextRadomInt(int maxInt) {
		return random.nextInt(maxInt);
	}
	
	//case insensitive
	//prefix+TS(11)+radom
	public static String generateUniqueID(String prefix, int radomLength){
		//generate random number
		char[] randomChars = new char[radomLength];
		for (int i = 0;  i < radomLength;  i++) {
			randomChars[i] = supportedChars[random.nextInt(supportedChars.length)];
		}
		String sdate = DateUtils.dateToString(new Date(), "yyMMddHHmmssSSS");

		String str = sdate.substring(0, 2);
		//repeat every 35 years
		char yy = supportedChars[Integer.parseInt(str) % (supportedChars.length)];

		str = sdate.substring(2, 4);
		char MM = supportedChars[Integer.parseInt(str)];

		str = sdate.substring(4, 6);
		char dd = supportedChars[Integer.parseInt(str)];

		str = sdate.substring(6, 8);
		char HH = supportedChars[Integer.parseInt(str)];

		str = sdate.substring(8);

		String uid = prefix + yy + MM + dd + HH + str + new String(randomChars);
		return uid;

	}
	
	public static String generateOrderID(){
		String id =  RandomUtil.generateUniqueID("", 5);
		id = id.substring(0, 8) + "-" + id.substring(8);
		return id;
	}
	public static String generateMessageID(){
		String id =  RandomUtil.generateUniqueID("m-", 7);
		return id;
	}
	public static String generateTransactionID(){
		String id =  RandomUtil.generateUniqueID("t-", 7);
		return id;
	}
	
	public static String generateAlphanumericString(String prefix, int length) {
		String str = generateAlphanumericString(length);
		if(StringUtil.isEmptyOrNull(prefix)) {
			prefix = "";
		}
		return prefix + str;
	}	
	public static String generateAlphanumericString(int length) {
		if(length <= 0){
			length = 10;
		}
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'
	    Random random = new Random();
	    //58-64 and 91-96 are not Alphanumeric
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(length)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();

	    return generatedString;
	}	
}
