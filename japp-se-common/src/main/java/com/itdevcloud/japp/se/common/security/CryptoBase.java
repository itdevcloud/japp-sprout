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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

//import javax.xml.bind.DatatypeConverter;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class CryptoBase {
	
	protected static byte[] getBytes(String s)  {
		try {
			return s.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected static String getString(byte[] s)  {
		try {
			return new String(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected static String encodeBase64(byte[] bytes)  {
		String encoded = Base64.getEncoder().encodeToString(bytes);
		return encoded;
	}

	protected static String encodeBase64(String s)  {
		String encoded = Base64.getEncoder().encodeToString(getBytes(s));
		return encoded;
	}
	protected static byte[] decodeBase64(String encoded)  {
		byte[] decoded = Base64.getDecoder().decode(encoded);		
		return decoded;
	}

}
