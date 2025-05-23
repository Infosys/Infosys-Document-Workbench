/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.FileData;
import com.infosys.ainauto.docwb.web.data.InputData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule030ExtractFileName extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule030ExtractFileName.class);

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
		try {
			InputData inputData = (InputData) objList.get(0);
			FileData fileData = inputData.getFileData();
			String fileName = fileData.getFileName();
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(EnumSystemAttributeName.FILE_NAME.getCde()) // 30=Filename
					.setAttrValue(fileName).setExtractType(EnumExtractType.DIRECT_COPY);

			attributeDataList = new ArrayList<>(Arrays.asList(attributeData));

		} catch (Exception e) {
			logger.error("Error occurred while extracting file name");
		}

		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
}