/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DocumentServiceTest {

	private static DocWbApiFactory docWbFactory = null;
	private static String tempDownloadPath = "D://TEMP";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		docWbFactory = new DocWbApiFactory("http://localhost:8080/docwbservice", "docwbengine", "docwbengine",
				"ae30c578-8569-4f86-be17-642ebaef2e52", true, tempDownloadPath, 5, 6);
		docWbFactory.startServiceThreads();
	}

	@Test
	public void getDocList() {
		IDocumentService documentService = docWbFactory.getDocumentService();
		EnumEventType higestEventType = EnumEventType.CASE_OPENED;
		EnumEventOperator highestEventTypeOperator = EnumEventOperator.EQUALS;
		String attrNameCdes = "";
		List<String> queueNameCdes = Arrays.asList("11", "12", "13", "14", "15", "107");
		List<List<DocumentData>> documentDataListOfList = documentService.getDocumentList(higestEventType,
				highestEventTypeOperator, null, null, 0, queueNameCdes, attrNameCdes);
		System.out.println(documentDataListOfList.size());
		documentDataListOfList.forEach(action -> System.out.println(action.size()));
		System.out.println(documentDataListOfList.toString());
	}

	@Test
	public void assignCase() {
		IDocumentService documentService = docWbFactory.getDocumentService();
		long docId = 1001316;
		long appUserID = 1012; // John Doe
		
		try {
			boolean result = documentService.assignCase(docId, appUserID);
			assertTrue("Case should be assigned", result == true);
		} catch (Exception ex) {
			String expectedErrorMsg = "Previous User data sent is not matching";
			assertTrue("Case should not be assigned with valid error", ex.getMessage().contains(expectedErrorMsg));
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		docWbFactory.stopServiceThreads();
	}
}
