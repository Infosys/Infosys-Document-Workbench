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
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
@AttributeExtractor(title = "Extract Attributes", propertiesFile = "customization.properties")
public class DocAttributeExtractor extends AttributeExtractorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IDocumentService documentService;

	private List<String> queueNameCdeList;

	@PostConstruct
	private void init() {
		documentService = docWbApiClient.getDocumentService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		this.queueNameCdeList = Arrays.asList(properties.getProperty("attribute-extractor.queue.name.cde").split(","));
		return true;
	}

	@Override
	protected List<List<DocumentData>> getDocuments() throws Exception {
		// Get documents on which highest event logged is document created
		EnumEventType higestEventType = EnumEventType.DOCUMENT_CREATED;
		EnumEventOperator highestEventTypeOperator = EnumEventOperator.EQUALS;
		String attrNameCdes = EnumSystemAttributeName.SUBJECT.getCde() + "," + EnumSystemAttributeName.CONTENT.getCde() + ","
				+ EnumSystemAttributeName.CATEGORY.getCde() + "," + EnumSystemAttributeName.FROM_ID.getCde();
		List<List<DocumentData>> documentDataListOfList = documentService.getDocumentList(higestEventType, highestEventTypeOperator,
				null, null, 0, queueNameCdeList, attrNameCdes);
		return documentDataListOfList;
	}

}
