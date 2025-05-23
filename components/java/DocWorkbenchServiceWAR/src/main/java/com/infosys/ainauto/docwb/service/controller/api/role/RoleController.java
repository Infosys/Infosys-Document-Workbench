/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.role;

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

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.controller.api.user.UserController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.role.AddRoleReqData;
import com.infosys.ainauto.docwb.service.model.api.role.DeleteRoleReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.role.IRoleProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/role")
@Api(tags = { "role" })
public class RoleController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private IRoleProcess roleProcess;
	@Autowired
	private IAuditProcess auditProcess;

	@ApiOperation(value = "Assign user role", tags = "role")
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> assignRole(@RequestBody List<AddRoleReqData> addRoleReqDataList) {
		// List<Long> appUserRoleRelIdList = null;
		List<List<EntityDbData>> entityDbDataListOfList = new ArrayList<List<EntityDbData>>();

		try {
			int changedCounter = 0;
			int noChangeCounter = 0;
			int failedCounter = 0;
			String apiResponse = "";
			ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
			List<EntityDbData> entityDbDataList = null;
			EntityDbData latestEntityDbData = null;
			long appUserRoleRelId = 0;

			if (ListUtility.hasValue(addRoleReqDataList)) {
				for (AddRoleReqData addRoleReqData : addRoleReqDataList) {
					try {
						entityDbDataList = roleProcess.addNewRole(addRoleReqData);
						if (!ListUtility.hasValue(entityDbDataList)) {
							noChangeCounter++;
						} else {
							latestEntityDbData = entityDbDataList.get(0);
							if (ListUtility.hasValue(latestEntityDbData.getAppUserRoleRelIdList())) {
								appUserRoleRelId = latestEntityDbData.getAppUserRoleRelIdList().get(0);
								if (appUserRoleRelId > 0) {
									changedCounter++;
									entityDbDataListOfList.add(entityDbDataList);
								} else if (appUserRoleRelId == 0) {
									noChangeCounter++;
								}
							} else {
								noChangeCounter++;
							}
						}
					} catch (Exception ex) {
						failedCounter++;
					}

				}
			}

			StringBuffer sbMessage = new StringBuffer();
			if (!ListUtility.hasValue(addRoleReqDataList)) {
				sbMessage.append("No updates made due to empty reqest");
			}
			if (changedCounter > 0) {
				sbMessage.append(changedCounter + " new role(s) assigned to user(s)");
			}
			if (noChangeCounter > 0) {
				if (sbMessage.length() > 0) {
					sbMessage.append(" AND ");
				}
				sbMessage.append(noChangeCounter + " user(s) could not be assigned the requested role(s)");
			}
			if (failedCounter > 0) {
				if (sbMessage.length() > 0) {
					sbMessage.append(" AND ");
				}
				sbMessage.append(failedCounter + " user(s) assignment to the requested role(s) failed.");
			}

			apiResponse = sbMessage.toString();
			if (!ListUtility.hasValue(addRoleReqDataList)) {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			} else if (addRoleReqDataList.size() == changedCounter) {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else if (addRoleReqDataList.size() == failedCounter) {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_FAILURE,
						WorkbenchConstants.API_RESPONSE_MSG_FAILURE);
			} else {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_PARTIAL_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_PARTIAL_SUCCESS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> prevdocEntityDbDataList = new ArrayList<EntityDbData>();
			List<EntityDbData> latestdocEntityDbDataList = new ArrayList<EntityDbData>();

			for (List<EntityDbData> entityDbDataList : entityDbDataListOfList) {
				if (ListUtility.hasValue(entityDbDataList)
						&& entityDbDataList.get(0).getAppUserRoleRelIdList().get(0) > 0) {
					if (entityDbDataList.size() == 2) {
						prevdocEntityDbDataList.add(entityDbDataList.get(1));
						latestdocEntityDbDataList.add(entityDbDataList.get(0));
						auditProcess.addAuditDetails(prevdocEntityDbDataList, latestdocEntityDbDataList,
								EnumEntityType.ROLE, EnumOperationType.UPDATE);
					} else
						auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.ROLE, EnumOperationType.INSERT);
				}
			}
		}
	}

	@ApiOperation(value = "Deletes user role", tags = "role")
	@RequestMapping(value = "", method = RequestMethod.DELETE, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteRole(@RequestBody List<DeleteRoleReqData> deleteRoleReqDataList) {
		int noOfRecordsDeleted = 0;
		List<Long> appUserRoleRelIdList = new ArrayList<>(); // AC

		try {
			appUserRoleRelIdList = roleProcess.deleteRole(deleteRoleReqDataList);

			noOfRecordsDeleted = appUserRoleRelIdList.size();

			String apiResponse = noOfRecordsDeleted + " record(s) deleted";
			ApiResponseData<String> apiResponseData;

			if (noOfRecordsDeleted > 0) {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			if (noOfRecordsDeleted > 0) {
				EntityDbData entityDbData = new EntityDbData();
				entityDbData.setAppUserRoleRelIdList(appUserRoleRelIdList);
				entityDbDataList.add(entityDbData);
				auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.ROLE, EnumOperationType.DELETE);
			}

		}
	}

}
