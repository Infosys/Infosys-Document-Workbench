/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.service.ml.table.ITableDataExtractorService;
import com.infosys.ainauto.docwb.engine.extractor.service.ml.table.TableDataExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule106ExtractRemittanceInvoice extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule106ExtractRemittanceInvoice.class);
	@Autowired
	private ITableDataExtractorService tableDataExtractorService;
	@Autowired
	private Environment environment;
	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
		try {
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = (objList != null && objList.size() > 1) ? objList.get(1).toString() : "";

			List<AttachmentData> attachmentAttrDataList = null;
			DocumentData responseDocumentData = new DocumentData();
			// Code for checking for attachment and extracting invoice number
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(),
						tempDownloadPath, EnumFileType.SUPPORTED);
				if (ListUtility.hasValue(attachmentDataList)) {
					List<TableDataExtractorService.AttachmentData> reqDataList = new ArrayList<>();
					TableDataExtractorService.AttachmentData attachData = new TableDataExtractorService.AttachmentData();
					BeanUtils.copyProperties(AttachmentDataHelper.getMainAttachmentData(attachmentDataList),
							attachData);
					reqDataList.add(attachData);

					List<TableDataExtractorService.AttachmentData> invoiceAttributeDataList = tableDataExtractorService
							.getTabularData(reqDataList);

					if (invoiceAttributeDataList == null || invoiceAttributeDataList.size() <= 0) {
						attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
					}

					// Finally, add attachmentAttributeDataList to attachment object
					AttachmentData resultAttachmentData = new AttachmentData();
					resultAttachmentData.setAttachmentId(attachData.getAttachmentId());
					resultAttachmentData.setAttributes(Arrays.asList(AttributeDataHelper.createMultipleAttibuteElement(
							EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde(), EngineConstants.GROUP_NAME_INVOICES,
							invoiceAttributeDataList.get(0).getAttributes())));
					responseDocumentData.setAttachmentDataList(new ArrayList<>());
					responseDocumentData.getAttachmentDataList().add(resultAttachmentData);

					attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
				}
			} else {
				responseDocumentData.setAttachmentDataList(attachmentAttrDataList);
				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
			}
		} catch (Exception e) {
			logger.error("Error occurred in Rule106ExtractRemittanceInvoice", e);
			attributeExtractRuleListener.onAttributeExtractionComplete(e, null);
		}
	}
}
