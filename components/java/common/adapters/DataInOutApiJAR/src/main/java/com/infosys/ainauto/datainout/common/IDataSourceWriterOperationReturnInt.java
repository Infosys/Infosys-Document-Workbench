/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.common;

import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.common.DataSourceException;

@FunctionalInterface
public interface IDataSourceWriterOperationReturnInt {
	int execute(IDataSourceWriter dataSourceWriter) throws DataSourceException;
}
