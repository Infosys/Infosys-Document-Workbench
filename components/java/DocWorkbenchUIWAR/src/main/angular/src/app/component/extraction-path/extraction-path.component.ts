/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { SessionService } from '../../service/session.service';
import { DocumentData } from '../../data/document-data';
import { BaseComponent } from '../../base.component';
import { ElasticsearchService } from '../../service/elasticsearch.service';
import { AttributeData } from '../../data/attribute-data';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { UtilityService } from '../../service/utility.service';
import { DataService } from '../../service/data.service';
import { AttachmentData } from '../../data/attachment-data';
import { CONSTANTS } from '../../common/constants';
import { QueryData } from '../../data/query-data';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-extraction-path',
  templateUrl: './extraction-path.component.html',
  styleUrls: ['./extraction-path.component.scss']
})
export class ExtractionPathComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "ExtractionPathComponent";
  }
  @Output() close = new EventEmitter<string>();
  @Input('minheight')
  set minheight(minheight: number) {
    this.model.minheight = minheight;
  }
  @Input('closeButton')
  set closeButton(closeButton: boolean) {
    this.model.closeButton = closeButton;
  }
  @Input()
  set isDocTypeFile(isDocTypeFile: boolean) {
    this.model.isDocTypeFile = isDocTypeFile;
  }
  @Input('popupButton')
  set popupButton(popupButton: boolean) {
    this.model.popupButton = popupButton;
  }

  @Input()
  set document(document: DocumentData) {
    this.model.document = document;
  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this.model._isDataReady = isDataReady;
  }

  // To simulate the DB call while page load, attachmentDataList received separately from parent component
  private _attachmentDataListFromParent: AttachmentData[];
  @Input()
  set attachmentDataList(attachmentDataList: AttachmentData[]) {
    this._attachmentDataListFromParent = attachmentDataList;
  }
  // To simulate the DB call while page load, attachmentAttrDataList received separately from parent component
  private _attachmentAttrDataListFromParent: AttributeData[];
  @Input()
  set attachmentAttrDataList(attachmentAttrDataList: AttributeData[]) {
    this._attachmentAttrDataListFromParent = attachmentAttrDataList;
  }

  @Input()
  set queueNameCde(queueNameCde: number) {
    this.model.queueNameCde = queueNameCde;
  }

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  closeResult: string;

  model: any = {
    minheight: undefined,
    closeButton: undefined,
    _isDataReady: undefined,
    popupButton: true,
    isDataLoaded: false,
    attrSourceDataList: [],
    document: undefined,
    attachmentDataList: undefined,
    attachmentAttrDataList: undefined,
    queueNameCde: undefined
  }
  private attachmentId = 0;
  private _bsEDComponentEvent: any;
  private _attrSourceImgData = {};
  private _selectedEDSourceNames = [];
  private _renderPageForEDSourceArray = [];
  private _sourceImageBbox = {};
  // private _sourceImageBbox = {
  //   "1":[0, 0, 3291, 2568],
  //   "2":[0, 0, 3291, 2568]
  // };

  constructor(private modalService: NgbModal, public sessionService: SessionService, private es: ElasticsearchService,
    private toastr: ToastrService, private msgInfo: MessageInfo, private utilityService: UtilityService,
    private dataService: DataService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  ngOnInit() {
    const parent = this;
    parent.model.attachmentDataList = parent._attachmentDataListFromParent;
    parent.model.attachmentAttrDataList = parent._attachmentAttrDataListFromParent;
    this._bsEDComponentEvent = this.dataService.postToEDComponent.subscribe(data => {
      if (data !== null) {
        if (data instanceof Object) {
          if (data.hasOwnProperty('isFileComponentLoaded')) {
            this.model.isEDReadyToModify = data.isFileComponentLoaded;
          }
          if (data.hasOwnProperty('attachmentId')) {
            this.attachmentId = data.attachmentId;
            this.refreshComponent();
          }
          if (data.hasOwnProperty('isPDFZoomFactorChanged')) {
            this.onPDFZoomManageSelectedAttribute();
          }
          if (data.hasOwnProperty('pdfPageRendered')) {
            this.onPDFPageRenderedManageSelectedAttribute(data['pdfPageRendered']);
          }
        }
      }
    });
  }

  refreshComponent() {
    const parent = this;
    if (parent.model.document != undefined) {
      let upstreamDocIdData = [];
      parent.model.attachmentAttrDataList = this.model.attachmentAttrDataList.filter
        (attachAttr => attachAttr.attachmentId === this.attachmentId);
      parent.model.attachmentAttrDataList.forEach(function (attrData) {
        upstreamDocIdData = attrData.attributes.filter(attributeData =>
          attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID);
      });
      if (upstreamDocIdData.length == 0) {
        upstreamDocIdData = parent.model.document.attributes.filter(x => x["attrNameCde"] === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID);
      }
      if (upstreamDocIdData.length > 0) {
        // upstreamDocIdData[0]["attrValue"] = 'dfaab162-4761-433c-8451-9e33db3328f9';
        const upstreamDocId = upstreamDocIdData[0]["attrValue"];
        console.log("upstreamDocId", upstreamDocId);
        const requestBody = {
          "query": {
            "match": { "doc_id": upstreamDocId }
          }
        };
        const queryData = new QueryData(CONSTANTS.APIS.DOCWBES.ATTR_SRC_INDEX_SEARCH, requestBody)
        parent.es.executeQuery(queryData).then(function (data: any) {
          //console.log("executeQuery", data)
          if (data != undefined && data.hasOwnProperty('hits') && data['hits']['hits']) {
            data = data['hits']['hits'][0]['_source']
            console.log("_source", data);
            parent.getAttributeSourceData(data);
            if ('image_bbox' in data) {
              parent._sourceImageBbox = data['image_bbox'];
            }
          }
        })
      }
      parent.model.isDataLoaded = true;
    }
  }

  arrayEquals(a, b) {
    return Array.isArray(a) &&
      Array.isArray(b) &&
      a.length === b.length &&
      a.every((val, index) => val === b[index]);
  }

  getAttributeSourceData(data: any) {
    const parent = this;
    for (const bizAttrData of data['business_attributes']) {
      const attrSourceData: object = {
        "bizAttrName": bizAttrData["name"],
        "bizAttrValue": bizAttrData["text"],
        "attrNameCde": bizAttrData["docwb_attribute_name_cde"],
        "rawAttrValues": [],
        "rules": []
      };
      for (const attrId of bizAttrData["participated_attribute_ids"]) {
        attrSourceData["rawAttrValues"].push(parent.getAttrValueData(attrId, data));
      }
      // ------------------ LOGIC AS PER CURRENT IN/OUT RULE HISTORY ------------------
      for (const ruleName of bizAttrData['contributed_rules']) {
        const ruleData = bizAttrData['rules_execution_history'][ruleName];
        let ruleInputIds = [];
        if (ruleData['input']['attribute_value_ids']['participated'].length > 0) {
          ruleInputIds = ruleData['input']['attribute_value_ids']['participated'];
        } else {
          ruleInputIds = ruleData['output']['attribute_value_ids']['participated'];
        }
        const ruleOutputIds = ruleData['output']['attribute_value_ids']['selected'];
        let ruleFlatData: object = {
          "name": ruleName,
          "input": [],
          "output": []
        };
        for (const attrId of ruleInputIds) {
          ruleFlatData["input"].push(parent.getAttrValueData(attrId, data));
        }
        for (const attrId of ruleOutputIds) {
          ruleFlatData["output"].push(parent.getAttrValueData(attrId, data));
        }
        attrSourceData["rules"].push(ruleFlatData);
      }
      // ---------------- MANIPULATE LOGIC LOAD DATA IN UI ---------------
      // for(const ruleName of bizAttrData['contributed_rules']){
      //   const ruleData = bizAttrData['rules_execution_history'][ruleName];
      //   let ruleInputIds = ruleData['output']['attribute_value_ids']['participated'];
      //   if (ruleData['input']['attribute_value_ids']['participated'].length>0){
      //     const ruleInputIds1 = ruleData['input']['attribute_value_ids']['participated'];
      //     if (parent.arrayEquals(ruleInputIds,ruleInputIds1)){
      //       ruleInputIds = ruleData['input']['attribute_value_ids']['selected'];
      //     }else{
      //       ruleInputIds = ruleInputIds1;
      //     }
      //   }
      //   const ruleOutputIds = ruleData['output']['attribute_value_ids']['selected'];
      //   let ruleFlatData:object = {
      //     "name" : ruleName,
      //     "input":[],
      //     "output":[]
      //   };
      //   for(const attrId of ruleInputIds){
      //     ruleFlatData["input"].push(parent.getAttrValueData(attrId, data));
      //   }
      //   for(const attrId of ruleOutputIds){
      //     ruleFlatData["output"].push(parent.getAttrValueData(attrId, data));
      //   }
      //   attrSourceData["rules"].push(ruleFlatData);
      // }
      // ----------------------------



      parent.model.attrSourceDataList.push(attrSourceData);
    }
    console.log("flat attrSourceDataList", parent.model.attrSourceDataList);
  }

  getAttrValueData(attrId: any, data: object) {
    const rawAttrId = attrId.substring(0, attrId.lastIndexOf("_"));
    const rawAttrData = data["raw_attributes"].filter(x => x["id"] == rawAttrId)[0];
    let attrValData: any = undefined;
    for (const valData of rawAttrData["values"]) {
      const newAttrValData = valData[valData["type"]].filter(y => y["id"] == attrId);
      if (newAttrValData.length > 0) {
        attrValData = newAttrValData[0];
        break;
      }
    }
    return JSON.parse(JSON.stringify(attrValData));
  }

  isSelected(rawAttrId: String, ruleData: any) {
    const foundData = ruleData.filter(x => x["id"] == rawAttrId);
    return foundData.length > 0 ? true : false;
  }

  getDerivedAttrValue(data) {
    const ruleDataList = data.rules
    if (ruleDataList.length > 0) {
      return ruleDataList[ruleDataList.length - 1]["output"];
    }
    return [];
  }

  manageSelectedAttribute(attrNameTxt: String, isNavigateToPage: Boolean, attributeData: any) {
    const parent = this;
    console.log("manageSelectedAttribute", attrNameTxt, attributeData)
    if (!(attributeData["page"] in this._sourceImageBbox)) {
      parent.toastr.info(parent.msgInfo.getMessage(179));
      return;
    }
    if (isNavigateToPage) {
      parent.navigateToPDFPage(attrNameTxt, attributeData, false);
    }
    parent.annotateAttrNameSource(attrNameTxt, !isNavigateToPage, attributeData);
    // parent.highlightSelectedAttributeRow(attrNameTxt, attributeData);
    // parent.expandSelectedAttributeRow(attributeData);
    return;
  }

  private annotateAttrNameSource(attrNameTxt, isZoomFactorChanged, attrData) {
    const parent = this;
    parent.getOriginalImgData();
    console.log("annotateAttrNameSource", attrNameTxt)

    const c: HTMLCanvasElement = document.querySelector("div[data-page-number='" + attrData['page'] + "']>div>canvas") as HTMLCanvasElement;
    if (c) {
      attrData["pageBbox"] = parent._sourceImageBbox[attrData['page'].toString()];
      const sdw = c.width / attrData["pageBbox"][2];
      const sdh = c.height / attrData["pageBbox"][3];
      const ctx = c.getContext("2d");
      const fillTxtMargin = 2;
      ctx.lineWidth = 1;
      ctx.strokeStyle = "red";
      ctx.fillStyle = "red";
      const page = attrData['page'];
      const selectedEPKey = attrNameTxt + "_" + page + "_" + attrData['bbox'].join("_");
      if (isZoomFactorChanged || parent._selectedEDSourceNames.indexOf(selectedEPKey) == -1) {
        // ---- Add annotation ----
        const l = attrData['bbox'][0] * sdw;
        const t = attrData['bbox'][1] * sdh;
        const w = attrData['bbox'][2] * sdw;
        const h = attrData['bbox'][3] * sdh;
        attrData["isHighlighted"] = true;
        ctx.fillText(attrNameTxt, l, t - fillTxtMargin, w)
        ctx.strokeRect(l, t, w, h);
        if (parent._selectedEDSourceNames.indexOf(selectedEPKey) == -1) {
          parent._selectedEDSourceNames.push(selectedEPKey);
        }
      } else if (parent._selectedEDSourceNames.indexOf(selectedEPKey) > -1 && page in parent._attrSourceImgData) {
        // ---- Remove annotation ----
        attrData["isHighlighted"] = false;
        parent._selectedEDSourceNames.splice(parent._selectedEDSourceNames.indexOf(selectedEPKey), 1);
        ctx.putImageData(parent._attrSourceImgData[attrData['page']], 0, 0);
        for (const selectedData of parent._selectedEDSourceNames) {
          const annSelDataList = selectedData.split("_");
          if (annSelDataList[1] != attrData['page']) {
            continue;
          }
          const l = annSelDataList[2] * sdw;
          const t = annSelDataList[3] * sdh;
          const w = annSelDataList[4] * sdw;
          const h = annSelDataList[5] * sdh;
          ctx.fillText(annSelDataList[0], l, t - fillTxtMargin, w)
          ctx.strokeRect(l, t, w, h);
        }
        parent._renderPageForEDSourceArray = parent._renderPageForEDSourceArray.filter(x => {
          const newKey = x['attrNameTxt'] + "_" + x["attribute"]["page"] + "_" + x["attribute"]['bbox'].join("_");
          return newKey != selectedEPKey;
        });
      }
    }

  }

  private getOriginalImgData() {
    console.log("getOriginalImgData")
    const parent = this;
    for (const page of Object.keys(parent._sourceImageBbox)) {
      const c: HTMLCanvasElement = document.querySelector("div[data-page-number='" + page + "']>div>canvas") as HTMLCanvasElement;
      if (c) {
        const ctx = c.getContext("2d");
        if (!(page in parent._attrSourceImgData)) {
          parent._attrSourceImgData[page] = ctx.getImageData(0, 0, c.width, c.height);
        }
      }
    }
  }

  isAttributeEPHighlighted(attrNameTxt) {
    return this._selectedEDSourceNames.filter(x => x.toString().startsWith(attrNameTxt + "_")).length > 0;
  }

  getContentAnnotateData(attributeData: AttributeData) {
    const attrNameTxt = attributeData.attrNameTxt;
    let annData = [];
    const parent = this;
    if (parent.utilityService.isListHasValue(parent.model.attachmentAnnAttrDataList)) {
      const annotations = JSON.parse(parent.model.attachmentAnnAttrDataList[0]["attrValue"]);
      const foundAttrDataList = annotations.filter(x => x["text"] === attrNameTxt);
      if (parent.utilityService.isListHasValue(foundAttrDataList)) {
        annData = foundAttrDataList.filter(y => y["page"] > 0);
      }
    }
    return annData;
  }

  navigateToPDFPage(attrNameTxt, attributeData, isClickEvent) {
    if (!isClickEvent) {
      const selectedEPKey = attrNameTxt + "_" + attributeData["page"] + "_" + attributeData['bbox'].join("_");
      const isExist = this._renderPageForEDSourceArray.filter(x => x['attrNameTxt'] === attrNameTxt && x['attrNameTxt'] + "_" + x["attribute"]["page"] + "_" + x["attribute"]['bbox'].join("_") === selectedEPKey);
      if (isExist.length == 0) {
        this._renderPageForEDSourceArray.push({ "attrNameTxt": attrNameTxt, "attribute": attributeData });
      }
    }
    this.dataService.publishAttributeOpData({
      'renderPdfPage': attributeData["page"]
    })
  }

  private onPDFPageRenderedManageSelectedAttribute(data) {
    console.log("onPDFPageRenderedManageSelectedAttribute", data);
    const parent = this;
    let removeItems = [];
    for (let i = 0; i < parent._renderPageForEDSourceArray.length; i++) {
      const pageData = parent._renderPageForEDSourceArray[i]
      if (pageData['attribute']['page'] === data['pageNumber']) {
        parent.manageSelectedAttribute(pageData['attrNameTxt'], pageData['attribute'], false);
        removeItems.push(i);
      }
    }
    for (const idx of removeItems) {
      parent._renderPageForEDSourceArray.splice(idx, 1);
    }
  }

  private onPDFZoomManageSelectedAttribute() {
    console.log("onPDFZoomManageSelectedAttribute");
    const parent = this;
    const previousSelectedNames = JSON.parse(JSON.stringify(parent._selectedEDSourceNames));
    parent.resetEDSourceAnnotateData();
    for (const selectedData of previousSelectedNames) {
      const annSelDataList = selectedData.split("_");
      parent.annotateAttrNameSource(annSelDataList[0], true, { "page": annSelDataList[1], "bbox": [annSelDataList[2], annSelDataList[3], annSelDataList[4], annSelDataList[5]] });
    }
  }

  private resetEDSourceAnnotateData() {
    const parent = this;
    parent._attrSourceImgData = {};
    parent._selectedEDSourceNames = [];
  }

  annotateExtractionPathWorkData(data) {
    const c: HTMLCanvasElement = document.querySelector("div[data-page-number='" + data['page'] + "']>div>canvas") as HTMLCanvasElement;
    if (c) {
      console.log("annotateExtractionPathWorkData", c);
      // const scalingRatio = 3291*2568/c.width*c.height;
      const sdw = c.width / 3291;
      const sdh = c.height / 2568;
      const ctx = c.getContext("2d");
      ctx.beginPath();
      ctx.lineWidth = 2;
      ctx.strokeStyle = "red";
      // ctx.clearRect(0,0,c.width,c.height);
      ctx.rect(data['bbox'][0] * sdw, data['bbox'][1] * sdh, data['bbox'][2] * sdw - data['bbox'][0] * sdw, data['bbox'][3] * sdh - data['bbox'][1] * sdh);
      ctx.stroke();
    }
  }

  open(content) {
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }
  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  modalWindow() {
    this.close.emit('window closed');
    this.model.closeButton = false;
    this.model.popupButton = true;
  }

  ngOnDestroy() {
    if (this._bsEDComponentEvent != null) {
      this._bsEDComponentEvent.unsubscribe();
    }
  }


}
