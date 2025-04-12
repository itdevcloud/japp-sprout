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
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.MfaInfo;
import com.itdevcloud.japp.core.api.vo.MfaOTP;
import com.itdevcloud.japp.core.api.vo.MfaTOTP;
import com.itdevcloud.japp.core.api.vo.MfaVO;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.SessionServiceHelperI;
import com.itdevcloud.japp.core.session.DefaultSessionServiceHelper;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.RandomUtil;
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

	// private static final Logger logger = LogManager.getLogger(AppUtil.class);
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

	public static BaseResponse createBaseResponse(String command, String status, String message) {

		BaseResponse response = new BaseResponse();
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
			logger.error(AppUtil.getStackTrace(t));
			return null;
		}
	}

	public static <T> T GsonDeepCopy(T sourceObj) {
		if (sourceObj == null) {
			return null;
		}
		return GsonDeepCopy(sourceObj, (Class<T>) sourceObj.getClass());
	}

	public static String getStackTrace(Throwable t) {
		if (t == null) {
			return null;
		}
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return t.getMessage() + "\n" + sw.toString();
	}

	public static RuntimeException throwRuntimeException(Throwable e) {
		if (e == null) {
			throw new RuntimeException("AppUtil.throwRuntimeException() --- Throwable is null!");
		} else if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else {
			throw new RuntimeException(e);
		}
	}

	/**
	 * parse a String to a X.509 Certificate object.
	 * 
	 */
	public static Certificate getCertificateFromString(String certStr) {
		if (StringUtil.isEmptyOrNull(certStr)) {
			return null;
		}
		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		try {
			certStr = "-----BEGIN CERTIFICATE-----\n" + certStr + "\n-----END CERTIFICATE-----";
			// logger.debug(".........certStr=" + certStr);
			in = new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8));
			bis = new BufferedInputStream(in);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			while (bis.available() <= 0) {
				// logger.debug(".........certStr=" + certStr);
				String err = "Can't Parse certificate: ...Stop....!!!!!!!!!!!!!!";
				logger.error(err);
				throw new RuntimeException(err);
			}
			Certificate cert = cf.generateCertificate(bis);
			bis.close();
			bis = null;
			in.close();
			in = null;
			return cert;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}

		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		if (certificate == null) {
			return null;
		}
		return certificate.getPublicKey();
	}

	public static boolean isSameBigDecimal(BigDecimal b1, BigDecimal b2) {
		if (b1 == null && b2 == null) {
			return true;
		} else if (b1 == null) {
			return false;
		} else if (b2 == null) {
			return false;
		} else {
			return b1.longValue() == b2.longValue();
		}
	}

	public static boolean isSameTimestamp(Timestamp ts1, Timestamp ts2) {
		if (ts1 != null && ts2 != null) {
			return ts1.equals(ts2);
		} else if (ts1 != null) {
			return false;
		} else {
			return ts2 == null;
		}
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

	public static void setPropertyValue(Class<?> targetClass, Object targetObj, String targetPropertyName,
			Object targetValue) {
		if (targetObj == null && targetClass == null) {
			logger.error("setPropertyValue() - object and targetClass can not be both null, do nothing...");
		}
		if (StringUtil.isEmptyOrNull(targetPropertyName)) {
			logger.error("setPropertyValue() - propertyName can not be null, do nothing...");
		}
		// object class override target class
		targetClass = (targetObj == null ? targetClass : targetObj.getClass());
		while (targetClass != null) {
			try {
				Field field = targetClass.getDeclaredField(targetPropertyName);
				if (field != null) {
					field.setAccessible(true);
					field.set(targetObj, targetValue);
				} else {
					logger.error("setPropertyValue() - propertyName <" + targetPropertyName
							+ "> is not defined in class " + targetClass.getSimpleName() + ", do nothing...");
				}
				return;
			} catch (NoSuchFieldException e) {
				targetClass = targetClass.getSuperclass();
			} catch (Exception e) {
				logger.error("setPropertyValue() - failed, exception: " + AppUtil.getStackTrace(e));
				return;
			}
		}
		return;
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
			logger.error(AppUtil.getStackTrace(e));
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

	public static String getSessionId(AppIaaUser iaaUser) {

		String uid = "na-" + RandomUtil.generateAlphanumericString(5);
		if (iaaUser != null) {
			uid = iaaUser.getUserIaaUID();
		}
		String sessionId = uid + "-" + DateUtils.dateToString(new Date(), "ddHHmmss") + "-"
				+ RandomUtil.generateAlphanumericString(6);
		return sessionId;
	}

	private static String appendParameterToUrl(String url, String parameterName, String parameterValue) {
		if (StringUtil.isEmptyOrNull(url) || StringUtil.isEmptyOrNull(parameterName)) {
			return url;
		}
		if (StringUtil.isEmptyOrNull(parameterValue)) {
			parameterValue = "";
		}
		// remove end "/"
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 2);
			url = url + "?" + parameterName.trim() + "=" + parameterValue.trim();
		} else {
			if (url.indexOf("?") > 1) {
				url = url + "&" + parameterName.trim() + "=" + parameterValue.trim();
			}
		}
		return url;
	}

//	private static MfaVO getMfaVOByType(List<MfaVO> mfaVOList, String type) {
//		if (mfaVOList == null || mfaVOList.isEmpty() || StringUtil.isEmptyOrNull(type)) {
//			return null;
//		}
//		for (MfaVO vo : mfaVOList) {
//			if (type.equalsIgnoreCase(vo.getType())) {
//				return vo;
//			}
//		}
//		return null;
//	}

	public static boolean handleMfa(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			AppIaaUser iaaUser) {
		if (httpRequest == null || httpResponse == null || iaaUser == null) {
			logger.debug("httpRequest,  httpResponse and iaaUser is null, do nothing......");
			return false;
		}
		boolean appMfaEnabled = ConfigFactory.appConfigService
				.getPropertyAsBoolean(AppConfigKeys.JAPPCORE_IAA_MFA_ENABLED);

		String appMfaDefaultType = ConfigFactory.appConfigService
				.getPropertyAsString(AppConfigKeys.JAPPCORE_IAA_MFA_DEFAULT_TYPE, MfaVO.MFA_TYPE_OTP);

		List<MfaVO> userMfaVOList = iaaUser.getMfaVOList();
		if (!appMfaEnabled && (userMfaVOList == null || userMfaVOList.isEmpty())) {
			// MFA is not enabled for the app and for the user, do nothing;
			logger.debug("MFA is not enabled for the user and for the app, do nothing......");
			return false;
		}
		try {
			// MFA is needed
			// validate session ID
			boolean foundSessionIdInRequest = false;
			String reqSessionId = httpRequest.getParameter("s-id");
			String userSessionId = iaaUser.getSessionId();
			if (StringUtil.isEmptyOrNull(reqSessionId) && StringUtil.isEmptyOrNull(userSessionId)) {
				userSessionId = getSessionId(iaaUser);
				iaaUser.setSessionId(userSessionId);
				reqSessionId = userSessionId;
			} else if (StringUtil.isEmptyOrNull(reqSessionId)) {
				reqSessionId = userSessionId;
			} else if (StringUtil.isEmptyOrNull(userSessionId)) {
				userSessionId = reqSessionId;
				iaaUser.setSessionId(userSessionId);
				foundSessionIdInRequest = true;
			} else {
				if (!userSessionId.equals(reqSessionId)) {
					foundSessionIdInRequest = true;
					logger.error("Session mismatch detected. code E200, throw 401 error====");
					AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
							"Authentication Failed. code E200");
					return false;
				}
			}
			// check which MFA has been done
			SessionServiceHelperI helper = AppFactory.getComponent(SessionServiceHelperI.class);
			MfaInfo mfaInfoFromSession = helper.getMfaInfoFromSessionRepo(userSessionId);

			RequestDispatcher dispatcher = null;
			String mfaUrl = null;
			String mfaUrlKey = null;
			String mfaType = null;
			if (userMfaVOList == null || userMfaVOList.isEmpty()) {
				// user MFA not enabled, use app default setting
				MfaVO vo = mfaInfoFromSession.getMfaVO(appMfaDefaultType);
				if (vo != null && vo.isVerified()) {
					// default MFA has been done, do nothing
					return false;
				}
				mfaUrlKey = AppConfigKeys.JAPPCORE_IAA_MFA_URL_BASE + "." + appMfaDefaultType.toLowerCase();
				mfaUrl = ConfigFactory.appConfigService.getPropertyAsString(mfaUrlKey);
				mfaType = appMfaDefaultType;
			} else {
				boolean mfaRequired = true;
				for (MfaVO mfaVO : userMfaVOList) {
					MfaVO vo = mfaInfoFromSession.getMfaVO(mfaVO.getType());
					if (vo != null && vo.isVerified()) {
						mfaRequired = false;
						continue;
					}
					mfaRequired = true;
					mfaUrlKey = AppConfigKeys.JAPPCORE_IAA_MFA_URL_BASE + "." + mfaVO.getType().trim().toLowerCase();
					mfaUrl = ConfigFactory.appConfigService.getPropertyAsString(mfaUrlKey);
					mfaType = mfaVO.getType().trim();
					break;
				} // end for
				if (!mfaRequired) {
					// at least verify one mfa, do nothing
					return false;
				}
			}

			if (StringUtil.isEmptyOrNull(mfaUrl)) {
				logger.error("Can not retrieve MFA URL, please check configuration, mfaUrlKey = " + mfaUrlKey);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed: NO MFA URL found! code E200");
				return false;
			}

			if (!foundSessionIdInRequest) {
				mfaUrl = appendParameterToUrl(mfaUrl, "s-id", userSessionId);
			}
			dispatcher = httpRequest.getRequestDispatcher(mfaUrl);
			dispatcher.forward(httpRequest, httpResponse);
		} catch (Throwable t) {
			logger.error("Can not process MFA , error: " + t, t);
			AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
					"MFA Authentication Failed:  code E200");
			//make sure not proceed to generate JWT by return true
			return true;

		}
		return true;
	}
	public static AppIaaUser getAppIaaUserFromSessionRepository(String userSessionId) {
		if (userSessionId == null || StringUtil.isEmptyOrNull(userSessionId) ) {
			logger.debug("userSessionId is null, return null......");
			return null;
		}
		IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);
		String userIaaUID = helper.getUserIaaUIDFromSessionRepository(userSessionId); 
		
		AppIaaUser iaaUser = helper.getIaaUserFromRepositoryByUserIaaUID(userIaaUID);
		return iaaUser;
	}

	public static void setHttpResponse(HttpServletResponse httpResponse, int httpStatus, String statusCode,
			String message) {
		if (StringUtil.isEmptyOrNull(statusCode)) {
			statusCode = "" + httpStatus;
		}
		BaseResponse jappBaseResponse = AppUtil.createBaseResponse(null, statusCode, message);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String jsonResponseStr = gson.toJson(jappBaseResponse);

		httpResponse.setStatus(httpStatus);
		PrintWriter out;
		try {
			out = httpResponse.getWriter();
		} catch (Exception e) {
			logger.error(AppUtil.getStackTrace(e));
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

	public static Map<String, Object> parseJwtClaims(String jwtToken) {
		try {
			if (jwtToken == null || jwtToken.trim().isEmpty()) {
				return null;
			}
			int idx = jwtToken.lastIndexOf('.');
			String tokenWithoutSignature = jwtToken.substring(0, idx + 1);

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().build().parseUnsecuredClaims(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getPayload();

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
			logger.error(AppUtil.getStackTrace(e));
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

			Jwt<Header, Claims> jwtWithoutSignature = Jwts.parser().build().parseUnsecuredClaims(tokenWithoutSignature);
			Claims claims = jwtWithoutSignature.getPayload();

			String subject = claims.getSubject();
			logger.info("subject ====== " + subject);
			if (claims.containsKey("upn")) {
				String upn = (String) claims.get("upn");
				logger.info("convert subject to upn ====== " + upn);
				subject = (String) claims.get("upn");
			}
			return subject;

		} catch (Exception e) {
			logger.error(AppUtil.getStackTrace(e));
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

	public static <T>  T getObjectFromJsonString(String jsonString, Class<T> clazz) {
		if (StringUtil.isEmptyOrNull(jsonString)) {
			return null;
		}
		String jsonStr = handleNullForJsonStringDeserialization(jsonString);

		Gson gson = new GsonBuilder().serializeNulls().create();
		T obj = gson.fromJson(jsonStr, clazz);
		return obj;
	}

	public static String getJsonStringFromObject(Object obj) {
		if (obj == null) {
			return null;
		}
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(obj);
	}

	private static String handleNullForJsonStringDeserialization(String jsonString) {
		if (StringUtil.isEmptyOrNull(jsonString)) {
			return jsonString;
		}
		String pattern = "(:)(\\s*)([,\\}])";
		String output = jsonString.replaceAll(pattern, "$1\"\"$3");
		return output;
	}

	public static void main(String[] args) {

		DefaultSessionServiceHelper sessionHelper = new DefaultSessionServiceHelper();
		
		MfaTOTP mfaTotp = new MfaTOTP();
		mfaTotp.setSecret("12345");
		mfaTotp.setExpiryDate(new Date());

		MfaOTP mfaOtp = new MfaOTP();
		mfaOtp.setCode("12345");
		mfaOtp.setExpiryDate(new Date());

		MfaInfo mfaInfo = new MfaInfo();
		mfaInfo.addOrUpdateMfaVO(mfaTotp);
		
		sessionHelper.setMfaInfoToSessionRepo("u-123456", mfaInfo);
		MfaInfo mfaInfoSession = sessionHelper.getMfaInfoFromSessionRepo("u-123456");
		logger.info("---------1-------" + mfaInfoSession);
		
		mfaInfoSession.addOrUpdateMfaVO(mfaOtp);
		logger.info("---------2-------" + mfaInfoSession);
		
		sessionHelper.setMfaInfoToSessionRepo("u-123456", mfaInfoSession);
		mfaInfoSession = sessionHelper.getMfaInfoFromSessionRepo("u-123456");
		logger.info("---------3-------" + mfaInfoSession);

	}

}