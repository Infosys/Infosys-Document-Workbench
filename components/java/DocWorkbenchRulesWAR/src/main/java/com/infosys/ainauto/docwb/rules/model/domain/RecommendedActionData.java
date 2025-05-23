/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

public class RecommendedActionData {
	private float confidencePct;
	private float recommendedPct;
	private int actionNameCde;

	public float getConfidencePct() {
		return confidencePct;
	}

	public void setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
	}

	public float getRecommendedPct() {
		return recommendedPct;
	}

	public void setRecommendedPct(float recommendedPct) {
		this.recommendedPct = recommendedPct;
	}

	public int getActionNameCde() {
		return actionNameCde;
	}

	public void setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
	}
}
