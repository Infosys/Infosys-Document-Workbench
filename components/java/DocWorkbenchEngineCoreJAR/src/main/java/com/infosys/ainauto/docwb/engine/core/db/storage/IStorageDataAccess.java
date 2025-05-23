/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.storage;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;

@Component
public interface IStorageDataAccess {

	public String getValue(String key) throws DocwbEngineException;

	public int updateKeyValue(String key, String value) throws DocwbEngineException;

	public long addKeyValue(String key, String value) throws DocwbEngineException;
}
