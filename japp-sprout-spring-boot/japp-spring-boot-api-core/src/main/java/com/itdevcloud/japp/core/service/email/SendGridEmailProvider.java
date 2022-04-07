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

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.SecurityUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.sendgrid.Client;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class SendGridEmailProvider implements EmailServiceProvider {

	private static final Logger logger = LogManager.getLogger(SendGridEmailProvider.class);

	private static String encryptionKey = "ygaZGekQzlYIb2tKMxad2jMvrAGHFkxFGAcd98ojJSs=";

	@Value("${"+AppConfigKeys.JAPPCORE_EMAIL_SENDGRID_API_KEY +":}")
	private String sendgridApiKey;

	@Value("${"+AppConfigKeys.JAPPCORE_HTTP_PROXY_SERVER +":}")
	private String httpProxyServer;

	@Value("${"+AppConfigKeys.JAPPCORE_HTTP_PROXY_PORT +":0}")
	private int httpProxyPort;

	private String getSendgridApiKey() {
		return SecurityUtil.decrypt(encryptionKey, sendgridApiKey);
	}

	@Override
	public void sendEmail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments) throws EmailException {

		long beginTS = System.currentTimeMillis();
		
		logger.info("sendEmail(SenGrid) begin - from=" + fromAddr + ", to=" + toAddrList + "......");

		if (toAddrList == null || toAddrList.isEmpty()) {
			throw new EmailException("701", "toAddrList is null, no email will be sent.......!");
		}
		
		if (StringUtil.isEmptyOrNull(subject)) {
			subject = "No Subject";
		}

		String sendgridApiKey = getSendgridApiKey();
		if (StringUtil.isEmptyOrNull(sendgridApiKey)) {
			throw new EmailException("702", "sendgridApiKey is empty or null......!");
		}

		Mail mail = createMail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList,
				bccAddrList, attachments);

		
		String subjectLog = (subject.length() <= 20 ? subject
				: subject.substring(0, 20 - 1));
		
		SendGrid sg = new SendGrid(sendgridApiKey);
		
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);

			long endTS = System.currentTimeMillis();
			long time = (endTS - beginTS) / 1000;
			if (response.getStatusCode() >= 200 && response.getStatusCode() <= 299) {
				logger.info("sendEmail(SenGrid) - successfully! SenGrid Response Code = " + response.getStatusCode()
						+ ", time = " + time + " seconds, subject=" + subjectLog + ", to=" + toAddrList);
			} else if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
				logger.error("sendEmail(SenGrid) - failed! SenGrid Response Code = " + response.getStatusCode()
						+ ", time = " + time + " seconds, subject=" + subjectLog + ", to=" + toAddrList + ", error: "
						+ response.getBody());
			} else if (response.getStatusCode() >= 400 && response.getStatusCode() <= 499) {
				logger.error("sendEmail(SenGrid) - failed! SenGrid Response Code = " + response.getStatusCode()
						+ ", time = " + time + " seconds, subject=" + subjectLog + ", to=" + toAddrList + ", error: "
						+ response.getBody());
			} else {
				logger.error("sendEmail(SenGrid) - failed! SenGrid Response Code = " + response.getStatusCode()
						+ ", time = " + time + " seconds, subject=" + subjectLog + ", to=" + toAddrList + ", error: "
						+ response.getBody());
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("sendEmail(SenGrid) - Error Message:" + e.getMessage(), e);
		}

		
	}

	private Mail createMail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments) {
		
		Mail mail = new Mail();
		// from
		Email fromEmail = new Email();
		fromEmail.setName(fromAddr.getDisplayName());
		fromEmail.setEmail(fromAddr.getAddress());
		mail.setFrom(fromEmail);
		//replyTo
		if (replyToAddr == null) {
			replyToAddr = fromAddr;
		}
		Email replyToEmail = new Email();
		replyToEmail.setName(replyToAddr.getDisplayName());
		replyToEmail.setEmail(replyToAddr.getAddress());
		mail.setReplyTo(replyToEmail);

		// Personalization
		Personalization personalization = new Personalization();
		for (EmailAddress addr : toAddrList) {
			Email to = new Email();
			to.setName(addr.getDisplayName());
			to.setEmail(addr.getAddress());
			personalization.addTo(to);
		}
		if (ccAddrList != null) {
			for (EmailAddress addr : ccAddrList) {
				Email cc = new Email();
				cc.setName(addr.getDisplayName());
				cc.setEmail(addr.getAddress());
				personalization.addCc(cc);
			}
		}
		if (bccAddrList != null) {
			for (EmailAddress addr : bccAddrList) {
				Email bcc = new Email();
				bcc.setName(addr.getDisplayName());
				bcc.setEmail(addr.getAddress());
				personalization.addBcc(bcc);
			}
		}
		
		personalization.setSubject(subject);

		mail.addPersonalization(personalization);

		Content emailContent = new Content();
		if (StringUtil.isEmptyOrNull(contentType)) {
			contentType = "text/plain";
		}
		emailContent.setType(contentType);
		emailContent.setValue(content);
		
		mail.addContent(emailContent);

		// Attachments
		if (attachments != null && !attachments.isEmpty()) {
			for (EmailAttachment a : attachments) {
				Attachments aa = new Attachments();
				aa.setContent(a.getContent());
				aa.setType(a.getContentType());
				aa.setFilename(a.getFilename());
				aa.setDisposition(a.getDisposition());
				aa.setContentId(a.getContentId());
				mail.addAttachments(aa);
			}
		}
		return mail;
	}
}
