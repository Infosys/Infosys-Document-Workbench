/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.scriptexecutor.data.ParameterData;
import com.infosys.ainauto.scriptexecutor.data.ScriptIdentifierData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptExecutorServiceTest001 {

	private final static Logger logger = LoggerFactory.getLogger(ScriptExecutorServiceTest001.class);
	private static IScriptExecutorService scriptExecutorService = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ScriptExecutorFactory scriptExecutorFactory = new ScriptExecutorFactory(
				"http://localhost:8111/scriptexecutionmanager/WEMScriptExecService.svc");
		scriptExecutorService = scriptExecutorFactory.getScriptExecutorService();
		logger.debug("Created script executor service instance");
	}

	@Test
	public void testInitiateExeuctionForInvalidRequest() {
		ScriptIdentifierData scriptIdentifierData = new ScriptIdentifierData();
		ScriptResponseData scriptResponseData = scriptExecutorService.initiateExecution(scriptIdentifierData);
		Assert.assertTrue(scriptResponseData.getScriptResponseItemDataList().size() == 1);

		String errorMessage = scriptResponseData.getScriptResponseItemDataList().get(0).getErrorMessage();
		Assert.assertEquals(errorMessage,
				"Invalid Parameters passed to check role access for user. Please try again with valid parameters.");
	}

	@Test
	public void testInitiateExeuctionForValidRequest() {
		logger.debug("Testing script execution service");
		ScriptIdentifierData scriptIdentifierData = new ScriptIdentifierData();
		List<ParameterData> parameterDataList = new ArrayList<ParameterData>();
		ParameterData parameterData = new ParameterData();
		parameterData.setParameterName("InvoiceNumber");
		parameterData.setParameterValue("23682374");
		parameterDataList.add(parameterData);
		scriptIdentifierData.setParameterDataList(parameterDataList);
		scriptIdentifierData.setCategoryId(156);
		scriptIdentifierData.setCompanyId(2);
		scriptIdentifierData.setExecutionMode(7);
		scriptIdentifierData.setReferenceKey("BP");
		scriptIdentifierData.setRemoteServerNames("default");
		scriptIdentifierData.setScriptId(100);
		scriptIdentifierData.setScriptName("GetInvoiceStatus");
		scriptIdentifierData.setResponseNotificationCallbackURL(
				"http://localhost:8088/docwbengine/api/v1/callback/scriptexecutor/status");
		ScriptResponseData scriptResponseData = scriptExecutorService.initiateExecution(scriptIdentifierData);

		Assert.assertTrue(scriptResponseData.getScriptResponseItemDataList().size() == 1);
		ScriptResponseItemData scriptResponseItemData = scriptResponseData.getScriptResponseItemDataList().get(0);
		Assert.assertEquals("default", scriptResponseItemData.getComputerName());
		Assert.assertEquals("", scriptResponseItemData.getErrorMessage());
		Assert.assertEquals("QUEUED", scriptResponseItemData.getStatus());
		Assert.assertTrue(
				scriptResponseItemData.getTransactionId().length() == "0868466d-78d3-4e6b-bb57-37baae5211c0".length());

	}

	@AfterClass
	public static void tearDown() throws Exception {
	}
}
