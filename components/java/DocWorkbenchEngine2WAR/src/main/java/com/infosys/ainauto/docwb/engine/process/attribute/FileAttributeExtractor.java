/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.attribute;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.stereotype.AttributeExtractor;
import com.infosys.ainauto.docwb.engine.core.template.attribute.AttributeExtractorBase;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;

@Component
@AttributeExtractor(title = "Extract Attributes", propertiesFile = "customization.properties")
public class FileAttributeExtractor extends AttributeExtractorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IDocumentService documentService;

	private List<String> queueNameCdes;

	@PostConstruct
	private void init() throws Exception {
		documentService = docWbApiClient.getDocumentService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		this.queueNameCdes = Arrays.asList(properties.getProperty("attribute-extractor.queue.name.cde").split(","));
		return true;
	}

	@Override
	protected List<List<DocumentData>> getDocuments() throws Exception {
		EnumEventType higestEventType = EnumEventType.DOCUMENT_CREATED;
		EnumEventOperator highestEventTypeOperator = EnumEventOperator.EQUALS;
		String attrNameCdes = "";
		List<List<DocumentData>> documentDataListOfList = documentService.getDocumentList(higestEventType,
				highestEventTypeOperator, null, null, 0, queueNameCdes, attrNameCdes);
		return documentDataListOfList;
	}
}
