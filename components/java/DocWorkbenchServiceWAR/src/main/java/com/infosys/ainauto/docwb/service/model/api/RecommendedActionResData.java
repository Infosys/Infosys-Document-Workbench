/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class RecommendedActionResData {

	private float confidencePct;
	private float recommendedPct;
	private int actionNameCde;

	public float getConfidencePct() {
		return confidencePct;
	}

	public RecommendedActionResData setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
		return this;
	}
	
	public float getRecommendedPct() {
		return recommendedPct;
	}

	public RecommendedActionResData setRecommendedPct(float recommendedPct) {
		this.recommendedPct = recommendedPct;
		return this;
	}
	
	public int getActionNameCde() {
		return actionNameCde;
	}

	public RecommendedActionResData setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
		return this;
	}

}
