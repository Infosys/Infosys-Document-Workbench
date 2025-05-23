/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.attribute;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumAttrOperationType;
import com.infosys.ainauto.docwb.service.model.api.AttributeNameValueResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeNameResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeSourceReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.DeleteAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ExportAttributeResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.InsertAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.UpdateAttributeReqData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.process.Counter;

public interface IAttributeProcess {

	public List<EntityDbData> addAttribute(List<InsertAttributeReqData> insertAttributeReqDataList)
			throws WorkbenchException;

	public ExportAttributeResData exportAllAttributes(long docId) throws WorkbenchException;

	public List<AttributeDbData> getDocumentAttributes(long docId) throws WorkbenchException;

	public List<AttributeDbData> getAttachmentAttributes(long docId, String attachmentIds,boolean origValue) throws WorkbenchException;

	public List<EntityDbData> updateAttribute(List<UpdateAttributeReqData> updateAttributeReqDataList)
			throws WorkbenchException;

	public List<EntityDbData> deleteAttribute(List<DeleteAttributeReqData> requestDataList) throws WorkbenchException;

	public List<AttributeDbData> getAttributeText() throws WorkbenchException;

	public List<AttributeNameValueResData> getAttributeNameValues(String attrNameCdes) throws WorkbenchException;

	public GetAttributeNotificationResData getAttributesNotification(long docId) throws WorkbenchException;

	public <T> List<AttributeDbData> convertReqDataToAttributeList(Object object, long docId);

	public List<AttributeDbData> convertToGroupedAttributeList(long docId, List<AttributeDbData> attributeDbDatas,
			EnumAttrOperationType opType, Counter count, long docActionRelId, Map<Long, String> docActionRelMap)
			throws WorkbenchException;

	public List<EntityDbData> createEntityDataList(List<Long> prevDocAttrRelIdList, List<Long> latestDocAttrRelIdList,
			List<Long> prevAttachAttrRelIdList, List<Long> latestAttachAttrRelIdList, long count);

	/**
	 * This method checks if there's at least one active child attribute within the
	 * provided multi-attribute to conclude that attribute should <b>not</b> be
	 * deleted.
	 * 
	 * @param attributeDbData
	 * @return
	 */
	public boolean isAttributeDeleteAllowed(AttributeDbData attributeDbData);

	public List<AttributeNameResData> getAttributeAttributeMapping() throws WorkbenchException;

	public List<AttributeNameResData> getAttributeSortingKey() throws WorkbenchException;

	public List<EntityDbData> addAttributeSource(AttributeSourceReqData attributeSourceReqData)
			throws WorkbenchException;

}
