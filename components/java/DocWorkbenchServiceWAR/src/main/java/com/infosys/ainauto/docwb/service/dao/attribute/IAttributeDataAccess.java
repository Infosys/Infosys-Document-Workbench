/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attribute;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeNameValueDbData;
import com.infosys.ainauto.docwb.service.model.db.annotation.ExportIOBDbData;

@Component
public interface IAttributeDataAccess {

	/**
	 * This method adds a <b>new</b> attribute to the database.
	 * @param attributeDbData
	 * @return
	 * @throws WorkbenchException
	 */
	public long addNewAttribute(AttributeDbData attributeDbData) throws WorkbenchException;
	
	/**
	 * The current design is to <b>(a) end-date active row in database </b> and <b>(b)
	 * create a new row in database</b> whenever an attribute is getting updated.
	 * <p>
	 * This method does part <b>(b)</b>. It ensures the <b>create_by</b> of the
	 * soft-deleted row is copied to the new row. And <b>last_mod_by</b> is set
	 * as current user.
	 * </p>
	 * @param attributeDbData
	 * @param prevCreateBy
	 * @return
	 * @throws WorkbenchException
	 */
	public long addExistingAttribute(AttributeDbData attributeDbData, String prevCreateBy) throws WorkbenchException;

	public List<AttributeDbData> getDocumentAttributes(long docId) throws WorkbenchException;

	public List<AttributeDbData> getAttachmentAttributes(long docId, String attachmentIds) throws WorkbenchException;
	
	public List<AttributeDbData> getAttachmentAttributesOrigValue(long docId,boolean origValue) throws WorkbenchException;

	public AttributeDbData deleteAttribute(AttributeDbData attributeDbData) throws WorkbenchException;

	public List<AttributeDbData> getAttributeText() throws WorkbenchException;

	public List<AttributeNameValueDbData> getAttributeNameValues(String attrNameCdes) throws WorkbenchException;

	public List<ExportIOBDbData> getAnnotationIob(AttributeDbData attributeDbData, Date startDtm, Date endDtm)
			throws WorkbenchException;

	public long addAttributeSource(long docId, String record) throws WorkbenchException;

	public long deleteAttributeSource(long docId) throws WorkbenchException;
}
