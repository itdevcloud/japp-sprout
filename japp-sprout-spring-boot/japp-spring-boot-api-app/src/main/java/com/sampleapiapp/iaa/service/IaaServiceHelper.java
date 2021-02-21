package com.sampleapiapp.iaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.core.common.AppException;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaServiceHelper;
import com.itdevcloud.japp.core.service.customization.IaaServiceHelperI;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;



@Component
public class IaaServiceHelper extends DefaultIaaServiceHelper {

	private static final Logger logger = LogManager.getLogger(IaaServiceHelper.class);



	@Autowired
	private JdbcTemplate jdbcTemplate; // used for quick queries only


	@PostConstruct
	private void init() {
	}
	
	
	public IaaUserI getIaaUserFromRepositoryByUserId(String loginId) {
		logger.info("getIaaUserFromRepository() begins ...");
		long start = System.currentTimeMillis();


			IaaUserI iaaUser = null;
//
//			try {
//
//							String sql = "SELECT iu.LOGIN_ID, iu.FIRST_NAME, iu.LAST_NAME, iu.EMAIL, iu.CIDR_WHITE_LIST, iu.HASHED_PASSWORD, iu.CONTACT_CELL_NUMBER "
//									+ "from IAA_USER au where (iu.expiry_ts > CURRENT_TIMESTAMP or iu.expiry_ts is null ) and iu.END_TS is null "
//									+ "and upper(iu.LOGIN_ID) = upper(?)";
//							logger.info("getUserInfo() - SQL: " + sql + "\n");
//				
//							SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
//				
//				
//							if (rs.next()) {
//								iaaUser = new IaaUser();
//								iaaUser.setCurrentLoginId(rs.getString("LOGIN_ID"));
//								iaaUser.setUserId(rs.getString("LOGIN_ID"));
//								iaaUser.setFirstName(rs.getString("FIRST_NAME"));
//								iaaUser.setLastName(rs.getString("LAST_NAME"));
//								iaaUser.setEmail(rs.getString("EMAIL"));
//								iaaUser.setPhone(rs.getString("CONTACT_CELL_NUMBER"));
//								String wlStr = rs.getString("CIDR_WHITE_LIST");
//								if(!StringUtil.isEmptyOrNull(wlStr)) {
//									List<String> list = new ArrayList<String>();
//									for(String cidr: wlStr.split(";")) {
//										if(!SkUtil.isEmptyOrNull(cidr)){
//											list.add(cidr.trim());
//										}
//									}
//									iaaUser.setCidrWhiteList(list);
//								}
//								iaaUser.setCurrentHashedPassword(rs.getString("HASHED_PASSWORD"));
//								iaaUser.setTotpSecret("E47CWVVTI7BAXDD3");//hard code for now, will come from db
//								setIaaUserBusinessRole(iaaUser, userId);
//								setIaaUserAppRole(iaaUser, userId);
//								return iaaUser;
//							}
//			} catch (Exception e) {
//				logger.error(CommonUtil.getStackTrace(e));
//			}


		iaaUser = getDummyIaaUserByLoginId(loginId);

		long end = System.currentTimeMillis();
		logger.info("getIaaUserFromRepository() end........ took " + (end - start) + " ms. " + loginId);
		return iaaUser;
	}

	
	@Override
	public List<String> getUpdatedIaaUsers(long lastCheckTimestamp) {
		if (lastCheckTimestamp == -1) {
			return null;
		}
		ArrayList<String> idList = new ArrayList<>();
		return idList;
	}

//	@Override
//	public String getAndSend2factorValue(IaaUserI iaaUser, String secondFactorType) {
//		// TODO Auto-generated method stub
//		if (AppConstant.IAA_2NDFACTOR_TYPE_VERIFICATION_CODE.equalsIgnoreCase(secondFactorType)) {
//			String email = (iaaUser == null ? null : iaaUser.getEmail());
//			if (StringUtil.isEmptyOrNull(email)) {
//				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR,
//						"can't get email address to send the verification code!");
//			}
//			String subject = "verification code from JAPP";
//			int length = 6;
//			boolean useLetters = false;
//			boolean useNumbers = true;
//			String content = RandomStringUtils.random(length, useLetters, useNumbers);
//			String toAddresses = email;
//			try {
//				AppComponents.emailService.sendEmail(subject, content, toAddresses);
//				return content;
//			} catch (Exception e) {
//				logger.error(CommonUtil.getStackTrace(e));
//				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, "can't send the verification code!", e);
//			}
//		}else if (AppConstant.IAA_2NDFACTOR_TYPE_TOTP.equalsIgnoreCase(secondFactorType)) {
//			return null;
//		}else {
//			throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SECURITY_2FACTOR, " 2 factor auth type is not supported! secondFactorType = " + secondFactorType);
//		}
//	}



	@Override
	public Class<?> getInterfaceClass() {
		return IaaServiceHelperI.class;
	}

	
	






}
