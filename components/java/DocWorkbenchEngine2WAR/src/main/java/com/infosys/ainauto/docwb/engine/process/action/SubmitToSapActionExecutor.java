/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.stereotype.ActionExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.ActionExecutorBase;
import com.infosys.ainauto.docwb.engine.core.template.action.IActionExecutorListener;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
@ActionExecutor(title = "Submit to SAP", propertiesFile = "customization.properties")
public class SubmitToSapActionExecutor extends ActionExecutorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IActionService actionService;

	private static Logger logger = LoggerFactory.getLogger(SubmitToSapActionExecutor.class);

	private static final String ACTION_RESULT_SUBMITTED = "Submitted to SAP successfully";

	private List<String> queueNameCdeList;
	private int actionNameCde;

	@PostConstruct
	private void init() {
		actionService = docWbApiClient.getActionService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		queueNameCdeList = Arrays.asList(properties.getProperty("action.submit-to-sap.queue.name.cde").split(","));
		actionNameCde = Integer.parseInt(properties.getProperty("action.submit-to-sap.action.name.cde"));
		return true;
	}

	@Override
	protected List<List<DocActionData>> getActions() throws Exception {
		List<List<DocActionData>> docActionDataListOfList = new ArrayList<>();
		for (String queueNameCde : queueNameCdeList) {
			docActionDataListOfList.add(actionService.getActionList(actionNameCde, EnumTaskStatus.YET_TO_START,
					Integer.valueOf(queueNameCde), EnumEventOperator.EQUALS));
		}
		return docActionDataListOfList;
	}

	@Override
	protected void executeAction(ActionParamAttrMappingData actionParamAttrMappingData,
			IActionExecutorListener actionExecutorListener) throws Exception {
		logger.info("Begin executing action with DocActionRelId - " + actionParamAttrMappingData.getDocActionRelId());
		String result = ACTION_RESULT_SUBMITTED;
		Exception exception = null;
		logger.info("Leave executing action with DocActionRelId - " + actionParamAttrMappingData.getDocActionRelId()
				+ " .Result is " + result);
		actionExecutorListener.onActionExecutionComplete(exception, result);
	}
}
