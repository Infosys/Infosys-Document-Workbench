/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.azure.cognitive.vision;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@RunWith(JUnit4.class)
public class FormRecognizerServiceTest {
	IFormRecognizerService formRecognizerService;
	ExtractorConfigData extractorConfigData;
	AttachmentData attachmentData;

	@Before
	public void setUp() throws Exception {
		Environment env = new StandardEnvironment();
		formRecognizerService = new FormRecognizerService(env);
		attachmentData = new AttachmentData();
		attachmentData.setPhysicalName("Fax2test.jpg");
		attachmentData.setPhysicalPath(getAbsolutePathOfResourceFile("Fax2test.jpg"));
		String fileContent = readFile(getAbsolutePathOfResourceFile("attributeExtractorAzureApiMappingConfig.json"));
		ObjectMapper mapper = new ObjectMapper();
		ExtractorData extractorData = mapper.readValue(fileContent, ExtractorData.class);
		extractorConfigData = AttributeDataHelper.getConfiguredExtractorApiDataByAttrNameCde(extractorData,
				EnumSystemAttributeName.DOCUMENT_TYPE.getCde(), getDocumentData());

	}

	@Test
	public void testPostAnalyzeForm() throws Exception {
		String operationLocationUrl = formRecognizerService.postAnalyzeForm(extractorConfigData, attachmentData);
		if (!operationLocationUrl.isEmpty()) {
			formRecognizerService.getAnalyzeFormResult(extractorConfigData, operationLocationUrl);
		}
	}

	private DocumentData getDocumentData() {
		DocumentData documentData = new DocumentData();
		AttachmentData attachmentData = new AttachmentData();
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(31).setAttrValue("Receipt");
		List<AttributeData> attrDataList = new ArrayList<AttributeData>();
		attrDataList.add(attributeData);
		attachmentData.setAttributes(attrDataList);
		List<AttachmentData> attachDataList = new ArrayList<>();
		attachDataList.add(attachmentData);
		documentData.setAttachmentDataList(attachDataList);
		return documentData;
	}

	private String readFile(String fileName) {
		StringBuilder out = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			stream.forEach(line -> out.append(line).append("\n"));

		} catch (IOException e) {
		}
		return out.toString();
	}

	public static String getAbsolutePathOfResourceFile(String resourceFileName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource(resourceFileName);
		File file = new File(resource.getPath());
		return file.getAbsolutePath();
	}
}
