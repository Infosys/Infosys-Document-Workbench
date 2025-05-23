/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.controller.web;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handler class for web page requests
 * 
 **/
@Controller
@RequestMapping("/")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

//    @RequestMapping(value = "/", method = RequestMethod.GET)
//    public String getIndexPage() {
//        logger.debug("Home page request received");
//        return "master";
//    }
    
//    @RequestMapping(value = "/swagger", method = RequestMethod.GET)
//	  public String getSwaggerPage() {
//		  logger.debug("Swagger page request received");
//		  return "swagger-ui.html";
//	  }
    
    @RequestMapping(value = "/swagger", method = RequestMethod.GET)
    public void method(HttpServletResponse httpServletResponse) {
    	logger.debug("Swagger page request received");
        httpServletResponse.setHeader("Location", "swagger-ui.html");
        httpServletResponse.setStatus(302);
    }
}
