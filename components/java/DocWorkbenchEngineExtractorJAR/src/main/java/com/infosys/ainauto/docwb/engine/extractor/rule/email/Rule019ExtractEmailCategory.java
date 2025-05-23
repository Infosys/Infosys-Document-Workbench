/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.GenericAttributeEntityRelData;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.DocumentDownloaderData;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.ml.email.IEmailClassifyService;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule019ExtractEmailCategory extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule019ExtractEmailCategory.class);
	
	@Autowired
	private IEmailClassifyService emailClassifyService;
	
	@Autowired
	private DownloaderQueueMappingConfig downloaderQueueMappingConfig;
	
	// Variables to store compiled regex pattern objects
	private List<Map<String, Pattern>> subjToPatternObjMapList = new ArrayList<>();
	private List<Map<String, Pattern>> bodyToPatternObjMapList = new ArrayList<>();

	private Map<String, Map<String,String>> subjPatternToCategoryAttributeMap = new HashMap<>();
	private Map<String, Map<String,String>> bodyPatternToCategoryAttributeMap = new HashMap<>();
	
	@PostConstruct
	private void init() {

		DocumentDownloaderData documentDownloaderConfigData = downloaderQueueMappingConfig.getData()
				.getDownloaderQueueMapping().get(0).getDocumentDownloader();
		
		if (documentDownloaderConfigData.getCategoryAssignments() != null) {
			// Email Subject Pattern Loading
			Optional<GenericAttributeEntityRelData> categoryAssignmentConfigData = documentDownloaderConfigData
					.getCategoryAssignments().stream()
					.filter(x -> x.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()
							&& x.getEntity().contentEquals(
									DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumEntity.EMAIL_SUBJECT_REGEX_PATTERN
											.getValue()))
					.findFirst();
			if (categoryAssignmentConfigData.isPresent()) {
				for (Entry<String, Map<String,String>> entry: categoryAssignmentConfigData.get().getEntityToAttributeMap().entrySet()) {
					subjPatternToCategoryAttributeMap.put(entry.getKey(), entry.getValue());
					Pattern p = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
					HashMap<String, Pattern> map = new HashMap<String, Pattern>();
					map.put(entry.getKey(), p);
					subjToPatternObjMapList.add(map);
				}
			}
			
			// Email Body Pattern Loading
			categoryAssignmentConfigData = documentDownloaderConfigData.getCategoryAssignments().stream().filter(
					x -> x.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde() && x.getEntity().contentEquals(
							DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumEntity.EMAIL_BODY_REGEX_PATTERN
									.getValue()))
					.findFirst();
			if (categoryAssignmentConfigData.isPresent()) {
				for (Entry<String, Map<String,String>> entry: categoryAssignmentConfigData.get().getEntityToAttributeMap().entrySet()) {
					bodyPatternToCategoryAttributeMap.put(entry.getKey(), entry.getValue());
					Pattern p = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
					HashMap<String, Pattern> map = new HashMap<String, Pattern>();
					map.put(entry.getKey(), p);
					bodyToPatternObjMapList.add(map);
				}
			}
		}
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = new EmailData();
		Object obj = objList.get(0);
		if (obj instanceof EmailData) {
			emailData = (EmailData) objList.get(0);
		} else if (obj instanceof DocumentData) {
			DocumentData documentData = (DocumentData) objList.get(0);
			emailData.setDocId(documentData.getDocId());
			for (AttributeData data : documentData.getAttributes()) {
				if (data.getAttrNameCde() == EnumSystemAttributeName.CONTENT_HTML.getCde())
					emailData.setEmailBodyHtml(data.getAttrValue());
				else if (data.getAttrNameCde() == EnumSystemAttributeName.CONTENT.getCde())
					emailData.setEmailBodyText(data.getAttrValue());
				else if (data.getAttrNameCde() == EnumSystemAttributeName.SUBJECT.getCde())
					emailData.setEmailSubject(data.getAttrValue());
			}
		} else {
			Exception ex = new Exception("Unknown object passed as request");
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
			return;
		}
		
		AttributeData attributeData = new AttributeData();
		
		String categoryValue = "";
		float confidencePct = EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED;
		
		
		// 1. Use regex pattern match on email SUBJECT
		String patternThatMatched = getKeyForFirstPatternMatch(emailData.getEmailSubject(), subjToPatternObjMapList);
		if (StringUtility.hasTrimmedValue(patternThatMatched)) {
			categoryValue = subjPatternToCategoryAttributeMap.get(patternThatMatched)
					.get(DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumPropertyName.ATTR_VALUE
							.getValue());
			String confPctStr = subjPatternToCategoryAttributeMap.get(patternThatMatched)
					.get(DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumPropertyName.CONFIDENCE_PCT
							.getValue());
			if (StringUtility.isNumber(confPctStr)) {
				confidencePct = Float.valueOf(confPctStr);
			}

		}

		// 2. Use regex pattern match on email BODY
		if (!StringUtility.hasTrimmedValue(categoryValue)) {
			patternThatMatched = getKeyForFirstPatternMatch(emailData.getEmailBodyText(), bodyToPatternObjMapList);
			if (StringUtility.hasTrimmedValue(patternThatMatched)) {
				categoryValue = bodyPatternToCategoryAttributeMap.get(patternThatMatched)
						.get(DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumPropertyName.ATTR_VALUE
								.getValue());
				String confPctStr = bodyPatternToCategoryAttributeMap.get(patternThatMatched).get(
						DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumPropertyName.CONFIDENCE_PCT
								.getValue());
				if (StringUtility.isNumber(confPctStr)) {
					confidencePct = Float.valueOf(confPctStr);
				}
			}
		}
		
	
		if (StringUtility.hasTrimmedValue(categoryValue)) {
			attributeData.setAttrNameCde(EnumSystemAttributeName.CATEGORY.getCde()) // 19=Category
					.setAttrValue(categoryValue).setExtractType(EnumExtractType.CUSTOM_LOGIC)
					.setConfidencePct(confidencePct);
		} else { // 3. Use ML for category prediction
			AttributeData categoryAttributeData = emailClassifyService.getCategory(emailData);
			if (categoryAttributeData == null) {
				attributeData.setAttrNameCde(EnumSystemAttributeName.CATEGORY.getCde()) // 19=Category
						.setAttrValue(EngineExtractorConstants.ATTR_NAME_VALUE_UNKNOWN)
						.setExtractType(EnumExtractType.CUSTOM_LOGIC)
						.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
			} else {
				attributeData.setAttrNameCde(EnumSystemAttributeName.CATEGORY.getCde()) // 19=Category
						.setAttrValue(categoryAttributeData.getAttrValue()).setExtractType(EnumExtractType.CUSTOM_LOGIC)
						.setConfidencePct(categoryAttributeData.getConfidencePct());
			}
		}
		

		
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
	
	private String getKeyForFirstPatternMatch(String contentString, List<Map<String, Pattern>> keyToPatternObjMapList) {
		String keyForSuccessfulPatternMatch = "";
		if (StringUtility.hasTrimmedValue(contentString) && keyToPatternObjMapList != null
				&& !keyToPatternObjMapList.isEmpty()) {
			for (Map<String, Pattern> listItem : keyToPatternObjMapList) {
				for (Entry<String, Pattern> entry : listItem.entrySet()) {
					Pattern patternObj = entry.getValue();
					try {
						Matcher m = patternObj.matcher(contentString);
						if (m.find()) {
							keyForSuccessfulPatternMatch = entry.getKey();
							return keyForSuccessfulPatternMatch;
						}
					} catch (Exception ex) {
						logger.error("Error occurred while performing regex on pattern=" + patternObj.pattern(), ex);
					}
				}
			}
		}
		return keyForSuccessfulPatternMatch;
	}
}
