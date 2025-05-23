/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.model.db;

import java.sql.Timestamp;

public class TransactionDbData {

	private String transactionExtId;
	private String keyName;
	private String keyValue;
	private int statusTypeCde;
	private String statusTypeTxt;
	private String transactionExtMessage;
	private String transactionExtStatusTxt;
	private Timestamp endDtm;
	private Timestamp transactionExtStartDtm;
	private Timestamp transactionExtEndDtm;
	
	public String getTransactionExtId() {
		return transactionExtId;
	}
	public void setTransactionExtId(String transactionExtId) {
		this.transactionExtId = transactionExtId;
	}
	public String getKeyName() {
		return keyName;
	}
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	public String getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}
	public int getStatusTypeCde() {
		return statusTypeCde;
	}
	public void setStatusTypeCde(int statusTypeCde) {
		this.statusTypeCde = statusTypeCde;
	}
	public String getStatusTypeTxt() {
		return statusTypeTxt;
	}
	public void setStatusTypeTxt(String statusTypeTxt) {
		this.statusTypeTxt = statusTypeTxt;
	}
	public String getTransactionExtMessage() {
		return transactionExtMessage;
	}
	public void setTransactionExtMessage(String transactionExtMessage) {
		this.transactionExtMessage = transactionExtMessage;
	}
	public String getTransactionExtStatusTxt() {
		return transactionExtStatusTxt;
	}
	public void setTransactionExtStatusTxt(String transactionExtStatusTxt) {
		this.transactionExtStatusTxt = transactionExtStatusTxt;
	}
	public Timestamp getEndDtm() {
		return endDtm;
	}
	public void setEndDtm(Timestamp endDtm) {
		this.endDtm = endDtm;
	}
	public Timestamp getTransactionExtStartDtm() {
		return transactionExtStartDtm;
	}
	public void setTransactionExtStartDtm(Timestamp transactionExtStartDtm) {
		this.transactionExtStartDtm = transactionExtStartDtm;
	}
	public Timestamp getTransactionExtEndDtm() {
		return transactionExtEndDtm;
	}
	public void setTransactionExtEndDtm(Timestamp transactionExtEndDtm) {
		this.transactionExtEndDtm = transactionExtEndDtm;
	}
}
