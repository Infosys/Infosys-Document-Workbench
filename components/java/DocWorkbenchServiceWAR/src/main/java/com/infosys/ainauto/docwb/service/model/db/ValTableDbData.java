/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.sql.Timestamp;

public class ValTableDbData {

    private int cde;
    private String txt;
    private String createBy;
    private Timestamp createDtm;
    private String lastModBy;
    private Timestamp lastModDtm;
    
    public int getCde() {
        return cde;
    }
    public void setCde(int cde) {
        this.cde = cde;
    }
    public String getTxt() {
        return txt;
    }
    public void setTxt(String txt) {
        this.txt = txt;
    }
    public String getCreateBy() {
        return createBy;
    }
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
    public Timestamp getCreateDtm() {
        return createDtm;
    }
    public void setCreateDtm(Timestamp createDtm) {
        this.createDtm = createDtm;
    }
    public String getLastModBy() {
        return lastModBy;
    }
    public void setLastModBy(String lastModBy) {
        this.lastModBy = lastModBy;
    }
    public Timestamp getLastModDtm() {
        return lastModDtm;
    }
    public void setLastModDtm(Timestamp lastModDtm) {
        this.lastModDtm = lastModDtm;
    }
}
