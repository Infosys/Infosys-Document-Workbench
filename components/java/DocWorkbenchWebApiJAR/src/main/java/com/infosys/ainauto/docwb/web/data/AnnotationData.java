/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

public class AnnotationData {

	private String value;
	private String label;
	private int occurrenceNum;
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the occurrenceNum
	 */
	public int getOccurrenceNum() {
		return occurrenceNum;
	}
	/**
	 * @param occurrenceNum the occurrenceNum to set
	 */
	public void setOccurrenceNum(int occurrenceNum) {
		this.occurrenceNum = occurrenceNum;
	}

}
