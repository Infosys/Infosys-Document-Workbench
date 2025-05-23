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
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;

@Component
public class Rule018ExtractInvoiceNumbers extends AttributeExtractRuleAsyncBase {
	private static final Logger logger = LoggerFactory.getLogger(Rule018ExtractInvoiceNumbers.class);

	@Autowired
	private Environment environment;

	@Autowired
	private DocwbExtractorService docwbExtractorService;

	// @Autowired
	// private IExtractService extractService;

	/*
	 * @Autowired private IInvoicePdfBotService invoicePdfBotService;
	 */

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private List<String> keywordsList = Arrays.asList("inv", "inv#", "invoice", "invoice#");
	private List<String> continuationList = Arrays.asList("and", "number", "status", "of");

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
		try {
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = (objList != null && objList.size() > 1) ? objList.get(1).toString() : "";
			DocumentData responseDocumentData = new DocumentData();
			String attachmentContent = "";
			List<AttachmentData> attachmentDataList = null;
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(), tempDownloadPath,
						EnumFileType.ALL);
				// Code for checking for attachment and extracting invoice number
				if (ListUtility.hasValue(attachmentDataList)) {
					// Go through attachments to see content can be extracted
					List<DocwbExtractorService.FilePathData> filePathDataList = new ArrayList<>();
					DocwbExtractorService.FilePathData filePathData = null;
					for (int i = 0; i < attachmentDataList.size(); i++) {
						filePathData = new DocwbExtractorService.FilePathData();
						filePathData.setFileName(attachmentDataList.get(i).getPhysicalName());
						filePathData.setFilePath(attachmentDataList.get(i).getPhysicalPath());
						filePathDataList.add(filePathData);
					}
					List<DocwbExtractorService.FileContentResData> fileContentDataList = docwbExtractorService
							.getFileContent(filePathDataList);
					if (ListUtility.hasValue(fileContentDataList)) {
						List<AttachmentData> attachmentAttrDataList = new ArrayList<>();
						for (DocwbExtractorService.FileContentResData fileContentData : fileContentDataList) {
							attachmentContent = fileContentData.getFileContent() + " ";
							if (StringUtility.hasTrimmedValue(attachmentContent)) {
								for (AttachmentData attachmentData : attachmentDataList) {
									if (attachmentData.getPhysicalName().equals(fileContentData.getFileName())) {
										AttachmentData attachmentAttrData = new AttachmentData();
										AttributeData attributeData = extractUsingRegex(attachmentContent);
										List<AttributeData> attributeDataList = new ArrayList<>(
												Arrays.asList(attributeData));
										attachmentAttrData.setAttachmentId(attachmentData.getAttachmentId());
										attachmentAttrData.setAttributes(attributeDataList);
										attachmentAttrDataList.add(attachmentAttrData);
										break;
									}
								}
							}
						}
						responseDocumentData.setAttachmentDataList(attachmentAttrDataList);
					}
					/*
					 * else { documentData.setAttachmentDataList(attachmentDataList);
					 * invoicePdfBotService.getInvoiceFromPdf(documentData, new
					 * IDocwbEngineCallback() {
					 * 
					 * @Override public void doCallback(Exception exception, Object... data) { if
					 * (data[0] != null) { InputStream is = (InputStream) data[0]; ExcelFileReader
					 * excelFileReader = new ExcelFileReader(is); while
					 * (excelFileReader.hasMoreRows()) { Map<String, Object> rowMap =
					 * excelFileReader.getNextRow(); if (rowMap.containsKey("2")) { String value =
					 * (String) rowMap.get("2"); pdfInvoiceNoList.add(value); } }
					 * excelFileReader.close(); } sendResponse(attributeExtractRuleListener,
					 * documentData, pdfInvoiceNoList); } }); }
					 */
				}
			}
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
				String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
				String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content
				String textToProcess = subject + " " + content;
				AttributeData attributeData = extractUsingRegex(textToProcess);
				List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
				responseDocumentData.setAttributes(attributeDataList);
			}
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);

		} catch (Exception e) {
			logger.error("Error occurred in extracting data", e);
			attributeExtractRuleListener.onAttributeExtractionComplete(e, null);
		}

	}

	private AttributeData extractUsingRegex(String contentString) {

		AttributeData attributeData = null;
		AttributeData invoiceAttributeData = new AttributeData();
		// Set default confidence as 90%
		invoiceAttributeData.setConfidencePct(90);
		List<String> outputList = new ArrayList<String>();
		// Step 1 - Remove all non alphanumeric characters
		contentString = contentString.replaceAll("[^A-Za-z0-9]", " ");
		logger.debug(contentString);

		// Step 2 - Convert all words to token
		StringTokenizer st = new StringTokenizer(contentString.toLowerCase());
		String token = "";
		boolean isPotentialMatch = false;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			if (isPotentialMatch) {
				if (isNumber(token) && token.length() > 3) {
					outputList.add(token);
				} else if (!continuationList.contains(token)) {
					isPotentialMatch = false;
				}
			}
			if (keywordsList.contains(token)) {
				isPotentialMatch = true;
			}
		}

		// Commenting out call to NER service as it is extracting unnecessary data in
		// pdf.
		// AttributeData invoiceAttributeData = extractService.getExtract(textToProcess,
		// entityList);
		// if (invoiceAttributeData != null && invoiceAttributeData.getAttrValue() !=
		// null) {
		// attrValue = invoiceAttributeData.getAttrValue();
		// if (attrValue.contains(",")) {
		// String[] b = attrValue.split(",");
		// for (int i = 0; i < b.length; i++) {
		// outputList.add(b[i]);
		// }
		// } else {
		// outputList.add(attrValue);
		// }
		// }

		if (outputList.size() > 0) {
			// Remove duplicates
			outputList = outputList.stream().distinct().collect(Collectors.toList());
			// Convert list to csv
			String result = outputList.stream().collect(Collectors.joining(","));
			// Assume 80% confidence for OCR / RegEx
			float confidencePct = (invoiceAttributeData != null & invoiceAttributeData.getAttrValue() != null)
					? invoiceAttributeData.getConfidencePct()
					: 80f;
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_INVOICE_NUMBERS) // 18=Invoice Numbers
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(confidencePct);
		}
		return attributeData;
	}

	private static boolean isNumber(String data) {
		if (data == null || data.length() == 0) {
			return false;
		}
		String regex = "\\d+";
		return data.matches(regex);
	}

}
