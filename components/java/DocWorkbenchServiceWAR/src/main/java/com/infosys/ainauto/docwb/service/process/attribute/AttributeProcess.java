/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.attribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.common.ExportAttributeHelper;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumAttrOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumExtractType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.dao.action.IDocActionDataAccess;
import com.infosys.ainauto.docwb.service.dao.attribute.IAttributeDataAccess;
import com.infosys.ainauto.docwb.service.dao.doc.IDocDataAccess;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.dao.val.IValDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AllowedValueResData;
import com.infosys.ainauto.docwb.service.model.api.AttributeNameValueResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeNameResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeSourceReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.DeleteAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ExportAttributeResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.InsertAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.InsertAttributeReqData.InsertAttributeData;
import com.infosys.ainauto.docwb.service.model.api.attribute.UpdateAttributeReqData;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeNameValueDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;
import com.infosys.ainauto.docwb.service.model.process.Counter;
import com.infosys.ainauto.docwb.service.service.ml.IRulesService;

@Component
public class AttributeProcess implements IAttributeProcess {

	@Autowired
	private IAttributeDataAccess attributeDataAccess;
	@Autowired
	private IDocDataAccess docDataAccess;
	@Autowired
	private IRulesService rulesService;
	@Autowired
	private IValDataAccess valDataAccess;
	@Autowired
	private IUserDataAccess userDataAccess;
	@Autowired
	private IDocActionDataAccess actionDataAccess;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infosys.ainauto.docwb.service.process.attribute.IAttributeProcess#
	 * addAttribute(java.util.List)
	 */
	@Override
	public List<EntityDbData> addAttribute(List<InsertAttributeReqData> insertAttrReqDataList)
			throws WorkbenchException {
		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<Long> prevAttachAttrRelIdList = new ArrayList<Long>();
		List<Long> latestAttachAttrRelIdList = new ArrayList<Long>();
		Counter insertCounter = new Counter();
		Map<Long, String> docActionRelMap = new HashMap<Long, String>();
		for (InsertAttributeReqData insertAttributeReqData : insertAttrReqDataList) {
			long docId = insertAttributeReqData.getDocId();
			long docActionRelId = insertAttributeReqData.getDocActionRelId();
			List<AttributeDbData> insertAttrDataList = new ArrayList<AttributeDbData>();
			List<AttributeDbData> attributeReqDataList = convertReqDataToAttributeList(insertAttributeReqData, docId);
			if (attributeReqDataList.size() > 0) {
				// To get the final attribute list for a document.
				insertAttrDataList = convertToGroupedAttributeList(docId, attributeReqDataList,
						EnumAttrOperationType.INSERT, insertCounter, docActionRelId, docActionRelMap);
			}
			if (ListUtility.hasValue(insertAttrDataList)) {
				for (AttributeDbData resultAttrDbData : insertAttrDataList) {
					long prevAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
					long latestAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
					AttributeDbData deletedAttributeDbData = null;

					// If this is not a normal attribute but a new "multi-attribute"
					// then this should be added to existing multi-attribute row in DB
					// For that, first delete currently active multi-attribute row
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
					}
				}
			}
		}
		return createEntityDataList(prevDocAttrRelIdList, latestDocAttrRelIdList, prevAttachAttrRelIdList,
				latestAttachAttrRelIdList, insertCounter.getCount());
	}

	@Override
	public List<EntityDbData> addAttributeSource(AttributeSourceReqData attributeSourceReqData)
			throws WorkbenchException {
//		soft delete existing record
		attributeDataAccess.deleteAttributeSource(attributeSourceReqData.getDocId());
		ObjectMapper mapper = new ObjectMapper();
		String recordJson;
		try {
			recordJson = mapper.writeValueAsString(attributeSourceReqData.getRecord());
		} catch (JsonProcessingException e) {
			throw new WorkbenchException(e);
		}
		long resId = attributeDataAccess.addAttributeSource(attributeSourceReqData.getDocId(),recordJson);
		List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
		EntityDbData entityDbData = new EntityDbData();
		entityDbData.setDocId(attributeSourceReqData.getDocId());
		entityDbData.setAttrSourceId(resId);
		entityDbDataList.add(entityDbData);
		return entityDbDataList;
	}
	
	@Override
	public List<EntityDbData> updateAttribute(List<UpdateAttributeReqData> updateAttributeReqDataList)
			throws WorkbenchException {
		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<Long> prevAttachAttrRelIdList = new ArrayList<Long>();
		List<Long> latestAttachAttrRelIdList = new ArrayList<Long>();
		Counter updateCounter = new Counter();
		Map<Long, String> docActionRelMap = new HashMap<Long, String>();
		for (int i = 0; i < updateAttributeReqDataList.size(); i++) {
			List<AttributeDbData> updateAttrDataList = new ArrayList<AttributeDbData>();
			long docId = updateAttributeReqDataList.get(i).getDocId();
			long docActionRelId = updateAttributeReqDataList.get(i).getDocActionRelId();
			List<AttributeDbData> attributeDbDatas = convertReqDataToAttributeList(updateAttributeReqDataList.get(i),
					docId);
			if (attributeDbDatas.size() > 0) {
				updateAttrDataList = convertToGroupedAttributeList(docId, attributeDbDatas,
						EnumAttrOperationType.UPDATE, updateCounter, docActionRelId, docActionRelMap);
			}
			for (AttributeDbData attributeData : updateAttrDataList) {
				AttributeDbData deletedAttributeDbData = attributeDataAccess.deleteAttribute(attributeData);
				long prevAttrRelId = deletedAttributeDbData.getDocAttrRelId() > 0
						? deletedAttributeDbData.getDocAttrRelId()
						: deletedAttributeDbData.getAttachmentAttrRelId();

				if (prevAttrRelId > 0) {
					long latestAttrRelId = attributeDataAccess.addExistingAttribute(attributeData,
							deletedAttributeDbData.getCreateByUserLoginId());
					// Audit- storing the previous and latest rel id based on entities.
					if (latestAttrRelId > 0) {
						if (attributeData.getAttachmentId() > 0) {
							latestAttachAttrRelIdList.add(latestAttrRelId);
							prevAttachAttrRelIdList.add(prevAttrRelId);
						} else {
							latestDocAttrRelIdList.add(latestAttrRelId);
							prevDocAttrRelIdList.add(prevAttrRelId);
						}
					}
				}
			}
		}
		return createEntityDataList(prevDocAttrRelIdList, latestDocAttrRelIdList, prevAttachAttrRelIdList,
				latestAttachAttrRelIdList, updateCounter.getCount());
	}

	@Override
	public List<EntityDbData> deleteAttribute(List<DeleteAttributeReqData> deleteAttributeReqDatas)
			throws WorkbenchException {
		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<Long> prevAttachAttrRelIdList = new ArrayList<Long>();
		List<Long> latestAttachAttrRelIdList = new ArrayList<Long>();
		// Integer deleteCount = 0;
		Counter deleteCounter = new Counter();
		deleteCounter.setCount(0);
		Map<Long, String> docActionRelMap = new HashMap<Long, String>();
		for (int i = 0; i < deleteAttributeReqDatas.size(); i++) {
			long docId = deleteAttributeReqDatas.get(i).getDocId();
			long docActionRelId = deleteAttributeReqDatas.get(i).getDocActionRelId();
			List<AttributeDbData> deleteAttrDataList = new ArrayList<>();
			List<AttributeDbData> attributeDbDatas = convertReqDataToAttributeList(deleteAttributeReqDatas.get(i),
					docId);
			if (attributeDbDatas.size() > 0) {
				deleteAttrDataList = convertToGroupedAttributeList(docId, attributeDbDatas,
						EnumAttrOperationType.DELETE, deleteCounter, docActionRelId, docActionRelMap);
			}
			for (AttributeDbData attributeData : deleteAttrDataList) {
				long prevAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
				long latestAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
				if (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
					// Always, delete currently active multi-attribute row first
					// before making any changes
					AttributeDbData deletedAttributeDbData = attributeDataAccess.deleteAttribute(attributeData);
					prevAttrRelId = deletedAttributeDbData.getDocAttrRelId() > 0
							? deletedAttributeDbData.getDocAttrRelId()
							: deletedAttributeDbData.getAttachmentAttrRelId();

					// If attributeId is valid as returned from delete method then proceed further
					if (prevAttrRelId > 0) {
						latestAttrRelId = attributeDataAccess.addExistingAttribute(attributeData,
								deletedAttributeDbData.getCreateByUserLoginId());

						// Audit- storing the previous and latest rel id based on entities.
						if (latestAttrRelId > 0) {
							if (attributeData.getAttachmentId() > 0) {
								latestAttachAttrRelIdList.add(latestAttrRelId);
								prevAttachAttrRelIdList.add(prevAttrRelId);
							} else {
								latestDocAttrRelIdList.add(latestAttrRelId);
								prevDocAttrRelIdList.add(prevAttrRelId);
							}
							// Check after previous delete of one or more multi-attribute children, whether
							// any more multi-attribute children are still active.
							// If none, then delete full row
							if (isAttributeDeleteAllowed(attributeData)) {
								AttributeDbData attributeDbData = new AttributeDbData();
								BeanUtils.copyProperties(attributeData, attributeDbData);
								attributeDbData.setId(latestAttrRelId);

								AttributeDbData deletedAttributeDbData2 = attributeDataAccess
										.deleteAttribute(attributeDbData);
								prevAttrRelId = deletedAttributeDbData2.getDocAttrRelId() > 0
										? deletedAttributeDbData2.getDocAttrRelId()
										: deletedAttributeDbData2.getAttachmentAttrRelId();

								// Audit- storing the previous and latest rel id based on entities.
								if (prevAttrRelId > 0) {
									if (attributeData.getAttachmentId() > 0) {
										latestAttachAttrRelIdList
												.add((long) WorkbenchConstants.DOC_ACTION_REL_ID_UNSET);
										prevAttachAttrRelIdList.add(prevAttrRelId);
									} else {
										latestDocAttrRelIdList.add((long) WorkbenchConstants.DOC_ACTION_REL_ID_UNSET);
										prevDocAttrRelIdList.add(prevAttrRelId);
									}
								}

							}
						}
					}
				} else { // If NOT a multi-attribute
					AttributeDbData deletedAttributeDbData = attributeDataAccess.deleteAttribute(attributeData);
					prevAttrRelId = deletedAttributeDbData.getDocAttrRelId() > 0
							? deletedAttributeDbData.getDocAttrRelId()
							: deletedAttributeDbData.getAttachmentAttrRelId();
					// Audit- storing the previous and latest rel id based on entities.
					if (prevAttrRelId > 0) {
						if (attributeData.getAttachmentId() > 0) {
							latestAttachAttrRelIdList.add(latestAttrRelId);
							prevAttachAttrRelIdList.add(prevAttrRelId);
						} else {
							latestDocAttrRelIdList.add(latestAttrRelId);
							prevDocAttrRelIdList.add(prevAttrRelId);
						}
					}
				}
			}
		}
		return createEntityDataList(prevDocAttrRelIdList, latestDocAttrRelIdList, prevAttachAttrRelIdList,
				latestAttachAttrRelIdList, deleteCounter.getCount());
	}

	@Override
	public List<AttributeDbData> getAttributeText() throws WorkbenchException {
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getAttributeText();
		return attributeDbDataList;
	}

	@Override
	public List<AttributeNameResData> getAttributeAttributeMapping() throws WorkbenchException {
		List<AttributeNameResData> attributeAttributeMapList = null;
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getAttributeText();
		Map<Integer, String> attributeDbDataMap = new HashMap<Integer, String>();
		for (AttributeDbData attributeDbData : attributeDbDataList) {
			attributeDbDataMap.put(attributeDbData.getAttrNameCde(), attributeDbData.getAttrNameTxt());
		}
		if (!attributeDbDataMap.isEmpty()) {
			attributeAttributeMapList = rulesService.getAttributeAttributeMapping(SessionHelper.getTenantId(),
					attributeDbDataMap);
		}
		return attributeAttributeMapList;
	}

	@Override
	public List<AttributeNameResData> getAttributeSortingKey() throws WorkbenchException {
		List<AttributeNameResData> attributeSortKeyList = null;
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getAttributeText();
		Map<Integer, String> attributeDbDataMap = new HashMap<Integer, String>();
		for (AttributeDbData attributeDbData : attributeDbDataList) {
			attributeDbDataMap.put(attributeDbData.getAttrNameCde(), attributeDbData.getAttrNameTxt());
		}
		if (!attributeDbDataMap.isEmpty()) {
			attributeSortKeyList = rulesService.getAttributeSortingKey(SessionHelper.getTenantId(), attributeDbDataMap);
		}
		return attributeSortKeyList;
	}

	@Override
	public ExportAttributeResData exportAllAttributes(long docId) throws WorkbenchException {
		ExportAttributeResData exportAttributeResData = new ExportAttributeResData();
		exportAttributeResData.setDocId(docId);
		exportAttributeResData
				.setAttributes(ExportAttributeHelper.convertAttrDataListToExportData(getDocumentAttributes(docId)));
		exportAttributeResData.setAttachments(
				ExportAttributeHelper.convertAttachDataListToExportData(getAttachmentAttributes(docId, null,false)));

		return exportAttributeResData;
	}

	@Override
	public List<AttributeDbData> getDocumentAttributes(long docId) throws WorkbenchException {
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getDocumentAttributes(docId);
		List<AttributeDbData> attrDbDataList = new ArrayList<>();
		List<ValTableDbData> extractTypeDbDataList = valDataAccess
				.getValTableData(WorkbenchConstants.EXTRACT_TYPE_VAL_TABLE_NAME);
		HashMap<Integer, String> extractTypeValMap = new HashMap<>();
		for (ValTableDbData valTableDbData : extractTypeDbDataList) {
			extractTypeValMap.put(valTableDbData.getCde(), valTableDbData.getTxt());
		}
		if (ListUtility.hasValue(attributeDbDataList)) {
			for (AttributeDbData attributeData : attributeDbDataList) {
				populateUserData(attributeData);
				attributeData.setExtractTypeTxt(extractTypeValMap.get(attributeData.getExtractTypeCde()));
				if (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
					AttributeDbData multiAttrData = AttributeHelper.convertJsonStringToMultiAttr(attributeData,
							userDataAccess);
					attributeData.setAttrValue(null);
					if (ListUtility.hasValue(multiAttrData.getAttributes())) {
						attributeData.setAttributes(AttributeHelper.getAlphabeticallySortedAttributeList(
								multiAttrData.getAttributes().stream().filter(attrData -> attrData.getEndDtm() == null)
										.collect(Collectors.toList())));
					}
					attrDbDataList.add(attributeData);
				} else {
					attrDbDataList.add(attributeData);
				}
			}
		}
		if (ListUtility.hasValue(attrDbDataList)) {
			attrDbDataList = AttributeHelper.getAlphabeticallySortedAttributeList(attrDbDataList);
		}
		return attrDbDataList;
	}

	@Override
	public List<AttributeDbData> getAttachmentAttributes(long docId, String attachmentIds,boolean origValue) throws WorkbenchException {
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getAttachmentAttributes(docId, attachmentIds);
		List<AttributeDbData> attrDbDataList = new ArrayList<>();
		List<AttributeDbData> attributeOrigDbDataList=new ArrayList<AttributeDbData>();;
		List<ValTableDbData> extractTypeDbDataList = valDataAccess
				.getValTableData(WorkbenchConstants.EXTRACT_TYPE_VAL_TABLE_NAME);
		HashMap<Integer, String> extractTypeValMap = new HashMap<>();
		for (ValTableDbData valTableDbData : extractTypeDbDataList) {
			extractTypeValMap.put(valTableDbData.getCde(), valTableDbData.getTxt());
		}
		
		if (origValue) {
			attributeOrigDbDataList= attributeDataAccess.getAttachmentAttributesOrigValue(docId,origValue);
			
		}
		if (ListUtility.hasValue(attributeDbDataList)) {
			for (AttributeDbData attributeData : attributeDbDataList) {
				populateUserData(attributeData);
				attributeData.setExtractTypeTxt(extractTypeValMap.get(attributeData.getExtractTypeCde()));
				if ((attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
						|| (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())) {
					AttributeDbData multiAttrData = AttributeHelper.convertJsonStringToMultiAttr(attributeData,
							userDataAccess);
					attributeData.setAttrValue(null);
					attributeData.setAttributes(
							AttributeHelper.getAlphabeticallySortedAttributeList(multiAttrData.getAttributes().stream()
									.filter(attrData -> attrData.getEndDtm() == null).collect(Collectors.toList())));
					if (ListUtility.hasValue(attributeOrigDbDataList)) {
						for (AttributeDbData attributeData1 : attributeOrigDbDataList) {
							if(attributeData1.getAttrNameTxt().equals(attributeData.getAttrNameTxt() ) )
							{
							attributeData.setAttrValueOrig(attributeData1.getAttrValueOrig());
							}
						}
						}
					attrDbDataList.add(attributeData);
				} else {
					if (ListUtility.hasValue(attributeOrigDbDataList)) {
						for (AttributeDbData attributeData1 : attributeOrigDbDataList) {
							if(attributeData1.getAttrNameTxt().equals(attributeData.getAttrNameTxt() ) )
							{
							attributeData.setAttrValueOrig(attributeData1.getAttrValueOrig());
							}
						}
						}
					attrDbDataList.add(attributeData);
				}
				
			}
		}

		if (ListUtility.hasValue(attrDbDataList)) {
			Map<Long, List<AttributeDbData>> groupByIdMap = attrDbDataList.stream()
					.collect(Collectors.groupingBy(AttributeDbData::getAttachmentId));
			attrDbDataList = new ArrayList<>();
			for (int i = 0; i < groupByIdMap.size(); i++) {
				AttributeDbData dbData = new AttributeDbData();
				long key = (long) groupByIdMap.keySet().toArray()[i];
				dbData.setAttachmentId(key);
				dbData.setAttributes(AttributeHelper.getAlphabeticallySortedAttributeList(groupByIdMap.get(key)));
				attrDbDataList.add(dbData);
			}
		}
		return attrDbDataList;
	}

	@Override
	public GetAttributeNotificationResData getAttributesNotification(long docId) throws WorkbenchException {
		DocumentDbData documentData = docDataAccess.getDocumentDetails(docId);
		int docTypeCde = documentData != null ? documentData.getDocTypeCde() : 0;
		List<AttributeDbData> docAttributeDbDataList = null;
		if (docTypeCde == WorkbenchConstants.EMAIL)
			docAttributeDbDataList = getDocumentAttributes(docId);

		List<AttributeDbData> attachmentAttributeDbDataList = getAttachmentAttributes(docId, "",false);
		GetAttributeNotificationResData responseData = null;
		if (ListUtility.hasValue(docAttributeDbDataList) || ListUtility.hasValue(attachmentAttributeDbDataList)) {
			responseData = rulesService.getAttributesNotification(SessionHelper.getTenantId(), docId, docTypeCde,
					docAttributeDbDataList, attachmentAttributeDbDataList);
		}
		return responseData;
	}

	@Override
	public List<AttributeNameValueResData> getAttributeNameValues(String attrNameCdes) throws WorkbenchException {
		List<AttributeNameValueDbData> attributeNameValueDbDataList = attributeDataAccess
				.getAttributeNameValues(attrNameCdes);
		// List to store unique attr name value data
		List<AttributeNameValueResData> uniqueAttrNameValueResDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributeNameValueDbDataList)) {
			AttributeNameValueResData attrNameValueResData = new AttributeNameValueResData();
			attrNameValueResData.setAttrNameCde(attributeNameValueDbDataList.get(0).getAttrNameCde());
			attrNameValueResData.setAttrNameTxt(attributeNameValueDbDataList.get(0).getAttrNameTxt());
			uniqueAttrNameValueResDataList.add(attrNameValueResData);
			for (AttributeNameValueDbData attributeNameValueDbData : attributeNameValueDbDataList) {
				// List from DB is sorted so compare current and prev to determine if new object
				// needs to be created
				if (attrNameValueResData.getAttrNameCde() != attributeNameValueDbData.getAttrNameCde()) {
					attrNameValueResData = new AttributeNameValueResData();
					attrNameValueResData.setAttrNameCde(attributeNameValueDbData.getAttrNameCde());
					attrNameValueResData.setAttrNameTxt(attributeNameValueDbData.getAttrNameTxt());
					uniqueAttrNameValueResDataList.add(attrNameValueResData);
				}
			}
		}
		List<AttributeNameValueResData> attributeValueResDataList = new ArrayList<AttributeNameValueResData>();
		AllowedValueResData allowedValueResData = new AllowedValueResData();
		for (AttributeNameValueResData attrNameValueResData : uniqueAttrNameValueResDataList) {
			List<AllowedValueResData> allowedValuesResDataList = new ArrayList<>();
			for (AttributeNameValueDbData attributeNameValueDbData : attributeNameValueDbDataList) {
				if (attrNameValueResData.getAttrNameCde() == attributeNameValueDbData.getAttrNameCde()) {
					if (attributeNameValueDbData.getTxt() != null) {
						allowedValueResData = new AllowedValueResData();
						allowedValueResData.setSequenceNum(attributeNameValueDbData.getSequenceNum());
						allowedValueResData.setTxt(attributeNameValueDbData.getTxt());
						allowedValuesResDataList.add(allowedValueResData);
					} else {
						allowedValuesResDataList = new ArrayList<AllowedValueResData>();
						break;
					}

				}
			}
			attrNameValueResData.setAllowedValues(allowedValuesResDataList);
			attributeValueResDataList.add(attrNameValueResData);
		}
		return attributeValueResDataList;
	}

	private List<AttributeDbData> getAttributes(List<InsertAttributeReqData.InsertAttributeData> attributes) {
		List<AttributeDbData> attributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributes)) {
			for (InsertAttributeData grtAttributeData : attributes) {
				AttributeDbData attributeData = new AttributeDbData();
				BeanUtils.copyProperties(grtAttributeData, attributeData);
				attributeData.setAttributes(getAttributes(grtAttributeData.getAttributes()));
				attributeDataList.add(attributeData);
			}
		}

		return attributeDataList;
	}

	/**
	 * Method is given an object data which is converted to a list of attributes.
	 * Method compares the type of object.
	 * 
	 * @param object
	 * @param docId
	 * @return
	 */
	@Override
	public <T> List<AttributeDbData> convertReqDataToAttributeList(Object object, long docId) {
		List<AttributeDbData> attributeDbDatas = new ArrayList<>();
		if (object.getClass().getTypeName() == InsertAttributeReqData.class.getTypeName()) {
			InsertAttributeReqData attributeReqData = (InsertAttributeReqData) object;
			if (ListUtility.hasValue(attributeReqData.getAttachments())) {
				for (InsertAttributeReqData.InsertAttachmentAttrData attachmentAttrData : attributeReqData
						.getAttachments()) {
					long attachmentId = attachmentAttrData.getAttachmentId();
					for (int i = 0; i < attachmentAttrData.getAttributes().size(); i++) {
						AttributeDbData attributeDbData = new AttributeDbData();
						BeanUtils.copyProperties(attachmentAttrData.getAttributes().get(i), attributeDbData);
						List<InsertAttributeReqData.InsertAttributeData> attachmentSubAttrDataList = attachmentAttrData
								.getAttributes().get(i).getAttributes();

						attributeDbData.setAttributes(getAttributes(attachmentSubAttrDataList));
						attributeDbData.setAttachmentId(attachmentId);
						attributeDbData.setDocId(docId);
						attributeDbDatas.add(attributeDbData);
					}
				}
			}
			if (ListUtility.hasValue(attributeReqData.getAttributes())) {
				for (InsertAttributeReqData.InsertAttributeData attributeData : attributeReqData.getAttributes()) {
					AttributeDbData attributeDbData = new AttributeDbData();
					BeanUtils.copyProperties(attributeData, attributeDbData);
					List<InsertAttributeReqData.InsertAttributeData> subAttrDataList = attributeData.getAttributes();
					if (ListUtility.hasValue(subAttrDataList)) {
						List<AttributeDbData> attributes = new ArrayList<>();
						for (InsertAttributeReqData.InsertAttributeData attrData : subAttrDataList) {
							AttributeDbData subAttrData = new AttributeDbData();
							BeanUtils.copyProperties(attrData, subAttrData);
							attributes.add(subAttrData);
						}
						attributeDbData.setAttributes(attributes);
					}
					attributeDbData.setDocId(docId);
					attributeDbDatas.add(attributeDbData);
				}

			}
		} else if (object.getClass().getTypeName() == UpdateAttributeReqData.class.getTypeName()) {
			UpdateAttributeReqData attributeReqData = (UpdateAttributeReqData) object;
			if (ListUtility.hasValue(attributeReqData.getAttributes())
					|| ListUtility.hasValue(attributeReqData.getAttachments())) {
				for (UpdateAttributeReqData.UpdateAttachmentAttrData attachmentAttrData : attributeReqData
						.getAttachments()) {
					long attachmentId = attachmentAttrData.getAttachmentId();
					for (int j = 0; j < attachmentAttrData.getAttributes().size(); j++) {
						AttributeDbData attributeDbData = new AttributeDbData();
						List<AttributeDbData> nestedAttrDatas = new ArrayList<>();
						BeanUtils.copyProperties(attachmentAttrData.getAttributes().get(j), attributeDbData);
						attributeDbData.setAttachmentId(attachmentId);
						if (ListUtility.hasValue(attachmentAttrData.getAttributes().get(j).getAttributes())) {
							for (UpdateAttributeReqData.NestedUpdateAttributeData nestedUpdateAttributeData : attachmentAttrData
									.getAttributes().get(j).getAttributes()) {
								AttributeDbData nestedAttrData = new AttributeDbData();
								BeanUtils.copyProperties(nestedUpdateAttributeData, nestedAttrData);
								nestedAttrDatas.add(nestedAttrData);
							}
						}
						attributeDbData.setAttributes(nestedAttrDatas);
						attributeDbData.setDocId(docId);
						attributeDbDatas.add(attributeDbData);
					}
				}
				for (UpdateAttributeReqData.UpdateAttributeData attributeData : attributeReqData.getAttributes()) {
					AttributeDbData attributeDbData = new AttributeDbData();
					List<AttributeDbData> nestedAttrDatas = new ArrayList<>();
					BeanUtils.copyProperties(attributeData, attributeDbData);
					if (ListUtility.hasValue(attributeData.getAttributes())) {
						for (UpdateAttributeReqData.NestedUpdateAttributeData nestedUpdateAttributeData : attributeData
								.getAttributes()) {
							AttributeDbData nestedAttrData = new AttributeDbData();
							BeanUtils.copyProperties(nestedUpdateAttributeData, nestedAttrData);
							nestedAttrDatas.add(nestedAttrData);
						}
					}
					attributeDbData.setAttributes(nestedAttrDatas);
					attributeDbData.setDocId(docId);
					attributeDbDatas.add(attributeDbData);
				}
			}
		} else if (object.getClass().getTypeName() == DeleteAttributeReqData.class.getTypeName()) {
			DeleteAttributeReqData attributeReqData = (DeleteAttributeReqData) object;
			if (ListUtility.hasValue(attributeReqData.getAttributes())
					|| ListUtility.hasValue(attributeReqData.getAttachments())) {
				for (DeleteAttributeReqData.DeleteAttachmentAttrData attachmentAttrData : attributeReqData
						.getAttachments()) {
					long attachmentId = attachmentAttrData.getAttachmentId();
					for (int j = 0; j < attachmentAttrData.getAttributes().size(); j++) {
						AttributeDbData attributeDbData = new AttributeDbData();
						List<AttributeDbData> nestedAttrDatas = new ArrayList<>();
						BeanUtils.copyProperties(attachmentAttrData.getAttributes().get(j), attributeDbData);
						attributeDbData.setAttachmentId(attachmentId);
						if (ListUtility.hasValue(attachmentAttrData.getAttributes().get(j).getAttributes())) {
							for (DeleteAttributeReqData.NestedDeleteAttributeData nestedAttributeData : attachmentAttrData
									.getAttributes().get(j).getAttributes()) {
								AttributeDbData nestedAttrData = new AttributeDbData();
								BeanUtils.copyProperties(nestedAttributeData, nestedAttrData);
								nestedAttrDatas.add(nestedAttrData);
							}
						}
						attributeDbData.setAttributes(nestedAttrDatas);
						attributeDbData.setDocId(docId);
						attributeDbDatas.add(attributeDbData);
					}
				}
				for (DeleteAttributeReqData.DeleteAttributeData attributeData : attributeReqData.getAttributes()) {
					AttributeDbData attributeDbData = new AttributeDbData();
					List<AttributeDbData> nestedAttrDatas = new ArrayList<>();
					BeanUtils.copyProperties(attributeData, attributeDbData);
					if (ListUtility.hasValue(attributeData.getAttributes())) {
						for (DeleteAttributeReqData.NestedDeleteAttributeData nestedAttributeData : attributeData
								.getAttributes()) {
							AttributeDbData nestedAttrData = new AttributeDbData();
							BeanUtils.copyProperties(nestedAttributeData, nestedAttrData);
							nestedAttrDatas.add(nestedAttrData);
						}
					}
					attributeDbData.setAttributes(nestedAttrDatas);
					attributeDbData.setDocId(docId);
					attributeDbDatas.add(attributeDbData);
				}
			}
		}
		return attributeDbDatas;
	}

	/**
	 * Method takes a list of all the attributes(document or attachment or both) and
	 * groups them by their entity.
	 * 
	 * @param docId
	 * @param attributeDbDatas
	 * @param opType
	 * @param count
	 * @return
	 * @throws WorkbenchException
	 */
	@Override
	public List<AttributeDbData> convertToGroupedAttributeList(long docId, List<AttributeDbData> attributeDbDatas,
			EnumAttrOperationType opType, Counter count, long docActionRelId, Map<Long, String> docActionRelMap)
			throws WorkbenchException {
		List<AttributeDbData> attrDataList = new ArrayList<AttributeDbData>();
		List<AttributeDbData> documentAttributes = attributeDbDatas.stream()
				.filter(attributeData -> attributeData.getAttachmentId() == 0).collect(Collectors.toList());
		List<AttributeDbData> attachmentAttributes = attributeDbDatas.stream()
				.filter(attributeData -> attributeData.getAttachmentId() != 0).collect(Collectors.toList());
		if (ListUtility.hasValue(documentAttributes)) {
			List<AttributeDbData> documentMultiAttributes = documentAttributes.stream()
					.filter(attrData -> (attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
							|| attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
							&& attrData.getId() > 0)
					.collect(Collectors.toList());
			List<AttributeDbData> attrDbDataList = null;
			if (ListUtility.hasValue(documentMultiAttributes)) {
				attrDbDataList = attributeDataAccess.getDocumentAttributes(docId);
			}
			attrDataList = convertTreeToFlatAttributeList(documentAttributes, attrDbDataList, docId, (long) 0,
					EnumEntityType.DOCUMENT, opType, count, docActionRelId, docActionRelMap);
		}
		if (ListUtility.hasValue(attachmentAttributes)) {
			Map<Long, List<AttributeDbData>> groupByIdMap = attachmentAttributes.stream()
					.collect(Collectors.groupingBy(AttributeDbData::getAttachmentId));
			List<Long> keys = groupByIdMap.keySet().stream().collect(Collectors.toList());
			boolean isIdAvailable = false;
			for (int i = 0; i < groupByIdMap.size(); i++) {
				long id = keys.get(i);
				List<AttributeDbData> attributes = groupByIdMap.get(id);
				List<AttributeDbData> attachmentMultiAttributes = attributes.stream().filter(
						attrData -> ((attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
								|| attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
								&& attrData.getId() > 0))
						.collect(Collectors.toList());
				if (ListUtility.hasValue(attachmentMultiAttributes)) {
					isIdAvailable = true;
					break;
				}
			}
			List<AttributeDbData> attrDbDataList = new ArrayList<>();
			if (isIdAvailable) {
				attrDbDataList = attributeDataAccess.getAttachmentAttributes(docId, "");
			}
			for (int i = 0; i < groupByIdMap.size(); i++) {
				long id = keys.get(i);
				List<AttributeDbData> dataList = null;
				if (ListUtility.hasValue(attrDbDataList)) {
					dataList = attrDbDataList.stream().filter(attributeData -> attributeData.getAttachmentId() == id)
							.collect(Collectors.toList());
				}
				List<AttributeDbData> attributeDbDataList = convertTreeToFlatAttributeList(groupByIdMap.get(id),
						dataList, docId, id, EnumEntityType.ATTACHMENT, opType, count, docActionRelId, docActionRelMap);
				for (int j = 0; j < attributeDbDataList.size(); j++) {
					attrDataList.add(attributeDbDataList.get(j));
				}
			}
		}
		return attrDataList;
	}

	/**
	 * Method converts an attribute list of a certain entity having tree structure
	 * to flat structure by applying rules on nested attributes.
	 * 
	 * @param attributeReqDatas
	 * @param attributeDbDatas
	 * @param docId
	 * @param id
	 * @param entity
	 * @param opType
	 * @param count
	 * @return
	 * @throws WorkbenchException
	 */
	private List<AttributeDbData> convertTreeToFlatAttributeList(List<AttributeDbData> attributeReqDatas,
			List<AttributeDbData> attributeDbDatas, Long docId, Long id, EnumEntityType entity,
			EnumAttrOperationType opType, Counter count, long docActionRelId, Map<Long, String> docActionRelMap)
			throws WorkbenchException {
		List<AttributeDbData> attrDataList = new ArrayList<AttributeDbData>();
		if (ListUtility.hasValue(attributeReqDatas)) {
			String userName = SessionHelper.getLoginUsername();
			if (docActionRelId > -1) {
				String actionResult = null;
				if (docActionRelMap == null) {
					docActionRelMap = new HashMap<Long, String>();
				}
				try {
					if (docActionRelMap.get(docActionRelId) != null) {
						actionResult = docActionRelMap.get(docActionRelId);
					} else {
						actionResult = actionDataAccess.getActionResult(docActionRelId);
						docActionRelMap.put(docActionRelId, actionResult);
					}
					if (actionResult != null) {
						JsonNode actionResultObj = new ObjectMapper().readValue(actionResult, JsonNode.class);
						userName = actionResultObj.get(WorkbenchConstants.ATTR_DATA_FIELD_DOCUMENT)
								.get(WorkbenchConstants.ATTR_DATA_FIELD_CREATEBY).textValue();
					}
				} catch (Exception e) {
				}
			}
			// For processing of multi-attribute data.
			List<AttributeDbData> filterMultiAttrDbDataList = attributeReqDatas.stream()
					.filter(attributeData -> ((attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE
							.getCde())
							|| (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
									.getCde())))
					.collect(Collectors.toList());

			List<AttributeDbData> filterNonMultiAttrDbDataList = attributeReqDatas.stream().filter(
					attributeData -> (attributeData.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
							&& attributeData.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
									.getCde()))
					.collect(Collectors.toList());

			if (ListUtility.hasValue(filterMultiAttrDbDataList)) {
				List<AttributeDbData> newMultiAttrDataList = filterMultiAttrDbDataList.stream()
						.filter(attrData -> attrData.getId() > 0).collect(Collectors.toList());

				List<AttributeDbData> multiAttrDbDataList = new ArrayList<>();
				if (ListUtility.hasValue(attributeDbDatas)) {
					multiAttrDbDataList = attributeDbDatas.stream()
							.filter(multiAttrData -> (multiAttrData
									.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
									|| (multiAttrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
											.getCde()))
							.collect(Collectors.toList());
				}

				Map<Long, AttributeDbData> mapAttributeDbData = new HashMap<Long, AttributeDbData>();
				if (ListUtility.hasValue(multiAttrDbDataList) && ListUtility.hasValue(newMultiAttrDataList)) {
					for (AttributeDbData newMultiAttrData : newMultiAttrDataList) {
						if (newMultiAttrData.getId() > 0) {
							List<AttributeDbData> prevMultiAttrDbDataList = multiAttrDbDataList.stream()
									.filter(attrData -> attrData.getId() == newMultiAttrData.getId())
									.collect(Collectors.toList());
							if (ListUtility.hasValue(prevMultiAttrDbDataList)) {
								AttributeDbData prevMultiAttrDbData = prevMultiAttrDbDataList.get(0);
								AttributeDbData manipulateAttributeDbData = AttributeHelper
										.convertJsonStringToMultiAttr(prevMultiAttrDbData, null);
								mapAttributeDbData.put(prevMultiAttrDbData.getId(), manipulateAttributeDbData);
							}
						}

					}

				}

				if (mapAttributeDbData.size() > 0) {
					for (Entry<Long, AttributeDbData> preAttributeDbData : mapAttributeDbData.entrySet()) {
						AttributeDbData multiAttrDbData = preAttributeDbData.getValue();
						createOperationTypeAttributeDataList(preAttributeDbData.getKey(), filterMultiAttrDbDataList,
								multiAttrDbData, newMultiAttrDataList, opType, count, userName);

					}
				}
				if (mapAttributeDbData.size() == 0) {
					/** Insert newly added Multiple attribute */
					mapAttributeDbData = createOperationTypeAttributeDataList((long) 0, filterMultiAttrDbDataList, null,
							newMultiAttrDataList, opType, count, userName);
				}
				for (Entry<Long, AttributeDbData> preAttributeDbData : mapAttributeDbData.entrySet()) {
					AttributeDbData multiAttrDbData = preAttributeDbData.getValue();
					String attrValue = AttributeHelper.convertMultiAttrToJsonString(multiAttrDbData);
					// Addition of multi-attribute.
					AttributeDbData dbData = new AttributeDbData();
					dbData.setAttrNameCde(multiAttrDbData.getAttrNameCde());
					dbData.setAttrValue(attrValue);
					dbData.setDocId(docId);
					if (entity == EnumEntityType.ATTACHMENT)
						dbData.setAttachmentId(id);
					dbData.setConfidencePct(WorkbenchConstants.CONFIDENCE_PCT_UNSET);
					if (opType == EnumAttrOperationType.DELETE) {
						dbData.setExtractTypeCde(EnumExtractType.MANUALLY_CORRECTED.getValue());
					} else
						dbData.setExtractTypeCde(EnumExtractType.DIRECT_COPY.getValue());
					if (preAttributeDbData.getKey() != null && preAttributeDbData.getKey() > 0) {
						dbData.setId(preAttributeDbData.getKey());
					}
					attrDataList.add(dbData);
				}
			}

			if (ListUtility.hasValue(filterNonMultiAttrDbDataList)) {
				updateUserDataToAttributeDbData(filterNonMultiAttrDbDataList, opType, userName);
				// Addition of Pre configured attributes.
				attrDataList.addAll(filterNonMultiAttrDbDataList);
				count.setCount(count.getCount() + filterNonMultiAttrDbDataList.size());
			}
		}
		return attrDataList;
	}

	private Map<Long, AttributeDbData> createOperationTypeAttributeDataList(Long key,
			List<AttributeDbData> attributeReqDatas, AttributeDbData multiAttrDbData,
			List<AttributeDbData> attrDataList, EnumAttrOperationType opType, Counter count, String userName)
			throws WorkbenchException {
		Map<Long, AttributeDbData> mapAttributeDbData = new HashMap<Long, AttributeDbData>();
		for (AttributeDbData attributeReqData : attributeReqDatas) {
			if (opType == EnumAttrOperationType.INSERT) {
				if (attributeReqData.getAttributes() != null) {
					updateUserDataToAttributeDbData(attributeReqData.getAttributes(), opType, userName);
				}
				createAttributeDataListToInsert(key, count, mapAttributeDbData, attributeReqData, multiAttrDbData);
			} else {
				if ((attributeReqData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
						|| attributeReqData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
						&& ListUtility.hasValue(attributeReqData.getAttributes())) {
					if (multiAttrDbData.getId() == attributeReqData.getId()
							&& multiAttrDbData.getAttrNameTxt().equals(attributeReqData.getAttrNameTxt())) {// Group
																											// should
																											// match
						for (AttributeDbData paramData : multiAttrDbData.getAttributes()) {// From DB
							for (AttributeDbData multiAttrData : attributeReqData.getAttributes()) {// From UI
								if (opType == EnumAttrOperationType.DELETE) {
									if (paramData.getId() == multiAttrData.getId() && paramData.getEndDtm() == null) {
										paramData.setEndDtm(DateUtility.toString(new Date(),
												WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
										paramData.setLastModByUserLoginId(userName);
										paramData.setLastModDtm(paramData.getEndDtm());
										count.setCount(count.getCount() + 1);
										break;
									}
								} else if (opType == EnumAttrOperationType.UPDATE) {
									if (paramData.getId() == multiAttrData.getId()
											&& paramData.getAttrNameTxt().equals(multiAttrData.getAttrNameTxt())) {
										paramData.setAttrValue(multiAttrData.getAttrValue());
										paramData.setConfidencePct(multiAttrData.getConfidencePct());
										paramData.setExtractTypeCde(multiAttrData.getExtractTypeCde());
										setLastModUserData(paramData, userName);
										if (ListUtility.hasValue(multiAttrData.getAttributes())) {
											getMultiLevelInnerAttributes(paramData.getAttributes(),
													multiAttrData.getAttributes(), opType,
													attributeReqData.getAttrNameCde());
										}
										count.setCount(count.getCount() + 1);
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		if (multiAttrDbData != null && multiAttrDbData.getAttachmentId() > 0) {
			mapAttributeDbData.put(generateNegativeKey(mapAttributeDbData), multiAttrDbData);
		}
		return mapAttributeDbData;
	}

	private void createAttributeDataListToInsert(Long key, Counter count, Map<Long, AttributeDbData> mapAttributeDbData,
			AttributeDbData attributeReqData, AttributeDbData multiAttrDbData) throws WorkbenchException {

		if (attributeReqData.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
				&& attributeReqData.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
			return;
		}

		if (multiAttrDbData != null && multiAttrDbData.getAttrNameCde() == attributeReqData.getAttrNameCde()
				&& multiAttrDbData.getAttrNameTxt().equals(attributeReqData.getAttrNameTxt())
				&& ListUtility.hasValue(multiAttrDbData.getAttributes())) {
			List<AttributeDbData> existAttributes = multiAttrDbData.getAttributes().stream()
					.filter(attrDbData -> attributeReqData.getAttributes().stream()
							.anyMatch(reqData -> isExistAttribute(attrDbData, reqData)))
					.collect(Collectors.toList());

			if (attributeReqData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
					&& existAttributes.size() > 0) {
				throw new WorkbenchException(WorkbenchConstants.DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS);
			} else {
				for (AttributeDbData attrData : attributeReqData.getAttributes()) {
					multiAttrDbData.getAttributes().add(attrData);
					count.setCount(count.getCount() + 1);
				}
			}
		} else {
			AttributeDbData multiAttrDbDataTemp = new AttributeDbData();
			BeanUtils.copyProperties(attributeReqData, multiAttrDbDataTemp);
			count.setCount(count.getCount() + 1);
			mapAttributeDbData.put(generateNegativeKey(mapAttributeDbData), multiAttrDbDataTemp);
		}
	}

	private boolean isExistAttribute(AttributeDbData dbData, AttributeDbData reqData) {
		return (dbData.getAttrNameTxt().equalsIgnoreCase(reqData.getAttrNameTxt()) && dbData.getEndDtm() == null);
	}

	private long generateNegativeKey(Map<Long, AttributeDbData> mapAttributeDbData) {
		int attrCount = mapAttributeDbData.size();
		return (attrCount == 0 ? attrCount : attrCount - (attrCount * 2));
	}

	private void getMultiLevelInnerAttributes(List<AttributeDbData> multiAttrDbDataList,
			List<AttributeDbData> attributeReqDataList, EnumAttrOperationType opType, int attrNameCde) {
		if (multiAttrDbDataList.size() < attributeReqDataList.size() && opType == EnumAttrOperationType.UPDATE
				&& attrNameCde == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
			for (AttributeDbData multiAttrData : attributeReqDataList) {// From UI

				List<AttributeDbData> paramDataList = multiAttrDbDataList.stream()
						.filter(multiAttrDbData -> multiAttrDbData.getId() == multiAttrData.getId())
						.collect(Collectors.toList());
				if (ListUtility.hasValue(paramDataList)) {
					AttributeDbData paramData = paramDataList.get(0);
					paramData.setAttrNameTxt(multiAttrData.getAttrNameTxt());
					setParamvalueToDBData(paramData, multiAttrData, opType, attrNameCde);
				} else {
					// At this stage New Column to the MultiAttribute Table would have been added.
					multiAttrDbDataList.add(multiAttrData);
				}

			}
		} else {
			for (AttributeDbData paramData : multiAttrDbDataList) {// From DB
				for (AttributeDbData multiAttrData : attributeReqDataList) {// From UI
					if (opType == EnumAttrOperationType.DELETE) {
						if (paramData.getId() == multiAttrData.getId() && paramData.getEndDtm() == null) {
							paramData.setEndDtm(
									DateUtility.toString(new Date(), WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
							break;
						}
					} else if (opType == EnumAttrOperationType.UPDATE) {
						if (attrNameCde == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
							if (paramData.getId() == multiAttrData.getId()) {
								paramData.setAttrNameTxt(multiAttrData.getAttrNameTxt()); // At this stage Tabular
																							// header
																							// name also can be changed
								setParamvalueToDBData(paramData, multiAttrData, opType, attrNameCde);
								break;
							}
						} else {
							if (paramData.getId() == multiAttrData.getId()
									&& paramData.getAttrNameTxt().equals(multiAttrData.getAttrNameTxt())) {
								setParamvalueToDBData(paramData, multiAttrData, opType, attrNameCde);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void setParamvalueToDBData(AttributeDbData paramData, AttributeDbData multiAttrData,
			EnumAttrOperationType opType, int attrNameCde) {
		paramData.setAttrValue(multiAttrData.getAttrValue());
		paramData.setConfidencePct(multiAttrData.getConfidencePct());
		paramData.setExtractTypeCde(multiAttrData.getExtractTypeCde());
		if (ListUtility.hasValue(multiAttrData.getAttributes())) {
			getMultiLevelInnerAttributes(paramData.getAttributes(), multiAttrData.getAttributes(), opType, attrNameCde);
		}
	}

	/**
	 * Method is to return an entity list with the populated data provided as
	 * inputs.
	 * 
	 * @param prevDocAttrRelIdList
	 * @param latestDocAttrRelIdList
	 * @param prevAttachAttrRelIdList
	 * @param latestAttachAttrRelIdList
	 * @param count
	 * @return
	 */
	@Override
	public List<EntityDbData> createEntityDataList(List<Long> prevDocAttrRelIdList, List<Long> latestDocAttrRelIdList,
			List<Long> prevAttachAttrRelIdList, List<Long> latestAttachAttrRelIdList, long count) {
		List<EntityDbData> docEntityDbDataList = new ArrayList<EntityDbData>();
		EntityDbData prevEntityDbData = new EntityDbData();
		EntityDbData latestEntityDbData = new EntityDbData();
		if ((ListUtility.hasValue(prevDocAttrRelIdList) && ListUtility.hasValue(latestDocAttrRelIdList))
				|| (ListUtility.hasValue(prevAttachAttrRelIdList) && ListUtility.hasValue(latestAttachAttrRelIdList))) {
			latestEntityDbData.setDocAttrRelIdList(latestDocAttrRelIdList);
			latestEntityDbData.setAttachAttrRelIdList(latestAttachAttrRelIdList);
			latestEntityDbData.setProcessedCount(count);
			docEntityDbDataList.add(latestEntityDbData);
			prevEntityDbData.setDocAttrRelIdList(prevDocAttrRelIdList);
			prevEntityDbData.setAttachAttrRelIdList(prevAttachAttrRelIdList);
			docEntityDbDataList.add(prevEntityDbData);
		}
		return docEntityDbDataList;
	}

	@Override
	public boolean isAttributeDeleteAllowed(AttributeDbData attributeDbData) {
		boolean isDeleteAllowed = false;
		if (StringUtility.hasValue(attributeDbData.getAttrValue())) {
			AttributeDbData data = AttributeHelper.convertJsonStringToMultiAttr(attributeDbData, null);
			if (ListUtility.hasValue(data.getAttributes())) {
				if (data.getAttributes().stream().filter(attrData -> attrData.getEndDtm() == null).count() > 0) {
					isDeleteAllowed = false;
				} else {
					isDeleteAllowed = true;
				}
			}
		}
		return isDeleteAllowed;
	}

	private void populateUserData(AttributeDbData attributeData) throws WorkbenchException {
		String createByKey = attributeData.getCreateByUserLoginId() + "_" + SessionHelper.getTenantId();
		AppUserDbData appCreateByUserDbData = userDataAccess.getUserData(createByKey);
		if (appCreateByUserDbData != null) {
			attributeData.setCreateByUserFullName(appCreateByUserDbData.getUserFullName());
			attributeData.setCreateByUserTypeCde((int) appCreateByUserDbData.getUserTypeCde());
			if (attributeData.getLastModByUserLoginId() != null) {
				String lastModByKey = attributeData.getLastModByUserLoginId() + "_" + SessionHelper.getTenantId();
				AppUserDbData appModByUserDbData = userDataAccess.getUserData(lastModByKey);
				if (appModByUserDbData != null) {
					attributeData.setLastModByUserFullName(appModByUserDbData.getUserFullName());
					attributeData.setLastModByUserTypeCde((int) appModByUserDbData.getUserTypeCde());
				}
			}
		}
	}

	private void updateUserDataToAttributeDbData(List<AttributeDbData> attributeDbDataList,
			EnumAttrOperationType opType, String userName) {
		for (AttributeDbData attributeDbData : attributeDbDataList) {
			if (opType == EnumAttrOperationType.INSERT) {
				setCreateByUserData(attributeDbData, userName);
			} else if (opType == EnumAttrOperationType.UPDATE || opType == EnumAttrOperationType.DELETE) {
				setLastModUserData(attributeDbData, userName);
			}
		}
	}

	private void setCreateByUserData(AttributeDbData attributeDbData, String userName) {
		attributeDbData.setCreateByUserLoginId(userName);
		attributeDbData.setCreateDtm(DateUtility.toString(new Date(), WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
	}

	private void setLastModUserData(AttributeDbData attributeDbData, String userName) {
		attributeDbData.setLastModByUserLoginId(userName);
		attributeDbData.setLastModDtm(DateUtility.toString(new Date(), WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
	}

}
