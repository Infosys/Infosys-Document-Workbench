/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.common.TestHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonSchemaUtilTest {

	private final static Logger logger = LoggerFactory.getLogger(JsonSchemaUtilTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.debug("Created script executor service instance");
	}

	@Test
	public void testGenerateJsonSchema() throws Exception {
		String jsonSchema = JsonSchemaUtil.generateSchema(StringUtility.class);
		logger.debug("Schema for ", jsonSchema);
		assertTrue("Schema is not empty", jsonSchema.length() > 0);
	}

	@Test
	public void validateJsonDataWithJsonSchema() throws Exception {
		String[] jsonSchema = FileUtility.getContent(TestHelper.getFileFullPathFromClassPath("schemaAttributeExtractorApiResponseV2.json"));
		StringBuilder jsonSchemaBuff = new StringBuilder();
		for(String s: jsonSchema) {
			jsonSchemaBuff.append(s).append("\r\n");
		}		
		String[] jsonData = FileUtility.getContent(TestHelper.getFileFullPathFromClassPath("response.json"));
		StringBuilder jsonDataBuff = new StringBuilder();
		for(String s: jsonData) {
			jsonDataBuff.append(s).append("\r\n");
		}
		if(JsonSchemaUtil.validateSchema(jsonSchemaBuff.toString(), jsonDataBuff.toString()) != null) {
			logger.info("Validation failed"+ JsonSchemaUtil.validateSchema(jsonSchemaBuff.toString(), jsonDataBuff.toString()).getAllMessages());			
		}
		assertTrue("Response is valid", JsonSchemaUtil.validateSchema(jsonSchemaBuff.toString(), jsonDataBuff.toString()) == null);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
	}
	
}
