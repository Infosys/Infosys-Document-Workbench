/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;

public class AnnotationService extends HttpClientBase implements IAnnotationService {

	private static final String PROP_NAME_API_ANNOTATION_ADD_URL = "docwb.api.ann.add.url";

	// Protected constructor to avoid instantiation by outside world
	protected AnnotationService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig) {
		super(null, bearerAuthConfig);
	}

	public void addAnnotation(DocumentData documentData) {
		List<AnnotationData> annotationDataList = documentData.getAnnotations();
		boolean isDataPresent = true;
		JsonArrayBuilder jsonRequestBuilder = Json.createArrayBuilder();
		JsonObjectBuilder documentBuilder = Json.createObjectBuilder();
		documentBuilder.add(DocwbWebConstants.DOC_ID, documentData.getDocId());
		JsonArray annotationArray = buildAnnotationArray(annotationDataList);
		documentBuilder.add(DocwbWebConstants.ANNOTATIONS, annotationArray);
		isDataPresent = !annotationArray.isEmpty();

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
					annotationArray = buildAnnotationArray(attachmentData.getAnnotations());
					objectBuilder.add(DocwbWebConstants.ANNOTATIONS, annotationArray);
					if (!isDataPresent) {
						isDataPresent = !annotationArray.isEmpty();
					}
					attachmentArrayBuilder.add(objectBuilder);
				}
			}

		}

		// JsonArray attachments = Json.createArrayBuilder().build();
		JsonArray attachments = attachmentArrayBuilder.build();
		documentBuilder.add(DocwbWebConstants.ATTACHMENTS, attachments);

		jsonRequestBuilder.add(documentBuilder);
		JsonArray jsonRequest = jsonRequestBuilder.build();
		if (isDataPresent) {
			executeHttpCall(HttpCallType.POST,
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ANNOTATION_ADD_URL), jsonRequest);
		}

	}

	private JsonArray buildAnnotationArray(List<AnnotationData> annotationDatas) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		if (annotationDatas != null)
			annotationDatas.removeIf(a -> a == null);

		if (ListUtility.hasValue(annotationDatas)) {

			for (AnnotationData annotationData : annotationDatas) {
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				objectBuilder.add(DocwbWebConstants.LABEL, annotationData.getLabel());
				objectBuilder.add(DocwbWebConstants.VALUE, annotationData.getValue());
				objectBuilder.add(DocwbWebConstants.OCCURRENCENUM, annotationData.getOccurrenceNum());
				arrayBuilder.add(objectBuilder);
			}
		}
		return arrayBuilder.build();
	}
}
