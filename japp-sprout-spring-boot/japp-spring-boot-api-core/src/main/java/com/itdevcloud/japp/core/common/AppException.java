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

import com.itdevcloud.japp.core.cahce.RefreshableCache;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppException extends RuntimeException {
	//private static final Logger logger = LogManager.getLogger(AppException.class);
	private static final Logger logger = LogManager.getLogger(AppException.class);

	private static final long serialVersionUID = 1L;

	private String errorCode;

	private Throwable nestedException;

	private String stackTraceString;

	public AppException(String errorCode, String message) {
		this(errorCode, message, null);
	}

	public AppException(String errorCode, String message, Throwable nestedException) {
		super(message);
		this.errorCode = errorCode;
		this.nestedException = nestedException;
		if (nestedException == null) {
			stackTraceString = null;
			logger.error("Exception occurred, message: " + message);
		} else {
			stackTraceString = generateStackTraceString(nestedException);
			logger.error("Exception occurred, stack trace: " + stackTraceString);
		}
	}

	/**
	 * Get the error code
	 * @return error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Get the nested exception
	 * @return
	 */
	public Throwable getNestedException() {
		return nestedException;
	}

	/**
	 * Get the stack trace string.
	 * @return stack trace string
	 */
	public String getStackTraceString() {
		// if there's no nested exception, there's no stackTrace
		if (nestedException == null)
			return "No stack trace available ( nestedException is null)";
		StringBuffer traceBuffer = new StringBuffer();
		if (nestedException instanceof AppException) {
			traceBuffer.append(((AppException) nestedException).getStackTraceString());
			traceBuffer.append("-------- nested by:\n");
		}
		traceBuffer.append(stackTraceString);
		return traceBuffer.toString();
	}

	/**
	 * Get the message.
	 */
	public String getMessage() {
		// superMsg will contain whatever String was passed into the
		// constructor, and null otherwise.
		String superMsg = super.getMessage();
		// if there's no nested exception, do like we would always do
		if (getNestedException() == null)
			return superMsg;
		StringBuffer buf = new StringBuffer();
		// get the nested exception's message
		String nestedMsg = getNestedException().getMessage();
		if (superMsg != null)
			buf.append(superMsg).append("\nNESTED MSG: [").append(nestedMsg).append("'");
		else
			buf.append(nestedMsg);
		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append("\nERROR KEY: ").append(errorCode);
		buf.append("\nMESSAGE: ").append(getMessage());
		if (nestedException != null) {
			buf.append("\n++++++++++++++++++++++++");
			buf.append("\nNESTED EXCEPTION: ").append(getNestedException());
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
