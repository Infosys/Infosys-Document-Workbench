/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.creditcard;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;

@PropertySource("classpath:engineextractor.properties")
@Component
public class CreditCardService extends HttpClientBase implements ICreditCardService {

	// Data class only for this service
	public static class FilePathData {
		String fileNumber;
		String fileName;
		String filePath;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
	}

	public static class FileContentResData {
		private int fileNumber;
		private String fileName;
		private String fileContent;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileContent() {
			return fileContent;
		}

		public void setFileContent(String fileContent) {
			this.fileContent = fileContent;
		}

		public int getFileNumber() {
			return fileNumber;
		}

		public void setFileNumber(int fileNumber) {
			this.fileNumber = fileNumber;
		}
	}

	public static class LastTransactionResData {
		private String transactionAmount;
		private String transactionStatus;
		
		public String getTransactionStatus() {
			return transactionStatus;
		}

		public void setTransactionStatus(String transactionStatus) {
			this.transactionStatus = transactionStatus;
		}

		public String getTransactionAmount() {
			return transactionAmount;
		}

		public void setTransactionAmount(String transactionAmount) {
			this.transactionAmount = transactionAmount;
		}
	}
	private static final String PROP_NAME_DOCWB_EXTRACTOR_USERNAME = "docwb.extractor.username";
	private static final String PROP_NAME_DOCWB_EXTRACTOR_DROWSSAP = "docwb.extractor.drowssap";
	private static final String CONTENT_ID_FILE = "file";
	private static final String APPLICATION_TYPE_PDF = "application/pdf";
	private static final String RESPONSE = "response";
	private static final String FILE_NUMBER = "fileNumber";
	private static final String FILE_NAME = "fileName";
	private static final String FILE_CONTENT = "fileContent";
	
	@Value("${docwb.extractor.file.content.api.url}")
	private String extractFileContentApi;

	@Autowired
	protected CreditCardService(Environment environment) {
		super(null, new BasicAuthenticationConfig(environment.getProperty(PROP_NAME_DOCWB_EXTRACTOR_USERNAME),
				environment.getProperty(PROP_NAME_DOCWB_EXTRACTOR_DROWSSAP), true));
	}
	

	@Override
	public List<LastTransactionResData> getLastTransaction(String creditCardNum) {
		String attachmentApiUrl="http://vl3mloppf3:8085/emailworkbench/emailworkbench/gettransactiondetails/";
		String creditCardNo=creditCardNum; 
		String getLastTransactionUrl = attachmentApiUrl+ creditCardNo;
		
		List<LastTransactionResData> lastTransactionResDataList = new ArrayList<LastTransactionResData>();

		JsonArray jsonResponse =(JsonArray)executeHttpCall(HttpCallType.GET, getLastTransactionUrl);
		
		for (int k = 0; k < jsonResponse.size(); k++) {
			JsonObject jsonTransactionObj = jsonResponse.getJsonObject(k);

			LastTransactionResData lastTransactionResData = new LastTransactionResData();

			lastTransactionResData.setTransactionStatus(jsonTransactionObj.getString("transactionStatus"));
			lastTransactionResData.setTransactionAmount(jsonTransactionObj.getJsonNumber("transactionAmount").toString());
			
			lastTransactionResDataList.add(lastTransactionResData);
		}

		return lastTransactionResDataList;
	}
}
