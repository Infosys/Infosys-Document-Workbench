/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.api;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptExecutorServiceTest002 {

	private final static Logger logger = LoggerFactory.getLogger(ScriptExecutorServiceTest002.class);
	private static IScriptExecutorService scriptExecutorService = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ScriptExecutorFactory scriptExecutorFactory = new ScriptExecutorFactory(
				"http://localhost:8111/scriptexecutionmanager/WEMScriptExecService.svc");
		scriptExecutorService = scriptExecutorFactory.getScriptExecutorService();
		logger.debug("Created script executor service instance");
	}

	@Test
	public void testGetTransactionStatusForInvalidRequest() {
		String transactionId = "";
		ScriptResponseData scriptResponseData = scriptExecutorService.getTransactionStatusAndResult(transactionId);

		Assert.assertNull(scriptResponseData);
	}

	@Test
	public void testGetTransactionStatusForValidRequest() {
		String transactionId = "0868466d-78d3-4e6b-bb57-37baae5211c0";
		ScriptResponseData scriptResponseData = scriptExecutorService.getTransactionStatusAndResult(transactionId);
		
		Assert.assertNotNull(scriptResponseData);
		Assert.assertTrue(scriptResponseData.getScriptResponseItemDataList().size() == 1);
		ScriptResponseItemData scriptResponseItemData = scriptResponseData.getScriptResponseItemDataList().get(0);
		
		Assert.assertTrue(scriptResponseItemData.getOutParameters().size()>0);
		
		Assert.assertEquals("default", scriptResponseItemData.getComputerName());
		Assert.assertEquals("", scriptResponseItemData.getErrorMessage());
		Assert.assertEquals("SUCCESS", scriptResponseItemData.getStatus());
		Assert.assertTrue(
				scriptResponseItemData.getTransactionId().length() == "0868466d-78d3-4e6b-bb57-37baae5211c0".length());

		
	}
	

	@AfterClass
	public static void tearDown() throws Exception {
	}
}
