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
package com.itdevcloud.japp.core;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = {"com.itdevcloud.japp", "${jappcore.app.spring.scan.base.package}"})
@EnableJpaRepositories(basePackages = {"com.itdevcloud.japp", "${jappcore.app.spring.scan.base.package}"})
@EntityScan(basePackages = {"com.itdevcloud.japp", "${jappcore.app.spring.scan.base.package}"})
public class AppConfig {

}
