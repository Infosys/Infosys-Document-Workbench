/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.filesystem.basic;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceWriterConfig;
import com.infosys.ainauto.testutils.FileTestUtility;
import com.infosys.ainauto.testutils.GeneralTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
	protected static FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig;
	protected static FileSystemDataSourceWriterConfig fileSystemDataSourceWriterConfig;
	protected static final int TEST_RECORDS_CREATE_COUNT = 3;
	// Generate new test record identification keyword to be used for
	// all test artifacts
	// created dynamically during a testing session
	protected static final String TEST_RECORDS_IDENTIFIER_KEYWORD = GeneralTestUtility.generateRandomNumber();

	protected static void createTestDataForReader() throws Exception {
		// Reader config
		if (fileSystemDataSourceReaderConfig != null) {
			String fileSource = fileSystemDataSourceReaderConfig.getFileSourceDir();
			String fileArchival = fileSystemDataSourceReaderConfig.getFileArchivalDir();

			// Create folders if they don't exist
			FileTestUtility.createDirsRecursively(fileSource);
			FileTestUtility.createDirsRecursively(fileArchival);

			String testArtifactPrefix = "UnitTesting_" + TEST_RECORDS_IDENTIFIER_KEYWORD + "_";

			// Create test records specifically files
			for (int i = 0; i < TEST_RECORDS_CREATE_COUNT; i++) {
				FileTestUtility.saveFile(fileSource + "/" + testArtifactPrefix + String.valueOf(i) + ".txt",
						"For testing");
			}
		}
	}

	protected static void createTestDataForWriter() throws Exception {
		// Writer config
		if (fileSystemDataSourceWriterConfig != null) {
			String saveToPath = fileSystemDataSourceWriterConfig.getSaveToPath();

			FileTestUtility.createDirsRecursively(saveToPath);
		}

	}

	@AfterClass
	public static void tearDown() throws Exception {
		logger.debug("Setup after class completed");
	}
}
