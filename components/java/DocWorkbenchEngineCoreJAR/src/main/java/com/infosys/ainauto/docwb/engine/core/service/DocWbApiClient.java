/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.web.api.DocWbApiFactory;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IAnnotationService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.api.IOutboundEmailService;
import com.infosys.ainauto.docwb.web.api.ITemplateService;

@Component
public class DocWbApiClient {

	@Autowired
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(DocWbApiClient.class);
	private static final String PROP_NAME_DOCWB_BASE_URL = "docwb.base.url";
	private static final String PROP_NAME_USERNAME = "docwb.username";
	private static final String PROP_NAME_DROWSSAP = "docwb.drowssap";
	private static final String PROP_NAME_THREAD_COUNT = "docwb.thread.count";
	private static final String PROP_NAME_IS_AUTHENTICATE = "docwb.authenticate";
	private static final String PROP_NAME_TENANT_ID = "docwb.tenant.id";
	private static final String PROP_NAME_TEMP_PATH = "docwb.engine.temp.path";
	private static final String PROP_NAME_DEFAULT_MAX_PER_ROUTE = "docwb.default.max.per.route";

	private DocWbApiFactory docWbApiFactory;

	@PostConstruct
	private void init() {
		docWbApiFactory = new DocWbApiFactory(environment.getProperty(PROP_NAME_DOCWB_BASE_URL),
				environment.getProperty(PROP_NAME_USERNAME), environment.getProperty(PROP_NAME_DROWSSAP),
				environment.getProperty(PROP_NAME_TENANT_ID),
				Boolean.valueOf(environment.getProperty(PROP_NAME_IS_AUTHENTICATE)),
				environment.getProperty(PROP_NAME_TEMP_PATH),
				Integer.valueOf(environment.getProperty(PROP_NAME_THREAD_COUNT)),
				Integer.valueOf(environment.getProperty(PROP_NAME_DEFAULT_MAX_PER_ROUTE)));
		docWbApiFactory.startServiceThreads();
		logger.info("Initialized");
	}

	public IDocumentService getDocumentService() {
		return docWbApiFactory.getDocumentService();
	}

	public IActionService getActionService() {
		return docWbApiFactory.getActionService();
	}

	public IAttributeService getAttributeService() {
		return docWbApiFactory.getAttributeService();
	}

	public IOutboundEmailService getOutboundEmailService() {
		return docWbApiFactory.getOutboundEmailService();
	}

	public IAttachmentService getAttachmentService() {
		return docWbApiFactory.getAttachmentService();
	}

	public ITemplateService getTemplateService() {
		return docWbApiFactory.getTemplateService();
	}

	public IAnnotationService getAnnotationService() {
		return docWbApiFactory.getAnnotationService();
	}

	@PreDestroy
	public void onDestroy() throws Exception {
		docWbApiFactory.stopServiceThreads();
		logger.info("DocwbApiClient destroyed");
	}
}
