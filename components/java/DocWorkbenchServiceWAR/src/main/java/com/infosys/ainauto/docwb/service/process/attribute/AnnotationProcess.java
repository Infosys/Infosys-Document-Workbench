/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.attribute;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumExtractType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.dao.attribute.AttributeDataAccess;
import com.infosys.ainauto.docwb.service.model.api.annotation.ExportIOBReqData;
import com.infosys.ainauto.docwb.service.model.api.annotation.InsertAnnotationReqData;
import com.infosys.ainauto.docwb.service.model.api.annotation.InsertAnnotationReqData.AnnotationReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.annotation.ExportIOBDbData;
import com.infosys.ainauto.docwb.service.model.process.AnnotationData;
import com.infosys.ainauto.docwb.service.model.process.Counter;
import com.infosys.ainauto.docwb.service.model.process.annotation.ExportIOBData;
import com.infosys.ainauto.docwb.service.model.process.annotation.ExportIOBData.RangeData;

import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class AnnotationProcess implements IAnnotationProcess {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationProcess.class);

	@Autowired
	private AttributeProcess attributeProcess;

	@Autowired
	private AttributeDataAccess attributeDataAccess;

	@Value("${attachmentFilePath}")
	private String attachmentFilePath;

	@Value("${docwb.service.temp.path}")
	private String attachmentTempFilePath;

	private final String FILE_NAME_ANNOTATIONS_LIST = "ListOfAnnotations.txt";
	private final String FILE_FORMAT_WINDOWS = "\r\n";
	private final String FILE_FORMAT_LINUX = "\n";

	public List<EntityDbData> addAnnotation(List<InsertAnnotationReqData> insertAnnotationReqDatas)
			throws WorkbenchException {
		List<Long> prevDocAttrRelIdList = new ArrayList<Long>();
		List<Long> latestDocAttrRelIdList = new ArrayList<Long>();
		List<Long> prevAttachAttrRelIdList = new ArrayList<Long>();
		List<Long> latestAttachAttrRelIdList = new ArrayList<Long>();
		Counter insertCounter = new Counter();
		for (InsertAnnotationReqData insertAnnotationReqData : insertAnnotationReqDatas) {
			List<AttributeDbData> insertAttrDataList = new ArrayList<AttributeDbData>();
			long docId = insertAnnotationReqData.getDocId();
			if (ListUtility.hasValue(insertAnnotationReqData.getAttachments())) {
				for (InsertAnnotationReqData.AttachmentAnnotationData attachmentAnnotationData : insertAnnotationReqData
						.getAttachments()) {
					insertAttrDataList.add(buildAttributeForAnnotations(attachmentAnnotationData.getAnnotations(),
							docId, attachmentAnnotationData.getAttachmentId(), insertCounter));
				}
			}
			if (ListUtility.hasValue(insertAnnotationReqData.getAnnotations())) {
				insertAttrDataList.add(buildAttributeForAnnotations(insertAnnotationReqData.getAnnotations(), docId, 0,
						insertCounter));
			}
			if (ListUtility.hasValue(insertAttrDataList)) {
				insertAttrDataList.removeIf(a -> a == null);
				for (AttributeDbData resultAttrDbData : insertAttrDataList) {
					long prevAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
					long latestAttrRelId = WorkbenchConstants.DOC_ACTION_REL_ID_UNSET;
					if (resultAttrDbData.getAttachmentId() > 0)
						prevAttachAttrRelIdList.add(prevAttrRelId);
					else
						prevDocAttrRelIdList.add(prevAttrRelId);
					latestAttrRelId = attributeDataAccess.addNewAttribute(resultAttrDbData);
					// Audit- storing the previous and latest rel id based on entities.
					if (latestAttrRelId > 0) {
						if (resultAttrDbData.getAttachmentId() > 0)
							latestAttachAttrRelIdList.add(latestAttrRelId);
						else
							latestDocAttrRelIdList.add(latestAttrRelId);
					}
				}
			}
		}
		return attributeProcess.createEntityDataList(prevDocAttrRelIdList, latestDocAttrRelIdList,
				prevAttachAttrRelIdList, latestAttachAttrRelIdList, insertCounter.getCount());
	}

	@Override
	public AttachmentDbData getAnnotationIOB(ExportIOBReqData exportIOBReqData) throws WorkbenchException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setLenient(false);
		try {
			Date startDtm = sdf
					.parse(exportIOBReqData.getCreateDtm().getStart() + WorkbenchConstants.DEFAULT_TIMESTAMP);
			Date endDtm = sdf.parse(exportIOBReqData.getCreateDtm().getEnd() + WorkbenchConstants.DEFAULT_TIMESTAMP);
			String attrValue = null;
			int attrNameCde = 0;
			if (ListUtility.hasValue(exportIOBReqData.getAttributes())) {
				attrValue = exportIOBReqData.getAttributes().get(0).getAttrValue();
				attrNameCde = exportIOBReqData.getAttributes().get(0).getAttrNameCde();
			} else if (ListUtility.hasValue(exportIOBReqData.getAttachments())
					&& ListUtility.hasValue(exportIOBReqData.getAttachments().get(0).getAttributes())) {
				attrValue = exportIOBReqData.getAttachments().get(0).getAttributes().get(0).getAttrValue();
				attrNameCde = exportIOBReqData.getAttachments().get(0).getAttributes().get(0).getAttrNameCde();
			}
			AttributeDbData attributeDbData = null;
			if (StringUtility.hasTrimmedValue(attrValue)) {
				attributeDbData = new AttributeDbData();
				attributeDbData.setAttrNameCde(attrNameCde);
				attributeDbData.setAttrValue(attrValue);
			}
			List<ExportIOBDbData> exportIOBDbDataList = attributeDataAccess.getAnnotationIob(attributeDbData, startDtm,
					endDtm);
			List<String> zipFiles = new ArrayList<>();
			List<String> annotationList = new ArrayList<>();
			for (ExportIOBDbData exportIOBDbData : exportIOBDbDataList) {
				List<ExportIOBData> annotationDataList = sortAnnDataBasedOnRanges(
						convertAnnotationStringToList(exportIOBDbData.getAttrValue()));
				Map<String, String> guidLabelMap = new HashMap<>();
				String fileContent, data;
				fileContent = data = new String(Files.readAllBytes(Paths.get(FileUtility.cleanPath(
						FileUtility.getConcatenatedPath(attachmentFilePath, exportIOBDbData.getPhysicalName())))));
				for (ExportIOBData annotationData : annotationDataList) {
					if (ListUtility.hasValue(annotationData.getRanges())) {
						String guid = StringUtility.getRandomAlphaString(20);
						String label = annotationData.getText();
						guidLabelMap.put(guid, label);
						Long startIndex = annotationData.getRanges().get(0).getStartOffset(),
								endIndex = annotationData.getRanges().get(0).getEndOffset();
						data = data.substring(0, startIndex.intValue()) + guid
								+ data.substring(startIndex.intValue(), endIndex.intValue()) + guid
								+ data.substring(endIndex.intValue());
						// For Unique Annotations.
						if (!ListUtility.hasValue(
								annotationList.stream().filter(annotation -> annotation.equalsIgnoreCase(label))
										.collect(Collectors.toList()))) {
							annotationList.add(label);
						}
					}
				}
				String classifiedValue = tokenizeFileContentIntoIOBFormat(data, guidLabelMap);
				String fileName = exportIOBDbData.getDocId() + "-"
						+ FileUtility.getFileNameNoExtension(exportIOBDbData.getLogicalName()) + "-"
						+ FileUtility.getFileNameNoExtension(exportIOBDbData.getPhysicalName());
				zipFiles.add(createFile(classifiedValue, fileName, WorkbenchConstants.FILE_EXTENSION_CONLL));
				// converting file content to windows irrespective of original format.
				fileContent = fileContent.replaceAll(FILE_FORMAT_LINUX, FILE_FORMAT_WINDOWS).replaceAll("\r\r", "\r");
				zipFiles.add(createFile(fileContent, fileName, WorkbenchConstants.FILE_EXTENSION_TXT));
			}
			String annotationContent = "";
			for (String annotation : annotationList) {
				annotationContent += annotation + FILE_FORMAT_WINDOWS;
			}
			String annotationFilePath = FileUtility.getConcatenatedPath(attachmentTempFilePath,
					FILE_NAME_ANNOTATIONS_LIST);
			if (!FileUtility.saveFile(annotationFilePath, annotationContent.trim())) {
				logger.error("Unique annotation txt file creation failed");
				throw new WorkbenchException("Unique annotation txt file creation failed");
			}
			zipFiles.add(annotationFilePath);
			String zipFileName = UUID.randomUUID().toString() + "." + WorkbenchConstants.FILE_EXTENSION_ZIP;
			String zipFilePath = FileUtility.getConcatenatedPath(attachmentTempFilePath, zipFileName);
			FileUtility.createZipFile(zipFilePath, zipFiles);
			for (String fileName : zipFiles) {
				FileUtility.deleteFile(fileName);
			}
			AttachmentDbData attachmentDbData = new AttachmentDbData();
			attachmentDbData.setPhysicalPath(zipFilePath);
			attachmentDbData.setLogicalName(zipFileName);
			return attachmentDbData;
		} catch (Exception e) {
			logger.error("Failed in getAnnotationIOB processing", e);
			throw new WorkbenchException("Failed in getAnnotationIOB processing", e);
		}
	}

	private AttributeDbData buildAttributeForAnnotations(List<AnnotationReqData> annotations, long docId,
			long attachmentId, Counter insertCounter) {
		AttributeDbData attributeDbData = new AttributeDbData();
		String attrValue = "";
		attributeDbData.setDocId(docId);
		attributeDbData.setAttachmentId(attachmentId);
		attributeDbData.setAttrNameCde(EnumSystemAttributeName.CONTENT_ANNOTATION.getCde());
		attributeDbData.setExtractTypeCde(EnumExtractType.CUSTOM_LOGIC.getValue());
		attributeDbData.setConfidencePct(WorkbenchConstants.CONFIDENCE_PCT_UNSET);
		attrValue = getAnnotationStringFromList(annotations);
		if (StringUtility.hasValue(attrValue) && StringUtility.isJsonValid(attrValue)) {
			attributeDbData.setAttrValue(attrValue);
			insertCounter.setCount(insertCounter.getCount() + annotations.size());
			return attributeDbData;
		}
		return null;

	}

	private String getAnnotationStringFromList(List<AnnotationReqData> annotations) {
		List<AnnotationData> annotationDatas = new ArrayList<>();
		int i = 0;
		String annotationList = "";
		for (AnnotationReqData data : annotations) {
			i++;
			AnnotationData annotationData = new AnnotationData();
			annotationData.setQuote(data.getValue());
			annotationData.setText(data.getLabel());
			annotationData.setId(i);
			annotationData.setCreatedByTypeCde(WorkbenchConstants.CREATED_BY_TYPE_CDE_SYSTEM);
			annotationData.setOccurrenceNum(data.getOccurrenceNum());
			annotationData.setPage(data.getPage());
			annotationData.setPageBbox(data.getPageBbox());
			annotationData.setSourceBbox(data.getSourceBbox());
			annotationDatas.add(annotationData);
		}
		if (ListUtility.hasValue(annotationDatas)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				annotationList = mapper.writeValueAsString(annotationDatas);
			} catch (JsonProcessingException e) {
				logger.error("JsonProcessingException occured while converting annotationDataList to json object", e);
			}
		}
		return annotationList;
	}

	// Convert Annotation json string to java objects
	private List<ExportIOBData> convertAnnotationStringToList(String attrValue) {
		JsonReader reader = Json.createReader(new StringReader(attrValue));
		JsonArray jsonArray = reader.readArray();
		List<ExportIOBData> annotationDataList = new ArrayList<>();
		for (int i = 0, size = jsonArray.size(); i < size; i++) {
			JsonObject jsonObject = jsonArray.getJsonObject(i);
			ExportIOBData annotationData = new ExportIOBData();
			if (jsonObject.containsKey("createdByTypeCde")) {
				annotationData.setCreatedByTypeCde(jsonObject.getInt("createdByTypeCde"));
			}
			annotationData.setId(jsonObject.getInt("id"));
			annotationData.setQuote(jsonObject.getString("quote"));
			annotationData.setText(jsonObject.getString("text"));
			List<RangeData> ranges = new ArrayList<>();
			JsonArray rangeArray = jsonObject.getJsonArray("ranges");
			for (int j = 0; j < rangeArray.size(); j++) {
				RangeData range = annotationData.new RangeData();
				JsonObject jsonRangeObject = rangeArray.getJsonObject(0);
				range.setStart(jsonRangeObject.getString("start"));
				range.setEnd(jsonRangeObject.getString("end"));
				range.setStartOffset(jsonRangeObject.getInt("startOffset"));
				range.setEndOffset(jsonRangeObject.getInt("endOffset"));
				ranges.add(range);
			}
			annotationData.setRanges(ranges);
			annotationDataList.add(annotationData);
		}
		return annotationDataList;
	}

	// Labeling file content tokens in IOB format.
	private String tokenizeFileContentIntoIOBFormat(String data, Map<String, String> annotatedMap) {
		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		// Tokenizing the given sentence
		String tokens[] = tokenizer.tokenize(data);
		String TEMP_TAG = " R";
		for (String key : annotatedMap.keySet()) {
			boolean isKeyLabelled = false;
			boolean isKeyStartFound = false;
			for (int i = 0; i < tokens.length; i++) {
				if (!isKeyStartFound) {
					if (tokens[i].contains(key)) {
						// To iterate next token when key alone present in tokens during number or
						// special characters annotated.
						if (tokens[i].length() == key.length()) {
							tokens[i] += TEMP_TAG;
							i++;
						} else {
							// Single word annotation with only alphabets.
							if (tokens[i].indexOf(key) != tokens[i].lastIndexOf(key)) {
								isKeyLabelled = true;
							}
							tokens[i] = tokens[i].replace(key, "");
						}
						isKeyStartFound = true;
						tokens[i] += WorkbenchConstants.EXPORT_ANNOTATION_TAG_B + annotatedMap.get(key);
					}
				} else {
					// To ignore when key alone present in tokens during number or special
					// characters annotated.
					if (tokens[i].contains(key) && tokens[i].length() == key.length()) {
						tokens[i] += TEMP_TAG;
						isKeyLabelled = true;
					} else {
						if (tokens[i].contains(key)) {
							tokens[i] = tokens[i].replace(key, "");
							isKeyLabelled = true;
						}
						tokens[i] += WorkbenchConstants.EXPORT_ANNOTATION_TAG_I + annotatedMap.get(key);
					}
				}
				if (isKeyLabelled) {
					break;
				}
			}
		}
		// To remove guid tokens present in single line for numeric and special
		// characters in token array.
		List<String> filteredAnnotatedToken = Arrays.asList(tokens).stream().filter(a -> !a.contains(TEMP_TAG))
				.collect(Collectors.toList());
		String convertedText = "";
		for (String token : filteredAnnotatedToken) {
			if (!(token.contains(WorkbenchConstants.EXPORT_ANNOTATION_TAG_B)
					|| token.contains(WorkbenchConstants.EXPORT_ANNOTATION_TAG_I))) {
				token += WorkbenchConstants.EXPORT_ANNOTATION_TAG_O;
			}
			convertedText += token + FILE_FORMAT_WINDOWS;
		}
		convertedText = convertedText.trim();
		return convertedText;
	}

	// Annotation data list sorted descending order based on end index of ranges.
	private List<ExportIOBData> sortAnnDataBasedOnRanges(List<ExportIOBData> annotationDataList) {
		Integer endIndexes[] = new Integer[annotationDataList.size()];
		int i = 0;
		for (ExportIOBData annotationData : annotationDataList) {
			endIndexes[i] = ((Long) annotationData.getRanges().get(0).getEndOffset()).intValue();
			i++;
		}
		Arrays.sort(endIndexes, Collections.reverseOrder());
		List<ExportIOBData> sortedList = new ArrayList<>();
		for (Integer endIndex : endIndexes) {
			for (ExportIOBData annotationData : annotationDataList) {
				if (endIndex.equals(((Long) annotationData.getRanges().get(0).getEndOffset()).intValue())) {
					sortedList.add(annotationData);
					break;
				}
			}
		}
		return sortedList;
	}

	private String createFile(String content, String fileName, String extension) throws WorkbenchException {
		fileName = fileName + "." + extension;
		String filePath = FileUtility.getConcatenatedPath(attachmentTempFilePath, fileName);
		if (!FileUtility.saveFile(filePath, content)) {
			logger.error(extension + " file creation failed");
			throw new WorkbenchException(extension + " file creation failed");
		}
		return filePath;
	}
}
