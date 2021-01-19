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
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.itdevcloud.japp.se.common.security.Crypter;
import com.itdevcloud.japp.se.common.security.EncryptedInfo;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class SecurityUtil {

	public static final String DEFAULT_CIPHER_ALGO = "AES";
	public static final String DEFAULT_CIPHER_SPEC = DEFAULT_CIPHER_ALGO + "/CBC/PKCS5Padding";
	public static final int DEFAULT_CIPHER_AES_KEY_SIZE = 256;
	public static final int DEFAULT_CIPHER_AES_BLOCK_SIZE_BITS = 128;
	public static final int DEFAULT_CIPHER_AES_BLOCK_SIZE_BYTES = 16;

	public static EncryptedInfo encrypt(String clearText) {
		if (StringUtil.isEmptyOrNull(clearText)) {
			return null;
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter cryptor = new Crypter();
		ei.setEncryptedText(cryptor.encrypt(clearText));
		ei.setEncryptionKey(cryptor.getKeyString());
		ei.setAlgorithm(cryptor.getCipher().getAlgorithm());
		ei.setIv(cryptor.getIv());
		return ei;
	}
	public static String decrypt(String encodedKey, String encryptedText) {
		if (StringUtil.isEmptyOrNull(encodedKey) || StringUtil.isEmptyOrNull(encryptedText)) {
			return null;
		}
		Crypter cryptor = new Crypter(DEFAULT_CIPHER_ALGO, encodedKey);
		return cryptor.decrypt(encryptedText);
	}

    
    
	private static int getKeySize(String algorithm) {
		if(SecurityUtil.DEFAULT_CIPHER_ALGO.equalsIgnoreCase(algorithm)) {
			return SecurityUtil.DEFAULT_CIPHER_AES_KEY_SIZE;
		}else {
			return SecurityUtil.DEFAULT_CIPHER_AES_KEY_SIZE;
		}
	}
	
    public static SecretKey generateKey()  {
    	return generateKey(null);
    }   
    public static SecretKey generateKey(String algorithm)  {
    	if(StringUtil.isEmptyOrNull(algorithm)) {
    		algorithm = DEFAULT_CIPHER_ALGO;
    	}
    	SecretKey key = null;
    	try {
	        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
	        keyGenerator.init(getKeySize(algorithm));
	        key = keyGenerator.generateKey();
	        return key;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
    }

    public static SecretKey generateKeyFromEncodedKey(String key)  {
    	if(StringUtil.isEmptyOrNull(key)) {
    		return null;
    	}
    	SecretKey secretKey = new SecretKeySpec(StringUtil.decodeBase64(key), DEFAULT_CIPHER_ALGO);
    	return secretKey;
    }   
    
    public static SecretKey generateKeyFromPassword(String password, String salt){
    	if(StringUtil.isEmptyOrNull(password) || StringUtil.isEmptyOrNull(salt) ) {
    		throw new RuntimeException("Password and/or Salt can not be null, check code!");
    	}
    	SecretKey key = null;
    	try {
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	        key = new SecretKeySpec(factory.generateSecret(spec)
	            .getEncoded(), DEFAULT_CIPHER_ALGO);
	        return key;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
    }


	

}
