/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StringUtilityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtilityTest.class);

	@Test
	public void testHasValue() {
		boolean result = StringUtility.hasValue(" ");
		LOGGER.info(String.valueOf(result));
		assertTrue("' ' has value", result == true);
	}

	@Test
	public void testHasTrimmedValue() {
		boolean result = StringUtility.hasTrimmedValue(" ");
		LOGGER.info(String.valueOf(result));
		assertTrue("' ' has value", result == false);
	}

	@Test
	public void testGetCapitalizedString() {
		try {
			String[] textToProcess = { "_doe","john_doe", "j_doe" };
			for (int i = 0; i < textToProcess.length; i++) {
				String output = StringUtility.getCapitalizedName(textToProcess[i]);
				System.out.println(output);
			}
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

}
