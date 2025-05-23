/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.file;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class Rule002ExtractFileReceivedDate extends AttributeExtractRuleAsyncBase {
	private static Logger logger = LoggerFactory.getLogger(Rule002ExtractFileReceivedDate.class);

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		
		InputData inputData = (InputData) objList.get(0);
		FileData fileData = inputData.getFileData();
		DateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss z");
		String dateCreated = dateFormat.format(fileData.getFileCreationTime().toMillis());
		logger.info("File : {} and Date Created : {}", fileData.getFileAbsolutePath(), dateCreated );
		
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(EnumSystemAttributeName.RECEIVED_DATE.getCde()).setAttrValue(dateCreated) // 2=ReceivedDate
			.setExtractType(EnumExtractType.DIRECT_COPY).setConfidencePct(100);
	
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}

}
