/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSourceApiTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testSceneario1Connect() {
		List<IDataSourceProvider> dataSourceAdapterProviderList = DataSourceApi.getAllProviders();
		assertTrue(dataSourceAdapterProviderList.size() == 0);
	}

}
