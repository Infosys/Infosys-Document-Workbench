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
public class SystemUtilityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtilityTest.class);

	@Test
	public void testGetHostName() {
		String value = SystemUtility.getHostName();
		LOGGER.info("hostname {}", value);
		assertTrue("hostName is not empty", value.length() > 0 == true);
	}

	@Test
	public void testGetHostIpAddress() {
		String value = SystemUtility.getHostIpAddress();
		LOGGER.info("hostIpAddress {}", value);
		assertTrue("hostIpAddress is not empty", value.length() > 0 == true);
	}

}
