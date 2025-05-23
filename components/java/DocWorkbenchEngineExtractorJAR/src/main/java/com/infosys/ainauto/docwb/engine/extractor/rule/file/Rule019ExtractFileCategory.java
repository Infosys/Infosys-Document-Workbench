/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.extractor.rule.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.DocumentDownloaderData;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.GenericAttributeEntityRelData;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.ml.document.IDocumentClassifyService;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.FileData;
import com.infosys.ainauto.docwb.web.data.InputData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule019ExtractFileCategory extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule019ExtractFileCategory.class);

	@Autowired
	private IDocumentClassifyService documentClassifyService;

	@Autowired
	private Environment environment;

	@Autowired
	private DownloaderQueueMappingConfig downloaderQueueMappingConfig;

	private List<String> permSubfolders;

	private Map<String, List<String>> dropFolderAttrValueToEntityMapConfig;
	private static final String PERMANENT_SUBFOLDERS = "ds.file-system.reader.dir.source.permanent.subfolders";

	@PostConstruct
	private void init() {
		String permSubFoldersProperty = environment.getProperty(PERMANENT_SUBFOLDERS);
		// For email flow, this property will be empty so handle for NPE
		if (StringUtility.hasTrimmedValue(permSubFoldersProperty)) {
			permSubfolders = Arrays.asList(permSubFoldersProperty.split(","));
			// Convert to lower case and trim to remove any trailing/leading spaces
			permSubfolders = permSubfolders.stream().map(String::trim).map(String::toLowerCase)
					.collect(Collectors.toList());
		}

		DocumentDownloaderData documentDownloaderConfigData = downloaderQueueMappingConfig.getData()
				.getDownloaderQueueMapping().get(0).getDocumentDownloader();
		if (documentDownloaderConfigData.getCategoryAssignments() != null) {
			Optional<GenericAttributeEntityRelData> categoryAssignmentConfigData = documentDownloaderConfigData
					.getCategoryAssignments().stream()
					.filter(x -> x.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()
							&& x.getEntity().contentEquals(
									DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumEntity.DROP_FOLDER_NAME
											.getValue()))
					.findFirst();
			if (categoryAssignmentConfigData.isPresent()) {
				dropFolderAttrValueToEntityMapConfig = categoryAssignmentConfigData.get().getAttrValueToEntitiesMap();
			}
		}
	}

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		try {
			DocumentData responseDocumentData = new DocumentData();
			FileData fileData = new FileData();
			Object obj = objList.get(0);
			if (obj instanceof InputData) {
				InputData inputData = (InputData) obj;
				fileData = inputData.getFileData();
			} else {
				Exception ex = new Exception("Unknown object passed as request");
				attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
				return;
			}

			List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
			String subfolderName = fileData.getFileSubPath();// Folder considered as file queue

			AttributeData fileAttributeData = new AttributeData();
			fileAttributeData.setAttrNameCde(EnumSystemAttributeName.CATEGORY.getCde())
					.setExtractType(EnumExtractType.DIRECT_COPY)
					.setAttrValue(EngineExtractorConstants.ATTR_NAME_VALUE_UNKNOWN)
					.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
			/**
			 * If files placed under permanent subfolders skip to call the python-document
			 * classifier service
			 */
			if (!StringUtility.hasTrimmedValue(subfolderName)
					|| !permSubfolders.contains(subfolderName.toLowerCase())) {
				AttributeData attributeDataRes = documentClassifyService.getDocumentType(fileData);
				if (attributeDataRes != null) {
					fileAttributeData.setAttrValue(attributeDataRes.getAttrValue())
							.setConfidencePct(attributeDataRes.getConfidencePct());
				}
			} else {
				String value = "";
				if (dropFolderAttrValueToEntityMapConfig != null && !dropFolderAttrValueToEntityMapConfig.isEmpty()) {
					for (Entry<String, List<String>> entry : dropFolderAttrValueToEntityMapConfig.entrySet()) {
						long matchCount = entry.getValue().stream()
								.filter(s -> s.equalsIgnoreCase(subfolderName.trim())).count();
						if (matchCount == 1) {
							value = entry.getKey();
							break;
						}

					}
				}
				// if value not set for folder name in prop file the default values will be
				// unknown
				if (StringUtility.hasTrimmedValue(value)) {
					fileAttributeData.setAttrValue(value)
							.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
				}
			}

			attributeDataList = new ArrayList<>(Arrays.asList(fileAttributeData));
			responseDocumentData.setAttributes(attributeDataList);
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		}
	}
}
