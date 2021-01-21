package com.itdevcloud.japp.se.common.test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.security.Crypter;
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
		EncryptedInfo encryptedInfo = SecurityUtil.encrypt(clearText1);
		System.out.println(""+encryptedInfo);
		if(encryptedInfo == null || StringUtil.isEmptyOrNull(encryptedInfo.getEncryptedText())) {
			assertFalse(true, "encryptedInfo is empty");
		}
		String clearText2 = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		assertEquals(clearText1, clearText2);
	}

	@Test
    public void encryptFileToString_decryptStringTest() {

        String inputfileName = "/Test-1.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName);
		System.out.println(""+encryptedInfo);
		
		String decryptedContent = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

    }
	
	@Test
    public void encryptFileToFile_decryptFileTest() {

        String inputfileName = "/Test-1.txt";
        String outputfileName = "Test-1-encrypted.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, outputfileName);
		
		String decryptedContent = SecurityUtil.decryptFile(encryptedInfo.getEncryptionKey(), outputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);
		
		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

		//assertTrue(true);

    }
	
	@Test
    public void encryptFileToFile_decryptFileToFileTest() {

        String inputfileName = "/Test-1.txt";
        String encryptedOutputfileName = "Test-1-encrypted.txt";
        String decryptedOutputfileName = "Test-1-decrypted.txt";
		
		EncryptedInfo encryptedInfo = SecurityUtil.encryptFile(inputfileName, encryptedOutputfileName);
		SecurityUtil.decryptFile(encryptedInfo.getEncryptionKey(), encryptedOutputfileName, decryptedOutputfileName);
		
		String decryptedContent = FileUtil.getFileContentAsString(decryptedOutputfileName);
		
		System.out.println("---Decrypted content---\n"+decryptedContent);

		assertTrue(decryptedContent.startsWith("Test-1"), "decrypted text start with 'Test-1");

		//assertTrue(true);

    }

}
