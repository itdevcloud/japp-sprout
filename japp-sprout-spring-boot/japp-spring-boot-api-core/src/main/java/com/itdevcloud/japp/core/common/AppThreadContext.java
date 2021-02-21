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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.service.customization.IaaUserI;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppThreadContext {

	private static final Logger logger = LogManager.getLogger(AppThreadContext.class);

	//private static ThreadLocal<String> userIdContext = new ThreadLocal<String>();
	//private static ThreadLocal<String> tokenSubjectContext = new ThreadLocal<String>();
	private static ThreadLocal<IaaUserI> iaaUserContext = new ThreadLocal<IaaUserI>();

	private static ThreadLocal<TransactionContext> txContext = new ThreadLocal<TransactionContext>();

	public static IaaUserI getIaaUser() {
		IaaUserI user = iaaUserContext.get();
		return user;
	}

	public static void setIaaUser(IaaUserI user) {
		if (user == null) {
			iaaUserContext.set(null);
		}
		iaaUserContext.set(user);
	}

//	public static String getTokenSubject() {
//		return new String(tokenSubjectContext.get());
//	}
//
//	public static void setTokenSubject(String id) {
//		tokenSubjectContext.set(new String(id));
//	}
//	
	public static TransactionContext getTransactionContext() {
		TransactionContext txCtx = txContext.get();
		if (txCtx == null) {
			txCtx = new TransactionContext();
			txContext.set(txCtx);
		}
		return txCtx;
	}

	public static void setTransactionContext(TransactionContext txCtx) {
		if (txCtx == null) {
			txContext.set(null);
		}
		txContext.set(txCtx);
	}

	public static void clean() {
		logger.debug("clean AppThreadContext..... => start");
		iaaUserContext.set(null);
//		tokenSubjectContext.set(null);
		txContext.set(null);
		logger.debug("clean AppThreadContext..... <= end");
	}
}
