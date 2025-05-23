/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumAttrOperationType;
import com.infosys.ainauto.docwb.service.dao.attribute.IAttributeDataAccess;
import com.infosys.ainauto.docwb.service.model.api.attribute.ManageMultiAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ManageMultiAttributeReqData.AttributeData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ManageMultiAttributeReqData.MultiAttributeData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.process.Counter;

@Component
public class MultiAttributeProcess implements IMultiAttributeProcess {

	@Autowired
	private AttributeProcess attributeProcess;

	@Autowired
	private IAttributeDataAccess attributeDataAccess;

	@Override
	public List<EntityDbData> manageAttributes(ManageMultiAttributeReqData attributeReqDataList)
			throws WorkbenchException {
		List<AttributeDbData> attributeDbData = null;

		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<Long> prevAttachAttrRelIdList = new ArrayList<Long>();
		List<Long> latestAttachAttrRelIdList = new ArrayList<Long>();

		long docId = attributeReqDataList.getDocId();
		long docActionRelId = attributeReqDataList.getDocActionRelId();
		Map<Long, String> docActionRelMap = new HashMap<Long, String>();
		Counter counterObj = null;
		List<AttributeDbData> attrDataListToConvert = new ArrayList<AttributeDbData>();
		List<AttributeDbData> attributeDbDataUpdatedList = new ArrayList<AttributeDbData>();
		List<AttributeDbData> attributeDbDataDeletedList = new ArrayList<AttributeDbData>();
		List<AttributeDbData> attributeDbDataAddedList = new ArrayList<AttributeDbData>();
		int updatedCount = 0;
		int addedCount = 0;
		int deletedCount = 0;

		int attrNameCde = 0;

		attributeDbData = convertEditReqToAttrList(attributeReqDataList);
		if (ListUtility.hasValue(attributeDbData)) {
			counterObj = new Counter();
			attrDataListToConvert = attributeProcess.convertToGroupedAttributeList(docId, attributeDbData,
					EnumAttrOperationType.UPDATE, counterObj, docActionRelId, docActionRelMap);
			for (AttributeDbData attrDataToConvert : attrDataListToConvert) {
				attributeDbDataUpdatedList
						.addAll(AttributeHelper.convertJsonStringToMultiAttr(attrDataToConvert, null).getAttributes());
			}
			updatedCount = counterObj.getCount();
		}

		attributeDbData = convertDeleteReqToAttrList(attributeReqDataList);
		if (ListUtility.hasValue(attributeDbData)) {
			counterObj = new Counter();
			attrDataListToConvert = attributeProcess.convertToGroupedAttributeList(docId, attributeDbData,
					EnumAttrOperationType.DELETE, counterObj, docActionRelId, docActionRelMap);
			for (AttributeDbData attrDataToConvert : attrDataListToConvert) {
				attributeDbDataDeletedList
						.addAll(AttributeHelper.convertJsonStringToMultiAttr(attrDataToConvert, null).getAttributes());
			}
			deletedCount = counterObj.getCount();
		}

		attributeDbData = convertAddReqToAttrList(attributeReqDataList);
		if (ListUtility.hasValue(attributeDbData)) {
			counterObj = new Counter();
			attrDataListToConvert = attributeProcess.convertToGroupedAttributeList(docId, attributeDbData,
					EnumAttrOperationType.INSERT, counterObj, docActionRelId, docActionRelMap);
			for (AttributeDbData attrDataToConvert : attrDataListToConvert) {
				attributeDbDataAddedList
						.addAll(AttributeHelper.convertJsonStringToMultiAttr(attrDataToConvert, null).getAttributes());
			}
			addedCount = counterObj.getCount();
		}

		List<AttributeDbData> consolidatedAttrData = getConsolidatedAttrDataList(attributeDbDataUpdatedList,
				attributeDbDataDeletedList, attributeDbDataAddedList);

		if (ListUtility.hasValue(attrDataListToConvert)) {
			AttributeDbData consolidatedData = attrDataListToConvert.get(0);
			consolidatedData.setAttributes(consolidatedAttrData);
			consolidatedData.setAttrValue(AttributeHelper.convertMultiAttrToJsonString(consolidatedData));

			attrNameCde = consolidatedData.getAttrNameCde();
			String userName = SessionHelper.getLoginUsername();
			if (docActionRelId > -1 && docActionRelMap.get(docActionRelId) != null) {
				try {
					JsonNode actionResultObj = new ObjectMapper().readValue(docActionRelMap.get(docActionRelId),
							JsonNode.class);
					userName = actionResultObj.get(WorkbenchConstants.ATTR_DATA_FIELD_DOCUMENT)
							.get(WorkbenchConstants.ATTR_DATA_FIELD_CREATEBY).textValue();
				} catch (Exception e) {
				}
			}

			for (AttributeDbData resultAttrDbData : attrDataListToConvert) {
				long prevAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
				long latestAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
				AttributeDbData deletedAttributeDbData = null;
				// Capture service name instead of user name when request comes from Re-Extract
				// review screen.
				resultAttrDbData.setLastModByUserLoginId(userName);
				resultAttrDbData.setCreateByUserLoginId(userName);
				// Always, delete currently active multi-attribute row first
				// before making any changes
				if (resultAttrDbData.getId() > 0) {
					deletedAttributeDbData = attributeDataAccess.deleteAttribute(resultAttrDbData);
					prevAttrRelId = deletedAttributeDbData.getDocAttrRelId() > 0
							? deletedAttributeDbData.getDocAttrRelId()
							: deletedAttributeDbData.getAttachmentAttrRelId();
				}
				if (resultAttrDbData.getAttachmentId() > 0)
					prevAttachAttrRelIdList.add(prevAttrRelId);
				else
					prevDocAttrRelIdList.add(prevAttrRelId);

				// Add as existing or new based on whether delete was performed earlier or not
				if (prevAttrRelId > 0 && deletedAttributeDbData != null) {
					latestAttrRelId = attributeDataAccess.addExistingAttribute(resultAttrDbData,
							deletedAttributeDbData.getCreateByUserLoginId());
				} else {
					latestAttrRelId = attributeDataAccess.addNewAttribute(resultAttrDbData);
				}

				// Audit- storing the previous and latest rel id based on entities.
				if (latestAttrRelId > 0) {
					if (resultAttrDbData.getAttachmentId() > 0)
						latestAttachAttrRelIdList.add(latestAttrRelId);
					else
						latestDocAttrRelIdList.add(latestAttrRelId);
					// TODO 11/06/2020 - Check on audit as below prevAttrRelId is not being used
					// anywhere
					if (attributeProcess.isAttributeDeleteAllowed(resultAttrDbData)) {
						AttributeDbData attributeDbData1 = new AttributeDbData();
						BeanUtils.copyProperties(resultAttrDbData, attributeDbData1);
						attributeDbData1.setId(latestAttrRelId);
						deletedAttributeDbData = attributeDataAccess.deleteAttribute(attributeDbData1);
						prevAttrRelId = deletedAttributeDbData.getDocAttrRelId() > 0
								? deletedAttributeDbData.getDocAttrRelId()
								: deletedAttributeDbData.getAttachmentAttrRelId();

					}
				}
			}
		}

		List<EntityDbData> entityDataList = attributeProcess.createEntityDataList(prevDocAttrRelIdList,
				latestDocAttrRelIdList, prevAttachAttrRelIdList, latestAttachAttrRelIdList,
				(addedCount + deletedCount + updatedCount));
		if (ListUtility.hasValue(entityDataList)) {
			entityDataList.get(0).setAttrNameCde(attrNameCde);
			entityDataList.get(0).setAddProcessedCount(addedCount);
			entityDataList.get(0).setUpdateProcessedCount(updatedCount);
			entityDataList.get(0).setDeleteProcessedCount(deletedCount);
		}

		return entityDataList;
	}

	/** Convert for the list of attributes which are added newly */
	private List<AttributeDbData> convertAddReqToAttrList(ManageMultiAttributeReqData attributeReqData) {
		List<AttributeDbData> attributeDbDatas = new ArrayList<>();
		if (attributeReqData.getAttachment() != null && attributeReqData.getAttachment().getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttachment().getAttribute().getAddAttributes())) {
			attributeDbDatas.add(getAttachmentAttributes(attributeReqData, EnumAttrOperationType.INSERT));
		}
		if (attributeReqData.getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttribute().getAddAttributes())) {
			attributeDbDatas.add(getAttributes(attributeReqData, EnumAttrOperationType.INSERT));
		}

		return attributeDbDatas;
	}

	/** Convert for the list of attributes which are updated */
	private List<AttributeDbData> convertEditReqToAttrList(ManageMultiAttributeReqData attributeReqData) {
		List<AttributeDbData> attributeDbDatas = new ArrayList<>();
		if (attributeReqData.getAttachment() != null && attributeReqData.getAttachment().getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttachment().getAttribute().getEditAttributes())) {
			attributeDbDatas.add(getAttachmentAttributes(attributeReqData, EnumAttrOperationType.UPDATE));
		}
		if (attributeReqData.getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttribute().getEditAttributes())) {
			attributeDbDatas.add(getAttributes(attributeReqData, EnumAttrOperationType.UPDATE));
		}
		return attributeDbDatas;
	}

	/** Convert for the list of attributes which are deleted */
	private List<AttributeDbData> convertDeleteReqToAttrList(ManageMultiAttributeReqData attributeReqData) {
		List<AttributeDbData> attributeDbDatas = new ArrayList<>();
		if (attributeReqData.getAttachment() != null && attributeReqData.getAttachment().getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttachment().getAttribute().getDeleteAttributes())) {
			attributeDbDatas.add(getAttachmentAttributes(attributeReqData, EnumAttrOperationType.DELETE));
		}
		if (attributeReqData.getAttribute() != null
				&& ListUtility.hasValue(attributeReqData.getAttribute().getDeleteAttributes())) {
			attributeDbDatas.add(getAttributes(attributeReqData, EnumAttrOperationType.DELETE));
		}
		return attributeDbDatas;
	}

	private AttributeDbData getAttachmentAttributes(ManageMultiAttributeReqData attributeReqData,
			EnumAttrOperationType opType) {
		MultiAttributeData attributeData = attributeReqData.getAttachment().getAttribute();
		AttributeDbData attributeDbData = new AttributeDbData();
		BeanUtils.copyProperties(attributeData, attributeDbData);
		List<AttributeData> attributes = null;
		if (opType == EnumAttrOperationType.DELETE) {
			attributes = attributeData.getDeleteAttributes();
		} else if (opType == EnumAttrOperationType.UPDATE) {
			attributes = attributeData.getEditAttributes();
		} else if (opType == EnumAttrOperationType.INSERT) {
			attributes = attributeData.getAddAttributes();
		}
		attributeDbData.setAttributes(getInnerAttributes(attributes));
		attributeDbData.setAttachmentId(attributeReqData.getAttachment().getAttachmentId());
		attributeDbData.setDocId(attributeReqData.getDocId());
		return attributeDbData;
	}

	private AttributeDbData getAttributes(ManageMultiAttributeReqData attributeReqData, EnumAttrOperationType opType) {
		AttributeDbData attributeDbData = new AttributeDbData();
		MultiAttributeData attributeData = attributeReqData.getAttribute();
		BeanUtils.copyProperties(attributeData, attributeDbData);
		List<AttributeData> attributes = null;
		if (opType == EnumAttrOperationType.DELETE) {
			attributes = attributeData.getDeleteAttributes();
		} else if (opType == EnumAttrOperationType.UPDATE) {
			attributes = attributeData.getEditAttributes();
		} else if (opType == EnumAttrOperationType.INSERT) {
			attributes = attributeData.getAddAttributes();
		}
		attributeDbData.setAttributes(getInnerAttributes(attributes));
		attributeDbData.setDocId(attributeReqData.getDocId());
		return attributeDbData;
	}

	private <T> List<AttributeDbData> getInnerAttributes(List<T> attributes) {
		List<AttributeDbData> attributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributes)) {
			for (T grtAttributeData : attributes) {
				AttributeDbData attributeData = new AttributeDbData();
				BeanUtils.copyProperties(grtAttributeData, attributeData);
				attributeData.setAttributes(getInnerAttributes(attributeData.getAttributes()));
				attributeDataList.add(attributeData);
			}
		}

		return attributeDataList;
	}

	/**
	 * consolidate all the added/ deleted/ updated attributes list into one object
	 */
	private List<AttributeDbData> getConsolidatedAttrDataList(List<AttributeDbData> attributeDbDataUpdatedList,
			List<AttributeDbData> attributeDbDataDeletedList, List<AttributeDbData> attributeDbDataAddedList) {
		List<AttributeDbData> consolidatedAttrData = new ArrayList<AttributeDbData>();

		if (ListUtility.hasValue(attributeDbDataUpdatedList)) {

			if (ListUtility.hasValue(attributeDbDataDeletedList)) {
				for (AttributeDbData updateAttrData : attributeDbDataUpdatedList) {
					for (AttributeDbData deletedAttrData : attributeDbDataDeletedList) {
						if (updateAttrData.getId() == deletedAttrData.getId()) {
							if (deletedAttrData.getEndDtm() != null) {
								updateAttrData.setEndDtm(deletedAttrData.getEndDtm());
							}
							break;
						}
					}
				}
			}

			if (ListUtility.hasValue(attributeDbDataAddedList)) {
				int startIndex = attributeDbDataUpdatedList.size();
				int endIndex = attributeDbDataAddedList.size();
				for (int i = startIndex; i < endIndex; i++) {
					attributeDbDataUpdatedList.add(attributeDbDataAddedList.get(i));
				}
			}

			consolidatedAttrData = attributeDbDataUpdatedList;

		} else if (ListUtility.hasValue(attributeDbDataDeletedList)) {
			if (ListUtility.hasValue(attributeDbDataAddedList)) {
				int startIndex = attributeDbDataDeletedList.size();
				int endIndex = attributeDbDataAddedList.size();
				while (startIndex < endIndex) {
					attributeDbDataDeletedList.add(attributeDbDataAddedList.get(startIndex));
					startIndex++;
				}
			}
			consolidatedAttrData = attributeDbDataDeletedList;
		} else if (ListUtility.hasValue(attributeDbDataAddedList)) {
			consolidatedAttrData = attributeDbDataAddedList;
		}

		return consolidatedAttrData;
	}

}
