/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.process;

import java.util.List;

public class AnnotationData {

	private long id;
	private String quote;
	private String text;
	private int createdByTypeCde;
	private int occurrenceNum;
	private int page;
	private List<Integer> pageBbox;
	private List<Integer> sourceBbox;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the createdByTypeCde
	 */
	public int getCreatedByTypeCde() {
		return createdByTypeCde;
	}

	/**
	 * @param createdByTypeCde
	 *            the createdByTypeCde to set
	 */
	public void setCreatedByTypeCde(int createdByTypeCde) {
		this.createdByTypeCde = createdByTypeCde;
	}

	/**
	 * @return the quote
	 */
	public String getQuote() {
		return quote;
	}

	/**
	 * @param quote the quote to set
	 */
	public void setQuote(String quote) {
		this.quote = quote;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
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

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public List<Integer> getPageBbox() {
		return pageBbox;
	}

	public void setPageBbox(List<Integer> pageBbox) {
		this.pageBbox = pageBbox;
	}

	public List<Integer> getSourceBbox() {
		return sourceBbox;
	}

	public void setSourceBbox(List<Integer> sourceBbox) {
		this.sourceBbox = sourceBbox;
	}


}
