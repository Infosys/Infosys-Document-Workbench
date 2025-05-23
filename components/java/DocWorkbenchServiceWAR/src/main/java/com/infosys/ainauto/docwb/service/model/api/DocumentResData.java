/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;

public class DocumentResData extends DocumentDbData{
    
    private long docId;
    private int taskStatusCde;
    private int eventTypeCde;
    private int taskTypeCde;
    private List<ActionResData> actionResDataList;

    public long getDocId() {
        return docId;
    }
    public void setDocId(long docId) {
        this.docId = docId;
    }
    public int getTaskStatusCde() {
        return taskStatusCde;
    }
    public void setTaskStatusCde(int taskStatusCde) {
        this.taskStatusCde = taskStatusCde;
    }
    public int getEventTypeCde() {
        return eventTypeCde;
    }
    public void setEventTypeCde(int eventTypeCde) {
        this.eventTypeCde = eventTypeCde;
    }
    public int getTaskTypeCde() {
        return taskTypeCde;
    }
    public void setTaskTypeCde(int taskTypeCde) {
        this.taskTypeCde = taskTypeCde;
    }
    public List<ActionResData> getActionDataList() {
        return actionResDataList;
    }
    public void setActionDataList(List<ActionResData> actionResDataList) {
        this.actionResDataList = actionResDataList;
    }
    
}
