/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public interface IDocumentService {

	public long addNewDocumentWithAttributes(DocumentData documentData) throws DocwbWebException;

	public void updateDocTaskStatus(long docId, EnumTaskStatus taskStatus);

	/**
	 * Method gets a list of documents for the given queuenameCde.
	 * 
	 */
	public List<DocumentData> getDocumentList(EnumEventType highestEventType,
			EnumEventOperator highestEventTypeOperator, EnumEventType latestEventType,
			EnumEventOperator latestEventTypeOperator, long docId, int queueNameCde, String attrNameCdes);

	/**
	 * Method gets a list of documents list for the given queuenameCdes.
	 * 
	 */
	public List<List<DocumentData>> getDocumentList(EnumEventType highestEventType,
			EnumEventOperator highestEventTypeOperator, EnumEventType latestEventType,
			EnumEventOperator latestEventTypeOperator, long docId, List<String> queueNameCdes, String attrNameCdes);

	public void insertDocEventType(long docId, EnumEventType eventType);

	public boolean assignCase(long docId, long appUserId) throws DocwbWebException;
}
