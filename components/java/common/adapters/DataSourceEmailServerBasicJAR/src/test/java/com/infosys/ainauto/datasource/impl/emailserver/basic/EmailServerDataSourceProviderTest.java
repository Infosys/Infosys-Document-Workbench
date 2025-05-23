/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.impl.emailserver.basic.common.TestHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmailServerDataSourceProviderTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceProviderTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		emailServerDataSourceReaderConfig = TestHelper.getEmailServerDataSourceReaderConfig("emailServerDataSourceTest.properties");
		logger.debug("Setup before class completed");
	}
	
	@Test
	public void testScenario01GetDataSourceReaderFromProvider() {
		boolean isNoException = true;
		IDataSourceReader dataSourceReader = null;
		try {
			dataSourceReader = TestHelper.getDataSourceReader(emailServerDataSourceReaderConfig);
		} catch (Exception e) {
			isNoException = false;
			logger.error("Error while getting FileReader", e);
		}
		assertTrue("Provider did not throw any exception", isNoException == true);
		assertTrue("FileReader is not null", dataSourceReader != null);
	}

	@AfterClass
	public static void tearDown() throws Exception {

	}
}
