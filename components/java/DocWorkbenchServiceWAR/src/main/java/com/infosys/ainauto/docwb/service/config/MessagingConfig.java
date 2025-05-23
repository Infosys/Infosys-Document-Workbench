/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.config;

import java.util.Arrays;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class MessagingConfig {

	private static final String PROP_NAME_MESSAGING_BROKER_URL = "messaging.broker.url";
	private static final String PROP_NAME_MESSAGING_BROKER_CLIENT_USERNAME = "messaging.broker.client.username";
	private static final String PROP_NAME_MESSAGING_BROKER_CLIENT_PWD = "messaging.broker.client.drowssap";

	@Autowired
	private Environment environment;

	@Bean
	public ActiveMQConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				environment.getProperty(PROP_NAME_MESSAGING_BROKER_CLIENT_USERNAME),
				environment.getProperty(PROP_NAME_MESSAGING_BROKER_CLIENT_PWD),
				environment.getProperty(PROP_NAME_MESSAGING_BROKER_URL));
		connectionFactory.setTrustedPackages(Arrays.asList("com.infosys.ainauto.docwb.service"));
		return connectionFactory;
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory());
		return template;
	}
}
