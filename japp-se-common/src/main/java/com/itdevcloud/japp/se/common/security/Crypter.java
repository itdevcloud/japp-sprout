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
import java.io.OutputStream;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
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

	private Cipher cipher;
	private SecretKey key;
	private String transformation;
	private String algorithm;

	public Crypter() {
		try {
			this.transformation = CIPHER_DEFAULT_TRANSFORMATION;
			this.cipher = Cipher.getInstance(this.transformation);
			this.algorithm = getAlgorithmFromTransfermation(this.transformation);
			this.key = SecurityUtil.generateKey(this.algorithm);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Crypter(String transformationStr, String encodedKey) {
		try {
			if (StringUtil.isEmptyOrNull(transformationStr)) {
				this.transformation = CIPHER_DEFAULT_TRANSFORMATION;
			}else {
				this.transformation = transformationStr;
			}
			this.cipher = Cipher.getInstance(transformation);
			this.algorithm = getAlgorithmFromTransfermation(this.transformation);
			
			if (StringUtil.isEmptyOrNull(encodedKey)) {
				this.key = SecurityUtil.generateKey(this.algorithm);
			} else {
				this.key = new SecretKeySpec(StringUtil.decodeBase64(encodedKey), this.algorithm);
			}
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

	public String encryptText(String text) {
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

	public String decryptText(String text) {
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

	
	public String encrypt(InputStream inputStream) {
        if(inputStream == null) {
        	throw new RuntimeException("inputStream is null, check code!");
        }
		ByteArrayOutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			
	        IvParameterSpec iv = generateIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			
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

	public String encryptFile(String inputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName)) {
			throw new RuntimeException("inputFileName can not be null, check code!");
		}
		InputStream inputStream = null;
		try {
	        File inputFile = new File(inputFileName);
	        if(!inputFile.exists() || !inputFile.isFile()){
	        	if(!inputFileName.startsWith("/")) {
	        		inputFileName = "/" + inputFileName;
	        	}
	        	inputStream = this.getClass().getResourceAsStream(inputFileName);
	        }else {
	        	inputStream = new FileInputStream(inputFile);
	        }
	        if(inputStream == null) {
	        	throw new RuntimeException("inputStream is null, can find the inputFileName: '" + inputFileName + "'");
	        }
	        return encrypt(inputStream);
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
		}
	}
	

	public void encryptFileNoEncode(String inputFileName, String outputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
	        File inputFile = new File(inputFileName);
	        if(!inputFile.exists() || !inputFile.isFile()){
	        	if(!inputFileName.startsWith("/")) {
	        		inputFileName = "/" + inputFileName;
	        	}
	        	inputStream = this.getClass().getResourceAsStream(inputFileName);
	        }else {
	        	inputStream = new FileInputStream(inputFile);
	        }
	        if(inputStream == null) {
	        	throw new RuntimeException("inputStream is null, can find the inputFileName: '" + inputFileName + "'");
	        }
			outputStream = new FileOutputStream(new File(outputFileName));
			
			encryptNoEncode(inputStream, outputStream);
			
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
	public void encryptNoEncode(InputStream inputStream, OutputStream outputStream) {
        if(inputStream == null || outputStream == null) {
        	throw new RuntimeException("inputStream or outputStream is null, check code!");
        }
		try {
	        
			IvParameterSpec iv = generateIV();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			
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
		}
	}
	public String decryptFileNoEncode(String inputFileName) {
		
		InputStream inputStream = null;
		try {
	        File inputFile = new File(inputFileName);
	        if(!inputFile.exists() || !inputFile.isFile()){
	        	if(!inputFileName.startsWith("/")) {
	        		inputFileName = "/" + inputFileName;
	        	}
	        	inputStream = this.getClass().getResourceAsStream(inputFileName);
	        }else {
	        	inputStream = new FileInputStream(inputFile);
	        }
	        if(inputStream == null) {
	        	throw new RuntimeException("inputStream is null, can find the inputFileName: '" + inputFileName + "'");
	        }
	        return decryptNoEncode(inputStream);

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
		}
	}
	public String decryptNoEncode(InputStream inputStream) {
		if (inputStream == null ) {
			throw new RuntimeException("inputStream can not be null, check code!");
		}
		FileOutputStream outputStream = null;
		BufferedReader reader = null;
		try {
			IvParameterSpec iv = generateIV();
			byte[] ivBytes = new byte[iv.getIV().length];
			inputStream.read(ivBytes);
			
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));

            CipherInputStream cipherIn = new CipherInputStream(inputStream, cipher);
            InputStreamReader inputReader = new InputStreamReader(cipherIn);
            reader = new BufferedReader(inputReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				reader = null;
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

	public void decryptFileNoEncode(String inputFileName, String outputFileName) {
		if (StringUtil.isEmptyOrNull(inputFileName) || StringUtil.isEmptyOrNull(outputFileName)) {
			throw new RuntimeException("inputFileName and/or outputFileName can not be null, check code!");
		}
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
	        File inputFile = new File(inputFileName);
	        if(!inputFile.exists() || !inputFile.isFile()){
	        	if(!inputFileName.startsWith("/")) {
	        		inputFileName = "/" + inputFileName;
	        	}
	        	inputStream = this.getClass().getResourceAsStream(inputFileName);
	        }else {
	        	inputStream = new FileInputStream(inputFile);
	        }
	        if(inputStream == null) {
	        	throw new RuntimeException("inputStream is null, can find the inputFileName: '" + inputFileName + "'");
	        }
			outputStream = new FileOutputStream(new File(outputFileName));
			
			decryptNoEncode(inputStream, outputStream);

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

	public void decryptNoEncode(InputStream inputStream, OutputStream outputStream) {
        if(inputStream == null || outputStream == null) {
        	throw new RuntimeException("inputStream or outputStream is null, check code!");
        }
		try {
			IvParameterSpec iv = generateIV();
			byte[] ivBytes = new byte[iv.getIV().length];
			inputStream.read(ivBytes);
			
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));

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
		}
	}

}
