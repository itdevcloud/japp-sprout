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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itdevcloud.japp.se.common.security.Crypter;
import com.itdevcloud.japp.se.common.security.EncryptedInfo;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class SecurityUtil {

	public static EncryptedInfo encrypt(String clearText) {
		if (StringUtil.isEmptyOrNull(clearText)) {
			return null;
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter cryptor = new Crypter();
		ei.setEncryptedText(cryptor.encrypt(clearText));
		ei.setEncryptionKey(cryptor.getKey());
		return ei;
	}
	public static String decrypt(String key, String encryptedText) {
		if (StringUtil.isEmptyOrNull(key) || StringUtil.isEmptyOrNull(encryptedText)) {
			return null;
		}
		Crypter cryptor = new Crypter(key);
		return cryptor.decrypt(encryptedText);
	}


}
