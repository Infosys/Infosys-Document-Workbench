/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common;

import java.util.HashMap;

public final class InMemoryPropertiesManager {

	private static InMemoryPropertiesManager instance = null;
	private static HashMap<String, String> keyValueMap = new HashMap<>();

	private InMemoryPropertiesManager() {
	}

	public synchronized static InMemoryPropertiesManager getInstance() {
		if (instance == null) {
			instance = new InMemoryPropertiesManager();
		}
		return (instance);
	}

	public synchronized String getProperty(String propertyName) {
		if (keyValueMap.containsKey(propertyName)) {
			return keyValueMap.get(propertyName);
		}
		return null;
	}

	public synchronized String getProperty(String propertyName, String defaultValue) {
		if (keyValueMap.containsKey(propertyName)) {
			return keyValueMap.get(propertyName);
		} else {
			return defaultValue;
		}
	}

	public synchronized void setProperty(String propertyName, String value) {
		keyValueMap.put(propertyName, value);
	}

}
