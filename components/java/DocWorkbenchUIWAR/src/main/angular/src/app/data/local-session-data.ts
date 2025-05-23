/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class LocalSessionData {

  constructor(
    public caseListSelectedTab: number,
    public sidebarSelectedMenuStyle: number,
    public lastWorkedQueueCde:number,
    public lastDocStatusCde:number,
    public dashboardSelectedTab: number,
    public dashboardSelectedMenuStyle: number
  ) { }

}
