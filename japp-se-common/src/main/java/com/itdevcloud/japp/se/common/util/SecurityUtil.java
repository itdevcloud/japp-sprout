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

import java.security.spec.KeySpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
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

	public static EncryptedInfo encrypt(String clearText) {
		if (StringUtil.isEmptyOrNull(clearText)) {
			return null;
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = new Crypter();
		ei.setEncryptedText(crypter.encrypt(clearText));
		ei.setEncryptionKey(crypter.getEncodedKeyString());
		ei.setAlgorithm(crypter.getAlgorithm());
		ei.setTransformation(crypter.getTransformation());
		return ei;
	}
	public static String decrypt(String encodedKey, String encryptedText) {
		if (StringUtil.isEmptyOrNull(encodedKey) || StringUtil.isEmptyOrNull(encryptedText)) {
			throw new RuntimeException("encodedKey and/or encryptedText can not be null, check code!");
		}
		Crypter cryptor = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		return cryptor.decrypt(encryptedText);
	}

	public static EncryptedInfo encryptFile(String inputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName) ) {
			throw new RuntimeException("inputFileName can not be null, check code!");
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = new Crypter();
		ei.setEncryptedText(crypter.encryptFile(inputFileName));
		ei.setEncryptionKey(crypter.getEncodedKeyString());
		ei.setAlgorithm(crypter.getAlgorithm());
		ei.setTransformation(crypter.getTransformation());
		return ei;
	}
	public static EncryptedInfo encryptFile(String inputFileName, String outputFileName ) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = new Crypter();
		crypter.encryptFile(inputFileName, outputFileName);
		ei.setEncryptedText(null);
		ei.setEncryptionKey(crypter.getEncodedKeyString());
		ei.setAlgorithm(crypter.getAlgorithm());
		ei.setTransformation(crypter.getTransformation());
		return ei;
	}
	
	public static String decryptFile(String encodedKey, String inputFileName) {
		if (StringUtil.isEmptyOrNull(encodedKey) || StringUtil.isEmptyOrNull(inputFileName)) {
			throw new RuntimeException("encodedKey and/or outputFileName can not be null, check code!");
		}
		Crypter cryptor = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		return cryptor.decryptFile(inputFileName);
	}
    
    
	private static int getKeySize(String algorithm) {
		if(Crypter.CIPHER_DEFAULT_ALGORITHM.equalsIgnoreCase(algorithm)) {
			return Crypter.CIPHER_DEFAULT_KEY_SIZE;
		}else {
			return Crypter.CIPHER_DEFAULT_KEY_SIZE;
		}
	}
	
    public static SecretKey generateKey()  {
    	return generateKey(null);
    }   
    public static SecretKey generateKey(String algorithm)  {
    	if(StringUtil.isEmptyOrNull(algorithm)) {
    		algorithm = Crypter.CIPHER_DEFAULT_ALGORITHM;
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

    public static SecretKey generateKeyFromEncodedKey(String key, String algorithm)  {
    	if(StringUtil.isEmptyOrNull(key)) {
    		return null;
    	}
    	if(StringUtil.isEmptyOrNull(algorithm)) {
    		algorithm = Crypter.CIPHER_DEFAULT_ALGORITHM;
    	}
    	SecretKey secretKey = new SecretKeySpec(StringUtil.decodeBase64(key), algorithm);
    	return secretKey;
    }   
    
    public static SecretKey generateKeyFromPassword(String password, String salt, String algorithm){
    	if(StringUtil.isEmptyOrNull(password) || StringUtil.isEmptyOrNull(salt) ) {
    		throw new RuntimeException("Password and/or Salt can not be null, check code!");
    	}
    	if(StringUtil.isEmptyOrNull(algorithm)) {
    		algorithm = Crypter.CIPHER_DEFAULT_ALGORITHM;
    	}
    	SecretKey key = null;
    	try {
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
	        key = new SecretKeySpec(factory.generateSecret(spec)
	            .getEncoded(), algorithm);
	        return key;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
    }


	

}
