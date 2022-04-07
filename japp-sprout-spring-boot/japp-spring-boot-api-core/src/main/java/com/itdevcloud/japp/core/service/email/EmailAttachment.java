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
package com.itdevcloud.japp.core.service.email;

import org.springframework.core.io.InputStreamSource;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class EmailAttachment {
	private String contentType;
	private String filename;
	private String disposition;
	private String contentId;
	private String content;
	private InputStreamSource springInputStreamSource;

	
	public String getContentType() {
		return contentType;
	}


	public void setContentType(String type) {
		this.contentType = type;
	}


	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getDisposition() {
		return disposition;
	}


	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}


	public String getContentId() {
		return contentId;
	}


	public void setContentId(String contentId) {
		this.contentId = contentId;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	@Override
	public String toString() {
		return "EmailAttachment [type=" + contentType + ", filename=" + filename + ", disposition=" + disposition
				+ ", contentId=" + contentId + ", content size=" + content.length() + "]";
	}


	public InputStreamSource getSpringInputStreamSource() {
		return springInputStreamSource;
	}


	public void setSpringInputStreamSource(InputStreamSource springInputStreamSource) {
		this.springInputStreamSource = springInputStreamSource;
	}



}
