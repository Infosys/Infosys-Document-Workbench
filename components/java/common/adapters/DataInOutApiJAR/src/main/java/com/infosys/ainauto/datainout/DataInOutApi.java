/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout;

import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.infosys.ainauto.datainout.spi.IDataInOutProvider;


public class DataInOutApi {

	public static List<IDataInOutProvider> getAllProviders() {
		List<IDataInOutProvider> providerList = new ArrayList<>();
		ServiceLoader<IDataInOutProvider> serviceLoader = ServiceLoader.load(IDataInOutProvider.class);
		serviceLoader.forEach(x -> {
			providerList.add(x);
		});
		return providerList;
	}

	public static IDataInOutProvider getProviderByClassName(String providerClassName) {
		List<IDataInOutProvider> providerList = getAllProviders();
		for (IDataInOutProvider provider : providerList) {
			if (providerClassName.equals(provider.getClass().getName())) {
				return provider;
			}
		}
		throw new ProviderNotFoundException("DataInOutApi provider " + providerClassName + " not found");
	}

}
