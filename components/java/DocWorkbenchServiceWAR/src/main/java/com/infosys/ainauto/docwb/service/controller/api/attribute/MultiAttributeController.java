/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.attribute;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.AttributeValidator;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ManageMultiAttributeReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attribute.IMultiAttributeProcess;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/multiattribute")
@Api(tags = { "multiattribute" })
public class MultiAttributeController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(MultiAttributeController.class);

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IMultiAttributeProcess multiAttributeProcess;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@ApiOperation(value = "Add/update/delete multi-attribute at document/attachment level", tags = "multiattribute")
	@RequestMapping(value = "/manage", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> manageAttribute(@RequestBody ManageMultiAttributeReqData requestData) {
		List<EntityDbData> prevDocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestDocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		try {
			ResponseEntity<String> responseEntity = attributeValidator.validateManageAttributeReq(requestData);
			if (responseEntity != null) {
				return responseEntity;
			}
			boolean isFeatureAllowed = checkFeatureAccess(requestData);
			if (!isFeatureAllowed) {
				return jsonResponseForbidden();
			}
			List<EntityDbData> docEntityDbDataList = multiAttributeProcess.manageAttributes(requestData);

			if (ListUtility.hasValue(docEntityDbDataList)) {
				prevDocEntityDbDataList.add(docEntityDbDataList.get(1));
				latestDocEntityDbDataList.add(docEntityDbDataList.get(0));

				apiResponse = 0 + " record(s) added/ deleted/ updated";
				if (latestDocEntityDbDataList.get(0).getProcessedCount() > 0) {

					apiResponse = "";
					if (latestDocEntityDbDataList.get(0).getAddProcessedCount() > 0) {
						apiResponse += latestDocEntityDbDataList.get(0).getAddProcessedCount() + " record(s) added, ";
					}
					if (latestDocEntityDbDataList.get(0).getDeleteProcessedCount() > 0) {
						apiResponse += latestDocEntityDbDataList.get(0).getDeleteProcessedCount()
								+ " record(s) deleted, ";
					}
					if (latestDocEntityDbDataList.get(0).getUpdateProcessedCount() > 0) {
						apiResponse += latestDocEntityDbDataList.get(0).getUpdateProcessedCount()
								+ " record(s) updated";
					}
					if (apiResponse.endsWith(", ")) {
						apiResponse = apiResponse.substring(0, apiResponse.length() - 2);
					}
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				} else {
					apiResponseData = getStringApiResponseData(apiResponse,
							WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
							WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
				}
			} else {
				apiResponse = docEntityDbDataList.size() + " record(s) added/ deleted/ updated";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			if (ex.getMessage() != null
					&& ex.getMessage().equalsIgnoreCase(WorkbenchConstants.DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS)) {
				try {
					return jsonResponseOk(
							getStringApiResponseData(WorkbenchConstants.DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS,
									WorkbenchConstants.API_RESPONSE_CDE_MULTI_ATTRIBUTE_ALREADY_EXIST,
									WorkbenchConstants.API_RESPONSE_MSG_MULTI_ATTRIBUTE_ALREADY_EXIST));
				} catch (JsonProcessingException e) {
				}
			}
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			callAudit(prevDocEntityDbDataList, latestDocEntityDbDataList);
		}
	}

	private void callAudit(List<EntityDbData> prevDocEntityDbDataList, List<EntityDbData> latestDocEntityDbDataList) {
		EntityDbData latestDocEntityDbData = latestDocEntityDbDataList.get(0);
		if (latestDocEntityDbData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()
				&& ((latestDocEntityDbData.getAddProcessedCount() > 0
						&& latestDocEntityDbData.getDeleteProcessedCount() > 0)
						|| (latestDocEntityDbData.getDeleteProcessedCount() > 0
								&& latestDocEntityDbData.getUpdateProcessedCount() > 0)
						|| (latestDocEntityDbData.getUpdateProcessedCount() > 0
								&& latestDocEntityDbData.getAddProcessedCount() > 0))) {
			auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList, EnumEntityType.ATTRIBUTE,
					EnumOperationType.UPDATE);

		} else {

			if (latestDocEntityDbData.getAddProcessedCount() > 0) {
				auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList,
						EnumEntityType.ATTRIBUTE, EnumOperationType.INSERT);
			}
			if (latestDocEntityDbData.getDeleteProcessedCount() > 0) {
				auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList,
						EnumEntityType.ATTRIBUTE, EnumOperationType.DELETE);
			}
			if (latestDocEntityDbData.getUpdateProcessedCount() > 0) {
				auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList,
						EnumEntityType.ATTRIBUTE, EnumOperationType.UPDATE);
			}
		}

	}

	private boolean checkFeatureAccess(ManageMultiAttributeReqData requestData) throws WorkbenchException {
		if (((requestData.getAttribute() != null && ListUtility.hasValue(requestData.getAttribute().getAddAttributes()))
				|| (requestData.getAttachment() != null && requestData.getAttachment().getAttribute() != null
						&& ListUtility.hasValue(requestData.getAttachment().getAttribute().getAddAttributes())))
				&& !apiRoleAuthorizationProcess
						.isFeatureAccessAllowed(WorkbenchConstants.FEATURE_ID_ATTRIBUTE_CREATE)) {
			return false;
		}
		if (((requestData.getAttribute() != null
				&& ListUtility.hasValue(requestData.getAttribute().getEditAttributes()))
				|| (requestData.getAttachment() != null && requestData.getAttachment().getAttribute() != null
						&& ListUtility.hasValue(requestData.getAttachment().getAttribute().getEditAttributes())))
				&& !apiRoleAuthorizationProcess.isFeatureAccessAllowed(WorkbenchConstants.FEATURE_ID_ATTRIBUTE_EDIT)) {
			return false;
		}
		if (((requestData.getAttribute() != null
				&& ListUtility.hasValue(requestData.getAttribute().getDeleteAttributes()))
				|| (requestData.getAttachment() != null && requestData.getAttachment().getAttribute() != null
						&& ListUtility.hasValue(requestData.getAttachment().getAttribute().getDeleteAttributes())))
				&& !apiRoleAuthorizationProcess
						.isFeatureAccessAllowed(WorkbenchConstants.FEATURE_ID_ATTRIBUTE_DELETE)) {
			return false;
		}
		return true;
	}

}
