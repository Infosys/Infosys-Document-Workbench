/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.web.common.IServiceThreadHandler;
import com.infosys.ainauto.docwb.web.common.PropertyManager;

public final class DocWbApiFactory {

	private static final Logger logger = LoggerFactory.getLogger(DocWbApiFactory.class);
	private static final String TENANT_ID = "tenantId";
	private static final String PROP_NAME_API_AUTH = "docwb.api.auth.url";
	private int totalThreadCount;

	private IActionService actionService;
	private IAttachmentService attachmentService;
	private IAttributeService attributeService;
	private IDocumentService documentService;
	private IOutboundEmailService outboundEmailService;
	private ITemplateService templateService;
	private IAnnotationService annotationService;

	private List<IServiceThreadHandler> serviceThreadHandlerList;

	public DocWbApiFactory(String baseUrl, String userName, String rawDrowssap, String tenantId, boolean isAuthenticate,
			String tempPath, int threadCount, int defaultMaxPerRoute) {
		// Set properties for using later in other classes
		PropertyManager.getInstance().setProperty("docwb.base.url", baseUrl);
		PropertyManager.getInstance().setProperty("docwb.temp.path", tempPath);

		String authUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_AUTH);

		this.totalThreadCount = threadCount;

		HashMap<String, String> customHeadersMap = new HashMap<>();
		customHeadersMap.put(TENANT_ID, tenantId);

		HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig = new HttpClientBase.Authentication.BearerAuthenticationConfig(
				authUrl, userName, rawDrowssap, customHeadersMap, 2, isAuthenticate);

		serviceThreadHandlerList = new ArrayList<>();
		// Services using threads
		{
			documentService = new DocumentService(bearerAuthConfig, defaultMaxPerRoute);
			serviceThreadHandlerList.add((IServiceThreadHandler) documentService);

			actionService = new ActionService(bearerAuthConfig, defaultMaxPerRoute);
			serviceThreadHandlerList.add((IServiceThreadHandler) actionService);
		}

		attachmentService = new AttachmentService(bearerAuthConfig);
		attributeService = new AttributeService(bearerAuthConfig);
		outboundEmailService = new OutboundEmailService(bearerAuthConfig);
		templateService = new TemplateService(bearerAuthConfig);
		annotationService = new AnnotationService(bearerAuthConfig);
		logger.debug("New instance of DocWbApiFactory created");
	}

	public IActionService getActionService() {
		return actionService;
	}

	public IAttachmentService getAttachmentService() {
		return attachmentService;
	}

	public IAttributeService getAttributeService() {
		return attributeService;
	}

	public IDocumentService getDocumentService() {
		return documentService;
	}

	public IOutboundEmailService getOutboundEmailService() {
		return outboundEmailService;
	}

	public ITemplateService getTemplateService() {
		return templateService;
	}

	public IAnnotationService getAnnotationService() {
		return annotationService;
	}

	/**
	 * Starts all threads
	 */
	public void startServiceThreads() {
		int numOfServices = serviceThreadHandlerList.size();
		// Split the totalThreadCount amongst all services
		int threadsPerServiceCount = (int) Math.ceil(totalThreadCount / (numOfServices * 1.0));
		for (IServiceThreadHandler serviceThreadHandler : serviceThreadHandlerList) {
			serviceThreadHandler.startThreads(threadsPerServiceCount);
		}
		logger.info("Started {} thread pools with {} threads per pool ", numOfServices, threadsPerServiceCount);
	}

	/**
	 * Stops all threads
	 */
	public void stopServiceThreads() {
		for (IServiceThreadHandler serviceThreadHandler : serviceThreadHandlerList) {
			serviceThreadHandler.stopThreads();
		}
		logger.info("Stopped {} thread pools", serviceThreadHandlerList.size());
	}
}
