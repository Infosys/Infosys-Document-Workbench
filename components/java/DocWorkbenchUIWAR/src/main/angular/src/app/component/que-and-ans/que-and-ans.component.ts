/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */
 
import { Component, OnInit, Input, ViewContainerRef } from '@angular/core';
import { QueAndAnsService } from '../../service/que-and-ans.service';
import { DataService } from '../../service/data.service';
import { CONSTANTS } from '../../common/constants';
// import { DocumentData } from '../../data/document-data';
import { AttributeHelper } from '../../utils/attribute-helper';
import { UtilityService } from '../../service/utility.service';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';
import { Overlay, OverlayConfig, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { FeedbackPopoverComponent } from '../feedback-popover/feedback-popover.component';


@Component({
  selector: 'app-que-and-ans',
  templateUrl: './que-and-ans.component.html',
  styleUrls: ['./que-and-ans.component.scss']
})
export class QueAndAnsComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "QueAndAnsComponent";
  }
  @Input('minheight')
  set minheight(minheight: number) {
    this.model.minheight = minheight;
  }
  @Input('attachmentAttrDataList')
  set attachmentAttrDataList(attachmentAttrDataList) {
    this.model.attachmentAttrDataList = attachmentAttrDataList;
    console.log("this.model.attachmentAttrDataList", this.model.attachmentAttrDataList)
  }
  @Input('attachmentDataList')
  set attachmentDataList(attachmentDataList) {
    this.model.attachDataList = attachmentDataList;
    console.log("this.model.attachDataList", this.model.attachDataList)
  }
  // For Email Attributes
  @Input('attributeDataList')
  set attributeDataList(attributeDataList) {
    this.model.attributeDataList = attributeDataList;
    console.log("this.model.attributeDataList", this.model.attributeDataList)
  }

  @Input()
  set isDocTypeFile(isDocTypeFile: boolean) {
    this.model.isDocTypeFile = isDocTypeFile;
    if (this.model.isDocTypeFile) {
      this.model.max_height = '29vh';
    } else {
      this.model.max_height = '15vh';
    }
  }

  // // To simulate the DB call while page load, attachmentAttrDataList received separately from parent component
  // private _attachmentAttrDataListFromParent: AttributeData[];
  // @Input()
  // set attachmentAttrDataList(attachmentAttrDataList: AttributeData[]) {
  //   this._attachmentAttrDataListFromParent = attachmentAttrDataList;
  // }

  // @Input()
  // set document(document: DocumentData) {
  //   this.model.document = document;
  // }

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };
  closeResult: string;
  overlayRef: OverlayRef | null = null;

  model: any = {
    pageNo: 0,
    optionChecked: false,
    answers: undefined,
    // textAreaCol:65,
    // queTextAreaCol
    // ansTextAreaRow:6,
    // queTextAreaRow: 4,
    defaultTextAreaRow: 1,
    minheight: undefined,
    attachmentAttrDataList: undefined,
    attachDataList: undefined,
    attributeDataList: undefined,
    document: undefined,
    errorMsg: undefined,
    isQueryButtonClicked: false,
    isDataLoaded: false,
    answerData: undefined,
    from_cache: true,
    sliderValue: 0.5,
    numbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    selectedNumber: 1,
    tooltips: ['Incorrect', 'Somewhat Correct', 'Correct'],
    contentid: "",
    isDisabled:  true,
  }
  // private _bsEDComponentEvent: any;
  // private attachmentId = 0;
  // private upstreamDocIdList = []
  constructor(private qnaService: QueAndAnsService, private dataService: DataService,
    private attributeHelper: AttributeHelper, private utilityService: UtilityService,
    private modalService: NgbModal, private toastr: ToastrService, public niaTelemetryService: NiaTelemetryService, public sessionService: SessionService, public configDataHelper: ConfigDataHelper, private overlay: Overlay, private viewContainerRef: ViewContainerRef) { super(sessionService, configDataHelper, niaTelemetryService) }

  ngOnInit(): void {
    console.log("QnA ngOnInit")

  }
  // ngOnInit() {
  //   const parent = this;
  //   // parent.model.attachmentDataList = parent._attachmentDataListFromParent;
  //   // parent.model.attachmentAttrDataList = parent._attachmentAttrDataListFromParent;
  //   this._bsEDComponentEvent = this.dataService.postToEDComponent.subscribe(data => {
  //     if (data !== null) {
  //       if (data instanceof Object) {
  //         // if (data.hasOwnProperty('isFileComponentLoaded')) {
  //         //   this.model.isEDReadyToModify = data.isFileComponentLoaded;
  //         // }
  //         if (data.hasOwnProperty('attachmentId')) {
  //           this.attachmentId = data.attachmentId;
  //           // this.refreshComponent();
  //         }
  //       }
  //     }
  //   });
  // }

  getdocIdList() {
    const parent = this;
    let upstreamDocIdList = [];
    // if (parent.model.document != undefined) {
    let upstreamDocIdData = [];
    // if (this.attachmentId==0){ 
    //   // Added for emailwb, Need to ask if this is correct or not 
    //   parent.model.attachmentAttrDataList = this.model.attachmentAttrDataList
    // }else{
    //   parent.model.attachmentAttrDataList = this.model.attachmentAttrDataList.filter
    //   (attachAttr => attachAttr.attachmentId === this.attachmentId);
    // }
    // parent.model.attachmentAttrDataList = this.model.attachmentAttrDataList.filter
    //   (attachAttr => attachAttr.attachmentId === this.attachmentId);
    parent.model.attachmentAttrDataList.forEach(function (attrData) {
      upstreamDocIdData.push(attrData.attributes.filter(attributeData =>
        attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID));
    });
    // console.log("upstreamDocIdData",upstreamDocIdData)
    // if (upstreamDocIdData.length == 0) {
    //   upstreamDocIdData = parent.model.document.attributes.filter(
    //     x => x["attrNameCde"] === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID);
    // }
    if (upstreamDocIdData.length > 0) {
      // upstreamDocIdData[0]["attrValue"] = 'dfaab162-4761-433c-8451-9e33db3328f9';
      // const upstreamDocId = upstreamDocIdData[0]["attrValue"];
      // console.log("upstreamDocId", upstreamDocId);

      upstreamDocIdData.forEach(function (upstreamDocId) {
        // console.log("upstreamDocId",upstreamDocId)
        upstreamDocIdList.push(upstreamDocId[0]["attrValue"]);
      });
      // console.log("parent.upstreamDocIdList",upstreamDocIdList)
      // let upstreamDocIdList = upstreamDocIdData.map(upstreamDocId => upstreamDocId["attrValue"]);
    }
    return upstreamDocIdList
    // }
  }
  fetchAnswer(query: String) {
    const parent = this;
    parent.model.isQueryButtonClicked = true;
    parent.model.isDataLoaded = false;
    let params = [];
    console.log("parent.model.isQueryButtonClicked", parent.model.isQueryButtonClicked)
    const doc_ids_list = this.getdocIdList(); //parent.upstreamDocIdList
    console.log("doc_ids_list", doc_ids_list)
    // if (doc_ids_list.length >0 ){
    const promise = parent.qnaService.queryLLM(query, doc_ids_list, parent.model.from_cache,
      parent.model.sliderValue, parent.model.selectedNumber)
    console.log("promise", promise)
    promise.then(result => {
      if (result['responseCde'] === 0) {
        parent.model.answers = result['response']['answers'];
        parent.model.isDataLoaded = true;
        parent.model.errorMsg = undefined
        console.log("result['response']['answers']!", result['response']['answers'])
        console.log("request", parent.model.answers[0].llm_prompt)
        console.log("response", parent.model.answers[0].llm_response)
        params = [
          {
            "request": {
              "prompt_template": JSON.stringify(parent.model.answers[0].llm_prompt.prompt_template),
              "question": JSON.stringify(parent.model.answers[0].llm_prompt.query),
              "context_within_top_k": JSON.stringify(parent.model.answers[0].llm_prompt.context),
              "context_outside_top_k": ""
            },
            "response": {
              "answer": JSON.stringify(parent.model.answers[0].llm_response.response)
            },
            "url": {
              "url": ""
            }
          }
        ]
        console.log("params", params);
        parent.model.contentid = this.createGuid();
        // console.log("guid",parent.model.contentid);
        parent.triggerTelemetryEvents(parent.bmodel.TELE_EVENTS.LOG, "", [], null, params, 0, "", parent.model.contentid);
        parent.model.isQueryButtonClicked = false;
      }
      if (result['responseCde'] === 999) {
        parent.model.errorMsg = result['responseMsg'];
        parent.model.isDataLoaded = true;
        parent.model.answers = undefined
        parent.model.isQueryButtonClicked = false;
      }
    }).catch(error => {
      parent.model.isQueryButtonClicked = false;
      console.log(error);
    });
    // }else{
    //   parent.model.errorMsg="ERROR: DocIdList is Empty"
    // }

  }
  getRowSize() {
    return this.model.defaultTextAreaRow;
  }

  createGuid(): string {
    return 'L-' + 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }
  // isMultiAttribute(attrNameCde: number): boolean {
  //   return +attrNameCde === 44;
  // }

  // isUpstreamDocId(attrNameCde: number): boolean {
  //   return +attrNameCde === 11;
  // }
  isMultiAttribute(attrNameCde: number): boolean {
    return this.attributeHelper.isMultiAttribute(attrNameCde);
  }
  isUpstreamDocId(attrNameCde: number): boolean {
    return this.attributeHelper.isUpstreamDocId(attrNameCde);
  }

  getMaskedFileName(fileName: string, limit: number) {
    // const splittedName = fileName.split('\\');
    return this.utilityService.getTruncatedFileName(fileName, limit);
  }

  save() {

  }
  cancel() {

  }

  updateButtonsState(value: string) {
    this.model.isDisabled = !(value.trim().length >= 1);
  }

  clearQueAns(queTextArea: HTMLTextAreaElement) {
    queTextArea.value = '';
    this.model.answers = [];
    this.model.errorMsg = [];
    this.updateButtonsState(queTextArea.value);
  }

  openLLMDetail(content, answerData) {
    this.model.answerData = answerData
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

  onCopyButtonClickMsg() {
    this.toastr.success("Copied to clipboard! ");
  }

  navigateToPDFPage(pageNum) {
    console.log("navigateToPDFPage", pageNum)
    this.dataService.publishAttributeOpData({
      'renderPdfPage': pageNum
    })
  }

  toggleExpand(type: 'answer' | 'error', answerData: any) {
    if (type === 'answer') {
      answerData.expandedAnswer = !answerData.expandedAnswer;
    } else {
      answerData.expandedError = !answerData.expandedError;
    }
  }

  setRatingAndOpenFeedbackForm(answerData: any, rating: number, contentid: string): void {
    console.log("answerData",answerData)
    answerData.rating = rating;
    this.openFeedbackForm(answerData, rating, contentid);
  }

  openFeedbackForm(answerData: any, rating: number, contentid: string) {
    if (this.overlayRef || answerData.feedbackSubmitted) {
      return;
    }

    const positionStrategy = this.overlay.position()
      .global()
      .centerHorizontally()
      .centerVertically();

    const overlayConfig = new OverlayConfig({
      hasBackdrop: false,
      panelClass: 'popover-panel',
      positionStrategy,
    });

    this.overlayRef = this.overlay.create(overlayConfig);

    const feedbackFormPortal = new ComponentPortal(FeedbackPopoverComponent, this.viewContainerRef);
    const feedbackFormRef = this.overlayRef.attach(feedbackFormPortal);
    feedbackFormRef.instance.selectedRating = rating;
    feedbackFormRef.instance.answerData = answerData;
    feedbackFormRef.instance.contentid = contentid

    feedbackFormRef.instance.closePopup.subscribe(() => {
      this.overlayRef.dispose();
      this.overlayRef = null;
      answerData.rating = null;
    });

    feedbackFormRef.instance.submitRating.subscribe(submittedRating => {
      this.overlayRef.dispose();
      this.overlayRef = null;
      answerData.rating = submittedRating;
      answerData.feedbackSubmitted = true;
    });
  }
}
