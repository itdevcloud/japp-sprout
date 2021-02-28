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
package com.itdevcloud.japp.core.iaa.web;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Verify2ndFactorServlet is used for supporting two-factor authentication service.
 * <p>
 * Two types of 2-factor authentication services are supported here. 
 * They are:
 * <ul>
 * 		<li>Verification code via Email - a user will receive an email with a verification 
 * 		code as the second factor authentication value.
 * 		<li>Time-based One-time Password (TOTP) - a user can install a TOTP client 
 * 		implementation (E.g. Google Authenticator) to generate a totp password as the second
 * 		 factor authentication value.
 * </ul>
 * <p>
 * This servlet checks the application's CIDR whitelist first. Then it verifies that the JWT
 * in the request header is valid and includes correct second factor authentication
 * information. If the second factor authentication value is correct, then this user is an authorized user.
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */

@WebServlet(name = "verify2ndFactorServlet", urlPatterns = "/auth/verify2ndfactor")
public class Verify2ndFactorServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Verify2ndFactorServlet.class);

	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

//		AppUtil.initTransactionContext(httpRequest);
//		try {
//			logger.debug("Verify2ndFactorServlet.doPost()...........");
//
//			// App CIDR white list check begin
//			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
//				logger.error(
//						"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = " + AppUtil.getClientIp(httpRequest) + ".....");
//				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//						"Authorization Failed. code E209");
//				return;
//			}
//
//			//String secondFactorValueFromReq = httpRequest.getParameter(AppConstant.JWT_CLAIM_KEY_2NDFACTOR_VALUE);
//			String appId = httpRequest.getParameter("appId");
//			
//			// validate current japp token
//			String token = AppUtil.getJwtTokenFromRequest(httpRequest);
//			if (StringUtil.isEmptyOrNull(token)) {
//				// jwt token is null return 401
//				logger.error("Authentication Failed. code E304 - token is not validated, throw 401 error====");
//				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//						"Authentication Failed. code E304");
//				return;
//
//			}
//			if (!AppComponents.jwtService.isValidToken(token, AppComponents.pkiKeyCache.getJappPublicKey(), null)) {
//				// jwt token is not valid, return 401
//				logger.error("Authentication Failed. code E305 - token is not validated, throw 401 error====");
//				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//						"Authentication Failed. code E305");
//				return;
//			}
////			if (StringUtil.isEmptyOrNull(secondFactorValueFromReq)) {
////				logger.error(
////						"Validation Failed. code E304 - request does not contains 2nd factor verification value..");
////				AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_ERROR_SECURITY_NO_VERIFICATION_CODE,
////						"Validation Failed. code E304");
////				return;
////
////			}
//			//subject will be uid, not LoginId
//			String uid = AppUtil.getSubjectFromJwt(token);
//			SecondFactorInfo secondFactorInfo = null;
//			String newToken = null;
//			if (AppComponents.jwtService.validateJappToken(token)) {
//				// Japp token is valid, no need to validate anymore
//				String err = "2nd factor token has been verified, no need to validate anymore....";
//				logger.debug(err);
//				// no need to change token, just return original token
//				newToken = token;
//				AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_WARN_NOACTION, err);
//			} else {
//				Key key = AppComponents.pkiKeyCache.getJappPrivateKey();
//				// verify 2nd factor
//				secondFactorInfo = AppUtil.getSecondFactorInfoFromToken(token);
//				String type = secondFactorInfo.getType();
//				boolean verified = secondFactorInfo.isVerified();
//				int retryCount = secondFactorInfo.getRetryCount();
//				if (retryCount >= 3) {
//					String err = "2nd factor verification failed for more than " + retryCount
//							+ " times, need to acquire new verification code!";
//					logger.error(err);
//					// no need to change token, just return original token
//					newToken = token;
//					AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_ERROR_SECURITY_EXCEED_RETRY_COUNT, err);
//				} else if (AppConstant.IAA_2NDFACTOR_TYPE_VERIFICATION_CODE.equalsIgnoreCase(type)) {
//					//---get hashed 2nd factor value
//					//--- from repository first
//					String value = AppComponents.iaaService.getHashed2FactorVerificationCodeFromRepositoryByUid(uid);
//					//value == null means use hashed value in token
//					if(StringUtil.isEmptyOrNull(value)) {
//						logger.debug("Verify2ndFactorServlet.doPost() - active 2nd factor code comes from token...........");
//						value = secondFactorInfo.getValue();
//					}else {
//						logger.debug("Verify2ndFactorServlet.doPost() - active 2nd factor code comes from repository...........");
//					}
//					if (Hasher.hashPassword(secondFactorValueFromReq).equals(value)) {
//						String err = "2nd factor verification succeed.....";
//						logger.debug(err);
//						secondFactorInfo.setVerified(true);
//						secondFactorInfo.setRetryCount(retryCount);
//						int expireMins = ConfigFactory.appConfigService
//								.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
//						newToken = AppComponents.jwtService.updateToken(token, key, expireMins, secondFactorInfo);
//						AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_SUCCESS, err);
//					} else {
//						String err = "2nd factor verification failed, verification code in request = "
//								+ secondFactorValueFromReq;
//						logger.error(err);
//						secondFactorInfo.setRetryCount(retryCount + 1);
//						secondFactorInfo.setVerified(false);
//						int expireMins = ConfigFactory.appConfigService
//								.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH);
//						newToken = AppComponents.jwtService.updateToken(token, key, expireMins, secondFactorInfo);
//						AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_ERROR_INVALID_CODE, err);
//					}
//
//				}  else if (AppConstant.IAA_2NDFACTOR_TYPE_TOTP.equalsIgnoreCase(type)) {
//					IaaUserI iaaUser = AppComponents.iaaService.getIaaUserBySystemUid(uid);
//					if(iaaUser == null) {
//						logger.error(
//								"Can't retrieve user,  uid = " + uid + ".....");
//						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//								"Can't retrieve user.");
//						return;
//					}
//					String totpSecret = iaaUser.getTotpSecret();
//					Totp totp = new Totp(iaaUser.getTotpSecret());
//					if (StringUtil.isEmptyOrNull(totpSecret) || totp == null) {
//						String err = "no TOTP secret is setup for the user, '" + uid + "......";
//						logger.error(err);
//						AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY, err);
//						return;
//					}
//					if (isValidLong(secondFactorValueFromReq) && totp.verify(secondFactorValueFromReq)) {
//						String err = "2nd factor verification succeed.....";
//						logger.debug(err);
//						secondFactorInfo.setVerified(true);
//						secondFactorInfo.setRetryCount(retryCount);
//						int expireMins = ConfigFactory.appConfigService
//								.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_EXPIRATION_LENGTH);
//						newToken = AppComponents.jwtService.updateToken(token, key, expireMins, secondFactorInfo);
//						AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_SUCCESS, err);
//					} else {
//						String err = "2nd factor verification failed, verification code in request = "
//								+ secondFactorValueFromReq;
//						logger.debug(err);
//						secondFactorInfo.setRetryCount(retryCount + 1);
//						secondFactorInfo.setVerified(false);
//						int expireMins = ConfigFactory.appConfigService
//								.getPropertyAsInteger(AppConfigKeys.JAPPCORE_IAA_TOKEN_VERIFY_EXPIRATION_LENGTH);
//						newToken = AppComponents.jwtService.updateToken(token, key, expireMins, secondFactorInfo);
//						AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_ERROR_INVALID_CODE, err);
//					}
//					
//				}else {
//					String err = "2nd factor verification type '" + type + " is not supported......";
//					logger.error(err);
//					// no need to change token, just return original token
//					newToken = token;
//					AppUtil.setHttpResponse(httpResponse, 200, ResponseStatus.STATUS_CODE_ERROR_SECURITY_VERIFICATION_TYPE_UNSUPPORTED, err);
//				}
//			}
//
//			if (StringUtil.isEmptyOrNull(newToken)) {
//				logger.error(
//						"Verify2ndFactorServlet.doPost() - Authentication Failed. code E306. JAPP Token can not be created.....");
//				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
//						"Verify2ndFactorServlet Failed. code E306");
//				return;
//			}
//
//			httpResponse.addHeader("Content-Security-Policy", "default-src 'self';");
//			httpResponse.addHeader("X-XSS-Protection", "1; mode=block");
//			
//			if (StringUtil.isEmptyOrNull(appId) || appId.equalsIgnoreCase("TRACS")) {
//				httpResponse.addHeader("Token", newToken);
//				// httpResponse.addHeader("Access-Control-Allow-Origin",
//				// authParameters.getAngularOrigin());
//				// httpResponse.addHeader("Access-Control-Allow-Headers",
//				// "X-Requested-With,Origin,Content-Type, Accept, Token");
//				httpResponse.addHeader("Access-Control-Expose-Headers", "Token");				
//
//			}else {
//				httpResponse.addHeader("CallingApp-Token", newToken);
//				httpResponse.addHeader("Access-Control-Expose-Headers", "CallingApp-Token");
//				
//			}
//		} finally {
//			AppUtil.clearTransactionContext();
//		}

	}
	private boolean isValidLong(String code) {
	    try {
	        Long.parseLong(code);
	    } catch (Exception e) {
	        return false;
	    }
	    return true;
	}


}
