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

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
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
	private static ThreadLocal<AppIaaUser> userContext = new ThreadLocal<AppIaaUser>();

	private static ThreadLocal<TransactionContext> txContext = new ThreadLocal<TransactionContext>();

	public static AppIaaUser getAppIaaUser() {
		AppIaaUser user = userContext.get();
		if (user != null) {
			user = AppUtil.GsonDeepCopy(user);
		}
		return user;
	}

	public static void setAppIaaUser(AppIaaUser user) {
		if (user == null) {
			userContext.set(null);
		}
		AppIaaUser u = AppUtil.GsonDeepCopy(user);
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
			AppIaaUser user = userContext.get();
			if (user != null) {
				id = user.getUID();
			}
		}
		if(StringUtil.isEmptyOrNull(id)) {
			id = "uid-unknown";
		}
		return new String(id);
	}

	public static void setUserId(String id) {
		userIdContext.set(new String(id));
	}

	public static TransactionContext getTransactionContext() {
		TransactionContext txCtx = txContext.get();
		TransactionContext targetCtx = null;
		
		//logger.debug("getTransactionContext() -----1----txCtx = " + txCtx);
		
		if (txCtx != null) {
			targetCtx = AppUtil.GsonDeepCopy(txCtx);
			//logger.debug("getTransactionContext() -----2----targetCtx = " + targetCtx);
		} else {
			targetCtx = new TransactionContext();
		}
		return targetCtx;
	}

	public static void setTransactionContext(TransactionContext txCtx) {
		if (txCtx == null) {
			txContext.set(null);
		}
		
		//logger.debug("setTransactionContext() -----1----txCtx = " + txCtx);
		
		TransactionContext targetObj = AppUtil.GsonDeepCopy(txCtx);
		
		//logger.debug("setTransactionContext() -----2----targetObj = " + targetObj);
		
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
