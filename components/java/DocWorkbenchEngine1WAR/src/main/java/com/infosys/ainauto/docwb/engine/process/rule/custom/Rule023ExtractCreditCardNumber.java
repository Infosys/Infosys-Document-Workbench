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
public class Rule023ExtractCreditCardNumber extends AttributeExtractRuleAsyncBase {
	private static final Logger logger = LoggerFactory.getLogger(Rule023ExtractCreditCardNumber.class);
	@Autowired
	private Environment environment;

	@Autowired
	private DocwbExtractorService docwbExtractorService;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private List<String> keywordsList = Arrays.asList("Credit card", "credit card", "number", "card", "CC-",
			"Credit card", "No", "no");
	private List<String> continuationList = Arrays.asList("and", "card", "no");

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
				if (ListUtility.hasValue(attachmentDataList)) {
					// Go through attachments to see content can be extracted
					List<DocwbExtractorService.FilePathData> filePathDataList = new ArrayList<>();
					DocwbExtractorService.FilePathData fileData = null;

					for (AttachmentData attachmentData : attachmentDataList) {
						fileData = new DocwbExtractorService.FilePathData();
						fileData.setFileName(attachmentData.getPhysicalName());
						fileData.setFilePath(attachmentData.getPhysicalPath());
						filePathDataList.add(fileData);
					}

					List<DocwbExtractorService.FileContentResData> fileContentDataList = docwbExtractorService
							.getFileContent(filePathDataList);
					if (ListUtility.hasValue(fileContentDataList)) {
						List<AttachmentData> attachmentAttrDataList = new ArrayList<>();
						for (DocwbExtractorService.FileContentResData fileContentData : fileContentDataList) {
							attachmentContent = fileContentData.getFileContent();
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
				}
			}
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
				String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
				String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content
				String searchInWhere = subject + " " + content;
				AttributeData attributeData = extractUsingRegex(searchInWhere);
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
		List<String> outputList = new ArrayList<String>();
		// Step 1 - Remove all non alphanumeric characters
		contentString = contentString.replaceAll("[^A-Za-z0-9]", " ");
		logger.debug(contentString);

		// Step 2 - Convert all words to token
		StringTokenizer st = new StringTokenizer(contentString.toLowerCase());
		String token = "";
		List<String> nToken = new ArrayList<String>();
		boolean isPotentialMatch = false;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			// nToken=token;
			if (isPotentialMatch) {
				if (isNumber(token) && token.length() > 3 && token.length() < 5) {
					nToken.add(token);
					continue;
					// outputList.add(token);
				} else if (!continuationList.contains(token)) {
					isPotentialMatch = false;
				}
			}

			if (keywordsList.contains(token)) {
				isPotentialMatch = true;
			}
		}
		if (nToken.size() > 0) {
			String fToken = nToken.stream().collect(Collectors.joining("-"));
			outputList.add(fToken);
		}
		if (outputList.size() > 0) {
			String result = outputList.stream().collect(Collectors.joining(","));
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_CREDITCARD_NUMBER) // 23=Credit card number
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(90);
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