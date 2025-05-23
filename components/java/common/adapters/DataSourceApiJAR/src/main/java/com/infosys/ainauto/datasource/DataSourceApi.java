/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource;

import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.infosys.ainauto.datasource.spi.IDataSourceProvider;

public class DataSourceApi {

	public static List<IDataSourceProvider> getAllProviders() {
		List<IDataSourceProvider> providerList = new ArrayList<>();
		ServiceLoader<IDataSourceProvider> serviceLoader = ServiceLoader.load(IDataSourceProvider.class);
		serviceLoader.forEach(x -> {
			providerList.add(x);
		});
		return providerList;
	}

	public static IDataSourceProvider getProviderByClassName(String providerClassName) {
		List<IDataSourceProvider> providerList = getAllProviders();
		for (IDataSourceProvider provider : providerList) {
			if (providerClassName.equals(provider.getClass().getName())) {
				return provider;
			}
		}
		throw new ProviderNotFoundException("DataSource provider " + providerClassName + " not found");
	}
}
