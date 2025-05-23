/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.rules.model.domain.ActionData;
import com.infosys.ainauto.docwb.rules.model.domain.ParamData;
import com.infosys.ainauto.docwb.rules.model.domain.AttachmentData;
import com.infosys.ainauto.docwb.rules.model.domain.AttributeData;

public class DocumentDataHelper {
	public static <T> List<AttributeData> getAttributes(List<T> attributes) {
		List<AttributeData> attributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributes)) {
			for (T grtAttributeData : attributes) {
				AttributeData attributeData = new AttributeData();
				BeanUtils.copyProperties(grtAttributeData, attributeData);
				attributeData.setAttributes(getAttributes(attributeData.getAttributes()));
				attributeDataList.add(attributeData);
			}
		}

		return attributeDataList;
	}

	public static <T> List<ActionData> getActionList(List<T> actionDataListParam) {
		List<ActionData> actionDataList = new ArrayList<>();
		if (ListUtility.hasValue(actionDataListParam)) {
			for (T grtActionData : actionDataListParam) {
				ActionData actionData = new ActionData();
				BeanUtils.copyProperties(grtActionData, actionData);
				actionData.setParamList(getParamList(actionData.getParamList()));
				actionDataList.add(actionData);
			}
		}
		return actionDataList;
	}

	public static <T> List<AttachmentData> getAttachments(List<T> attachments) {
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		if (ListUtility.hasValue(attachments)) {
			for (T grtAttachmentData : attachments) {
				AttachmentData attachmentData = new AttachmentData();
				BeanUtils.copyProperties(grtAttachmentData, attachmentData);
				attachmentData.setAttributes(getAttributes(attachmentData.getAttributes()));
				attachmentDataList.add(attachmentData);
			}
		}
		return attachmentDataList;
	}

	public static <T> List<ParamData> getParamList(List<T> reqParamDataList) {
		List<ParamData> paramDataList = new ArrayList<>();
		if (ListUtility.hasValue(reqParamDataList)) {
			for (T reqParamData : reqParamDataList) {
				ParamData paramData = new ParamData();
				BeanUtils.copyProperties(reqParamData, paramData);
				paramDataList.add(paramData);
			}
		}
		return paramDataList;
	}
}
