/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.regex("/api/.*"))
            .build()
            .apiInfo(apiInfo())
        	.tags(	new Tag("about","Get details about the product"),
        			new Tag("action","Manage actions on document"),
        			new Tag("attachment", "Manage attachments"),
        			new Tag("attribute","Manage attributes"),
        			new Tag("multiattribute","Manage multi-attributes"),
        			new Tag("auth","Manage authorization token generation"),
        			new Tag("document","Manage documents"),
        			new Tag("email", "Manage outbound emails"),
        			new Tag("role","Manage roles for user"),
        			new Tag("session","Manage session"),
        			new Tag("template", "Manage email templates"),
        			new Tag("user","Manage users"),
        			new Tag("val","Manage static values"),
        			new Tag("variable","Manage app variables"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Document Workbench")
            .description("The list of APIs used by Document Workbench.")
            .version("V1")
//            .termsOfServiceUrl("http://terms-of-services.url")
//            .license("LICENSE")
//            .licenseUrl("http://url-to-license.com")
            .build();
    }

}