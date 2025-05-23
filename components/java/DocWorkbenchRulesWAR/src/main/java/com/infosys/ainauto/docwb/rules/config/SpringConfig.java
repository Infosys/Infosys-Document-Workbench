/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

	private static Logger logger = LoggerFactory.getLogger(SpringConfig.class);
	
	@Bean
	public KieContainer kieContainer() {
		long start = System.currentTimeMillis();
		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		{
			KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

			//To be used if DRL files need to be pre-loaded on startup
			//kieFileSystem.write(ResourceFactory.newClassPathResource("rules/Applicant.drl"));
			//kieFileSystem.write(ResourceFactory.newClassPathResource("rules/Applicant2.drl"));
			
			KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
			kieBuilder.buildAll();
			if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
				throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
			}
		}
		KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
		long end = System.currentTimeMillis();
		logger.info("Time taken to create kieContainer=" + (end-start));
		
		return kieContainer;
	}
}
