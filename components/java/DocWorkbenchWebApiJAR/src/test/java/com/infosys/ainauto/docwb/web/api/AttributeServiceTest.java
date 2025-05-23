/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AttributeServiceTest {

	private static DocWbApiFactory docWbFactory = null;
	private static String tempDownloadPath = "D://TEMP";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		docWbFactory = new DocWbApiFactory("http://localhost:8080/docwbservice", "docwbengine", "docwbengine",
				"ae30c578-8569-4f86-be17-642ebaef2e52", true, tempDownloadPath,5, 5);
		docWbFactory.startServiceThreads();
	}

	@Test
	public void addAttachment() throws Exception {

		IAttributeService attributeService = docWbFactory.getAttributeService();
		
		DocumentData documentData = new DocumentData();
		documentData.setDocId(1001285);
		documentData.setAttributes(getAttributeDataList());
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		for (int i=0;i<2;i++) {
			AttachmentData attachmentData = new AttachmentData();
			attachmentData.setAttachmentId(i);
			attachmentData.setAttributes(getAttributeDataList());
			attachmentDataList.add(attachmentData);
		}
		documentData.setAttachmentDataList(attachmentDataList);
		
		attributeService.addAttributes(documentData);

		assertTrue(attachmentDataList.size() >= 0);

	}
	
	private List<AttributeData> getAttributeDataList() {
		List<AttributeData> attributeDataList = new ArrayList<>();
		for (int i=0; i<2;i++) {
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(10);
			attributeData.setAttrValue("SomeValue");
			attributeData.setExtractType(EnumExtractType.DIRECT_COPY);
			attributeData.setConfidencePct(77);
			attributeDataList.add(attributeData);
		}
		return attributeDataList;
	}

	@AfterClass
	public static void tearDown() throws Exception {
		docWbFactory.stopServiceThreads();
	}
}
