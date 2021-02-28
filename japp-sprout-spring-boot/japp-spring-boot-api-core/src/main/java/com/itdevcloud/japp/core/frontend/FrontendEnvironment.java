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
package com.itdevcloud.japp.core.frontend;

/**
*
* @author Marvin Sun
* @since 1.0.0
*/

public class FrontendEnvironment {
	private String name;
	private String production;
	private FrontendEnvSettings settings;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getProduction() {
		return production;
	}
	
	public void setProduction(String production) {
		this.production = production;
	}
	

	public FrontendEnvSettings getSettings() {
		return settings;
	}
	
	public void setSettings(FrontendEnvSettings settings) {
		this.settings = settings;
	}


}
