/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
package com.itdevcloud.japp.core.iaa.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.aerogear.security.otp.Totp;

import com.itdevcloud.japp.core.api.vo.AppIaaUser;
import com.itdevcloud.japp.core.api.vo.MfaInfo;
import com.itdevcloud.japp.core.api.vo.MfaTOTP;
import com.itdevcloud.japp.core.api.vo.MfaVO;
import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.cahce.PkiKeyCache;
import com.itdevcloud.japp.core.common.CommonService;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.IaaUser;
import com.itdevcloud.japp.core.iaa.service.IaaService;
import com.itdevcloud.japp.core.iaa.service.JwtService;
import com.itdevcloud.japp.core.service.customization.ConfigServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@WebServlet(name = "TotpAuthServlet", urlPatterns = "/open/auth/totpauth")
public class TotpAuthnServlet extends jakarta.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	// private static final Logger logger =
	// LogManager.getLogger(Reset2ndFactorValueServlet.class);
	private static final Logger logger = LogManager.getLogger(TotpAuthnServlet.class);

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
						"Authorization Failed. code E209 - request IP is not on the APP's IP white list, user IP = "
								+ AppUtil.getClientIp(httpRequest) + ".....");
				AppUtil.setHttpResponse(httpResponse, 403, ResponseStatus.STATUS_CODE_ERROR_SECURITY,
						"Authorization Failed. code E209");
				return;
			}
			IaaServiceHelperI helper = AppFactory.getComponent(IaaServiceHelperI.class);

			String sessionIdFromReq = httpRequest.getParameter("s-id");
			String totpCodeFromReq = httpRequest.getParameter("totp-code");

			AppIaaUser iaaUser = AppUtil.getAppIaaUserFromSessionRepository(sessionIdFromReq);

			MfaInfo mfaInfo = iaaUser.getMfaInfo();
			MfaTOTP mfaTotp = mfaInfo==null?null:mfaInfo.getMfaTotp();
			String totpSecret = mfaTotp == null?null:mfaTotp.getSecret();
			
			if (StringUtil.isEmptyOrNull(totpSecret)) {
				String err = "no TOTP secret is setup for the user, '" + iaaUser.getUserIaaUID() + "'......";
				logger.error(err);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, err);
				return;
			}

			Totp totp = new Totp(totpSecret);

			if (totp == null) {
				String err = "no TOTP secret is setup for the user, '" + iaaUser.getUserIaaUID() + "'......";
				logger.error(err);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, err);
				return;
			}
			if (!isValidLong(totpCodeFromReq) || !totp.verify(totpCodeFromReq)) {
				String err = "TOTP secret <" + totpCodeFromReq + "> is not correct......";
				logger.error(err);
				AppUtil.setHttpResponse(httpResponse, 401, ResponseStatus.STATUS_CODE_ERROR_SECURITY, err);
				return;
			}

			boolean handleMFA = AppUtil.handleMfa(httpRequest, httpResponse, iaaUser);
			if (!handleMFA) {
				// issue new JAPP JWT token;
				String newToken = AppComponents.jwtService.issueJappToken(iaaUser);

				if (StringUtil.isEmptyOrNull(newToken)) {
					logger.error(
							"Reset2ndFactorValueServlet.doPost() - Authentication Failed. code E307. JAPP Token can not be created..for userIaaUId ="
									+ iaaUser.getUserIaaUID());
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
			}
			return;
		} finally {
			AppUtil.clearTransactionContext();
		}

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
