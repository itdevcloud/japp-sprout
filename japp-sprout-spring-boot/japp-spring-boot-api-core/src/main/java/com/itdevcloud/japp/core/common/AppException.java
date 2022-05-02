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
package com.itdevcloud.japp.core.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppException extends RuntimeException {
	private static final Logger logger = LogManager.getLogger(AppException.class);
	private static final long serialVersionUID = 1L;

	private Status status;

	private Throwable nestedException;

	private String stackTraceString;

	public AppException(Status status) {
		this(status, null, null);
	}
	public AppException(Status status,  String customizedMessage) {
		this(status, customizedMessage, null);
	}

	public AppException(Status status, String customizedMessage, Throwable nestedException) {
		super(customizedMessage=(StringUtil.isEmptyOrNull(customizedMessage)?"n/a":customizedMessage.trim()));
		this.status = (status==null?Status.NA:status);
		this.nestedException = nestedException;
		if (nestedException == null) {
			stackTraceString = null;
		} else {
			stackTraceString = generateStackTraceString(nestedException);
		}
	}

	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getErrorCode() {
		return status.code;
	}

	public String getStackTraceString() {
		// if there's no nested exception, there's no stackTrace
		if (nestedException == null)
			return "No stack trace available ( nestedException is null)";
		StringBuffer traceBuffer = new StringBuffer();
		if (nestedException instanceof AppException) {
			traceBuffer.append(((AppException) nestedException).getStackTraceString());
		}
		traceBuffer.append(stackTraceString);
		return traceBuffer.toString();
	}

	public String getMessage() {
		// superMsg will contain whatever String was passed into the
		// constructor, and null otherwise.
		StringBuffer buf = new StringBuffer();
		String superMsg = super.getMessage();
		buf.append("Status Code: ").append(status.code);
		buf.append(", Message: ").append(status.message);
		if (superMsg != null) {
			buf.append(" - " + superMsg);
		}
		// if there's no nested exception, do like we would always do
		if (nestedException != null) {
			buf.append(" - " + nestedException.getMessage());
		}
		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append(getMessage());
		if (nestedException != null) {
			buf.append("\n++++++++++++++++++++++++");
			buf.append("\nNESTED EXCEPTION: ").append(getStackTraceString());
			buf.append("\n------------------------");
		}
		return buf.toString();
	}

	// convert a stack trace to a String so it can be serialized
	public static String generateStackTraceString(Throwable t) {
		StringWriter s = new StringWriter();
		t.printStackTrace(new PrintWriter(s));
		return s.toString();
	}

}
