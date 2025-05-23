/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.sentiment;

import com.infosys.ainauto.docwb.web.data.AttributeData;

public interface ISentimentAnalysisService {
	
	public AttributeData getSentimentVal(String text);

}
