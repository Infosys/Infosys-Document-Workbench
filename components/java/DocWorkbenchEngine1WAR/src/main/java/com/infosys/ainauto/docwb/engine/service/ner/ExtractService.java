/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.service.ner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;

@Component
public class ExtractService extends HttpClientBase implements IExtractService {

	@Autowired
	private Environment environment;

	private static final String MODELID = "modelId";
	private static final String REQUESTNUMBER = "requestNumber";
	private static final String PORTFOLIONAME = "portfolioName";
	private static final String TEXTCONTENT = "textContent";
	private static final String ENTITIES = "entities";
	private static final String NER_PREDICTOR_API = "service.ner.predict.api.url";
	private static final String CLASSTOPREDICT = "classToPredict";
	private static final String CATEGORY = "category";
	private static final String FILE_NAME = "fileName";
	private static final String CONFIDENCE_PCT = "confidencePct";
	private static final String VALUE = "value";
	private static final String ATTRIBUTES = "attributes";
	private static final String PROP_NAME_SOCKET_TIMEOUT_VALUE = "service.ner-extractor.socket-timeout-secs";

	private static Logger logger = LoggerFactory.getLogger(ExtractService.class);

	@Autowired
	public ExtractService(Environment environment) {
		super(new HttpClientConfig()
				.setSocketTimeoutInSecs(Integer.valueOf(environment.getProperty(PROP_NAME_SOCKET_TIMEOUT_VALUE))));
	}

	public AttributeData getExtract(String text, List<String> entityList) {
		String requestNumber = generateCorrelationId();
		AttributeData attributeData = null;
		List<String> outputList = new ArrayList<>();
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();

		jsonRequestBuilder.add(MODELID, environment.getProperty(MODELID));
		jsonRequestBuilder.add(REQUESTNUMBER, requestNumber);
		jsonRequestBuilder.add(PORTFOLIONAME, environment.getProperty(PORTFOLIONAME));

		String textContent = StringUtility.findAndReplace(text, StringUtility.NON_ASCI_REGEX, "");
		jsonRequestBuilder.add(TEXTCONTENT, textContent);

		JsonArrayBuilder builder = Json.createArrayBuilder();
		builder.add(CATEGORY);

		jsonRequestBuilder.add(CLASSTOPREDICT, builder.build());

		JsonArrayBuilder builder1 = Json.createArrayBuilder();
		for (String entity : entityList) {
			builder1.add(entity);
		}

		jsonRequestBuilder.add(ENTITIES, builder1.build());
		JsonObject jsonRequest = jsonRequestBuilder.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST,
				environment.getProperty(NER_PREDICTOR_API), jsonRequest);
		float confidence;
		if (jsonResponse != null) {
			JsonObject obj = jsonResponse.getJsonObject("response");
			JsonArray jsonResponseArray = obj.getJsonArray(ENTITIES);
			JsonObject object = jsonResponseArray.getJsonObject(0);
			attributeData = new AttributeData();
			JsonArray values = object.getJsonArray("values");
			for (int i = 0; i < values.size(); i++) {
				JsonObject valuesObject = values.getJsonObject(i);
				outputList.add(valuesObject.getString("value"));
				confidence = Float.parseFloat(valuesObject.get("confidence").toString());
				if (confidence <= 1) {
					confidence = confidence * 100;
				}
				attributeData.setConfidencePct(confidence);
				logger.info("jsonResponse" + jsonResponseArray.toString());
			}
			if (ListUtility.hasValue(outputList)) {
				String result = outputList.stream().collect(Collectors.joining(","));
				attributeData.setAttrValue(result);
			}
		}

		return attributeData;
	}

	// Data class only for this service
	public class FilePathData {
		String fileNumber;
		String fileName;
		String filePath;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
	}

	public class NERResData {
		private String fileName;
		private List<AttributeData> attributes;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}

	}

	@Override
	public List<NERResData> extractAttributes(List<FilePathData> filePathDataList, String paramFileName)
			throws DocwbWebException {

		List<NERResData> nerResDataList = null;
		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		if (ListUtility.hasValue(filePathDataList)) {
			int k = 0;
			for (FilePathData filePathData : filePathDataList) {
				String fileName = filePathData.getFileName();
				String filePath = filePathData.getFilePath();
				String contentId = "";
				if (fileName.equals(paramFileName)) {
					contentId = EngineConstants.NER_EXTRACTION_REQUEST_PART_BODY_VAL;
				} else {
					contentId = EngineConstants.PDF_EXTRACTION_REQUEST_PART_FILE_VAL + (++k);
				}
				httpFileDataList.add(new HttpFileRequestData(fileName, filePath, contentId,
						FileUtility.getContentType(filePath, FileUtility.getFileExtension(fileName))));
			}

			JsonObject jsonResponse = executePostAttachmentWithAuthCall(environment.getProperty(NER_PREDICTOR_API),
					httpFileDataList).getResponse();
			if (jsonResponse == null) {
				throw new DocwbWebException("Error occurred in NER Response");
			}
			if (jsonResponse != null) {
				int responseCde = jsonResponse.getInt("responseCde");
				if (responseCde != 0) {
					throw new DocwbWebException(
							"Error occurred in NER Response : " + jsonResponse.getString(DocwbWebConstants.RESPONSE));
				} else {
					JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
					if (ListUtility.hasValue(jsonResponseArray)) {
						nerResDataList = new ArrayList<>();
						for (int i = 0; i < jsonResponseArray.size(); i++) {
							JsonObject jsonObject = jsonResponseArray.getJsonObject(i);
							if (jsonObject != null) {
								NERResData nerResData = new NERResData();
								List<AttributeData> attributes = null;
								JsonArray attributesArray = jsonObject.getJsonArray(ATTRIBUTES);
								if (ListUtility.hasValue(attributesArray)) {
									attributes = new ArrayList<>();
									for (int j = 0; j < attributesArray.size(); j++) {
										JsonObject jsonAttributeValue = attributesArray.getJsonObject(j);
										for (String key : jsonAttributeValue.keySet()) {
											JsonArray jsonAttrValueArray = jsonAttributeValue.getJsonArray(key);
											if (ListUtility.hasValue(jsonAttrValueArray)) {
												JsonObject jsonAttrValueObject = jsonAttrValueArray.getJsonObject(0);
												AttributeData attributeData = new AttributeData();
												attributeData.setAttrNameTxt(key);
												attributeData.setAttrValue(jsonAttrValueObject.getString(VALUE));
												attributeData
														.setConfidencePct(jsonAttrValueObject.getInt(CONFIDENCE_PCT));
												attributes.add(attributeData);
											}
										}
									}
								}
								nerResData.setAttributes(attributes);
								nerResData.setFileName(jsonObject.getString(FILE_NAME));
								nerResDataList.add(nerResData);
							}
						}
					}
				}
			}
		}
		return nerResDataList;
	}

}
