/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
package com.itdevcloud.japp.core.iaa.web;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.cahce.PkiKeyCache;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.IaaService;
import com.itdevcloud.japp.core.iaa.service.JwtService;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.tools.common.util.StringUtil;
/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@WebServlet(name = "reset2ndFactorValueServlet", urlPatterns = "/auth/reset2ndFactorValue")
public class Reset2ndFactorValueServlet extends javax.servlet.http.HttpServlet {


	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Reset2ndFactorValueServlet.class);

	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		AppUtil.initTransactionContext(httpRequest);
		try {
			logger.debug("Reset2ndFactorValueServlet.doPost()......start.....");

			// resolve CORS error
			httpResponse.addHeader("Access-Control-Allow-Origin",
					ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_FRONTEND_UI_ORIGIN));
			httpResponse.addHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
			httpResponse.addHeader("Access-Control-Allow-Headers",
					"Origin, X-Requested-With, Content-Type, Accept, Authorization");

			// CIDR white list check begin
			if (!AppComponents.commonService.matchAppIpWhiteList(httpRequest)) {
				logger.error(
						"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = " + AppUtil.getClientIp(httpRequest) + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E209");
				return;
			}

			// validate current piscesjapp token
			String token = AppUtil.getJwtTokenFromRequest(httpRequest);
			if (StringUtil.isEmptyOrNull(token)) {
				// jwt token is null return 401
				logger.error("Authentication Failed. code E804 - token is not validated, throw 401 error====");
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E804");
				return;

			}
			if (!AppComponents.jwtService.isValidToken(token, AppComponents.pkiKeyCache.getPiscesJappPublicKey(), null)) {
				// jwt token is not valid, return 401
				logger.error("Authentication Failed. code E805 - token is not validated, throw 401 error====");
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E805");
				return;
			}
			//subject must be userId, not loginId!!!
			String userId = AppUtil.getSubjectFromJwt(token);

			IaaUser piscesjappIaaUser = null;
			try{
				piscesjappIaaUser = AppComponents.iaaService.getIaaUserByUserId(userId);
			}catch(AppException e){
				logger.error(
						"Reset2ndFactorValueServlet.doPost() - Authentication Failed. code E306. can't retrieve iaa user - userId =" + userId + " - \n"
								+ AppUtil.getStackTrace(e));
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authentication Failed. code E306");
				return;
			}

			// Application role list check
			if (!AppComponents.commonService.matchAppRoleList(piscesjappIaaUser)) {
				logger.error(
						"Authorization Failed. code E508 - requestor's is not on the APP's role list" + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E508");
				return;
			}

			// issue new PISCESJAPP JWT token;
			String newToken = AppComponents.jwtService.issuePiscesJappToken(piscesjappIaaUser);

			if (StringUtil.isEmptyOrNull(newToken)) {
				logger.error(
						"Reset2ndFactorValueServlet.doPost() - Authentication Failed. code E307. PISCESJAPP Token can not be created..for userId =" + userId);
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Reset2ndFactorValueServlet Failed. code E307");
				return;
			}

			httpResponse.addHeader("Token", newToken);
			// httpResponse.addHeader("Access-Control-Allow-Origin",
			// authParameters.getAngularOrigin());
			// httpResponse.addHeader("Access-Control-Allow-Headers",
			// "X-Requested-With,Origin,Content-Type, Accept, Token");
			httpResponse.addHeader("Access-Control-Expose-Headers", "Token,MaintenanceMode");
		} finally {
			AppUtil.clearTransactionContext();
		}

	}

}
