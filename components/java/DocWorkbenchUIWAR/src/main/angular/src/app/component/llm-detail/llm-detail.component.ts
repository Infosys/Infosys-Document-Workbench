/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

 
 import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
 import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
 import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
 import { ClipboardModule } from '@angular/cdk/clipboard';
 import { ToastrService } from 'ngx-toastr';
 
 @Component({
   selector: 'app-llm-detail',
   templateUrl: './llm-detail.component.html',
   styleUrls: ['./llm-detail.component.scss']
 })
 
 export class LlmDetailComponent implements OnInit {
   @Input('minheight') minheight: number;
   @Output() close = new EventEmitter<string>();
   @Input('closeButton') closeButton: boolean;
   @Input('answerData') answerData:any;
   closeResult: string;
   ngbModalOptions: NgbModalOptions = {
     backdrop: 'static',
     keyboard: false
   };
 
   constructor(protected sanitizer: DomSanitizer, 
        private modalService: NgbModal,private toastr: ToastrService) { }
 
   model: any = {
     llmResponse: "",
    copyPrompt:undefined
   }; 
   private llmPrompt=""
   private formattedTemplate=""
   
   ngOnInit() {
     if (this.answerData['doc_id']) {
       try {
        // Convert the object to a JSON-formatted string with indentation (e.g., 2 spaces)
        const parsedJson = JSON.parse(this.answerData['llm_response']['response']);
        this.model.llmResponse = JSON.stringify(parsedJson, null, 4);       
      } catch (error) {
        this.model.llmResponse = this.answerData['llm_response']['response'];
      }
       this.llmPrompt = this.answerData['llm_prompt'];
     } 
   }
  
    getFormattedPrompt(): SafeHtml {
      this.formattedTemplate = this.llmPrompt["prompt_template"]
        .replace('{context}', `<span style="color: blue;">${this.llmPrompt['context']}</span>`)
        .replace('{question}', `<span style="color: green;">${this.llmPrompt['query']}</span>`);
        this.formattedTemplate=this.formattedTemplate .replace(/(\r\n|\n|\r)/gm,"<br/>");
      // Use DomSanitizer to mark the HTML as safe
      this.model.copyPrompt=this.llmPrompt["prompt_template"]
      .replace('{context}', this.llmPrompt['context'])
      .replace('{question}', this.llmPrompt['query']);
      return this.sanitizer.bypassSecurityTrustHtml(this.formattedTemplate);
    }
    onCopyButtonClickMsg(){
      this.toastr.success("Copied to clipboard! ");
    }
   
   open(content) {
     this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
       this.closeResult = `Closed with: ${result}`;
     }, (reason) => {
       this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
     });
   }
 
   modalWindow() {
     this.close.emit('window closed');
     this.closeButton = false;
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
 
  
 
 }
 