/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class DocAttrWrapperDbData {

    private DocumentDbData documentDbData;
    private AttributeDbData attributeDbData;
    
    public DocumentDbData getDocumentDbData() {
        return documentDbData;
    }
    public void setDocumentDbData(DocumentDbData documentDbData) {
        this.documentDbData = documentDbData;
    }
    public AttributeDbData getAttributeDbData() {
        return attributeDbData;
    }
    public void setAttributeDbData(AttributeDbData attributeDbData) {
        this.attributeDbData = attributeDbData;
    }
    
    
}
