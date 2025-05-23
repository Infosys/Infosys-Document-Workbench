/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.data.AnnotationData;

public class AnnotationHelper {
	
	public static JsonArray getAnnotations(List<AnnotationData> annotations) {
		JsonArrayBuilder annBuilder = Json.createArrayBuilder();
		if (ListUtility.hasValue(annotations)) {
			for (AnnotationData data : annotations) {
				JsonObjectBuilder annData = Json.createObjectBuilder();
				annData.add(DocwbWebConstants.VALUE, data.getValue());
				annData.add(DocwbWebConstants.LABEL, data.getLabel());
				annData.add(DocwbWebConstants.OCCURRENCENUM, data.getOccurrenceNum());
				annBuilder.add(annData);
			}
		}
		
		return annBuilder.build();
	}

}
