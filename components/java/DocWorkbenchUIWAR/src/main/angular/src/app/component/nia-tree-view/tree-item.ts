/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class TreeItem {
  constructor(public id: string,
    public label: string,
    public cde?: string,
    public isVisible?: boolean,
    public isSelected?: boolean,
    public url?:string,
    public items?: TreeItem[]) { }
}
