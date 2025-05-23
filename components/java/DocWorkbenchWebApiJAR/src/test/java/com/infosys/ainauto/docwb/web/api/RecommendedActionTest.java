/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.docwb.web.data.RecommendedActionData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecommendedActionTest {

	private static DocWbApiFactory docWbFactory = null;
	private static String tempDownloadPath = "D://TEMP";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		docWbFactory = new DocWbApiFactory("http://localhost:8080/docwbservice", "docwbengine", "docwbengine",
				"ae30c578-8569-4f86-be17-642ebaef2e52", true, tempDownloadPath,5, 5);
		docWbFactory.startServiceThreads();
	}

	@Test
	public void testRecommenedationService() {
		IActionService actionService = docWbFactory.getActionService();
		RecommendedActionData recommendedActionData = actionService.getRecommendation(1000883);
		Assert.assertNotNull(recommendedActionData);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		docWbFactory.stopServiceThreads();
	}
}
