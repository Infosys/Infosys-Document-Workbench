/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

public class InputOutputWrapperData<I,O> {

    private I inputData;
    private O outputData;
	public I getInputData() {
		return inputData;
	}
	public void setInputData(I inputData) {
		this.inputData = inputData;
	}
	public O getOutputData() {
		return outputData;
	}
	public void setOutputData(O outputData) {
		this.outputData = outputData;
	}
}
