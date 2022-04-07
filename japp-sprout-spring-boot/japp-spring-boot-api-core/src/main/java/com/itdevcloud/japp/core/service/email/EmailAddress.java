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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;

import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class EmailAddress {
	private String displayName;
	private String address;

	/*
	 * format: DisplayName<email address>
	 */
	public EmailAddress(String address) {
		super();
		if(StringUtil.isEmptyOrNull(address) ) {
			throw new RuntimeException("email address is empty or null, check code!");
		}
	    String pattern = "(.*)<(.*)>";
	    Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(address);
        if (m.find( )) {
        	this.displayName = m.group(1);
        	this.address = m.group(2);
         } else {
         	this.displayName = null;
         	this.address = address;
         }
        EmailValidator validator = EmailValidator.getInstance();
		if(this.address == null || !validator.isValid(this.address)) {
			throw new RuntimeException("email address '" + this.address + "' is invalid, check code! original address string = " + address);
		}
     	this.displayName = (this.displayName==null?null:this.displayName.trim());
     	this.address = (this.address==null?null:this.address.trim());
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getAddress() {
		return this.address;
	}

	@Override
	public String toString() {
		return "EmailAddress [name=" + displayName + ", address=" + address + "]";
	}
	
	public static void main(String[] args)  {
		String address = "sun@a.com";
		EmailAddress emailAddress = new EmailAddress(address);
		System.out.println("address = " + address + ", email address = " + emailAddress);
		
		address = "<sun@a.com>";
		emailAddress = new EmailAddress(address);
		System.out.println("address = " + address + ", email address = " + emailAddress);
		
		address = "M Sun. <sun@a.com>";
		emailAddress = new EmailAddress(address);
		System.out.println("address = " + address + ", email address = " + emailAddress);

		address = " M Sun. <sun@a.com> abc";
		emailAddress = new EmailAddress(address);
		System.out.println("address = " + address + ", email address = " + emailAddress);

	}

}
