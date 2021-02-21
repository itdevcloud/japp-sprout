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
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.CommonUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AsyncEmailService implements AppFactoryComponentI {
	private Logger logger = LogManager.getLogger(AsyncEmailService.class);

	@Async
	public Future<String> sendEmail(String subject, String contentType, String content, EmailAddress fromAddr, EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments, String template, Map<String, Object> templateArgs) {
		logger.info("AsyncEmailService.sendEmail(), begin.......");
		try {
			AppComponents.emailService.send(subject, contentType, content, fromAddr, replyToAddr, toAddrList,  ccAddrList,
					bccAddrList, attachments, template, templateArgs);
			logger.info("AsyncEmailService.sendEmail(), end.......");
			return new AsyncResult<String>("Success");
		} catch (Throwable t) {
			logger.error(CommonUtil.getStackTrace(t));
			return new AsyncResult<String>("Email Sending Failed: " + t.getMessage());
		}
	}

}
