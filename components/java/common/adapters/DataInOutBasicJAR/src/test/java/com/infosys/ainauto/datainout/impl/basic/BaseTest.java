/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic;

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

import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.config.DataOutputConfig;
import com.infosys.ainauto.datainout.impl.basic.common.TestHelper;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceWriterConfig;
import com.infosys.ainauto.testutils.FileTestUtility;
import com.infosys.ainauto.testutils.GeneralTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
	protected static DataInputConfig dataInputConfig;
	protected static DataOutputConfig dataOutputConfig;
	protected static final int TEST_RECORDS_CREATE_COUNT = 3;
	// Generate new test record identification keyword to be used for
	// all test artifacts
	// created dynamically during a testing session
	protected static final String TEST_RECORDS_IDENTIFIER_KEYWORD = GeneralTestUtility.generateRandomNumber();;

	protected static void createTestDataForReader(boolean emailInMailServer, boolean emailInFileSystem,
			boolean nonEmailInFileSystem) throws Exception {

		String testArtifactPrefix = "UnitTesting_" + TEST_RECORDS_IDENTIFIER_KEYWORD + "_";

		for (AbstractDataInOutConfig.ProviderDataSourceConfig providerDataSourceConfig : dataInputConfig
				.getProviderDataSourceConfigList()) {

			if (providerDataSourceConfig.getDataSourceConfig() instanceof FileSystemDataSourceReaderConfig) {
				FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig = (FileSystemDataSourceReaderConfig) providerDataSourceConfig
						.getDataSourceConfig();
				String fileSource = fileSystemDataSourceReaderConfig.getFileSourceDir();
				String fileArchival = fileSystemDataSourceReaderConfig.getFileArchivalDir();

				// Create folders if they don't exist
				FileTestUtility.createDirsRecursively(fileSource);
				FileTestUtility.createDirsRecursively(fileArchival);

				// Create random files (non-email) in File System
				if (nonEmailInFileSystem) {
					for (int i = 0; i < TEST_RECORDS_CREATE_COUNT; i++) {
						FileTestUtility.saveFile(fileSource + "/" + testArtifactPrefix + String.valueOf(i) + ".txt",
								"For testing");
					}
				}

				// Create email in File System
				if (emailInFileSystem) {
					// Create EML file as test record
					String fileName = "test.eml";
					String emlFileName = FileTestUtility.getAbsolutePathOfResourceFile(fileName);
					FileTestUtility.copyFile(emlFileName, fileSource + "/" + testArtifactPrefix + fileName);
				}
			}

		}

		// Create random emails in Mail Server
		if (emailInMailServer) {
			Properties properties = FileTestUtility.readPropertiesFileFromClassPath("basicDataOutputTest.properties");

			// Let To address be same as From address configured in SMTP
			String toId = properties.getProperty("ds.email-server.basic.writer.from.id");
			String toName = properties.getProperty("ds.email-server.basic.writer.from.name");

			List<Map<String, String>> mailFieldsMapList = new ArrayList<>();
			// Create test records specifically emails
			for (int i = 0; i < TEST_RECORDS_CREATE_COUNT; i++) {
				Map<String, String> mailFieldsMap = new HashMap<String, String>();
				mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_SUBJECT,
						testArtifactPrefix + String.valueOf(i));
				mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_TO_ID, toId);
				mailFieldsMap.put(TestHelper.EmailSenderHelper.MAIL_FIELD_TO_NAME, toName);
				mailFieldsMapList.add(mailFieldsMap);
			}

			TestHelper.EmailSenderHelper.sendEmails(mailFieldsMapList);
		}
	}

	protected static void createTestDataForWriter() throws Exception {
		for (AbstractDataInOutConfig.ProviderDataSourceConfig providerDataSourceConfig : dataOutputConfig
				.getProviderDataSourceConfigList()) {
			if (providerDataSourceConfig.getDataSourceConfig() instanceof FileSystemDataSourceWriterConfig) {
				FileSystemDataSourceWriterConfig fileSystemDataSourceWriterConfig = (FileSystemDataSourceWriterConfig) providerDataSourceConfig
						.getDataSourceConfig();

				String saveToPath = fileSystemDataSourceWriterConfig.getSaveToPath();
				FileTestUtility.createDirsRecursively(saveToPath);
			}
		}
	}

	protected String createTrackableEmailSubject(String subjectText) {
		return subjectText + " [" + GeneralTestUtility.generateRandomNumber() + "]";
	}

	@AfterClass
	public static void tearDown() throws Exception {
		logger.debug("Setup after class completed");
	}
}
