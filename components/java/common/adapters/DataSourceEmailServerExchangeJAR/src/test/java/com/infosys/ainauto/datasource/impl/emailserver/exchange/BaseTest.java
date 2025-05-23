/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.exchange;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
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

	@AfterClass
	public static void tearDown() throws Exception {
		logger.debug("Setup after class completed");
	}
}
