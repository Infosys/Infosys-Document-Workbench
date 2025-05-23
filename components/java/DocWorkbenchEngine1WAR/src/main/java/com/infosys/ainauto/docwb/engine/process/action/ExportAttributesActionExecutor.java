
/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.action;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.SystemUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.stereotype.ActionExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.ActionExecutorBase;
import com.infosys.ainauto.docwb.engine.core.template.action.IActionExecutorListener;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
@ActionExecutor(title = "Export Attributes", propertiesFile = "customization.properties")
public class ExportAttributesActionExecutor extends ActionExecutorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IActionService actionService;

	private IAttributeService attributeService;

	private static Logger logger = LoggerFactory.getLogger(ExportAttributesActionExecutor.class);

	private List<String> queueNameCdeList;
	private int actionNameCde;
	private String exportFilePath;

	@PostConstruct
	private void init() {
		actionService = docWbApiClient.getActionService();
		attributeService = docWbApiClient.getAttributeService();
	}

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		queueNameCdeList = Arrays.asList(properties.getProperty("action.export-attributes.queue.name.cde").split(","));
		actionNameCde = Integer.parseInt(properties.getProperty("action.export-attributes.action.name.cde"));
		exportFilePath = properties.getProperty("action.export-attributes.path");
		return true;
	}

	@Override
	protected List<List<DocActionData>> getActions() throws Exception {
		List<List<DocActionData>> docActionDataListOfList = actionService.getActionList(actionNameCde,
				EnumTaskStatus.YET_TO_START, queueNameCdeList, EnumEventOperator.EQUALS);
		return docActionDataListOfList;
	}

	@Override
	protected void executeAction(ActionParamAttrMappingData actionParamAttrMappingData,
			IActionExecutorListener actionExecutorListener) throws Exception {
		Long docId = actionParamAttrMappingData.getDocId();
		logger.info("Begin executing action with DocId  - " + docId);
		String result = "";
		Exception exception = null;
		try {
			String attributes = attributeService.getAttributesToExport(docId);
			if (attributes != null) {
				String fileName = docId + "_" + UUID.randomUUID().toString();
				String filePath = FileUtility.getConcatenatedName(exportFilePath,
						fileName + EngineConstants.EXTRACTION_JSON_FILE_EXTENSION);
				ObjectMapper mapper = new ObjectMapper();
				Object jsonObject = mapper.readValue(attributes, Object.class);
				String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
				boolean isSuccess = FileUtility.saveFile(filePath, prettyJson);

				// Exporting attributes in CSV files.
				filePath += createCSVFile(fileName, attributes);

				if (isSuccess) {
					String hostname = SystemUtility.getHostName();
					String outputLocation = File.separator + File.separator
							+ FileUtility.getConcatenatedPath(hostname, filePath);
					result = EngineConstants.ACTION_RESULT_EXPORT_SUCCESS + outputLocation;
				}
			}
		} catch (Exception ex) {
			exception = ex;
		}
		actionExecutorListener.onActionExecutionComplete(exception, result);
	}

	// TODO 11/11/20 - Optimization needed in future(Because createCSVFile
	// method logic is used in engine-2 file also).
	private String createCSVFile(String fileName, String attributes) throws Exception {
		DocumentData documentData = AttributeDataHelper.convertJsonToDocumentData(attributes);
		Map<String, List<String>> nonTabularAttributeContent = AttributeDataHelper.convertAttributesToCsv(documentData,
				fileName);
		String resultFileName = "";
		for (String key : nonTabularAttributeContent.keySet()) {
			fileName = key + EngineConstants.EXTRACTION_CSV_FILE_EXTENSION;
			resultFileName += " " + fileName;
			String csvFilePath = FileUtility.getConcatenatedName(exportFilePath, fileName);
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(FileUtility.cleanPath(csvFilePath));
				for (String content : nonTabularAttributeContent.get(key))
					fileWriter.append(content);
				fileWriter.flush();
			} finally {
				FileUtility.safeCloseFileWriter(fileWriter);
			}
		}
		return resultFileName;
	}

}
