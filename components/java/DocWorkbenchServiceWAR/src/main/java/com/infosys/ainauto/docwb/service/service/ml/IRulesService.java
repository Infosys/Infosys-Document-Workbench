/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.service.ml;

import java.util.List;
import java.util.Map;

import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.RecommendedActionResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeNameResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.service.model.db.ActionTempMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

public interface IRulesService {

	public RecommendedActionResData getRecommendedAction(String tenantId, long docId, int docTypeCde,
			List<DocumentResData> actionParamAttrMappingDataList, List<AttributeDbData> attributeDbDataList,
			List<AttributeDbData> attachmentAttributeDbDataList);

	public List<ActionTempMappingDbData> getTemplates(String tenantId);

	public GetAttributeNotificationResData getAttributesNotification(String tenantId, long docId, int docTypeCde,
			List<AttributeDbData> attributeDbDataList, List<AttributeDbData> attachmentAttributeDbDataList);

	public List<ActionTempMappingDbData> getFlattenedTemplate(String tenantId, long docId, int docTypeCde,
			List<DocumentResData> actionParamAttrMappingDataList, List<AttributeDbData> attributeDbDataList,
			List<AttributeDbData> attachmentAttributeDbDataList);

	public List<AttributeNameResData> getAttributeAttributeMapping(String tenantId,
			Map<Integer, String> attributeDbDataMap);

	public List<AttributeNameResData> getAttributeSortingKey(String tenantId, Map<Integer, String> attributeDbDataMap);
}
