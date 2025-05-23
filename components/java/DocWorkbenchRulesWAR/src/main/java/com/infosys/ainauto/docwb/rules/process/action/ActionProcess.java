/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.rules.process.action;

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

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.docwb.rules.common.DocWbConstants;
import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.common.KieHelper;
import com.infosys.ainauto.docwb.rules.common.SerializationHelper;
import com.infosys.ainauto.docwb.rules.common.TenantResourceHelper;
import com.infosys.ainauto.docwb.rules.model.api.action.GetRecommendedActionResData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.model.domain.InputOutputWrapperData;
import com.infosys.ainauto.docwb.rules.model.domain.RecommendedActionData;

@Component
public class ActionProcess implements IActionProcess {

	private static final Logger logger = LoggerFactory.getLogger(ActionProcess.class);
	private static Map<String, InternalKnowledgeBase> fileIkbMap = new HashMap<>();
	private static Map<String, Date> fileLmdMap = new HashMap<>();

	@PostConstruct
	private void init() {
		long startTime = System.nanoTime();
		List<String> ruleFileLocationList = new ArrayList<>();
		// Load files to Map to avoiding lazy loading when first API call is made post
		// restart
		try {
			ruleFileLocationList = FileUtility.getResourceFilesInPath(DocWbConstants.RULES_FOLDER_ACTION,
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
	public List<GetRecommendedActionResData> getRecommendedAction(String tenantId, DocumentData documentData)
			throws DocWbRulesException {

		List<GetRecommendedActionResData> getRecommendationResDataList = new ArrayList<>();

		String ruleFileLocation = TenantResourceHelper.validateAndReturnRuleFileLocation(
				DocWbConstants.RULES_FOLDER_ACTION, tenantId,
				DocWbConstants.RULE_FILE_NAME_RECOMMENDED_ACTION);

		try {
			InternalKnowledgeBase kbase = null;
			Date lastModifiedDtm = FileUtility.getLastModifiedDtm(ruleFileLocation);

			// Should be done only once per class instance and not per request
			synchronized (this) {
				// Check if LastModifiedDateMap has entry for ruleFileLocation
				// OR if file was modified after last check
				if (!fileLmdMap.containsKey(ruleFileLocation)
						|| fileLmdMap.get(ruleFileLocation).before(lastModifiedDtm)) {
					kbase = (InternalKnowledgeBase) SerializationHelper
							.serializeObject(KieHelper.loadKnowledgeBase(ruleFileLocation));
					fileLmdMap.put(ruleFileLocation, lastModifiedDtm);
					fileIkbMap.put(ruleFileLocation, kbase);
				} else {
					kbase = fileIkbMap.get(ruleFileLocation);
				}
			}

			InputOutputWrapperData<DocumentData, List<RecommendedActionData>> inputOutputWrapperData = new InputOutputWrapperData<>();
			inputOutputWrapperData.setInputData(documentData);

			StatelessKieSession statelessKieSession = KieHelper.createStatelessKnowledgeSession(kbase);

			statelessKieSession.execute(inputOutputWrapperData);
			if (inputOutputWrapperData != null) {
				for (RecommendedActionData recommendedActionData : inputOutputWrapperData.getOutputData()) {
					GetRecommendedActionResData getRecommendedActionResData = new GetRecommendedActionResData();
					getRecommendedActionResData.setActionNameCde(recommendedActionData.getActionNameCde());
					getRecommendedActionResData.setConfidencePct(recommendedActionData.getConfidencePct());
					getRecommendedActionResData.setRecommendedPct(recommendedActionData.getRecommendedPct());
					getRecommendationResDataList.add(getRecommendedActionResData);
				}
			}

		} catch (Exception e) {
			logger.error("Error occurred in Get Recommended Action rule", e);
			throw new DocWbRulesException("Error occurred in Get Recommended Action rule", e);
		}
		return getRecommendationResDataList;
	}

}
