/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class CategoryResData {

	private int categoryNameCde;
	private float confidencePct;
	private float recommendedPct;
	private int actionNameCde;

	public int getCategoryNameCde() {
		return categoryNameCde;
	}

	public void setCategoryNameCde(int categoryNameCde) {
		this.categoryNameCde = categoryNameCde;
	}

	public float getConfidencePct() {
		return confidencePct;
	}

	public CategoryResData setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
		return this;
	}
	
	public float getRecommendedPct() {
		return recommendedPct;
	}

	public CategoryResData setRecommendedPct(float recommendedPct) {
		this.recommendedPct = recommendedPct;
		return this;
	}
	
	public int getActionNameCde() {
		return actionNameCde;
	}

	public CategoryResData setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
		return this;
	}

}
