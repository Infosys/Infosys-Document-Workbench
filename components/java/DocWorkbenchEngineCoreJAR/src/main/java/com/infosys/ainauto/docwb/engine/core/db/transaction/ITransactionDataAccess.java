/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.transaction;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.model.db.KeyValuePairData;
import com.infosys.ainauto.docwb.engine.core.model.db.TransactionDbData;

@Component
public interface ITransactionDataAccess {

	public long addTransaction(TransactionDbData dbData) throws DocwbEngineException;

	public int updateTransaction(TransactionDbData transactionData) throws DocwbEngineException;

	public List<TransactionDbData> getTransactionByStatus(int statusTypeCde) throws DocwbEngineException;

	public List<KeyValuePairData> getKeyValuePairList(String transactionIdExt) throws DocwbEngineException;

	public int updateExternalTransaction(TransactionDbData transactionData) throws DocwbEngineException;
}
