/** =============================================================================================================== *
 * Copyright 2017 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;

public final class PropertyManager {

	private static PropertyManager instance = null;
	private static Logger logger = LoggerFactory.getLogger(PropertyManager.class);
	private static final String INTERNAL_PROP_FILE_PATH = "db/auditlog.properties";
	private static Properties properties;

	private PropertyManager() {
		// private constructor for singleton class
		properties = loadInternalProperties(INTERNAL_PROP_FILE_PATH);
	}

	public synchronized static PropertyManager getInstance() {
		if (instance == null) {
			instance = new PropertyManager();
		}
		return (instance);
	}

	public String getProperty(String propertyName) {
		String val = null;
		if (propertyName != null) {
			if (properties != null) {
				val = (String) properties.getProperty(propertyName);
				// E.g. docwb.base.url=http://10.177.120.69:8081/docwbservice
				// docwb.api.doc.url=${docwb.base.url}/api/v1/document
				String embededVariableName = extractVariableName(val);
				if (embededVariableName.length() > 0) {
					String embededVariableValue = properties.getProperty(embededVariableName);
					val = val.replace("${" + embededVariableName + "}", embededVariableValue);
				}
			}
		}
		return (val);
	}

	public void setProperty(String propertyName, String propertyValue) {
		properties.setProperty(propertyName, propertyValue);
	}

	private static Properties loadInternalProperties(String propertiesFile) {
		InputStream input = null;
		// load a properties file from class path, inside static method
		Properties properties = new Properties();
		try {
			input = PropertyManager.class.getClassLoader().getResourceAsStream(propertiesFile);
			if (input == null) {
				logger.info("Sorry, unable to find " + propertiesFile);
				return null;
			}

			try {
				properties.load(input);
			} catch (IOException e) {
				logger.error("Sorry, unable to find " + e);
			}
		} catch (Exception e) {
			logger.error("Exception occured" + e);
		} finally {
			FileUtility.safeCloseInputStream(input);
		}
		return properties;
	}

	private static String extractVariableName(String text) {
		if (text == null || text.length() == 0 || !text.contains("${") || !text.contains("}")) {
			return "";
		}
		int startIndex = text.indexOf("${");
		int endIndex = text.indexOf("}");

		return text.substring(startIndex + 2, endIndex);
	}
}
