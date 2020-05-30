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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class Crypter extends CryptoBase {
	private static final String CIPHER_ALGO = "AES";
	private static final String CIPHER_SPEC = CIPHER_ALGO + "/CBC/PKCS5Padding";
	//private static final Logger logger = LogManager.getLogger(Crypter.class);

	private Cipher cipher;
	private Key key;

	public Crypter()  {
		try {
			this.cipher = Cipher.getInstance(CIPHER_SPEC);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.key = generateKey();
	}

	public Crypter(String key)  {
		this(decodeBase64(key));
	}

	public Crypter(byte[] key)  {
		this(key, null);
	}

	public Crypter(byte[] key, byte[] iv)  {
		try {
			this.cipher = Cipher.getInstance(CIPHER_SPEC);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.key = new SecretKeySpec(key, CIPHER_ALGO);
	}

	public String encrypt(String text) {
		return encodeBase64(encrypt(getBytes(text)));
	}

	public byte[] encrypt(byte[] buf) {
		IvParameterSpec iv = generateIV();
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);

			byte[] encryptedTextBytes = cipher.doFinal(buf);
			byte[] ivBytes = iv.getIV(); // always 128 bit for AES

			byte[] message = new byte[ivBytes.length + encryptedTextBytes.length];
			System.arraycopy(ivBytes, 0, message, 0, ivBytes.length);
			System.arraycopy(encryptedTextBytes, 0, message, ivBytes.length, encryptedTextBytes.length);
			return message;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public String decrypt(String message) {
		return getString(decrypt(decodeBase64(message)));
	}

	public byte[] decrypt(byte[] message) {
		IvParameterSpec iv = getIVfromMessage(message);
		int ivSize = iv.getIV().length;

		try {
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			return cipher.doFinal(message, ivSize, message.length - ivSize);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getKey() {
		return DatatypeConverter.printBase64Binary(key.getEncoded());
	}

	private Key generateKey() {
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance(CIPHER_ALGO);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		kg.init(256);
		return kg.generateKey();
	}

	private IvParameterSpec generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[cipher.getBlockSize()];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	private IvParameterSpec getIVfromMessage(byte[] message) {
		int ivSize = generateIV().getIV().length;
		byte[] ivBytes = new byte[ivSize]; 

		if (message.length <= ivSize) {
			throw new RuntimeException("Message is too short - can't contain Initial Vector");
		}

		System.arraycopy(message, 0, ivBytes, 0, ivBytes.length);
		return new IvParameterSpec(ivBytes);
	}
}
