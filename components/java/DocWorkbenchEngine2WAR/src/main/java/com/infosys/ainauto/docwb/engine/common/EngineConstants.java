/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.common;

public class EngineConstants {

	public static final String CATEGORY_PASSPORT = "Passport";
	public static final String CATEGORY_REMITTANCE_ADVICE = "Remittance Advice";
	public static final String CATEGORY_LEASE_FOR_PROPERTY = "Lease for Property";
	public static final String CATEGORY_LOAN_AGREEMENT = "Loan Agreement";

	public static final String APPLICATION_TYPE_JSON = "application/json";
	public static final String APPLICATION_TYPE_PDF = "application/pdf";
	public static final String EXTRACTION_JSON_FILE_EXTENSION = ".json";
	public static final String EXTRACTION_CSV_FILE_EXTENSION = ".csv";

	public static final String GROUP_NAME_INVOICE_BILL_COPY = "userinfo";
	public static final String GROUP_NAME_KYC = "kycinfo";
	public static final String GROUP_NAME_INVOICES = "Invoices";
	public static final String GROUP_NAME_ROW = "Row";
	public static final int CONFIDENCE_PCT_UNDEFINED = -1;
	public static final int CONFIDENCE_PCT_DEFAULT = 80;

	public static final String UNKNOWN_ATTR_VALUE = "Unknown";

	public static final String ACTION_RESULT_EXPORT_SUCCESS = "Success: File exported to ";

	public static final String ATTR_VALUE_DELIMITER = ",";

	public static final String GROUP_NAME_LOAN_NOTE = "loaninfo";

	public static final String FILE_EXTENSION_PDF = ".pdf";

	public static final String API_SOURCE_FORMAT_WORD = "word";
	public static final String API_TARGET_FORMAT_PDF = "pdf";

}
