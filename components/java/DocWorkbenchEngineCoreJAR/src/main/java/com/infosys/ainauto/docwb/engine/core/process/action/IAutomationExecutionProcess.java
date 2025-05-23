/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.process.action;

import java.util.List;

import com.infosys.ainauto.docwb.engine.core.model.db.TransactionDbData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

public interface IAutomationExecutionProcess {

	public int updateResultsForAction(ScriptResponseItemData scriptResponseItemData);

	public long insertTransaction(String transactionIdExt, String parameterName, String parameterValue);

	public int updateTransaction(String transactionIdExt, String transactionExtMsg, String statusTypeTxt);

	public List<TransactionDbData> getTransactionByStatus(int statusTypeCde);

	public int updateExternalTransaction(String transactioExtId, String transactionExtStatus);

	public String getValue(String key);

	public int updateKeyValue(String key, String value);

	public long addKeyValue(String key, String value);
}
