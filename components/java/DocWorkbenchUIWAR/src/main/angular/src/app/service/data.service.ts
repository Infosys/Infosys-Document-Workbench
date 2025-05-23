/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AttachmentData } from './../data/attachment-data';

@Injectable()
export class DataService {

  constructor() { }

  // For pub-sub on selected queue and subqueue in side bar
  private bsSelectedQueueNameCde: BehaviorSubject<object> = new BehaviorSubject<object>(null);
  // To subscribe or receive selected queue and subqueue in side bar
  selectedQueueNameCde = this.bsSelectedQueueNameCde.asObservable();
  // For pub-sub on action added from add action page
  private bsDocActionAdded: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  // To subscribe or receive action added from add action page
  public docActionAddedEvent = this.bsDocActionAdded.asObservable();
  // For pub-sub on action added from re-extracted popup
  private bsReExtractAction: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  // To subscribe or receive action added from re-extracted popup
  public reExtractActionCompletedEvent = this.bsReExtractAction.asObservable();
  // For pub-sub on annotation added from file-content to extracted-data
  private bsAnnotation: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  // To subscribe or receive annotation added from file-content to extracted-data
  public annotationOpData = this.bsAnnotation.asObservable();
  // For pub-sub on attribute added or deleted from extracted-data to file-content
  private bsAttribute: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  // To subscribe or receive attribute added or deleted from extracted-data to file-content
  public attributeOpData = this.bsAttribute.asObservable();
  // For pub-sub on attribute saved or cancelled from extracted-data to file-content
  private bsExtractedDataCustomOp: BehaviorSubject<number> = new BehaviorSubject<number>(null);
  // To subscribe or receive attribute saved or cancelled from extracted-data to file-content
  public extractedDataCustomEvent = this.bsExtractedDataCustomOp.asObservable();
  // For pub-sub on annotationList added from file-content to extracted-data
  private bsAnnotationIndexList: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  // To subscribe or receive annotationList added from file-content to extracted-data
  public annotationIndexList = this.bsAnnotationIndexList.asObservable();

  private bsPostToEDComponentEvent: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  public postToEDComponent = this.bsPostToEDComponentEvent.asObservable();

  // For pub-sub on selected attachment data based on attachment url.
  private bsSelectedAttachmentData: BehaviorSubject<AttachmentData> = new BehaviorSubject<AttachmentData>(null);
  // To subscribe or receive selected attachment data based on attachment url.
  selectedAttachmentData = this.bsSelectedAttachmentData.asObservable();

  // To publish or broadcast selected queue and subqueue in side bar
  publishQueueNameCde(queueNameCde: number, subQueueNameCde: number) {
    this.bsSelectedQueueNameCde.next({ queueNameCde, subQueueNameCde });
  }

  // To publish or broadcast action added from add action page
  public publishDocActionAddedEvent(newmessage: boolean) {
    this.bsDocActionAdded.next(newmessage);
  }

  // To publish or broadcast action added from re-extracted popup
  public publishReExtractActionCompleteEvent(newmessage: boolean) {
    this.bsReExtractAction.next(newmessage);
  }

  // To publish or broadcast attributeDataList added from file-content to extracted-data
  public publishAnnotationOpData(annotation: any) {
    this.bsAnnotation.next(annotation);
  }

  // To publish or broadcast attribute added or deleted from extracted-data to file-content
  public publishAttributeOpData(attribute: any) {
    this.bsAttribute.next(attribute);
  }

  // To publish or broadcast attribute saved or cancelled from extracted-data to file-content
  public publishExtractedDataCustomEvent(eventType: number) {
    this.bsExtractedDataCustomOp.next(eventType);
  }

  // To publish or broadcast annotationDataList added from file-content to extracted-data
  public publishAnnotationIndexList(annotations: any) {
    this.bsAnnotationIndexList.next(annotations);
  }

  public publishToEDComponentEvent(data: any) {
    this.bsPostToEDComponentEvent.next(data);
  }

  public publishSelectedAttachmentDataEvent(attachmentData: AttachmentData) {
    this.bsSelectedAttachmentData.next(attachmentData);
  }

}
