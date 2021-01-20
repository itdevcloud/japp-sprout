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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * Class Definition
 *
 *A transformation is a string that describes the operation (or set of operations) to be performed on the given input, 
 *to produce some output. A transformation always includes the name of a cryptographic algorithm (e.g., AES), 
 *and may be followed by a feedback mode and padding scheme.
 *A transformation is of the form:
 *    "algorithm/mode/padding" or
 *    "algorithm"
 *    
 * @author Marvin Sun
 * @since 1.0.0
 */
public class Crypter {

	public static final String CIPHER_DEFAULT_ALGORITHM = "AES";
	public static final String CIPHER_DEFAULT_TRANSFORMATION = CIPHER_DEFAULT_ALGORITHM + "/CBC/PKCS5Padding";
	public static final int CIPHER_DEFAULT_KEY_SIZE = 256;
	public static final int CIPHER_AES_BLOCK_SIZE_BITS = 128;
	public static final int CIPHER_AES_BLOCK_SIZE_BYTES = 16;

	private Cipher cipher;
	private SecretKey key;
	private String transformation;
	private String algorithm;
	//private IvParameterSpec iv;

	public Crypter() {
		try {
			this.transformation = CIPHER_DEFAULT_TRANSFORMATION;
			this.cipher = Cipher.getInstance(this.transformation);
			this.algorithm = getAlgorithmFromTransfermation(this.transformation);
			this.key = SecurityUtil.generateKey(this.algorithm);
			//this.iv = generateIV();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Crypter(String transformationStr, String encodedKey) {
		try {
			if (StringUtil.isEmptyOrNull(transformation)) {
				this.transformation = CIPHER_DEFAULT_TRANSFORMATION;
			}else {
				this.transformation = transformationStr;
			}
			this.cipher = Cipher.getInstance(transformation);
			this.algorithm = getAlgorithmFromTransfermation(this.transformation);
			
			if (StringUtil.isEmptyOrNull(encodedKey)) {
				this.key = SecurityUtil.generateKey(this.algorithm);
			} else {
				this.key = new SecretKeySpec(StringUtil.decodeBase64(encodedKey), algorithm);
			}
			//this.iv = generateIV();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Cipher getCipher() {
		return cipher;
	}


	public SecretKey getKey() {
		return key;
	}
	
	public String getEncodedKeyString() {
		return StringUtil.encodeBase64(key.getEncoded());
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	
	public String getTransformation() {
		return this.transformation;
	}

	private String getAlgorithmFromTransfermation(String transformationStr) {
		if(StringUtil.isEmptyOrNull(transformationStr)) {
			throw new RuntimeException("transformationStr can not be null, check code!");
		}
		int idx = transformationStr.indexOf("/");
		if(idx == -1) {
			return transformationStr;
		}else {
			return transformationStr.substring(0,  idx);
		}
	}
	private IvParameterSpec generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] newIv = new byte[this.cipher.getBlockSize()];
		random.nextBytes(newIv);
		return new IvParameterSpec(newIv);
	}

	private  IvParameterSpec getIVfromMessage(byte[] bytes) {
		int ivSize = generateIV().getIV().length;
		byte[] ivBytes = new byte[ivSize]; 

		if (bytes.length <= ivSize) {
			throw new RuntimeException("Message is too short - can't detect Initial Vector");
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
			IvParameterSpec ivFromBytes = getIVfromMessage(bytes);
			int ivSize = ivFromBytes.getIV().length;
			cipher.init(Cipher.DECRYPT_MODE, key, ivFromBytes);
			return  cipher.doFinal(bytes, ivSize, bytes.length - ivSize);
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public String encryptFile(String inputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName)) {
			throw new RuntimeException("inputFileName can not be null, check code!");
		}
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			IvParameterSpec iv = generateIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			inputStream = this.getClass().getResourceAsStream(inputFileName);
			outputStream = new ByteArrayOutputStream();
			
			//add iv at the beginning of encryptedBytes - when decrypt, get IV from the beginning encrypted file
			outputStream.write(iv.getIV());
			
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
			return StringUtil.encodeBase64(outputStream.toByteArray());
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

	public void encryptFile(String inputFileName, String outputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			IvParameterSpec iv = generateIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			inputStream = this.getClass().getResourceAsStream(inputFileName);
			outputStream = new FileOutputStream(new File(outputFileName));
			
			//add iv at the beginning of encryptedBytes - when decrypt, get IV from the beginning encrypted file
			outputStream.write(iv.getIV());
			
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

	public String decryptFile(String inputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName) ) {
			throw new RuntimeException("inputFileName can not be null, check code!");
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			IvParameterSpec iv = generateIV();
			inputStream = new FileInputStream(inputFileName);
			byte[] ivBytes = new byte[iv.getIV().length];
			inputStream.read(ivBytes);
			
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));

            CipherInputStream cipherIn = new CipherInputStream(inputStream, cipher);
            InputStreamReader inputReader = new InputStreamReader(cipherIn);
            BufferedReader reader = new BufferedReader(inputReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();

		} catch (Exception e) {
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

	public void decryptFile(File encryptedFile, File decryptedFile) {
		if (encryptedFile == null || decryptedFile == null) {
			throw new RuntimeException("encryptedFile and/or decryptedFile can not be null, check code!");
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			IvParameterSpec iv = generateIV();
			inputStream = new FileInputStream(encryptedFile);
			byte[] ivBytes = new byte[iv.getIV().length];
			inputStream.read(ivBytes);
			
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));

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


}
