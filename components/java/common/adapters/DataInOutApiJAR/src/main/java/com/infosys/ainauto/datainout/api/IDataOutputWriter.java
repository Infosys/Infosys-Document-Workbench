/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.api;

import java.util.List;
import java.util.Map;

import com.infosys.ainauto.datainout.common.DataOutputException;
import com.infosys.ainauto.datainout.model.DataOutputRecord;

public interface IDataOutputWriter {

	/** Basic Operations **/
	public List<Boolean> connect() throws DataOutputException;

	public List<Boolean> disconnect() throws DataOutputException;

	public boolean writeItem(DataOutputRecord dataOutputRecord, Map<String, String> paramsMap) throws DataOutputException;
}
