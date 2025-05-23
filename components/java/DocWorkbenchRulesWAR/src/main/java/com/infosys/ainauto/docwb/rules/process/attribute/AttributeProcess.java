/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.process.attribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.rules.common.DocWbConstants;
import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.common.KieHelper;
import com.infosys.ainauto.docwb.rules.common.SerializationHelper;
import com.infosys.ainauto.docwb.rules.common.TenantResourceHelper;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.model.domain.InputOutputWrapperData;
import com.infosys.ainauto.docwb.rules.process.template.TemplateProcess;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

@Component
public class AttributeProcess implements IAttributeProcess {
	private static final Logger logger = LoggerFactory.getLogger(TemplateProcess.class);
	private static Map<String, InternalKnowledgeBase> fileIkbMap = new HashMap<>();
	private static Map<String, Date> fileLmdMap = new HashMap<>();

	@PostConstruct
	private void init() {
		long startTime = System.nanoTime();
		List<String> ruleFileLocationList = new ArrayList<>();
		// Load files to Map to avoiding lazy loading when first API call is made post
		// restart
		try {
			ruleFileLocationList = FileUtility.getResourceFilesInPath(DocWbConstants.RULES_FOLDER_ATTRIBUTE,
					DocWbConstants.FILE_EXTENSION_RULE, true);
			Date lastModifiedDtm = null;
			InternalKnowledgeBase kbase = null;
			for (String ruleFileLocation : ruleFileLocationList) {
				lastModifiedDtm = FileUtility.getLastModifiedDtm(ruleFileLocation);
				kbase = (InternalKnowledgeBase) SerializationHelper
						.serializeObject(KieHelper.loadKnowledgeBase(ruleFileLocation));
				fileLmdMap.put(ruleFileLocation, lastModifiedDtm);
				fileIkbMap.put(ruleFileLocation, kbase);
			}

		} catch (Exception e) {
			logger.error("Error occurred in init", e);
		} finally {
			logger.info("Loaded {} DRL files in {} sec(s)", ruleFileLocationList.size(),
					(System.nanoTime() - startTime) / 1000000000.0);
		}
	}

	@Override
	public DocumentData getAttributesNotification(String tenantId, DocumentData documentData)
			throws DocWbRulesException {

		if (!ListUtility.hasValue(documentData.getAttributes()) && !ListUtility.hasValue(documentData.getAttachments())) {
			logger.error("Invalid Request format");
			throw new DocWbRulesException(EnumApiResponseCde.INVALID_REQUEST,
					EnumApiResponseCde.INVALID_REQUEST.getMessageValue() + " : Empty List");
		}

		String ruleFileLocation = TenantResourceHelper.validateAndReturnRuleFileLocation(
				DocWbConstants.RULES_FOLDER_ATTRIBUTE, tenantId, DocWbConstants.RULE_FILE_NAME_ATTRIBUTE_NOTIFICATION);

		DocumentData responseData = null;
		try {
			InternalKnowledgeBase attributeBase = null;
			Date ruleLastModifiedDtm = FileUtility.getLastModifiedDtm(ruleFileLocation);
			// Should be done only once per class instance and not per request
			synchronized (this) {
				// Check if LastModifiedDateMap has entry for ruleFileLocation
				// OR if file was modified after last check

				if (!fileLmdMap.containsKey(ruleFileLocation)
						|| fileLmdMap.get(ruleFileLocation).before(ruleLastModifiedDtm)) {
					attributeBase = (InternalKnowledgeBase) SerializationHelper
							.serializeObject(KieHelper.loadKnowledgeBase(ruleFileLocation));
					fileLmdMap.put(ruleFileLocation, ruleLastModifiedDtm);
					fileIkbMap.put(ruleFileLocation, attributeBase);
				} else {
					attributeBase = fileIkbMap.get(ruleFileLocation);
				}
			}

			InputOutputWrapperData<DocumentData, DocumentData> inputOutputWrapperData = new InputOutputWrapperData<>();
			inputOutputWrapperData.setInputData(documentData);

			StatelessKieSession statelessKieSession = KieHelper.createStatelessKnowledgeSession(attributeBase);

			statelessKieSession.execute(inputOutputWrapperData);

			responseData = inputOutputWrapperData.getOutputData();
		} catch (Exception e) {
			logger.error("Error occurred in getNotification", e);
			throw new DocWbRulesException("Error occurred in getNotification", e);
		}
		return responseData;
	}

	@Override
	public Object getAttributeAttributeMapping(String tenantId) throws DocWbRulesException {
		String jsonFileLocation = TenantResourceHelper
				.validateAndReturnTenantJsonFileLocation(DocWbConstants.ATTRIBUTE_ATTRIBUTE_MAPPING_FOLDER, tenantId);
		return readAttributeJsonFile(jsonFileLocation);
	}

	@Override
	public Object getAttributeSortingKey(String tenantId) throws DocWbRulesException {
		String jsonFileLocation = TenantResourceHelper
				.validateAndReturnTenantJsonFileLocation(DocWbConstants.ATTRIBUTE_SORTING_KEY_FOLDER, tenantId);
		return readAttributeJsonFile(jsonFileLocation);
	}

	private Object readAttributeJsonFile(String fileLocation) throws DocWbRulesException {
		String fileContent = "";
		Object jsonObj = null;
		try {
			fileContent = FileUtility.readResourceFile(fileLocation);
			ObjectMapper mapper = new ObjectMapper();
			jsonObj = mapper.readValue(fileContent, Object.class);
		} catch (Exception e) {
			throw new DocWbRulesException("Error occurred in readAttributeConfigFile", e);
		}
		return jsonObj;
	}
}
