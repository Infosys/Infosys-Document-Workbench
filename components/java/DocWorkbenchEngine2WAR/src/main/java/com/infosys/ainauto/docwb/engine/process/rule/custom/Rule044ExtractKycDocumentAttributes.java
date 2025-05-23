/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.service.sample.FileAttributeExtractorService;
import com.infosys.ainauto.docwb.engine.service.sample.IFileAttributeExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule044ExtractKycDocumentAttributes extends AttributeExtractRuleAsyncBase {

	@Autowired
	private Environment environment;

	@Autowired
	private IFileAttributeExtractorService iFileAttributeExtractorService;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private static final Logger logger = LoggerFactory.getLogger(Rule044ExtractKycDocumentAttributes.class);

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		try {
			DocumentData responseDocumentData = new DocumentData();

			String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
			DocumentData documentData = (DocumentData) objList.get(0);
			List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(),
					tempDownloadPath, EnumFileType.SUPPORTED);

			// Step 1 - Check if attachments are present
			if (ListUtility.hasValue(attachmentDataList)) {

				// Step 2 - Make service call to get extracted data
				// Go through attachments to see content can be extracted
				FileAttributeExtractorService.FilePathData filePathData = null;
				FileAttributeExtractorService fileExtractorService = new FileAttributeExtractorService();

				// Only one attachment expected
				AttachmentData attachmentData = AttachmentDataHelper.getMainAttachmentData(attachmentDataList);
				filePathData = fileExtractorService.new FilePathData();
				filePathData.setFileName(attachmentData.getPhysicalName());
				filePathData.setFilePath(attachmentData.getPhysicalPath());

				List<FileAttributeExtractorService.AttributeData> fileAttributeDataList = iFileAttributeExtractorService
						.extractAttributes(filePathData);

				List<AttributeData> attachmentAttributeDataList = new ArrayList<>();
				// Step 3 - Check if attributes are present
				if (ListUtility.hasValue(fileAttributeDataList)) {
					Optional<FileAttributeExtractorService.AttributeData> optionalDocTypeAttribute = fileAttributeDataList
							.stream().filter(item -> item.getAttrName().equals("DocumentType")).findAny();

					// Check if document type is present in list
					if (optionalDocTypeAttribute.isPresent()) {
						// Remove document type from list for further processing
						fileAttributeDataList.removeIf(item -> item.getAttrName().equals("DocumentType"));
					}

					// Step 4 - Check if more attributes are present
					if (ListUtility.hasValue(fileAttributeDataList)) {
						List<AttributeData> innerAttributeDataList = new ArrayList<>();
						// Add all attributes as nested attributes
						for (FileAttributeExtractorService.AttributeData fileAttributeData : fileAttributeDataList) {
							AttributeData innerAttributeData = new AttributeData();
							innerAttributeData.setAttrNameTxt(fileAttributeData.getAttrName());
							innerAttributeData.setAttrValue(fileAttributeData.getAttrValue());
							innerAttributeData.setConfidencePct(fileAttributeData.getConfidencePct());
							innerAttributeData.setExtractType(EnumExtractType.CUSTOM_LOGIC);
							innerAttributeDataList.add(innerAttributeData);
						}

						// Add all nested attributes under single multi-attribute
						AttributeData attributeData = new AttributeData();
						attributeData.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
								.setAttrNameTxt(EngineConstants.GROUP_NAME_KYC)
								.setConfidencePct(EngineConstants.CONFIDENCE_PCT_UNDEFINED)
								.setExtractType(EnumExtractType.DIRECT_COPY)
								.setAttributeDataList(innerAttributeDataList);
						attachmentAttributeDataList.add(attributeData);
					}

					// Finally, add attachmentAttributeDataList to attachment object
					AttachmentData resultAttachmentData = new AttachmentData();
					resultAttachmentData.setAttachmentId(attachmentData.getAttachmentId());
					resultAttachmentData.setAttributes(attachmentAttributeDataList);
					responseDocumentData.setAttachmentDataList(new ArrayList<>());
					responseDocumentData.getAttachmentDataList().add(resultAttachmentData);

					attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);

				} else {
					attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
				}

			} else {
				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
			}
		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		}

	}

}
