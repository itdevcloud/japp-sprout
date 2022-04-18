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
package com.itdevcloud.japp.core.iaa.provider;



/**
 * Base Request Class.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */

import java.io.Serializable;
import com.itdevcloud.japp.core.api.vo.ClientAuthProvider;

public class AuthProviderHandlerInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected String clientAppId ;
	protected String clientAuthKey;
	protected String tokenNonce;
	protected String loginUserEmail;
	protected String clientIP;
	protected ClientAuthProvider authProvider;
	
}
