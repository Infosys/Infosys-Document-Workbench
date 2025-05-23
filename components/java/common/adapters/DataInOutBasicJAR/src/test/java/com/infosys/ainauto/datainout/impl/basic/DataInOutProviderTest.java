/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.api.IDataOutputWriter;
import com.infosys.ainauto.datainout.impl.basic.common.TestHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataInOutProviderTest extends BaseTest {

	private final static Logger logger = LoggerFactory.getLogger(DataInOutProviderTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataInputConfig = TestHelper.getDataInputConfig("basicDataInputTest.properties");
		dataOutputConfig = TestHelper.getDataOutputConfig("basicDataOutputTest.properties");
		logger.debug("Setup before class completed");
	}
	
	@Test
	public void testScenario01GetInputReaderFromProvider() {
		boolean isNoException = true;
		IDataInputReader dataInputReader = null;
		try {
			dataInputReader = TestHelper.getDataInputReader(dataInputConfig);
		} catch (Exception ex) {
			isNoException = false;
			logger.error("Error while getting FileReader", ex);
		}
		assertTrue("Provider should not throw any exception", isNoException == true);
		assertTrue("DataInputReader should not be null", dataInputReader != null);
	}
	
	@Test
	public void testScenario02GeOutputWriterFromProvider() {
		boolean isNoException = true;
		IDataOutputWriter dataOutputWriter = null;
		try {
			dataOutputWriter = TestHelper.getDataOutputWriter(dataOutputConfig);
		} catch (Exception ex) {
			isNoException = false;
			logger.error("Error while getting FileReader", ex);
		}
		assertTrue("Provider should not throw any exception", isNoException == true);
		assertTrue("DataInputReader should not be null", dataOutputWriter != null);
	}
}
