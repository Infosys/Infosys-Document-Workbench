/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeMetaData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigAttributeData;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

public abstract class Rule000ExtractUsingConfigurationBase extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule000ExtractUsingConfigurationBase.class);

	public static final String TEMP_PATH_TXT = "docwb.engine.temp.path";

	@Autowired
	private DocWbApiClient docWbApiClient;

	private ObjectMapper mapper;

	private IAttributeService docwbAttributeService;

	private Map<Integer, String> attributeDBNamesMapByCde;

	ExtractorData extractorConfiData;

	@PostConstruct
	private void init() {
		mapper = new ObjectMapper();
		docWbApiClient.getAttachmentService();
		docwbAttributeService = docWbApiClient.getAttributeService();
		attributeDBNamesMapByCde = docwbAttributeService.getAttributeNames();
	}

	/**
	 * Copy DB fetched attachment data to Service class attachment object
	 */
	protected List<AttachmentData> createDBAttachmentReqList(List<AttachmentData> attachmentDataList) {
		List<AttachmentData> reqDataList = new ArrayList<>();
		AttachmentData attachData = null;
		if (ListUtility.hasValue(attachmentDataList)) {
			for (AttachmentData attachmentData : attachmentDataList) {
				attachData = new AttachmentData();
				BeanUtils.copyProperties(attachmentData, attachData);
				reqDataList.add(attachData);
			}
		}
		return reqDataList;
	}

	/**
	 * Filter method used only on Re-extraction flow. This will filter out the
	 * service result and returns only selected re-extract attribute data
	 */
	protected List<AttributeData> filterReExtractParamAttachAttributes(List<AttributeData> extractedAttributes,
			DocumentData reExtractParamData, long attachmentId) {
		if (ListUtility.hasValue(extractedAttributes) && reExtractParamData != null
				&& ListUtility.hasValue(reExtractParamData.getAttachmentDataList())) {
			List<AttachmentData> filteredAttachmentList = reExtractParamData.getAttachmentDataList().stream()
					.filter(attachment -> attachment.getAttachmentId() == attachmentId).collect(Collectors.toList());

			if (ListUtility.hasValue(filteredAttachmentList)) {
				for (AttachmentData attachmentData : filteredAttachmentList) {
					List<AttributeData> reEParamAttachAttributes = attachmentData.getAttributes();

					if (ListUtility.hasValue(reEParamAttachAttributes)) {
						List<AttributeData> reEParamAttributes = reEParamAttachAttributes.stream()
								.filter(attributeData -> attributeData
										.getAttrNameCde() == EnumSystemAttributeName.DOCUMENT_TYPE.getCde())
								.collect(Collectors.toList());

						if (!ListUtility.hasValue(reEParamAttributes)) {
							List<AttributeData> filteredAttributes = extractedAttributes.stream()
									.filter(extractedAttribute -> reEParamAttachAttributes.stream().anyMatch(
											reEAttribute -> matchAttributeData(extractedAttribute, reEAttribute)))
									.collect(Collectors.toList());
							extractedAttributes = filteredAttributes;
						}
					}
				}
			}

		}
		return extractedAttributes;
	}

	private boolean matchAttributeData(AttributeData extractedAttribute, AttributeData reExtractParamData) {
		boolean found = false;
		if (reExtractParamData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
				|| reExtractParamData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
			if (reExtractParamData.getAttrNameTxt().equals(extractedAttribute.getAttrNameTxt())) {
				found = true;
			}
		} else if (reExtractParamData.getAttrNameCde() == extractedAttribute.getAttrNameCde()) {
			found = true;
		}
		return found;

	}

	/**
	 * Main method for validating the configured attributes mapping data
	 */
	protected void validateConfigAttributeData(List<ExtractorConfigAttributeData> extractorConfigAttributeDataList)
			throws ConfigurationException {
		List<ExtractorConfigAttributeData> normalAttributes = AttributeDataHelper.updateAttrNameTxtToAttributes(
				getNormalAttributeList(extractorConfigAttributeDataList), attributeDBNamesMapByCde);
		verifyDuplicateOfNormalAttrMappedToTableAttr(extractorConfigAttributeDataList, normalAttributes);
		verifyDuplicateOfNormalAttrMappedToMultiAttr(extractorConfigAttributeDataList, normalAttributes);
	}

	/**
	 * Validate is configured normal attributes are configures again as multiple
	 * attribute If yes on UI will show duplicates attr names, hence its not allowed
	 * should fail adding attributes to Case.
	 */
	private void verifyDuplicateOfNormalAttrMappedToMultiAttr(
			List<ExtractorConfigAttributeData> extractorConfigAttributeDataList,
			List<ExtractorConfigAttributeData> normalAttributes) throws ConfigurationException {
		List<ExtractorConfigAttributeData> multiAttributeList = getMultiAttributeList(extractorConfigAttributeDataList);
		boolean isConfigErrorFound = false;
		for (ExtractorConfigAttributeData multiAttr : multiAttributeList) {
			// if (!ListUtility.hasValue(multiAttr.getAttributes())) {
			// throw new ConfigurationException(
			// "Configure /response/mapping/attributes/attributes properties for
			// attrNameCde: "
			// + EngineExtractorConstants.ATTR_NAME_CDE_MULTI_ATTRIBUTE);
			// }
			for (ExtractorConfigAttributeData normalAttr : normalAttributes) {
				List<ExtractorConfigAttributeData> filteredAttr = multiAttr.getAttributes().stream()
						.filter(attr -> attr.getAttrNameTxt().equalsIgnoreCase(normalAttr.getAttrNameTxt()))
						.collect(Collectors.toList());
				if (ListUtility.hasValue(filteredAttr)) {
					isConfigErrorFound = true;
					logger.error("AttrNameTxt: " + normalAttr.getAttrNameTxt()
							+ " has mapped for multiple AttrNameCdes : "
							+ EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde() + ", " + normalAttr.getAttrNameCde());
				}
			}
		}

		if (isConfigErrorFound) {
			throw new ConfigurationException("Error found in attributes configuration mapping");
		}
	}

	/**
	 * Validate is configured normal attributes are configured again as multiple
	 * attribute table's name and Validate is configured multiple attribute table's
	 * name repeated If yes on UI will show duplicates attr names, hence its not
	 * allowed should fail adding attributes to Case.
	 */
	private void verifyDuplicateOfNormalAttrMappedToTableAttr(
			List<ExtractorConfigAttributeData> extractorConfigAttributeDataList,
			List<ExtractorConfigAttributeData> normalAttributes) throws ConfigurationException {
		List<ExtractorConfigAttributeData> multiAttributeTableList = getMultiAttributeTableList(
				extractorConfigAttributeDataList);

		// List<ExtractorConfigAttributeData> emptyAttributes =
		// multiAttributeTableList.stream()
		// .filter(attr ->
		// !ListUtility.hasValue(attr.getAttributes())).collect(Collectors.toList());
		// if (ListUtility.hasValue(emptyAttributes)) {
		// throw new ConfigurationException(
		// "Configure /response/mapping/attributes/attributes properties for
		// attrNameCde: "
		// + EngineExtractorConstants.ATTR_NAME_CDE_MULTI_ATTRIBUTE_TABLE);
		// }

		Map<String, Long> bymultiAttrTableAttrTableName = multiAttributeTableList.stream()
				.collect(Collectors.groupingBy(ExtractorConfigAttributeData::getTableName, Collectors.counting()));

		boolean isConfigErrorFound = false;
		for (Entry<String, Long> multiAttrMap : bymultiAttrTableAttrTableName.entrySet()) {
			long count = multiAttrMap.getValue();
			String name = multiAttrMap.getKey();
			if (count > 1) {
				isConfigErrorFound = true;
				logger.error("AttrNameCde : " + EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()
						+ " and AttrNameTxt: " + name + "has configured for " + count + " times");
			}

			List<ExtractorConfigAttributeData> byNormalAttributeAttrNameTxt = normalAttributes.stream()
					.filter(attribute -> attribute.getAttrNameTxt().equalsIgnoreCase(name))
					.collect(Collectors.toList());
			if (ListUtility.hasValue(byNormalAttributeAttrNameTxt)) {
				isConfigErrorFound = true;
				logger.error("AttrNameTxt: " + name + " has mapped for multiple AttrNameCdes : "
						+ EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde() + ", "
						+ byNormalAttributeAttrNameTxt.get(0).getAttrNameCde());
			}
		}

		if (isConfigErrorFound) {
			throw new ConfigurationException("Error found in attributes configuration mapping");
		}
	}

	/**
	 * Filter normal attributes from response as per config file mapping
	 */
	private List<ExtractorConfigAttributeData> getNormalAttributeList(
			List<ExtractorConfigAttributeData> extractorConfigAttributeDataList) {
		return extractorConfigAttributeDataList.stream().filter(
				attribute -> (attribute.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()
						&& attribute.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()))
				.collect(Collectors.toList());
	}

	/**
	 * Filter multi attributes(44) from response as per config file mapping
	 */
	private List<ExtractorConfigAttributeData> getMultiAttributeList(
			List<ExtractorConfigAttributeData> extractorConfigAttributeDataList) {
		return extractorConfigAttributeDataList.stream()
				.filter(attribute -> (attribute.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()))
				.collect(Collectors.toList());
	}

	/**
	 * Filter multi attributes table(45) from response as per config file mapping
	 */
	protected List<ExtractorConfigAttributeData> getMultiAttributeTableList(
			List<ExtractorConfigAttributeData> extractorConfigAttributeDataList) {
		return extractorConfigAttributeDataList.stream().filter(
				attribute -> (attribute.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()))
				.collect(Collectors.toList());
	}

	/**
	 * Meta Data about response data
	 */
	protected AttributeMetaData createAttributeMetaData(ExtractorConfigAttributeData extractorConfigAttributeData) {
		AttributeMetaData consolidateAttibuteData = new AttributeMetaData();
		consolidateAttibuteData.setMultiAttributes(AttributeDataHelper.isMultiAttributes(extractorConfigAttributeData));
		consolidateAttibuteData
				.setMultiAttributeTable(AttributeDataHelper.isMultiAttributeTable(extractorConfigAttributeData));
		consolidateAttibuteData.setMultiAttribute(AttributeDataHelper.isMultiAttribute(extractorConfigAttributeData));
		consolidateAttibuteData.setTableName(extractorConfigAttributeData.getTableName());
		consolidateAttibuteData.setGroupName(extractorConfigAttributeData.getGroupName());
		consolidateAttibuteData.setResAttrName(extractorConfigAttributeData.getResAttrName());
		consolidateAttibuteData.setAttrNameCde(extractorConfigAttributeData.getAttrNameCde());
		return consolidateAttibuteData;
	}

	/**
	 * Create Unique attribute from list of attributes If any duplicate attribute
	 * data, then values will be concatenated to attrname
	 */
	private List<AttributeData> creatUniqueAttrDataList(List<AttributeData> attrDataList) {
		List<AttributeData> newAttributeDataList = null;
		for (AttributeData attributeData : attrDataList) {
			if (newAttributeDataList == null) {
				newAttributeDataList = new ArrayList<>();
				newAttributeDataList.add(attributeData);
			} else {
				concatAttributeDataListByNameTxt(newAttributeDataList, attributeData);
			}
		}
		return newAttributeDataList;
	}

	/**
	 * Create API response as normal attribute if it configure in config file
	 */
	protected void createNormalAttributeDataList(List<AttributeData> nonMultiAttributeDataList,
			AttributeData attributeData) {
		List<AttributeData> attrDataList = AttributeDataHelper.filterByAttrNameCde(nonMultiAttributeDataList,
				attributeData.getAttrNameCde());
		if (ListUtility.hasValue(attrDataList)) {
			AttributeData attributeDataTemp = attrDataList.get(0);
			attributeDataTemp.setAttrValue(AttributeDataHelper.concatAttrValueByDelimiter(
					Arrays.asList(attributeDataTemp.getAttrValue(), attributeData.getAttrValue())));
			attributeDataTemp.setConfidencePct(AttributeDataHelper.getConfidenceAvg(
					Arrays.asList(attributeDataTemp.getConfidencePct(), attributeData.getConfidencePct())));
		} else {
			nonMultiAttributeDataList.add(attributeData);
		}
	}

	/**
	 * Create API response as multi attribute(44)/ multi attribute table (45) if it
	 * configure in config file
	 */
	protected List<AttributeData> mapMultiAttributesFromConsolidatedAttrList(
			Map<String, List<AttributeData>> tableAttributeDataMap, Map<String, Integer> tableNameCdeMap) {
		List<AttributeData> multiAttributeDataList = new ArrayList<AttributeData>();
		tableAttributeDataMap.forEach((tableOrGroupName, tablecols) -> {
			if (tableNameCdeMap.get(tableOrGroupName).equals(EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())) {
				multiAttributeDataList.add(AttributeDataHelper.createMultipleAttibuteElement(
						EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde(), tableOrGroupName,
						createTableRowAttrDataList(tablecols)));
			} else if (tableNameCdeMap.get(tableOrGroupName).equals(EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())) {
				multiAttributeDataList.add(AttributeDataHelper.createMultipleAttibuteElement(
						EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde(), tableOrGroupName,
						creatUniqueAttrDataList(tablecols)));
			}
		});

		return multiAttributeDataList;
	}

	/**
	 * Filter and Create API response as mutli attribute table(45)
	 */
	private List<AttributeData> createTableRowAttrDataList(List<AttributeData> attrDataList) {
		List<AttributeData> rowAttributeDataList = new ArrayList<AttributeData>();
		Set<Long> tableRows = new HashSet<Long>();
		attrDataList.forEach(attributeData -> {
			tableRows.add(attributeData.getId());
		});
		tableRows.forEach(rowId -> {
			List<AttributeData> rowAttributes = attrDataList.stream().filter(attribute -> attribute.getId() == rowId)
					.collect(Collectors.toList());
			rowAttributeDataList.add(AttributeDataHelper.createMultipleAttibuteElement(
					EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde(), EngineExtractorConstants.GROUP_NAME_ROW,
					rowAttributes));
		});
		return rowAttributeDataList;
	}

	/**
	 * concat same attrname attributes value and confidence pct
	 */
	private void concatAttributeDataListByNameTxt(List<AttributeData> attributeDataList, AttributeData attributeData) {
		List<AttributeData> attrDataListTemp = AttributeDataHelper.filterByAttrNameTxt(attributeDataList,
				attributeData.getAttrNameTxt());
		if (ListUtility.hasValue(attrDataListTemp)) {
			AttributeData attributeDataTemp = attrDataListTemp.get(0);
			attributeDataTemp.setAttrValue(AttributeDataHelper.concatAttrValueByDelimiter(
					Arrays.asList(attributeDataTemp.getAttrValue(), attributeData.getAttrValue())));
			attributeDataTemp.setConfidencePct(AttributeDataHelper.getConfidenceAvg(
					Arrays.asList(attributeDataTemp.getConfidencePct(), attributeData.getConfidencePct())));
		} else {
			attributeDataList.add(attributeData);
		}
	}

	/**
	 * If multi attibutes configured 44/45 to show AttrName on UI fetch APi response
	 * colname mapped to attrname from config file
	 */
	protected String getAttrNameTxtByColName(ExtractorConfigAttributeData extractorConfigAttributeData,
			String attrColName) {
		String attrNameTxt = "";
		try {
			if (AttributeDataHelper.isMultiAttributes(extractorConfigAttributeData)) {
				ExtractorConfigAttributeData attribute = extractorConfigAttributeData.getAttributes().stream()
						.filter(attr -> attr.getColName().equals(attrColName)).findFirst().get();
				attrNameTxt = attribute.getAttrNameTxt();
			}
		} catch (NoSuchElementException e) {
		}
		return attrNameTxt;
	}

	/**
	 * If multi attributes configured 44/45 to show AttrName on UI fetch APi
	 * response colName\attrName mapped to attrname from config file
	 */
	protected String getAttrNameTxtByConfName(ExtractorConfigAttributeData extractorConfigAttributeData,
			String attrColName) {
		String attrNameTxt = "";
		try {
			if (AttributeDataHelper.isMultiAttributeTable(extractorConfigAttributeData)) {
				ExtractorConfigAttributeData attribute = extractorConfigAttributeData.getAttributes().stream()
						.filter(attr -> attr.getResColName().equals(attrColName)).findFirst().get();
				attrNameTxt = attribute.getAttrNameTxt();
			} else if (AttributeDataHelper.isMultiAttribute(extractorConfigAttributeData)) {
				ExtractorConfigAttributeData attribute = extractorConfigAttributeData.getAttributes().stream()
						.filter(attr -> attr.getResAttrName().equals(attrColName)).findFirst().get();
				attrNameTxt = attribute.getAttrNameTxt();
			} else {
				attrNameTxt = extractorConfigAttributeData.getAttrNameTxt();
			}
		} catch (NoSuchElementException e) {
		}
		return attrNameTxt;
	}

	/**
	 * Read config file and map to object
	 **/
	protected ExtractorData readAttributeExtractorConfigFile(String fileName) {
		ExtractorData extractorData = null;
		if (FileUtility.doesResourceExist(fileName)) {
			String fileContent = FileUtility.readResourceFile(fileName);
			try {
				extractorData = mapper.readValue(fileContent, ExtractorData.class);
			} catch (IOException e) {
				logger.error("Error while reading the " + fileName + " file");
			}
		}
		return extractorData;
	}

	/**
	 * @param attachmentReqDataList
	 * @return
	 */
	protected long getAttachIdOfConvTextAttachments(List<AttachmentData> attachmentReqDataList) {
		long attachmentId = 0;
		if (ListUtility.hasValue(attachmentReqDataList)) {
			attachmentId = attachmentReqDataList.stream()
					.filter(attachment -> attachment.getExtractTypeCde() == EnumExtractType.CUSTOM_LOGIC.getValue()
							&& attachment.getLogicalName().endsWith(EngineExtractorConstants.FILE_EXTENSION_TXT))
					.findFirst().orElse(new AttachmentData()).getAttachmentId();
		}
		return attachmentId;
	}

	/**
	 * Filter method used only on Re-extraction flow. This will filter out the
	 * service result and returns only selected re-extract attribute data
	 */
	protected List<AttributeData> filterReExtractParamEmailAttributes(List<AttributeData> extractedAttributes,
			DocumentData reExtractParamData) {
		if (ListUtility.hasValue(extractedAttributes) && reExtractParamData != null) {
			List<AttributeData> reExtractParamEmailAttributes = reExtractParamData.getAttributes();
			if (ListUtility.hasValue(reExtractParamEmailAttributes)) {
				List<AttributeData> reExtractParamAttributes = reExtractParamEmailAttributes.stream().filter(
						attributeData -> attributeData.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde())
						.collect(Collectors.toList());
				if (!ListUtility.hasValue(reExtractParamAttributes)) {
					List<AttributeData> filteredAttributes = extractedAttributes.stream()
							.filter(extractedAttribute -> reExtractParamEmailAttributes.stream().anyMatch(
									reExtractAttribute -> matchAttributeData(extractedAttribute, reExtractAttribute)))
							.collect(Collectors.toList());
					extractedAttributes = filteredAttributes;
				}
			}
		}
		return extractedAttributes;
	}
}
