/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.doc;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.dao.attachment.IAttachmentDataAccess;
import com.infosys.ainauto.docwb.service.dao.attribute.IAttributeDataAccess;
import com.infosys.ainauto.docwb.service.dao.doc.IDocDataAccess;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.dao.val.IValDataAccess;
import com.infosys.ainauto.docwb.service.model.api.DocAndAttrResData;
import com.infosys.ainauto.docwb.service.model.api.DocUserResData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.action.GetActionReqData;
import com.infosys.ainauto.docwb.service.model.api.document.CloseCaseReqData;
import com.infosys.ainauto.docwb.service.model.api.document.GetDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocEventReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.UpdateDocStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAttrWrapperDbData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData;
import com.infosys.ainauto.docwb.service.model.db.DocUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData.DocIdOperation;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;
import com.infosys.ainauto.docwb.service.process.action.IActionProcess;
import com.infosys.ainauto.docwb.service.service.messaging.IMessagingService;

@Component
public class DocumentProcess implements IDocumentProcess {

	private static final Logger logger = LoggerFactory.getLogger(DocumentProcess.class);

	@Value("${pageSize}")
	private int pageSize;

	@Value("${attachmentFilePath}")
	private String attachmentFilePath;

	@Value("${queue.docwb.service.case}")
	private String queueDocwbServiceCase;

	@Autowired
	private IDocDataAccess docDataAccess;

	@Autowired
	private IAttributeDataAccess attributeDataAccess;

	@Autowired
	private IAttachmentDataAccess attachmentDataAccess;

	@Autowired
	private IActionProcess actionProcess;

	@Autowired
	private IValDataAccess valDataAccess;

	@Autowired
	private IUserDataAccess userDataAccess;

	@Autowired
	private IMessagingService messagingService;
	
	private static final int PAGE_SIZE = 15;

	// Map to store details of docId being processed for case assignment
	private static Map<String, Long> docIdUserAssignmentProcessingMap = new HashMap<>();

	public List<EntityDbData> addDocumentAndAttributes(InsertDocReqData insertDocReqData) throws WorkbenchException {
		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<EntityDbData> docEntityDbDataList = new ArrayList<EntityDbData>();
		DocumentDbData documentDbData = new DocumentDbData();
		documentDbData.setDocTypeCde(insertDocReqData.getDocTypeCde());
		documentDbData.setDocLocation(insertDocReqData.getDocLocation());
		documentDbData.setQueueNameCde(insertDocReqData.getQueueNameCde());
		{
			int lockStatusCde = insertDocReqData.getLockStatusCde() == 0 ? 1 : insertDocReqData.getLockStatusCde();
			documentDbData.setLockStatusCde(lockStatusCde);
		}
		{
			int taskStatusCde = EnumTaskStatus.UNDEFINED.getValue();
			documentDbData.setTaskStatusCde(taskStatusCde);
		}

		long docId = docDataAccess.addDocument(documentDbData);
		if (docId > 0) {
			if (ListUtility.hasValue(insertDocReqData.getAttributes())) {
				AttributeDbData attributeDbData;
				long prevDocAttrRelId = -1;
				for (InsertDocData addAttributesRequestData : insertDocReqData.getAttributes()) {
					attributeDbData = new AttributeDbData();
					attributeDbData.setDocId(docId);
					attributeDbData.setAttrNameCde(addAttributesRequestData.getAttrNameCde());
					attributeDbData.setAttrValue(addAttributesRequestData.getAttrValue());
					attributeDbData.setExtractTypeCde(addAttributesRequestData.getExtractTypeCde());
					attributeDbData.setConfidencePct(addAttributesRequestData.getConfidencePct());
					long latestDocAttrRelId = attributeDataAccess.addNewAttribute(attributeDbData);
					if (latestDocAttrRelId > 0) {
						latestDocAttrRelIdList.add(latestDocAttrRelId);
						prevDocAttrRelIdList.add(prevDocAttrRelId);
					}
				}
			}
			if (ListUtility.hasValue(prevDocAttrRelIdList) && ListUtility.hasValue(latestDocAttrRelIdList)) {
				EntityDbData prevEntityDbData = new EntityDbData();
				EntityDbData latestEntityDbData = new EntityDbData();
				latestEntityDbData.setDocAttrRelIdList(latestDocAttrRelIdList);
				latestEntityDbData.setDocId(docId);
				latestEntityDbData.setTaskStatusCde(EnumTaskStatus.UNDEFINED.getValue());
				docEntityDbDataList.add(latestEntityDbData);
				prevEntityDbData.setDocAttrRelIdList(prevDocAttrRelIdList);
				docEntityDbDataList.add(prevEntityDbData);
			}
		}
		return docEntityDbDataList;

	}

	public EntityDbData deleteDocument(long docId) throws WorkbenchException {
		EntityDbData data = new EntityDbData();
		data.setDocId(docDataAccess.deleteDocument(docId));
		return data;
	}

	@SuppressWarnings("unchecked")
	public void getDocumentDetails(PaginationApiResponseData<List<DocAndAttrResData>> apiResponseData,
			GetDocReqData getDocReqData) throws WorkbenchException {
		if (getDocReqData.getIsPagination()) {
			List<Future<?>> taskList = new ArrayList<>();
			int threadPoolCount = (getDocReqData.getDocId() == 0) ? 2 : 1;
			ExecutorService executor = Executors.newFixedThreadPool(threadPoolCount);
			try {
				final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (!StringUtility.hasValue(getDocReqData.getDocumentName())) {
					if (getDocReqData.getDocId() == 0) {
						Future<PaginationResData> callable1 = executor.submit(() -> {
							SecurityContext ctx = SecurityContextHolder.createEmptyContext();
							ctx.setAuthentication(auth);
							SecurityContextHolder.setContext(ctx);
							return getPaginationDetails(getDocReqData);
						});
						taskList.add(callable1);
					}

					Future<List<DocAndAttrResData>> callable2 = executor.submit(() -> {
						SecurityContext ctx = SecurityContextHolder.createEmptyContext();
						ctx.setAuthentication(auth);
						SecurityContextHolder.setContext(ctx);
						return getDocumentAndAttributes(getDocReqData);
					});
					taskList.add(callable2);

					for (Future<?> future : taskList) {
						if (future.get() instanceof PaginationResData) {
							apiResponseData.setPagination((PaginationResData) future.get());
						} else {
							apiResponseData.setResponse((List<DocAndAttrResData>) future.get());
						}
					}

				} else if (StringUtility.hasTrimmedValue(getDocReqData.getDocumentName())) {
					String docIdListStr = getDocList(getDocReqData);
					if (getDocReqData.getDocId() == 0) {
						Future<PaginationResData> callable1 = executor.submit(() -> {
							SecurityContext ctx = SecurityContextHolder.createEmptyContext();
							ctx.setAuthentication(auth);
							SecurityContextHolder.setContext(ctx);
							return getPaginationDetailForSearch(docIdListStr, getDocReqData);
						});
						taskList.add(callable1);
					}

					Future<List<DocAndAttrResData>> callable2 = executor.submit(() -> {
						SecurityContext ctx = SecurityContextHolder.createEmptyContext();
						ctx.setAuthentication(auth);
						SecurityContextHolder.setContext(ctx);
						return getDocumentAndAttributesDocumentSearch(docIdListStr, getDocReqData);
					});
					taskList.add(callable2);

					for (Future<?> future : taskList) {
						if (future.get() instanceof PaginationResData) {
							apiResponseData.setPagination((PaginationResData) future.get());

						} else {
							apiResponseData.setResponse((List<DocAndAttrResData>) future.get());
						}
					}

				}
			} catch (Exception e) {
				throw new WorkbenchException("Error occurred while getDocumentDetails", e);
			} finally {
				executor.shutdown();
			}
		} else {
			if (StringUtility.hasTrimmedValue(getDocReqData.getDocumentName())) {
				String docIdListStr = getDocList(getDocReqData);
				apiResponseData.setResponse(getDocumentAndAttributesDocumentSearch(docIdListStr, getDocReqData));
			} else {
				apiResponseData.setResponse(getDocumentAndAttributes(getDocReqData));
			}
		}
	}
	
	@Override
	public void getDocumentDetailsByAttribute(PaginationApiResponseData<List<DocDetailDbData>> apiResponseData,
			GetDocReqData getDocReqData) throws WorkbenchException {
		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setSortOrder(getDocReqData.getSortOrder());
		documentDbDataIn.setToCaseCreateDtm(getDocReqData.getToCaseCreateDtm());
		documentDbDataIn.setFromCaseCreateDtm(getDocReqData.getFromCaseCreateDtm());
		documentDbDataIn.setPageNumber((getDocReqData.getPageNumber()<1)?1:getDocReqData.getPageNumber());
		documentDbDataIn.setPageSize(PAGE_SIZE);
		
		List<DocDetailDbData> resultList = docDataAccess.getDocumentListByAttribute(documentDbDataIn, 
				getDocReqData.getSearchCriteria(), getDocReqData.getQueueNameCdes());
		
		long prevDocId = 0;
		int flat_record_counter = 1;
		List<DocDetailDbData> newDocDetailDbDataList = new ArrayList<DocDetailDbData>();
		DocDetailDbData newDocDetailDbData = null;
		for(int i=0;i<resultList.size();i++) {
			DocDetailDbData docDetailDbData =resultList.get(i); 
			if(flat_record_counter>PAGE_SIZE) {
				break;
			}
			if (prevDocId==docDetailDbData.getDocId()) {
				newDocDetailDbData.getAttributeData().addAll(docDetailDbData.getAttributeData());
			}else {
				if (newDocDetailDbData!=null) {
					newDocDetailDbDataList.add(newDocDetailDbData);
					flat_record_counter++;
				}
				newDocDetailDbData = docDetailDbData;
				prevDocId = docDetailDbData.getDocId();
			}
			if (i==resultList.size()-1) {
				newDocDetailDbDataList.add(newDocDetailDbData);
			}
		}
		
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setPageSize((newDocDetailDbDataList!=null)?newDocDetailDbDataList.size():0);
		paginationResData.setCurrentPageNumber(documentDbDataIn.getPageNumber());
		
		apiResponseData.setResponse(newDocDetailDbDataList);
		apiResponseData.setPagination(paginationResData);
	}

	@Override
	public DocumentDbData getBasicDocumentDetails(long docId) throws WorkbenchException {
		DocumentDbData documentData = docDataAccess.getDocumentDetails(docId);
		return documentData;
	}

	public PaginationResData getPaginationDetails(GetDocReqData getDocReqData) throws WorkbenchException {
		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setHighestEventTypeCde(getDocReqData.getHighestEventTypeCde());
		documentDbDataIn.setHighestEventTypeOperator(getDocReqData.getHighestEventTypeOperator());
		documentDbDataIn.setLatestEventTypeCde(getDocReqData.getLatestEventTypeCde());
		documentDbDataIn.setLatestEventTypeOperator(getDocReqData.getLatestEventTypeOperator());
		documentDbDataIn.setLockStatusCde(getDocReqData.getLockStatusCde());
		documentDbDataIn.setQueueNameCde(getDocReqData.getQueueNameCde());
		documentDbDataIn.setDocId(getDocReqData.getDocId());
		documentDbDataIn.setPageNumber(getDocReqData.getPageNumber());
		documentDbDataIn.setAppUserId(getDocReqData.getAppUserId());
		documentDbDataIn.setToEventDtm(getDocReqData.getToEventDtm());
		documentDbDataIn.setFromEventDtm(getDocReqData.getFromEventDtm());
		documentDbDataIn.setAssignedTo(getDocReqData.getAssignedTo());
		documentDbDataIn.setDocumentName(getDocReqData.getDocumentName());
		documentDbDataIn.setPageSize(getDocReqData.getPageSize());
		int pageSizeVal = pageSize;
		if (documentDbDataIn.getPageSize() > 0) {
			pageSizeVal = documentDbDataIn.getPageSize();
		}

		if (getDocReqData.getDocIdOperationList() != null && !getDocReqData.getDocIdOperationList().isEmpty()) {
			List<DocIdOperation> docIdOperationList = new ArrayList<DocumentDbData.DocIdOperation>();

			for (GetDocReqData.DocIdOperation docIdOperationReq : getDocReqData.getDocIdOperationList()) {
				DocumentDbData.DocIdOperation docIdOperation = new DocumentDbData.DocIdOperation();
				docIdOperation.setDocId(docIdOperationReq.getDocId());
				docIdOperation.setOperator(docIdOperationReq.getOperator());
				docIdOperationList.add(docIdOperation);
			}
			documentDbDataIn.setDocIdOperationList(docIdOperationList);

		}
		long totalDocCount = 0;
		totalDocCount = docDataAccess.getTotalDocCount(documentDbDataIn);
		int totalPages = 0;
		int currentPage = 0;
		if (totalDocCount <= 0) {
			currentPage = 0;
			totalPages = 0;
		} else {
			double total = (totalDocCount * 1.0) / pageSizeVal;
			totalPages = (int) Math.ceil(total);
			currentPage = getDocReqData.getPageNumber();
			if (totalDocCount <= pageSizeVal || currentPage < 1) {
				if (currentPage <= totalPages) {
					currentPage = 1;
				}
			}
		}
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setCurrentPageNumber(currentPage);
		paginationResData.setTotalPageCount(totalPages);
		paginationResData.setTotalItemCount(totalDocCount);
		paginationResData.setPageSize(pageSizeVal);
		return paginationResData;
	}

	public List<DocAndAttrResData> getDocumentAndAttributes(GetDocReqData getDocReqData) throws WorkbenchException {

		List<DocAndAttrResData> docAndAttrResDataList = new ArrayList<DocAndAttrResData>();
		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setAttrNameCdes(getDocReqData.getAttrNameCdes());
		documentDbDataIn.setAttachmentAttrNameCdes(getDocReqData.getAttachmentAttrNameCdes());
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setHighestEventTypeCde(getDocReqData.getHighestEventTypeCde());
		documentDbDataIn.setHighestEventTypeOperator(getDocReqData.getHighestEventTypeOperator());
		documentDbDataIn.setLatestEventTypeCde(getDocReqData.getLatestEventTypeCde());
		documentDbDataIn.setLatestEventTypeOperator(getDocReqData.getLatestEventTypeOperator());
		documentDbDataIn.setLockStatusCde(getDocReqData.getLockStatusCde());
		documentDbDataIn.setQueueNameCde(getDocReqData.getQueueNameCde());
		documentDbDataIn.setDocId(getDocReqData.getDocId());
		documentDbDataIn.setPageNumber(getDocReqData.getPageNumber());
		documentDbDataIn.setAppUserId(getDocReqData.getAppUserId());
		documentDbDataIn.setSortOrder(getDocReqData.getSortOrder());
		documentDbDataIn.setToEventDtm(getDocReqData.getToEventDtm());
		documentDbDataIn.setFromEventDtm(getDocReqData.getFromEventDtm());
		documentDbDataIn.setAssignedTo(getDocReqData.getAssignedTo());
		documentDbDataIn.setAssignedToKey(getDocReqData.getAssignedToKey());
		documentDbDataIn.setDocumentName(getDocReqData.getDocumentName());
		documentDbDataIn.setPageSize(getDocReqData.getPageSize());
		documentDbDataIn.setSortByAttrNameCde(getDocReqData.getSortByAttrNameCde());

		if (getDocReqData.getDocIdOperationList() != null && !getDocReqData.getDocIdOperationList().isEmpty()) {
			List<DocIdOperation> docIdOperationList = new ArrayList<DocumentDbData.DocIdOperation>();

			for (GetDocReqData.DocIdOperation docIdOperationReq : getDocReqData.getDocIdOperationList()) {
				DocumentDbData.DocIdOperation docIdOperation = new DocumentDbData.DocIdOperation();
				docIdOperation.setDocId(docIdOperationReq.getDocId());
				docIdOperation.setOperator(docIdOperationReq.getOperator());
				docIdOperationList.add(docIdOperation);
			}
			documentDbDataIn.setDocIdOperationList(docIdOperationList);
		}
		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = docDataAccess.getDocumentList(documentDbDataIn);
		if (ListUtility.hasValue(docAttrWrapperDbDataList)) {
			DocAndAttrResData docAndAttrResData = null;
			List<AttributeDbData> attributeDbDataList = null;
			boolean newDocument;
			long currentDocId = -1;
			List<ValTableDbData> extractTypeDbDataList = valDataAccess
					.getValTableData(WorkbenchConstants.EXTRACT_TYPE_VAL_TABLE_NAME);
			HashMap<Integer, String> extractTypeValMap = new HashMap<>();

			for (ValTableDbData valTableDbData : extractTypeDbDataList) {
				extractTypeValMap.put(valTableDbData.getCde(), valTableDbData.getTxt());
			}
			for (DocAttrWrapperDbData docAttrWrapperDbData : docAttrWrapperDbDataList) {
				newDocument = false;
				DocumentDbData documentDbData = docAttrWrapperDbData.getDocumentDbData();

				if (currentDocId != documentDbData.getDocId()) {
					newDocument = true;
					// Add previous list to main list
					if (docAndAttrResData != null) {
						docAndAttrResData.setAttributes(attributeDbDataList);
						docAndAttrResDataList.add(docAndAttrResData);
						docAndAttrResData = null;
					}
				}
				currentDocId = documentDbData.getDocId();
				if (newDocument) {
					docAndAttrResData = new DocAndAttrResData();
					BeanUtils.copyProperties(documentDbData, docAndAttrResData);
					attributeDbDataList = new ArrayList<AttributeDbData>();
				}
				AttributeDbData attributeDbData = docAttrWrapperDbData.getAttributeDbData();
				if (attributeDbData == null) {
					continue;
				}
				if (attributeDbData.getAttrNameCde() == EnumSystemAttributeName.CONTENT_HTML.getCde()) {
					String contentHtml = attributeDbData.getAttrValue().replace("<html>", "").replace("</html>", "");
					contentHtml = contentHtml.trim();
					logger.debug("html email body before encoding" + contentHtml);
					List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess
							.getDocAttachmentList(currentDocId);
					// Converting Image to Base64 String
					List<String> fileList = PatternUtility.getHtmlImgSrcValues(contentHtml);
					String filePath = null;
					for (String fileName : fileList) {
						logger.debug("file name " + fileName);
						String[] strings = fileName.split("\\.");

						String extension = strings.length == 2 ? strings[1] : "";

						String fileExtension = StringUtility.getBase64Extension1(extension);
						String base64String = "";
						try {
							// Reading a Image file from file system and convert it into Base64 String
							for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
								if (attachmentDbData.isInlineImage()) {
									if (fileName.equalsIgnoreCase(attachmentDbData.getLogicalName())) {
										filePath = FileUtility.getConcatenatedName(attachmentFilePath,
												attachmentDbData.getPhysicalName());
										byte[] imageData = FileUtility.readFile(filePath);
										base64String = fileExtension + ","
												+ Base64.getEncoder().encodeToString(imageData);
									}
								}
							}
							contentHtml = contentHtml.replace(fileName, base64String);
						} catch (Exception e) {
							logger.error("Exception while reading the Image " + e);
						}
					}
					attributeDbData.setAttrValue(contentHtml);
				}
				if (attributeDbData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
					List<AttributeDbData> multiAttrDbDataList = new ArrayList<>();
					AttributeDbData parameterData = AttributeHelper.convertJsonStringToMultiAttr(attributeDbData,
							userDataAccess);
					attributeDbData.setAttrNameTxt(parameterData.getAttrNameTxt());
					List<AttributeDbData> parameterDataList = parameterData.getAttributes();
					if (ListUtility.hasValue(parameterDataList)) {
						for (AttributeDbData multiAttrParameterData : parameterDataList) {
							AttributeDbData attrDbData = new AttributeDbData();
							BeanUtils.copyProperties(multiAttrParameterData, attrDbData);
							multiAttrDbDataList.add(attrDbData);
						}
						attributeDbData.setAttributes(
								AttributeHelper.getAlphabeticallySortedAttributeList(multiAttrDbDataList));
					}
					attributeDbData.setAttrValue(null);
				}
				if (attributeDbDataList != null) {
					attributeDbDataList.add(attributeDbData);
				}
			}
			if (ListUtility.hasValue(attributeDbDataList))
				attributeDbDataList = AttributeHelper.getAlphabeticallySortedAttributeList(attributeDbDataList);
			// Add previous list to main list - handle last item which is not handled by
			// loop
			if (docAndAttrResData != null) {
				docAndAttrResData.setAttributes(attributeDbDataList);
				docAndAttrResDataList.add(docAndAttrResData);
			}

		}

		return docAndAttrResDataList;
	}

	public String getDocList(GetDocReqData getDocReqData) throws WorkbenchException {

		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setAttrNameCdes(getDocReqData.getAttrNameCdes());
		documentDbDataIn.setAttachmentAttrNameCdes(getDocReqData.getAttachmentAttrNameCdes());
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setHighestEventTypeCde(getDocReqData.getHighestEventTypeCde());
		documentDbDataIn.setHighestEventTypeOperator(getDocReqData.getHighestEventTypeOperator());
		documentDbDataIn.setLatestEventTypeCde(getDocReqData.getLatestEventTypeCde());
		documentDbDataIn.setLatestEventTypeOperator(getDocReqData.getLatestEventTypeOperator());
		documentDbDataIn.setLockStatusCde(getDocReqData.getLockStatusCde());
		documentDbDataIn.setQueueNameCde(getDocReqData.getQueueNameCde());
		documentDbDataIn.setDocId(getDocReqData.getDocId());
		documentDbDataIn.setPageNumber(getDocReqData.getPageNumber());
		documentDbDataIn.setAppUserId(getDocReqData.getAppUserId());
		documentDbDataIn.setSortOrder(getDocReqData.getSortOrder());
		documentDbDataIn.setToEventDtm(getDocReqData.getToEventDtm());
		documentDbDataIn.setFromEventDtm(getDocReqData.getFromEventDtm());
		documentDbDataIn.setAssignedTo(getDocReqData.getAssignedTo());
		documentDbDataIn.setDocumentName(getDocReqData.getDocumentName());
		documentDbDataIn.setSortByAttrNameCde(getDocReqData.getSortByAttrNameCde());

		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = docDataAccess.getDocumentList(documentDbDataIn);
		String docIdListStr = null;
		if (ListUtility.hasValue(docAttrWrapperDbDataList)) {
			if (StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
				List<String> docIdList = new ArrayList<String>();
				docIdListStr = "(";
				for (DocAttrWrapperDbData docAttrWrapperDbData : docAttrWrapperDbDataList) {
					DocumentDbData documentDbData = docAttrWrapperDbData.getDocumentDbData();
					if (!docIdList.contains(String.valueOf(documentDbData.getDocId()))) {
						docIdList.add(String.valueOf(documentDbData.getDocId()));
					}
				}
				docIdListStr += String.join(",", docIdList) + ")";
			}
		}
		return docIdListStr;
	}

	public List<DocAndAttrResData> getDocumentAndAttributesDocumentSearch(String docIdListStr,
			GetDocReqData getDocReqData) throws WorkbenchException {

		List<DocAndAttrResData> docAndAttrResDataList = new ArrayList<DocAndAttrResData>();
		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setAttrNameCdes(getDocReqData.getAttrNameCdes());
		documentDbDataIn.setAttachmentAttrNameCdes(getDocReqData.getAttachmentAttrNameCdes());
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setHighestEventTypeCde(getDocReqData.getHighestEventTypeCde());
		documentDbDataIn.setHighestEventTypeOperator(getDocReqData.getHighestEventTypeOperator());
		documentDbDataIn.setLatestEventTypeCde(getDocReqData.getLatestEventTypeCde());
		documentDbDataIn.setLatestEventTypeOperator(getDocReqData.getLatestEventTypeOperator());
		documentDbDataIn.setLockStatusCde(getDocReqData.getLockStatusCde());
		documentDbDataIn.setQueueNameCde(getDocReqData.getQueueNameCde());
		documentDbDataIn.setDocId(getDocReqData.getDocId());
		documentDbDataIn.setPageNumber(getDocReqData.getPageNumber());
		documentDbDataIn.setAppUserId(getDocReqData.getAppUserId());
		documentDbDataIn.setSortOrder(getDocReqData.getSortOrder());
		documentDbDataIn.setToEventDtm(getDocReqData.getToEventDtm());
		documentDbDataIn.setFromEventDtm(getDocReqData.getFromEventDtm());
		documentDbDataIn.setAssignedTo(getDocReqData.getAssignedTo());
		documentDbDataIn.setDocumentName(getDocReqData.getDocumentName());
		documentDbDataIn.setPageSize(getDocReqData.getPageSize());
		documentDbDataIn.setSortByAttrNameCde(getDocReqData.getSortByAttrNameCde());

		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = docDataAccess.getSearchCriteriaDetails(docIdListStr,
				documentDbDataIn);
		if (ListUtility.hasValue(docAttrWrapperDbDataList)) {
			DocAndAttrResData docAndAttrResData = null;
			List<AttributeDbData> attributeDbDataList = null;
			boolean newDocument;
			long currentDocId = -1;
			List<ValTableDbData> extractTypeDbDataList = valDataAccess
					.getValTableData(WorkbenchConstants.EXTRACT_TYPE_VAL_TABLE_NAME);
			HashMap<Integer, String> extractTypeValMap = new HashMap<>();

			for (ValTableDbData valTableDbData : extractTypeDbDataList) {
				extractTypeValMap.put(valTableDbData.getCde(), valTableDbData.getTxt());
			}
			for (DocAttrWrapperDbData docAttrWrapperDbData : docAttrWrapperDbDataList) {
				newDocument = false;
				DocumentDbData documentDbData = docAttrWrapperDbData.getDocumentDbData();

				if (currentDocId != documentDbData.getDocId()) {
					newDocument = true;
					// Add previous list to main list
					if (docAndAttrResData != null) {
						docAndAttrResData.setAttributes(attributeDbDataList);
						docAndAttrResDataList.add(docAndAttrResData);
						docAndAttrResData = null;
					}
				}
				currentDocId = documentDbData.getDocId();
				if (newDocument) {
					docAndAttrResData = new DocAndAttrResData();
					BeanUtils.copyProperties(documentDbData, docAndAttrResData);
					attributeDbDataList = new ArrayList<AttributeDbData>();
				}
				AttributeDbData attributeDbData = docAttrWrapperDbData.getAttributeDbData();
				if (attributeDbData == null) {
					continue;
				}
				if (attributeDbData.getAttrNameCde() == EnumSystemAttributeName.CONTENT_HTML.getCde()) {
					String contentHtml = attributeDbData.getAttrValue().replace("<html>", "").replace("</html>", "");
					contentHtml = contentHtml.trim();
					logger.debug("html email body before encoding" + contentHtml);
					List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess
							.getDocAttachmentList(currentDocId);
					// Converting Image to Base64 String
					List<String> fileList = PatternUtility.getHtmlImgSrcValues(contentHtml);
					String filePath = null;
					for (String fileName : fileList) {
						logger.debug("file name " + fileName);
						String[] strings = fileName.split("\\.");

						String extension = strings.length == 2 ? strings[1] : "";

						String fileExtension = StringUtility.getBase64Extension1(extension);
						String base64String = "";
						try {
							// Reading a Image file from file system and convert it into Base64 String
							for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
								if (attachmentDbData.isInlineImage()) {
									if (fileName.equalsIgnoreCase(attachmentDbData.getLogicalName())) {
										filePath = FileUtility.getConcatenatedName(attachmentFilePath,
												attachmentDbData.getPhysicalName());
										byte[] imageData = FileUtility.readFile(filePath);
										base64String = fileExtension + ","
												+ Base64.getEncoder().encodeToString(imageData);
									}
								}
							}
							contentHtml = contentHtml.replace(fileName, base64String);
						} catch (Exception e) {
							logger.error("Exception while reading the Image " + e);
						}
					}
					attributeDbData.setAttrValue(contentHtml);
				}
				if (attributeDbData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
					List<AttributeDbData> multiAttrDbDataList = new ArrayList<>();
					AttributeDbData parameterData = AttributeHelper.convertJsonStringToMultiAttr(attributeDbData,
							userDataAccess);
					attributeDbData.setAttrNameTxt(parameterData.getAttrNameTxt());
					List<AttributeDbData> parameterDataList = parameterData.getAttributes();
					if (ListUtility.hasValue(parameterDataList)) {
						for (AttributeDbData multiAttrParameterData : parameterDataList) {
							AttributeDbData attrDbData = new AttributeDbData();
							BeanUtils.copyProperties(multiAttrParameterData, attrDbData);
							multiAttrDbDataList.add(attrDbData);
						}
						attributeDbData.setAttributes(
								AttributeHelper.getAlphabeticallySortedAttributeList(multiAttrDbDataList));
					}
					attributeDbData.setAttrValue(null);
				}
				if (attributeDbDataList != null) {
					attributeDbDataList.add(attributeDbData);
				}
			}
			if (ListUtility.hasValue(attributeDbDataList))
				attributeDbDataList = AttributeHelper.getAlphabeticallySortedAttributeList(attributeDbDataList);
			// Add previous list to main list - handle last item which is not handled by
			// loop
			if (docAndAttrResData != null) {
				docAndAttrResData.setAttributes(attributeDbDataList);
				docAndAttrResDataList.add(docAndAttrResData);
			}

		}

		return docAndAttrResDataList;
	}

	public PaginationResData getPaginationDetailForSearch(String docIdListStr, GetDocReqData getDocReqData)
			throws WorkbenchException {
		DocumentDbData documentDbDataIn = new DocumentDbData();
		documentDbDataIn.setTaskStatusCde(getDocReqData.getTaskStatusCde());
		documentDbDataIn.setTaskStatusOperator(getDocReqData.getTaskStatusOperator());
		documentDbDataIn.setHighestEventTypeCde(getDocReqData.getHighestEventTypeCde());
		documentDbDataIn.setHighestEventTypeOperator(getDocReqData.getHighestEventTypeOperator());
		documentDbDataIn.setLatestEventTypeCde(getDocReqData.getLatestEventTypeCde());
		documentDbDataIn.setLatestEventTypeOperator(getDocReqData.getLatestEventTypeOperator());
		documentDbDataIn.setLockStatusCde(getDocReqData.getLockStatusCde());
		documentDbDataIn.setQueueNameCde(getDocReqData.getQueueNameCde());
		documentDbDataIn.setDocId(getDocReqData.getDocId());
		documentDbDataIn.setPageNumber(getDocReqData.getPageNumber());
		documentDbDataIn.setAppUserId(getDocReqData.getAppUserId());
		documentDbDataIn.setToEventDtm(getDocReqData.getToEventDtm());
		documentDbDataIn.setFromEventDtm(getDocReqData.getFromEventDtm());
		documentDbDataIn.setAssignedTo(getDocReqData.getAssignedTo());
		documentDbDataIn.setDocumentName(getDocReqData.getDocumentName());
		documentDbDataIn.setPageSize(getDocReqData.getPageSize());
		int pageSizeVal = pageSize;
		if (documentDbDataIn.getPageSize() > 0) {
			pageSizeVal = documentDbDataIn.getPageSize();
		}

		long totalDocCount = docDataAccess.getTotalCountOnSearchCriteria(docIdListStr, documentDbDataIn);
		int totalPages = 0;
		int currentPage = 0;
		if (totalDocCount <= 0) {
			currentPage = 0;
			totalPages = 0;
		} else {
			double total = (totalDocCount * 1.0) / pageSizeVal;
			totalPages = (int) Math.ceil(total);
			currentPage = getDocReqData.getPageNumber();
			if (totalDocCount <= pageSizeVal || currentPage < 1) {
				if (currentPage <= totalPages) {
					currentPage = 1;
				}
			}
		}
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setCurrentPageNumber(currentPage);
		paginationResData.setTotalPageCount(totalPages);
		paginationResData.setTotalItemCount(totalDocCount);
		paginationResData.setPageSize(pageSizeVal);
		return paginationResData;
	}

	public List<EntityDbData> addUserToDoc(long prevAppUserId, long appUserId, long docId, long docRoleTypeCde)
			throws WorkbenchException {
		List<EntityDbData> docEntityDbDataList = new ArrayList<EntityDbData>();
		List<Long> prevIdList = new ArrayList<Long>();
		List<Long> latestIdList = new ArrayList<Long>();
		EntityDbData prevEntityDbData = new EntityDbData();
		EntityDbData latestEntityDbData = new EntityDbData();
		// Do not allow updates to happen for same docId
		synchronized (this) {
			if (docIdUserAssignmentProcessingMap.containsKey(String.valueOf(docId))) {
				latestIdList.add((long) WorkbenchConstants.PREV_USER_DETAILS_OUTDATED);
				latestEntityDbData.setDocAppUserRelIdList(latestIdList);
				docEntityDbDataList.add(latestEntityDbData);
				return docEntityDbDataList;
			}
			// Add docId to map before processing starts
			docIdUserAssignmentProcessingMap.put(String.valueOf(docId), docId);
		}
		long docAppUserRelId = -1;
		long prevDocAppUserRelId = -1;
		try {
			boolean updateDocEvent = false;
			boolean isContains = false;

			List<DocAppUserDbData> docUserList = docDataAccess.getUserDocDetails(docId, docRoleTypeCde);
			if (docUserList.size() > 0) {
				for (int i = 0; i < docUserList.size(); i++) {
					if (docUserList.get(i).getAppUserId() != appUserId
							&& prevAppUserId == docUserList.get(i).getAppUserId()) {
						isContains = false;
						prevDocAppUserRelId = docDataAccess.updateDocAppUser(docUserList.get(i));
						if (prevDocAppUserRelId > 0) {
							updateDocEvent = true;
							prevIdList.add(prevDocAppUserRelId);
						} else {
							updateDocEvent = false;
						}
					} else if (prevAppUserId != docUserList.get(i).getAppUserId()) {
						latestIdList.add((long) WorkbenchConstants.PREV_USER_DETAILS_OUTDATED);
						latestEntityDbData.setDocAppUserRelIdList(latestIdList);
						docEntityDbDataList.add(latestEntityDbData);
						return docEntityDbDataList;
					} else {
						isContains = true;
					}
				}
			}

			if (!isContains) {
				docAppUserRelId = docDataAccess.insertUserDocRel(appUserId, docId, docRoleTypeCde);
				if (docAppUserRelId > 0) {
					updateDocEvent = true;
					latestIdList.add(docAppUserRelId);
				}
			}
			if (updateDocEvent) {
				InsertDocEventReqData docData = new InsertDocEventReqData();
				docData.setDocId(docId);
				docData.setEventTypeCde(WorkbenchConstants.EVENT_TYPE_CDE_CASE_ASSIGNED);
				insertDocEventType(docData);
			}
			if (ListUtility.hasValue(latestIdList)) {
				latestEntityDbData.setDocAppUserRelIdList(latestIdList);
				docEntityDbDataList.add(latestEntityDbData);
				prevEntityDbData.setDocAppUserRelIdList(prevIdList);
				docEntityDbDataList.add(prevEntityDbData);
			}

		} catch (Exception ex) {
			logger.error("Error occurred in addUserToDoc while assigning/reassigning user to docId {}", docId);
		} finally {
			// Remove docId from map after processing is over
			synchronized (this) {
				docIdUserAssignmentProcessingMap.remove(String.valueOf(docId));
			}
		}

		return docEntityDbDataList;
	}

	@Override
	public List<DocUserResData> getDocUserDetails(long docId) throws WorkbenchException {
		List<DocUserResData> docUserResDataList = new ArrayList<DocUserResData>();
		List<DocUserDbData> docUserDbDataLis = docDataAccess.getDocUserRel(docId);
		for (DocUserDbData docUserDbData : docUserDbDataLis) {
			DocUserResData docUserResData = new DocUserResData();
			BeanUtils.copyProperties(docUserDbData, docUserResData);
			docUserResDataList.add(docUserResData);
		}
		return docUserResDataList;
	}

	public EntityDbData closeCaseForDocument(int queueNameCde, List<CloseCaseReqData> closeCaseReqDataList)
			throws WorkbenchException {

		// TODO  Add logic to handle more than one items in list
		CloseCaseReqData closeCaseReqData = closeCaseReqDataList.get(0);

		boolean updateDocEvent = false;
		EntityDbData data = new EntityDbData();

		GetActionReqData getActionReqData = new GetActionReqData();
		getActionReqData.setQueueNameCde(queueNameCde);
		getActionReqData.setDocId(closeCaseReqData.getDocId());
		getActionReqData.setTaskStatusCde(EnumTaskStatus.COMPLETE.getValue());
		getActionReqData.setTaskStatusOperator(WorkbenchConstants.LESSER_THAN);

		List<DocumentResData> resultList = actionProcess.getActionTaskList(getActionReqData);

		if (ListUtility.hasValue(resultList) && ListUtility.hasValue(resultList.get(0).getActionDataList())) {
			data.setDocId(WorkbenchConstants.CASE_HAS_PENDING_ACTIONS);
			return data;
		}

		DocumentDbData documentDbData = new DocumentDbData();
		documentDbData.setDocId(closeCaseReqData.getDocId());
		documentDbData.setQueueNameCde(queueNameCde);

		// Get document details and check if case is already closed
		List<DocAttrWrapperDbData> document = docDataAccess.getDocumentList(documentDbData);

		if (!(document.size() > 0
				&& document.get(0).getDocumentDbData().getTaskStatusCde() != EnumTaskStatus.COMPLETE.getValue())) {
			data.setDocId(WorkbenchConstants.CASE_STATUS_DIFFERENT);
			return data;
		}

		// Get user details and check if user is same as case owned
		List<DocAppUserDbData> docUserList = docDataAccess.getUserDocDetails(closeCaseReqData.getDocId(),
				WorkbenchConstants.DOC_ROLE_TYPE_CDE_FOR_OWNER);
		if (docUserList.size() > 0) {
			for (int i = 0; i < docUserList.size(); i++) {
				if (closeCaseReqData.getAppUserId() > 0
						&& docUserList.get(i).getAppUserId() == closeCaseReqData.getAppUserId()) {
					updateDocEvent = true;
				}
			}
		}

		if (updateDocEvent) {
			documentDbData.setTaskStatusCde(EnumTaskStatus.COMPLETE.getValue());
			List<EntityDbData> dataList = docDataAccess.updateDocActionStatus(documentDbData);

			if (ListUtility.hasValue(dataList)) {
				documentDbData.setEventTypeCde(WorkbenchConstants.EVENT_TYPE_CDE_CASE_CLOSED);
				docDataAccess.insertDocEventType(documentDbData);
				data = dataList.get(0);

				// Put message to MQ
				try {
					HashMap<String, String> propMap = new HashMap<>();
					propMap.put(WorkbenchConstants.MESSAGE_PROP_QUEUE_NAME_CDE, String.valueOf(queueNameCde));
					propMap.put(WorkbenchConstants.MESSAGE_PROP_DOC_ID, String.valueOf(closeCaseReqData.getDocId()));
					propMap.put(WorkbenchConstants.MESSAGE_PROP_TASK_STATUS_CDE,
							String.valueOf(EnumTaskStatus.COMPLETE.getValue()));
					propMap.put(WorkbenchConstants.MESSAGE_PROP_EVENT_TYPE_CDE,
							String.valueOf(WorkbenchConstants.EVENT_TYPE_CDE_CASE_CLOSED));
					propMap.put(WorkbenchConstants.MESSAGE_PROP_TENANT_ID, SessionHelper.getTenantId());
					messagingService.putMessageInQueue(queueDocwbServiceCase, propMap);
				} catch (Exception ex) {
					// Just log the error and proceed
					logger.error("Error occurred while putting message to queue={} for docId={}", queueDocwbServiceCase,
							closeCaseReqData.getDocId(), ex);
				}
			}
		} else {
			data.setDocId(WorkbenchConstants.CASE_OWNER_NOT_SAME_AS_USER);
		}
		return data;
	}

	public List<EntityDbData> updateDocActionStatus(UpdateDocStatusReqData documentRequestData)
			throws WorkbenchException {
		DocumentDbData documentDbData = new DocumentDbData();
		documentDbData.setDocId(documentRequestData.getDocId());
		documentDbData.setTaskStatusCde(documentRequestData.getTaskStatusCde());
		return docDataAccess.updateDocActionStatus(documentDbData);
	}

	public void insertDocEventType(InsertDocEventReqData docEventRequestData) throws WorkbenchException {
		DocumentDbData documentDbData = new DocumentDbData();
		documentDbData.setDocId(docEventRequestData.getDocId());
		documentDbData.setEventTypeCde(docEventRequestData.getEventTypeCde());
		docDataAccess.insertDocEventType(documentDbData);
	}

}
