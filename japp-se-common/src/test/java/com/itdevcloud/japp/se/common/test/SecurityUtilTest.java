package com.itdevcloud.japp.se.common.test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itdevcloud.japp.se.common.security.EncryptedInfo;
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
	public void defaultEncryptionTest() {
		String clearText1 = "Hello World!";
		EncryptedInfo encryptedInfo = SecurityUtil.encrypt(clearText1);
		System.out.println(""+encryptedInfo);
		if(encryptedInfo == null || StringUtil.isEmptyOrNull(encryptedInfo.getEncryptedText())) {
			assertFalse(true, "encryptedInfo is empty");
		}
		String clearText2 = SecurityUtil.decrypt(encryptedInfo.getEncryptionKey(), encryptedInfo.getEncryptedText());
		assertEquals(clearText1, clearText2);
	}

}
