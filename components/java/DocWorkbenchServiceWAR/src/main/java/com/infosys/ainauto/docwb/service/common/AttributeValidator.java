/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.annotation.ExportIOBReqData;
import com.infosys.ainauto.docwb.service.model.api.annotation.InsertAnnotationReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.InsertAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ManageMultiAttributeReqData;

@Component
public class AttributeValidator extends BaseController {

	public ResponseEntity<String> validateAddAttributeRequest(List<InsertAttributeReqData> requestDataList)
			throws JsonProcessingException {
		for (InsertAttributeReqData data : requestDataList) {
			if (data.getDocId() <= 0) {
				String message = "Please provide document Id.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (data.getAttributes() != null) {
				for (InsertAttributeReqData.InsertAttributeData attrData : data.getAttributes()) {
					if ((attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
							&& !(ListUtility.hasValue(attrData.getAttributes()))) {
						String validationMessage = "For a multi-attribute, sub attributes should be populated";
						return jsonResponseOk(getStringApiResponseData(validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
				}
			}
			if (data.getAttachments() != null) {
				for (InsertAttributeReqData.InsertAttachmentAttrData attachmentData : data.getAttachments()) {
					for (InsertAttributeReqData.InsertAttributeData attrData : attachmentData.getAttributes()) {
						if ((attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
								&& !(ListUtility.hasValue(attrData.getAttributes()))) {
							String validationMessage = "For a multi-attribute, sub attributes should be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
						if ((attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
								&& !(ListUtility.hasValue(attrData.getAttributes()))) {
							String validationMessage = "For a Multi-Attribute Table attribute, sub attributes should be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
					}
				}
			}
		}
		return null;
	}

	public ResponseEntity<String> validateManageAttributeReq(ManageMultiAttributeReqData requestData)
			throws JsonProcessingException {
		if (requestData.getDocId() <= 0) {
			String message = "Please provide document Id.";
			return jsonResponseOk(
					getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
		}
		if (requestData.getAttribute() != null && requestData.getAttachment() != null) {
			String message = "Both Document and Attachment attributes are not allowed. Please provide either one of it. ";
			return jsonResponseOk(
					getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
		}
		if (requestData.getAttribute() != null
				&& (requestData.getAttribute().getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
						|| requestData.getAttribute().getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
								.getCde())
				&& (!ListUtility.hasValue(requestData.getAttribute().getAddAttributes())
						&& !ListUtility.hasValue(requestData.getAttribute().getDeleteAttributes())
						&& !ListUtility.hasValue(requestData.getAttribute().getEditAttributes()))) {
			String validationMessage = "For a multi-attribute, sub attributes should be populated";
			return jsonResponseOk(getStringApiResponseData(validationMessage,
					WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
					WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
		}

		if (requestData.getAttachment() != null && requestData.getAttachment().getAttribute() != null
				&& (requestData.getAttachment().getAttribute()
						.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
						|| requestData.getAttachment().getAttribute()
								.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
				&& (!ListUtility.hasValue(requestData.getAttachment().getAttribute().getAddAttributes())
						&& !ListUtility.hasValue(requestData.getAttachment().getAttribute().getDeleteAttributes())
						&& !ListUtility.hasValue(requestData.getAttachment().getAttribute().getEditAttributes()))) {
			String validationMessage = "For a multi-attribute, sub attributes should be populated";
			return jsonResponseOk(getStringApiResponseData(validationMessage,
					WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
					WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
		}

		return null;

	}

	public ResponseEntity<String> validateAddAnnotationRequest(List<InsertAnnotationReqData> requestDataList)
			throws JsonProcessingException {
		for (InsertAnnotationReqData data : requestDataList) {
			if (data.getDocId() <= 0) {
				String message = "Please provide document Id.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (data.getAnnotations() != null) {
				for (InsertAnnotationReqData.AnnotationReqData annData : data.getAnnotations()) {
					if (!StringUtility.hasTrimmedValue(annData.getLabel()) || isAnnotationAttrValueRequired(annData)) {
						String validationMessage = "For an annotation, label and value should be populated";
						return jsonResponseOk(getStringApiResponseData(validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
				}
			}
			if (data.getAttachments() != null) {
				for (InsertAnnotationReqData.AttachmentAnnotationData attachmentData : data.getAttachments()) {
					if (attachmentData.getAttachmentId() <= 0) {
						String message = "Please provide attachment Id.";
						return jsonResponseOk(getStringApiResponseData(message,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
					for (InsertAnnotationReqData.AnnotationReqData annData : attachmentData.getAnnotations()) {
						if (!StringUtility.hasTrimmedValue(annData.getLabel())
								|| isAnnotationAttrValueRequired(annData)) {
							String validationMessage = "For an annotation, label and value should be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
					}
				}
			}
		}
		return null;
	}

	private boolean isAnnotationAttrValueRequired(InsertAnnotationReqData.AnnotationReqData annData) {
		return (!StringUtility.hasTrimmedValue(annData.getValue()) && (annData.getOccurrenceNum() < 0
				|| annData.getSourceBbox() == null || annData.getSourceBbox().size() != 4));
	}

	public String validateExportIobRequest(ExportIOBReqData requestData) throws JsonProcessingException {
		String validationMessage = "";
		String start = requestData.getCreateDtm().getStart();
		if (StringUtility.hasTrimmedValue(start)) {
			String format = "yyyy-MM-dd";
			Date startDate = this.convertDateFormat(start, format);
			if (startDate != null) {
				String end = requestData.getCreateDtm().getEnd();
				if (StringUtility.hasTrimmedValue(end)) {
					Date endDate = this.convertDateFormat(end, format);
					if (endDate != null) {
						if (startDate.before(endDate)) {
							if (ListUtility.hasValue(requestData.getAttributes())
									&& ListUtility.hasValue(requestData.getAttachments())) {
								validationMessage = "Both Email and Document level annotation extract are not allowed. Please provide either one of it";
							} else if (ListUtility.hasValue(requestData.getAttributes())) {
								validationMessage = isExportIOBReqAttributeValid(requestData.getAttributes().get(0));
							} else if (ListUtility.hasValue(requestData.getAttachments())
									&& ListUtility.hasValue(requestData.getAttachments().get(0).getAttributes())) {
								validationMessage = isExportIOBReqAttributeValid(
										requestData.getAttachments().get(0).getAttributes().get(0));
							}
						} else {
							validationMessage = "Start time for create Date Time must be lesser than End time";
						}
					} else {
						validationMessage = "End time for create Date Time is invalid format";
					}
				} else {
					validationMessage = "End time for create Date Time is mandatory";
				}

			} else {
				validationMessage = "Start time for create Date Time is invalid format";
			}
		} else {
			validationMessage = "Start time for create Date Time is mandatory";
		}
		return validationMessage;
	}

	private Date convertDateFormat(String dateStr, String dateFormat) {
		Date convertedDate = null;
		try {
			convertedDate = new SimpleDateFormat(dateFormat).parse(dateStr);
		} catch (ParseException e) {
		}
		return convertedDate;
	}

	private String isExportIOBReqAttributeValid(ExportIOBReqData.ExportIOBAttributeData requestData) {
		String validationMessage = null;
		if (!(requestData.getAttrNameCde() > 0)) {
			validationMessage = "Attribute name code is invalid";
		} else if (!StringUtility.hasTrimmedValue(requestData.getAttrValue())) {
			validationMessage = "Attribute Value is mandatory";
		}
		return validationMessage;
	}
}