/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumFileType;

public class AttachmentService extends HttpClientBase implements IAttachmentService {

	private static final String PROP_NAME_API_DOC_ATTACHMENT = "docwb.api.doc.attachment.url";
	private static final String PROP_NAME_API_DOC_ATTACHMENT_GROUP = "docwb.api.doc.attachment.group.url";
	private static final String PROP_NAME_API_DOC_ATTACHMENT_FILE = "docwb.api.doc.attachment.file.url";
	private String attachmentApiUrl = "";
	private String attachmentFileApiUrl = "";
	private String attachmentGroupApiUrl = "";
	private static final int MAX_FILE_COUNT_ALLOWED = 5;
	private final static String FILENAME_DELIMITER = "|";

	// Protected constructor to avoid instantiation by outside world
	protected AttachmentService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig) {
		super(null, bearerAuthConfig);
		attachmentApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DOC_ATTACHMENT);
		attachmentFileApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DOC_ATTACHMENT_FILE);
		attachmentGroupApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DOC_ATTACHMENT_GROUP);
	}

	/**
	 * Method adds attachments and inline images to a case.
	 * 
	 * @param documentData
	 * @param isInlineImage
	 * @return
	 * @throws DocwbWebException
	 */
	public long addUngroupedAttachment(DocumentData documentData, boolean isInlineImage) throws DocwbWebException {
		long docId = documentData.getDocId();
		String addAttachmentUrl = attachmentApiUrl + "?isInlineImage=" + isInlineImage + "&docId=" + docId;
		int responseCde = addAttachmentToDb(documentData, isInlineImage, addAttachmentUrl);
		return responseCde;
	}

	/**
	 * Method adds attachments and inline images to a specified group of the case.
	 * 
	 * @param documentData
	 * @param isInlineImage
	 * @param groupName
	 * @return
	 * @throws DocwbWebException
	 */
	public long addAttachmentToGroup(DocumentData documentData, boolean isInlineImage, String groupName)
			throws DocwbWebException {
		long docId = documentData.getDocId();
		String addAttachmentUrl = attachmentApiUrl + "?isInlineImage=" + isInlineImage + "&docId=" + docId;
		if (StringUtility.hasValue(groupName)) {
			addAttachmentUrl = addAttachmentUrl + "&groupName=" + groupName;
		}
		int responseCde = addAttachmentToDb(documentData, isInlineImage, addAttachmentUrl);
		return responseCde;
	}

	/**
	 * Method adds inline images (or/and) attachments together to a new group on the
	 * case.
	 * 
	 * @param documentData
	 * @param isInlineImage
	 * @return
	 * @throws DocwbWebException
	 */
	public long addGroupedAttachments(DocumentData documentData, boolean isInlineImage) throws DocwbWebException {
		long docId = documentData.getDocId();
		String addAttachmentUrl = attachmentGroupApiUrl + "?isInlineImage=" + isInlineImage + "&docId=" + docId;
		int responseCde = 0;
		List<AttachmentData> attachmentDataList = new ArrayList<AttachmentData>();
		if (isInlineImage) {
			attachmentDataList = documentData.getInlineAttachmentDataList();
		} else {
			attachmentDataList = documentData.getAttachmentDataList();
			attachmentDataList.forEach(attachmentData -> attachmentData.setLogicalName(
					attachmentData.getLogicalName().replace(DocwbWebConstants.FILE_SEPARATOR, FILENAME_DELIMITER)));
		}
		int totalAttachmentCount = attachmentDataList.size();

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		if (ListUtility.hasValue(attachmentDataList)) {
			String groupName = "";
			int size = MAX_FILE_COUNT_ALLOWED > totalAttachmentCount ? totalAttachmentCount : MAX_FILE_COUNT_ALLOWED;
			for (int i = 0; i < size; i++) {
				addAttachmentUrl = addAttachmentUrl + "&extractTypeCde" + (i + 1) + "="
						+ attachmentDataList.get(i).getExtractTypeCde();
				httpFileDataList.add(new HttpFileRequestData(attachmentDataList.get(i).getLogicalName(),
						attachmentDataList.get(i).getPhysicalPath(), "file" + (i + 1), null));
			}
			JsonObject jsonResponse = executePostAttachmentWithAuthCall(addAttachmentUrl, httpFileDataList)
					.getResponse();
			if (jsonResponse == null) {
				throw new DocwbWebException("Error occurred while adding attachments");
			}
			if (jsonResponse != null) {
				responseCde = jsonResponse.getInt("responseCde");
				if (responseCde != 0) {
					throw new DocwbWebException("Error occurred while adding attachments : "
							+ jsonResponse.getString(DocwbWebConstants.RESPONSE));
				} else {
					JsonArray jsonResponseArry = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
					if (jsonResponseArry != null && !jsonResponseArry.isEmpty()) {
						JsonObject jsonAttachmentObj = jsonResponseArry.getJsonObject(0);
						groupName = jsonAttachmentObj.getString("groupName");
						if (StringUtility.hasValue(groupName) && totalAttachmentCount > MAX_FILE_COUNT_ALLOWED) {
							documentData.setAttachmentDataList(documentData.getAttachmentDataList()
									.subList(MAX_FILE_COUNT_ALLOWED, totalAttachmentCount));
							responseCde = (int) addAttachmentToGroup(documentData, isInlineImage, groupName);
						}
					}
				}
			}

		}
		return responseCde;
	}

	/**
	 * @param documentData
	 * @param isInlineImage
	 * @param addAttachmentUrl
	 * @return
	 * @throws DocwbWebException
	 */
	private int addAttachmentToDb(DocumentData documentData, boolean isInlineImage, String addAttachmentUrl)
			throws DocwbWebException {
		int responseCde = 0;
		List<AttachmentData> attachmentDataList = new ArrayList<AttachmentData>();
		if (isInlineImage) {
			attachmentDataList = documentData.getInlineAttachmentDataList();
		} else {
			attachmentDataList = documentData.getAttachmentDataList();
			attachmentDataList.forEach(attachmentData -> attachmentData.setLogicalName(
					attachmentData.getLogicalName().replace(DocwbWebConstants.FILE_SEPARATOR, FILENAME_DELIMITER)));
		}
		int fileCounter = 0;
		int batchSize = 0;

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		if (ListUtility.hasValue(attachmentDataList)) {
			Map<Integer, List<AttachmentData>> attachmentDataExtractTypeMap = attachmentDataList.stream()
					.collect(Collectors.groupingBy(AttachmentData::getExtractTypeCde, Collectors.toList()));
			for (Entry<Integer, List<AttachmentData>> mapEntry : attachmentDataExtractTypeMap.entrySet()) {
				String extractTypeCde = "&extractTypeCde=" + mapEntry.getKey();
				int totalCount = mapEntry.getValue().size();
				for (AttachmentData attachmentData : mapEntry.getValue()) {
					fileCounter++;
					batchSize++;
					httpFileDataList.add(new HttpFileRequestData(attachmentData.getLogicalName(),
							attachmentData.getPhysicalPath(), "file" + batchSize, null));
					// As soon as max file count value is reached, fire request to save attachments
					if (fileCounter == totalCount || batchSize == MAX_FILE_COUNT_ALLOWED) {
						JsonObject jsonResponse = executePostAttachmentWithAuthCall(addAttachmentUrl + extractTypeCde,
								httpFileDataList).getResponse();
						if (jsonResponse == null) {
							throw new DocwbWebException("Error occurred while adding attachments");
						}
						if (jsonResponse != null) {
							responseCde = jsonResponse.getInt("responseCde");
							if (responseCde != 0) {
								throw new DocwbWebException("Error occurred while adding attachments");
							}
						}
						// Reset list to hold fresh set of attachments
						httpFileDataList = new ArrayList<>();
						totalCount -= fileCounter;
						batchSize = 0;
						fileCounter = 0;
					}
				}
			}

		}
		return responseCde;
	}

	public List<AttachmentData> getAttachmentList(long docId, String attachmentSaveFolder, EnumFileType enumFileType)
			throws DocwbWebException {

		String getAttachmentListUrl = attachmentApiUrl + "?docId=" + docId;
		String getAttachmentFileUrl = attachmentFileApiUrl + "?docId=" + docId;
		List<AttachmentData> attachmentDataList = new ArrayList<>();

		JsonObject jsonResponseObj = (JsonObject) executeHttpCall(HttpCallType.GET, getAttachmentListUrl);
		JsonArray jsonResponse = jsonResponseObj.getJsonArray(DocwbWebConstants.RESPONSE);

		for (int k = 0; k < jsonResponse.size(); k++) {
			JsonObject jsonAttachmentObj = jsonResponse.getJsonObject(k);

			AttachmentData attachmentData = new AttachmentData();
			attachmentData.setDocId(docId);

			attachmentData.setAttachmentId(jsonAttachmentObj.getInt(DocwbWebConstants.ATTACHMENT_ID));

			String logicalName = jsonAttachmentObj.getString(DocwbWebConstants.FILE_NAME);
			attachmentData.setLogicalName(logicalName);

			attachmentData.setInlineImage(jsonAttachmentObj.getBoolean(DocwbWebConstants.INLINE_IMAGE));
			attachmentData.setExtractTypeCde(jsonAttachmentObj.getInt(DocwbWebConstants.EXTRACT_TYPE_CDE));
			attachmentData.setGroupName(jsonAttachmentObj.getString("groupName"));
			attachmentData.setSortOrder(jsonAttachmentObj.getInt(DocwbWebConstants.SORT_ORDER));
			if (jsonAttachmentObj.containsKey(DocwbWebConstants.ATTRIBUTES)) {
				attachmentData.setAttributes(
						AttributeHelper.getAttributes(jsonAttachmentObj.getJsonArray(DocwbWebConstants.ATTRIBUTES)));
			}
			// Make call to download attachment file
			String url = getAttachmentFileUrl + "&attachmentId=" + attachmentData.getAttachmentId();
			HttpClientBase.HttpFileResponseData httpFileResData = executeGetAttachmentCall(url, attachmentSaveFolder);
			if (httpFileResData != null) {
				attachmentData.setLogicalName(httpFileResData.getFileName());
				attachmentData.setPhysicalName(httpFileResData.getFilePhysicalName());
				attachmentData.setPhysicalPath(httpFileResData.getFilePhysicalPath());
			}
			attachmentDataList.add(attachmentData);
		}
		attachmentDataList = AttachmentDataHelper.getFilteredAttachmentList(attachmentDataList, enumFileType);
		return attachmentDataList;
	}

}
