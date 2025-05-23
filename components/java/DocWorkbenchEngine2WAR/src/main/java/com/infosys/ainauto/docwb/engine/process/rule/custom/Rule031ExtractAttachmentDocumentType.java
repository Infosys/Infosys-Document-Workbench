/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule031ExtractAttachmentDocumentType extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule031ExtractAttachmentDocumentType.class);

	@Autowired
	private Environment environment;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		try {
			DocumentData responseDocumentData = new DocumentData();
			String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
			DocumentData documentData = (DocumentData) objList.get(0);

			List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(),
					tempDownloadPath, EnumFileType.SUPPORTED);
			if (!ListUtility.hasValue(attachmentDataList)) {
				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
				return;
			}

			AttributeData categoryAttributeData = null;
			for (AttributeData attributeData : documentData.getAttributes()) {
				if (attributeData.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()) {
					categoryAttributeData = attributeData;
					break;
				}
			}

			if (categoryAttributeData != null) {
				AttributeData fileAttributeData = new AttributeData();
				fileAttributeData.setAttrNameCde(EnumSystemAttributeName.DOCUMENT_TYPE.getCde())
						.setExtractType(EnumExtractType.CUSTOM_LOGIC).setAttrValue(categoryAttributeData.getAttrValue())
						.setConfidencePct(categoryAttributeData.getConfidencePct());

				List<AttributeData> attachmentAttributeDataList = new ArrayList<>();
				attachmentAttributeDataList.add(fileAttributeData);

				AttachmentData resultAttachmentData = new AttachmentData();
				long attachmentId = AttachmentDataHelper.getMainAttachmentData(attachmentDataList).getAttachmentId();
				resultAttachmentData.setAttachmentId(attachmentId);
				resultAttachmentData.setAttributes(attachmentAttributeDataList);
				responseDocumentData.setAttachmentDataList(new ArrayList<>());
				responseDocumentData.getAttachmentDataList().add(resultAttachmentData);
				/** incase of removing category to document enable the below lines */
				// AttributeData categoryAttributeData1 = new AttributeData();
				// categoryAttributeData1.setAttrNameCde(EngineConstants.ATTR_CATEGORY);
				//
				// List<AttributeData> attributeDataList = new
				// ArrayList<>(Arrays.asList(categoryAttributeData1));
				// responseDocumentData.setAttributes(attributeDataList);

				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
			} else {
				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
			}

		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		}
	}
}
