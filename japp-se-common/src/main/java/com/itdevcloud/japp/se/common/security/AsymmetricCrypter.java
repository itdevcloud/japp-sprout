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
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AsymmetricCrypter {

	public static final String CIPHER_DEFAULT_ALGORITHM = "RSA";

	private Cipher cipher;
	private String algorithm;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public AsymmetricCrypter(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
		try {
			if(StringUtil.isEmptyOrNull(algorithm)) {
				algorithm = CIPHER_DEFAULT_ALGORITHM;
			}
			this.algorithm = algorithm;
			this.cipher = Cipher.getInstance(this.algorithm);
			if(privateKey == null || publicKey == null) {
				throw new RuntimeException("privateKey or publicKey is null, when init AsymmetricCrypter, check code!");
			}
			this.privateKey = privateKey;
			this.publicKey = publicKey;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public Cipher getCipher() {
		return cipher;
	}

	
	public String getAlgorithm() {
		return algorithm;
	}
	


	public String encryptText(String text) {
		if (StringUtil.isEmptyOrNull(text)) {
			return null;
		}
		return StringUtil.encodeBase64(encrypt(text.getBytes()));
	}

	public byte[] encrypt(byte[] bytes) {
		try {
			this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			byte[] encryptedBytes = cipher.doFinal(bytes);
			
			return encryptedBytes;
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
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return  cipher.doFinal(bytes);
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
			
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
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
	        
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
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
		
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

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
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

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
