/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.about;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.AboutResData;

@Component
public class AboutProcess implements IAboutProcess {

	@Autowired
	ServletContext servletContext;

	private static final Logger logger = LoggerFactory.getLogger(AboutProcess.class);

	private static final String MANIFEST_CUSTOM_SECTION_NAME = "Nia Build";
	private static final String MANIFEST_PROP_CHANGESET_NUM = "ChangesetNum";
	private static final String MANIFEST_PROP_BUILD_TIME = "BuildTime";
	private static final String MANIFEST_PROP_PROJECT_VERSION = "ProjectVersion";

	@Override
	public AboutResData getProductDetails() throws WorkbenchException {
		AboutResData aboutResData = null;
		InputStream inputStream = null;
		try {
			inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(inputStream);
			Attributes attr = manifest.getMainAttributes();
			aboutResData = new AboutResData();
			attr = manifest.getAttributes(MANIFEST_CUSTOM_SECTION_NAME);
			aboutResData.setProductVersion(attr.getValue(MANIFEST_PROP_PROJECT_VERSION));
			aboutResData.setBuildVersion(attr.getValue(MANIFEST_PROP_CHANGESET_NUM));
			aboutResData.setBuildDtm(attr.getValue(MANIFEST_PROP_BUILD_TIME));
			aboutResData.setStatus("Healthy");
		} catch (Exception ex) {
			logger.error("Error occurred while getting product details", ex);
			throw new WorkbenchException("Error occurred while getting product details", ex);
		} finally {
			FileUtility.safeCloseInputStream(inputStream);
		}
		return aboutResData;
	}
}
