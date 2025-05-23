/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */
 
import { Component, OnInit, Output, EventEmitter, Renderer2, ElementRef, ViewChild } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';

@Component({
  selector: 'app-feedback-popover',
  templateUrl: './feedback-popover.component.html',
  styleUrls: ['./feedback-popover.component.scss']
})
export class FeedbackPopoverComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "QueAndAnsComponent";
  }
  @Output() closePopup = new EventEmitter<void>();
  @Output() submitRating = new EventEmitter<number>();
  @ViewChild('dragHandle', { static: true }) dragHandle: ElementRef;
  dragStart = { x: 0, y: 0 };
  dragging = false;
  minimized = false;
  form: FormGroup;
  selectedRating: number;
  answerData: any;
  commenttxt: string = "";
  contentid: string;

  constructor(private renderer: Renderer2, private el: ElementRef, public niaTelemetryService: NiaTelemetryService, public sessionService: SessionService, public configDataHelper: ConfigDataHelper,) { super(sessionService, configDataHelper, niaTelemetryService) }

  model = {
    tooltips: ['Incorrect', 'Somewhat Correct', 'Correct'],
  }


  ngOnInit(): void {
    this.initializeForm();
    this.setupRatingWatcher();
    this.renderer.listen(this.dragHandle.nativeElement, 'mousedown', this.onMouseDown.bind(this));
    this.renderer.listen('document', 'mousemove', this.onMouseMove.bind(this));
    this.renderer.listen('document', 'mouseup', this.onMouseUp.bind(this));
    console.log("conid", this.contentid)
  }

  initializeForm(): void {
    if (this.selectedRating === 3) {
      this.form = new FormGroup({
        rating: new FormControl(this.selectedRating),
        expectedAnswer: new FormControl({ value: '', disabled: true }),
        pageNo: new FormControl({ value: '', disabled: true }),
        comment: new FormControl(''),
      });
    } else {
      this.form = new FormGroup({
        rating: new FormControl(this.selectedRating),
        expectedAnswer: new FormControl(this.answerData ? this.answerData.answer : '', { validators: [this.notEmpty], updateOn: 'change' }),
        pageNo: new FormControl(this.answerData ? this.answerData.page_num : '', { validators: [this.notEmpty], updateOn: 'change' }),
        comment: new FormControl(''),
      });
    }
  }

  notEmpty(control: FormControl) {
    if (control.value === '') {
      return { empty: true };
    }
    return null;
  }

  setupRatingWatcher(): void {
    this.form.get('rating').valueChanges.subscribe(value => {
      if (value === 3) {
        this.form.get('expectedAnswer').disable();
        this.form.get('pageNo').disable();
        this.form.get('expectedAnswer').clearValidators();
        this.form.get('pageNo').clearValidators();
        this.form.get('expectedAnswer').setValue('');
        this.form.get('pageNo').setValue('');
      } else {
        this.form.get('expectedAnswer').enable();
        this.form.get('pageNo').enable();
        this.form.get('expectedAnswer').setValidators(Validators.required);
        this.form.get('pageNo').setValidators(Validators.required);
        if (!this.form.get('expectedAnswer').dirty) {
          this.form.get('expectedAnswer').setValue(this.answerData ? this.answerData.answer : '');
        }
        if (!this.form.get('pageNo').dirty) {
          this.form.get('pageNo').setValue(this.answerData ? this.answerData.page_num : '');
        }
      }
      this.form.get('expectedAnswer').updateValueAndValidity();
      this.form.get('pageNo').updateValueAndValidity();
    });
  }

  pageInput(event): void {
    const newValue = event.target.value.replace(/[^0-9,-]/g, '');
    event.target.value = newValue;
    this.form.get('pageNo').setValue(newValue, { emitEvent: false });
  }

  onRatingClick(star: number) {
    if (this.form.value.rating === star) {
      this.form.controls.rating.setValue(Math.max(star - 1, 1));
    } else {
      this.form.controls.rating.setValue(star);
    }
  }

  onSubmit(): void {
    const parent = this;
    console.log(this.form.value);
    this.submitRating.emit(this.form.value.rating);
    console.log('Rating:', this.form.value.rating);

    // console.log('Expected Answer:', this.form.value.expectedAnswer);
    // console.log('Page No:', this.form.value.pageNo);
    // console.log('Comments:', this.form.value.comment);

    this.commenttxt = `expected_answer=${this.form.value.expectedAnswer}\npage_num=${this.form.value.pageNo}\nuser_comment=${this.form.value.comment}`;
    console.log(this.commenttxt);

    parent.triggerTelemetryEvents(parent.bmodel.TELE_EVENTS.FEEDBACK, '', [], null, [], this.form.value.rating, this.commenttxt, this.contentid);
  }

  onCloseClick() {
    this.form.reset({
      rating: 0,
      expectedAnswer: '',
      pageNo: '',
      comment: '',
    });
    this.closePopup.emit();
  }

  resetExpectedAnswer(): void {
    this.form.get('expectedAnswer').setValue(this.answerData ? this.answerData.answer : '');
  }

  // starHovered = 0;

  // onStarHover(star: number): void {
  //   this.starHovered = star;
  // }

  // onStarLeave(): void {
  //   this.starHovered = 0;
  // }

  onMouseDown(event: MouseEvent) {
    const style = getComputedStyle(this.dragHandle.nativeElement);
    this.dragStart = {
      x: event.clientX - parseInt(style.left, 10),
      y: event.clientY - parseInt(style.top, 10)
    };
    this.dragging = true;
  }

  onMouseMove(event: MouseEvent) {
    if (this.dragStart && this.dragging) {
      this.dragHandle.nativeElement.style.left = `${event.clientX - this.dragStart.x}px`;
      this.dragHandle.nativeElement.style.top = `${event.clientY - this.dragStart.y}px`;
    }
  }

  onMouseUp() {
    this.dragStart = null;
    this.dragging = false;
  }

  onMinimizeClick() {
    this.minimized = true;
  }

  onRestoreClick() {
    this.minimized = false;
  }

}
