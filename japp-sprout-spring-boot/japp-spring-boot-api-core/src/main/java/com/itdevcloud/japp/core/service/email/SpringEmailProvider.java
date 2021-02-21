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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class SpringEmailProvider implements EmailServiceProvider {

	@Autowired
	private JavaMailSender sender;

	private static final Logger logger = LogManager.getLogger(SpringEmailProvider.class);

	@Override
	public void sendEmail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments) throws EmailException {

		logger.debug("OnRelayEmailService.createAndSendEmail() start.......");
		if (toAddrList == null || toAddrList.isEmpty()) {
			throw new EmailException(601, "toAddrList is null, no email will be sent.......!");
		}
		try {
			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			String[] toArr = this.getAddressArray(toAddrList);
			logger.debug("createAndSendEmail......to = " + Arrays.toString(toArr));
			helper.setTo(toArr);
			if (fromAddr != null && !StringUtil.isEmptyOrNull(fromAddr.getAddress())) {
				helper.setFrom(fromAddr.getAddress());
				logger.debug("createAndSendEmail......from = " + fromAddr);
			}
			if (replyToAddr != null && !StringUtil.isEmptyOrNull(replyToAddr.getAddress())) {
				helper.setReplyTo(replyToAddr.getAddress());
				logger.debug("createAndSendEmail......replyTo = " + replyToAddr);
			}

			if (ccAddrList != null && !ccAddrList.isEmpty()) {
				String[] ccArr = this.getAddressArray(ccAddrList);
				helper.setCc(ccArr);
				logger.debug("createAndSendEmail......cc = " + Arrays.toString(ccArr));
			}
			if (bccAddrList != null && !bccAddrList.isEmpty()) {
				String[] bccArr = this.getAddressArray(bccAddrList);
				helper.setBcc(bccArr);
				logger.debug("createAndSendEmail......bcc = " + Arrays.toString(bccArr));
			}
			if (contentType == null || contentType.equalsIgnoreCase("text/plain")) {
				helper.setText(content);
			} else if (contentType.equalsIgnoreCase("text/html")) {
				helper.setText(content, true);
			} else {
				throw new EmailException(602, "currently only support text/plain and text/html content type......!");

			}
			helper.setSubject(subject);

			// Attachments
			if (attachments != null && !attachments.isEmpty()) {
				for (EmailAttachment a : attachments) {
					if (a.getSpringInputStreamSource() != null) {
						helper.addAttachment(a.getFilename(), a.getSpringInputStreamSource(), a.getType());
					} else if (a.getContent() != null) {
						InputStream in = new ByteArrayInputStream(a.getContent().getBytes(StandardCharsets.UTF_8));
						helper.addAttachment(a.getFilename(), new InputStreamResource(in), a.getType());
					} else {
						throw new EmailException(603, "un supported attachment type, no email will be sent.......!");
					}
				}
			}

			sender.send(message);
		} catch (Exception e) {
			throw new EmailException(604, CommonUtil.getStackTrace(e));
		}

	}

	private String[] getAddressArray(List<EmailAddress> addrList) {
		if (!isValidAddressList(addrList)) {
			return null;
		}
		String[] arr = new String[addrList.size()];
		for (int i = 0; i < addrList.size(); i++) {
			arr[i] = addrList.get(i).getAddress();
		}
		return arr;

	}

	private boolean isValidAddressList(List<EmailAddress> addrList) {
		if (addrList == null || addrList.isEmpty()) {
			logger.debug("email address list is null......!!!");
			return false;
		}
		for (EmailAddress addr : addrList) {
			if (!isValidAddress(addr)) {
				return false;
			}
		}
		return true;
	}

	private boolean isValidAddress(EmailAddress addr) {
		if (addr == null || StringUtil.isEmptyOrNull(addr.getAddress()) || !addr.getAddress().contains("@")) {
			logger.debug("email address is not valid......addr = " + addr);
			return false;
		}
		return true;
	}
}
