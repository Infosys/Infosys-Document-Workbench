/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.model;

public class SummaryData {

    private String name;
    private long totalCount = 0;
    private long waitingCount =0;
    private long successfulCount = 0;
    private long failedCount = 0;
    private String workUnit ="";

    public SummaryData(String name, String workUnit) {
    	this.name = name;
    	this.workUnit = workUnit;
    }
    
    public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

    public long getSuccessfulCount() {
		return successfulCount;
	}

	public void setSuccessfulCount(long successfulCount) {
		this.successfulCount = successfulCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(long failedCount) {
		this.failedCount = failedCount;
	}
    
	public long getWaitingCount() {
		return waitingCount;
	}

	public void setWaitingCount(long waitingCount) {
		this.waitingCount = waitingCount;
	}
	
	public String getWorkUnit() {
		return workUnit;
	}

	public void setWorkUnit(String workUnit) {
		this.workUnit = workUnit;
	}
    
    /**
     * Helper method to add counts
     * 
     * @param summaryData
     */
    public void addCounts(SummaryData summaryData) {
        this.totalCount += summaryData.totalCount;
        this.successfulCount+= summaryData.successfulCount;
        this.failedCount+= summaryData.failedCount;
        this.waitingCount+= summaryData.waitingCount;
    }

	@Override
	public String toString() {
		return  "\n#############################################################\n" +
                "### -------------" + name + "-------------\n" + 
                "### Work Unit                  :" + this.workUnit + "\n" +
                "### --------------------------------------\n" +
                "### Total                      :" + this.totalCount + "\n" +
                "### Waiting                    :" + this.waitingCount + "\n" +
                "### Successful                 :" + this.successfulCount + "\n" +
                "### Failed                     :" + this.failedCount + "\n" +
                "#############################################################";
	}

	public String toSimpleString() {
		return  "[WorkUnit=" + this.workUnit + "] " +
				"Total:" + this.totalCount + "|" +
                "Waiting:" + this.waitingCount + "|" +
                "Successful:" + this.successfulCount + "|" +
                "Failed:" + this.failedCount;
	}
	

   }
