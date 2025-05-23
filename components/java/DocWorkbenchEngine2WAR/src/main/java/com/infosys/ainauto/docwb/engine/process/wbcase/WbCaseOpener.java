/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.wbcase;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.stereotype.CaseOpener;
import com.infosys.ainauto.docwb.engine.core.template.wbcase.CaseOpenerBase;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
@CaseOpener(title = "Open New Cases", propertiesFile = "customization.properties")
public class WbCaseOpener extends CaseOpenerBase {

	@Autowired
	private DocWbApiClient docWbApiClient;
	private IDocumentService documentService;

	private static Logger logger = LoggerFactory.getLogger(WbCaseOpener.class);

	private List<String> queueNameCdes;

	@PostConstruct
	private void init() {
		documentService = docWbApiClient.getDocumentService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		logger.info("Initialized");
		this.queueNameCdes = Arrays.asList(properties.getProperty("case-opener.queue.name.cde").split(","));
		return true;
	}

	@Override
	protected List<List<DocumentData>> getDocuments() throws Exception {
		// Get documents on which highest event logged is attributes extracted
		EnumEventType higestEventType = EnumEventType.ATTRIBUTES_EXTRACTED;
		EnumEventOperator highestEventTypeOperator = EnumEventOperator.EQUALS;
		List<List<DocumentData>> documentDataListOfList = documentService.getDocumentList(higestEventType,
				highestEventTypeOperator, null, null, 0, queueNameCdes,
				String.valueOf(EnumSystemAttributeName.CATEGORY.getCde()));// 19=Category

		return documentDataListOfList;
	}
}
