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

import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class AppThreadContext {

	//private static final Logger logger = LogManager.getLogger(AppThreadContext.class);
	private static final Logger logger = LogManager.getLogger(AppThreadContext.class);

	private static ThreadLocal<String> userIdContext = new ThreadLocal<String>();
	private static ThreadLocal<String> tokenSubjectContext = new ThreadLocal<String>();
	private static ThreadLocal<IaaUser> userContext = new ThreadLocal<IaaUser>();

	private static ThreadLocal<TransactionContext> txContext = new ThreadLocal<TransactionContext>();

	public static IaaUser<?> getIaaUser() {
		IaaUser<?> user = userContext.get();
		if (user != null) {
			user = AppUtil.GsonDeepCopy(user, null);
		}
		return user;
	}

	public static void setIaaUser(IaaUser<?> user) {
		if (user == null) {
			userContext.set(null);
		}
		IaaUser<?> u = AppUtil.GsonDeepCopy(user, null);
		userContext.set(u);
	}

	public static String getTokenSubject() {
		return new String(tokenSubjectContext.get());
	}

	public static void setTokenSubject(String id) {
		tokenSubjectContext.set(new String(id));
	}
	
	public static String getUserId() {
		String id = userIdContext.get();
		if(StringUtil.isEmptyOrNull(id)) {
			IaaUser user = userContext.get();
			if (user != null) {
				id = user.getUserId();
			}
		}
		if(StringUtil.isEmptyOrNull(id)) {
			id = "unknown";
		}
		return new String(id);
	}

	public static void setUserId(String id) {
		userIdContext.set(new String(id));
	}

	public static TransactionContext getTransactionContext() {
		TransactionContext txCtx = txContext.get();
		if (txCtx != null) {
			txCtx = AppUtil.GsonDeepCopy(txCtx, null);
		} else {
			txCtx = new TransactionContext();
		}
		return txCtx;
	}

	public static void setTransactionContext(TransactionContext txCtx) {
		if (txCtx == null) {
			txContext.set(null);
		}
		TransactionContext targetObj = AppUtil.GsonDeepCopy(txCtx, null);
		txContext.set(targetObj);
	}

	public static void clean() {
		logger.debug("clean AppThreadContext..... => start");
		userContext.set(null);
		userIdContext.set(null);
		tokenSubjectContext.set(null);
		txContext.set(null);
		logger.debug("clean AppThreadContext..... <= end");
	}
}
