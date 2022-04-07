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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.itdevcloud.japp.se.common.security.AsymmetricCrypter;
import com.itdevcloud.japp.se.common.security.Crypter;
import com.itdevcloud.japp.se.common.security.EncryptedInfo;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class SecurityUtil {

	private static final String SIGNING_ALGORITHM = "SHA256withRSA";
	
	public static final String certPrefix = "-----BEGIN CERTIFICATE-----";
	public static final String certSuffix = "-----END CERTIFICATE-----";
	public static final String publicKeyPrefix = "-----BEGIN PUBLIC KEY-----";
	public static final String publicKeySuffix = "-----END PUBLIC KEY-----";

	public static EncryptedInfo encrypt(String clearText, String encodedKey) {
		if (StringUtil.isEmptyOrNull(clearText)) {
			return null;
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = null;
		if (StringUtil.isEmptyOrNull(encodedKey)) {
			crypter = new Crypter();
		}else {
			crypter = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		}
		
		ei.setEncryptedText(crypter.encryptText(clearText));
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
		return cryptor.decryptText(encryptedText);
	}

	public static EncryptedInfo encryptFile(String inputFileName, String encodedKey) {
		if (StringUtil.isEmptyOrNull(inputFileName) ) {
			throw new RuntimeException("inputFileName can not be null, check code!");
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = null;
		if (StringUtil.isEmptyOrNull(encodedKey)) {
			crypter = new Crypter();
		}else {
			crypter = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		}
		ei.setEncryptedText(crypter.encryptFile(inputFileName));
		ei.setEncryptionKey(crypter.getEncodedKeyString());
		ei.setAlgorithm(crypter.getAlgorithm());
		ei.setTransformation(crypter.getTransformation());
		return ei;
	}
	public static EncryptedInfo encryptFile(String inputFileName, String outputFileName, String encodedKey ) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		EncryptedInfo ei = new EncryptedInfo();
		Crypter crypter = null;
		if (StringUtil.isEmptyOrNull(encodedKey)) {
			crypter = new Crypter();
		}else {
			crypter = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		}
		crypter.encryptFileNoEncode(inputFileName, outputFileName);
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
		return cryptor.decryptFileNoEncode(inputFileName);
	}
	
	public static void decryptFile(String encodedKey, String inputFileName, String outputFileName) {
		if (StringUtil.isEmptyOrNull(encodedKey) || StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("encodedKey, inputFileName and/or outputFileName can not be null, check code!");
		}
		Crypter cryptor = new Crypter(Crypter.CIPHER_DEFAULT_TRANSFORMATION, encodedKey);
		cryptor.decryptFileNoEncode(inputFileName, outputFileName);
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
    
    public static String generateKeyFromPassword(String password, String salt, String algorithm){
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
	        String encodedKey = StringUtil.encodeBase64(key.getEncoded());
	        return encodedKey;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
    }

	public static String encryptAsym(String clearText, PrivateKey privateKey, PublicKey publicKey) {
		if (StringUtil.isEmptyOrNull(clearText)) {
			return null;
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		return crypter.encryptText(clearText);
	}
	
	public static String decryptAsym(String encryptedText, PrivateKey privateKey, PublicKey publicKey) {
		if (StringUtil.isEmptyOrNull(encryptedText)) {
			return null;
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		return crypter.decryptText(encryptedText);
	}

	public static String encryptFileAsym(String inputFileName, PrivateKey privateKey, PublicKey publicKey) {
		if (StringUtil.isEmptyOrNull(inputFileName) ) {
			return null;
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		return crypter.encryptFile(inputFileName);
	}
	public static void encryptFileAsym(String inputFileName, String outputFileName, PrivateKey privateKey, PublicKey publicKey ) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		crypter.encryptFileNoEncode(inputFileName, outputFileName);
	}
	
	public static String decryptFileAsym(String inputFileName, PrivateKey privateKey, PublicKey publicKey) {
		if (StringUtil.isEmptyOrNull(inputFileName)) {
			throw new RuntimeException("iputFileName can not be null, check code!");
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		return crypter.decryptFileNoEncode(inputFileName);
	}
	
	public static void decryptFile(String inputFileName, String outputFileName, PrivateKey privateKey, PublicKey publicKey) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		if (privateKey == null || publicKey == null) {
			throw new RuntimeException("publicKey and/or privateKey can not be null, check code!");
		}		
		AsymmetricCrypter crypter = new AsymmetricCrypter(null, privateKey, publicKey);
		crypter.decryptFileNoEncode(inputFileName, outputFileName);
	}
    

    
	/**
	 * parse a String to a X.509 Certificate object.
	 * 
	 */
	public static Certificate getCertificateFromString(String certStr) {
		if (StringUtil.isEmptyOrNull(certStr)) {
			return null;
		}
		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		try {
			if(certStr.indexOf(certPrefix) < 0 ) {
				//change to pem string
				certStr = certPrefix + System.lineSeparator() + certStr + System.lineSeparator() + certSuffix;
			}
			// logger.debug(".........certStr=" + certStr);
			in = new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8));
			bis = new BufferedInputStream(in);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			while (bis.available() <= 0) {
				// logger.debug(".........certStr=" + certStr);
				String err = "Can't Parse certificate: ...Stop....!!!!!!!!!!!!!!";
				System.out.println(err);
				throw new RuntimeException(err);
			}
			Certificate cert = cf.generateCertificate(bis);
			bis.close();
			bis = null;
			in.close();
			in = null;
			return cert;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}

		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getCertificatePemString(Certificate certificate, boolean insertLineSeparator) {
		if (certificate == null) {
			return null;
		}
		byte[] bytes;
		try {
			bytes = certificate.getEncoded();
			String encodedKey = null;
			if (bytes != null) {
				encodedKey = new String(Base64.getEncoder().encodeToString(bytes));
				if(insertLineSeparator) {
					encodedKey = certPrefix + System.lineSeparator() + encodedKey
							+ System.lineSeparator() + certSuffix;
				}else {
					encodedKey = certPrefix + encodedKey + certSuffix;
				}
			}
			return encodedKey;
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String getPublicKeyPemString(PublicKey key, boolean insertLineSeparator) {
		if (key == null) {
			return null;
		}
		byte[] bytes = key.getEncoded();
		String encodedKey = null;
		if (bytes != null) {
			encodedKey = new String(Base64.getEncoder().encodeToString(bytes));
			if(insertLineSeparator) {
				encodedKey = publicKeyPrefix + System.lineSeparator() + encodedKey + System.lineSeparator()
						+ publicKeySuffix;
			}else {
				encodedKey = publicKeyPrefix +  encodedKey + publicKeySuffix;
			}
		}
		return encodedKey;

	}

	/**
	 * parse a String to a X.509 PublicKey object.
	 * 
	 */
	public static PublicKey getPublicKeyFromString(String keyStr, String algorithm) {
		if (StringUtil.isEmptyOrNull(keyStr)) {
			return null;
		}
		if(StringUtil.isEmptyOrNull(algorithm)) {
			algorithm = AsymmetricCrypter.CIPHER_DEFAULT_ALGORITHM;
		}
		try {
			keyStr = keyStr.replaceAll("(\\r|\\n)", "");
			keyStr = keyStr.replace(publicKeyPrefix, "");
			keyStr = keyStr.replace(publicKeySuffix, "");
			byte[] decodedStr = Base64.getDecoder().decode(keyStr);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedStr);
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			if (kf != null) {
				return kf.generatePublic(spec);
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		if (certificate == null) {
			return null;
		}
		return certificate.getPublicKey();
	}

	public static String sign(PrivateKey privateKey, String text) {
		if (privateKey == null || StringUtil.isEmptyOrNull(text)) {
			return null;
		}

		try {
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initSign(privateKey);
			byte[] textBytes = StringUtil.getBytes(text);
			signature.update(textBytes);
			byte[] signatureBytes = signature.sign();

			return StringUtil.getString(Base64.getEncoder().encode(signatureBytes));

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Sign message failed.", e);
		}
	}

	public static boolean verifySignature(PublicKey publicKey, String signatureStr, String text) {
		if (publicKey == null || StringUtil.isEmptyOrNull(signatureStr) ||  StringUtil.isEmptyOrNull(text)) {
			return false;
		}

		try {
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initVerify(publicKey);
			byte[] textBytes = StringUtil.getBytes(text);

			signature.update(textBytes);
			byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);

			boolean isCorrect = signature.verify(signatureBytes);

			return isCorrect;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Signature verification failed.", e);
		}
	}
	

}
