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
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.tools.common.util.SecurityUtil;
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

		logger.debug("SendGridEmailService.createAndSendEmail() start.......");
		if (toAddrList == null || toAddrList.isEmpty()) {
			throw new EmailException(701, "toAddrList is null, no email will be sent.......!");
		}

		String sendgridApiKey = getSendgridApiKey();
		if (StringUtils.isEmpty(sendgridApiKey)) {
			throw new EmailException(702, "sendgridApiKey is empty or null......!");
		}

		Mail mail = createMail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList,
				bccAddrList, attachments);

		// send email
		SendGrid sg;
		if (StringUtils.isEmpty(httpProxyServer) || httpProxyPort == 0) {
			sg = new SendGrid(sendgridApiKey);
		} else {
			CloseableHttpClient httpclient = HttpClients.custom().useSystemProperties()
					.setProxy(new HttpHost(httpProxyServer, httpProxyPort)).build();

			Client client = new Client(httpclient);
			sg = new SendGrid(sendgridApiKey, client);
		}
		Request request = new Request();
		try {
			logger.debug("sending email start...........");
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			logger.debug(
					"sending email end. Response.code=" + response.getStatusCode() + ", message=" + response.getBody());
		} catch (Exception e) {
			throw new EmailException(704, AppUtil.getStackTrace(e));
		}
	}

	private Mail createMail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments) {
		Mail mail = new Mail();
		// from
		Email fromEmail = new Email();
		if (!StringUtils.isEmpty(fromAddr.getName())) {
			fromEmail.setName(fromAddr.getName());
		}
		fromEmail.setEmail(fromAddr.getAddress());
		mail.setFrom(fromEmail);
		// subject
		mail.setSubject(subject);
		// Personalization
		Personalization personalization = new Personalization();
		for (EmailAddress addr : toAddrList) {
			Email to = new Email();
			if (!StringUtils.isEmpty(addr.getName())) {
				to.setName(addr.getName());
			}
			to.setEmail(addr.getAddress());
			personalization.addTo(to);
		}
		if (ccAddrList != null) {
			for (EmailAddress addr : ccAddrList) {
				Email cc = new Email();
				if (!StringUtils.isEmpty(addr.getName())) {
					cc.setName(addr.getName());
				}
				cc.setEmail(addr.getAddress());
				personalization.addCc(cc);
			}
		}
		if (bccAddrList != null) {
			for (EmailAddress addr : bccAddrList) {
				Email bcc = new Email();
				if (!StringUtils.isEmpty(addr.getName())) {
					bcc.setName(addr.getName());
				}
				bcc.setEmail(addr.getAddress());
				personalization.addBcc(bcc);
			}
		}
		mail.addPersonalization(personalization);

		Content emailContent = new Content();
		emailContent.setType(contentType);
		emailContent.setValue(content);
		mail.addContent(emailContent);

		// Attachments
		if (attachments != null && !attachments.isEmpty()) {
			for (EmailAttachment a : attachments) {
				Attachments aa = new Attachments();
				aa.setContent(a.getContent());
				aa.setType(a.getType());
				aa.setFilename(a.getFilename());
				aa.setDisposition(a.getDisposition());
				aa.setContentId(a.getContentId());
				mail.addAttachments(aa);
			}
		}

		if (replyToAddr != null) {

			Email replyTo = new Email();
			if (StringUtils.isEmpty(replyToAddr.getName())) {
				replyTo.setName(replyToAddr.getName());
			}
			replyTo.setEmail(replyToAddr.getAddress());
			mail.setReplyTo(replyTo);
		}
		return mail;

	}
}
