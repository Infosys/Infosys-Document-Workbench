/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListUtilityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListUtilityTest.class);

	@Test
	public void testGetSubList() {
		int noOfPartitions = 3;
		List<Integer> dataList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 4, 7, 2, 9, 0, 2);
		List<List<Integer>> test = ListUtility.convertListToPartitions(dataList, noOfPartitions);
		LOGGER.info(test.toString());
		assertTrue(test.size() <= noOfPartitions);
	}

}
