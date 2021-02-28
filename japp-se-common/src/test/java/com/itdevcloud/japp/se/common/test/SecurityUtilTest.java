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
package com.itdevcloud.japp.se.common.test;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.security.EncryptedInfo;
import com.itdevcloud.japp.se.common.util.FileUtil;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class SecurityUtilTest  {


	@BeforeAll
	public static void setup() {
	}

	@BeforeEach
	public void init() {
	}
	
	@Disabled
	public void methodYouWantIgnored() {
	}

	@Test
	public void encryptText_decryptTextTest() {
		String clearText1 = "Hello World!";
		EncryptedInfo encryptedInfo = SecurityUtil.encrypt(clearText1, null);
		System.out.println(""+encryptedInfo);
		if(encryptedInfo == null || StringUtil.isEmptyOrNull(encryptedInfo.getEncryptedText())) {
			assertFalse(true, "encryptedInfo is empty");
		}
		String clearText2 = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		assertEquals(clearText1, clearText2);
	}
	
	//@Test
	public void encryptTextPwd_decryptPwdTextTest() {
		String clearText1 = "Hello World!";
		String pwd = "password12345";
		String salt = "12345";
		String encodedKey = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
		
		EncryptedInfo encryptedInfo = SecurityUtil.encrypt(clearText1, encodedKey);
		
		System.out.println(""+encryptedInfo);
		if(encryptedInfo == null || StringUtil.isEmptyOrNull(encryptedInfo.getEncryptedText())) {
			assertFalse(true, "encryptedInfo is empty");
		}
		
		String encodedKey2 = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
		String clearText2 = SecurityUtil.decrypt(encodedKey2, encryptedInfo.getEncryptedText());
		assertEquals(clearText1, clearText2);
	}

	//@Test
    public void encryptFileToString_decryptStringTest() {

        String inputfileName = "/Test-1.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, null);
		System.out.println(""+encryptedInfo);
		
		String decryptedContent = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

    }
    
	//@Test
    public void encryptFileToStringPwd_decryptStringPwdTest() {

        String inputfileName = "/Test-1.txt";
		
        String pwd = "password12345";
		String salt = "12345";
		String encodedKey = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, encodedKey);
		System.out.println(""+encryptedInfo);
		
		String encodedKey2 = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
		String decryptedContent = SecurityUtil.decrypt(encodedKey2, encryptedInfo.getEncryptedText());
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

    }
	
	//@Test
    public void encryptFileToFile_decryptFileTest() {

        String inputfileName = "/Test-1.txt";
        String outputfileName = "Test-1-encrypted.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, outputfileName, null);
		
		String decryptedContent = SecurityUtil.decryptFile(encryptedInfo.getEncryptionKey(), outputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

		//assertTrue(true);

    }
	//@Test
    public void encryptFileToFilePwd_decryptFilePwdTest() {

        String inputfileName = "/Test-1.txt";
        String outputfileName = "Test-1-encrypted.txt";
		
        String pwd = "password12345";
		String salt = "12345";
		String encodedKey = SecurityUtil.generateKeyFromPassword(pwd, salt, null);

		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, outputfileName, encodedKey);
		
		String encodedKey2 = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
		String decryptedContent = SecurityUtil.decryptFile(encodedKey2, outputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

    }
	
	//@Test
    public void encryptFileToFile_decryptFileToFileTest() {

        String inputfileName = "/Test-1.txt";
        String encryptedOutputfileName = "Test-1-encrypted.txt";
        String decryptedOutputfileName = "Test-1-decrypted.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, encryptedOutputfileName, null);
		SecurityUtil.decryptFile(encryptedInfo.getEncryptionKey(), encryptedOutputfileName, decryptedOutputfileName);
		
		String decryptedContent = FileUtil.getFileContentAsString(decryptedOutputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);

		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

		//assertTrue(true);

    }
	//@Test
    public void encryptFileToFilePwd_decryptFileToFilePwdTest() {

        String inputfileName = "/Test-1.txt";
        String encryptedOutputfileName = "Test-1-encrypted.txt";
        String decryptedOutputfileName = "Test-1-decrypted.txt";
		
        String pwd = "password12345";
		String salt = "12345";
		String encodedKey = SecurityUtil.generateKeyFromPassword(pwd, salt, null);

		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, encryptedOutputfileName, encodedKey);

		String encodedKey2 = SecurityUtil.generateKeyFromPassword(pwd, salt, null);
        SecurityUtil.decryptFile(encodedKey2, encryptedOutputfileName, decryptedOutputfileName);
		
		String decryptedContent = FileUtil.getFileContentAsString(decryptedOutputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);

		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

		//assertTrue(true);

    }

}
