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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.iaa.service.SecondFactorInfo;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

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

	public static BaseResponse createBaseResponse(String status, String message) {

		BaseResponse response = new BaseResponse();
		ResponseStatus responseStatus = createResponseStatus(status, message);

		response.setResponseStatus(responseStatus);

		TransactionContext tcContext = AppThreadContext.getTransactionContext();
		String txId = tcContext == null ? null : tcContext.getTransactionId();

		response.setServerTxId(txId);

		return response;
	}

	public static <T extends BaseResponse> T createResponse(Class<T> responseClass, String command, String status, String message) {

		T response = AppFactory.getInstance(responseClass);
		response.setCommand(command);

		ResponseStatus responseStatus = createResponseStatus(status, message);

		response.setResponseStatus(responseStatus);

		TransactionContext tcContext = AppThreadContext.getTransactionContext();
		String txId = tcContext == null ? null : tcContext.getTransactionId();

		response.setServerTxId(txId);

		return response;
	}


	public static ResponseStatus createResponseStatus(String status, String message) {

		ResponseStatus responseStatus = new ResponseStatus(status, message);

		return responseStatus;
	}

	public static <T> T GsonDeepCopy(Object sourceObj, Class<T> outputClass) {
		if (sourceObj == null || outputClass == null) {
			return null;
		}
		T targetObj;
		Gson gson = null;
		try {
			gson = new GsonBuilder().serializeNulls().create();
			String jsonStr = gson.toJson(sourceObj);
			targetObj = gson.fromJson(jsonStr, outputClass);
			return targetObj;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(CommonUtil.getStackTrace(t));
			return null;
		}
	}

	public static <T> T GsonDeepCopy(T sourceObj) {
		if (sourceObj == null) {
			return null;
		}
		return GsonDeepCopy(sourceObj, (Class<T>) sourceObj.getClass());
	}




	public static String getQuestionMarks1(List list) {
		if (list == null || list.size() == 0) {
			return null;
		}

		StringBuilder result = new StringBuilder("?");
		int size = list.size() - 1;
		for (int i = 0; i < size; i++) {
			result.append(", ?");
		}
		return result.toString();
	}

	public static String getIdentifier1(int count) {
		String result = null;
		if (count < 10) {
			result = "000" + count;
		} else if (count < 100) {
			result = "00" + count;
		} else if (count < 1000) {
			result = "0" + count;
		}

		return result;
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

	public static String getCorrespondingCommand(Object object) {
		String classSimpleName = object.getClass().getSimpleName();
		int idx = classSimpleName.indexOf(REQUEST_POSTFIX);
		if (idx <= 0) {
			idx = classSimpleName.indexOf(RESPONSE_POSTFIX);
			if (idx <= 0) {
				idx = classSimpleName.indexOf(PROCESSOR_POSTFIX);
			}
		}
		if (idx <= 0) {
			return null;
		}
		String command = classSimpleName.substring(0, idx);
		if (StringUtil.isEmptyOrNull(command)) {
			return null;
		}
		if (command.equalsIgnoreCase("Base")) {
			return null;
		}
		return command;

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

	public static String parseHttpCodeAuthString(ServletRequest request) {
		if (request == null || !(request instanceof HttpServletRequest)) {
			logger.error(
					"parseHttpCodeAuthString() - request is null or not instanceof HttpServletRequest, return null... ");
			return null;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String code;
		try {
			code = httpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}
		// logger.debug("verification code = " + code);
		return code;
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
				logger.debug("-------------remoteAddr 1-----X-FORWARDED-FOR-----" + remoteAddr);
				int idx = remoteAddr.indexOf(",");
				if (idx > 0) {
					// client part
					remoteAddr = remoteAddr.substring(0, idx);
				}
				idx = remoteAddr.lastIndexOf(":");
				if (idx > 0) {
					remoteAddr = remoteAddr.substring(0, idx);
				}
				logger.debug("-------------remoteAddr 2-------------------------" + remoteAddr);
			}
		}
		if (InetAddressValidator.getInstance().isValid(remoteAddr)) {
			return remoteAddr;
		} else {
			return "n/a";
		}

	}

	public static void setHttpResponse(HttpServletResponse httpResponse, int httpStatus, String statusCode,
			String message) {
		if (StringUtil.isEmptyOrNull(statusCode)) {
			statusCode = "" + httpStatus;
		}
		BaseResponse jappBaseResponse = AppUtil.createResponse(BaseResponse.class, null, statusCode, message);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String jsonResponseStr = gson.toJson(jappBaseResponse);

		httpResponse.setStatus(httpStatus);
		PrintWriter out;
		try {
			out = httpResponse.getWriter();
		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			return;
		}
		httpResponse.setContentType("application/json");
		httpResponse.setCharacterEncoding("UTF-8");
		out.print(jsonResponseStr);
		// out.flush();
	}

	public static String getJwtTokenFromRequest(HttpServletRequest httpRequest) {
		if (httpRequest == null) {
			logger.debug("getJwtTokenFromRequest - httpRequest is null.........");
			return null;
		}
		String authoHeader = httpRequest.getHeader(AppConstant.HTTP_AUTHORIZATION_HEADER_NAME);
		String token = null;
		if ((authoHeader != null) && (authoHeader.startsWith("Bearer "))) {
			token = authoHeader.substring(7);
			logger.debug("getJwtTokenFromRequest - found token in request header.........");
			return token;
		} else {
			logger.debug("getJwtTokenFromRequest - can not found token in request header.........");
			return null;
		}

	}

//	public static SecondFactorInfo getSecondFactorInfoFromToken(String jwtToken) {
//		SecondFactorInfo secondFactorInfo = new SecondFactorInfo();
//		Map<String, Object> claims = parseJwtClaims(jwtToken);
//		if (claims == null) {
//			return null;
//		}
//		String tmpV = "" + claims.get(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VERIFIED);
//		// logger.debug("getSecondFactorInfoFromToken() - tmpV = " + tmpV);
//		boolean isVerified = Boolean.valueOf(StringUtil.isEmptyOrNull(tmpV) ? "false" : tmpV);
//
//		String type = "" + claims.get(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_TYPE);
//		String value = "" + claims.get(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VALUE);
//
//		// logger.error("hashed 2nd factor value in token (1) - " + value);
//
//		tmpV = "" + claims.get(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_RETRY_COUNT);
//		int retryCount = Integer.valueOf((NumberUtils.isCreatable(tmpV) ? tmpV : "0"));
//
//		secondFactorInfo.setVerified(isVerified);
//		secondFactorInfo.setType(StringUtil.isEmptyOrNull(type) ? AppConstant.IAA_2NDFACTOR_TYPE_NONE : type);
//		secondFactorInfo.setValue(value);
//		secondFactorInfo.setRetryCount(retryCount);
//		return secondFactorInfo;
//	}

	public static Map<String, Object> parseJwtClaims(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getBody();

			Set<String> keySet = claims.keySet();
			if (keySet == null || keySet.isEmpty()) {
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keySet) {
				map.put(key, claims.get(key));
			}
			return map;

		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}

	}

	public static String getSubjectFromJwt(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getBody();

			String subject = claims.getSubject();
			logger.info("subject ====== " + subject);
			if (claims.containsKey("upn")) {
				String upn = (String) claims.get("upn");
				logger.info("convert subject to upn ====== " + upn);
				subject = (String) claims.get("upn");
			}
			return subject;

		} catch (Exception e) {
			logger.error(CommonUtil.getStackTrace(e));
			return null;
		}
	}

	public static void initTransactionContext(HttpServletRequest request) {
		logger.debug("initTransactionContext()...init transaction context........");
		TransactionContext txnCtx = new TransactionContext();
		String txId = UUID.randomUUID().toString();

		txnCtx.setTransactionId(txId);
		txnCtx.setRequestReceivedTimeStamp(new Timestamp(new Date().getTime()));
		txnCtx.setServerTimezoneId(TimeZone.getDefault().getID());
		if (request != null) {
			String host = getClientHost(request);
			host = (StringUtil.isEmptyOrNull(host) ? "n/a" : host);
			txnCtx.setClientHostName(host);
			String ip = getClientIp(request);
			ip = (StringUtil.isEmptyOrNull(ip) ? "n/a" : ip);
			txnCtx.setClientIP(ip);
		}

		// for request processor
		AppThreadContext.setTransactionContext(txnCtx);

		// for log4j2
		ThreadContext.put(AppConstant.JAPPCORE_TX_ID, txId);

	}

	public static void clearTransactionContext() {
		logger.debug("clearTransactionContext()...clear transaction context........");
		AppThreadContext.clean();
		ThreadContext.clearAll();
	}

}