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
package com.itdevcloud.japp.se.common.security;

import java.security.MessageDigest;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class Hasher  {

	//private static final Logger logger = LogManager.getLogger(Hasher.class);
	private static String preferredHashAlgorithm = "SHA-512";

	public static String getPreferredHashAlgorithm() {
		return preferredHashAlgorithm;
	}

	public static String getHash(String message)  {
		return getHash(message,  null) ;
	}
	public static String getHash(String message, String algorithm)  {
		if(StringUtil.isEmptyOrNull(message)) {
			return message;
		}
		if(StringUtil.isEmptyOrNull(algorithm)) {
			algorithm = preferredHashAlgorithm;
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);

			messageDigest.reset();
			messageDigest.update(StringUtil.getBytes(message));

			return StringUtil.encodeBase64(messageDigest.digest());

		}catch (Exception e) {
			//logger.error(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public static String hashPassword(String cleartextPassword) {
		return getHash(cleartextPassword, preferredHashAlgorithm);
	}

	public static void main(String[] args) {
		String clearPwd = "12345";
		String hashedPwd = Hasher.getHash(clearPwd);
		System.out.println("hashedPwd = " + hashedPwd);

	}
}
