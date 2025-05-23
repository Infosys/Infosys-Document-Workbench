/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.common;

public class EngineConstants {

	public static final int ATTR_NAME_CDE_INVOICE_NUMBERS = 18;
	public static final int ATTR_NAME_CDE_PURCHASE_ORDER = 21;
	public static final int ATTR_NAME_CDE_ACCOUNT_NUMBER = 22;
	public static final int ATTR_NAME_CDE_CREDITCARD_NUMBER = 23;
	public static final int ATTR_NAME_CDE_ORDER_NUMBERS = 24;
	public static final int ATTR_NAME_CDE_ASSIGNMENT_GROUP = 26;
	public static final int ATTR_NAME_CDE_PLAN_TYPE = 102;
	public static final int ATTR_NAME_CDE_PLAN_TERM = 103;
	public static final int ATTR_NAME_CDE_MODEL = 104;
	public static final int ATTR_NAME_CDE_CONFIGURATION = 105;
	public static final int ATTR_NAME_CDE_LAST_TRANSACTION_AMOUNT = 1010;
	public static final int ATTR_NAME_CDE_LAST_TRANSACTION_STATUS = 1011;

	public static final String PLAN_TYPE_EXTRACTION_REGEX = "Co.*?[\\d]";

	public static final String CONFIGURATION_EXTRACTION_REGEX = "[0-9]{1,3}?.gb";

	public static final String ATTR_NAME_TXT_EMAIL_ADDRESS = "Email Address";

	public static final String MODEL_EXTRACTION_REGEX = "iphone\\s.{2,6}[^A-Za-z0-9]";
	//
	// public static final String PLAN_TERM_EXTRACTION_REGEX =
	// "\\d{1,2}\\s?.[\\w]{4,6}\\s?.[c][a-zA-Z]{0,7}";

	public static final String PDF_EXTRACTION_REQUEST_PART_VAL = "requestData";
	public static final String PDF_EXTRACTION_REQUEST_PART_FILE_VAL = "file";
	public static final String EXTRACTION_JSON_FILE_EXTENSION = ".json";
	public static final String EXTRACTION_CSV_FILE_EXTENSION = ".csv";
	public static final String PDF_ANNOTATED_FILE = "-annotate.pdf";

	public static final String GROUP_NAME_INVOICE_BILL_COPY = "userinfo";
	public static final String GROUP_NAME_KYC = "kycinfo";
	public static final int CONFIDENCE_PCT_UNDEFINED = -1;

	public static final String ATTR_NAME_VALUE_UNKNOWN = "Unknown";

	public static final String ATTR_VALUE_DELIMITER = ",";

	public static final String ACTION_RESULT_EXPORT_SUCCESS = "Success: File exported to ";

	public static final String APPLICATION_TYPE_PDF = "application/pdf";

	public static final String GROUP_NAME_MNDOT = "mndotinfo";
	public static final String NER_EXTRACTION_REQUEST_PART_BODY_VAL = "body";
}
