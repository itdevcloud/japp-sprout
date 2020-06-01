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

import java.util.List;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public interface EmailServiceProvider {
	/**
	 * Send an email.
	 * @param subject subject
	 * @param contentType content type
	 * @param content content
	 * @param fromAddr from address
	 * @param replyToAddr replyto address
	 * @param toAddrList to address list
	 * @param ccAddrList cc address list
	 * @param bccAddrList bcc address list
	 * @param attachments attachment list
	 * @throws EmailException
	 */
	public void sendEmail(String subject,
			String contentType,
			String content,
			EmailAddress fromAddr,
			EmailAddress replyToAddr,
			List<EmailAddress> toAddrList,
			List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList,
			List<EmailAttachment> attachments) throws EmailException;

}
