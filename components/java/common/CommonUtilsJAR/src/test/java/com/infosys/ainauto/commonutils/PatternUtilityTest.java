/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PatternUtilityTest {

	private static final Logger logger = LoggerFactory.getLogger(PatternUtilityTest.class);

	@Test
	public void testUsername() {
		boolean result;
		result = PatternUtility.isValidUserName("abcde");
		assertTrue(result);
		result = PatternUtility.isValidUserName("abc#de");
		assertTrue(!result);
	}

	@Test
	public void testPassword() {
		boolean result;
		result = PatternUtility.isValidPassword("Abc$de123");
		assertTrue(result);
		result = PatternUtility.isValidPassword("abc~de");
		assertTrue(!result);
	}

	@Test
	public void testGetHtmlImgSrcValues() {
		List<String> stringList;

		stringList = PatternUtility.getHtmlImgSrcValues("<img src='file1.jpg'");
		stringList.forEach(x -> logger.debug(x));
		assertTrue(stringList.size() == 1);
		assertTrue("file1.jpg".equals(stringList.get(0)));

		stringList = PatternUtility.getHtmlImgSrcValues("<img src=\"file1.jpg\"");
		stringList.forEach(x -> logger.debug(x));
		assertTrue(stringList.size() == 1);
		assertTrue("file1.jpg".equals(stringList.get(0)));

		stringList = PatternUtility
				.getHtmlImgSrcValues("<img src='data:image/png;base64, iVBORw0==' alt='randome image1' />");
		stringList.forEach(x -> logger.debug(x));
		assertTrue(stringList.size() == 1);
		assertTrue("data:image/png;base64, iVBORw0==".equals(stringList.get(0)));
	}

}
