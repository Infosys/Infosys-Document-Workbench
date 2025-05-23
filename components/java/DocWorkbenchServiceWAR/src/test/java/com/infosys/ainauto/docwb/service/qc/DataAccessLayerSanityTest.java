/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.qc;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.docwb.service.common.TestHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataAccessLayerSanityTest {

	private static final Logger logger = LoggerFactory.getLogger(DataAccessLayerSanityTest.class);

	@Test
	public void validateForDuplicatePerformanceLoggingIdentifiers() throws Exception {

		String errorMessage = "";
		try {
			List<String> javaFilePathList = TestHelper.getAllMatchingFiles("src/main/java", "DataAccess.java");
			for (String javaFilePath: javaFilePathList) {
				byte[] content = FileUtility.readFile(javaFilePath);
				String contentStr = new String(content);
				String[] lines = contentStr.split("\r\n");
				List<String> perfLoggerStatementList = new ArrayList<String>();
				for (String line : lines) {
					if (line.contains("PERF_LOGGER.info(")) {
						//E.g. PERF_LOGGER.info("addEmailSql" + "," + timeElapsed + ",secs");
						String[] tokens= line.split("\\+");
						String perfIdentifier = tokens[0].trim().replace("PERF_LOGGER.info(", "");
						if (perfLoggerStatementList.contains(perfIdentifier)) {
							errorMessage+= "File=" + javaFilePath + " contains line which is already used=" + perfIdentifier;
						}else {
							perfLoggerStatementList.add(perfIdentifier);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred in validateForDuplicatePerformanceLoggingIdentifiers()", e);
			fail("Sanity test on data access layer java files failed");
		}
		
		if (errorMessage.length() > 0) {
			logger.error("Sanity check failed on data access layer. Please see violations below:\n" + errorMessage);
			fail("Sanity check failed on data access layer");
		}
	}

}
