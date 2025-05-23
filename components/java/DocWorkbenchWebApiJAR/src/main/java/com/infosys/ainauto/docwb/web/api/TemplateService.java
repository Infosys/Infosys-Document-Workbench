/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.ActionTempMappingData;

public class TemplateService extends HttpClientBase implements ITemplateService {
	
	private static Logger logger = LoggerFactory.getLogger(TemplateService.class);
	private static final String PROP_NAME_API_TEMPLATE_FLATTENED_URL = "docwb.api.template.flattened.url";
	
	//Protected constructor to avoid instantiation by outside world
	protected TemplateService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig) {
		super(null, bearerAuthConfig);
	}
	
	public List<ActionTempMappingData> getFlattenedTemplates(long docId) {
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_TEMPLATE_FLATTENED_URL));
			if (docId > 0)
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getFlattenedTemplates", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		JsonArray responseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
		List<ActionTempMappingData> actionTempMappingDataList = new ArrayList<ActionTempMappingData>();
		for (int k = 0; k < responseArray.size(); k++) {
			JsonObject object = responseArray.getJsonObject(k);
			ActionTempMappingData actionTempMappingData = new ActionTempMappingData();
			actionTempMappingData.setTemplateName(object.getString(DocwbWebConstants.TEMPLATE_NAME));
			actionTempMappingData
					.setIsRecommendedTemplate(object.getBoolean(DocwbWebConstants.IS_RECOMMENDED_TEMPLATE));
			actionTempMappingData.setTemplateText(object.getString(DocwbWebConstants.TEMPLATE_TEXT, null));
			actionTempMappingData.setTemplateHtml(object.getString(DocwbWebConstants.TEMPLATE_HTML, null));
			actionTempMappingDataList.add(actionTempMappingData);
		}
		return actionTempMappingDataList;
	}

}
