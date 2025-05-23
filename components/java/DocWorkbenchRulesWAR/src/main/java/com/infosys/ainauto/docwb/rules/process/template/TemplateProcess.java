/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.rules.process.template;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.rules.common.DocWbConstants;
import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.common.KieHelper;
import com.infosys.ainauto.docwb.rules.common.SerializationHelper;
import com.infosys.ainauto.docwb.rules.common.TenantResourceHelper;
import com.infosys.ainauto.docwb.rules.model.api.template.FlattenedTemplateResData;
import com.infosys.ainauto.docwb.rules.model.domain.ActionData;
import com.infosys.ainauto.docwb.rules.model.domain.AttachmentData;
import com.infosys.ainauto.docwb.rules.model.domain.AttributeData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.model.domain.InputOutputWrapperData;
import com.infosys.ainauto.docwb.rules.model.domain.ParamData;
import com.infosys.ainauto.docwb.rules.model.domain.TemplateData;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

@Component
public class TemplateProcess implements ITemplateProcess {

	private static final Logger logger = LoggerFactory.getLogger(TemplateProcess.class);
	private static Map<String, InternalKnowledgeBase> fileIkbMap = new HashMap<>();
	private static Map<String, Date> fileLmdMap = new HashMap<>();
	private static final String REPLACEMENT_FOR_PIPE = Character.toString((char) 25);

	@PostConstruct
	private void init() {
		long startTime = System.nanoTime();
		List<String> ruleFileLocationList = new ArrayList<>();
		// Load files to Map to avoiding lazy loading when first API call is made post
		// restart
		try {
			ruleFileLocationList = FileUtility.getResourceFilesInPath(DocWbConstants.RULES_FOLDER_TEMPLATE,
					DocWbConstants.FILE_EXTENSION_RULE, true);
			Date lastModifiedDtm = null;
			InternalKnowledgeBase kbase = null;
			for (String ruleFileLocation : ruleFileLocationList) {
				lastModifiedDtm = FileUtility.getLastModifiedDtm(ruleFileLocation);
				kbase = (InternalKnowledgeBase) SerializationHelper
						.serializeObject(KieHelper.loadKnowledgeBase(ruleFileLocation));
				fileLmdMap.put(ruleFileLocation, lastModifiedDtm);
				fileIkbMap.put(ruleFileLocation, kbase);
			}

		} catch (Exception e) {
			logger.error("Error occurred in init", e);
		} finally {
			logger.info("Loaded {} DRL files in {} sec(s)", ruleFileLocationList.size(),
					(System.nanoTime() - startTime) / 1000000000.0);
		}
	}

	private TemplateData getRecommendedTemplateName(String tenantId, String ruleFileName, DocumentData documentData)
			throws DocWbRulesException {
		TemplateData templateData = new TemplateData();
		String ruleFileLocation = TenantResourceHelper
				.validateAndReturnRuleFileLocation(DocWbConstants.RULES_FOLDER_TEMPLATE, tenantId, ruleFileName);

		try {
			InternalKnowledgeBase kbase = null;
			Date lastModifiedDtm = FileUtility.getLastModifiedDtm(ruleFileLocation);

			// Should be done only once per class instance and not per request
			synchronized (this) {
				// Check if LastModifiedDateMap has entry for ruleFileLocation
				// OR if file was modified after last check
				if (!fileLmdMap.containsKey(ruleFileLocation)
						|| fileLmdMap.get(ruleFileLocation).before(lastModifiedDtm)) {
					kbase = (InternalKnowledgeBase) SerializationHelper
							.serializeObject(KieHelper.loadKnowledgeBase(ruleFileLocation));
					fileLmdMap.put(ruleFileLocation, lastModifiedDtm);
					fileIkbMap.put(ruleFileLocation, kbase);
				} else {
					kbase = fileIkbMap.get(ruleFileLocation);
				}
			}

			InputOutputWrapperData<DocumentData, String> inputOutputWrapperData = new InputOutputWrapperData<>();
			inputOutputWrapperData.setInputData(documentData);

			StatelessKieSession statelessKieSession = KieHelper.createStatelessKnowledgeSession(kbase);

			statelessKieSession.execute(inputOutputWrapperData);

			logger.info("Template Name=" + inputOutputWrapperData.getOutputData());
			templateData.setTemplateName(inputOutputWrapperData.getOutputData());

		} catch (Exception e) {
			logger.error("Error occurred in getTemplateName", e);
			throw new DocWbRulesException("Error occurred in getTemplateName", e);
		}
		return templateData;
	}

	@Override
	public List<TemplateData> getTemplateList(String tenantId) throws DocWbRulesException {
		String[] templateTypes = { DocWbConstants.FILE_EXTENSION_TXT,
				DocWbConstants.FILE_EXTENSION_HTML };
		List<TemplateData> templateDataList = new ArrayList<TemplateData>();
		String tenantIdtemplatesFolderLocation = DocWbConstants.ASSETS_FOLDER_TEMPLATE + "/" + tenantId;
		String imagesFolderLocation = tenantIdtemplatesFolderLocation + "/" + DocWbConstants.ASSETS_FOLDER_IMAGES;

		boolean tenantIdFolderExists = FileUtility.doesResourceExist(tenantIdtemplatesFolderLocation);
		if (!tenantIdFolderExists) {
			throw new DocWbRulesException(EnumApiResponseCde.INVALID_TENANT_ID,
					EnumApiResponseCde.INVALID_TENANT_ID.getMessageValue() + ": " + tenantId);
		}

		boolean imageFolderExists = FileUtility.doesResourceExist(imagesFolderLocation);
		if (!imageFolderExists) {
			throw new DocWbRulesException(EnumApiResponseCde.INVALID_RESOURCE,
					EnumApiResponseCde.INVALID_RESOURCE.getMessageValue() + ": " + tenantId);
		}

		try {
			Pattern p = Pattern.compile(DocWbConstants.IMAGE_EXTRACTION_REGEX, Pattern.CASE_INSENSITIVE);
			for (String templateType : templateTypes) {
				List<String> templateFileList = FileUtility.getResourceFilesInPath(tenantIdtemplatesFolderLocation, templateType, false);
				TemplateData templateData = null;
				for (String fileName : templateFileList) {
					templateData = new TemplateData();
					templateData.setTemplateName(FileUtility.getFileNameNoExtension(fileName));
					String fileContent = FileUtility.readResourceFile(tenantIdtemplatesFolderLocation + "/" + fileName);
					if (templateType.equalsIgnoreCase(".html")) {
						logger.info("started conversion for html file" + System.nanoTime());
						Matcher m = p.matcher(fileContent);
						while (m.find()) {
							String imageFileName = m.group(1).substring(10, m.group(1).length());
							logger.debug("file name" + imageFileName);
							String[] strings = imageFileName.split("\\.");
							String imageFileExtension = StringUtility.getBase64Extension(strings[1]);
							String base64String = "";
							// Reading a Image file from file system and convert it into Base64 String
							byte[] imageData = FileUtility.readResourceFileAsByte(imagesFolderLocation + "/" + imageFileName);
							base64String = imageFileExtension + "," + Base64.getEncoder().encodeToString(imageData);
							fileContent = fileContent.replace(m.group(1), base64String);
						}
						logger.info("finished conversion for html file" + System.nanoTime());
					}
					templateData.setTemplateText(fileContent);
					templateData.setTemplateType(templateType);
					templateDataList.add(templateData);
				}
			}

			// Sort the list
			if (ListUtility.hasValue(templateDataList)) {
				templateDataList = templateDataList.stream().sorted(Comparator.comparing(TemplateData::getTemplateName))
						.collect(Collectors.toList());
			}

		} catch (Exception e) {
			logger.error("Error occurred in getTemplateName", e);
			throw new DocWbRulesException("Error occurred in getTemplateName", e);
		}
		return templateDataList;
	}

	@Override
	public List<FlattenedTemplateResData> getFlattenedTemplates(String tenantId, DocumentData documentData)
			throws DocWbRulesException {
		List<FlattenedTemplateResData> flattenedTemplateResDataList = new ArrayList<>();

		// Get All available templates for the given group name
		List<TemplateData> templateDataList = getTemplateList(tenantId);

		FlattenedTemplateResData flattenedTemplateResData = null;
		for (TemplateData templateData : templateDataList) {
			flattenedTemplateResData = new FlattenedTemplateResData();
			BeanUtils.copyProperties(templateData, flattenedTemplateResData);
			flattenedTemplateResDataList.add(flattenedTemplateResData);
		}

		String ruleFileName = DocWbConstants.RULE_FILE_NAME_RECOMMENDED_TEMPLATE;

		// Get the recommended template data
		TemplateData recommentedTemplateData = getRecommendedTemplateName(tenantId, ruleFileName, documentData);

		// Replace placeholder values by attributes
		createFlattenTemplate(flattenedTemplateResDataList, documentData, recommentedTemplateData);

		// Sort the list
		if (ListUtility.hasValue(flattenedTemplateResDataList)) {
			flattenedTemplateResDataList = flattenedTemplateResDataList.stream()
					.sorted(Comparator.comparing(FlattenedTemplateResData::getTemplateName))
					.collect(Collectors.toList());
		}

		return flattenedTemplateResDataList;
	}

	private String getOutboundEmailHeaderAndBody(DocumentData documentData) {
		boolean isContentHtmlExist = false;
		String emailHeaderAndBody = "From: <<FromId>>\r\nSent: <<ReceivedDate>>\r\nTo: <<To Address Id>>\r\nSubject: <<Subject>>\r\n\r\n";
		if (documentData.getAttributes() == null) {
			return emailHeaderAndBody;
		}
		for (AttributeData attributeData : documentData.getAttributes()) {
			if (attributeData.getAttrNameCde() == DocWbConstants.ATTR_NAME_CDE_CC_ADDRESS_ID
					&& attributeData.getAttrValue().length() > 0 && attributeData.getAttrValue() != null) {
				emailHeaderAndBody = "From: <<FromId>>\r\nSent: <<ReceivedDate>>\r\nTo: <<To Address Id>>\r\nCc: <<CC Address Id>>\r\nSubject: <<Subject>>\r\n\r\n";
			}
			if (attributeData.getAttrNameCde() == DocWbConstants.ATTR_NAME_CDE_CONTENT_HTML
					&& attributeData.getAttrValue().length() > 0 && attributeData.getAttrValue() != null) {
				isContentHtmlExist = true;
			}
		}
		if (isContentHtmlExist) {
			emailHeaderAndBody += "<<ContentHtml>>";
		} else {
			emailHeaderAndBody += "<<Content>>";
		}

		// First populate all extracted data attribute values
		for (AttributeData attributeData : documentData.getAttributes()) {
			if (attributeData.getAttrNameCde() == DocWbConstants.ATTR_NAME_CDE_MULTI_ATTRIBUTE) {
				// TODO
			} else {
				if ((emailHeaderAndBody.toLowerCase()).contains(attributeData.getAttrNameTxt().toLowerCase())) {
					emailHeaderAndBody = emailHeaderAndBody.replace(("<<" + attributeData.getAttrNameTxt() + ">>"),
							attributeData.getAttrValue().toString());
				}
			}

		}
		return emailHeaderAndBody;
	}

	private void createFlattenTemplate(List<FlattenedTemplateResData> flattenedTemplateResDataList,
			DocumentData documentData, TemplateData recommentedTemplateData) {

		// Frame sent email content from the attributes
		String emailHeaderAndBody = getOutboundEmailHeaderAndBody(documentData);

		boolean isRecommendedTemplate = false;
		String templateText = "";
		for (FlattenedTemplateResData flattenedTemplateResData : flattenedTemplateResDataList) {
			String templateRulesText = flattenedTemplateResData.getTemplateText();

			String templateName = flattenedTemplateResData.getTemplateName();

			if (recommentedTemplateData != null
					&& templateName.equalsIgnoreCase(recommentedTemplateData.getTemplateName())) {
				isRecommendedTemplate = true;
			} else {
				isRecommendedTemplate = false;
			}

			flattenedTemplateResData.setIsRecommendedTemplate(isRecommendedTemplate);

			templateText = templateRulesText.trim() + "\n\n" + "-------------------------------------------------"
					+ "\n" + emailHeaderAndBody;
			Map<String, String> attrNameTxtValueMap = new HashMap<>();

			attrNameTxtValueMap = getAttrMap(documentData.getAttributes(), attrNameTxtValueMap);
			if (ListUtility.hasValue(documentData.getAttachments())) {
				for (AttachmentData attachmentData : documentData.getAttachments()) {
					attrNameTxtValueMap = getAttrMap(attachmentData.getAttributes(), attrNameTxtValueMap);
				}
			}
			for (Map.Entry<String, String> entry : attrNameTxtValueMap.entrySet()) {
				if ((templateText.toLowerCase()).contains(entry.getKey().toLowerCase())) {
					templateText = templateText.replace(("<<" + entry.getKey() + ">>"), entry.getValue());
				}
			}

			if (documentData.getActionDataList() != null) {
				for (ActionData actionData : documentData.getActionDataList()) {
					String actionNameTxt = actionData.getActionNameTxt();
					// if (docId == actionParamAttrMappingDbData.getDocId()) {
					if (true) {
						String actionResult = actionData.getActionResult();
						if (StringUtility.hasValue(actionResult)) {
							// Pipe character separates all key value pairs
							if (actionResult.contains("=")) {
								String[] tokens = actionResult.split("\\|");
								for (String token : tokens) {
									// Key and value are separated by equals sign
									token = token.replace(REPLACEMENT_FOR_PIPE, "|");
									int separatorPos = token.indexOf("=");
									if (separatorPos > -1) {
										String key = token.substring(0, separatorPos);
										String value = token.substring((separatorPos + 1), token.length());
										templateText = templateText.replace(("<<" + actionNameTxt + ":"
												+ DocWbConstants.TEMPLATE_OUTPUT + ":" + key + ">>"), value);
									}
								}
							} else {
								templateText = templateText.replace(("<<" + actionData.getActionNameTxt() + ">>"),
										actionResult);
							}
						}
					}

					if (ListUtility.hasValue(actionData.getParamList())) {
						for (ParamData paramData : actionData.getParamList()) {
							String value = paramData.getParamValue();
							if (StringUtility.hasValue(value)) {
								String key = paramData.getParamNameTxt();
								templateText = templateText.replace(
										("<<" + actionNameTxt + ":" + DocWbConstants.TEMPLATE_INPUT + ":" + key + ">>"),
										value);
							}
						}
					}
				}
			}
			if (templateText.contains(DocWbConstants.FIND_REPLACE_STRING)) {
				List<String> functionFindReplaceList = StringUtility.getBetweenStrings(templateText,
						DocWbConstants.FIND_REPLACE_STRING, "))");
				for (String findReplace : functionFindReplaceList) {
					String[] regexFindReplaceContent = findReplace.split("\\|\\|");
					String maskedReplacement = regexFindReplaceContent[0].replaceAll(regexFindReplaceContent[1],
							regexFindReplaceContent[2]);
					templateText = templateText.replace(DocWbConstants.FIND_REPLACE_STRING + findReplace + "))",
							maskedReplacement);
				}
				flattenedTemplateResData.setTemplateText(templateText);
			} else {
				flattenedTemplateResData.setTemplateText(templateText);
			}

			// Converting normal text to HTML text.
			templateText = flattenedTemplateResData.getTemplateText();
			String templateHtml = null;
			if (flattenedTemplateResData.getTemplateType().equalsIgnoreCase(".html")) {
				templateText = StringUtility.findAndReplace(templateText, DocWbConstants.HTML_TAG_REGEX, "").trim();
			}
			templateText = StringUtility.replaceNewLineCharacters(templateText);
			templateHtml = "<html>" + templateText + "</html>";
			flattenedTemplateResData.setTemplateHtml(templateHtml);
		}
	}

	private Map<String, String> getAttrMap(List<AttributeData> attributes, Map<String, String> existingAttrMap) {
		if (ListUtility.hasValue(attributes)) {
			for (AttributeData attrData : attributes) {
				if (attrData.getAttrNameCde() == 44) {
					existingAttrMap = getAttrMap(attrData.getAttributes(), existingAttrMap);
				} else {
					String attrNameTxt = attrData.getAttrNameTxt();
					if (existingAttrMap.containsKey(attrNameTxt)) {
						existingAttrMap.put(attrNameTxt, existingAttrMap.get(attrNameTxt)
								+ DocWbConstants.ATTR_VALUE_DELIMITER + attrData.getAttrValue());
					} else {
						existingAttrMap.put(attrNameTxt, attrData.getAttrValue());
					}
				}
			}
		}
		return existingAttrMap;
	}
}
