/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.ActionData;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.RecommendedActionData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public interface IActionService {

	List<DocActionData> getActionListForDoc(int actionNameCde, EnumTaskStatus enumTaskStatus, int queueNameCde, EnumEventOperator taskStatusOperator, long docId);
	
	/**
	 * Method gets a list of actions for the given queuenameCde.
	 * 
	 */
	List<DocActionData> getActionList(int actionNameCde, EnumTaskStatus enumTaskStatus, int queueNameCde, EnumEventOperator taskStatusOperator);
	
	/**
	 * Method gets a list of action list for the given queuenameCdes list, used for
	 * creating multiple threads to get the list from api.
	 * 
	 */
	List<List<DocActionData>> getActionList(int actionNameCde, EnumTaskStatus enumTaskStatus, List<String> queueNameCdes, EnumEventOperator taskStatusOperator);

	int updateAction(ActionParamAttrMappingData actionData, EnumTaskStatus EnumTaskStatus);

    ActionData getActionData(int categoryNameCde,long docId);
    
    void addAction(DocActionData docActionData);

	RecommendedActionData getRecommendation(long docId);
}
