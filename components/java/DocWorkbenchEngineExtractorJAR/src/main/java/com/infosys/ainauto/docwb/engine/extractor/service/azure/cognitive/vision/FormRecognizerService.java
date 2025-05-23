/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.azure.cognitive.vision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;

@Component
public class FormRecognizerService extends HttpClientBase implements IFormRecognizerService {
	private static Logger logger = LoggerFactory.getLogger(FormRecognizerService.class);

	private static final String FORM_ANALYZE_MODELID_PLACEHOLDER = "{modelId}";
	private static final String FORM_ANALYZE_RESULT_OP_LOCATION = "Operation-Location";
	private static final String FORM_ANALYZE_RESULT = "analyzeResult";
	private static final String FORM_ANALYZE_VERSION_TXT = "version";
	private static final String FORM_ANALYZE_VERSION_VALUE = "2.0.0";
	private static final String FORM_ANALYZE_DOCUMENT_RESULT = "documentResults";
	private static final String FORM_ANALYZE_RESULT_STATUS = "status";
	private static final String FORM_ANALYZE_RESULT_STATUS_SUCCEEDED = "succeeded";
	private static final String FORM_ANALYZE_RESULT_STATUS_NOTSTATRED = "notstarted";
	private static final String FORM_ANALYZE_RESULT_STATUS_STATRING = "starting";
	private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
	private static final String SSL_CERT_PATH_AZURE = "ssl.certificate.path.azure.form.reco";

	@Autowired
	protected FormRecognizerService(Environment environment) {
		super(new HttpClientConfig().setCertificatePath(environment.getProperty(SSL_CERT_PATH_AZURE)));
	}

	public String postAnalyzeForm(ExtractorConfigData extractorConfigData, AttachmentData attachmentData)
			throws Exception {
		String operationLocationUrl = "";
		extractorConfigData.setApi(extractorConfigData.getApi().replace(FORM_ANALYZE_MODELID_PLACEHOLDER,
				extractorConfigData.getModelId()));
		try {
			String contentType = FileUtility.getContentType(attachmentData.getPhysicalPath(),
					FileUtility.getFileExtension(attachmentData.getPhysicalName()));
			List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
			httpFileDataList.add(new HttpFileRequestData(attachmentData.getPhysicalName(),
					attachmentData.getPhysicalPath(), "", contentType));
			HashMap<String, String> headerPropertiesMap = new HashMap<String, String>();
			headerPropertiesMap.put(OCP_APIM_SUBSCRIPTION_KEY, extractorConfigData.getOcpApimSubscriptionKey());
			HttpResponseData responseData = executePostAttachmentWithAuthCall(extractorConfigData.getApi(),
					httpFileDataList, headerPropertiesMap, false);
			if (responseData.getHeaders() != null) {
				operationLocationUrl = responseData.getHeaders().get(FORM_ANALYZE_RESULT_OP_LOCATION);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		logger.info("operationLocationUrl : " + StringUtility.sanitizeReqData(operationLocationUrl));
		return operationLocationUrl;
	}

	public JsonArray getAnalyzeFormResult(ExtractorConfigData extractorConfigData, String operationLocationUrl) {
		HashMap<String, String> headerPropertiesMap = new HashMap<String, String>();
		headerPropertiesMap.put(OCP_APIM_SUBSCRIPTION_KEY, extractorConfigData.getOcpApimSubscriptionKey());
		JsonObject jsonResponse = null;
		JsonArray jsonArray = null;
		boolean fileProcessedStatus = false;
		try {
			while (fileProcessedStatus == false) {
				logger.info("Waiting for 6 secs");
				TimeUnit.SECONDS.sleep(6);
				jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, operationLocationUrl, "",
						headerPropertiesMap);
				logger.info("Status: " + jsonResponse.get(FORM_ANALYZE_RESULT_STATUS));
				String jsonRespStatus = jsonResponse.getString(FORM_ANALYZE_RESULT_STATUS).toLowerCase(Locale.ENGLISH);
				if (jsonRespStatus.equals(FORM_ANALYZE_RESULT_STATUS_SUCCEEDED)) {
					JsonObject resultJsonObject = jsonResponse.getJsonObject(FORM_ANALYZE_RESULT);
					if (resultJsonObject.getString(FORM_ANALYZE_VERSION_TXT).equals(FORM_ANALYZE_VERSION_VALUE)) {
						jsonArray = jsonResponse.getJsonObject(FORM_ANALYZE_RESULT)
								.getJsonArray(FORM_ANALYZE_DOCUMENT_RESULT);
					} else {
						logger.info("Azure Cognitive Service-Form Recognizer Version changed from : "
								+ FORM_ANALYZE_VERSION_VALUE);
					}
					fileProcessedStatus = true;
				} else if ((jsonRespStatus.equals(FORM_ANALYZE_RESULT_STATUS_NOTSTATRED))
						|| (jsonRespStatus.equals(FORM_ANALYZE_RESULT_STATUS_STATRING))) {
					// do nothing
				} else {
					// failed
					fileProcessedStatus = true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return jsonArray;
	}

	@Override
	public String postAnalyzeLayout() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String postAnalyzeReceipt() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String postCopyCustomModel() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String postGenerateCopyAuthorization() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String postTrainCustomModel() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAnalyzeLayoutResult() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAnalyzeReceiptResult() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCopyModelResult() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCustomModel() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCustomModelList() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
