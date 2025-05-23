/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.datainout.spi.IDataInOutProvider;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataInOutApiTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testSceneario1Connect() {
		List<IDataInOutProvider> DataInputAdapterProviderList = DataInOutApi.getAllProviders();
		assertTrue(DataInputAdapterProviderList.size() == 0);
	}

}
