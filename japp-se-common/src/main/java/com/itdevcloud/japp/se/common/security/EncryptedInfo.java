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

import javax.crypto.spec.IvParameterSpec;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class EncryptedInfo {

	private String encryptedText;
	private String encryptionKey;
	private String transformation;
	private String algorithm;
	
	public void setEncryptedText(String encryptedText) {
		this.encryptedText = encryptedText;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String getEncryptedText() {
		return encryptedText;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getTransformation() {
		return transformation;
	}

	public void setTransformation(String transformation) {
		this.transformation = transformation;
	}

	@Override
	public String toString() {
		return "EncryptedInfo [encryptedText=" + encryptedText + ", encryptionKey=" + encryptionKey
				+ ", transformation=" + transformation + ", algorithm=" + algorithm + "]";
	}






}
