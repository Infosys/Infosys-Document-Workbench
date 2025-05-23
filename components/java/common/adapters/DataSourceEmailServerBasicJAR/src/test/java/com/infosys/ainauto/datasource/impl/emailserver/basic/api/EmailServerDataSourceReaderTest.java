/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.Message;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.impl.emailserver.basic.BaseTest;
import com.infosys.ainauto.datasource.impl.emailserver.basic.common.TestHelper;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceFolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmailServerDataSourceReaderTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceReaderTest.class);
	private static IDataSourceReader dataSourceReader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		emailServerDataSourceReaderConfig = TestHelper.getEmailServerDataSourceReaderConfig("emailServerDataSourceTest.properties");
		createTestDataForReader();
		
		try {
			dataSourceReader = TestHelper.getDataSourceReader(emailServerDataSourceReaderConfig);
			boolean status = dataSourceReader.connect();
			assertTrue(status == true);
		} catch (Exception e) {
			logger.error("Error while getting DataSourceReader", e);
			fail("Some error occurred");
		}
		
		logger.debug("Setup before class completed");
	}

	@Test
	public void testScenario01GetNewRecordsAndMarkAsRead() {
		try {
			int noOfTestRecordsFetched = 0;
			dataSourceReader.openFolder(new EmailServerDataSourceFolder("Inbox"));
			List<DataSourceRecord> dataSourceRecordList = dataSourceReader.getNewItems();
			logger.info("Total number of records fetched = {}", dataSourceRecordList.size());

			// Filter for test email records created as part of test initialization
			List<EmailServerDataSourceRecord> emailServerDataSourceRecordList = dataSourceRecordList.stream()
					.map(r -> (EmailServerDataSourceRecord) r).collect(Collectors.toList());

			// Filter for test email records
			{
				Iterator<EmailServerDataSourceRecord> iterator = emailServerDataSourceRecordList.iterator();
				while (iterator.hasNext()) {
					EmailServerDataSourceRecord record = iterator.next();
					try {
						if (!((Message) record.getMessage()).getSubject().contains(TEST_RECORDS_IDENTIFIER_KEYWORD)) {
							iterator.remove();
						}
					} catch (Exception ex) {
						String message = "Error occurred while iterating through records";
						logger.error(message, ex);
						throw new Exception(message, ex);
					}
				}
			}

			noOfTestRecordsFetched = emailServerDataSourceRecordList.size();

			logger.info("Total number of test email records fetched = {}", noOfTestRecordsFetched);
			assertTrue("No. of test email records created and fetched should match",
					noOfTestRecordsFetched == TEST_RECORDS_CREATE_COUNT);

			for (EmailServerDataSourceRecord dataSourceRecord : emailServerDataSourceRecordList) {
				dataSourceReader.updateItemAsRead(dataSourceRecord.getDataSourceRecordId());
			}
		} catch (Exception e) {
			logger.error("Error while reading files", e);
			fail("Some error occurred");
		}
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		try {
			boolean status = dataSourceReader.disconnect();
			assertTrue(status == true);
		} catch (Exception e) {
			logger.error("Error while disconnecting DataSourceReader", e);
			fail("Some error occurred");
		}
	}

}
