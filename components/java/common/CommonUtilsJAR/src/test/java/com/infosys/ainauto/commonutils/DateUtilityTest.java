/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DateUtilityTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtilityTest.class);

	@Test
	public void testToIncrementDate() {
		String TIMESTAMP_FORMAT_24HR="yyyy-MM-dd HH:mm:ss";
		String dateStr ="2019-01-01 20:45:48.439";
		int daysToAdd=1;
		Date date=DateUtility.toTimestamp(dateStr,TIMESTAMP_FORMAT_24HR);
		Date addDate = DateUtility.addDate(date,daysToAdd);
		String datewithTimestamp=DateUtility.toString(addDate,TIMESTAMP_FORMAT_24HR);
		LOGGER.info(datewithTimestamp);
		assertTrue("Date with Timestamp", datewithTimestamp.equalsIgnoreCase("2019-01-02 20:45:48"));
	}
	
	
}
