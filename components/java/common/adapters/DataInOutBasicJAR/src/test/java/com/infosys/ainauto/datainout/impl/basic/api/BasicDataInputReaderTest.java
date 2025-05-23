/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.impl.basic.BaseTest;
import com.infosys.ainauto.datainout.impl.basic.common.TestHelper;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceFolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicDataInputReaderTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(BasicDataInputReaderTest.class);
	private static IDataInputReader dataInputReader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataInputConfig = TestHelper.getDataInputConfig("basicDataInputTest.properties");
		
		try {
			dataInputReader = TestHelper.getDataInputReader(dataInputConfig);
			List<Boolean> resultList = dataInputReader.connect();
			long trueCount = resultList.stream().filter(x -> x == true).count();
			assertTrue(resultList.size() == trueCount);
		} catch (Exception ex) {
			logger.error("Error while getting DataInputReader", ex);
			fail("Some error occurred");
		}
		
		logger.debug("Setup before class completed");
	}

	@Test
	public void testScenario01GetNewRecordsAndMarkAsRead() {
		try {
			boolean emailInMailServer, emailInFileSystem, nonEmailInFileSystem;
			emailInMailServer = true;
			emailInFileSystem = false;
			nonEmailInFileSystem = true;
			createTestDataForReader(emailInMailServer, emailInFileSystem, nonEmailInFileSystem);

			int noOfTestRecordsFetched = 0;

			// For EmailServer data source, open folder first
			{
				dataInputReader.openFolder(new EmailServerDataSourceFolder("Inbox"));
			}

			// Fetch all new items
			List<DataInputRecord> dataInputRecordList = dataInputReader.getNewItems();

			logger.info("Total number of records fetched = {}", dataInputRecordList.size());

			// Filter for test email records created as part of test initialization
			List<DataInputRecord> emailDataInputRecordList = dataInputRecordList.stream()
					.filter(r -> r.getEmailRecord() != null
							&& r.getEmailRecord().getEmailSubject().contains(TEST_RECORDS_IDENTIFIER_KEYWORD))
					.collect(Collectors.toList());
			noOfTestRecordsFetched = emailDataInputRecordList.size();
			logger.info("Total number of test email records fetched = {}", noOfTestRecordsFetched);
			assertTrue("No. of test email records created and fetched should match (" + noOfTestRecordsFetched + "<>"
					+ TEST_RECORDS_CREATE_COUNT + ")", noOfTestRecordsFetched == TEST_RECORDS_CREATE_COUNT);

			// Filter for test file records created as part of test initialization
			List<DataInputRecord> fileDataInputRecordList = dataInputRecordList.stream()
					.filter(r -> r.getFileRecord() != null
							&& r.getFileRecord().getFileName().contains(TEST_RECORDS_IDENTIFIER_KEYWORD))
					.collect(Collectors.toList());
			noOfTestRecordsFetched = fileDataInputRecordList.size();
			logger.info("Total number of test file records fetched = {}", fileDataInputRecordList.size());
			assertTrue("No. of test file records created and fetched should match (" + noOfTestRecordsFetched + "<>"
					+ TEST_RECORDS_CREATE_COUNT + ")", noOfTestRecordsFetched == TEST_RECORDS_CREATE_COUNT);

			// Update email records as READ
			for (DataInputRecord dataInputRecord : emailDataInputRecordList) {
				dataInputReader.updateItemAsRead(dataInputRecord.getDataInputRecordId());
				dataInputReader.deleteSavedAttachments(dataInputRecord.getDataInputRecordId());
			}

			// Update file records as READ
			for (DataInputRecord dataInputRecord : fileDataInputRecordList) {
				dataInputReader.updateItemAsRead(dataInputRecord.getDataInputRecordId());
			}

		} catch (Exception e) {
			logger.error("Error while reading files", e);
		}
	}

	@Test
	public void testScenario02GetEmailRecordFromFileSystemAndMarkAsRead() {
		try {
			boolean emailInMailServer, emailInFileSystem, nonEmailInFileSystem;
			emailInMailServer = false;
			emailInFileSystem = true;
			nonEmailInFileSystem = false;
			createTestDataForReader(emailInMailServer, emailInFileSystem, nonEmailInFileSystem);

			int noOfTestRecordsFetched = 0;

			// For EmailServer data source, open folder first
			{
				dataInputReader.openFolder(new EmailServerDataSourceFolder("Inbox"));
			}

			// Fetch all new items
			List<DataInputRecord> dataInputRecordList = dataInputReader.getNewItems();

			logger.info("Total number of records fetched = {}", dataInputRecordList.size());

			// Filter for test email records created as part of test initialization
			List<DataInputRecord> emailDataInputRecordList = dataInputRecordList.stream()
					.filter(r -> r.getEmailRecord() != null).collect(Collectors.toList());
			noOfTestRecordsFetched = emailDataInputRecordList.size();
			logger.info("Total number of test email records fetched = {}", noOfTestRecordsFetched);

			// Update email records as READ
			for (DataInputRecord dataInputRecord : emailDataInputRecordList) {
				dataInputReader.updateItemAsRead(dataInputRecord.getDataInputRecordId());
				dataInputReader.deleteSavedAttachments(dataInputRecord.getDataInputRecordId());
			}

		} catch (Exception e) {
			logger.error("Error while reading files", e);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		try {
			List<Boolean> resultList = dataInputReader.disconnect();
			long trueCount = resultList.stream().filter(x -> x == true).count();
			assertTrue(resultList.size() == trueCount);
		} catch (Exception e) {
			logger.error("Error while disconnecting dataInputReader", e);
			fail("Some error occurred");
		}
	}
}
