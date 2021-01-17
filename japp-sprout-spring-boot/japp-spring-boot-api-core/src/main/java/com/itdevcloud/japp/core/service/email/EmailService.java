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

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.util.StringUtils;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
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
public class EmailService implements AppFactoryComponentI{
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

	@PostConstruct
	public void init() {
		//ConfigServiceHelperI configService = piscesjappFactory.getComponent(ConfigServiceHelperI.class);
		emailServiceProviderName = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_PROVIDER);
		busEmailToAddrs = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_BUS_TOADDRESSES);
		itEmailToAddrs = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_IT_TOADDRESSES);
		systemEmailFromAddr = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_SYSTEM_FROMADDRESS);
		systemEmailReplyToAddr = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_EMAIL_SYSTEM_REPLYTOADDRESS);
		isSendAsGroup = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_SENDASGROUP);
		appId = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_APP_APPLICATION_ID);
		sendAsyncEmail = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_DEFAULT_TO_ASYNC);
		waitAsyncEmailResponse = ConfigFactory.appConfigService.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_EMAIL_ASYNC_WAITFORRESPONSE);
	}

	/**
	 * Send an email to the predefined IT support team by using the predefined delivery type.
	 * @param subject subject
	 * @param content content
	 * @throws EmailException
	 */
	public void sendITNotification(String subject, String content) throws EmailException {
		sendITNotification(subject, content, sendAsyncEmail, waitAsyncEmailResponse);
	}
	
	/**
	 * Send an email to the predefined IT support team, and define a delivery type by parameters async and waitForResponse.
	 * @param subject subject 
	 * @param content content
	 * @param async if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendITNotification(String subject, String content, boolean async, boolean waitForResponse) throws EmailException {
		if (StringUtils.isEmpty(itEmailToAddrs)) {
			throw new EmailException(103, "IT email toAddress is null.....no email will be sent...!!!");
		}

		EmailAddress fromAddr = new EmailAddress(null, systemEmailFromAddr);
		List<EmailAddress> toAddrList = getEmailAddresses(itEmailToAddrs);
		sendEmail(subject, "text/plain", content, fromAddr, null, toAddrList, null, null, null, null, null, async, waitForResponse);
	}

	/**
	 * Send an email to the predefined business team by using the predefined delivery type.
	 * @param subject subject
	 * @param content content
	 * @throws EmailException
	 */
	public void sendBusNotification(String subject, String content) throws EmailException {
		sendBusNotification(subject, content, sendAsyncEmail, waitAsyncEmailResponse);
	}
	
	/**
	 * Send an email to the predefined business team, and define a delivery type by parameters async and waitForResponse.
	 * @param subject subject 
	 * @param content content
	 * @param async if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendBusNotification(String subject, String content, boolean async, boolean waitForResponse) throws EmailException {
		if (StringUtils.isEmpty(busEmailToAddrs)) {
			throw new EmailException(203, "Business email toaddresses is null........no email will be sent...!!!");
		}

		EmailAddress fromAddr = new EmailAddress(null, systemEmailFromAddr);
		List<EmailAddress> toAddrList = getEmailAddresses(busEmailToAddrs);
		sendEmail(subject, "text/plain", content, fromAddr, null, toAddrList, null, null, null, null, null, async, waitForResponse);

	}

	/**
	 * Send an email by using the predefined delivery type.
	 * @param subject subject 
	 * @param content content
	 * @param toAddresses to addresses
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String content, String toAddresses) throws EmailException {
		sendEmail(subject, content, toAddresses, sendAsyncEmail, waitAsyncEmailResponse);
	}

	/**
	 * Send an email and define a delivery type by parameters async and waitForResponse.
	 * @param subject subject 
	 * @param content content
	 * @param toAddresses to addresses
	 * @param async if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String content, String toAddresses, boolean async, boolean waitForResponse) throws EmailException {
		if (StringUtils.isEmpty(toAddresses)) {
			throw new EmailException(301, "sendEmail()...toAddresses is empty or null.....no email will be sent...!!!");
		}
		EmailAddress fromAddr = new EmailAddress(null, systemEmailFromAddr);
		List<EmailAddress> toAddrList = getEmailAddresses(toAddresses);
		sendEmail(subject, "text/plain", content, fromAddr, null, toAddrList, null, null, null, null, null,async,  waitForResponse);

	}

	/**
	 * Send an email with attachments by using predefined delivery type.
	 * @param subject subject 
	 * @param contentType content type
	 * @param content content
	 * @param fromAddress from address
	 * @param replyToAddress replyto address
	 * @param toAddresses to addresses
	 * @param ccAddresses cc addresses
	 * @param bccAddresses bcc addresses
	 * @param attachments attachment list
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String contentType, String content, String fromAddress, String replyToAddress, String toAddresses, String ccAddresses, String bccAddresses,
			List<EmailAttachment> attachments) throws EmailException {
		sendEmail(subject, contentType, content, fromAddress, replyToAddress, toAddresses, ccAddresses, bccAddresses,
				attachments, sendAsyncEmail, waitAsyncEmailResponse);
	}

	/**
	 * Send an email with attachments, and define a delivery type by parameters async and waitForResponse.
	 * @param subject subject 
	 * @param contentType content type
	 * @param content content
	 * @param fromAddress from address
	 * @param replyToAddress replyto address
	 * @param toAddresses to addresses
	 * @param ccAddresses cc addresses
	 * @param bccAddresses bcc addresses
	 * @param attachments attachment list
	 * @param async if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String contentType, String content, String fromAddress, String replyToAddress, String toAddresses, String ccAddresses, String bccAddresses,
			List<EmailAttachment> attachments, boolean async, boolean waitForResponse) throws EmailException {
		if (StringUtils.isEmpty(toAddresses)) {
			throw new EmailException(401, "sendEmail()...toAddresses is empty or null.....no email will be sent...!!!");
		}

		List<EmailAddress> toAddrList = getEmailAddresses(toAddresses);
		List<EmailAddress> ccAddrList = getEmailAddresses(ccAddresses);
		List<EmailAddress> bccAddrList = getEmailAddresses(bccAddresses);

		EmailAddress fromAddr = new EmailAddress(null, fromAddress);
		EmailAddress replyToAddr = new EmailAddress(null, replyToAddress);

		sendEmail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList, bccAddrList, attachments, null, null, async, waitForResponse);

	}

	private List<EmailAddress> getEmailAddresses(String addresses) {
		List<EmailAddress> list = new ArrayList<EmailAddress>();
		if(StringUtil.isEmptyOrNull(addresses)){
			return null;
		}
		String[] addrArr = addresses.split(";");
		for (String addr : addrArr) {
			String str = addr.trim();
			if (!StringUtil.isEmptyOrNull(str)) {
				list.add(new EmailAddress(null, str));
			}
		}
		if(list.isEmpty()) {
			return null;
		}
		return list;
	}

	/**
	 * Check the delivery type (Synchronous or Asynchronous) first, then choose a proper email provider to send an email.
	 * @param subject subject 
	 * @param contentType content type
	 * @param content content
	 * @param fromAddr from address
	 * @param replyToAddr replyto address
	 * @param toAddrList to address list
	 * @param ccAddrList cc address list
	 * @param bccAddrList bcc address list
	 * @param attachments attachment list
	 * @param template template
	 * @param templateArgs template arguments
	 * @param async if it is an asynchronous execution
	 * @param waitForResponse if wait for a response
	 * @throws EmailException
	 */
	public void sendEmail(String subject, String contentType, String content, EmailAddress fromAddr, EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments, String template, Map<String, Object> templateArgs, boolean async, boolean waitForResponse) throws EmailException {
		logger.info("EmailService.sendEmail(), started.......");

		if(async) {
			Future<String> future = AppComponents.asyncEmailService.sendEmail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList,
					bccAddrList, attachments, template,  templateArgs);
			if(waitForResponse) {
				logger.debug("Send email using async method - wait for response......");
				try {
					String response = future.get();
					if ( !response.equalsIgnoreCase("Success")) {
						throw new EmailException(800, response);
					}
					logger.debug("Send email using async method - response = " + response);
				} catch (Throwable t) {
					logger.error(AppUtil.getStackTrace(t));
					throw new EmailException(900, "Getting Email Response Failed: \n" + t.getMessage());
				}
				logger.debug("Send email using async method - end......");
			}else {
				//please note:
				//when application main thread exit, the executer thread will stop as well
				logger.debug("Send email using async method - start...fire and forget...");
			}

		}else {

			send(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList,
					bccAddrList, attachments, template,  templateArgs);
		}
		
	}

	/**
	 * Choose a proper email provider to send an email.
	 * @param subject subject 
	 * @param contentType content type
	 * @param content content
	 * @param fromAddr from address
	 * @param replyToAddr replyto address
	 * @param toAddrList to address list
	 * @param ccAddrList cc address list
	 * @param bccAddrList bcc address list
	 * @param attachments attachment list
	 * @param template template
	 * @param templateArgs template arguments
	 * @throws EmailException
	 */
	public void send(String subject, String contentType, String content, EmailAddress fromAddr, EmailAddress replyToAddr, List<EmailAddress> toAddrList, List<EmailAddress> ccAddrList,
			List<EmailAddress> bccAddrList, List<EmailAttachment> attachments, String template, Map<String, Object> templateArgs) throws EmailException {

		logger.debug("send() - check and set default values.......");

		// Set defaults
		if (StringUtils.isEmpty(subject)) {
			subject = StringUtil.changeFirstCharCase(appId, true) + " EMAIL Service";
			logger.debug("subject is null, add default subject = "+subject);
		}

		if (StringUtils.isEmpty(fromAddr) || StringUtils.isEmpty(fromAddr.getAddress())) {
			fromAddr = new EmailAddress(null, systemEmailFromAddr);
			logger.debug("fromAddr is null, use default fromAddr = " + systemEmailFromAddr);
		}
		if (StringUtils.isEmpty(replyToAddr) || StringUtils.isEmpty(replyToAddr.getAddress())) {
			replyToAddr = new EmailAddress(null, systemEmailReplyToAddr);
			logger.debug("replyToAddr is null, use default replyToAddr = " + systemEmailReplyToAddr);
		}

		// Validation
		if (replyToAddr != null && !isValidAddress(replyToAddr)) {
			throw new EmailException(504, "replyToAddr is not valid!");
		}
		if (StringUtils.isEmpty(contentType) || StringUtils.isEmpty(content)) {
			throw new EmailException(505, "contentType and/or content are null or empty.......!");
		}
		if (!isValidAttachments(attachments)) {
			throw new EmailException(506, "attachment is not valid: " + attachments);
		}
		if (!isValidAddress(fromAddr)) {
			throw new EmailException(507, "fromAddr is not valid: " + fromAddr);
		}
		if (!isValidAddressList(toAddrList)) {
			throw new EmailException(508, "toAddrList is not valid!  toAddrList = " + toAddrList);
		}
		if (ccAddrList != null && !isValidAddressList(ccAddrList)) {
			throw new EmailException(509, "ccAddrList is not valid!  ccAddrList = " + ccAddrList);
		}
		if (bccAddrList != null && !isValidAddressList(bccAddrList)) {
			throw new EmailException(510, "bccAddrList is not valid!  bccAddrList = " + bccAddrList);
		}

		// Template
		if (!StringUtils.isEmpty(template)) {
			freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
			try {
				logger.debug("send() - create content based on template.......");
				content = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfig.getTemplate(template), templateArgs);
			} catch (TemplateNotFoundException e) {
				throw new EmailException(511, "Template not found: " + template + ", error=" + e.getMessage());
			} catch (MalformedTemplateNameException e) {
				throw new EmailException(512, "Malformed template name: " + template + ", error=" + e.getMessage());
			} catch (ParseException e) {
				throw new EmailException(513, "Template parsing error: " + template + ", error=" + e.getMessage());
			} catch (IOException e) {
				throw new EmailException(514, "Template IO error: " + template + ", error=" + e.getMessage());
			} catch (TemplateException e) {
				throw new EmailException(515, "Template error: " + template + ", error=" + e.getMessage());
			}
		}

		// Send
		if (isSendAsGroup) {
			logger.debug("send() - sending as a group.......");
			getEmailServiceProvider().sendEmail(subject, contentType, content, fromAddr, replyToAddr, toAddrList, ccAddrList, bccAddrList, attachments);
			//sendAsync(subject, contentType, content, fromAddr, replyToAddr, ccAddrList, bccAddrList, attachments, toAddrList);
		} else {
			// send email one by one in case invalid email address was provided.
			logger.debug("send() - sending one by one.......");
			for (EmailAddress ad : toAddrList) {
				List<EmailAddress> toOne = new ArrayList<EmailAddress>();
				toOne.add(ad);
				getEmailServiceProvider().sendEmail(subject, contentType, content, fromAddr, replyToAddr, toOne, ccAddrList, bccAddrList, attachments);
				//sendAsync(subject, contentType, content, fromAddr, replyToAddr, ccAddrList, bccAddrList, attachments, toOne);
			}
		}
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
		if (addr == null || StringUtil.isEmptyOrNull(addr.getAddress())) {
			logger.debug("email address is not valid......addr = " + addr);
			return false;
		}
		EmailValidator validator = EmailValidator.getInstance();
		return validator.isValid(addr.getAddress());
	}

	private boolean isValidAttachments(List<EmailAttachment> attachments) {
		if (attachments != null && !attachments.isEmpty()) {
			for (EmailAttachment a : attachments) {
				if (a != null) {
					if (StringUtils.isEmpty(a.getContent()) || StringUtils.isEmpty(a.getType()) || StringUtils.isEmpty(a.getFilename()) ) {
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
			throw new EmailException(1, "Property 'startkit.email.provider' not set");
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
			throw new EmailException(1, "Wrong property value for 'startkit.email.provider': " + emailServiceProviderName);
		}

	}
}
