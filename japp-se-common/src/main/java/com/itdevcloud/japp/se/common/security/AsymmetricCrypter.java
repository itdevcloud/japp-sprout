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
import java.security.cert.Certificate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.itdevcloud.japp.se.common.util.SecurityUtil;
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
	

	public PrivateKey getPrivateKey() {
		return privateKey;
	}


	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}


	public PublicKey getPublicKey() {
		return publicKey;
	}


	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}


	public String encryptText(String text) {
		if (StringUtil.isEmptyOrNull(text)) {
			return null;
		}
		return StringUtil.encodeBase64(encrypt(text.getBytes()));
	}

	public byte[] encrypt(byte[] bytes) {
		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		try {
			if(this.publicKey != null) {
				this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			}else {
				this.cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			}

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

		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		try {
			if(this.privateKey != null) {
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
			}else {
				this.cipher.init(Cipher.DECRYPT_MODE, publicKey);
			}
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
		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		ByteArrayOutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			
			if(this.publicKey != null) {
				this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			}else {
				this.cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			}
			
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
		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		try {
	        
			if(this.publicKey != null) {
				this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			}else {
				this.cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			}
			
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
		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		FileOutputStream outputStream = null;
		BufferedReader reader = null;
		try {
		
			if(this.privateKey != null) {
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
			}else {
				this.cipher.init(Cipher.DECRYPT_MODE, publicKey);
			}

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
		if(this.privateKey == null && this.publicKey == null) {
			throw new RuntimeException("private key and public key can not be both null, check code!");
		}
		try {
			if(this.privateKey != null) {
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
			}else {
				this.cipher.init(Cipher.DECRYPT_MODE, publicKey);
			}

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

	public static void main(String[] args) {
		SecurityUtil securityService = new SecurityUtil();
//		String clearText = "abcdefg";
//		EncryptionResult encryptionResult = securityService.encrypt(clearText);
//		logger.info("encryptionResult........" + encryptionResult);
//		
//		System.out.println("...............1.............");
//		String totpSecret = Base32.random();
//		System.out.println("..............totpSecret............" + totpSecret);
//		logger.info("totpSecret........" + totpSecret);

		String certStr = "MIIDTzCCAjegAwIBAgIEdf3ggjANBgkqhkiG9w0BAQsFADBYMQswCQYDVQQGEwJjYTELMAkGA1UECBMCb24xEDAOBgNVBAcTB3Rvcm9udG8xDDAKBgNVBAoTA210bzEMMAoGA1UECxMDbXRvMQ4wDAYDVQQDEwV0ZXN0IDAeFw0xODAzMjkxNTI2MjVaFw0yODAzMjYxNTI2MjVaMFgxCzAJBgNVBAYTAmNhMQswCQYDVQQIEwJvbjEQMA4GA1UEBxMHdG9yb250bzEMMAoGA1UEChMDbXRvMQwwCgYDVQQLEwNtdG8xDjAMBgNVBAMTBXRlc3QgMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt1drXlapnJpjgolASeSyEHy6uouEIvHDatI7T7jTHZ29KGVI4jQ0o8EoebuhtOzN1cv7WT+tiWASo2t9CuwQ5er6cXCNCk6FyX7QhcUquHAHSLBA54OffXhWI2wj1xTvSpZlJ9t24Sd9HUI2dDyWH4XFfBN2hL43vFraeF8WjCccpDcmwykaLa6cSRPExlQ3JVUN06S3HfIHvXNsDTfijAypCBQ2fI3COuVOcgJLbi6Rj1mHe3v5PK8jxvIlg8hrcW5B3F28ZfBps/mLNisjHjEt+Bm7GniY5u2erjJ/6NZYLGuQh7w1CrlJUiD9/sPNj99kkEozhZUYdjnGyjxVWQIDAQABoyEwHzAdBgNVHQ4EFgQULEYwcCzZhXHDAWeRcM7jMRsTEsUwDQYJKoZIhvcNAQELBQADggEBAKz/+lf4spxZdUONnFNMyFKp4a//u4h6N6cF4vX4f9/kevm4ZeeQC9l0coYfuuIjiD3JQrzHWAxF9ki04SDPWdz9eYztmJJ8ogGJUJ3ZXpqQvKvr0iKuoEhTBXbeOv6HFfQ/gX1JpecLpI6Cv1rgdliV/b0tfGFmOlK82Py//Z7mO9y4n6pdmeXqvDQdTzuObxG3BuVaolBOQ1JZlItjzzJltY49dGYyUOb/UgxyLdNfvt2q+ezq9n2FgMGP7SgriKZf1mFn7pI0WrIu8hclVlwszeNmikW+F5jr7QwaLW4W7jOhQk1DSoXzkEmoe0NF2Aw09HM8oqTqLnIeDnZr8o8=";
		String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt1drXlapnJpjgolASeSyEHy6uouEIvHDatI7T7jTHZ29KGVI4jQ0o8EoebuhtOzN1cv7WT+tiWASo2t9CuwQ5er6cXCNCk6FyX7QhcUquHAHSLBA54OffXhWI2wj1xTvSpZlJ9t24Sd9HUI2dDyWH4XFfBN2hL43vFraeF8WjCccpDcmwykaLa6cSRPExlQ3JVUN06S3HfIHvXNsDTfijAypCBQ2fI3COuVOcgJLbi6Rj1mHe3v5PK8jxvIlg8hrcW5B3F28ZfBps/mLNisjHjEt+Bm7GniY5u2erjJ/6NZYLGuQh7w1CrlJUiD9/sPNj99kkEozhZUYdjnGyjxVWQIDAQAB";
		PublicKey publicKey = SecurityUtil.getPublicKeyFromString(publicKeyStr, null);
		Certificate certificate = SecurityUtil.getCertificateFromString(certStr);
		
		String message = "hello world!";
		String signatureStr = "azKo7ikSRsvGDf6T40d5tA+pyfyFbxf/vbL18woU+aGd9vF9qGzytWp870cmeuDqriTQX8PDC6zacob8uB+LGMhvPoBaoevlzl1/ixUFNnv26pxh4OJrawCMeEr70C/9VRJeHVmfygGHyGn6wRHdm3ZKiuxR8V+rpvCUSwbnuaGlkR57miTxsswlULBQAFsPEch33Y4RE9/kxXxlCqwXGGA2t7zEFYwG0kJKlSNWNjQ3We9tuxpBqNDH48JpNs1K2NI9HFjHkW1z0OzJtg0QEdEZrJASMkA5TgoY8oYWeoO+hWXpgLGCcv5IRDip6XxVXhk34JJxaDjT/6r7lR/ybQ==";
		String encryptedMessage = "j2qbX5JdxPiFzoUIl1jHWa1G/Luea9iR5JjgQlo6onZ71MxKWk7rCMc99G7G+UB6ZJKHxjjTZ8EmYBwp84nkKnNHMtk9jeaRHMPDmrdWpCt1eVV/OBm0mt/TTT2YvSR8rFgyvfuVfknSXjbGR85XRDLF/3IO4bQUGYWJJMfFvM0r1AT0mNK5wx7IsDS7WZyW/njaRUKfw1eQNR0wZG5LA1HpJPdhx41zBH+yXXYNkXNYA+IAuwzM+TycUCC9WcvlProgDKLnFqbA23IcrSCgx2oQNW3e2oVOE5HuqP+x0QBYs5S+1uAW0jYs/z/N+F75jIvJmFnEebvwIlYkxb0Fhw==";
		boolean isVerified = SecurityUtil.verifySignature(publicKey, signatureStr, message);
		System.out.printf("is verified = " + isVerified);
		System.out.printf("\ndecrypted message = " + SecurityUtil.decryptAsym(encryptedMessage, null, publicKey));
		
		String encrypted12345 = "U7RHSPJXqZHmEqwi7a4upIEtAZTA1bxhwomVnWi2gsIpsSO5HQr5IfScWwCKR/5jjMiMLHZirAb0ooZ4rGc86qmDmrHVBIc/sc+b5Lcv1LCaVIfYiBCYLKU4Vw1AGa/evNXcWAL/laL7wz1KycdvDAaVFKaH2RMcTqYC5IRcJEl2h2dFQUmaCCvkanPsj0mJ0KVsP1q+pJ4YpqZaFfI9L+3O5cnHgd+7WqWq2txZ5KPukO1izfxuw+nzLTYKQUT0NiKOIPwGhC+aXKrlkNhI/8SlFUnF8Og+w0cSgM0+a9u2/8kK2cCullR+mvqF7MttoaMun6557c8Mh+CAG0Kqyg==";
	}

}
