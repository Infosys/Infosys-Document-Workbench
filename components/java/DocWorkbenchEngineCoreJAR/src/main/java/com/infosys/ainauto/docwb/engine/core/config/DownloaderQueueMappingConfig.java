/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.config;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;

@Component
public class DownloaderQueueMappingConfig {

	private static Logger LOGGER = LoggerFactory.getLogger(DownloaderQueueMappingConfig.class);
	private static final String CONFIG_FILE_NAME = "downloaderQueueMappingConfig.json";
	private DownloaderQueueMappingConfigData queueMappingData = null;

	public DownloaderQueueMappingConfig() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		String fileContent = FileUtility.readResourceFile(CONFIG_FILE_NAME);

		// Store as Java object
		queueMappingData = mapper.readValue(fileContent, DownloaderQueueMappingConfigData.class);

		LOGGER.info("{} file read successfully", CONFIG_FILE_NAME);
	}

	public DownloaderQueueMappingConfigData getData() {
		return queueMappingData;
	}

	public static class DownloaderQueueMappingConfigData {
		
		public enum EnumEntity {
			QUEUE_NAME_CDE("queueNameCde"), 
			USER_ID("userId"), 
			EMAIL_SUBJECT_REGEX_PATTERN("emailSubjectRegExPattern"), 
			EMAIL_BODY_REGEX_PATTERN("emailBodyRegExPattern"), 
			DROP_FOLDER_NAME("dropFolderName");

			private String propertyValue;

			private EnumEntity(String s) {
				propertyValue = s;
			}

			public String getValue() {
				return propertyValue;
			}
		}
		
		public enum EnumPropertyName {
			ATTR_NAME_CDE("attrNameCde"),
			ATTR_VALUE("attrValue"),
			CONFIDENCE_PCT("confidencePct");
			
			private String propertyValue;

			private EnumPropertyName(String s) {
				propertyValue = s;
			}

			public String getValue() {
				return propertyValue;
			}
		}
		
		private List<DownloaderQueueMappingData> downloaderQueueMapping;
		
		public List<DownloaderQueueMappingData> getDownloaderQueueMapping() {
			return downloaderQueueMapping;
		}

		public void setDownloaderQueueMapping(List<DownloaderQueueMappingData> downloaderQueueMapping) {
			this.downloaderQueueMapping = downloaderQueueMapping;
		}

		public static class DocumentDownloaderData {
			private List<String> defaultRules;
			private List<GenericAttributeEntityRelData> categoryAssignments;
			private List<GenericAttributeEntityRelData> queueAssignments;

			public List<String> getDefaultRules() {
				return defaultRules;
			}

			public void setDefaultRules(List<String> defaultRules) {
				this.defaultRules = defaultRules;
			}

			public List<GenericAttributeEntityRelData> getCategoryAssignments() {
				return categoryAssignments;
			}

			public void setCategoryAssignments(List<GenericAttributeEntityRelData> categoryAssignments) {
				this.categoryAssignments = categoryAssignments;
			}

			public List<GenericAttributeEntityRelData> getQueueAssignments() {
				return queueAssignments;
			}

			public void setQueueAssignments(List<GenericAttributeEntityRelData> queueAssignments) {
				this.queueAssignments = queueAssignments;
			}
		}

		public static class AttributeExtractorData {
			private List<String> defaultRules;
			private List<ConditionalRuleData> conditionalRules;

			public List<String> getDefaultRules() {
				return defaultRules;
			}

			public void setDefaultRules(List<String> defaultRules) {
				this.defaultRules = defaultRules;
			}

			public List<ConditionalRuleData> getConditionalRules() {
				return conditionalRules;
			}

			public void setConditionalRules(List<ConditionalRuleData> conditionalRules) {
				this.conditionalRules = conditionalRules;
			}
		}
		
		public static class CaseOpenerData {
			private List<GenericAttributeEntityRelData> userAssignments;
			private boolean isAutoTriggerAction;

			public List<GenericAttributeEntityRelData> getUserAssignments() {
				return userAssignments;
			}

			public void setUserAssignments(List<GenericAttributeEntityRelData> userAssignments) {
				this.userAssignments = userAssignments;
			}

			public boolean getIsAutoTriggerAction() {
				return isAutoTriggerAction;
			}

			public void setIsAutoTriggerAction(boolean isAutoTriggerAction) {
				this.isAutoTriggerAction = isAutoTriggerAction;
			}
		}
		
		public static class DownloaderQueueMappingData {
			private DocumentDownloaderData documentDownloader;
			private AttributeExtractorData attributeExtractor;
			private CaseOpenerData caseOpener;

			public DocumentDownloaderData getDocumentDownloader() {
				return documentDownloader;
			}

			public void setDocumentDownloader(DocumentDownloaderData documentDownloader) {
				this.documentDownloader = documentDownloader;
			}

			public AttributeExtractorData getAttributeExtractor() {
				return attributeExtractor;
			}

			public void setAttributeExtractor(AttributeExtractorData attributeExtractor) {
				this.attributeExtractor = attributeExtractor;
			}

			public CaseOpenerData getCaseOpener() {
				return caseOpener;
			}

			public void setCaseOpener(CaseOpenerData caseOpener) {
				this.caseOpener = caseOpener;
			}
		}
		
		public static class GenericAttributeEntityRelData {
			private String entity;
			private int attrNameCde;
			// To store one-to-many relationships
			private Map<String, List<String>> attrValueToEntitiesMap;
			// To store one-to-one relationship
			private Map<String, String> attrValueToEntityMap;
			// To store one-to-one hash map relationship
			private Map<String, Map<String,String>> entityToAttributeMap;
			private int noMatchEntity;
			
			public int getAttrNameCde() {
				return attrNameCde;
			}
			public void setAttrNameCde(int attrNameCde) {
				this.attrNameCde = attrNameCde;
			}
			public String getEntity() {
				return entity;
			}
			public void setEntity(String entity) {
				this.entity = entity;
			}
			public Map<String, List<String>> getAttrValueToEntitiesMap() {
				return attrValueToEntitiesMap;
			}
			public void setAttrValueToEntitiesMap(Map<String, List<String>> attrValueToEntitiesMap) {
				this.attrValueToEntitiesMap = attrValueToEntitiesMap;
			}
			public int getNoMatchEntity() {
				return noMatchEntity;
			}
			public void setNoMatchEntity(int noMatchEntity) {
				this.noMatchEntity = noMatchEntity;
			}
			public Map<String, String> getAttrValueToEntityMap() {
				return attrValueToEntityMap;
			}
			public void setAttrValueToEntityMap(Map<String, String> attrValueToEntityMap) {
				this.attrValueToEntityMap = attrValueToEntityMap;
			}
			public Map<String, Map<String,String>> getEntityToAttributeMap() {
				return entityToAttributeMap;
			}
			public void setEntityToAttributeMap(Map<String, Map<String,String>> entityToAttributeMap) {
				this.entityToAttributeMap = entityToAttributeMap;
			}
		}
		
		public static class ConditionalRuleData {
			private int attrNameCde;
			private Map<String, List<String>> attrValueToRuleMap;
			private List<String> noMatchRules;

			public int getAttrNameCde() {
				return attrNameCde;
			}

			public void setAttrNameCde(int attrNameCde) {
				this.attrNameCde = attrNameCde;
			}

			public Map<String, List<String>> getAttrValueToRuleMap() {
				return attrValueToRuleMap;
			}

			public void setAttrValueToRuleMap(Map<String, List<String>> attrValueToRuleMap) {
				this.attrValueToRuleMap = attrValueToRuleMap;
			}

			public List<String> getNoMatchRules() {
				return noMatchRules;
			}

			public void setNoMatchRules(List<String> noMatchRules) {
				this.noMatchRules = noMatchRules;
			}
		}
	}
}
