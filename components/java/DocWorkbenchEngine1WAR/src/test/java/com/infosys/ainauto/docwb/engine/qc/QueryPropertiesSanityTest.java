/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.qc;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.StringUtility;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryPropertiesSanityTest {

	private static final Logger logger = LoggerFactory.getLogger(QueryPropertiesSanityTest.class);

	@Test
	public void validateForQueryStatementsUsingSantize() throws Exception {

		Properties queryProperties = new Properties();
		queryProperties.load(this.getClass().getResourceAsStream("/query.properties"));
		for (Object key : queryProperties.keySet()) {
			String query = queryProperties.get(key).toString();
			if (!StringUtility.sanitizeSql(query).equals(query)) {
				logger.error("Query failed in sanitizing - " + query);
				fail("Sanitize SQL test on querty.properties file failed");
				break;
			}
		}
	}

}
