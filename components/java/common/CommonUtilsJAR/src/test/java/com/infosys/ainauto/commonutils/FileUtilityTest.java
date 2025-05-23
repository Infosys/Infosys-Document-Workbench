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
public class FileUtilityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtilityTest.class);

	@Test
	public void testGetAbsolutePathWindowsWhenPathIsAbsolute() {
		String location = "D:/EmailWorkbench/temp";
		String absolutePath = FileUtility.getAbsolutePath(location);
		LOGGER.info(absolutePath);
		assertTrue("Absolute path is same as input", absolutePath.equalsIgnoreCase(location));
	}
	
	@Test
	public void testGetAbsolutePathLinuxWhenPathIsAbsolute() {
		String location = "//home/projadmin/workarea/docwbsln/data/temp";
		String absolutePath = FileUtility.getAbsolutePath(location);
		LOGGER.info(absolutePath);
		assertTrue("Absolute path is same as input", absolutePath.equalsIgnoreCase(location));
	}
	
	@Test
	public void testGetAbsolutePathWindowsWhenPathIsRelative() {
		String location = "temp";
		String absolutePath = FileUtility.getAbsolutePath(location);
		LOGGER.info(absolutePath);
		assertTrue("Absolute path is same as input", !absolutePath.equalsIgnoreCase(location));
	}
	
	@Test
	public void testCleanPath() {
		String location = "D:\\EmailWorkbench\\temp";
		assertTrue(location.equals(FileUtility.cleanPath(location)));
	}
}
