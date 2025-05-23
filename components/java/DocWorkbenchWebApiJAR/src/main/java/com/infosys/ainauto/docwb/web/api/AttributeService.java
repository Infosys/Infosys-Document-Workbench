/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;

public class AttributeService extends HttpClientBase implements IAttributeService {

	private static Logger logger = LoggerFactory.getLogger(AttributeService.class);

	private static final String PROP_NAME_API_ATTRIBUTE_URL = "docwb.api.doc.attr.url";
	private static final String PROP_NAME_API_ATTACH_ATTRIBUTE_URL = "docwb.api.attach.attr.url";
	private static final String PROP_NAME_API_ATTRIBUTE_ADD_URL = "docwb.api.attr.add.url";
	private static final String PROP_NAME_API_ATTRIBUTE_NAME_URL = "docwb.api.attr.name.url";
	private static final String PROP_NAME_API_ATTRIBUTE_EXPORT_URL = "docwb.api.attr.export.url";

	// Protected constructor to avoid instantiation by outside world
	protected AttributeService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig) {
		super(null, bearerAuthConfig);
	}

	public List<AttributeData> getDocAttributeList(Long docId) {
		List<AttributeData> attributeDataList = null;
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ATTRIBUTE_URL));
			if (docId > 0)
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));

			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getAttributeList", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null) {
			JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
			attributeDataList = AttributeHelper.getAttributes(jsonResponseArray);
		}
		return attributeDataList;
	}

	public List<AttachmentData> getAttachmentAttributeList(Long docId) {
		List<AttachmentData> attachmentAttrDatas = new ArrayList<>();
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ATTACH_ATTRIBUTE_URL));
			if (docId > 0)
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));

			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getAttributeList", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null) {
			JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
			for (int i = 0; i < jsonResponseArray.size(); i++) {
				AttachmentData attachmentData = new AttachmentData();
				JsonObject object = jsonResponseArray.getJsonObject(i);
				attachmentData.setAttachmentId(object.getInt(DocwbWebConstants.ATTACHMENT_ID));
				if (object.containsKey(DocwbWebConstants.ATTRIBUTES)) {
					attachmentData.setAttributes(
							AttributeHelper.getAttributes(object.getJsonArray(DocwbWebConstants.ATTRIBUTES)));
				}
				attachmentAttrDatas.add(attachmentData);
			}
		}
		return attachmentAttrDatas;
	}

	public Map<Integer, String> getAttributeNames() {
		Map<Integer, String> attributeNameMap = new HashMap<Integer, String>();
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ATTRIBUTE_NAME_URL));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getAttributeList", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null) {
			JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
			for (int i = 0; i < jsonResponseArray.size(); i++) {
				JsonObject object = jsonResponseArray.getJsonObject(i);
				attributeNameMap.put(object.getInt(DocwbWebConstants.ATTR_NAME_CDE),
						object.getString(DocwbWebConstants.ATTR_NAME_TXT));
			}
		}
		return attributeNameMap;
	}

	public String getAttributesToExport(Long docId) {
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ATTRIBUTE_EXPORT_URL));
			if (docId > 0)
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));

			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in exportAttributes", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null) {
			return jsonResponse.get(DocwbWebConstants.RESPONSE).toString();
		}
		return null;
	}

	public void addAttributes(DocumentData documentData) {
		List<AttributeData> attributeDataList = documentData.getAttributes();

		JsonArrayBuilder jsonRequestBuilder = Json.createArrayBuilder();
		JsonObjectBuilder documentBuilder = Json.createObjectBuilder();
		documentBuilder.add(DocwbWebConstants.DOC_ID, documentData.getDocId());

		documentBuilder.add(DocwbWebConstants.ATTRIBUTES, buildAttributeArray(attributeDataList));

		// Handle attachments
		JsonArrayBuilder attachmentArrayBuilder = Json.createArrayBuilder();
		{
			List<AttachmentData> attachmentDataList = documentData.getAttachmentDataList();
			if (attachmentDataList != null)
				attachmentDataList.removeIf(a -> a == null);

			if (ListUtility.hasValue(attachmentDataList)) {
				for (AttachmentData attachmentData : attachmentDataList) {
					JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
					objectBuilder.add(DocwbWebConstants.ATTACHMENT_ID, attachmentData.getAttachmentId());
					objectBuilder.add(DocwbWebConstants.ATTRIBUTES,
							buildAttributeArray(attachmentData.getAttributes()));
					attachmentArrayBuilder.add(objectBuilder);
				}
			}

		}

		// JsonArray attachments = Json.createArrayBuilder().build();
		JsonArray attachments = attachmentArrayBuilder.build();
		documentBuilder.add(DocwbWebConstants.ATTACHMENTS, attachments);

		jsonRequestBuilder.add(documentBuilder);
		JsonArray jsonRequest = jsonRequestBuilder.build();
		executeHttpCall(HttpCallType.POST, PropertyManager.getInstance().getProperty(PROP_NAME_API_ATTRIBUTE_ADD_URL),
				jsonRequest);

	}

	private JsonArray buildAttributeArray(List<AttributeData> attributeDataList) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		if (attributeDataList != null)
			attributeDataList.removeIf(a -> a == null);

		if (ListUtility.hasValue(attributeDataList)) {

			for (AttributeData attributeData : attributeDataList) {
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				if (attributeData.getAttrNameTxt() != null) {
					objectBuilder.add(DocwbWebConstants.ATTR_NAME_TXT, attributeData.getAttrNameTxt());
				}
				if (ListUtility.hasValue(attributeData.getAttributeDataList())) {
					objectBuilder.add(DocwbWebConstants.ATTRIBUTES,
							buildAttributeArray(attributeData.getAttributeDataList()));
				}
				objectBuilder.add(DocwbWebConstants.ATTR_NAME_CDE, attributeData.getAttrNameCde());
				if (attributeData.getAttrValue() != null && attributeData.getAttrValue().length() > 0) {
					objectBuilder.add(DocwbWebConstants.ATTR_VALUE, attributeData.getAttrValue());
				}
				objectBuilder.add(DocwbWebConstants.EXTRACT_TYPE_CDE, attributeData.getExtractType().getValue());
				objectBuilder.add(DocwbWebConstants.CONFIDENCE_PCT, attributeData.getConfidencePct());
				arrayBuilder.add(objectBuilder);
			}
		}
		return arrayBuilder.build();
	}
}
