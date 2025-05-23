/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datainout.api.IDataOutputWriter;
import com.infosys.ainauto.datainout.impl.basic.BaseTest;
import com.infosys.ainauto.datainout.impl.basic.common.TestHelper;
import com.infosys.ainauto.datainout.model.DataOutputRecord;
import com.infosys.ainauto.datainout.model.email.EmailAddress;
import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.testutils.FileTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicDataOutputWriterTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(BasicDataOutputWriterTest.class);
	private static IDataOutputWriter dataOutputWriter;
	private static List<EmailAddress> emailAddressToList = new ArrayList<>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataOutputConfig = TestHelper.getDataOutputConfig("basicDataOutputTest.properties");

		// Set recipient same as sender
		Properties smtpProperties = FileTestUtility.readPropertiesFileFromClassPath("basicDataOutputTest.properties");
		String senderEmailId = smtpProperties.getProperty("ds.email-server.basic.writer.from.id");
		emailAddressToList.add(new EmailAddress(senderEmailId, " "));

		createTestDataForWriter();

		try {
			dataOutputWriter = TestHelper.getDataOutputWriter(dataOutputConfig);
			List<Boolean> resultList = dataOutputWriter.connect();
			long trueCount = resultList.stream().filter(x -> x == true).count();
			assertTrue(resultList.size() == trueCount);
		} catch (Exception ex) {
			logger.error("Error while getting DataInputReader", ex);
			fail("Some error occurred");
		}

		logger.debug("Setup before class completed");
	}

	@Test
	public void testScenario01WriteItem() throws Exception {
		EmailRecord emailRecordToSend = new EmailRecord();
		emailRecordToSend.setEmailAddressToList(emailAddressToList);
		emailRecordToSend.setEmailSubject(createTrackableEmailSubject("UnitTest_ - \\/:*<>| \" Plain text"));
		emailRecordToSend.setEmailBodyText("This is a plain text email.\r\n");

		DataOutputRecord dataOutputRecord = new DataOutputRecord();
		dataOutputRecord.setEmailRecord(emailRecordToSend);

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("isSaveMessageCopy", "True");
		dataOutputWriter.writeItem(dataOutputRecord, paramsMap);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		try {
			List<Boolean> resultList = dataOutputWriter.disconnect();
			long trueCount = resultList.stream().filter(x -> x == true).count();
			assertTrue(resultList.size() == trueCount);
		} catch (Exception e) {
			logger.error("Error while disconnecting dataOutputWriter", e);
			fail("Some error occurred");
		}
	}
}
