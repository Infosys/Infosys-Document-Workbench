/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.AttachmentResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttaAttaRelReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttaAttaRelResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttachmentResData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attachment.IAttachmentProcess;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1")
@Api(tags = { "attachment" })
public class AttachmentController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

	@Autowired
	private IAuditProcess auditProcess;
	@Autowired
	private IAttachmentProcess attachProcess;

	@ApiOperation(value = "Add a new attachment along with associated data", tags = "attachment")
	@RequestMapping(value = "/attachment/doc", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> addDocAttachment(@RequestParam(value = "file1") MultipartFile file1,
			@RequestParam(value = "file2", required = false) MultipartFile file2,
			@RequestParam(value = "file3", required = false) MultipartFile file3,
			@RequestParam(value = "file4", required = false) MultipartFile file4,
			@RequestParam(value = "file5", required = false) MultipartFile file5,
			@RequestParam(value = "isInlineImage", required = false, defaultValue = "false") boolean isInLineImage,
			@RequestParam("docId") int docId,
			@RequestParam(value = "extractTypeCde", required = true) int extractTypeCde,
			@RequestParam(value = "groupName", required = false) String groupName,
			@RequestParam(value = "isPrimary",required = false, defaultValue = "true")boolean isPrimary) {
		List<Long> attachmentRelIdList = new ArrayList<Long>();
		List<EntityDbData> entityData = new ArrayList<EntityDbData>();
		ApiResponseData<List<InsertAttachmentResData>> apiResponseData = new ApiResponseData<>();
		try {
			List<MultipartFile> files = new ArrayList<MultipartFile>();
			files.add(file1);
			files.add(file2);
			files.add(file3);
			files.add(file4);
			files.add(file5);
			AttachmentReqData attachmentRequestData = new AttachmentReqData();
			attachmentRequestData.setMultipartFileList(files);
			attachmentRequestData.setInlineImage(isInLineImage);
			attachmentRequestData.setPrimary(isPrimary);
			if (extractTypeCde > 0) {
				List<Integer> extractTypeCdeList = new ArrayList<>();
				extractTypeCdeList.add(extractTypeCde);
				attachmentRequestData.setExtractTypeCdeList(extractTypeCdeList);
			} else {
				String validationMessage = "Please provide valid extractTypeCde.";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (StringUtility.hasValue(groupName)) {
				long attachmentCount = attachProcess.getDocAttachmentList(docId).stream()
						.filter(attachment -> attachment.getGroupName().equals(groupName)).count();
				if (attachmentCount <= 0) {
					String validationMessage = "Provided group name is invalid for this case.";
					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
				attachmentRequestData.setGroupName(groupName);
			}
			List<InsertAttachmentResData> attachmentResDatas = attachProcess.addAttachmentToDoc(attachmentRequestData,
					docId);
			attachmentRelIdList = attachmentResDatas.stream().map(InsertAttachmentResData::getDocAttachmentRelId)
					.collect(Collectors.toList());
			apiResponseData.setResponse(attachmentResDatas);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract long getDocAttachmentRelId();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(InsertAttachmentResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setDocAttachmentRelIdList(attachmentRelIdList);
			entityData.add(entityDbData);
			auditProcess.addAuditDetails(entityData, EnumEntityType.ATTACHMENT, EnumOperationType.INSERT);
		}
	}

	@ApiOperation(value = "Get list of attachments associated with docId", tags = "attachment")
	@RequestMapping(value = "/attachment/doc", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getDocAttachmentList(@RequestParam("docId") int docId) {
		List<AttachmentResData> resultList = new ArrayList<AttachmentResData>();
		try {
			ApiResponseData<List<AttachmentResData>> apiResponseData = new ApiResponseData<List<AttachmentResData>>();
			resultList = attachProcess.getDocAttachmentList(docId);
			apiResponseData.setResponse(resultList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get attachment file for a given document id and attachment id", tags = "attachment")
	@RequestMapping(value = "/attachment/doc/file", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDocAttachmentFile(@RequestParam("docId") int docId,
			@RequestParam("attachmentId") int attachmentId) {

		try {
			AttachmentDbData attachmentDbData = attachProcess.getDocAttachmentFile(docId, attachmentId);
			byte[] contents = FileUtility.readFile(attachmentDbData.getPhysicalPath());

			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-type", FileUtility.getContentType(attachmentDbData.getPhysicalPath(),
					StringUtility.getFileExtension(attachmentDbData.getPhysicalName())));
			// setContentType(
			// StringUtil.getContentType(StringUtil.getFileExtension(attachmentDbData.getPhysicalName())));
			// add file name instead file path
			headers.setContentDispositionFormData("attachment",
					StringUtility.sanitizeReqData(attachmentDbData.getLogicalName()));
			return new ResponseEntity<>(contents, headers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get attachment file for a given document id and attachment id", tags = "attachment")
	@RequestMapping(value = "/attachment/doc/filepath", method = RequestMethod.GET)
	public ResponseEntity<String> getDocAttachmentFilepath(@RequestParam("docId") int docId,
			@RequestParam("attachmentId") int attachmentId) {

		try {
			ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
			AttachmentDbData attachmentDbData = attachProcess.getDocAttachmentFile(docId, attachmentId);
			apiResponseData.setResponse(attachmentDbData.getPhysicalName());
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@ApiOperation(value = "Get attachment file for a given email outbound id and attachment id", tags = "attachment")
	@RequestMapping(value = "/attachment/email/file", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getOutboundAttachmentFile(@RequestParam("emailOutboundId") int emailOutboundId,
			@RequestParam("attachmentId") int attachmentId) {

		try {
			AttachmentDbData attachmentDbData = attachProcess.getOutboudAttachmentFile(emailOutboundId, attachmentId);
			byte[] contents = FileUtility.readFile(attachmentDbData.getPhysicalPath());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			// add file name instead file path
			headers.setContentDispositionFormData("attachment",
					StringUtility.sanitizeReqData(attachmentDbData.getLogicalName()));
			return new ResponseEntity<>(contents, headers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get list of attachments associated with email outbound id", tags = "attachment")
	@RequestMapping(value = "/attachment/email", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAttachmentListEmail(@RequestParam("emailOutboundId") long emailOutboundId) {
		List<AttachmentResData> resultList = new ArrayList<AttachmentResData>();
		try {
			ApiResponseData<List<AttachmentResData>> apiResponseData = new ApiResponseData<List<AttachmentResData>>();
			resultList = attachProcess.getAttachmentListEmail(emailOutboundId);
			apiResponseData.setResponse(resultList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Add a new attachment along with associated data to a group", tags = "attachment")
	@RequestMapping(value = "/attachment/doc/group", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> addDocAttachmentToGroup(@RequestParam(value = "file1") MultipartFile file1,
			@RequestParam(value = "extractTypeCde1") int extractTypeCde1,
			@RequestParam(value = "file2", required = false) MultipartFile file2,
			@RequestParam(value = "extractTypeCde2", defaultValue = "-1") int extractTypeCde2,
			@RequestParam(value = "file3", required = false) MultipartFile file3,
			@RequestParam(value = "extractTypeCde3", defaultValue = "-1") int extractTypeCde3,
			@RequestParam(value = "file4", required = false) MultipartFile file4,
			@RequestParam(value = "extractTypeCde4", defaultValue = "-1") int extractTypeCde4,
			@RequestParam(value = "file5", required = false) MultipartFile file5,
			@RequestParam(value = "extractTypeCde5", defaultValue = "-1") int extractTypeCde5,
			@RequestParam(value = "isInlineImage", required = false, defaultValue = "false") boolean isInLineImage,
			@RequestParam("docId") int docId) {
		logger.info("Started addDocAttachmentToGroup");
		List<Long> attachmentRelIdList = new ArrayList<Long>();
		List<EntityDbData> entityData = new ArrayList<EntityDbData>();
		ApiResponseData<List<InsertAttachmentResData>> apiResponseData = new ApiResponseData<>();
		try {
			logger.info("Started addDocAttachmentToGroup try");
			List<MultipartFile> files = new ArrayList<MultipartFile>();
			AttachmentReqData attachmentRequestData = new AttachmentReqData();
			attachmentRequestData.setInlineImage(isInLineImage);
			List<Integer> extractTypeCdeList = new ArrayList<>();
			String validationMessage = "";
			if (extractTypeCde1 > 0) {
				files.add(file1);
				extractTypeCdeList.add(extractTypeCde1);
			} else {
				validationMessage = "Please provide valid extractTypeCde1.";
			}
			if (file2 != null) {
				if (extractTypeCde2 > 0 && file2 != null) {
					files.add(file2);
					extractTypeCdeList.add(extractTypeCde2);
				} else {
					validationMessage = "Please provide valid extractTypeCde2.";
				}
			}
			if (file3 != null) {
				if (extractTypeCde3 > 0) {
					files.add(file3);
					extractTypeCdeList.add(extractTypeCde3);
				} else {
					validationMessage = "Please provide valid extractTypeCde3.";
				}
			}
			if (file4 != null) {
				if (extractTypeCde4 > 0) {
					files.add(file4);
					extractTypeCdeList.add(extractTypeCde4);
				} else {
					validationMessage = "Please provide valid extractTypeCde4.";
				}
			}
			if (file5 != null) {
				if (extractTypeCde5 > 0) {
					files.add(file5);
					extractTypeCdeList.add(extractTypeCde5);
				} else {
					validationMessage = "Please provide valid extractTypeCde5.";
				}
			}
			if (StringUtility.hasValue(validationMessage)) {
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			} else {
				attachmentRequestData.setMultipartFileList(files);
				attachmentRequestData.setExtractTypeCdeList(extractTypeCdeList);
				List<InsertAttachmentResData> attachmentResDatas = attachProcess
						.addAttachmentToDoc(attachmentRequestData, docId);
				attachmentRelIdList = attachmentResDatas.stream().map(InsertAttachmentResData::getDocAttachmentRelId)
						.collect(Collectors.toList());
				apiResponseData.setResponse(attachmentResDatas);
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				abstract class MixIn {
					@JsonIgnore
					abstract long getDocAttachmentRelId();
				}
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.addMixIn(InsertAttachmentResData.class, MixIn.class);
				return jsonResponseOk(apiResponseData, objectMapper);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setDocAttachmentRelIdList(attachmentRelIdList);
			entityData.add(entityDbData);
			auditProcess.addAuditDetails(entityData, EnumEntityType.ATTACHMENT, EnumOperationType.INSERT);
		}
	}
	
	@ApiOperation(value = "Add parent-child relationship records between two attachments", tags = "attachment")
	@RequestMapping(value = "/attachment/attachment", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE}, 
	produces = {MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addAttaAttaRel(@RequestBody AttaAttaRelReqData attachmentRelReqData) {
		List<EntityDbData> entityData = new ArrayList<EntityDbData>();
		List<InsertAttaAttaRelResData> inserstAttaAttaRelResDataList=new ArrayList<InsertAttaAttaRelResData>();
		ApiResponseData<List<InsertAttaAttaRelResData>> apiResponseData = new ApiResponseData<>();
		try {
			long docAttaRelCount=attachProcess.countAttaAttaDocExist(attachmentRelReqData);
			if(docAttaRelCount<2) {
				String validationMessage = "Either Attachment IDs invalid or Doc Id is invalid.";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			long attaAttaRelCount=attachProcess.countAttaAttaRelRecord(attachmentRelReqData);
			if(attaAttaRelCount > 0) {
				String validationMessage = "This record already exist.";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			inserstAttaAttaRelResDataList=attachProcess.addAttaAttaRel(attachmentRelReqData);

			
			apiResponseData.setResponse(inserstAttaAttaRelResDataList);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
		finally {
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setParentAttachmentId(inserstAttaAttaRelResDataList.get(0).getAttachmentId1());
			entityDbData.setChildAttachmentId(inserstAttaAttaRelResDataList.get(0).getAttachmentId2());
			entityDbData.setDocId(inserstAttaAttaRelResDataList.get(0).getDocId());
			entityDbData.setAttaAttaRelId(inserstAttaAttaRelResDataList.get(0).getAttaAttaRelId());
			entityData.add(entityDbData);
			auditProcess.addAuditDetails(entityData, EnumEntityType.ATTACHMENT_REL, EnumOperationType.INSERT);
		}
	}
}
