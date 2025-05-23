/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.infosys.ainauto.docwb.web.data.EmailData;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class EmailServiceTest extends TestCase {
	@Autowired
	private IEmailReaderService emailService;

	@Configuration
	static class TestConfiguration {
		@Bean
		public IEmailReaderService itsmService() {
			try {
				return new EmailReaderService();
			} catch (Exception e) {
				fail("Failed to create object");
			}
			return null;
		}
	}

	public void testConnectToEmailServer() {
		try {
			emailService.connectToEmailServer();
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
			fail("Failed to connect to email server");
		}

	}

	public void testGetAllUnreadEmails() {
		try {

			List<EmailData> emailDataList = emailService.readEmails();
			// for (int i = 0; i < emailDataList.size(); i++) {
			// }
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
			fail("Failed to get email list");
		}
	}

	public void testUpdateEmailAsRead() {
		try {
			emailService.updateEmailAsRead("305");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
			fail("Failed to Update Email As Read");
		}
	}

	public void testDisConnectToEmailServer() {
		try {
			emailService.disconnectFromEmailServer();
		} catch (Exception e) {
			assertTrue(false);
			fail("Failed to Disconnect to email server");
		}
	}
}
