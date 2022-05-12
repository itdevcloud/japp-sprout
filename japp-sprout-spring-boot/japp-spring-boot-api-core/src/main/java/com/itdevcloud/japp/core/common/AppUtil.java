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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ApiAuthInfo;
import com.itdevcloud.japp.core.api.vo.BasicCredential;
import com.itdevcloud.japp.core.api.vo.ClientAppInfo;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.api.vo.ResponseStatus.Status;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AppUtil {

	private static final Logger logger = LogManager.getLogger(AppUtil.class);

	public static final DateFormat defaulDateStringFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat defaulDateTimeStringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final String REQUEST_POSTFIX = "Request";
	public static final String RESPONSE_POSTFIX = "Response";
	public static final String PROCESSOR_POSTFIX = "Processor";

	private static String springActiveProfile = null;
	private static String deploymentRootDir = null;
	private static final String DEFAULT_BACKEND_ENV = "DEFAULT";
	private static Date startupDate;

	static {
		init();
	}

	public static void init() {
		logger.info("AppUtil.init() - start........");
		startupDate = new Date();
		logger.info("AppUtil.init() - end........");
	}

	public static Date getStartupDate() {
		return startupDate;
	}
	
	public static String getClassSimpleName(String classFullName) {
		if (StringUtil.isEmptyOrNull(classFullName)) {
			return null;
		}
		classFullName = classFullName.replaceAll("\\.class", "");
		int idx = classFullName.lastIndexOf(".");
		if(idx < 0) {
			return classFullName;
		}else {
			return classFullName.substring(idx+1);
		}
	}
	public static String getCallerMethodNameTrace() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = null;
		String nameTrace = null;
		if(stacktrace != null && stacktrace.length >=5) {
			e = stacktrace[4];
			nameTrace = getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			e = stacktrace[3];
			nameTrace = nameTrace + "::" + getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			e = stacktrace[2];
			nameTrace = nameTrace + "::" + getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			return nameTrace;
		}else if(stacktrace != null && stacktrace.length >=4) {
			e = stacktrace[3];
			nameTrace = getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			e = stacktrace[2];
			nameTrace = nameTrace + "::" + getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			return nameTrace;
		}else if(stacktrace != null && stacktrace.length >=3) {
			e = stacktrace[2];
			nameTrace = getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			return nameTrace;
		}else  {
			e = stacktrace[1];
			nameTrace = getClassSimpleName(e.getClassName()) + "." + e.getMethodName() + "()";
			return nameTrace;
		}
	}


	public static BaseResponse createBaseResponse(Status status, String customizedMessage) {
		
		if(status == null) {
			status = Status.NA;
		}

		BaseResponse response = new BaseResponse();
		ResponseStatus responseStatus = createResponseStatus(status, customizedMessage);

		response.setResponseStatus(responseStatus);

		TransactionContext tcContext = AppThreadContext.getTransactionContext();
		String txId = tcContext == null ? null : tcContext.getTransactionId();

		response.setServerTxId(txId);
		
		return response;
	}

	public static <T extends BaseResponse> T createResponse(Class<T> responseClass, String command, Status status, String customizedMessage) {

		if(status == null) {
			status = Status.NA;
		}
		T response = AppFactory.getInstance(responseClass);
		response.setCommand(command);

		ResponseStatus responseStatus = createResponseStatus(status, customizedMessage);

		response.setResponseStatus(responseStatus);

		TransactionContext tcContext = AppThreadContext.getTransactionContext();
		String txId = tcContext == null ? null : tcContext.getTransactionId();

		response.setServerTxId(txId);
		
		return response;
	}


	public static ResponseStatus createResponseStatus(Status status, String customizedMessage) {

		if(status == null) {
			status = Status.NA;
		}
		ResponseStatus responseStatus = new ResponseStatus(status, customizedMessage);
		return responseStatus;
	}

	public static <T> T GsonDeepCopy(Object sourceObj, Class<T> outputClass) {
		if (sourceObj == null || outputClass == null) {
			String errMsg = getCallerMethodNameTrace() + ".....sourceObj and/or outputClass can not be null or empty.";
			logger.warn(errMsg);
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		T targetObj;
		Gson gson = null;
		try {
			gson = new GsonBuilder().serializeNulls().create();
			String jsonStr = gson.toJson(sourceObj);
			targetObj = gson.fromJson(jsonStr, outputClass);
			return targetObj;
		} catch (Throwable t) {
			String errMsg = getCallerMethodNameTrace() + "....." + t.getMessage();
			logger.error(errMsg);
			logger.error(CommonUtil.getStackTrace(t));
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
	}

	public static <T> T GsonDeepCopy(T sourceObj) {
		if (sourceObj == null) {
			String errMsg = getCallerMethodNameTrace() + "......sourceObj can not be null.";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		return GsonDeepCopy(sourceObj, (Class<T>) sourceObj.getClass());
	}

	public static String getPropertyFileName(String baseName, String suffix){
		if (StringUtil.isEmptyOrNull(baseName)) {
			String errMsg = getCallerMethodNameTrace() + ".....baseName can not be null or empty.";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		String env = getSpringActiveProfile();
		String fileName = baseName + "-" + env + (StringUtil.isEmptyOrNull(suffix)?"":("." + suffix.trim()));
		logger.debug("getPropertyFileName().....getPropertyFileName = " + fileName);
		return fileName;
	}

	public static String getSpringActiveProfile() {
		if (StringUtil.isEmptyOrNull(springActiveProfile)) {
			String env = System.getenv("spring.profiles.active");
			if (StringUtil.isEmptyOrNull(env)) {
				logger.info("getSpringActiveProfile().....get spring.profiles.active from Java Option.... ");
				env = System.getProperty("spring.profiles.active");
			}
			if (StringUtil.isEmptyOrNull(env)) {
				env = DEFAULT_BACKEND_ENV;
			}
			springActiveProfile = env;
			logger.info("getSprintActiveProfile().....spring.profiles.active = " + springActiveProfile);
		}
		return springActiveProfile;
	}

	public static String getDeploymentRootDir() {
		if (StringUtil.isEmptyOrNull(deploymentRootDir)) {
			String env = System.getenv("jappcore.deployment.root.dir");
			if (StringUtil.isEmptyOrNull(env)) {
				logger.info("getDeploymentRootDir().....get jappcore.deployment.root.dir from Java Option.... ");
				env = System.getProperty("jappcore.deployment.root.dir");
			}
			deploymentRootDir = env;
			if (StringUtil.isEmptyOrNull(deploymentRootDir)) {
				return "./";
			}
			if (!StringUtil.isEmptyOrNull(deploymentRootDir) && !deploymentRootDir.endsWith(File.separator)) {
				deploymentRootDir = deploymentRootDir + File.separator;
			}
		}
		logger.info("getDeploymentRootDir().....jappcore.deployment.root.dir = " + deploymentRootDir);
		return deploymentRootDir;
	}

	public static String getCorrespondingCommand(String classSimpleName) {
		if(StringUtil.isEmptyOrNull(classSimpleName)) {
			String msg = getCallerMethodNameTrace() + ".....classSimpleName is null or empty.";
			logger.warn(msg);
			return null;
		}
		int idx = classSimpleName.indexOf(REQUEST_POSTFIX);
		if (idx <= 0) {
			idx = classSimpleName.indexOf(RESPONSE_POSTFIX);
			if (idx <= 0) {
				idx = classSimpleName.indexOf(PROCESSOR_POSTFIX);
			}
		}
		if (idx <= 0) {
			String msg = getCallerMethodNameTrace() + ".....can not get command name from classSimpleName(" + classSimpleName+").";
			logger.warn(msg);
			return null;
		}
		String command = classSimpleName.substring(0, idx);
		if (StringUtil.isEmptyOrNull(command)) {
			String msg = getCallerMethodNameTrace() + ".....can not get command name from classSimpleName(" + classSimpleName+").";
			logger.warn(msg);
			return null;
		}
		if (command.equalsIgnoreCase("Base")) {
			String msg = getCallerMethodNameTrace() + ".....can not get command name from classSimpleName(" + classSimpleName+").";
			logger.warn(msg);
			return null;
		}
		return command;
	}

	public static String getCorrespondingCommand(Object object) {
		String classSimpleName = object.getClass().getSimpleName();
		return getCorrespondingCommand(classSimpleName);

	}


	public static String[] parseHttpBasicAuthString(ServletRequest request) {
		if (request == null || !(request instanceof HttpServletRequest)) {
			logger.error(
					"parseHttpBasicAuthString() - request is null or not instanceof HttpServletRequest, return null... ");
			return null;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authorization = httpRequest.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Basic")) {
			// Authorization: Basic base64credentials
			String base64Credentials = authorization.substring("Basic".length()).trim();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));

			// credentials = username:password
			String[] values = credentials.split(":", 2);
			return values;
		} else {
			return null;
		}
	}

	public static String getClientHost(HttpServletRequest request) {

		String remoteHost = null;

		if (request != null) {
			remoteHost = request.getRemoteAddr();
		}

		return remoteHost;
	}

	public static String getClientIp(HttpServletRequest request) {

		String remoteAddr = null;

		if (request != null) {
			remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (StringUtil.isEmptyOrNull(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			} else {
				//logger.debug("-------------remoteAddr 1-----X-FORWARDED-FOR-----" + remoteAddr);
				int idx = remoteAddr.indexOf(",");
				if (idx > 0) {
					// client part
					remoteAddr = remoteAddr.substring(0, idx);
				}
				idx = remoteAddr.lastIndexOf(":");
				if (idx > 0) {
					remoteAddr = remoteAddr.substring(0, idx);
				}
				//logger.debug("-------------remoteAddr 2-------------------------" + remoteAddr);
			}
		}
		if (InetAddressValidator.getInstance().isValid(remoteAddr)) {
			return remoteAddr;
		} else {
			return "n/a";
		}

	}

	public static void setHttpResponse(HttpServletResponse httpResponse, int httpStatus, Status status, String customizedMessage) {
		if(httpResponse == null) {
			String errMsg = getCallerMethodNameTrace() + ".....httpResponse can not be null.";
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}

		PrintWriter out = null;
		try {
			httpResponse.setStatus(httpStatus);
			BaseResponse jappBaseResponse = AppUtil.createResponse(BaseResponse.class, null, status, customizedMessage);
			Gson gson = new GsonBuilder().serializeNulls().create();
			String jsonResponseStr = gson.toJson(jappBaseResponse);
			out = httpResponse.getWriter();
			httpResponse.setContentType("application/json");
			httpResponse.setCharacterEncoding("UTF-8");
			out.print(jsonResponseStr);
		} catch (AppException ae) {
			throw ae;
		} catch (Throwable t) {
			String errMsg = getCallerMethodNameTrace() + "....." + t.getMessage();
			logger.error(CommonUtil.getStackTrace(t));
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}finally {
			if(out != null) {
				out.close();
				out = null;
			}
		}
	}

	public static String getJwtTokenFromRequest(HttpServletRequest httpRequest) {
		if (httpRequest == null) {
			return null;
		}
		String authHeader = httpRequest.getHeader(AppConstant.HTTP_AUTHORIZATION_HEADER_NAME);
		String token = null;
		if ((authHeader != null) && (authHeader.startsWith("Bearer "))) {
			token = authHeader.substring(7);
			logger.debug("getJwtTokenFromRequest - found token in request header.........");
			return token;
		} else {
			logger.debug("getJwtTokenFromRequest - can not found token in request header.........");
			return null;
		}

	}

	public static String getHttpRequestJsonBody (HttpServletRequest request) {
		logger.debug("getHttpRequestJsonBody()...start........");
		if (request == null) {
			return null;
		}
		BufferedReader reader = null;
		StringBuffer sBuffer = new StringBuffer();
		try {
           reader = request.getReader();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sBuffer.append(line);
			}
			reader.close();
			reader = null;
		} catch (Exception e) {
			String errMsg = getCallerMethodNameTrace() + "....." + e.getMessage();
			logger.error("getHttpRequestJsonBody()...end....with Error, json read: " + sBuffer.toString());
			e.printStackTrace();
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		} finally{
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String jsonStr = sBuffer.toString();
		//logger.debug("getHttpRequestJsonBody()......end......JSON Read: " + jsonStr);
		return jsonStr;
	}

	public static String getValueFromJsonString (String jsonString, String name) {
		logger.debug("getValueFromJsonString()...start........");
		if (StringUtil.isEmptyOrNull(jsonString) || StringUtil.isEmptyOrNull(name) || !jsonString.startsWith("{")) {
			String errMsg = getCallerMethodNameTrace() + ".....jsonString is null / empty or it is not a valid json string or property name is null / empty." ;
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		String value = null;
		try {
			JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
			if(obj != null) {
				JsonElement element = obj.get(name.trim());
				value = (element == null? null: element.getAsString());
			}
			logger.debug("getValueFromJsonString()......end......name = " + name +", value = "+value);
		} catch (Exception e) {
			String errMsg = getCallerMethodNameTrace() + "....." + e.getMessage();
			logger.error(CommonUtil.getStackTrace(e));
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		} 
		return value;
	}


	public static void initTransactionContext() {
		
		clearTransactionContext();
		
		TransactionContext txnCtx = new TransactionContext();
		String txId = UUID.randomUUID().toString();
		// for log4j2
		ThreadContext.put(AppConstant.CONTEXT_KEY_JAPPCORE_TX_ID, txId);

		logger.debug("initTransactionContext()......begin....");
		
		txnCtx.setTransactionId(txId);
		txnCtx.setRequestReceivedTimeStamp(new Timestamp(new Date().getTime()));
		txnCtx.setServerTimezoneId(TimeZone.getDefault().getID());
		
		AppThreadContext.setTransactionContext(txnCtx);


		logger.debug("initTransactionContext().....end.....\ntxnCtx = " + txnCtx );
		
	}
	
	public static void initAuthContext(HttpServletRequest request) {
		
		logger.debug("initAuthContext().......begin....");
		String errMsg = null;
		if (request == null ) {
			errMsg = "initAuthContext() - request can not be null!" ;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_SYSTEM_ERROR, errMsg);
		}
		AppComponents.commonService.setValidatedAuthTokenClaimsAndApiAuthInfoContext(request);	
		
		logger.debug("initAuthContext()....end.....\nauthInfo = " + AppThreadContext.getApiAuthInfo());
		
	}

	public static void clearTransactionContext() {
		logger.debug("clearTransactionContext()...clear transaction context........");
		AppThreadContext.clean();
		ThreadContext.clearAll();
	}

	public static String getParaCookieHeaderValue(HttpServletRequest request, String name) {
        if(request == null || StringUtil.isEmptyOrNull(name)) {
        	return null;
        }
        String value = request.getParameter(name);
        if(StringUtil.isEmptyOrNull(value)) {
        	value = request.getHeader(name);
            if(StringUtil.isEmptyOrNull(value)) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equalsIgnoreCase(name)) {
                            value = cookie.getValue();
                            return value;
                        }
                    }
                }

            }
        }
        return value;
    }
	public static BasicCredential getBasicCredential(HttpServletRequest request) {
        if(request == null) {
        	return null;
        }
        String loginId = getParaCookieHeaderValue(request, "username");
        String password = null;
        if(StringUtil.isEmptyOrNull(loginId)) {
        	loginId = getParaCookieHeaderValue(request, "loginId");
        }
        if(StringUtil.isEmptyOrNull(loginId)) {
			String[] basicInfo = parseHttpBasicAuthString(request);
			if (basicInfo == null || basicInfo.length != 2) {
				return null;
			}
			loginId = basicInfo[0];
			password = basicInfo[1];

        }else {
        	password = getParaCookieHeaderValue(request, "password");
        }
        
        BasicCredential basicCredential = new BasicCredential();
        basicCredential.setLoginId(loginId);
        basicCredential.setPassword(password);
        
        return basicCredential;
    }

	public static boolean isEnforceTokenIp() {
		ClientAppInfo clientAppInfo = getClientAppInfo();
		boolean enforceTokenIp = clientAppInfo.getEnforceTokenIP();
		return enforceTokenIp;
	}
	public static boolean isEnforceTokenNonce() {
		ClientAppInfo clientAppInfo = getClientAppInfo();
		boolean enforceTokenNonce = clientAppInfo.getEnforceTokenNonce();
		return enforceTokenNonce;
	}

	public static void checkTokenIpAndNonceRequirement(String tokenIp, String tokenNonce) {
	
		if (StringUtil.isEmptyOrNull(tokenIp) && isEnforceTokenIp()) {
			String errMsg = "enforceTokenIp is set to true. can not find token ip in the IaaUser!";
			throw new AppException(Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
		}
		if (StringUtil.isEmptyOrNull(tokenNonce) && isEnforceTokenNonce()) {
			String errMsg = "enforceTokenNonce is set to true. can not find token nonce in the IaaUser!";
			throw new AppException(Status.ERROR_SECURITY_AUTHENTICATION, errMsg);
		}
		return;

	}
	
	public static ClientAppInfo getClientAppInfo() {
		ApiAuthInfo apiAuthInfo = AppThreadContext.getApiAuthInfo();
		String clientAppId = apiAuthInfo.clientAppId;
		ClientAppInfo clientAppInfo = AppComponents.clientAppInfoCache.getClientAppInfo(clientAppId);
		if(clientAppInfo == null) {
			String errMsg = "getClientAppInfo() - clientAppId (" + clientAppId + ") is not supported!" ;
			logger.error(errMsg);
			throw new AppException(Status.ERROR_VALIDATION, errMsg);
		}

		return clientAppInfo;

	}
	
	public static void handleHttpResposeBeforeBackToClient(BaseResponse response) {
		
		//task 1. set TxStatusContext
		//will be used in AuthenticationFilter
		ResponseStatus responseStatus = null;
		Status status = null;
		if (response == null || (responseStatus = response.getResponseStatus())==null || (status = responseStatus.getStatus()) == null) {
			AppThreadContext.setTxStatus(null);
		}
		AppThreadContext.setTxStatus(status);
		
		//task 2. auto renew access token if required
		//use data set from from AuthenticationFileter
		Boolean skipAuthEnabled = AppThreadContext.getSkipAuthEnabled();
		if(skipAuthEnabled) {
			return;
		}
		//auto renew access token
		ClientAppInfo clientAppInfo = AppUtil.getClientAppInfo();
		Boolean autoRenew = clientAppInfo.getApiAutoRenewAccessToken();
		String newToken = "";
		if(autoRenew) {
			if(status != null && status.code.equalsIgnoreCase(Status.SUCCESS.code)) {
				logger.info("Auto renew access token........");
				RequestAttributes ra = RequestContextHolder.getRequestAttributes();
				ServletRequestAttributes sra = null;
				if(ra != null) {
					 sra = (ServletRequestAttributes)ra;
					 HttpServletResponse httpResponse = sra.getResponse();
					 IaaUserI iaaUser = AppThreadContext.getIaaUser();
				 	 newToken = AppComponents.jwtService.issueToken(iaaUser, TokenHandlerI.TYPE_ACCESS_TOKEN, null);
					 httpResponse.addHeader("Token", newToken);
				}else {
					logger.info("RequestAttributes is null, does not auto renew access token........");
				}
			}else {
				logger.info("Transaction Status is not success, does not auto renew access token........");
			}
		}else {
			logger.debug("autoRenew is disabled, does not auto renew access token........");
		}

	}

}