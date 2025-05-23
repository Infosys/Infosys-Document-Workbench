/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.process.about;

import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;
import com.infosys.ainauto.docwb.ui.model.api.about.AboutResData;

public interface IAboutProcess {

	public AboutResData getProductDetails() throws DocWorkbenchUIException;
}

