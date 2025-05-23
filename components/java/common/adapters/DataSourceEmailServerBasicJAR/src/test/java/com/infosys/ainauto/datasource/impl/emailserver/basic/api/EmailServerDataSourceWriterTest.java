/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.impl.emailserver.basic.BaseTest;
import com.infosys.ainauto.datasource.impl.emailserver.basic.common.TestHelper;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.testutils.FileTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmailServerDataSourceWriterTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceWriterTest.class);
	private static IDataSourceWriter dataSourceWriter;
	private static Address[] recipientAddressArray;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		emailServerDataSourceWriterConfig = TestHelper
				.getEmailServerDataSourceWriterConfig("emailServerDataSourceTest.properties");
		Properties smtpProperties = FileTestUtility
				.readPropertiesFileFromClassPath("emailServerDataSourceTest.properties");
		{
			String senderEmailId = smtpProperties.getProperty("ds.email-server.basic.writer.from.id");

			// Set receiver same as sender
			Address recipientAddress = new InternetAddress(senderEmailId, senderEmailId);
			List<Address> addressList = new ArrayList<>();
			addressList.add(recipientAddress);
			recipientAddressArray = addressList.toArray(new Address[addressList.size()]);
		}

		try {
			dataSourceWriter = TestHelper.getDataSourceWriter(emailServerDataSourceWriterConfig);
			boolean status = dataSourceWriter.connect();
			assertTrue(status == true);
		} catch (Exception e) {
			logger.error("Error while getting DataSourceWriter", e);
			fail("Some error occurred");
		}

		logger.debug("Setup before class completed");
	}

	@Test
	public void testScenario01WriteRecord() throws Exception {
		try {

			String currentTime = new Date().toString();

			EmailServerDataSourceRecord emailServerDataSourceRecord = (EmailServerDataSourceRecord) dataSourceWriter
					.generateNewItem();

			MimeMessage mimeMessage = (MimeMessage) emailServerDataSourceRecord.getMimeMessage();

			mimeMessage.setSubject("Unit Testing - " + currentTime, "UTF-8");
			mimeMessage.setText("This is a simple body.", "UTF-8");

			mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddressArray);
			dataSourceWriter.writeItem(emailServerDataSourceRecord, null);
		} catch (Exception e) {
			logger.error("Error while using dataSourceWriter", e);
			fail("Some error occurred");
		}
	}

	@Test
	public void testScenario02WriteRecordAndSaveACopy() throws Exception {
		try {
			String currentTime = new Date().toString();

			EmailServerDataSourceRecord emailServerDataSourceRecord = (EmailServerDataSourceRecord) dataSourceWriter
					.generateNewItem();

			MimeMessage mimeMessage = (MimeMessage) emailServerDataSourceRecord.getMimeMessage();

			mimeMessage.setSubject("Unit Testing - " + currentTime, "UTF-8");
			mimeMessage.setText("This is a simple body.", "UTF-8");
			mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddressArray);

			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put("isSaveMessageCopy", "True");
			dataSourceWriter.writeItem(emailServerDataSourceRecord, paramsMap);
		} catch (Exception e) {
			logger.error("Error while using dataSourceWriter", e);
			fail("Some error occurred");
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		try {
			boolean status = dataSourceWriter.disconnect();
			assertTrue(status == true);
		} catch (Exception e) {
			logger.error("Error while disconnecting DataSourceWriter", e);
			fail("Some error occurred");
		}
	}

}
