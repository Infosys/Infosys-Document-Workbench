/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

public class AttributeHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeHelper.class);

	public static DocumentData convertJsonStringToAttr(String textToProcess) {
		DocumentData documentData = new DocumentData();
		if (StringUtility.hasTrimmedValue(textToProcess)) {
			try {
				JsonReader reader = Json.createReader(new StringReader(textToProcess));
				JsonObject documentObj = reader.readObject();
				JsonObject document = documentObj.getJsonObject("document");
				JsonArray docJsonArray = null;
				JsonArray attachmentJsonArray = null;
				if (document.containsKey("attributes") && !document.isNull("attributes")) {
					docJsonArray = document.getJsonArray("attributes");
					List<AttributeData> dataList = getAttributes(docJsonArray);
					documentData.setAttributes(dataList);
				} else {
					documentData.setAttributes(null);
				}
				if (document.containsKey("attachments")) {
					attachmentJsonArray = document.getJsonArray("attachments");
					List<AttachmentData> attachmentDatas = new ArrayList<>();
					for (int i = 0; i < attachmentJsonArray.size(); i++) {
						AttachmentData attachmentData = new AttachmentData();
						JsonObject objectInArray = attachmentJsonArray.getJsonObject(i);
						attachmentData.setAttachmentId((long) objectInArray.getInt("attachmentId"));
						if (objectInArray.containsKey("attributes") && !objectInArray.isNull("attributes")) {
							List<AttributeData> dataList = getAttributes(objectInArray.getJsonArray("attributes"));
							attachmentData.setAttributes(dataList);
						}
						attachmentDatas.add(attachmentData);
					}
					documentData.setAttachmentDataList(attachmentDatas);
				} else {
					documentData.setAttachmentDataList(null);
				}

			} catch (Exception e) {
				LOGGER.error("Exception occured in convertJsonStringToAttr()", e);
			}
		}
		return documentData;

	}

	private static List<AttributeData> getAttributes(JsonArray jsonArray) {
		List<AttributeData> attrDataList = new ArrayList<>();
		if (jsonArray.size() > 0) {
			for (int i = 0, size = jsonArray.size(); i < size; i++) {
				AttributeData attrData = new AttributeData();
				JsonObject objectInArray = jsonArray.getJsonObject(i);
				attrData.setAttrNameCde(objectInArray.getInt("attrNameCde"));
				attrData.setId(objectInArray.getInt("id"));
				attrData.setAttrNameTxt(objectInArray.getString("attrNameTxt"));
				attrData.setConfidencePct(objectInArray.getInt("confidencePct"));
				if (objectInArray.getInt("attrNameCde") == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
						|| objectInArray.getInt("attrNameCde") == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
								.getCde()) {
					JsonArray nestedAttr = objectInArray.getJsonArray("attributes");
					List<AttributeData> nestedAttrDataList = new ArrayList<>();
					for (int j = 0; j < nestedAttr.size(); j++) {
						AttributeData nestedAttrData = new AttributeData();
						BeanUtils.copyProperties(nestedAttr.get(j), nestedAttrData);
						nestedAttrDataList.add(nestedAttrData);
					}
					if (ListUtility.hasValue(nestedAttrDataList)) {
						attrData.setAttributeDataList(nestedAttrDataList);
					}
				} else {
					attrData.setAttrValue(objectInArray.getString("attrValue"));
				}
				attrDataList.add(attrData);
			}
		}
		return attrDataList;
	}

	public static String convertAttrToJsonString(DocumentData processedDocumentData, DocumentData documentData,
			Map<Integer, String> attrNameMap, String userName) {
		List<AttributeData> docAttributes = new ArrayList<>();
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		List<AnnotationData> annotationDatas = new ArrayList<>();
		if (ListUtility.hasValue(processedDocumentData.getAttachmentDataList()))
			attachmentDataList = processedDocumentData.getAttachmentDataList();
		if (ListUtility.hasValue(processedDocumentData.getAttributes()))
			docAttributes = processedDocumentData.getAttributes();
		if (ListUtility.hasValue(processedDocumentData.getAnnotations()))
			annotationDatas = processedDocumentData.getAnnotations();
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonObjectBuilder document = Json.createObjectBuilder();
		JsonArrayBuilder attachmentBuilder = Json.createArrayBuilder();
		JsonArray attributes = null;
		JsonArray attachments = null;
		JsonArray annotations = null;
		if (ListUtility.hasValue(docAttributes)) {
			// Set<Integer> attrNameCdes =
			// attributeDataList.stream().map(AttributeData::getAttrNameCde)
			// .collect(Collectors.toSet());
			// // stream the list and use the set to filter it
			// List<AttributeData> dataList = docAttributes.stream()
			// .filter(e ->
			// attrNameCdes.contains(e.getAttrNameCde())).collect(Collectors.toList());
			attributes = getAttrJsonArray(docAttributes, attrNameMap);
		}
		if (ListUtility.hasValue(annotationDatas)) {
			annotations = AnnotationHelper.getAnnotations(annotationDatas);
		}
		if (ListUtility.hasValue(attachmentDataList) && ListUtility.hasValue(documentData.getAttachmentDataList())) {
			for (int i = 0; i < attachmentDataList.size(); i++) {
				long attachmentId = attachmentDataList.get(i).getAttachmentId();
				if (documentData.getAttachmentDataList().stream().filter(data -> data.getAttachmentId() == attachmentId)
						.count() > 0) {
					JsonObjectBuilder attachment = Json.createObjectBuilder();
					attachment.add(DocwbWebConstants.ATTACHMENT_ID, attachmentId);
					JsonArray attachAttr = getAttrJsonArray(attachmentDataList.get(i).getAttributes(), attrNameMap);
					if (attachAttr != null)
						attachment.add(DocwbWebConstants.ATTRIBUTES, attachAttr);
					JsonArray attachAnn = AnnotationHelper.getAnnotations(attachmentDataList.get(i).getAnnotations());
					if (attachAnn != null)
						attachment.add(DocwbWebConstants.ANNOTATIONS, attachAnn);
					attachmentBuilder.add(attachment.build());
				}
			}
			attachments = attachmentBuilder.build();
		}
		if (attributes != null)
			document.add(DocwbWebConstants.ATTRIBUTES, attributes);
		else
			document.add(DocwbWebConstants.ATTRIBUTES, Json.createArrayBuilder().build());
		if (annotations != null)
			document.add(DocwbWebConstants.ANNOTATIONS, annotations);
		if (attachments != null)
			document.add(DocwbWebConstants.ATTACHMENTS, attachments);
		else
			document.add(DocwbWebConstants.ATTACHMENTS, Json.createArrayBuilder().build());
		document.add("createBy", userName);
		builder.add("document", document.build());
		JsonObject response = builder.build();
		String result = response.toString();
		return result;
	}

	private static JsonArray getAttrJsonArray(List<AttributeData> attributeDataList, Map<Integer, String> attrNameMap) {
		JsonArrayBuilder attributeBuilder = Json.createArrayBuilder();
		for (AttributeData attributeData : attributeDataList) {
			if (attributeData != null) {
				JsonObjectBuilder attribute = Json.createObjectBuilder();
				if (attributeData.getAttrNameTxt() != null) {
					attribute.add(DocwbWebConstants.ATTR_NAME_TXT, attributeData.getAttrNameTxt());
				} else {
					attribute.add(DocwbWebConstants.ATTR_NAME_TXT, attrNameMap.get(attributeData.getAttrNameCde()));
				}
				attribute.add(DocwbWebConstants.ATTR_NAME_CDE, attributeData.getAttrNameCde());
				if (attributeData.getAttrValue() != null)
					attribute.add(DocwbWebConstants.ATTR_VALUE,
							StringUtility.hasTrimmedValue(attributeData.getAttrValue()) ? attributeData.getAttrValue()
									: "");

				attribute.add("attributes", getAttributes(attributeData.getAttributeDataList()));
				attribute.add(DocwbWebConstants.CONFIDENCE_PCT, Math.floor(attributeData.getConfidencePct()));
				attributeBuilder.add(attribute);
			}
		}
		JsonArray attributes = attributeBuilder.build();
		return attributes;

	}

	public static JsonArray getAttributes(List<AttributeData> attributes) {
		JsonArrayBuilder attrBuilder = Json.createArrayBuilder();
		if (ListUtility.hasValue(attributes)) {
			for (AttributeData data : attributes) {
				JsonObjectBuilder attrData = Json.createObjectBuilder();
				attrData.add(DocwbWebConstants.ATTR_NAME_TXT, data.getAttrNameTxt());
				attrData.add(DocwbWebConstants.ATTR_VALUE,
						StringUtility.hasTrimmedValue(data.getAttrValue()) ? data.getAttrValue() : "");
				attrData.add(DocwbWebConstants.CONFIDENCE_PCT, data.getConfidencePct());
				attrData.add(DocwbWebConstants.ATTRIBUTES, getAttributes(data.getAttributeDataList()));
				attrBuilder.add(attrData);
			}
		}

		return attrBuilder.build();
	}

	public static void removeNullAttributes(DocumentData documentData) {
		// Remove null items from list
		if (documentData.getAttributes() != null)
			documentData.getAttributes().removeIf(a -> a == null);

		if (ListUtility.hasValue(documentData.getAttachmentDataList())) {
			documentData.getAttachmentDataList().removeIf(a -> a == null);
			for (AttachmentData attachmentData : documentData.getAttachmentDataList()) {
				if (attachmentData.getAttributes() != null)
					attachmentData.getAttributes().removeIf(a -> a == null);
			}
		}
	}

	/**
	 * This method helps to update existing attributes with its newer values created
	 * as result of running rules in re-extraction flow e.g. Category value already
	 * exists with say value 'X' and upon running rules, it get's a new value 'Y'.
	 * So, Y will not be added as another attribute. Instead, existing attribute
	 * value will be updated.
	 * 
	 * @param toBeUpdatedDocumentData
	 * @param docAttributeList
	 * @param attachmentList
	 */
	public static void refreshAttributesWithNewerValues(DocumentData toBeUpdatedDocumentData,
			List<AttributeData> newAttributeDataList, List<AttachmentData> newAttachmentDataList) {

		// STEP 1 - Remove all matching attributes

		// Remove old attributes if their new counterparts exist at document level
		removeAttributesFromList(toBeUpdatedDocumentData.getAttributes(), newAttributeDataList);

		// Remove old attributes if their new counterparts exist at attachment level
		if (ListUtility.hasValue(toBeUpdatedDocumentData.getAttachmentDataList())
				&& ListUtility.hasValue(newAttachmentDataList)) {
			for (AttachmentData attachmentData : toBeUpdatedDocumentData.getAttachmentDataList()) {
				if (attachmentData != null) {
					Optional<AttachmentData> optionalForNewAttachmentData = newAttachmentDataList.stream()
							.filter(a -> a.getAttachmentId() == attachmentData.getAttachmentId()).findAny();

					if (optionalForNewAttachmentData.isPresent()) {
						AttachmentData newAttachmentData = optionalForNewAttachmentData.get();
						if (newAttachmentData != null) {
							// Remove old items if their new counterparts exist at attachment level
							removeAttributesFromList(attachmentData.getAttributes(), newAttachmentData.getAttributes());
						}
					}
				}
			}
		}

		// STEP 2 - Add new version of matching attributes (that were removed earlier)

		// Add new attributes at document level
		if (ListUtility.hasValue(newAttributeDataList)) {

			// Create empty attribute list if none exists
			if (!ListUtility.hasValue(toBeUpdatedDocumentData.getAttributes())) {
				toBeUpdatedDocumentData.setAttributes(new ArrayList<AttributeData>());
			}

			// Add new attribute list to document
			toBeUpdatedDocumentData.getAttributes().addAll(newAttributeDataList);
		}

		// Add new attributes at attachment level
		if (ListUtility.hasValue(newAttachmentDataList)) {

			// If attachment list is empty, assign directly and exit
			if (!ListUtility.hasValue(toBeUpdatedDocumentData.getAttachmentDataList())) {
				toBeUpdatedDocumentData.setAttachmentDataList(newAttachmentDataList);
			} else {
				// Loop through RHS first
				for (AttachmentData newAttachmentData : newAttachmentDataList) {
					Optional<AttachmentData> optionalForAttachmentData = toBeUpdatedDocumentData.getAttachmentDataList()
							.stream().filter(a -> a.getAttachmentId() == newAttachmentData.getAttachmentId()).findAny();

					// LHS attachment already exists so edit that
					if (optionalForAttachmentData.isPresent()) {
						AttachmentData attachmentData = optionalForAttachmentData.get();
						// Create empty attribute list if none exists
						if (!ListUtility.hasValue(attachmentData.getAttributes())) {
							attachmentData.setAttributes(new ArrayList<AttributeData>());
						}
						attachmentData.getAttributes().addAll(newAttachmentData.getAttributes());
					} else {
						// RHS attachment doesn't exist so add directly
						toBeUpdatedDocumentData.getAttachmentDataList().add(newAttachmentData);
					}
				}
			}
		}

	}

	private static void removeAttributesFromList(List<AttributeData> toBeModifiedAttributeDataList,
			List<AttributeData> lookupAttributeDataList) {

		// Proceed only if both lists contain items
		if (ListUtility.hasValue(toBeModifiedAttributeDataList) && ListUtility.hasValue(lookupAttributeDataList)) {
			for (AttributeData newAttributeData : lookupAttributeDataList) {
				// For non multi-attribute, remove if code value matches
				toBeModifiedAttributeDataList
						.removeIf(a -> a.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
								&& a.getAttrNameCde() == newAttributeData.getAttrNameCde());

				// For multi-attribute, remove if text value (i.e. group name) matches
				toBeModifiedAttributeDataList
						.removeIf(a -> a.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
								&& a.getAttrNameTxt() == newAttributeData.getAttrNameTxt());
			}
		}
	}
}
