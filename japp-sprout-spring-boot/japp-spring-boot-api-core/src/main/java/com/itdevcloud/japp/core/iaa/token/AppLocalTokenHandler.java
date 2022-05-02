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
package com.itdevcloud.japp.core.iaa.token;

import java.security.Key;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.core.iaa.service.DefaultIaaUser;
import com.itdevcloud.japp.core.common.AppUtil;
import com.itdevcloud.japp.core.common.ConfigFactory;
import com.itdevcloud.japp.core.service.customization.IaaUserI;
import com.itdevcloud.japp.core.service.customization.TokenHandlerI;
import com.itdevcloud.japp.se.common.security.Hasher;
import com.itdevcloud.japp.se.common.util.CommonUtil;
import com.itdevcloud.japp.se.common.util.DateUtil;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

@Component
public class AppLocalTokenHandler implements TokenHandlerI {
	private static final Logger logger = LogManager.getLogger(AppLocalTokenHandler.class);

	@PostConstruct
	public void init() {
	}

	
	@Override
	public String issueToken(IaaUserI iaaUser, String tokenType, Key privateKey, int expireMinutes, Map<String, Object> customClaimMap) {
		return TokenHandlerI.issueTokenDefault(iaaUser, tokenType, privateKey, expireMinutes, customClaimMap);
	}



	@Override
	public String getAccessToken(String token) {
		return null;
	}

	@Override
	public Map<String, Object> parseTokenClaims(String token) {
		return TokenHandlerI.parseTokenClaimsDefault(token);
	}

	@Override
	public Map<String, Object> isValidTokenByPublicKey(String token, PublicKey publicKey) {
		return TokenHandlerI.isValidTokenByPublicKeyDefault(token, publicKey);
	}


	@Override
	public IaaUserI getIaaUserBasedOnToken(String token) {
		return TokenHandlerI.getIaaUserBaseOnTokenDefault(token);
	}

	@Override
	public Map<String, Object> parseTokenHeaders(String token) {
		return TokenHandlerI.parseTokenHeadersDefault(token);
	}

	@Override
	public Map<String, Object> isValidToken(String token, Map<String, Object> claimEqualMatchMap,
			boolean ingoreNullInToken, String... args) {
		// TODO Auto-generated method stub
		return TokenHandlerI.isValidTokenDefault(token, claimEqualMatchMap, ingoreNullInToken, args);
	}
	
}
