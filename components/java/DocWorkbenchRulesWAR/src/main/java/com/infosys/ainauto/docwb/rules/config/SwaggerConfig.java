/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@PropertySource("classpath:application.properties")
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI customOpenAPI() {
	    return new OpenAPI()
	            // Set the basic information about your API
	            .info(new Info().title("Docwb Rules")
	                  .description("APIs to execute business rules.")
	                  .version("V1"));         
	}

}