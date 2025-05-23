/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.datasource.impl.emailserver.basic.common.TestHelper;
import com.infosys.ainauto.testutils.FileTestUtility;
import com.infosys.ainauto.testutils.GeneralTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
	protected static EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig;
	protected static EmailServerDataSourceWriterConfig emailServerDataSourceWriterConfig;
	protected static final int TEST_RECORDS_CREATE_COUNT = 3;
	// Generate new test record identification keyword to be used for
	// all test artifacts
	// created dynamically during a testing session
	protected static final String TEST_RECORDS_IDENTIFIER_KEYWORD = GeneralTestUtility.generateRandomNumber();;

	protected static void createTestDataForReader() throws Exception {
		Properties properties = FileTestUtility.readPropertiesFileFromClassPath("emailServerDataSourceTest.properties");

		// Let To address be same as From address configured in SMTP
		String toId = properties.getProperty("ds.email-server.basic.writer.from.id");
		String toName = properties.getProperty("ds.email-server.basic.writer.from.name");

		String testArtifactPrefix = "UnitTesting_" + TEST_RECORDS_IDENTIFIER_KEYWORD + "_";

		List<Map<String, String>> mailFieldsMapList = new ArrayList<>();
		// Create test records specifically emails
		for (int i = 0; i < TEST_RECORDS_CREATE_COUNT; i++) {
			Map<String, String> mailFieldsMap = new HashMap<String, String>();
			mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_SUBJECT, testArtifactPrefix + String.valueOf(i));
			mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_TO_ID, toId);
			mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_TO_NAME, toName);
			mailFieldsMapList.add(mailFieldsMap);
		}

		TestHelper.EmailSenderHelper.sendEmails(mailFieldsMapList);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		logger.debug("Setup after class completed");
	}
}
