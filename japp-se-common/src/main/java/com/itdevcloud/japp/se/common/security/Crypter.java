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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class Crypter {

	private Cipher cipher;
	private SecretKey key;
	private IvParameterSpec iv;

	public Crypter() {
		try {
			this.cipher = Cipher.getInstance(SecurityUtil.DEFAULT_CIPHER_SPEC);
			this.key = SecurityUtil.generateKey(SecurityUtil.DEFAULT_CIPHER_ALGO);
			this.iv = generateIV();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Crypter(String algorithm, String encodedKey) {
		try {
			String spec = null;
			if (StringUtil.isEmptyOrNull(algorithm)) {
				algorithm = SecurityUtil.DEFAULT_CIPHER_ALGO;
				spec = SecurityUtil.DEFAULT_CIPHER_SPEC;
			} else {
				spec = algorithm + "/CBC/PKCS5Padding";
			}
			this.cipher = Cipher.getInstance(spec);
			if (StringUtil.isEmptyOrNull(encodedKey)) {
				this.key = SecurityUtil.generateKey(algorithm);
			} else {
				this.key = new SecretKeySpec(StringUtil.decodeBase64(encodedKey), algorithm);
			}
			this.iv = generateIV();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Crypter(String algorithm, SecretKey key) {
		try {
			String spec = null;
			if (StringUtil.isEmptyOrNull(algorithm)) {
				algorithm = SecurityUtil.DEFAULT_CIPHER_ALGO;
				spec = SecurityUtil.DEFAULT_CIPHER_SPEC;
			} else {
				spec = algorithm + "/CBC/PKCS5Padding";
				;
			}
			this.cipher = Cipher.getInstance(spec);
			if (key == null) {
				this.key = SecurityUtil.generateKey(algorithm);
			} else {
				this.key = key;
			}
			this.iv = generateIV();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	public Cipher getCipher() {
		return cipher;
	}

	public IvParameterSpec getIv() {
		return iv;
	}

	public SecretKey getKey() {
		return key;
	}
	
	public String getKeyString() {
		return StringUtil.encodeBase64(key.getEncoded());
	}
	
	private IvParameterSpec generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[cipher.getBlockSize()];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	private  IvParameterSpec getIVfromMessage(byte[] bytes) {
		int ivSize = generateIV().getIV().length;
		byte[] ivBytes = new byte[ivSize]; 

		if (bytes.length <= ivSize) {
			throw new RuntimeException("Message is too short - can't contain Initial Vector");
		}

		System.arraycopy(bytes, 0, ivBytes, 0, ivBytes.length);
		return new IvParameterSpec(ivBytes);
	}

	public String encrypt(String text) {
		if (StringUtil.isEmptyOrNull(text)) {
			return null;
		}
		return StringUtil.encodeBase64(encrypt(text.getBytes()));
	}

	public byte[] encrypt(byte[] bytes) {
		try {
			IvParameterSpec iv = generateIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);

			byte[] encryptedBytes = cipher.doFinal(bytes);
			byte[] ivBytes = iv.getIV();
			//add iv at the beginning of encryptedBytes - when decrypt, get IV from the encrypted text
			byte[] message = new byte[ivBytes.length + encryptedBytes.length];
			System.arraycopy(ivBytes, 0, message, 0, ivBytes.length);
			System.arraycopy(encryptedBytes, 0, message, ivBytes.length, encryptedBytes.length);
			return message;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public String decrypt(String text) {
		if (StringUtil.isEmptyOrNull(text)) {
			return null;
		}
		return StringUtil.getString(decrypt(StringUtil.decodeBase64(text)));
	}

	public byte[] decrypt(byte[] bytes) {

		try {
			IvParameterSpec iv = getIVfromMessage(bytes);
			int ivSize = iv.getIV().length;
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			return  cipher.doFinal(bytes, ivSize, bytes.length - ivSize);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void encryptFile(File inputFile, File outputFile) {
		if (inputFile == null || outputFile == null) {
			throw new RuntimeException("inputFile and/or outputFile can not be null, check code!");
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			inputStream = new FileInputStream(inputFile);
			outputStream = new FileOutputStream(outputFile);
			byte[] bytes = new byte[64];
			int bytesRead;
			while ((bytesRead = inputStream.read(bytes)) != -1) {
				byte[] output = cipher.update(bytes, 0, bytesRead);
				if (output != null) {
					outputStream.write(output);
				}
			}
			byte[] outputBytes = cipher.doFinal();
			if (outputBytes != null) {
				outputStream.write(outputBytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				inputStream = null;
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outputStream = null;
			}
		}
	}

	public void decryptFile(File encryptedFile, File decryptedFile) {
		if (encryptedFile == null || decryptedFile == null) {
			throw new RuntimeException("encryptedFile and/or decryptedFile can not be null, check code!");
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			inputStream = new FileInputStream(encryptedFile);
			outputStream = new FileOutputStream(decryptedFile);
			byte[] bytes = new byte[64];
			int bytesRead;
			while ((bytesRead = inputStream.read(bytes)) != -1) {
				byte[] output = cipher.update(bytes, 0, bytesRead);
				if (output != null) {
					outputStream.write(output);
				}
			}
			byte[] output = cipher.doFinal();
			if (output != null) {
				outputStream.write(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				outputStream = null;
			}
		}
	}

	public SealedObject encryptObject(Serializable object) {
		if (object == null) {
			return null;
		}
		SealedObject sealedObject = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			sealedObject = new SealedObject(object, cipher);
			return sealedObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Serializable decryptObject(SealedObject sealedObject) {

		if (sealedObject == null) {
			return null;
		}
		Serializable unsealObject = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			unsealObject = (Serializable) sealedObject.getObject(cipher);
			return unsealObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
