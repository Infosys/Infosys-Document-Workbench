/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.qc;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryPropertiesSanityTest {

	private static final Logger logger = LoggerFactory.getLogger(QueryPropertiesSanityTest.class);

	@Test
	public void validateForMissingFieldsInInsertAndUpdateStatements() throws Exception {
		String[] sqlFilesArr = new String[] { "db/query.properties", "db/audit.query.properties" };
		for (String sqlFile : sqlFilesArr) {
			logger.info("Testing for file {}", sqlFile);
			String errorMessage = "";
			try {
				byte[] content = FileUtility.readFile(FileUtility.getFileFullPathFromClassPath(sqlFile));
				String contentStr = new String(content);
				contentStr = contentStr.replace("\\\r\n", "");
				String[] lines = contentStr.split("\r\n");
				String lastFoundSqlName = "";
				for (String line : lines) {
					// logger.info(line);
					if (line.startsWith("#")) {// Ignore comments line
						continue;
					}
					String sqlStatement = line.replaceFirst("=", "#");
					String[] tokens = sqlStatement.split("#");
					if (tokens.length == 2) {
						String sqlName = tokens[0];
						lastFoundSqlName = sqlName;
						String sqlText = tokens[1].toLowerCase();
						if (sqlText.contains("insert")) {
							if (!sqlText.contains("create_by")) {
								errorMessage += "{" + sqlName + "} should contain create_by.\n";
							}
						} else if (sqlText.contains("update")) {
							if (!sqlText.contains("last_mod_by")) {
								errorMessage += "{" + sqlName + "} should contain last_mod_by.\n";
							}
						} else if (sqlText.contains("delete")) {
							errorMessage += "{" + sqlName + "} contains delete operation which is not allowed.\n";
						}

					} else {
						errorMessage += "Not a valid SQL statement (e.g. empty lines) after SQL statement {"
								+ lastFoundSqlName + "}\n";
					}
				}
			} catch (Exception e) {
				logger.error("Error occurred in validateForMissingFieldsInInsertAndUpdateStatements()", e);
				fail("Sanity test on querty.properties file failed");
			}

			if (errorMessage.length() > 0) {
				logger.error(
						"Sanity check failed in query.properties file. Please see violations below:\n" + errorMessage);
				fail("Sanity test on querty.properties file failed");
			}
		}

	}

	@Test
	public void validateForTenantId() throws Exception {
		String[] sqlFilesArr = new String[] { "db/query.properties", "db/audit.query.properties" };
		for (String sqlFile : sqlFilesArr) {
			logger.info("Testing for file {}", sqlFile);
			String errorMessage = "";
			List<String> exclusionList = new ArrayList<>(
					Arrays.asList("getExtract", "getValTableData", "getAnnotationIOBAttrFilter"));
			try {
				byte[] content = FileUtility.readFile(FileUtility.getFileFullPathFromClassPath(sqlFile));
				String contentStr = new String(content);
				contentStr = contentStr.replace("\\\r\n", "");
				String[] lines = contentStr.split("\r\n");
				String lastFoundSqlName = "";
				for (String line : lines) {
					// logger.info(line);
					if (line.startsWith("#")) {// Ignore comments line
						continue;
					}
					String sqlStatement = line.replaceFirst("=", "#");
					String[] tokens = sqlStatement.split("#");
					if (tokens.length == 2) {
						String sqlName = tokens[0];
						lastFoundSqlName = sqlName;
						String sqlText = tokens[1].toLowerCase();
						if (sqlText.contains("insert")) {
							if (!sqlText.contains("tenant_id")) {
								errorMessage += "{" + sqlName + "} should contain tenant_id.\n";
							}
						} else if (sqlText.contains("update")) {
							if (!sqlText.contains("tenant_id")) {
								errorMessage += "{" + sqlName + "} should contain tenant_id.\n";
							}
						} else if (sqlText.contains("select")) {
							if (!exclusionList.contains(sqlName)) {
								if (!sqlText.contains("tenant_id")) {
									errorMessage += "{" + sqlName + "} should contain tenant_id.\n";
								}
							}
						} else if (sqlText.contains("delete")) {
							errorMessage += "{" + sqlName + "} contains delete operation which is not allowed.\n";
						}

					} else {
						errorMessage += "Not a valid SQL statement (e.g. empty lines) after SQL statement {"
								+ lastFoundSqlName + "}\n";
					}
				}
			} catch (Exception e) {
				logger.error("Error occurred in validateForTenantId()", e);
				fail("Sanity test on querty.properties file failed");
			}

			if (errorMessage.length() > 0) {
				logger.error(
						"Sanity check failed in query.properties file. Please see violations below:\n" + errorMessage);
				fail("Sanity test on querty.properties file failed");
			}
		}

	}

	@Test
	public void validateForQueryStatementsUsingSantize() throws Exception {

		Properties queryProperties = new Properties();
		queryProperties.load(this.getClass().getResourceAsStream("/db/query.properties"));
		for (Object key : queryProperties.keySet()) {
			String query = queryProperties.get(key).toString();
			if (!StringUtility.sanitizeSql(query).equals(query)) {
				logger.error("Query failed in sanitizing - " + query);
				fail("Sanitize SQL test on querty.properties file failed");
				break;
			}
		}
		
		Properties auditProperties = new Properties();
		auditProperties.load(this.getClass().getResourceAsStream("/db/audit.query.properties"));
		for (Object key : auditProperties.keySet()) {
			String query = auditProperties.get(key).toString();
			if (!StringUtility.sanitizeSql(query).equals(query)) {
				logger.error("Query failed in sanitizing - " + query);
				fail("Sanitize SQL test on querty.properties file failed");
				break;
			}
		}
	}

}
