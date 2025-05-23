/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;

public class DocAndAttrResData extends DocumentDbData {

	private List<AttributeDbData> attributes;

	public List<AttributeDbData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDbData> attributes) {
		this.attributes = attributes;
	}

}
