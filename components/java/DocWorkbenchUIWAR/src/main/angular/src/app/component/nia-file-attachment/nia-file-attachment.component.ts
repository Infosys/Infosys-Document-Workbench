/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FileAttachment } from './file-attachment';
import { UtilityService } from '../../service/utility.service';

@Component({
  selector: 'app-nia-file-attachment',
  templateUrl: './nia-file-attachment.component.html',
  styleUrls: ['./nia-file-attachment.component.scss']
})

export class NiaFileAttachmentComponent implements OnInit {

  fileList: FileList[] = [];
  fileAttachments: FileAttachment[] = [];
  fileCount = 0;
  selectCount = 0;
  isFilesLengthExceeded = false;
  totalFileCount = 0;
  isFileSizeExceeded = false;
  fileNameOfSizeExceeded: string;
  @Output() attachmentFileList = new EventEmitter<FileList[]>();

  constructor(private utilityService: UtilityService) { }

  ngOnInit() {
  }

  onSelectFile(event) {
    const parent = this;
    const maxFileSize = 2; // 2MB size
    parent.isFileSizeExceeded = false;
    const fCount: number = event.target.files.length + parent.totalFileCount;

    if (event.target.files.length > 5 || parent.totalFileCount >= 5 || fCount > 5) {
      parent.isFilesLengthExceeded = true;
    } else {
      parent.isFilesLengthExceeded = false;

      for (let i = 0; i < event.target.files.length; i++) {
        const fileSize: number = event.target.files[i].size;

        const fileSizeinMB = fileSize / (1024 * 1000);
        const size = Math.round(fileSizeinMB * 100) / 100;  // convert upto 2 decimal place

        if (size > maxFileSize) {
          parent.isFileSizeExceeded = true;
          parent.fileNameOfSizeExceeded = event.target.files[i].name;
          break;
        }
      }

      if (!parent.isFileSizeExceeded) {
        parent.isFilesLengthExceeded = false;

        for (let i = 0; i < event.target.files.length; i++) {
          if (parent.totalFileCount < 5) {

            const fileDetails: FileAttachment = new FileAttachment('');

            parent.isFileSizeExceeded = false;
            parent.fileCount += 1;
            if (parent.fileList.length === 0) {
              parent.fileList[i] = event.target.files[i];
              parent.totalFileCount = parent.fileList.length;

            } else {
              if (parent.selectCount === 0) {
                parent.fileList[i] = event.target.files[i];
                parent.totalFileCount = parent.fileList.length;
              } else {
                parent.fileList[parent.fileList.length] = event.target.files[i];
                parent.totalFileCount = parent.fileList.length;
              }
            }

            fileDetails.fileName = event.target.files[i].name;
            fileDetails.fileRandomName = 'file' + parent.fileCount;
            parent.fileAttachments.push(fileDetails);
          } else {
            parent.isFilesLengthExceeded = true;
          }
        }
        parent.selectCount += 1;
        parent.attachmentFileList.emit(parent.fileList);
      }
      parent.attachmentFileList.emit(parent.fileList);
    }
  }

  removeAttachment(file, index) {
    const parent = this;
    parent.isFileSizeExceeded = false;

    if (parent.fileAttachments.filter(a => a.fileRandomName === file.fileRandomName).length > 0) {
      parent.fileAttachments = parent.fileAttachments.filter(a => a.fileRandomName !== file.fileRandomName);
      parent.fileList.splice(index, 1);
      parent.totalFileCount -= 1;

      if (parent.totalFileCount > 5) {
        parent.isFilesLengthExceeded = true;
      } else {
        parent.isFilesLengthExceeded = false;
      }

      parent.attachmentFileList.emit(parent.fileList);
    }
  }

  getMaskedFileName(fileName: string, limit: number) {
    const parent = this;
    return parent.utilityService.getTruncatedFileName(fileName, limit);
  }

}
