/** =============================================================================================================== *
 * Copyright 2021 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, HostListener, OnInit, Directive } from '@angular/core';
import { TelemetryActor, TelemetryContextData, TelemetryEventOptions } from '@infy-docwb/infy-telemetry-sdk/lib/data/telemetry-sunbird-data';
import { CONSTANTS } from './common/constants';
import { FeatureAccessMode } from './data/feautre-access-mode';
import { NiaTelemetryService } from './service/nia-telemetry.service';
import { SessionService } from './service/session.service';
import { ConfigDataHelper } from './utils/config-data-helper';

@Directive()
export abstract class BaseComponent implements OnInit {
  constructor(public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService: NiaTelemetryService) { }
  bmodel: any = {
    FID: CONSTANTS.FEATURE_ID_CONFIG,
    FID_ERR_MSG: CONSTANTS.FEATURE_ERROR_MSG,
    TELE_EVENTS: CONSTANTS.TELEMETRY_EVENT,
    TELEID: CONSTANTS.TELEMETRY_INTERACT_NAME
  }

  // private sf = this.niaTelemetryService.serviceFactory();
  private eventOptions: TelemetryEventOptions = {};
  private pageIdMap = {}

  ngOnInit() { }

  getFeature(featureId: string): FeatureAccessMode {
    return this.sessionService.getFeatureAccessModeDataFor(featureId);
  }

  @HostListener('window:unload', ['$event'])
  unloadHandler(event) {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

  protected abstract getClassName(): string;

  triggerTelemetryEvents(event: string, interactId?: string, cdata?: [], extra?: any, params?: any[], rating?: number, commenttxt?: string, contentid?: string) {
    const parent = this;
    console.log('telemetryEnabled=', parent.configDataHelper.getValue(CONSTANTS.CONFIG.TELEMETRY_ENABLED))
    if (!parent.configDataHelper.getValue(CONSTANTS.CONFIG.TELEMETRY_ENABLED)) {
      return;
    }
    let cdata_default = [{ "type": "tenantId", "id": parent.configDataHelper.getValue(CONSTANTS.CONFIG.TENANT_ID) }];
    if (cdata) {
      cdata_default = [...cdata_default, ...cdata];
    }

    let currentPageId = window.location.href.split(window.location.host + window.location.pathname)[1];
    const componentName = this.getClassName();
    currentPageId = currentPageId.startsWith("/") ? currentPageId : "/" + currentPageId;
    const pageId = componentName + "" + currentPageId;
    const startData = {
      type: 'workflow', // Required. app, session, editor, player, workflow, assessment
      pageid: pageId // Optional. Page/Stage id where the start has happened.
    };
    const endData = {
      type: 'workflow', // Required. app, session, editor, player, workflow, assessment
      pageid: this.pageIdMap[event + componentName], // Optional. Page/Stage id where the start has happened.
      contentid: this.pageIdMap[event + componentName] // used to find the view and leave time of component / worflow
    };
    const impressionData = {
      type: 'view', // Required. Impression type (list, detail, view, edit, workflow, search)
      pageid: pageId, // Required. Unique page id
      uri: window.location.href, // Required. Relative URL of the content
    };

    if (Array.isArray(params)) {
      params.forEach(param => {
        param.url = { url: pageId };
      });
    } else {
      params = [
        {
          request: {
            prompt_template: "",
            question: "",
            context_within_top_k: "",
            context_outside_top_k: ""
          },
          response: {
            answer: ""
          },
          url: {
            url: pageId
          }
        }
      ];
    }
    const promiseAll = [];
    promiseAll.push(parent.sessionService.getLoggedInUserDetailsPromise());
    Promise.all(promiseAll).then(function (data) {
      //console.log("Trigger Tele userdata ", data)
      if (data && data[0]) {
        const actor: TelemetryActor = {
          'id': "[" + data[0]['roleTypeTxt'] + "]-" + data[0]['userName'],
          'type': 'user'
        }
        const contextData: TelemetryContextData = {
          'channel': NiaTelemetryService.telemetryConfig.channel,
          'env': NiaTelemetryService.telemetryConfig.env,
          // 'sid': parent.niaTelemetryService.getDocwbSessionId(),
          'sid': parent.niaTelemetryService.getTelemetrySessionId(),
          'cdata': cdata_default
        }
        parent.eventOptions = { "actor": actor, "context": contextData }
        switch (event) {
          case CONSTANTS.TELEMETRY_EVENT.START:
            parent.pageIdMap["END" + componentName] = pageId
            parent.niaTelemetryService.getTelemetryService().start(NiaTelemetryService.telemetryConfig,
              pageId, '1.0', startData, parent.eventOptions);
            break;
          case CONSTANTS.TELEMETRY_EVENT.END:
            parent.niaTelemetryService.getTelemetryService().end(endData, parent.eventOptions);
            delete parent.pageIdMap[event + componentName]
            break;
          case CONSTANTS.TELEMETRY_EVENT.IMPRESSION:
            parent.niaTelemetryService.getTelemetryService().impression(impressionData, parent.eventOptions);
            break;
          case CONSTANTS.TELEMETRY_EVENT.INTERACT:
            const interactData = {
              type: 'touch',
              pageid: pageId,
              id: interactId ? interactId : 'submit',
              extra: extra ? extra : {}
            };
            parent.niaTelemetryService.getTelemetryService().interact(interactData, parent.eventOptions);
            break;
          case CONSTANTS.TELEMETRY_EVENT.LOG:
            const logData = {
              type: "api_call",
              level: "INFO",
              contentid: contentid !== undefined ? contentid : "",
              params: params
            };
            console.log("LOG", logData)
            parent.niaTelemetryService.getTelemetryService().log(logData, parent.eventOptions);
            break;
          case CONSTANTS.TELEMETRY_EVENT.FEEDBACK:
            const feedbackData = {
              rating: rating !== undefined ? rating : 0,
              commentid: contentid !== undefined ? contentid : "",
              commenttxt: commenttxt !== undefined ? commenttxt : "",
            };
            console.log("FEEDBACK", feedbackData)
            parent.niaTelemetryService.getTelemetryService().feedback(feedbackData, parent.eventOptions);
            break;
          default:
            break;
        }
      }
    });
  }
}
