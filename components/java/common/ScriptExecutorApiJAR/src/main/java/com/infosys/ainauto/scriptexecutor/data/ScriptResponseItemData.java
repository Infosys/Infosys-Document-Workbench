/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.data;

import java.util.List;

public class ScriptResponseItemData {

	private String computerName;
	private String errorMessage;
	private String inputCommand;
	private boolean isSuccess;
	private String logData;
	private List<ParameterData> outParameters;
	private String sourceTransactionId;
	private String status;
	private String successMessage;
	private String transactionId;

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getComputerName() {
		return computerName;
	}

	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	public String getInputCommand() {
		return inputCommand;
	}

	public void setInputCommand(String inputCommand) {
		this.inputCommand = inputCommand;
	}

	public List<ParameterData> getOutParameters() {
		return outParameters;
	}

	public void setOutParameters(List<ParameterData> outParameters) {
		this.outParameters = outParameters;
	}

	public String getLogData() {
		return logData;
	}

	public void setLogData(String logData) {
		this.logData = logData;
	}

	public String getSourceTransactionId() {
		return sourceTransactionId;
	}

	public void setSourceTransactionId(String sourceTransactionId) {
		this.sourceTransactionId = sourceTransactionId;
	}
}