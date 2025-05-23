/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.dao.action.IDocActionDataAccess;
import com.infosys.ainauto.docwb.service.dao.doc.IDocDataAccess;
import com.infosys.ainauto.docwb.service.model.api.ActionResData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.ParamAttrResData;
import com.infosys.ainauto.docwb.service.model.api.ParamResData;
import com.infosys.ainauto.docwb.service.model.api.RecommendedActionResData;
import com.infosys.ainauto.docwb.service.model.api.action.GetActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.InsertActionData;
import com.infosys.ainauto.docwb.service.model.api.action.InsertActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.ParamAttrReqData;
import com.infosys.ainauto.docwb.service.model.api.action.UpdateActionData;
import com.infosys.ainauto.docwb.service.model.api.action.UpdateActionReqData;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attribute.IAttributeProcess;
import com.infosys.ainauto.docwb.service.service.ml.IRulesService;

@Component
public class ActionProcess implements IActionProcess {

	private static final String REPLACEMENT_FOR_PIPE = Character.toString((char) 25);

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private IDocActionDataAccess docActionDataAccess;

	@Autowired
	private IDocDataAccess docDataAccess;

	@Autowired
	private IAttributeProcess attributeProcess;

	@Autowired
	private IRulesService rulesService;

	public List<Long> insertActions(List<InsertActionReqData> actionDataList) throws WorkbenchException {
		ActionParamAttrMappingDbData dbData;
		List<Long> docActionRelIdList = new ArrayList<Long>();
		for (InsertActionReqData actionReqData : actionDataList) {
			dbData = new ActionParamAttrMappingDbData();
			BeanUtils.copyProperties(actionReqData, dbData);

			for (InsertActionData actionData : actionReqData.getActionDataList()) {
				BeanUtils.copyProperties(actionData, dbData);
				dbData.setTaskStatusCde(EnumTaskStatus.YET_TO_START.getValue());
				//TODO: check for entity= document then call existing class ;if entity= queue then call queue class
				long docActionRelId = docActionDataAccess.addActionToDocument(dbData);
				if (docActionRelId > 0) {
					docActionRelIdList.add(docActionRelId);
					dbData.setDocActionRelId(docActionRelId);
					List<ParamAttrReqData> reqMappingDataList = actionData.getMappingList();
					List<ActionParamAttrMappingDbData> dbDataList = new ArrayList<ActionParamAttrMappingDbData>();
					for (ParamAttrReqData reqMappingData : reqMappingDataList) {
						dbData = new ActionParamAttrMappingDbData();
						dbData.setDocActionRelId(docActionRelId);
						BeanUtils.copyProperties(reqMappingData, dbData);
						dbDataList.add(dbData);
					}

					if (dbDataList.size() > 0) {
						docActionDataAccess.addActionParamAttrRel(dbDataList);
					}
				}
			}
		}
		return docActionRelIdList;
	}

	public List<ActionResData> getActionMappingList() throws WorkbenchException {
		List<ActionResData> actionResDataList = new ArrayList<ActionResData>();
		List<ActionParamAttrMappingDbData> dbDataList = docActionDataAccess.getActionMappingList();

		Map<Integer, ActionResData> actionResDataMap = new HashMap<Integer, ActionResData>();
		Map<Integer, List<ParamAttrResData>> actionParamMap = new HashMap<Integer, List<ParamAttrResData>>();

		List<Integer> keyList = new ArrayList<Integer>();

		if (ListUtility.hasValue(dbDataList)) {
			ActionResData actionResData = null;
			ParamAttrResData paramAttrResData = null;
			for (ActionParamAttrMappingDbData docAttrWrapperDbData : dbDataList) {
				int actionNameCde = docAttrWrapperDbData.getActionNameCde();
				if (!actionResDataMap.containsKey(actionNameCde)) {
					actionResData = new ActionResData();
					actionResData.setActionNameCde(actionNameCde);
					actionResData.setActionNameTxt(docAttrWrapperDbData.getActionNameTxt());
					actionResData.setCreateByUserLoginId(docAttrWrapperDbData.getCreateByUserLoginId());
					actionResDataMap.put(actionNameCde, actionResData);
					actionParamMap.put(actionNameCde, new ArrayList<ParamAttrResData>());
					// Add action name code to keylist for retrieval later
					keyList.add(actionNameCde);
				}
				paramAttrResData = new ParamAttrResData();
				// Add only if action has a parameter to pass
				if (docAttrWrapperDbData.getParamNameCde() > 0) {
					paramAttrResData.setParamNameCde(docAttrWrapperDbData.getParamNameCde());
					paramAttrResData.setParamNameTxt(docAttrWrapperDbData.getParamNameTxt());
					paramAttrResData.setAttrNameCde(docAttrWrapperDbData.getAttrNameCde());
					paramAttrResData.setAttrNameTxt(docAttrWrapperDbData.getAttrNameTxt());
					actionParamMap.get(actionNameCde).add(paramAttrResData);
				}

			}

			// Loop through list to gather all items to be sent as reply
			for (Integer key : keyList) {
				actionResData = actionResDataMap.get(key);
				if (actionParamMap.containsKey(key)) {
					actionResData.setMappingList(actionParamMap.get(key));
				}
				actionResDataList.add(actionResData);
			}
		}
		return actionResDataList;
	}

	public List<EntityDbData> updateActionTaskList(List<UpdateActionReqData> documentDataList)
			throws WorkbenchException {
		List<EntityDbData> entityDataList = new ArrayList<>();
		for (UpdateActionReqData documentData : documentDataList) {
			List<ActionParamAttrMappingDbData> actionDataList = new ArrayList<ActionParamAttrMappingDbData>();
			ActionParamAttrMappingDbData dbData;
			for (UpdateActionData actionData : documentData.getActionDataList()) {
				dbData = new ActionParamAttrMappingDbData();
				BeanUtils.copyProperties(documentData, dbData);
				BeanUtils.copyProperties(actionData, dbData);
				actionDataList.add(dbData);
			}
			if (ListUtility.hasValue(actionDataList)) {
				entityDataList = docActionDataAccess.updateActionTask(actionDataList);
			}
		}
		return entityDataList;
	}

	public PaginationResData getPaginationForActions(GetActionReqData getActionReqData) throws WorkbenchException {
		long totalCount = 0;
		{
			ActionParamAttrMappingDbData filterData = new ActionParamAttrMappingDbData();
			filterData.setDocId(getActionReqData.getDocId());
			filterData.setPageNumber(getActionReqData.getPageNumber());
			filterData.setActionNameCde(getActionReqData.getActionNameCde());
			filterData.setTaskStatusCde(getActionReqData.getTaskStatusCde());

			// Make the call to Data Layer with inputs provided by caller
			totalCount = docActionDataAccess.getTotalActionCount(filterData, getActionReqData.getQueueNameCde(),
					getActionReqData.getTaskStatusOperator());
		}
		int totalPages = 0;
		int currentPage = 0;
		if (totalCount <= 0) {
			currentPage = 0;
			totalPages = 0;
		} else {
			double total = (totalCount * 1.0) / pageSize;
			totalPages = (int) Math.ceil(total);
			currentPage = getActionReqData.getPageNumber();
			if (totalCount <= pageSize || currentPage < 1) {
				if (currentPage <= totalPages) {
					currentPage = 1;
				}
			}
		}
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setCurrentPageNumber(currentPage);
		paginationResData.setTotalPageCount(totalPages);
		return paginationResData;

	}

	public List<DocumentResData> getActionTaskList(GetActionReqData getActionReqData) throws WorkbenchException {

		List<ActionParamAttrMappingDbData> dbDataList = new ArrayList<ActionParamAttrMappingDbData>();
		{
			ActionParamAttrMappingDbData filterData = new ActionParamAttrMappingDbData();
			filterData.setDocId(getActionReqData.getDocId());
			filterData.setPageNumber(getActionReqData.getPageNumber());
			filterData.setActionNameCde(getActionReqData.getActionNameCde());
			filterData.setTaskStatusCde(getActionReqData.getTaskStatusCde());

			// Make the call to Data Layer with inputs provided by caller
			dbDataList = docActionDataAccess.getActionTaskList(filterData, getActionReqData.getQueueNameCde(),
					getActionReqData.getTaskStatusOperator());
		}
		List<DocumentResData> documentResDataList = new ArrayList<DocumentResData>();

		Map<Long, DocumentResData> documentDataMap = new HashMap<Long, DocumentResData>();
		Map<String, ActionResData> docActionResDataMap = new HashMap<String, ActionResData>();
		Map<String, ParamResData> actParamMap = new HashMap<String, ParamResData>();

		List<Long> docIdList = new ArrayList<Long>();
		List<Long> docActionRelIdList = new ArrayList<Long>();
		List<Long> actionParamAttrRelIdList = new ArrayList<Long>();

		if (ListUtility.hasValue(dbDataList)) {
			DocumentResData documentResData = null;
			ActionResData actionResData = null;
			ParamResData paramResData = null;
			for (ActionParamAttrMappingDbData docAttrWrapperDbData : dbDataList) {
				long docId = docAttrWrapperDbData.getDocId();
				long docActionRelId = docAttrWrapperDbData.getDocActionRelId();
				long actionParamAttrRelId = docAttrWrapperDbData.getActionParamAttrRelId();

				if (!documentDataMap.containsKey(docId)) {
					documentResData = new DocumentResData();
					documentResData.setDocId(docId);
					documentResData.setQueueNameCde(docAttrWrapperDbData.getQueueNameCde());
					// Initialize an empty array list
					documentResData.setActionDataList(new ArrayList<ActionResData>());
					documentDataMap.put(docId, documentResData);
					docIdList.add(docId);
				}

				String combKey2 = String.valueOf(docId) + "-" + String.valueOf(docActionRelId);
				if (!docActionResDataMap.containsKey(combKey2)) {
					actionResData = new ActionResData();
					actionResData.setDocActionRelId(docActionRelId);
					actionResData.setActionNameCde(docAttrWrapperDbData.getActionNameCde());
					actionResData.setTaskTypeCde(docAttrWrapperDbData.getTaskTypeCde());
					actionResData.setActionNameTxt(docAttrWrapperDbData.getActionNameTxt());
					actionResData.setTaskStatusCde(docAttrWrapperDbData.getTaskStatusCde());
					actionResData.setTaskStatusTxt(docAttrWrapperDbData.getTaskStatusTxt());
					actionResData.setSnapShot(docAttrWrapperDbData.getSnapShot());
					if (StringUtility.hasValue(docAttrWrapperDbData.getActionResult())) {
						String result = docAttrWrapperDbData.getActionResult().replace(REPLACEMENT_FOR_PIPE, "|");
						actionResData.setActionResult(result);
					}
					actionResData.setCreateByUserLoginId(docAttrWrapperDbData.getCreateByUserLoginId());
					actionResData.setCreateByUserFullName(docAttrWrapperDbData.getCreateByUserFullName());
					actionResData.setCreateByUserTypeCde(docAttrWrapperDbData.getCreateByUserTypeCde());
					actionResData.setCreateByUserTypeTxt(docAttrWrapperDbData.getCreateByUserTypeTxt());
					actionResData.setCreateDtm(DateUtility.toString(docAttrWrapperDbData.getCreateDtm(),
							WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
					actionResData.setLastModDtm(DateUtility.toString(docAttrWrapperDbData.getLastModDtm(),
							WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
					actionResData.setCreateDtmDuration(DateUtility.getDuration(docAttrWrapperDbData.getCreateDtm()));
					actionResData.setLastModDtmDuration(DateUtility.getDuration(docAttrWrapperDbData.getLastModDtm()));
					docActionResDataMap.put(combKey2, actionResData);
					docActionRelIdList.add(docActionRelId);
				}

				String combKey3 = combKey2 + "-" + String.valueOf(actionParamAttrRelId);
				if (!actParamMap.containsKey(combKey3)) {
					paramResData = new ParamResData();
					paramResData.setParamNameCde(docAttrWrapperDbData.getParamNameCde());
					paramResData.setParamNameTxt(docAttrWrapperDbData.getParamNameTxt());
					paramResData.setParamValue(docAttrWrapperDbData.getParamValue());
					actParamMap.put(combKey3, paramResData);
					actionParamAttrRelIdList.add(actionParamAttrRelId);

				}
			}

			// Loop through list to gather all items to be sent as reply
			for (Long docId : docIdList) {
				documentResData = documentDataMap.get(docId);
				for (Long docActionRelId : docActionRelIdList) {
					String combKey2 = String.valueOf(docId) + "-" + String.valueOf(docActionRelId);
					if (docActionResDataMap.containsKey(combKey2)) {
						actionResData = docActionResDataMap.get(combKey2);
						actionResData.setParamList(new ArrayList<ParamResData>());
						for (Long actionParamAttrRelId : actionParamAttrRelIdList) {
							String combKey3 = combKey2 + "-" + String.valueOf(actionParamAttrRelId);
							if (actParamMap.containsKey(combKey3)) {
								actionResData.getParamList().add(actParamMap.get(combKey3));
							}
						}
						documentResData.getActionDataList().add(actionResData);
					}
				}
				documentResDataList.add(documentResData);
			}
		}
		return documentResDataList;
	}

	public long deleteAction(long docActionRelId) throws WorkbenchException {
		long docActionRelIdOut = docActionDataAccess.deleteActionFromDoc(docActionRelId);
		if (docActionRelIdOut > 0) {
			docActionDataAccess.deleteActionParamAttrRel(docActionRelId);
		}
		return docActionRelIdOut;

	}

	public RecommendedActionResData getRecommendedAction(long docId) throws WorkbenchException {
		int docTypeCde = docDataAccess.getDocumentDetails(docId).getDocTypeCde();
		List<AttributeDbData> attributeDbDataList = null;
		if (docTypeCde == WorkbenchConstants.EMAIL)
			attributeDbDataList = attributeProcess.getDocumentAttributes(docId);
		List<AttributeDbData> attachmentAttributeDbDataList = attributeProcess.getAttachmentAttributes(docId, "",false);

		List<DocumentResData> actionParamAttrMappingDbDataList = new ArrayList<DocumentResData>();
		{
			GetActionReqData getActionReqData = new GetActionReqData();
			getActionReqData.setDocId(docId);
			getActionReqData.setTaskStatusOperator("");
			// Make the call to Data Layer with inputs provided by caller
			actionParamAttrMappingDbDataList = getActionTaskList(getActionReqData);
		}

		// Call rule service to get the recommended action by passing the list of
		// action(s) and
		// attribute(s) on the case
		RecommendedActionResData recommendedActionResData = rulesService.getRecommendedAction(
				SessionHelper.getTenantId(), docId, docTypeCde, actionParamAttrMappingDbDataList, attributeDbDataList,
				attachmentAttributeDbDataList);
		return recommendedActionResData;
	}

	public List<ActionResData> getActionData(int actionNameCde1, long docId) throws WorkbenchException {
		List<ActionResData> actionResDataList = new ArrayList<ActionResData>();
		List<ActionParamAttrMappingDbData> dbDataList = docActionDataAccess.getActionData(actionNameCde1, docId);

		Map<Integer, ActionResData> actionResDataMap = new HashMap<Integer, ActionResData>();
		Map<Integer, List<ParamAttrResData>> actionParamMap = new HashMap<Integer, List<ParamAttrResData>>();
		List<Integer> keyList = new ArrayList<Integer>();

		if (ListUtility.hasValue(dbDataList)) {
			Map<Integer, List<String>> attrValueMap = new HashMap<Integer, List<String>>();
			ActionResData actionResData = null;
			ParamAttrResData paramAttrResData = null;
			List<Integer> paramAddedList = new ArrayList<>();
			for (ActionParamAttrMappingDbData docAttrWrapperDbData : dbDataList) {
				int actionNameCde = docAttrWrapperDbData.getActionNameCde();
				if (!actionResDataMap.containsKey(actionNameCde)) {
					actionResData = new ActionResData();
					actionResData.setActionNameCde(actionNameCde);
					actionResData.setActionNameTxt(docAttrWrapperDbData.getActionNameTxt());
					actionResDataMap.put(actionNameCde, actionResData);
					actionParamMap.put(actionNameCde, new ArrayList<ParamAttrResData>());
					// Add action name code to keylist for retrieval later
					keyList.add(actionNameCde);
				}
				String attrValue = docAttrWrapperDbData.getAttrValue();
				if (docAttrWrapperDbData.getParamNameCde() > 0 && StringUtility.hasValue(attrValue)) {
					Integer paramNameCde = docAttrWrapperDbData.getParamNameCde();
					String attrNameTxt = docAttrWrapperDbData.getAttrNameTxt();
					List<String> attrValues;
					if (docAttrWrapperDbData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
						List<AttributeDbData> parameterDataList = AttributeHelper
								.convertJsonStringToMultiAttr(attrValue).getAttributes();
						attrValue = parameterDataList.stream()
								.filter(multiAttrData -> multiAttrData.getAttrNameTxt().equals(attrNameTxt))
								.collect(Collectors.toList()).get(0).getAttrValue();
					}
					if (attrValueMap.containsKey(paramNameCde)) {
						attrValues = attrValueMap.get(paramNameCde);
					} else {
						attrValues = new ArrayList<>();
					}
					attrValues.add(attrValue);
					attrValueMap.put(paramNameCde, attrValues);
				}
			}
			for (ActionParamAttrMappingDbData docAttrWrapperDbData : dbDataList) {
				int actionNameCde = docAttrWrapperDbData.getActionNameCde();
				Integer paramNameCde = docAttrWrapperDbData.getParamNameCde();
				if (paramNameCde > 0 && !paramAddedList.contains(paramNameCde)) {
					paramAttrResData = new ParamAttrResData();
					paramAttrResData.setParamNameCde(docAttrWrapperDbData.getParamNameCde());
					paramAttrResData.setParamNameTxt(docAttrWrapperDbData.getParamNameTxt());
					paramAttrResData.setAttrNameCde(docAttrWrapperDbData.getAttrNameCde());
					List<String> attrValues = attrValueMap.get(paramNameCde);
					if (!ListUtility.hasValue(attrValues)) {
						attrValues = new ArrayList<>();
					}
					paramAttrResData.setAttrValues(attrValues);
					paramAttrResData.setAttrNameTxt(docAttrWrapperDbData.getAttrNameTxt());
					actionParamMap.get(actionNameCde).add(paramAttrResData);
					paramAddedList.add(paramNameCde);
				}
			}

			// Loop through list to gather all items to be sent as reply
			for (Integer key : keyList) {
				actionResData = actionResDataMap.get(key);
				actionResData.setMappingList(actionParamMap.get(key));
				actionResDataList.add(actionResData);
			}
		}
		return actionResDataList;
	}
}
