/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

public class RecommendedActionData {

	private float confidencePct;
	private int actionNameCde;
	private float recommendedPct;

	public float getConfidencePct() {
		return confidencePct;
	}

	public RecommendedActionData setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
		return this;
	}

	public void setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
	}

	public int getActionNameCde() {
		return actionNameCde;
	}

	public float getRecommendedPct() {
		return recommendedPct;
	}

	public void setRecommendedPct(float recommendedPct) {
		this.recommendedPct = recommendedPct;
	}
}
