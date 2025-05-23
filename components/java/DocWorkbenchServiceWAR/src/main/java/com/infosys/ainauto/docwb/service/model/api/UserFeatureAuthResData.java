/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class UserFeatureAuthResData {

	private String featureId;
	private long accessLevelCde;

	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}

	public long getAccessLevelCde() {
		return accessLevelCde;
	}

	public void setAccessLevelCde(long accessLevelCde) {
		this.accessLevelCde = accessLevelCde;
	}


}
