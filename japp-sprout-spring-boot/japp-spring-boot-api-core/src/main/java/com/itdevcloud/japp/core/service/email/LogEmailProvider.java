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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.Logger;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class LogEmailProvider implements EmailServiceProvider {
	//private static final Logger logger = LogManager.getLogger(LogEmailProvider.class);
	private static final Logger logger = LogManager.getLogger(LogEmailProvider.class);

	@Override
	public void sendEmail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments) {
        String sb = "Email: subject=" + subject + ", contentType=" + contentType +
                ", content=" + content +
                ", attachments=" + attachments +
                ", fromAddr=" + fromAddr +
                ", replyToAddr=" + replyToAddr +
                ", toAddrList=" + toAddrList +
                ", ccAddrList=" + ccAddrList +
                ", bccAddrList=" + bccAddrList;
        logger.info(sb);
	}
}
