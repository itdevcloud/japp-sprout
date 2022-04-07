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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class EmailService implements AppFactoryComponentI {
	private Logger logger = LogManager.getLogger(EmailService.class);
	private static final String EMAIL_PROVIDER_SPRING = "spring";
	private static final String EMAIL_PROVIDER_SENDGRID = "sendgrid";
	private static final String EMAIL_PROVIDER_LOG = "log";

	@Autowired
	private Configuration freemarkerConfig;

	@Autowired
	private SpringEmailProvider springEmailProvider;

	@Autowired
	private SendGridEmailProvider sendGridEmailProvider;

	@Autowired
	private LogEmailProvider logEmailProvider;

	private String appId = null;
	private String emailServiceProviderName = null;
	private String busEmailToAddrs = null;
	private String itEmailToAddrs = null;
	private String systemEmailFromAddr = null;
	private String systemEmailReplyToAddr = null;
	private boolean isSendAsGroup = true;
	private boolean sendAsyncEmail = true;
	private boolean waitAsyncEmailResponse = false;
	private long maxFileSizeBtyes = -1;

	@PostConstruct
	public void init() {
		emailServiceProviderName = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_PROVIDER);
		busEmailToAddrs = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_BUS_TOADDRESSES);
		itEmailToAddrs = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_IT_TOADDRESSES);
		systemEmailFromAddr = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_SYSTEM_FROMADDRESS);
		systemEmailReplyToAddr = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_SYSTEM_REPLYTOADDRESS);
		isSendAsGroup = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_SENDASGROUP);
		appId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		sendAsyncEmail = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_DEFAULT_TO_ASYNC);
		waitAsyncEmailResponse = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_ASYNC_WAITFORRESPONSE);
		maxFileSizeBtyes = ConfigFactory.appConfigService
				.getPropertyAsLong(AppConfigKeys.JAPPCORE_EMAIL_MAX_ATTACHMENT_FILE_SIZE_BYTES);
	}

	public String getPredefinedBusinessToAddresses() {
		return this.busEmailToAddrs;
	}

	public String getPredefinedItToAddresses() {
		return this.itEmailToAddrs;
	}

	public void sendEmail(String subject, String content, String toAddresses) throws EmailException {
		sendEmail(subject, content, toAddresses, sendAsyncEmail, waitAsyncEmailResponse);
	}

	public void sendEmail(String subject, String content, String toAddresses, boolean async, boolean waitForResponse)
			throws EmailException {
		if (StringUtil.isEmptyOrNull(toAddresses)) {
			throw new EmailException("301", "sendEmail()...toAddresses is empty or null.....no email will be sent...!!!");
		}
		EmailAddress fromAddr = new EmailAddress(systemEmailFromAddr);
		List<EmailAddress> toAddrList = getEmailAddresses(toAddresses);
		sendEmail(subject, "text/plain", content, fromAddr, null, toAddrList, null, null, null, null, null, async,
				waitForResponse);

	}

	public void sendEmail(String subject, String contentType, String content, String fromAddress, String replyToAddress,
			String toAddresses, String ccAddresses, String bccAddresses, List<EmailAttachment> attachments)
			throws EmailException {
		sendEmail(subject, contentType, content, fromAddress, replyToAddress, toAddresses, ccAddresses, bccAddresses,
				attachments, sendAsyncEmail, waitAsyncEmailResponse);
	}

	/**
	 * Send an email with attachments, and define a delivery type by parameters
	 * async and waitForResponse.
	 * 
	 * @param subject         subject
	 * @param contentType     content type
	 * @param content         content
	 * @param fromAddress     from address
	 * @param replyToAddress  replyto address
	 * @param toAddresses     to addresses
	 * @param ccAddresses     cc addresses
	 * @param bccAddresses    bcc addresses
	 * @param attachments     attachment list
	 * @param async           if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String contentType, String content, String fromAddress, String replyToAddress,
			String toAddresses, String ccAddresses, String bccAddresses, List<EmailAttachment> attachments,
			boolean async, boolean waitForResponse) throws EmailException {
		if (StringUtil.isEmptyOrNull(toAddresses)) {
			throw new EmailException("401", "sendEmail()...toAddresses is empty or null.....no email will be sent...!!!");
		}

		List<EmailAddress> toAddrList = getEmailAddresses(toAddresses);
		List<EmailAddress> ccAddrList = getEmailAddresses(ccAddresses);
		List<EmailAddress> bccAddrList = getEmailAddresses(bccAddresses);

		EmailAddress fromAddr = new EmailAddress(fromAddress);
		EmailAddress replyToAddr = new EmailAddress(replyToAddress);

		sendEmail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList, bccAddrList,
				attachments, null, null, async, waitForResponse);

	}

	private List<EmailAddress> getEmailAddresses(String addresses) {
		List<EmailAddress> list = new ArrayList<EmailAddress>();
		if (StringUtil.isEmptyOrNull(addresses)) {
			return null;
		}
		String[] addrArr = addresses.split(";");
		for (String addr : addrArr) {
			String str = addr.trim();
			if (!StringUtil.isEmptyOrNull(str)) {
				list.add(new EmailAddress(str));
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return list;
	}

	/**
	 * Check the delivery type (Synchronous or Asynchronous) first, then choose a
	 * proper email provider to send an email.
	 * 
	 * @param subject         subject
	 * @param contentType     content type
	 * @param content         content
	 * @param fromAddr        from address
	 * @param replyToAddr     replyto address
	 * @param toAddrList      to address list
	 * @param ccAddrList      cc address list
	 * @param bccAddrList     bcc address list
	 * @param attachments     attachment list
	 * @param template        template
	 * @param templateArgs    template arguments
	 * @param async           if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments, String template,
			Map<String, Object> templateArgs, boolean async, boolean waitForResponse) throws EmailException {
		logger.info("EmailService.sendEmail(), started.......");

		if (async) {
			Future<String> future = AppComponents.asyncEmailService.sendEmail(subject, contentType, content, fromAddr,
					replyToAddr, toAddrList, ccAddrList, bccAddrList, attachments, template, templateArgs);
			if (waitForResponse) {
				logger.debug("Send email using async method - wait for response......");
				try {
					String response = future.get();
					if (!response.equalsIgnoreCase("Success")) {
						throw new EmailException("800", response);
					}
					logger.debug("Send email using async method - response = " + response);
				} catch (Throwable t) {
					logger.error(CommonUtil.getStackTrace(t));
					throw new EmailException("900", "Getting Email Response Failed: \n" + t.getMessage());
				}
				logger.debug("Send email using async method - end......");
			} else {
				// please note:
				// when application main thread exit, the executer thread will stop as well
				logger.debug("Send email using async method - start...fire and forget...");
			}

		} else {

			send(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList, bccAddrList, attachments,
					template, templateArgs);
		}

	}

	public void send(String subject, String contentType, String content, EmailAddress fromAddr,
			EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments, String template,
			Map<String, Object> templateArgs) throws EmailException {

		logger.debug("send() - check and set default values.......");

		// Set defaults
		if (StringUtil.isEmptyOrNull(subject)) {
			subject = StringUtil.changeFirstCharCase(appId, true) + " EMAIL Service";
			logger.debug("subject is null, add default subject = " + subject);
		}

		if (fromAddr == null) {
			fromAddr = new EmailAddress(systemEmailFromAddr);
			logger.debug("fromAddr is null, use default fromAddr = " + systemEmailFromAddr);
		}
		if (replyToAddr == null) {
			replyToAddr = new EmailAddress(systemEmailReplyToAddr);
			logger.debug("replyToAddr is null, use default replyToAddr = " + systemEmailReplyToAddr);
		}

		// Validation
		if (StringUtil.isEmptyOrNull(contentType) || StringUtil.isEmptyOrNull(content)) {
			throw new EmailException("505", "contentType and/or content are null or empty.......!");
		}
		if (toAddrList == null || toAddrList.isEmpty()) {
			throw new EmailException("505", "toAddress List is null or empty.......!");
		}
		if (!isValidAttachments(attachments)) {
			throw new EmailException("506", "attachment is not valid: " + attachments);
		}

		// Template
		if (!StringUtil.isEmptyOrNull(template)) {
			freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
			try {
				logger.debug("send() - create content based on template.......");
				content = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfig.getTemplate(template),
						templateArgs);
			} catch (TemplateNotFoundException e) {
				throw new EmailException("511", "Template not found: " + template + ", error=" + e.getMessage());
			} catch (MalformedTemplateNameException e) {
				throw new EmailException("512", "Malformed template name: " + template + ", error=" + e.getMessage());
			} catch (ParseException e) {
				throw new EmailException("513", "Template parsing error: " + template + ", error=" + e.getMessage());
			} catch (IOException e) {
				throw new EmailException("514", "Template IO error: " + template + ", error=" + e.getMessage());
			} catch (TemplateException e) {
				throw new EmailException("515", "Template error: " + template + ", error=" + e.getMessage());
			}
		}

		// Send
		if (isSendAsGroup) {
			logger.debug("send() - sending as a group.......");
			getEmailServiceProvider().sendEmail(subject, contentType, content, fromAddr, replyToAddr, toAddrList,
					ccAddrList, bccAddrList, attachments);
			// sendAsync(subject, contentType, content, fromAddr, replyToAddr, ccAddrList,
			// bccAddrList, attachments, toAddrList);
		} else {
			// send email one by one in case invalid email address was provided.
			logger.debug("send() - sending one by one.......");
			for (EmailAddress ad : toAddrList) {
				List<EmailAddress> toOne = new ArrayList<EmailAddress>();
				toOne.add(ad);
				getEmailServiceProvider().sendEmail(subject, contentType, content, fromAddr, replyToAddr, toOne,
						ccAddrList, bccAddrList, attachments);
				// sendAsync(subject, contentType, content, fromAddr, replyToAddr, ccAddrList,
				// bccAddrList, attachments, toOne);
			}
		}
	}

	private boolean isValidAttachments(List<EmailAttachment> attachments) {
		if (attachments != null && !attachments.isEmpty()) {
			for (EmailAttachment a : attachments) {
				if (a != null) {
					if (StringUtil.isEmptyOrNull(a.getContent()) || StringUtil.isEmptyOrNull(a.getContentType())
							|| StringUtil.isEmptyOrNull(a.getFilename())) {
						logger.debug("email attachment is not valid......attachment = " + a);
						return false;
					}
				}
			}
		}

		return true;
	}

	private EmailServiceProvider getEmailServiceProvider() throws EmailException {
		if (emailServiceProviderName == null) {
			throw new EmailException("1", "emailServiceProviderName is null, check configuration !");
		}
		switch (emailServiceProviderName) {
		case EMAIL_PROVIDER_SPRING:
			logger.debug("using SPRING email provider");
			return springEmailProvider;
		case EMAIL_PROVIDER_SENDGRID:
			logger.debug("using SendGrid email provider");
			return sendGridEmailProvider;
		case EMAIL_PROVIDER_LOG:
			logger.debug("using Log email provider");
			return logEmailProvider;
		default:
			throw new EmailException("1", "emailServiceProviderName is not supported: " + emailServiceProviderName);
		}

	}

	private List<EmailAttachment> getEmailAttachments(String fileNames) throws EmailException{

		String fileContentType = null;
		String fileName = null;
		EmailAttachment attachment = null;
		if (!StringUtil.isEmptyOrNull(fileNames)) {
			return null;
		}
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		String[] files = fileNames.split(";");
		int contentId = 1;
		for (String str : files) {
			if (StringUtil.isEmptyOrNull(str)) {
				continue;
			}
			String[] splitStrArr = str.split("\\|");
			fileName = splitStrArr[0].trim();
			if (splitStrArr.length > 1) {
				fileContentType = splitStrArr[1].trim();
			} else {
				fileContentType = null;
			}
			if (StringUtil.isEmptyOrNull(fileName)) {
				continue;
			}
			File tmpFile = new File(fileName);
			if (!tmpFile.exists()) {
				throw new EmailException(ResponseStatus.STATUS_CODE_ERROR_VALIDATION, "Can't find this file:" + fileName);
			}
			long len = tmpFile.length();
			if (len >= maxFileSizeBtyes) {
				throw new EmailException(ResponseStatus.STATUS_CODE_ERROR_VALIDATION,
						"File exceeds maximum size(" + maxFileSizeBtyes + ") :" + fileName + "( size = " + len + " )");
			}
			if (StringUtil.isEmptyOrNull(fileContentType)) {
				try {
					fileContentType = Files.probeContentType(tmpFile.toPath());
					logger.info("contentType is null or empty, detect contentType automatically = " + fileContentType);
				} catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}

			String content;
			try {
				content = Base64.getEncoder().encodeToString(Files.readAllBytes(tmpFile.toPath()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (StringUtil.isEmptyOrNull(str)) {
				logger.error("File content is null, do nothing. File Name = " + fileName + "...............!");
			}
			attachment = new EmailAttachment();
			attachment.setContentType(fileContentType);
			attachment.setContent(content);
			attachment.setContentId("File-" + contentId++);
			attachment.setFilename(tmpFile.getName());
			attachment.setDisposition("attachment");
			
			attachments.add(attachment);
		}
		logger.info("emailFileList = " + attachments);
		return attachments;
	}

}
