/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.template;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttachmentHelper;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.attachment.IAttachmentDataAccess;
import com.infosys.ainauto.docwb.service.dao.doc.IDocDataAccess;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.action.GetActionReqData;
import com.infosys.ainauto.docwb.service.model.db.ActionTempMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.process.action.IActionProcess;
import com.infosys.ainauto.docwb.service.process.attachment.IAttachmentProcess;
import com.infosys.ainauto.docwb.service.process.attribute.IAttributeProcess;
import com.infosys.ainauto.docwb.service.service.ml.IRulesService;

@Component
public class TemplateProcess implements ITemplateProcess {

	@Autowired
	private IAttributeProcess attributeProcess;

	@Autowired
	private IActionProcess actionProcess;

	@Autowired
	private IRulesService rulesService;

	@Autowired
	private IDocDataAccess docDataAccess;

	@Autowired
	private IAttachmentDataAccess attachmentDataAccess;
	
	@Autowired
	private IAttachmentProcess attachProcess;

	@Value("${attachmentFilePath}")
	private String attachmentFilePath;

	private static final Logger logger = LoggerFactory.getLogger(TemplateProcess.class);

	public List<ActionTempMappingDbData> getTemplates() {
		List<ActionTempMappingDbData> templateDbDataList = rulesService.getTemplates(SessionHelper.getTenantId());
		return templateDbDataList;
	}

	public List<ActionTempMappingDbData> getTemplatesWithData(long docId) throws WorkbenchException {
		logger.info("started call after templates" + System.nanoTime());

		int docTypeCde = docDataAccess.getDocumentDetails(docId).getDocTypeCde();
		List<AttributeDbData> attributeDbDataList = null;
		if (docTypeCde == WorkbenchConstants.EMAIL)
			attributeDbDataList = attributeProcess.getDocumentAttributes(docId);
		List<AttributeDbData> attachmentAttributeDbDataList = attributeProcess.getAttachmentAttributes(docId, "",false);

		List<DocumentResData> actionParamAttrMappingDbDataList = new ArrayList<DocumentResData>();
		{
			GetActionReqData getActionReqData = new GetActionReqData();
			getActionReqData.setDocId(docId);
			getActionReqData.setTaskStatusOperator("");
			// Make the call to Data Layer with inputs provided by caller
			actionParamAttrMappingDbDataList = actionProcess.getActionTaskList(getActionReqData);
		}

		/**
		 * Call Rule Service to Get the Flattened Templates which replaces template
		 * place holders by value of action and attributes
		 */
		List<ActionTempMappingDbData> actionResDataList = rulesService.getFlattenedTemplate(SessionHelper.getTenantId(),
				docId, docTypeCde, actionParamAttrMappingDbDataList, attributeDbDataList,
				attachmentAttributeDbDataList);
		
		{
			// Below logic is added as part of new design to store email body as an
			// attachment instead of attribute
			List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getDocAttachmentList(docId);

			// Get the attachment record that stores the email data
			AttachmentDbData attachmentDbData = AttachmentHelper.getEmailAttachment(attachmentDbDataList);
			String fileSystemContent = "";
			String emailAttachmentFileName = "";
			if (attachmentDbData != null) {
				attachmentDbData = attachProcess.getDocAttachmentFile(docId, attachmentDbData.getAttachmentId());
				byte[] contents = FileUtility.readFile(attachmentDbData.getPhysicalPath());
				fileSystemContent = new String(contents, StandardCharsets.UTF_8); // for UTF-8 encoding
				emailAttachmentFileName = AttachmentHelper.getEmailAttachmentFileName(attachmentDbData);
				// If email is stored as file, then the two placeholder texts will still be
				// present. Replace placeholder text with corresponding values
				for (ActionTempMappingDbData actionTempMappingDbData : actionResDataList) {
					if (emailAttachmentFileName.equalsIgnoreCase(WorkbenchConstants.EMAIL_BODY_HTML_STANDARD_FILE_NAME)) {
						String modifiedString = actionTempMappingDbData.getTemplateHtml()
								.replaceFirst(WorkbenchConstants.PLACEHOLDER_CONTENT_HTML, fileSystemContent);
						actionTempMappingDbData.setTemplateHtml(modifiedString);
					} else if (emailAttachmentFileName
							.equalsIgnoreCase(WorkbenchConstants.EMAIL_BODY_TXT_STANDARD_FILE_NAME)) {
						String modifiedString = actionTempMappingDbData.getTemplateText()
								.replaceFirst(WorkbenchConstants.PLACEHOLDER_CONTENT_TXT, fileSystemContent);
						actionTempMappingDbData.setTemplateText(modifiedString);
	
						modifiedString = actionTempMappingDbData.getTemplateHtml()
								.replaceFirst(WorkbenchConstants.PLACEHOLDER_CONTENT_TXT, fileSystemContent);
						actionTempMappingDbData.setTemplateHtml(modifiedString);
	
					}
				}
			}
		}
		
		/**
		 * From the Rule Response template replace the filename into encrypted image
		 * from attachment file location
		 */
		replaceFileNameToImage(docId, actionResDataList);
		logger.info("end of call after templates" + System.nanoTime());
		return actionResDataList;
	}

	private void replaceFileNameToImage(long docId, List<ActionTempMappingDbData> actionResDataList)
			throws WorkbenchException {
		List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getDocAttachmentList(docId);
		if (attachmentDbDataList != null && attachmentDbDataList.size() > 0) {
			for (ActionTempMappingDbData actionTempMappingDbData : actionResDataList) {
				actionTempMappingDbData.setTemplateText(
						replaceFileNames(docId, actionTempMappingDbData.getTemplateText(), attachmentDbDataList));
				actionTempMappingDbData.setTemplateHtml(
						replaceFileNames(docId, actionTempMappingDbData.getTemplateHtml(), attachmentDbDataList));
			}
		}
	}

	private String replaceFileNames(long docId, String emailHeaderAndBody,
			List<AttachmentDbData> attachmentDbDataList) {
		// Converting Inline image logical name to physical name
		List<String> fileList = PatternUtility.getHtmlImgSrcValues(emailHeaderAndBody);
		List<String> physicalFileNameList = new ArrayList<>();
		for (String fileName : fileList) {
			for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
				if (attachmentDbData.isInlineImage()) {
					if (fileName.equalsIgnoreCase(attachmentDbData.getLogicalName())) {
						emailHeaderAndBody = emailHeaderAndBody.replace(fileName, attachmentDbData.getPhysicalName());
						physicalFileNameList.add(attachmentDbData.getPhysicalName());
					}
				}
			}
		}
		String filePath = null;
		for (String fileName : physicalFileNameList) {
			String[] strings = fileName.split("\\.");
			String fileExtension = StringUtility.getBase64Extension1(strings[1]);
			String base64String = "";
			try {
				// Reading a Image file from file system and convert it into Base64 String
				filePath = FileUtility.getConcatenatedName(attachmentFilePath, fileName);
				byte[] imageData = FileUtility.readFile(filePath);
				base64String = fileExtension + "," + Base64.getEncoder().encodeToString(imageData);
				emailHeaderAndBody = emailHeaderAndBody.replace(fileName, base64String);
			} catch (Exception e) {
				logger.error("Exception while reading the Image " + e);
			}
		}

		return emailHeaderAndBody;
	}
}
