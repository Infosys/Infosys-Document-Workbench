/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.extractor.rule.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService.FileContentResData;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.IDocwbExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileMetadata;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumFileUnsupported;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule033ExtractFileMetadata extends AttributeExtractRuleAsyncBase {

	@Autowired
	private Environment environment;

	@Autowired
	private IDocwbExtractorService iDocwbExtractorService;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private static final String UNSUPPORTED_EMAIL_EXTENSION = "file.eml.extension.to.html";
	private static final String UNSUPPORTED_WORD_EXTENSION = "file.word.extension.to.pdf";

	private static final Logger logger = LoggerFactory.getLogger(Rule033ExtractFileMetadata.class);

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		try {
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = (objList != null && objList.size() > 1) ? objList.get(1).toString() : "";

			String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));

			DocumentData responseDocumentData = new DocumentData();
			List<AttachmentData> attachmentDataList1 = new ArrayList<>();
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(),
						tempDownloadPath, EnumFileType.ALL);
				if (ListUtility.hasValue(attachmentDataList)) {
					// Overall unsupportedFormatList configured in web api.
					List<String> unsupportedFormatList = new ArrayList<>();
					Arrays.asList(EnumFileUnsupported.values())
							.forEach(fileType -> unsupportedFormatList.add(fileType.getValue().toLowerCase()));
					List<String> unSupportedWordList = Arrays
							.asList(environment.getProperty(UNSUPPORTED_WORD_EXTENSION).split(","));
					List<String> unSupportedEmailList = Arrays
							.asList(environment.getProperty(UNSUPPORTED_EMAIL_EXTENSION).split(","));

					DocwbExtractorService.FilePathData filePathData = null;
					for (AttachmentData attachmentData : attachmentDataList) {
						long attachmentId = attachmentData.getAttachmentId();
						String fileExtension = FileUtility.getFileExtension(attachmentData.getLogicalName())
								.toLowerCase();
						// If unsupported format then in UI need to do manual process instead of
						// automation.
						if (unsupportedFormatList.contains(fileExtension)) {
							AttributeData attributeData = new AttributeData();
							String attrValue;
							if (unSupportedWordList.contains(fileExtension)) {
								attrValue = EnumFileMetadata.MS_WORD.getValue();
							} else if (unSupportedEmailList.contains(fileExtension)) {
								attrValue = EnumFileMetadata.EMAIL.getValue();
							} else {
								throw new DocwbEngineException("MetaData assignment failed for Unsupported extension");
							}
							attributeData.setAttrNameCde(EnumSystemAttributeName.FILE_METADATA.getCde())
									.setAttrValue(attrValue).setExtractType(EnumExtractType.DIRECT_COPY)
									.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
							addAttachmentDataToList(attachmentDataList1, attributeData, attachmentId);
						} else if (attachmentData.getPhysicalName().toLowerCase(Locale.ENGLISH)
								.endsWith(EngineExtractorConstants.FILE_EXTENSION_PDF)) {
							filePathData = new DocwbExtractorService.FilePathData();
							filePathData.setFileName(attachmentData.getPhysicalName());
							filePathData.setFilePath(attachmentData.getPhysicalPath());
							List<DocwbExtractorService.FilePathData> filePathDataList = new ArrayList<>();
							filePathDataList.add(filePathData);
							List<FileContentResData> responseDataList = iDocwbExtractorService
									.getFileContent(filePathDataList);
							if (ListUtility.hasValue(responseDataList)) {
								FileContentResData responseData = responseDataList.get(0);
								String content = responseData.getFileContent().replaceAll("\n ", "").trim();
								AttributeData attributeData = new AttributeData();
								attributeData.setAttrNameCde(EnumSystemAttributeName.FILE_METADATA.getCde())
										.setAttrValue(EnumFileMetadata.PDF_NATIVE.getValue())
										.setExtractType(EnumExtractType.DIRECT_COPY)
										.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
								// if received file content is empty then consider it as scanned pdf.
								if (content.isEmpty()) {
									attributeData.setAttrValue(EnumFileMetadata.PDF_SCANNED.getValue());
								}
								addAttachmentDataToList(attachmentDataList1, attributeData, attachmentId);
							}
						}
					}
				}
			}
			responseDocumentData.setAttachmentDataList(attachmentDataList1);
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		}

	}

	private void addAttachmentDataToList(List<AttachmentData> attachmentDataList, AttributeData attributeData,
			long attachmentId) {
		List<AttributeData> attachmentAttributeDataList = new ArrayList<>();
		attachmentAttributeDataList.add(attributeData);
		AttachmentData resultAttachmentData = new AttachmentData();
		resultAttachmentData.setAttachmentId(attachmentId);
		resultAttachmentData.setAttributes(attachmentAttributeDataList);
		attachmentDataList.add(resultAttachmentData);
	}
}
