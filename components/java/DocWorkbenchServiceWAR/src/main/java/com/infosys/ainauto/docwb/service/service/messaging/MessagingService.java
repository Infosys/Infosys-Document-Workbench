/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.service.messaging;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.SystemUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.service.MessageData;

@Component
public class MessagingService implements IMessagingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessagingService.class);
	private static final String PROP_NAME_MESSAGING_BROKER_CLIENT_NAME = "messaging.broker.client.name";
	private static final String PROP_NAME_MESSAGING_BROKER_FEATURE_ENABLED = "messaging.broker.feature.enabled";

	@Autowired
	private Environment environment;

	@Autowired
	JmsTemplate jmsTemplate;

	private boolean isMessagingFeatureEnabled;
	private String myIpAddress;
	private String myHostName;

	@PostConstruct
	private void init() {
		LOGGER.debug("Initialized");
		isMessagingFeatureEnabled = Boolean
				.parseBoolean(environment.getProperty(PROP_NAME_MESSAGING_BROKER_FEATURE_ENABLED));
		LOGGER.info("Messaging featured enabled = " + isMessagingFeatureEnabled);
		
		try {
			myIpAddress = SystemUtility.getHostIpAddress();
		} catch (Exception ex) {
			LOGGER.error("Error while getting IP Address of this system", ex);
		} 
		try {
			myHostName = SystemUtility.getHostName();
		} catch (Exception ex) {
			LOGGER.error("Error while getting Hostname of this system", ex);
		} 
	}

	@Override
	public void putMessageInQueue(String queueName, Object obj) throws WorkbenchException {
		if (!isMessagingFeatureEnabled) {
			LOGGER.debug("Messaging skipped as feature is not enabled");
			return;
		}

		String objAsJson = convertToJson(obj);
		jmsTemplate.send(queueName, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(objAsJson);
			}
		});
		LOGGER.debug("Message put to queue =>" + queueName);
	}

	private String convertToJson(Object obj) throws WorkbenchException {
		String json = "";
		try {
			MessageData<Object> messageData = new MessageData<Object>();
			messageData.setMessage(obj);
			messageData.setSender(environment.getProperty(PROP_NAME_MESSAGING_BROKER_CLIENT_NAME));
			messageData.setHostname(myHostName);
			messageData.setHostIp(myIpAddress);

			abstract class MixIn {
				// Placeholder
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ActionParamAttrMappingDbData.class, MixIn.class);
			objectMapper.addMixIn(InsertEmailReqData.class, MixIn.class);
			json = objectMapper.writeValueAsString(messageData);
		} catch (Exception ex) {
			LOGGER.error("Error occured while converting object to JSON", ex);
			throw new WorkbenchException("Error occured while adding template in database", ex);
		}
		return json;
	}

}