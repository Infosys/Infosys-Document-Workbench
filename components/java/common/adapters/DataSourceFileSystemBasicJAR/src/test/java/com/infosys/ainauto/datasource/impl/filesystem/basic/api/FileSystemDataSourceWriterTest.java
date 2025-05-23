/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.filesystem.basic.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.impl.filesystem.basic.BaseTest;
import com.infosys.ainauto.datasource.impl.filesystem.basic.common.TestHelper;
import com.infosys.ainauto.datasource.model.file.FileSystemDataSourceRecord;
import com.infosys.ainauto.testutils.GeneralTestUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileSystemDataSourceWriterTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(FileSystemDataSourceWriterTest.class);
	private static IDataSourceWriter dataSourceWriter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fileSystemDataSourceWriterConfig = TestHelper
				.getFileSystemDataSourceWriterConfig("fileSystemDataSourceTest.properties");
		createTestDataForWriter();
		
		try {
			dataSourceWriter = TestHelper.getDataSourceWriter(fileSystemDataSourceWriterConfig);
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
			
			String uniqueFileName = "UnitTest_" + GeneralTestUtility.generateRandomNumber() + ".txt" ;
			
			String fileToBeSavedLocation = fileSystemDataSourceWriterConfig.getSaveToPath() + "/" + uniqueFileName;
			// Confirm that file doesn't already exist
			assertTrue(FileUtility.doesFileExist(fileToBeSavedLocation)==false);
			
			FileSystemDataSourceRecord fileSystemDataSourceRecord = new FileSystemDataSourceRecord();
			fileSystemDataSourceRecord.setFileNameToSave(uniqueFileName);
			fileSystemDataSourceRecord.setFileContent("This is a dummy file".getBytes());
			dataSourceWriter.writeItem(fileSystemDataSourceRecord, null);
			
			// Not check that file exists
			assertTrue(FileUtility.doesFileExist(fileToBeSavedLocation));
		} catch (Exception e) {
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
