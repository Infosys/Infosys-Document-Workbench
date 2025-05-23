/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Directive, ElementRef, Input, OnInit, AfterViewInit, OnChanges, SimpleChange } from '@angular/core';
import { fromEvent, Subscription } from 'rxjs';

@Directive({
  selector: '[niaAuthCheck]'
})
export class NiaAuthCheckDirective implements OnInit, AfterViewInit, OnChanges {
  private subscription = new Subscription();
  private ELEMENTS_WITHOUT_DISABLE_PROPERTY: Array<string> = ['span']

  constructor(public elementRef: ElementRef) { }

  @Input() niaAuthCheck = null;
  @Input() nacIsAuthorized = false;
  @Input() nacIsDisabled = false;

  ngOnInit() {

    const nativeElement = this.elementRef.nativeElement;
    const tagName = nativeElement.tagName.toLowerCase();
    const parent = this;
    // As elements like "span" don't have a "physical" "disabled" property, the below logic is to
    // intercept and cancel the click event if the element is "virtually" "disabled"
    if (this.ELEMENTS_WITHOUT_DISABLE_PROPERTY.includes(tagName)) {
      this.subscription = fromEvent(nativeElement.parentNode, 'click', { capture: true })
        .subscribe((e: any) => {
          if (e.target === nativeElement) {
            // if (parent.nacIsDisabled) { //Note: This logic is not working
            if (e.target.getAttribute('ng-reflect-nac-is-authorized')=='false' ||
               e.target.getAttribute('ng-reflect-nac-is-disabled')=='true') {

              e.stopPropagation()
            }
          }
        });
    }
    this.updateElement()
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.updateElement()
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    console.log('I have changed ', changes)
    this.updateElement()
  }



  private updateElement() {
    let nativeElement = this.elementRef.nativeElement;
    const tagName = nativeElement.tagName.toLowerCase();
    console.log('I am', tagName, ',disabled =', nativeElement.disabled )
    console.log('nacIsAuthorized =', this.nacIsAuthorized,',nacIsDisabled =', this.nacIsDisabled)

    // First and foremost, check if item is authorized for access by evaluating provided condition
    if (!this.nacIsAuthorized) {
      // If element has disabled property, then set it
      if (!this.ELEMENTS_WITHOUT_DISABLE_PROPERTY.includes(tagName)) {
        nativeElement.disabled = true
      }
      nativeElement.title = 'Restricted'
      nativeElement.style.cursor = 'not-allowed'
    } else {
      // If item is authorized, then set disabled status by evaluating provided condition
      if (!this.ELEMENTS_WITHOUT_DISABLE_PROPERTY.includes(tagName)) {
        nativeElement.disabled = this.nacIsDisabled
      }
      if (this.nacIsDisabled) {
        // nativeElement.classList.add('nia-cursor-not-allowed');
        nativeElement.style.cursor = 'not-allowed'
      } else {
        // nativeElement.classList.remove('nia-cursor-not-allowed');
        nativeElement.style.cursor = 'pointer'
      }
    }
  }
}
