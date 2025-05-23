
/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.action;

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
@ActionExecutor(title = "Get Info- Billing Address", propertiesFile = "customization.properties")
public class GetInfoBillingAddressActionExecutor extends ActionExecutorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IActionService actionService;

	private static Logger logger = LoggerFactory.getLogger(GetInfoBillingAddressActionExecutor.class);

	private List<String> queueNameCdeList;
	private int actionNameCde;

	@PostConstruct
	private void init() {
		actionService = docWbApiClient.getActionService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		queueNameCdeList = Arrays.asList(properties.getProperty("action.get-info-bill-address.queue.name.cde").split(","));
		actionNameCde = Integer.parseInt(properties.getProperty("action.get-info-bill-address.action.name.cde"));
		return true;
	}

	@Override
	protected List<List<DocActionData>> getActions() throws Exception {
		List<List<DocActionData>> docActionListOfList = actionService.getActionList(actionNameCde,
				EnumTaskStatus.YET_TO_START, queueNameCdeList, EnumEventOperator.EQUALS);
		return docActionListOfList;
	}

	@Override
	protected void executeAction(ActionParamAttrMappingData actionParamAttrMappingData,
			IActionExecutorListener actionExecutorListener) throws Exception {
		logger.info("Begin executing action with DocActionRelId - " + actionParamAttrMappingData.getDocActionRelId());
		String result = "";
		Exception exception = null;
		result=	"1.	Sign in with your User ID and password.\r\n" + 
				"2.	Select View Profile, found in the top navigation area.\r\n" + 
				"3.	Select Update billing address.\r\n" + 
				"4.	Follow the online instructions.\r\n";
		
		logger.info("Leave executing action with DocActionRelId - " + actionParamAttrMappingData.getDocActionRelId()
				+ " .Result is " + result);

		actionExecutorListener.onActionExecutionComplete(exception, result);
	}

}
