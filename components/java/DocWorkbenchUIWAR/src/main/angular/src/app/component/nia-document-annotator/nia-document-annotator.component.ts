/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import {
  Component, OnInit, Input, Output,
  EventEmitter, SimpleChange, OnDestroy, ChangeDetectorRef, ElementRef
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import * as annotator from 'annotator';
import { NiaAnnotatorUtil } from './nia-annotator-util';
@Component({
  selector: 'app-nia-document-annotator',
  templateUrl: './nia-document-annotator.component.html',
  styleUrls: ['./nia-document-annotator.component.scss']
})
export class NiaDocumentAnnotatorComponent implements OnInit, OnDestroy {

  @Input()
  set fileContent(fileContent: string) {
    this._fileContent = fileContent;
    this.isAnnotatorRunRequired = false;
  }
  @Input()
  set fileName(name: string) {
    this._fileName = name;
  }
  @Input()
  set fileViewer(type: string) {
    this.model.fileViewer = type;
  }
  @Input() 
  set renderPdfPage(page:string){
    this.model.renderPdfPage = page;
  }
  @Input() isAnnotationVisible: boolean;
  @Input() isAnnotatorReadOnly: boolean;
  @Input() inputAnnotationList;
  @Input() height: number;
  @Input() options;
  @Input() fileUrl: string;
  @Output() annotationChange = new EventEmitter<{}>();
  @Output() zoomFactorChange = new EventEmitter<{}>();
  @Output() textLayerRendered = new EventEmitter<{}>();
  @Output() pageRendered = new EventEmitter<{}>();
  @Output() pageChanged = new EventEmitter<{}>();
  
  private app = undefined;
  private inputAnnotations;
  private _fileContent;
  private _fileName = '';
  private _isAnnotationVisible: boolean;
  private isAnnotatorRunRequired: boolean;
  private range: {
    'start': '',
    'startOffset': '',
    'end': '',
    'endOffset': ''
  } = {
      'start': '',
      'startOffset': '',
      'end': '',
      'endOffset': ''
    };
  private isUrlChanged = false;
  model: any = {
    content: '',
    fileView: '',
    fileViewPdfIE: undefined,
    fileViewer: '',
    contentPdf: undefined,
    contentHtml: '',
    isViewFeatureAllowed:false,
    renderPdfPage:1
  }; // For binding to view

  constructor(private sanitizer: DomSanitizer, private niaAnnotatorUtil: NiaAnnotatorUtil, 
    private cd: ChangeDetectorRef, private elementRef: ElementRef) {
     }

  // tslint:disable-next-line:use-lifecycle-interface
  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    const parent = this;
    for (const propName in changes) {
      if (changes.hasOwnProperty(propName)) {
        switch (propName) {
          case 'fileViewer': {
            if (parent.isAnnotationVisible && parent.isAnnotatorRunRequired
              && !changes['fileViewer'].isFirstChange() && changes['isAnnotatorReadOnly'] === undefined
              && changes['inputAnnotationList'] === undefined && changes['isAnnotationVisible'] === undefined) {
              parent.runAnnotator();
            }
            break;
          }
          case 'fileContent': {
            if (changes['fileContent'].currentValue !== undefined && changes['fileContent'].currentValue !== null) {
              parent.loadFileContent();
            }
            break;
          }
          case 'isAnnotationVisible': {
            parent._isAnnotationVisible = changes['isAnnotationVisible'].currentValue;
            if (parent.isAnnotatorRunRequired && !changes['isAnnotationVisible'].isFirstChange()
              && changes['inputAnnotationList'] === undefined) {
              if (parent._isAnnotationVisible) {
                if (parent.app === undefined) {
                  parent.runAnnotator();
                }
              } else {
                parent.stopAnnotator();
              }
            }
            break;
          }
          case 'inputAnnotationList': {
            const annotations = changes['inputAnnotationList'].currentValue;
            if (annotations !== undefined && annotations !== null) {
              parent.inputAnnotations = parent.niaAnnotatorUtil.getDuplicateList(annotations);
              if (parent.isAnnotatorRunRequired) {
                parent.runAnnotator();
              }
            }
            break;
          }
          case 'isAnnotatorReadOnly': {
            if (parent.isAnnotatorRunRequired && !changes['isAnnotatorReadOnly'].isFirstChange()
              && parent.inputAnnotations !== undefined && parent.inputAnnotations !== null
              && changes['isAnnotationVisible'] === undefined) {
              parent.runAnnotator();
            }
            break;
          }
          case 'fileUrl': {
            if (parent.model.fileViewPdfIE !== undefined && changes['fileUrl'].currentValue !== changes['fileUrl'].previousValue) {
              this.isUrlChanged = true;
            } else {
              this.isUrlChanged = false;
            }
          }
        }
      }
    }
  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.stopAnnotator();
    annotator.util.$('#pdfContent').remove();
    annotator.util.$('.content').remove();
  }

  callbackStartAnnotator(event) {
    console.log('callbackStartAnnotator', event);
    if (event.numTextDivs > 0 && event.source.textContentItemsStr.join('').trim().length > 0) {
      event.source.textLayerDiv.innerHTML = this.niaAnnotatorUtil.cleanHtml(event.source.textLayerDiv.innerHTML);
      this.isAnnotatorRunRequired = true;
      this.runAnnotator();
    }
  }

  callbackTextLayerRendered(event){
    console.log('callbackTextLayerRendered', event);
    this.textLayerRendered.emit(event);
  }

  callbackZoomFactor(event){
    console.log('callbackZoomFactor', event);
    this.zoomFactorChange.emit(event);
  }

  callbackPageRendered(event){
    console.log('callbackPageRendered', event);
    this.pageRendered.emit(event);

  }
  callbackPageChanged(event){
    console.log('callbackPageChanged', event);
    this.pageChanged.emit(event);

  }

  private preProcessAmbiguousAnnotations() {
    if (this.niaAnnotatorUtil.getIfListPopulated(this.inputAnnotations)) {
      const savedAnnotationsWithoutRange = this.inputAnnotations.filter(ann => ann.op === undefined &&
        ((!this.niaAnnotatorUtil.getIfListPopulated(ann.ranges)) ||
          (ann.ranges[0].end === '' && ann.ranges[0].endOffset <= 0)));
      const savedAnnotationsWithRange =
        this.inputAnnotations.filter(ann => this.niaAnnotatorUtil.getIfListPopulated(ann.ranges));
      const annotationList = [];
      const selector = document.querySelectorAll(this.niaAnnotatorUtil.SELECTOR).length > 0 ? this.niaAnnotatorUtil.SELECTOR :
        this.niaAnnotatorUtil.getSelectorBasedOnViewer(this.model.fileViewer);
      savedAnnotationsWithoutRange.forEach(data => {
        if (data.quote.trim().length > 0) {
          const ranges = this.niaAnnotatorUtil.findTextRangeFromTextlayer(data.quote.trim(),
            selector,
            data.occurrenceNum !== undefined ? data.occurrenceNum : 0);
          if (ranges !== undefined) {
            for (let i = 0; i < ranges.length; i++) {
              const annData = {
                'quote': ranges[i]['quote'],
                'ranges': [ranges[i]],
                'text': data.text,
                'id': this.getCurrentCounterPos(this, annotationList),
                'createdByTypeCde': data.createdByTypeCde
              };
              delete ranges[i]['quote'];
              annotationList.push(annData);
            }
          }
        }
      });
      if (annotationList.length > 0) {
        this.inputAnnotations = annotationList;
        if (this.niaAnnotatorUtil.getIfListPopulated(savedAnnotationsWithRange)) {
          this.inputAnnotations = this.inputAnnotations.concat(savedAnnotationsWithRange);
        }
        this.annotationChange.emit([annotationList]);
      }
    }
  }

  private getDuplicates(annotationsList, annotation) {
    let duplicateList = [];
    duplicateList = annotationsList.filter(ann => {
      if (ann.text === annotation.text) {
        const valRangeData = this.range;
        valRangeData.start = ann.ranges[0].start;
        valRangeData.end = ann.ranges[0].end;
        valRangeData.startOffset = ann.ranges[0].startOffset;
        valRangeData.endOffset = ann.ranges[0].endOffset;
        const annRangeData = annotation.ranges[0];
        return JSON.stringify(valRangeData) === JSON.stringify(annRangeData);
      }
      return false;
    });
    return duplicateList;
  }

  private loadFileContent() {
    const parent = this;
    this.elementRef.nativeElement.style.setProperty('--pdf-viewer-height', parent.height-4 + 'vh')
    switch (parent.model.fileViewer) {
      case this.niaAnnotatorUtil.FILE_VIEWER_HTML: {
        parent._fileContent = parent.niaAnnotatorUtil.cleanHtml(parent._fileContent);
        parent.model.contentHtml = parent.sanitizer.bypassSecurityTrustHtml(parent._fileContent);
        break;
      }
      case this.niaAnnotatorUtil.FILE_VIEWER_EMBED: {
        const height = this.height;
        const innerHtml = `<object  style="width:100%;height:${height - 4}vh;" data="${parent._fileContent}"
        alt="${this._fileName}">`;
        parent.model.fileView = parent.sanitizer.bypassSecurityTrustHtml(innerHtml);
        break;
      }
      case this.niaAnnotatorUtil.FILE_VIEWER_PDF: {
        if ((parent.model.fileViewPdfIE === undefined || parent.isUrlChanged) && parent.fileUrl) {
          const innerHtml = `<object>
        <embed style="width:100%;height:${this.height - 5}vh;" src="${parent.fileUrl}" type="application/pdf" />
        </object>`;
          parent.model.fileViewPdfIE = parent.sanitizer.bypassSecurityTrustHtml(innerHtml);
        } else if ((parent.model.contentPdf === undefined || parent.model.contentPdf !== parent._fileContent) && parent._fileContent) {
          parent.model.contentPdf = parent._fileContent;
        }
        break;
      }
      default: {
        parent.model.content = parent._fileContent.replace(new RegExp(String.fromCharCode(13), 'g'), '');
        break;
      }
    }
    parent.cd.detectChanges();
    parent.isAnnotatorRunRequired = true;
  }

  private runAnnotator() {
    const parent = this;
    parent.stopAnnotator();
    const length = document.querySelectorAll(this.niaAnnotatorUtil.getSelectorBasedOnViewer(this.model.fileViewer)).length;
    if (parent._isAnnotationVisible && length > 0) {
      annotator.util.$('div').remove('.annotator-hide, .annotator-outer');
      annotator.util.$(document.querySelectorAll(
        parent.niaAnnotatorUtil.getSelectorBasedOnViewer(parent.model.fileViewer))[length - 1])[0].normalize();
      this.preProcessAmbiguousAnnotations();
      parent.app = new annotator.App();
      if (!parent.isAnnotatorReadOnly) {
        const niaAnnotator = function (options) {
          options = options || {};
          const NS = 'annotator-editor';
          const customUI = {
            interactionPoint: null,
            adder: null,
            editor: null,
            viewer: null,
            highlighter: null,
            textselector: null
          };
          const utility_methods = {
            hideAnnotatorEditorAdder: function () {
              // hide annotator editor window, adder button, and deselect text
              // whenever an image selection is drawn or adjusted
              // hide editor if visible
              const visible_editor = annotator.util.$('.annotator-editor:not(.annotator-hide)');
              if (visible_editor.length > 0) {
                visible_editor.addClass('annotator-hide');
              }
              // hide the adder whenever a new selection is started
              const visible_adder = annotator.util.$('.annotator-adder:not(.annotator-hide)');
              if (visible_adder.length > 0) {
                visible_adder.addClass('annotator-hide');
              }
            },
            modifyAnnotatorEditor: function () {
              const hidden_editor = annotator.util.$('.annotator-editor.annotator-hide.custom-editor');
              const visible_editor = annotator.util.$('.annotator-editor:not(.annotator-hide)');
              if (visible_editor.length > 0) {
                visible_editor.addClass('annotator-hide');
              }
              if (hidden_editor.length > 0) {
                hidden_editor[0].classList.remove('annotator-hide');
              }
              customUI.editor.show(customUI.interactionPoint);
              const textbox = annotator.util.$('#textbox');
              if (textbox.length > 0) {
                textbox[0].setAttribute('list', 'attributes');
                textbox[0].setAttribute('autoComplete', 'Off');
              }
              const selectOptions = parent.options;
              const dataList = annotator.util.$('#attributes');
              if (dataList.length > 0) {
                dataList.empty();
                selectOptions.forEach(opt => {
                  dataList.append(new Option(opt['name'], opt['name']));
                });
              }
              const annotatorResize = annotator.util.$('.custom-editor > form > ul > li > span.annotator-resize');
              if (annotatorResize.length > 0) {
                annotatorResize[0].outerHTML = '';
              }
            },
            id: function () {
              return parent.getCurrentCounterPos(parent, parent.inputAnnotations);
            },
            createAnnotation: function (annotation) {
              if (annotation.text.trim().length > 0) {
                annotation.id = utility_methods.id();
                annotation.op = 'A';
                customUI.highlighter.drawAll([annotation])
                  .then(function (data) {
                    data.forEach(elem => {
                      elem.classList.add('user-added');
                    });
                  });
                parent.annotationChange.emit(annotation);
              }
            }
          };
          return {
            start: function (app) {
              customUI.adder = new annotator.ui.adder.Adder({
                onCreate: function (ann) { }
              });
              customUI.adder.attach();
              customUI.viewer = new annotator.ui.viewer.Viewer({
                onDelete: function (ann) {
                  app.annotations['delete'](ann);
                },
                permitEdit: function (ann) {
                  const visible_viewer = annotator.util.$('.annotator-viewer:not(.custom-viewer)');
                  if (visible_viewer.length > 0) {
                    visible_viewer[0].parentNode.removeChild(visible_viewer[0]);
                  }
                  return false;
                },
                permitDelete: function (ann) {
                  const viewer = annotator.util.$('.annotator-viewer > ul.annotator-widget.annotator-listing');
                  if (viewer.length > 0) {
                    viewer.css('min-width', '120px');
                    if (ann.text.length > 50) {
                      viewer.css('height', '100px');
                      viewer.css('overflow', 'auto');
                    } else {
                      viewer.css('height', '');
                      viewer.css('overflow', '');
                    }
                  }
                  if (ann.op !== 'D') {
                    return true;
                  } else {
                    return false;
                  }
                },
                autoViewHighlights: options.element
              });
              customUI.viewer.element.addClass('custom-viewer');
              customUI.viewer.attach();
              customUI.highlighter = new annotator.ui.highlighter.Highlighter(options.element, {
                // The CSS class to apply to drawn highlights
                highlightClass: 'annotator-hl',
                // Number of annotations to draw at once
                chunkSize: 50,
                // Time (in ms) to pause between drawing chunks of annotations
                chunkDelay: 1
              });
              customUI.textselector = new annotator.ui.textselector.TextSelector(options.element, {
                onSelection: function (ranges, event) {
                  if (ranges.length > 0) {
                    let text = '';
                    ranges.forEach(range => {
                      text += range.text().trim();
                    });
                    customUI.interactionPoint = annotator.util.mousePosition(event);
                    if (text.length <= 0) {
                      utility_methods.hideAnnotatorEditorAdder();
                    }
                  }
                }
              });
              customUI.editor = new annotator.ui.editor.Editor({ defaultFields: false });
              const customField = customUI.editor.addField({
                type: 'input',
                id: 'textbox',
                label: 'Enter Name',
                load: function (field, ann) {
                  annotator.util.$(field).find('input').val(ann.text || '');
                },
                submit: function (field, annotation) {
                  annotation.text = annotator.util.$(field).find('input')[0].value;
                  utility_methods.createAnnotation(annotation);
                }
              });
              const input = customField.children[0];
              let innerhtml: string = input.outerHTML;
              innerhtml += '<datalist id="attributes"></datalist>';
              input.outerHTML = innerhtml;
              customUI.editor.element[0].classList.add('custom-editor');
              customUI.editor.attach();
            },
            highlightAnnotations: function (annotations) {
              return new Promise<void>(function (fulfilled, rejected) {
                annotations.forEach(annotation => {
                  const duplicates = parent.getDuplicates(parent.inputAnnotations, annotation);
                  if (parent.model.fileViewer === parent.niaAnnotatorUtil.FILE_VIEWER_TXT) {
                    annotation.ranges[0].start = '/pre[1]';
                    annotation.ranges[0].end = '/pre[1]';
                  } else {
                    annotation.ranges[0].start = '';
                    annotation.ranges[0].end = '';
                  }
                  customUI.highlighter.drawAll([annotation])
                    .then(function (data: any[]) {
                      if (parent.niaAnnotatorUtil.getIfListPopulated(duplicates) && duplicates.length > 1) {
                        data.forEach(element => element.classList.add(parent.niaAnnotatorUtil.DUPLICATE_ANN_ID_PREFIX + annotation.id));
                      }
                      if (data.length > 0) {
                        const elem = data[data.length - 1];
                        elem.id = annotation.text;
                        if (elem.nextSibling === null || (elem.nextSibling !== null && elem.nextSibling.className !== 'sup')) {
                          const newSpan = document.createElement('span');
                          newSpan.className = 'sup';
                          newSpan.id = elem.id;
                          elem.insertAdjacentElement('afterend', newSpan);
                        }
                      }
                    });
                });
                fulfilled();
              });
            },
            highlightUserAnnotations: function (annotations) {
              const addedAnnotations = annotations.filter(ann => ann.op === 'A');
              const editedAnnotations = annotations.filter(ann => ann.op === 'E');
              const deletedAnnotations = annotations.filter(ann => ann.op === 'D');
              const annotationList = [deletedAnnotations, addedAnnotations, editedAnnotations];
              return new Promise<void>(function (fulfilled, rejected) {
                annotationList.forEach(annList => {
                  annList.forEach(annotation => {
                    const duplicates = parent.getDuplicates(parent.inputAnnotations, annotation);
                    if (parent.model.fileViewer === parent.niaAnnotatorUtil.FILE_VIEWER_TXT) {
                      annotation.ranges[0].start = '/pre[1]';
                      annotation.ranges[0].end = '/pre[1]';
                    } else {
                      annotation.ranges[0].start = '';
                      annotation.ranges[0].end = '';
                    }
                    customUI.highlighter.drawAll([annotation])
                      .then(function (data) {
                        data.forEach((elem) => {
                          if (annotation.op === 'A') {
                            elem.classList.add('user-added');
                          } else if (annotation.op === 'D') {
                            elem.classList.add('user-deleted');
                          } else {
                            elem.classList.add('user-edited');
                          }
                          if (parent.niaAnnotatorUtil.getIfListPopulated(duplicates) && duplicates.length > 1) {
                            elem.classList.add(parent.niaAnnotatorUtil.DUPLICATE_ANN_ID_PREFIX + annotation.id);
                          }
                        });
                        if (data.length > 0) {
                          const element = data[data.length - 1];
                          element.id = annotation.text;
                          if (element.nextSibling === null || (element.nextSibling !== null && element.nextSibling.className !== 'sup')) {
                            const newSpan = document.createElement('span');
                            newSpan.className = 'sup';
                            newSpan.id = element.id;
                            element.insertAdjacentElement('afterend', newSpan);
                          }
                        }
                      });
                  });
                });
                fulfilled();
              });
            },
            destroy: function () {
              customUI.textselector.destroy();
              customUI.adder.destroy();
              customUI.highlighter.destroy();
              customUI.editor.destroy();
              customUI.viewer.destroy();
              annotator.util.$(options.element)[0].normalize();
            },
            beforeAnnotationCreated: function (annotation) {
              utility_methods.modifyAnnotatorEditor();
              return customUI.editor.load(annotation, customUI.interactionPoint);
            },
            beforeAnnotationUpdated: function (annotation) {
              utility_methods.hideAnnotatorEditorAdder();
            },
            annotationDeleted: function (annotation) {
              if (annotation.op !== 'A') {
                customUI.highlighter.drawAll([annotation])
                  .then(function (data) {
                    data.forEach(elem => {
                      elem.classList.add('user-deleted');
                    });
                  });
              }
              annotation.op = 'D';
              delete annotation.index;
              parent.annotationChange.emit(annotation);
            }
          };
        };
        parent.app.include(annotator.ui.main, {
          element: document.querySelectorAll(parent.niaAnnotatorUtil.getSelectorBasedOnViewer(parent.model.fileViewer))[length - 1],
        })
          .include(niaAnnotator, {
            element: document.querySelectorAll(parent.niaAnnotatorUtil.getSelectorBasedOnViewer(parent.model.fileViewer))[length - 1],
          });
      } else {
        parent.app.include(annotator.ui.main, {
          element: document.querySelectorAll(parent.niaAnnotatorUtil.getSelectorBasedOnViewer(parent.model.fileViewer))[length - 1],
        })
          .include(parent.niaAnnotatorReadOnlyViewer, {
            element: document.querySelectorAll(parent.niaAnnotatorUtil.getSelectorBasedOnViewer(parent.model.fileViewer))[length - 1],
            this: parent,
          });
      }
      parent.app
        .start()
        .then(function () {
          parent.highlightAnnotations().then((fullfilled) => {
            parent.addNumberNotation();
          });
        });
    }
  }

  private highlightAnnotations() {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if (parent.niaAnnotatorUtil.getIfListPopulated(parent.inputAnnotations)) {
        const unsavedUserAnnotations = parent.inputAnnotations.filter(ann => ann.op !== undefined);
        const savedAnnotations = parent.inputAnnotations.filter(ann => ann.op === undefined);
        const promiseAll = [];
        if (savedAnnotations.length > 0) {
          promiseAll.push(parent.app.runHook('highlightAnnotations',
            [parent.niaAnnotatorUtil.getDuplicateList(savedAnnotations)]));
        }
        if (unsavedUserAnnotations.length > 0) {
          promiseAll.push(parent.app.runHook('highlightUserAnnotations',
            [parent.niaAnnotatorUtil.getDuplicateList(unsavedUserAnnotations)]));
        }
        Promise.all(promiseAll).then(function (data) {
          fulfilled();
        });
      }
    });
  }

  private addNumberNotation() {
    const $ = annotator.util.$;
    const annotations: any[] = $('.annotator-hl:not(.annotator-hl:not([id]))');
    if (annotations.length > 0) {
      const annotationMap = this.niaAnnotatorUtil.getMappedAnnotations(annotations, 'id');
      let index = 0;
      annotationMap.forEach((value, key) => {
        index = index + 1;
        this.assignAnnIndexBasedOnQuote(value, index, key);
      });
      this.annotationChange.emit([this.inputAnnotations]);
    }
  }

  private assignAnnIndexBasedOnQuote(annotations: any[], index, key) {
    const annotationList = this.removeDeletedAnnsIfEdit(annotations);
    const annotationMap = this.niaAnnotatorUtil.getMappedAnnotations(annotationList, 'innerText');
    let quoteIndex = 0;
    annotationMap.forEach((value, quoteKey) => {
      // let isDuplicateExists = false;
      const indexArray = [];
      // const duplicates = value.filter(val => {
      //   const classList: any[] = Array.from(val.classList);
      //   if (classList.filter(className => className.startsWith(this.niaAnnotatorUtil.DUPLICATE_ANN_ID_PREFIX)).length > 0) {
      //     return true;
      //   }
      //   return false;
      // });
      // if (this.niaAnnotatorUtil.getIfListPopulated(duplicates)) {
      //   isDuplicateExists = true;
      // }
      let indexStr = '';
      // if (isDuplicateExists) {
      //   let idList = [];
      //   duplicates.forEach(ann => {
      //     const id: string = ann.classList[ann.classList.length - 1];
      //     idList.push(id.substring(this.niaAnnotatorUtil.DUPLICATE_ANN_ID_PREFIX.length));
      //   });
      //   idList = idList.sort();
      //   idList.forEach(id => {
      //     quoteIndex++;
      //     indexArray.push({ id: id, subIndex: quoteIndex });
      //     indexStr += index + '.' + quoteIndex + ', ';
      //   });
      //   indexStr = indexStr.substring(0, indexStr.length - 2);
      // } else {
      quoteIndex++;
      indexStr = annotationMap.size > 1 ? index + '.' + (quoteIndex) : index;
      // }
      this.inputAnnotations.forEach(annotation => {
        if (annotation.text.toLowerCase() === key && (this.model.fileViewer ===
          this.niaAnnotatorUtil.FILE_VIEWER_PDF || this.niaAnnotatorUtil.getIfStringsMatch(annotation.quote.trim(), quoteKey.trim())
          || annotation.quote.toLowerCase().trim().endsWith(quoteKey.trim()))) {
          // if (isDuplicateExists) {
          //   const id = indexArray.filter(ind => ind.id === annotation.id);
          //   const subIndex = id.length > 0 ? id[0].subIndex : 0;
          //   const i = index + '.' + subIndex;
          //   annotation.index = i;
          // } else {
          annotation.index = indexStr;
          // }
        }
      });
      value.forEach((ann) => {
        if (ann.nextSibling !== null) {
          ann.nextSibling.setAttribute('data-index', indexStr);
        }
      });
    });
  }
  private removeDeletedAnnsIfEdit(annotations: any[]) {
    let annotationList = annotations;
    const editedAnns = annotations.filter(ann => {
      const classList: string[] = Array.from(ann.classList);
      return classList.includes('user-edited');
    });
    if (this.niaAnnotatorUtil.getIfListPopulated(editedAnns)) {
      annotationList = annotations.filter(ann => {
        const classList: string[] = Array.from(ann.classList);
        return !classList.includes('user-deleted');
      });
    }
    return annotationList;
  }

  private stopAnnotator() {
    const parent = this;
    if (parent.app !== undefined) {
      parent.app.destroy();
      parent.app = undefined;
      annotator.util.$('div').remove('.annotator-hide, .annotator-outer');
      annotator.util.$('span.sup').remove();
    }
  }

  private niaAnnotatorReadOnlyViewer = function (options) {
    options = options || {};
    const customUI = {
      interactionPoint: null,
      viewer: null,
      highlighter: null,
      textselector: null
    };
    const utility_methods = {
      hideAnnotatorEditorAdder: function () {
        // hide annotator editor window, adder button, and deselect text
        // whenever an image selection is drawn or adjusted
        // hide editor if visible
        const visible_editor = annotator.util.$('.annotator-editor:not(.annotator-hide)');
        if (visible_editor.length > 0) {
          visible_editor.addClass('annotator-hide');
        }
        // hide the adder whenever a new selection is started
        const visible_adder = annotator.util.$('.annotator-adder:not(.annotator-hide)');
        if (visible_adder.length > 0) {
          const addButton = visible_adder[0].children;
          if (addButton.length > 0) {
            addButton[0].style.cursor = 'not-allowed';
            addButton[0].style.color = 'red';
            addButton[0].setAttribute('disabled', true);
          }
        }
      }
    };
    return {
      start: function (app) {
        customUI.viewer = new annotator.ui.viewer.Viewer({
          permitEdit: function (ann) {
            const visible_viewer = annotator.util.$('.annotator-viewer:not(.custom-viewer)');
            if (visible_viewer.length > 0) {
              visible_viewer[0].parentNode.removeChild(visible_viewer[0]);
            }
            return false;
          },
          permitDelete: function (ann) {
            const viewer = annotator.util.$('.annotator-viewer > ul.annotator-widget.annotator-listing');
            if (viewer.length > 0) {
              viewer.css('min-width', '120px');
              viewer.css('cursor', 'not-allowed');
              viewer.css('color', 'red');
              if (ann.text.length > 50) {
                viewer.css('height', '100px');
                viewer.css('overflow', 'auto');
              } else {
                viewer.css('height', '');
                viewer.css('overflow', '');
              }
            }
            return false;
          },
          autoViewHighlights: options.element
        });
        customUI.viewer.element.addClass('custom-viewer');
        customUI.viewer.element.addClass('disabled');
        customUI.viewer.attach();
        customUI.highlighter = new annotator.ui.highlighter.Highlighter(options.element, {
          // The CSS class to apply to drawn highlights
          highlightClass: 'annotator-hl',
          // Number of annotations to draw at once
          chunkSize: 50,
          // Time (in ms) to pause between drawing chunks of annotations
          chunkDelay: 1
        });
        customUI.textselector = new annotator.ui.textselector.TextSelector(options.element, {
          onSelection: function (ranges, event) {
            if (ranges.length > 0) {
              customUI.interactionPoint = annotator.util.mousePosition(event);
              utility_methods.hideAnnotatorEditorAdder();
            }
          }
        });
      },
      highlightAnnotations: function (annotations) {
        return new Promise<void>(function (fulfilled, rejected) {
          annotations.forEach(annotation => {
            if (options.this.model.fileViewer === options.this.niaAnnotatorUtil.FILE_VIEWER_TXT) {
              annotation.ranges[0].start = '/pre[1]';
              annotation.ranges[0].end = '/pre[1]';
            } else {
              annotation.ranges[0].start = '';
              annotation.ranges[0].end = '';
            }
            customUI.highlighter.drawAll([annotation])
              .then(function (data: any[]) {
                if (data.length > 0) {
                  const elem = data[data.length - 1];
                  elem.id = annotation.text;
                  if (elem.nextSibling === null || (elem.nextSibling !== null && elem.nextSibling.className !== 'sup')) {
                    const newSpan = document.createElement('span');
                    newSpan.className = 'sup';
                    newSpan.id = elem.id;
                    elem.insertAdjacentElement('afterend', newSpan);
                  }
                }
              });
          });
          fulfilled();
        });
      },
      highlightUserAnnotations: function (annotations) {
        const addedAnnotations = annotations.filter(ann => ann.op === 'A');
        const editedAnnotations = annotations.filter(ann => ann.op === 'E');
        const deletedAnnotations = annotations.filter(ann => ann.op === 'D');
        const annotationList = [addedAnnotations, editedAnnotations, deletedAnnotations];
        return new Promise<void>(function (fulfilled, rejected) {
          annotationList.forEach(annList => {
            annList.forEach(annotation => {
              if (options.this.model.fileViewer === options.this.niaAnnotatorUtil.FILE_VIEWER_TXT) {
                annotation.ranges[0].start = '/pre[1]';
                annotation.ranges[0].end = '/pre[1]';
              } else {
                annotation.ranges[0].start = '';
                annotation.ranges[0].end = '';
              }
              customUI.highlighter.drawAll([annotation])
                .then(function (data) {
                  data.forEach((elem) => {
                    if (annList.filter(ann => ann.op === 'A').length > 0) {
                      elem.classList.add('user-added');
                    } else if (annList.filter(ann => ann.op === 'D').length > 0) {
                      elem.classList.add('user-deleted');
                    } else {
                      elem.classList.add('user-edited');
                    }
                  });
                  if (data.length > 0) {
                    const element = data[data.length - 1];
                    element.id = annotation.text;
                    if (element.nextSibling === null || (element.nextSibling !== null && element.nextSibling.className !== 'sup')) {
                      const newSpan = document.createElement('span');
                      newSpan.className = 'sup';
                      newSpan.id = element.id;
                      element.insertAdjacentElement('afterend', newSpan);
                    }
                  }
                });
            });
          });
          fulfilled();
        });
      },
      destroy: function () {
        customUI.textselector.destroy();
        customUI.highlighter.destroy();
        customUI.viewer.destroy();
        annotator.util.$(options.element)[0].normalize();
      }
    };
  };

  private getCurrentCounterPos(parent: this, list) {
    let counter;
    counter = 1;
    if (parent.niaAnnotatorUtil.getIfListPopulated(list)) {
      counter += list[list.length - 1].id;
    }
    return counter;
  }

  /***
  * Reason : For future development purpose
  * Desc   : Folowing block of does pdf file loading to dom, enabling text layer on top of it to do annotation using annotator.js.
  * Infy git Issue : #230
  * Date: Feb/6/2020
  * Commented By: user
  *
  * 1. *** ng2-pdfjs-viewer ***
  * Desc : Follow given instructions from https://www.npmjs.com/package/ng2-pdfjs-viewer
  * Required html div : Enable follwing line in "nia-document-annotator.component.html"
  *         <div class="content" *ngIf="viewer==='Ng2PDFJSViewer'" id="textContent">
  *               <ng2-pdfjs-viewer pdfSrc="/assets/test.pdf" ></ng2-pdfjs-viewer>
  *           </div>
  *
  * 2. *** ngx-extended-pdf-viewer ***
  * Desc : Follow given instructions from https://www.npmjs.com/package/ngx-extended-pdf-viewer
  * Vesrion required : Angular v8
  *
  * 3. *** pdf.js ***
  * Method : loadPDFDataMain()
  * Required Imports :
  *          import PDFJS = require('pdfjs-dist');
  *          import * as PDFJS from 'pdfjs-dist/build/pdf';
  *          PDFJS.GlobalWorkerOptions.workerSrc = './../../../assets/vendor/pdfjs/build/pdf.worker.js';
  * Required html div : Enable follwing line in "nia-document-annotator.component.html"
  *            <div class="content" *ngIf="viewer==='PDFJS'" id="textContent">
  *                <div id="pdfcanvas" [style.height.vh]="height-10" class="pdfViewer"></div>
  *            </div>
  */
  /*
     private loadPDFDataMain() {
       const loadingTask = PDFJS.getDocument(this.model.content);
       loadingTask.promise.then(function (pdfDocument) {
         const container = document.getElementById('pdfcanvas');
         const numPages = pdfDocument.numPages;
         let lastPromise;
         lastPromise = pdfDocument.getMetadata().then(function (data) { });
         const loadPage = function (pageNum) {
           return pdfDocument.getPage(pageNum).then(function (pdfPage) {
             const viewport = pdfPage.getViewport({ scale: 1.0 });
             const div = document.createElement('div');
             div.setAttribute('id', 'page-' + (pdfPage.pageIndex + 1));
             div.setAttribute('style', 'position: relative; overflow-y: auto; max-height: 46.4756vh');
             container.appendChild(div);
             const canvas = <HTMLCanvasElement>document.createElement('canvas');
             div.appendChild(canvas);
             canvas.width = viewport.width;
             canvas.height = viewport.height;
             const ctx = canvas.getContext('2d');
             const renderContext = {
               canvasContext: ctx,
               viewport: viewport
             };
             const renderTask = pdfPage.render(renderContext).then(function () {
               return pdfPage.getTextContent();
             }).then(function (textContent) {
               const textLayerDiv = document.createElement('div');
               textLayerDiv.setAttribute('class', 'textLayer');
               div.appendChild(textLayerDiv);
               const textLayer = PDFJS.renderTextLayer({
                 textContent: textContent,
                 container: textLayerDiv,
                 viewport: viewport
               });
               textLayer._render();
             });
             return renderTask.promise;
           });
         };
         for (let i = 1; i <= numPages; i++) {
           lastPromise = lastPromise.then(loadPage.bind(null, i));
         }
         return lastPromise;
       });
     }*/

}
