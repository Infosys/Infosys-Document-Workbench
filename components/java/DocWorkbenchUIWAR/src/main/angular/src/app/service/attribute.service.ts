/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AttributeData } from '../data/attribute-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { DocumentData } from '../data/document-data';
import { AttributeAttributeMappingData } from '../data/attribute-attribute-mapping-data';
import { AttributeSortKeyData } from '../data/attribute-sort-key';
import { SessionService } from './session.service';

@Injectable()
export class AttributeService {

    private valMap = new Map();

    constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper,
        private sessionService: SessionService) { }
    private attributeNameValuesList: AttributeData;

    getAttributeText() {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.GET_ATTRIBUTE_TEXT;
        let attributeTextList: AttributeData[];
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.get(url)
                .subscribe(
                    _data => {
                        attributeTextList = _data['response'];
                        fulfilled(attributeTextList);
                    },
                    _error => {
                        rejected(_error);
                    }
                );
        });
    }

    /**
     *@param entity = attrattr-mapping
     */
    getAttributeAttributeMapping(entity) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {
            if (!parent.valMap.has(entity)) {
                const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                    CONSTANTS.APIS.DOCWBSERVICE.GET_ATTRIBUTE_ATTRIBUTE_MAPPING;
                parent.httpClient.get(url)
                    .subscribe(
                        _data => {
                            const attrAttrMapping: AttributeAttributeMappingData[] = _data['response'];
                            parent.valMap.set(entity, attrAttrMapping);
                            fulfilled(attrAttrMapping);
                        },
                        _error => {
                            rejected(_error);
                        }
                    );
            } else {
                fulfilled(parent.valMap.get(entity));
            }
        });
    }

    /**
     *@param entity = attr-sortkey
     */
    getAttributeSortkeyData(entity) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {
            if (!parent.valMap.has(entity)) {
                const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                    CONSTANTS.APIS.DOCWBSERVICE.GET_ATTRIBUTE_SORTKEY;
                parent.httpClient.get(url)
                    .subscribe(
                        _data => {
                            const attrSortKey: AttributeSortKeyData[] = _data['response'];
                            parent.valMap.set(entity, attrSortKey);
                            fulfilled(attrSortKey);
                        },
                        _error => {
                            rejected(_error);
                        }
                    );
            } else {
                fulfilled(parent.valMap.get(entity));
            }
        });
    }

    manageAttributeData(data) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.MANAGE_ATTRIBUTE_DATA;
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.post(url,
                data
            ).subscribe(
                _data => {
                    fulfilled(_data);
                },
                _error => {
                    rejected(_error);
                }
            );
        });
    }

    addAttributeData(data) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.ADD_ATTRIBUTE_DATA;
        // console.log('addAttributeData()',data);
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.post(url,
                data
            ).subscribe(
                _data => {
                    fulfilled(_data);
                },
                _error => {
                    rejected(_error);
                }
            );
        });
    }

    editAttributeData(attributeList) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.EDIT_ATTRIBUTE_DATA;
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.post(url,
                attributeList
            ).subscribe(
                _data => {
                    fulfilled(_data);
                },
                _error => {
                    rejected(_error);
                }
            );
        });
    }

    deleteAttributeData(attributeList) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.DELETE_ATTRIBUTE_DATA;
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.post(url,
                attributeList
            ).subscribe(
                _data => {
                    fulfilled(_data);
                },
                _error => {
                    rejected(_error);
                }
            );
        });
    }

    getDocumentAttributes(docId: number) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {
            if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.ATTRIBUTE_VIEW).isVisible) {
                rejected(null);
            } else {
                let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                    CONSTANTS.APIS.DOCWBSERVICE.GET_ATTRIBUTES;
                let queryParams = '';
                if (docId > 0) {
                    queryParams += 'docId=' + docId;
                }
                if (queryParams.length > 0) {
                    url += '?' + queryParams;
                }
                parent.httpClient.get(url)
                    .subscribe(data => {
                        fulfilled(data['response'] as AttributeData[]);
                    },
                        error => {
                            rejected(null);
                        });
            }
        });
    }

    getAttributeNameValues(attrNameCdes: string) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {
            if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.ATTRIBUTE_VIEW).isVisible) {
                rejected(null);
            } else {
                if (parent.attributeNameValuesList == null) {
                    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                        CONSTANTS.APIS.DOCWBSERVICE.GET_ATTR_NAME_VALUES;
                    let queryParams = '';
                    queryParams += 'attrNameCdes=' + attrNameCdes;

                    if (queryParams.length > 0) {
                        url += '?' + queryParams;
                    }
                    parent.httpClient.get(url)
                        .subscribe(data => {
                            parent.attributeNameValuesList = data['response'] as AttributeData;
                            fulfilled(parent.attributeNameValuesList);
                        },
                            error => {
                                rejected(error);
                            }
                        );
                    // return null;
                } else {
                    fulfilled(parent.attributeNameValuesList);
                }
            }
        });
    }

    getAttributesNotification(docId: number) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {

            let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                CONSTANTS.APIS.DOCWBSERVICE.GET_ATTRIBUTES_NOTIFICATION;
            if (docId > 0) {
                url += '?docId=' + docId;
            } else {
                rejected('docId invalid');
            }
            parent.httpClient.get(url).subscribe(
                data => {
                    fulfilled(data['response'] as DocumentData);
                },
                error => {
                    rejected(error);
                }
            );
        });
    }

    getAttachmentAttributes(docId: number, attachmentId?: number) {
        const parent = this;
        return new Promise(function (fulfilled, rejected) {
            let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_ATTRIBUTES;
            if (docId > 0) {
                url += '?docId=' + docId;
            } else {
                rejected('docId invalid');
            }
            if (attachmentId > 0) {
                url += '&attachmentId=' + attachmentId;
            }
            parent.httpClient.get(url).subscribe(
                data => {
                    fulfilled(data['response'] as AttributeData[]);
                },
                error => {
                    rejected(error);
                }
            );
        });
    }
}

