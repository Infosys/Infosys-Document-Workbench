/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, ElementRef, HostListener } from '@angular/core';
import { ModalDismissReasons, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { QueueData } from '../../data/queue-data';
import { UserData } from '../../data/user-data';
import { ValData } from '../../data/val-data';
import { AdminService } from '../../service/admin.service';
import { MessageInfo } from '../../utils/message-info';
import { ValService } from '../../service/val.service';
import { AuditData } from '../../data/audit-data';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';


@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "UserListComponent";
  }
  @Input() maxAllowedHeightInPx: number;
  @Input() maxAllowedWidthInPx: number;

  constructor(private modalService: NgbModal, private adminService: AdminService,
    private valService: ValService, private toastr: ToastrService,
    private msgInfo: MessageInfo, public sessionService: SessionService,
    private elementRef: ElementRef, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService)
     }
  private operation: string;
  private addedQueues: number[] = [];
  private removedQueues: number[] = [];
  private userDataEditList: UserData[] = [];
  private isRoleUpdated = false;
  private isEnableUpdated = false;
  private isQueueAddUpdated = false;
  private isQueueDeleteUpdated = false;
  conenteHeightInPx: Number;
  userDataList: UserData[] = [];
  roleDataList: ValData[] = [];
  queueDataList: QueueData[] = [];
  userData: UserData;
  isEnabledEditable = false;
  isRoleEditable = false;
  isQueueEditable = false;
  isSaveAllowed = false;
  isCancelAllowed = false;
  isRowSelectAllowed: boolean;
  selectedRow = 0;
  accountEnabled: boolean;
  isDataLoaded = false;
  auditData: AuditData;
  isCancel = false;
  isCancelModal = false;
  closeResult: string;

  toggleShowUserQueueList(userData: UserData,) {
    userData.uiShowQueueList = !userData.uiShowQueueList
  }

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  ngOnInit() {
    this.isDataLoaded = false;
    this.getUserList();
    this.getQueueList();
    this.getRoleList();

  }

  ngAfterContentInit() {
    // This method will be called only once in component lifecycle, NOT called on page resize.
    this.updateProperties();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // This method will be called only on page resize and NOT on component load.
    this.updateProperties();
  }

  private updateProperties() {
    this.conenteHeightInPx = this.maxAllowedHeightInPx - 50; // Calculated using trial-error
    // This is the technique to update variables in SCSS
    const m = getComputedStyle(document.querySelector(':root')).getPropertyValue('--docwb-main-content-width')
    console.log('user-list->updateProperties()', m)
    this.elementRef.nativeElement.style.setProperty('--user-table-width', m + 'px')

  }

  cancelModal() {
    this.isCancelAllowed = true;
    this.isCancel = true;
    this.isCancelModal = true;
    this.refreshComponent();
  }

  getUserList() {
    const parent = this;
    const promise = parent.adminService.getUserListPromise();
    return promise.then(function (value) {
      const userDataList: UserData[] = value as UserData[];
      parent.userDataList = userDataList;
      parent.isDataLoaded = true;
    }).catch(function (error) {
      parent.isDataLoaded = true;
    });
  }


  getRoleList() {
    const parent = this;
    parent.valService.getValList('role-type', function (error, data) {
      if (!error) {
        parent.roleDataList = data;
      }
    });
  }

  getQueueList() {
    const parent = this;
    const promise = parent.adminService.getQueueListPromise();
    return promise.then(function (value) {
      const queueDataList: QueueData[] = value as QueueData[];
      parent.queueDataList = queueDataList;
      parent.isDataLoaded = true;
    }).catch(function (error) {
      parent.isDataLoaded = true;
    });
  }

  isQueueAssigned(userData: UserData, queueNameCde: number) {
    if (userData.queueDataList.length > 0) {
      const dataList = userData.queueDataList.filter(a => a.queueNameCde == queueNameCde);
      if (dataList.length > 0) {
        userData.isQueueAssigned = true;
        return userData.isQueueAssigned;
      } else {
        userData.isQueueAssigned = false;
        return userData.isQueueAssigned;
      }
    }
    userData.isQueueAssigned = false;
    return userData.isQueueAssigned;

  }

  editRole() {
    this.isRoleEditable = true;
  }

  editEnabled(accountEnabled: boolean) {
    this.isEnabledEditable = true;
    this.accountEnabled = accountEnabled;
  }

  editQueue(userData: UserData, queueNameCde: number) {
    this.isQueueEditable = true;
    const isSelected = this.isQueueAssigned(userData, queueNameCde);
    if (isSelected == false) {
      if (this.addedQueues.filter(a => a == queueNameCde).length <= 0) {
        this.addedQueues.push(queueNameCde);
      } else {
        this.removedQueues.push(queueNameCde);
        if (this.removedQueues.filter(a => a == queueNameCde).length > 1) {
          this.removedQueues = this.removedQueues.filter(a => a != queueNameCde);
        }
      }
    } else {
      if (this.removedQueues.filter(a => a == queueNameCde).length <= 0) {
        this.removedQueues.push(queueNameCde);
      } else {

        this.addedQueues.push(queueNameCde);
        if (this.addedQueues.filter(a => a == queueNameCde).length > 1) {
          this.addedQueues = this.addedQueues.filter(a => a != queueNameCde);
        }
      }
    }
  }



  saveModal() {
    const parent = this;
    this.isCancelModal = true;


    const promise = parent.saveEnableUser();
    return promise.then(result => {
      if (result) {
        parent.isEnableUpdated = true;
      }
    }).catch(error => {
      if (error != null) {
        parent.isEnableUpdated = false;
      }
    }).then(result => {
      if (result != null) {
        parent.isEnableUpdated = false;
      }
      const promise = parent.saveRole();
      return promise.then(result => {
        if (result) {
          parent.isRoleUpdated = true;
        }
      }).catch(error => {
        if (!error) {
          parent.isRoleUpdated = false;
        }

      }).then(result => {
        parent.saveQueue();
      });
    });
  }




  saveEnableUser() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      if (parent.isEnabledEditable) {
        const promise = parent.adminService.saveUserEnabledData(parent.userDataEditList[0].userId, parent.accountEnabled);
        return promise.then(result => {
          parent.isEnableUpdated = true;
          fulfilled(parent.isEnableUpdated);

        }).catch(error => {
          parent.isEnableUpdated = false;
          rejected(parent.isEnableUpdated);
        });

      } else {
        parent.isEnableUpdated = true;
        fulfilled(parent.isEnableUpdated);
      }
    });
  }


  saveRole() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      if (parent.isRoleEditable) {
        const promise = parent.adminService.saveRoleData(parent.userDataEditList[0].userId, parent.userDataEditList[0].roleTypeCde);
        return promise.then(result => {
          parent.isRoleUpdated = true;
          fulfilled(parent.isRoleUpdated);

        }).catch(error => {
          parent.isEnableUpdated = false;
          rejected(parent.isRoleUpdated);
        });

      } else {
        parent.isRoleUpdated = true;
        fulfilled(parent.isRoleUpdated);
      }
    });
  }


  saveQueue() {
    const parent = this;

    if (parent.isQueueEditable) {
      const similar = [];
      if (parent.addedQueues.length > 0 || parent.removedQueues.length > 0) {

        for (let i = 0; i < parent.addedQueues.length; i++) {
          if (parent.removedQueues.filter(a => a == parent.addedQueues[i]).length > 0) {
            for (let sameNameCde = 0; sameNameCde < parent.removedQueues.filter(a => a == parent.addedQueues[i]).length; sameNameCde++) {
              const sameNameCdeList = parent.removedQueues.filter(a => a == parent.addedQueues[i]);
              similar.push(sameNameCdeList[sameNameCde]);
            }
          }
        }

        for (let i = 0; i < similar.length; i++) {
          parent.addedQueues = parent.addedQueues.filter(a => a != similar[i]);
          parent.removedQueues = parent.removedQueues.filter(b => b != similar[i]);
        }

        const promise = this.addUserQueueData();
        return promise.then(r => {
          parent.isQueueAddUpdated = true;
        }).catch(error => {
          if (error != null) {
            parent.isQueueAddUpdated = false;
          }

        }).then(result => {
          if (result != null) {
            parent.isQueueAddUpdated = false;
          } else {
            parent.isQueueAddUpdated = true;
          }
          const promise = parent.deleteUserQueueData();
          return promise.then(result => {
            if (result) {
              parent.isQueueDeleteUpdated = true;
            }
            parent.refreshComponent();
          }).catch(error => {
            parent.refreshComponent();
          });
        });

      } else {
        parent.isQueueAddUpdated = true;
        parent.isQueueDeleteUpdated = true;
        parent.refreshComponent();
      }
    } else {
      parent.isQueueAddUpdated = true;
      parent.isQueueDeleteUpdated = true;
      parent.refreshComponent();
    }
  }


  addUserQueueData() {
    const parent = this;
    const promises = [];
    return new Promise(function (fulfilled, rejected) {

      if (parent.addedQueues.length > 0) {
        parent.addedQueues.forEach(queueCde => {
          const promise = parent.adminService.addUserQueueData(parent.userDataEditList[0].userId, queueCde);
          promises.push(promise);
        });
      } else {
        parent.isQueueAddUpdated = true;
        fulfilled(parent.isQueueAddUpdated);
      }

      return Promise.all(promises).then(result => {
        parent.isQueueAddUpdated = true;
        fulfilled(parent.isQueueAddUpdated);

      }).catch(error => {
        parent.isQueueAddUpdated = false;
        rejected(parent.isQueueAddUpdated);
      });
    });
  }


  deleteUserQueueData() {
    const parent = this;
    const promises = [];

    return new Promise(function (fulfilled, rejected) {

      const promise = parent.adminService.getQueueListForUser(parent.userDataEditList[0].userId);
      return promise.then(value => {

        const queueListUser = value['response'];
        if (parent.removedQueues.length > 0) {
          parent.removedQueues.forEach(queueCde => {

            if (queueListUser.filter(a => a.queueNameCde == queueCde).length > 0) {
              const list = queueListUser.filter(a => a.queueNameCde == queueCde);

              const promise = parent.adminService.deleteUserQueueData(list[0].appUserQueueRelId);
              promises.push(promise);
            }
          });
        } else {
          parent.isQueueDeleteUpdated = true;
          fulfilled(parent.isQueueDeleteUpdated);
        }

        return Promise.all(promises).then(result => {
          parent.isQueueDeleteUpdated = true;
          fulfilled(parent.isQueueDeleteUpdated);
        });
      }).catch(error => {
        parent.isQueueDeleteUpdated = false;
        rejected(parent.isQueueDeleteUpdated);
      });
    });
  }


  refreshComponent() {
    if (!this.isCancel && this.isCancelModal) {
      if (this.isEnabledEditable || this.isRoleEditable || this.isQueueEditable) {
        if (!this.isRoleUpdated) {
          this.toastr.error(this.msgInfo.getMessage(117));
        }
        if (!this.isEnableUpdated) {
          this.toastr.error(this.msgInfo.getMessage(118));
        }
        if (!this.isQueueAddUpdated) {
          this.toastr.error(this.msgInfo.getMessage(120));
        }
        if (!this.isQueueDeleteUpdated) {
          this.toastr.error(this.msgInfo.getMessage(121));
        }
        if (!this.isRoleUpdated && !this.isEnableUpdated && !this.isQueueAddUpdated && !this.isQueueDeleteUpdated) {
          this.toastr.error(this.msgInfo.getMessage(102));
        } else {
          this.toastr.success(this.msgInfo.getMessage(116));
        }
      }
    }

    this.isDataLoaded = false;
    this.getUserList();
    this.getRoleList();
    this.getQueueList();
    this.isEnabledEditable = false;
    this.isRoleEditable = false;
    this.isQueueEditable = false;
    this.isCancelAllowed = false;
    this.isCancel = false;
    this.isCancelModal = false;
    this.isSaveAllowed = false;
    this.isRoleUpdated = false;
    this.isEnableUpdated = false;
    this.isQueueAddUpdated = false;
    this.isQueueDeleteUpdated = false;
    this.addedQueues = [];
    this.removedQueues = [];
  }

  onRowChecboxClick(userData: UserData) {
    for (let i = 0; i < this.userDataList.length; i++) {
      if (this.userDataList[i].userId == userData.userId) {
        this.userDataList[i].optionChecked = userData.optionChecked;
        this.auditData = new AuditData(0, userData.userId,0, '', '');
      } else {
        this.userDataList[i].optionChecked = false;
        this.isEnabledEditable = false;
        this.isQueueEditable = false;
        this.isRoleEditable = false;
      }
    }
    this.addedQueues = [];
    this.removedQueues = [];
    this.userDataEditList = this.userDataList
      .filter(opt => opt.optionChecked)
      .map(opt => opt);
    this.operation = 'edit';
    this.enableDisableFields();
  }

  onRowClick(rowNum: number) {
    this.selectedRow = rowNum;
  }

  enableDisableFields() {
    this.isSaveAllowed = false;
    this.isCancelAllowed = false;

    if (this.userDataEditList != null && this.userDataEditList.length > 0) {
      this.isSaveAllowed = true;
      this.isCancelAllowed = true;
    } else if (this.isEnabledEditable || this.isQueueEditable || this.isQueueEditable) {
      this.isSaveAllowed = true;
      this.isCancelAllowed = true;
    } else {
      this.isSaveAllowed = false;
      this.isCancelAllowed = false;
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
}
