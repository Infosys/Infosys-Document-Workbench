/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

public class TenantResourceHelper {

	private static final Logger logger = LoggerFactory.getLogger(TenantResourceHelper.class);

	public static String validateAndReturnRuleFileLocation(String ruleFolderName, String tenantId,
			String ruleFileName) throws DocWbRulesException {
		String tenantIdFolderLocation = ruleFolderName + "/" + tenantId;
		String ruleFileLocation = tenantIdFolderLocation + "/" + ruleFileName;
		logger.debug("ruleFileLocation=" + ruleFileLocation);

		boolean ruleFileExists = FileUtility.doesResourceExist(ruleFileLocation);
		if (!ruleFileExists) {
			// If file doesn't exist, do additional check for tenantId folder
			boolean ruleFolderExists = FileUtility.doesResourceExist(tenantIdFolderLocation);
			if (!ruleFolderExists) {
				throw new DocWbRulesException(EnumApiResponseCde.INVALID_TENANT_ID,
						EnumApiResponseCde.INVALID_TENANT_ID.getMessageValue() + ": " + tenantId);
			}
			throw new DocWbRulesException(EnumApiResponseCde.INVALID_RESOURCE,
					EnumApiResponseCde.INVALID_RESOURCE.getMessageValue() + ": " + ruleFileName);
		}
		return ruleFileLocation;
	}

	public static String validateAndReturnTenantJsonFileLocation(String jsonFolderName, String tenantId)
			throws DocWbRulesException {
		String jsonFileLocation = jsonFolderName + "/" + tenantId + DocWbConstants.FILE_EXTENSION_JSON;
		boolean ruleFileExists = FileUtility.doesResourceExist(jsonFileLocation);
		if (!ruleFileExists) {
			throw new DocWbRulesException(EnumApiResponseCde.INVALID_TENANT_ID,
					EnumApiResponseCde.INVALID_TENANT_ID.getMessageValue() + ": " + tenantId);
		}
		return jsonFileLocation;
	}

}
