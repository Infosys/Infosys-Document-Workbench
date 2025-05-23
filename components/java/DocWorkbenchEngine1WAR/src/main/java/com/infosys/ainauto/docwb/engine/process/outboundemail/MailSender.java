/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.outboundemail;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.stereotype.EmailSender;
import com.infosys.ainauto.docwb.engine.core.template.emailsender.OutboundEmailSenderBase;

@Component
@EmailSender(title = "Send Outbound Emails", propertiesFile = "customization.properties")
public class MailSender extends OutboundEmailSenderBase {
	
	private static Logger logger = LoggerFactory.getLogger(MailSender.class);

	@PostConstruct
	private void init() {
		logger.debug("Initialized");
	}

}
