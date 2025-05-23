/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.audit;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.common.PropertyManager;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.dao.audit.IAuditDataAccess;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAuditDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserAuditDbData;

@Component
public class AuditAsyncProcess implements IAuditAsyncProcess {

	private static final Logger logger = LoggerFactory.getLogger(AuditAsyncProcess.class);

	private static final String PROP_NAME_AUDIT_ACTION_MESSAGE = "audit.action.insert.message";
	private static final String PROP_NAME_AUDIT_ACTION_UPDATE_MESSAGE = "audit.action.update.message";
	private static final String PROP_NAME_AUDIT_ACTION_DELETE_MESSAGE = "audit.action.delete.message";
	private static final String PROP_NAME_AUDIT_ATTRIBUTE_MESSAGE = "audit.attribute.insert.message";
	private static final String PROP_NAME_AUDIT_ATTRIBUTE_UPDATE_MESSAGE = "audit.attribute.update.message";
	private static final String PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_INSERT_MESSAGE = "audit.multi.attribute.table.insert.message";
	private static final String PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_UPDATE_MESSAGE = "audit.multi.attribute.table.update.message";
	private static final String PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_DELETE_MESSAGE = "audit.multi.attribute.table.delete.message";
	private static final String PROP_NAME_AUDIT_ATTRIBUTE_DELETE_MESSAGE = "audit.attribute.delete.message";
	private static final String PROP_NAME_AUDIT_ATTACHMENT_MESSAGE = "audit.attachment.insert.message";
	private static final String PROP_NAME_AUDIT_ATTACHMENT_REALTION_MESSAGE = "audit.attachment.relation.insert.message";
	private static final String PROP_NAME_AUDIT_DOCUMENT_MESSAGE = "audit.case.insert.message";
	private static final String PROP_NAME_AUDIT_DOCUMENT_UPDATE_MESSAGE = "audit.case.update.message";
	private static final String PROP_NAME_AUDIT_DOCUMENT_DELETE_MESSAGE = "audit.case.delete.message";
	private static final String PROP_NAME_AUDIT_EMAIL_MESSAGE = "audit.email.insert.message";
	private static final String PROP_NAME_AUDIT_EMAIL_UPDATE_MESSAGE = "audit.email.update.message";
	private static final String PROP_NAME_AUDIT_EMAIL_DELETE_MESSAGE = "audit.email.delete.message";
	private static final String PROP_NAME_AUDIT_ROLE_MESSAGE = "audit.role.insert.message";
	private static final String PROP_NAME_AUDIT_ROLE_DELETE_MESSAGE = "audit.role.delete.message";
	private static final String PROP_NAME_AUDIT_ROLE_UPDATE_MESSAGE = "audit.role.update.message";
	private static final String PROP_NAME_AUDIT_QUEUE_ASSIGNMENT_INSERT_MESSAGE = "audit.queue.assignment.insert.message";
	private static final String PROP_NAME_AUDIT_QUEUE_ASSIGNMENT_DELETE_MESSAGE = "audit.queue.assignment.delete.message";
	private static final String PROP_NAME_AUDIT_USER_MESSAGE = "audit.user.insert.message";
	private static final String PROP_NAME_AUDIT_USER_ENABLE_STATUS_UPDATE_MESSAGE = "audit.user.enable.status.update.message";
	private static final String PROP_NAME_AUDIT_USER_PASSWRD_UPDATE_MESSAGE = "audit.user.password.update.message";
	private static final String PROP_NAME_AUDIT_CASE_ASSIGNMENT_UPDATE_MESSAGE = "audit.case.assignment.update.message";
	private static final String PROP_NAME_AUDIT_CASE_ASSIGNMENT_INSERT_MESSAGE = "audit.case.assignment.insert.message";
	private static final String PROP_NAME_AUDIT_EMAIL_SEND_MESSAGE = "audit.email.send.message";
	private static final String PROP_NAME_AUDIT_EMAIL_SEND_INSERT_MESSAGE = "audit.email.send.insert.message";
	private static final String PROP_NAME_AUDIT_QUEUE_INSERT_MESSAGE = "audit.queue.insert.message";

	@Autowired
	private IAuditDataAccess auditDataAccess;

	// Async method should be public and not called from same class
	@Async
	public void addAuditDetailsAsync(List<EntityDbData> entityDataList, EnumEntityType entityName,
			EnumOperationType operationType, String loggedInUser, String tenantId) {
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		String auditMessage = "";
		String currentValue = "";
		String prevValue = "";
		List<Long> inputDataList = new ArrayList<Long>();
		if (ListUtility.hasValue(entityDataList)) {
			switch (entityName) {
			case ACTION:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getDocActionRelIdList())
						? entityDataList.get(0).getDocActionRelIdList()
						: new ArrayList<>();
				for (int i = 0; i < inputDataList.size(); i++) {
					List<DocAuditDbData> dataList = getDocAuditValues(entityName, inputDataList.get(i), tenantId);
					if (operationType == EnumOperationType.INSERT) {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ACTION_MESSAGE);
						currentValue = dataList.get(0).getActionNameTxt();
					} else {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ACTION_DELETE_MESSAGE);
						prevValue = dataList.get(0).getActionNameTxt();
					}
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(dataList.get(0).getDocId());
					auditDataList.add(auditData);
				}
				break;
			case ATTACHMENT_REL:
				if (operationType == EnumOperationType.INSERT) {
					auditMessage = PropertyManager.getInstance()
							.getProperty(PROP_NAME_AUDIT_ATTACHMENT_REALTION_MESSAGE);
					String parentAttachmentName = getDocAuditValues(entityName,
							entityDataList.get(0).getParentAttachmentId(), tenantId).get(0).getFileName();
					String childAttachmentName = getDocAuditValues(entityName,
							entityDataList.get(0).getChildAttachmentId(), tenantId).get(0).getFileName();

					currentValue = parentAttachmentName + " <- " + childAttachmentName;
			
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(entityDataList.get(0).getAttaAttaRelId()));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(entityDataList.get(0).getDocId());
					auditDataList.add(auditData);
				}
				break;
			case ATTACHMENT:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getDocAttachmentRelIdList())
						? entityDataList.get(0).getDocAttachmentRelIdList()
						: new ArrayList<>();
				if (operationType == EnumOperationType.INSERT) {
					auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ATTACHMENT_MESSAGE);
				}
				for (int i = 0; i < inputDataList.size(); i++) {
					List<DocAuditDbData> dataList = getDocAuditValues(entityName, inputDataList.get(i), tenantId);
					currentValue = dataList.get(0).getFileName();
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(dataList.get(0).getDocId());
					auditDataList.add(auditData);
				}
				break;

			case DOCUMENT:
				for (int i = 0; i < entityDataList.size(); i++) {
					if (entityDataList.get(i).getDocId() > 0) {
						List<DocAuditDbData> dataList = getDocAuditValues(entityName, entityDataList.get(i).getDocId(),
								tenantId);
						if (operationType == EnumOperationType.INSERT) {
							auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_DOCUMENT_MESSAGE);
							currentValue = String.valueOf(entityDataList.get(i).getDocId());
						} else if (operationType == EnumOperationType.UPDATE) {
							auditMessage = PropertyManager.getInstance()
									.getProperty(PROP_NAME_AUDIT_DOCUMENT_UPDATE_MESSAGE);
							prevValue = entityDataList.get(0).getTaskStatusTxt();
							currentValue = dataList.get(0).getTaskStatusTxt();
						} else {
							auditMessage = PropertyManager.getInstance()
									.getProperty(PROP_NAME_AUDIT_DOCUMENT_DELETE_MESSAGE);
							prevValue = String.valueOf(entityDataList.get(i).getDocId());
						}
						AuditDbData auditData = new AuditDbData();
						auditData.setEntityName(entityName.getValue());
						auditData.setEntityValue(String.valueOf(entityDataList.get(i).getDocId()));
						auditData.setAuditMessage(auditMessage.trim());
						auditData.setPreviousValue(prevValue);
						auditData.setCurrentValue(currentValue);
						auditData.setDocId(entityDataList.get(i).getDocId());
						auditDataList.add(auditData);
					}
				}
				break;

			case EMAIL:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getEmailIdList())
						? entityDataList.get(0).getEmailIdList()
						: new ArrayList<>();
				for (int i = 0; i < inputDataList.size(); i++) {
					List<DocAuditDbData> dataList = getDocAuditValues(entityName, inputDataList.get(i), tenantId);
					if (operationType == EnumOperationType.INSERT) {
						if (dataList.get(0).getTaskStatusCde() == EnumTaskStatus.YET_TO_START.getValue()) {
							auditMessage = PropertyManager.getInstance()
									.getProperty(PROP_NAME_AUDIT_EMAIL_SEND_INSERT_MESSAGE);
							currentValue = "Subject: " + dataList.get(0).getEmailSubject();
							String attachment = " (Attachment(s) : ";
							currentValue += entityDataList.get(0).getAttachmentCount() > 0
									? attachment + entityDataList.get(0).getAttachmentCount() + ")"
									: "";
						} else {
							auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_EMAIL_MESSAGE);
							currentValue = dataList.get(0).getEmailSubject();
						}

					} else if (operationType == EnumOperationType.UPDATE) {
						if (dataList.get(0).getTaskStatusCde() == EnumTaskStatus.UNDEFINED.getValue()) {
							auditMessage = PropertyManager.getInstance()
									.getProperty(PROP_NAME_AUDIT_EMAIL_UPDATE_MESSAGE);
							auditMessage = auditMessage.replace(WorkbenchConstants.ENTITY_IDENTIFIER,
									dataList.get(0).getEmailSubject());
						} else {
							auditMessage = PropertyManager.getInstance()
									.getProperty(PROP_NAME_AUDIT_EMAIL_SEND_MESSAGE);
							auditMessage = auditMessage.replace(WorkbenchConstants.ENTITY_IDENTIFIER,
									dataList.get(0).getEmailSubject());
							prevValue = entityDataList.get(0).getTaskStatusTxt();
							currentValue = dataList.get(0).getTaskStatusTxt();
						}

					} else {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_EMAIL_DELETE_MESSAGE);
						prevValue = dataList.get(0).getEmailSubject();
					}

					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(dataList.get(0).getDocId());
					auditDataList.add(auditData);
				}
				break;

			case CASE_OWNER_ASSIGNMENT:
			case CASE_REVIEWER_ASSIGNMENT:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getDocAppUserRelIdList())
						? entityDataList.get(0).getDocAppUserRelIdList()
						: new ArrayList<>();
				if (operationType == EnumOperationType.INSERT) {
					auditMessage = PropertyManager.getInstance()
							.getProperty(PROP_NAME_AUDIT_CASE_ASSIGNMENT_INSERT_MESSAGE);
					String roleTypeTxt = (entityName == EnumEntityType.CASE_OWNER_ASSIGNMENT)
							? WorkbenchConstants.ROLE_TYPE_TXT_OWNER
							: WorkbenchConstants.ROLE_TYPE_TXT_REVIEWER;
					auditMessage=auditMessage.replace(WorkbenchConstants.ROLE_TYPE_TXT_PLACEHOLDER, roleTypeTxt);
				}
				for (int i = 0; i < inputDataList.size(); i++) {
					List<DocAuditDbData> dataList = getDocAuditValues(entityName, inputDataList.get(i), tenantId);
					currentValue = dataList.get(0).getUserFullName();
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(dataList.get(0).getDocId());
					auditDataList.add(auditData);
				}
				break;

			case ROLE:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getAppUserRoleRelIdList())
						? entityDataList.get(0).getAppUserRoleRelIdList()
						: new ArrayList<>();

				for (int i = 0; i < inputDataList.size(); i++) {
					List<UserAuditDbData> dataList = getUserAuditValues(entityName, inputDataList.get(i), tenantId);
					if (operationType == EnumOperationType.INSERT) {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ROLE_MESSAGE);
						currentValue = dataList.get(0).getRoleTypeTxt();
					} else {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ROLE_DELETE_MESSAGE);
						prevValue = dataList.get(0).getRoleTypeTxt();
					}
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setAppUserId(dataList.get(0).getAppUserId());
					auditDataList.add(auditData);
				}
				break;
			case QUEUE_ASSIGNMENT:
				inputDataList = ListUtility.hasValue(entityDataList.get(0).getAppUserQueueRelIdList())
						? entityDataList.get(0).getAppUserQueueRelIdList()
						: new ArrayList<>();
				for (int i = 0; i < inputDataList.size(); i++) {
					List<UserAuditDbData> dataList = getUserAuditValues(entityName, inputDataList.get(i), tenantId);
					if (operationType == EnumOperationType.INSERT) {
						auditMessage = PropertyManager.getInstance()
								.getProperty(PROP_NAME_AUDIT_QUEUE_ASSIGNMENT_INSERT_MESSAGE);
						currentValue = dataList.get(0).getQueueNameTxt();
					} else {
						auditMessage = PropertyManager.getInstance()
								.getProperty(PROP_NAME_AUDIT_QUEUE_ASSIGNMENT_DELETE_MESSAGE);
						prevValue = dataList.get(0).getQueueNameTxt();
					}
					AuditDbData auditData = new AuditDbData();
					auditData.setEntityName(entityName.getValue());
					auditData.setEntityValue(String.valueOf(inputDataList.get(i)));
					auditData.setAuditMessage(auditMessage.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setAppUserId(dataList.get(0).getAppUserId());
					auditDataList.add(auditData);
				}
				break;
			case USER:

				for (EntityDbData entityDbData : entityDataList) {
					if (entityDbData.getAppUserId() > 0) {
						List<UserAuditDbData> dataList = getUserAuditValues(entityName, entityDbData.getAppUserId(),
								tenantId);
						if (operationType == EnumOperationType.INSERT) {
							auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_USER_MESSAGE);
							currentValue = dataList.get(0).getUserFullName();
						} else {
							if (entityDbData.isUserPasswordChanged())
								auditMessage = PropertyManager.getInstance()
										.getProperty(PROP_NAME_AUDIT_USER_PASSWRD_UPDATE_MESSAGE);
							else {
								auditMessage = PropertyManager.getInstance()
										.getProperty(PROP_NAME_AUDIT_USER_ENABLE_STATUS_UPDATE_MESSAGE);
								prevValue = entityDbData.isAccountEnabled() ? WorkbenchConstants.ENABLED
										: WorkbenchConstants.DISABLED;
								currentValue = dataList.get(0).isAccountEnabled() ? WorkbenchConstants.ENABLED
										: WorkbenchConstants.DISABLED;
							}
						}
						AuditDbData auditData = new AuditDbData();
						auditData.setEntityName(entityName.getValue());
						auditData.setEntityValue(String.valueOf(entityDbData.getAppUserId()));
						auditData.setAuditMessage(auditMessage.trim());
						auditData.setPreviousValue(prevValue);
						auditData.setCurrentValue(currentValue);
						auditData.setAppUserId(dataList.get(0).getAppUserId());
						auditDataList.add(auditData);

					}
				}
				break;
			case APP_VARIABLE:
				if (operationType == EnumOperationType.UPDATE) {
					auditMessage = String.valueOf(entityDataList.get(0).getAppVariableKey()).toUpperCase() + " updated";
					currentValue = auditMessage + " by " + loggedInUser;
				}
				AuditDbData auditData = new AuditDbData();
				auditData.setEntityName(entityName.getValue());
				auditData.setEntityValue(String.valueOf(entityDataList.get(0).getAppVariableKey()));
				auditData.setAuditMessage(auditMessage.trim());
				auditData.setPreviousValue(prevValue);
				auditData.setCurrentValue(currentValue);
				auditDataList.add(auditData);
				break;
			case QUEUE:
				if (operationType == EnumOperationType.INSERT) {
					auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_QUEUE_INSERT_MESSAGE);
				}
				AuditDbData auditDbData = new AuditDbData();
				auditDbData.setEntityName(entityName.getValue());
				auditDbData.setEntityValue(String.valueOf(entityDataList.get(0).getQueueNameCde()));
				auditDbData.setAuditMessage(auditMessage.trim());
				auditDataList.add(auditDbData);
				break;
			default:
				break;
			}
			try {
				addAudit(auditDataList, loggedInUser, tenantId);
			} catch (WorkbenchException e) {
				logger.error("Exception occured while adding audit", e);
			}
		}

	}

	// Async method should be public and not called from same class
	@Async
	public void addAuditDetailsAsync(List<EntityDbData> prevEntityDbDataList, List<EntityDbData> latestEntityDbDataList,
			EnumEntityType entityName, EnumOperationType operationType, String loggedInUser, String tenantId) {
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		String auditMessage = "";
		String currentValue = "";
		String prevValue = "";
		List<Long> prevInputDataList = new ArrayList<Long>();
		List<Long> latestInputDataList = new ArrayList<Long>();
		try {
			if (ListUtility.hasValue(latestEntityDbDataList)) {
				switch (entityName) {
				case ACTION:
					for (EntityDbData entityData : latestEntityDbDataList) {
						List<DocAuditDbData> latestDataList = getDocAuditValues(entityName,
								entityData.getDocActionRelIdList().get(0), tenantId);
						if (entityData.isTaskStatusUpdate()) {
							prevValue = "Status: " + entityData.getTaskStatusTxt();
							currentValue = "Status: " + latestDataList.get(0).getTaskStatusTxt();
						}
						if (entityData.isActionResultUpdated()) {
							currentValue = getOverFlowHidden(currentValue, "Result",
									latestDataList.get(0).getActionResult());
							prevValue = getOverFlowHidden(prevValue, "Result", entityData.getActionResult());
						}
						if (entityData.isSnapShotUpdated()) {
							currentValue = getOverFlowHidden(currentValue, "Summary",
									latestDataList.get(0).getSnapShot());
							prevValue = getOverFlowHidden(prevValue, "Summary", entityData.getSnapShot());
						}
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ACTION_UPDATE_MESSAGE);
						auditMessage = auditMessage.replace(WorkbenchConstants.ENTITY_IDENTIFIER,
								latestDataList.get(0).getActionNameTxt());
						AuditDbData auditData = new AuditDbData();
						auditData.setEntityName(entityName.getValue());
						auditData.setEntityValue(String.valueOf(entityData.getDocActionRelIdList().get(0)));
						auditData.setAuditMessage(auditMessage.trim());
						auditData.setPreviousValue(prevValue);
						auditData.setCurrentValue(currentValue);
						auditData.setDocId(latestDataList.get(0).getDocId());
						auditDataList.add(auditData);
					}
					break;
				case ATTRIBUTE:
					if (ListUtility.hasValue(latestEntityDbDataList.get(0).getDocAttrRelIdList())) {
						prevInputDataList = ListUtility.hasValue(prevEntityDbDataList.get(0).getDocAttrRelIdList())
								? prevEntityDbDataList.get(0).getDocAttrRelIdList()
								: new ArrayList<>();
						latestInputDataList = ListUtility.hasValue(latestEntityDbDataList.get(0).getDocAttrRelIdList())
								? latestEntityDbDataList.get(0).getDocAttrRelIdList()
								: new ArrayList<>();
						if (prevInputDataList.size() == latestInputDataList.size()) {
							auditDataList.addAll(fetchAuditListForAttribute(prevInputDataList, latestInputDataList,
									EnumEntityType.DOC_ATTRIBUTE, operationType, tenantId));
						}

					}
					if (ListUtility.hasValue(latestEntityDbDataList.get(0).getAttachAttrRelIdList())) {
						prevInputDataList = ListUtility.hasValue(prevEntityDbDataList.get(0).getAttachAttrRelIdList())
								? prevEntityDbDataList.get(0).getAttachAttrRelIdList()
								: new ArrayList<>();
						latestInputDataList = ListUtility
								.hasValue(latestEntityDbDataList.get(0).getAttachAttrRelIdList())
										? latestEntityDbDataList.get(0).getAttachAttrRelIdList()
										: new ArrayList<>();
						if (prevInputDataList.size() == latestInputDataList.size()) {
							auditDataList.addAll(fetchAuditListForAttribute(prevInputDataList, latestInputDataList,
									EnumEntityType.ATTACH_ATTRIBUTE, operationType, tenantId));
						}
					}
					if (latestEntityDbDataList.get(0).getAttrSourceId() > 0) {
						if (operationType == EnumOperationType.INSERT) {
							auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ATTRIBUTE_MESSAGE);
							currentValue = "Document attribute source added";
						}
						AuditDbData auditDbData = new AuditDbData();
						auditDbData.setEntityName(entityName.getValue());
						auditDbData.setEntityValue(String.valueOf(latestEntityDbDataList.get(0).getAttrSourceId()));
						auditDbData.setAuditMessage(auditMessage
								.replace(WorkbenchConstants.ATTR_ENTITY, EnumEntityType.DOC_ATTRIBUTE.getValue())
								.trim());
						auditDbData.setPreviousValue(prevValue);
						auditDbData.setCurrentValue(currentValue);
						auditDbData.setDocId(latestEntityDbDataList.get(0).getDocId());
						auditDataList.add(auditDbData);
					}

					break;
				case CASE_OWNER_ASSIGNMENT:
				case CASE_REVIEWER_ASSIGNMENT:
					auditMessage = PropertyManager.getInstance()
							.getProperty(PROP_NAME_AUDIT_CASE_ASSIGNMENT_UPDATE_MESSAGE);
					String roleTypeTxt = (entityName == EnumEntityType.CASE_OWNER_ASSIGNMENT)
							? WorkbenchConstants.ROLE_TYPE_TXT_OWNER
							: WorkbenchConstants.ROLE_TYPE_TXT_REVIEWER;
					auditMessage=auditMessage.replace(WorkbenchConstants.ROLE_TYPE_TXT_PLACEHOLDER, roleTypeTxt);
					prevInputDataList = ListUtility.hasValue(prevEntityDbDataList.get(0).getDocAppUserRelIdList())
							? prevEntityDbDataList.get(0).getDocAppUserRelIdList()
							: new ArrayList<>();
					latestInputDataList = ListUtility.hasValue(latestEntityDbDataList.get(0).getDocAppUserRelIdList())
							? latestEntityDbDataList.get(0).getDocAppUserRelIdList()
							: new ArrayList<>();
					if (prevInputDataList.size() == latestInputDataList.size()) {
						for (int i = 0; i < latestInputDataList.size(); i++) {
							List<DocAuditDbData> latestDataList = getDocAuditValues(entityName,
									latestInputDataList.get(i), tenantId);
							List<DocAuditDbData> prevDataList = getDocAuditValues(entityName, prevInputDataList.get(i),
									tenantId);
							prevValue = prevDataList.get(0).getUserFullName();
							currentValue = latestDataList.get(0).getUserFullName();
							AuditDbData auditData = new AuditDbData();
							auditData.setEntityName(entityName.getValue());
							auditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
							auditData.setAuditMessage(auditMessage.trim());
							auditData.setPreviousValue(prevValue);
							auditData.setCurrentValue(currentValue);
							auditData.setDocId(latestDataList.get(0).getDocId());
							auditDataList.add(auditData);
						}
					}
					break;
				case ROLE:
					auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ROLE_UPDATE_MESSAGE);
					prevInputDataList = ListUtility.hasValue(prevEntityDbDataList.get(0).getAppUserRoleRelIdList())
							? prevEntityDbDataList.get(0).getAppUserRoleRelIdList()
							: new ArrayList<>();
					latestInputDataList = ListUtility.hasValue(latestEntityDbDataList.get(0).getAppUserRoleRelIdList())
							? latestEntityDbDataList.get(0).getAppUserRoleRelIdList()
							: new ArrayList<>();
					if (prevInputDataList.size() == latestInputDataList.size()) {
						for (int i = 0; i < latestInputDataList.size(); i++) {
							List<UserAuditDbData> latestDataList = getUserAuditValues(entityName,
									latestInputDataList.get(i), tenantId);
							List<UserAuditDbData> prevDataList = getUserAuditValues(entityName,
									prevInputDataList.get(i), tenantId);
							prevValue = prevDataList.get(0).getRoleTypeTxt();
							currentValue = latestDataList.get(0).getRoleTypeTxt();
							AuditDbData auditData = new AuditDbData();
							auditData.setEntityName(entityName.getValue());
							auditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
							auditData.setAuditMessage(auditMessage.trim());
							auditData.setPreviousValue(prevValue);
							auditData.setCurrentValue(currentValue);
							auditData.setAppUserId(latestDataList.get(0).getAppUserId());
							auditDataList.add(auditData);
						}
					}
					break;
				default:
					break;
				}

				addAudit(auditDataList, loggedInUser, tenantId);
			}
		} catch (Exception e) {
			logger.info("Error occured in addAuditDetailsAsync method : " + e.getMessage());
		}

	}

	public void addAudit(List<AuditDbData> auditDataList, String loggedInUser, String tenantId)
			throws WorkbenchException {
		try {
			List<AuditDbData> auditDbDataList = new ArrayList<AuditDbData>();
			auditDbDataList = auditDataAccess.addAudit(auditDataList, loggedInUser, tenantId);
			if (ListUtility.hasValue(auditDbDataList)) {
				for (int i = 0; i < auditDbDataList.size(); i++) {
					if (auditDbDataList.get(i).getDocId() > 0) {
						auditDataAccess.addDocAuditRel(auditDbDataList.get(i), loggedInUser, tenantId);
					} else if (auditDbDataList.get(i).getAppUserId() > 0) {
						auditDataAccess.addUserAuditRel(auditDbDataList.get(i), loggedInUser, tenantId);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error occured while adding audit message", e);
			throw new WorkbenchException("Error occurred while adding audit message", e);
		}
	}

	private List<DocAuditDbData> getDocAuditValues(EnumEntityType entityName, long entityValue, String tenantId) {
		List<DocAuditDbData> docAuditDataList = new ArrayList<DocAuditDbData>();
		String columnName = "";
		try {
			switch (entityName) {
			case ACTION:
				columnName = "doc_action_rel_id";
				break;
			case DOCUMENT:
				columnName = "doc_id";
				break;
			case ATTACHMENT:
				columnName = "doc_attachment_rel_id";
				break;
			case ATTACHMENT_REL:
				columnName = "attachment_id";
				break;
			case DOC_ATTRIBUTE:
				columnName = "doc_attr_rel_id";
				break;
			case ATTACH_ATTRIBUTE:
				columnName = "attachment_attr_rel_id";
				break;
			case EMAIL:
				columnName = "email_outbound_id";
				break;
			case CASE_OWNER_ASSIGNMENT:
			case CASE_REVIEWER_ASSIGNMENT:
				columnName = "doc_app_user_rel_id";
				break;
			default:
				break;
			}
			docAuditDataList = auditDataAccess.getDocValues(entityName, columnName, entityValue, tenantId);
		} catch (Exception e) {
			logger.error("Exception occured while getting doc values", e);
		}
		return docAuditDataList;
	}

	private List<UserAuditDbData> getUserAuditValues(EnumEntityType entityName, Long entityValue, String tenantId) {
		List<UserAuditDbData> userAuditDataList = new ArrayList<UserAuditDbData>();
		String columnName = "";
		try {
			switch (entityName) {
			case ROLE:
				columnName = "app_user_role_rel_id";
				break;
			case QUEUE_ASSIGNMENT:
				columnName = "app_user_queue_rel_id";
				break;
			case USER:
				columnName = "app_user_id";
				break;
			default:
				break;
			}
			userAuditDataList = auditDataAccess.getUserValues(entityName, columnName, entityValue, tenantId);
		} catch (Exception e) {
			logger.error("Exception occured while getting user values", e);
		}
		return userAuditDataList;
	}

	private void fetchAuditListForTabularAttribute(String entityValue, List<DocAuditDbData> prevDataList,
			List<DocAuditDbData> latestDataList, EnumOperationType operationType, EnumEntityType entityName,
			List<AuditDbData> auditDataList) {
		String auditMessage = "";
		String currentValue = "";
		String prevValue = "";
		String oldValue = ListUtility.hasValue(prevDataList) ? prevDataList.get(0).getAttrValue() : "";
		String latestValue = ListUtility.hasValue(latestDataList) ? latestDataList.get(0).getAttrValue() : "";
		if (StringUtility.hasValue(oldValue)) {
			if (operationType == EnumOperationType.UPDATE) {
				auditMessage = PropertyManager.getInstance()
						.getProperty(PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_UPDATE_MESSAGE);
				prevValue = oldValue;
				currentValue = latestValue;
			} else if (operationType == EnumOperationType.DELETE) {
				boolean isTableDeleted = true;
				if (StringUtility.hasValue(latestValue)) {
					List<AttributeDbData> dataList = AttributeHelper.convertJsonStringToMultiAttr(latestValue)
							.getAttributes();
					if (dataList.stream().filter(attrData -> attrData.getEndDtm() == null).count() < dataList.size()) {
						auditMessage = PropertyManager.getInstance()
								.getProperty(PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_UPDATE_MESSAGE);
						prevValue = oldValue;
						currentValue = latestValue;
						isTableDeleted = false;
					}
				}
				if (isTableDeleted) {
					auditMessage = PropertyManager.getInstance()
							.getProperty(PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_DELETE_MESSAGE);
					prevValue = oldValue;
				}
			} else if (operationType == EnumOperationType.INSERT) {
				// At this stage new row to the table would have added
				auditMessage = PropertyManager.getInstance()
						.getProperty(PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_UPDATE_MESSAGE);
				prevValue = oldValue;
				currentValue = latestValue;
			}
			auditDataList.add(createAuditDbData(entityName, entityValue, prevDataList.get(0).getDocId(), auditMessage,
					prevValue, currentValue));
		} else {
			if (operationType == EnumOperationType.INSERT) {
				auditMessage = PropertyManager.getInstance()
						.getProperty(PROP_NAME_AUDIT_MULTI_ATTRIBUTE_TABLE_INSERT_MESSAGE);
				currentValue = latestValue;
			}

			auditDataList.add(createAuditDbData(entityName, entityValue, latestDataList.get(0).getDocId(), auditMessage,
					prevValue, currentValue));
		}
	}

	private AuditDbData createAuditDbData(EnumEntityType entityName, String entityValue, long docId,
			String auditMessage, String prevValue, String currentValue) {

		AuditDbData multiAuditData = new AuditDbData();
		multiAuditData.setEntityName(entityName.getValue());
		multiAuditData.setEntityValue(entityValue);
		auditMessage = auditMessage.replaceFirst(WorkbenchConstants.ATTR_ENTITY,
				entityName.getValue().replace(EnumEntityType.ATTRIBUTE.getValue(), "")).trim();
		String multiAttrGrpName = "";
		if (StringUtility.hasValue(prevValue)) {
			multiAttrGrpName = AttributeHelper.convertJsonStringToMultiAttr(prevValue).getAttrNameTxt();
		} else if (StringUtility.hasValue(currentValue)) {
			multiAttrGrpName = AttributeHelper.convertJsonStringToMultiAttr(currentValue).getAttrNameTxt();
		}
		auditMessage = auditMessage.replace(WorkbenchConstants.ENTITY_IDENTIFIER, multiAttrGrpName).trim();
		multiAuditData.setAuditMessage(auditMessage);
		multiAuditData.setPreviousValue(prevValue);
		multiAuditData.setCurrentValue(currentValue);
		multiAuditData.setDocId(docId);
		return multiAuditData;
	}

	private List<AuditDbData> fetchAuditListForAttribute(List<Long> prevInputDataList, List<Long> latestInputDataList,
			EnumEntityType entityName, EnumOperationType operationType, String tenantId) {
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		String auditMessage = "";
		String message = "";
		String currentValue = "";
		String prevValue = "";
		for (int i = 0; i < prevInputDataList.size(); i++) {
			AuditDbData auditData = new AuditDbData();
			auditData.setEntityName(entityName.getValue());
			List<DocAuditDbData> prevDataList = new ArrayList<>();
			List<DocAuditDbData> latestDataList = new ArrayList<>();
			if (prevInputDataList.get(i) > 0)
				prevDataList = getDocAuditValues(entityName, prevInputDataList.get(i), tenantId);
			if (latestInputDataList.get(i) > 0)
				latestDataList = getDocAuditValues(entityName, latestInputDataList.get(i), tenantId);
			if (prevDataList.size() > 0) {
				if (!(prevDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE)
						|| prevDataList.get(0).getAttrNameTxt()
								.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE_TABLE))) {
					if (operationType == EnumOperationType.UPDATE) {
						message = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ATTRIBUTE_UPDATE_MESSAGE);
						auditMessage = message.replace(WorkbenchConstants.ENTITY_IDENTIFIER,
								latestDataList.get(0).getAttrNameTxt());
						prevValue = prevDataList.get(0).getAttrValue();
						currentValue = latestDataList.get(0).getAttrValue();
						auditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
					} else if (operationType == EnumOperationType.DELETE) {
						auditMessage = PropertyManager.getInstance()
								.getProperty(PROP_NAME_AUDIT_ATTRIBUTE_DELETE_MESSAGE);
						prevValue = prevDataList.get(0).getAttrNameTxt();
						auditData.setEntityValue(String.valueOf(prevInputDataList.get(i)));
					}
					auditData
							.setAuditMessage(auditMessage
									.replace(WorkbenchConstants.ATTR_ENTITY,
											entityName.getValue().replace(EnumEntityType.ATTRIBUTE.getValue(), ""))
									.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(prevDataList.get(0).getDocId());
					auditDataList.add(auditData);
				} else if (prevDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE)) {
					String oldValue = ListUtility.hasValue(prevDataList) ? prevDataList.get(0).getAttrValue() : "";
					String latestValue = ListUtility.hasValue(latestDataList) ? latestDataList.get(0).getAttrValue()
							: "";
					List<List<AttributeDbData>> multiAttrParamDataLists = new ArrayList<>();
					if (StringUtility.hasValue(oldValue) && StringUtility.hasValue(latestValue))
						multiAttrParamDataLists = AttributeHelper.getMultiAttrDataModified(oldValue, latestValue,
								operationType);
					if (ListUtility.hasValue(multiAttrParamDataLists) && multiAttrParamDataLists.size() == 2) {
						List<AttributeDbData> oldMultiAttrParameterDatas = multiAttrParamDataLists.get(1);
						List<AttributeDbData> newMultiAttrParameterDatas = multiAttrParamDataLists.get(0);
						if (oldMultiAttrParameterDatas.size() > 0) {
							for (int j = 0; j < oldMultiAttrParameterDatas.size(); j++) {
								if (operationType == EnumOperationType.UPDATE) {
									message = PropertyManager.getInstance()
											.getProperty(PROP_NAME_AUDIT_ATTRIBUTE_UPDATE_MESSAGE);
									auditMessage = message.replace(WorkbenchConstants.ENTITY_IDENTIFIER,
											newMultiAttrParameterDatas.get(j).getAttrNameTxt());
									prevValue = oldMultiAttrParameterDatas.get(j).getAttrValue();
									currentValue = newMultiAttrParameterDatas.get(j).getAttrValue();
								} else {
									auditMessage = PropertyManager.getInstance()
											.getProperty(PROP_NAME_AUDIT_ATTRIBUTE_DELETE_MESSAGE);
									prevValue = oldMultiAttrParameterDatas.get(j).getAttrNameTxt();
								}
								AuditDbData multiAuditData = new AuditDbData();
								multiAuditData.setEntityName(entityName.getValue());
								multiAuditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
								multiAuditData
										.setAuditMessage(
												auditMessage
														.replace(WorkbenchConstants.ATTR_ENTITY,
																entityName.getValue().replace(
																		EnumEntityType.ATTRIBUTE.getValue(), ""))
														.trim());
								multiAuditData.setPreviousValue(prevValue);
								multiAuditData.setCurrentValue(currentValue);
								multiAuditData.setDocId(latestDataList.get(0).getDocId());
								auditDataList.add(multiAuditData);
							}
						} else {

							for (int j = 0; j < newMultiAttrParameterDatas.size(); j++) {
								if (operationType == EnumOperationType.INSERT) {
									auditMessage = PropertyManager.getInstance()
											.getProperty(PROP_NAME_AUDIT_ATTRIBUTE_MESSAGE);
									currentValue = newMultiAttrParameterDatas.get(j).getAttrNameTxt();
								}
								AuditDbData multiAuditData = new AuditDbData();
								multiAuditData.setEntityName(entityName.getValue());
								multiAuditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
								multiAuditData
										.setAuditMessage(
												auditMessage
														.replace(WorkbenchConstants.ATTR_ENTITY,
																entityName.getValue().replace(
																		EnumEntityType.ATTRIBUTE.getValue(), ""))
														.trim());
								multiAuditData.setPreviousValue(prevValue);
								multiAuditData.setCurrentValue(currentValue);
								multiAuditData.setDocId(latestDataList.get(0).getDocId());
								auditDataList.add(multiAuditData);
							}
						}
					}

				} else if (prevDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE_TABLE)) {

					fetchAuditListForTabularAttribute(String.valueOf(latestInputDataList.get(i)), prevDataList,
							latestDataList, operationType, entityName, auditDataList);

				}
			} else if (latestDataList.size() > 0) {
				if (!(latestDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE)
						|| latestDataList.get(0).getAttrNameTxt()
								.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE_TABLE))) {
					if (operationType == EnumOperationType.INSERT) {
						auditMessage = PropertyManager.getInstance().getProperty(PROP_NAME_AUDIT_ATTRIBUTE_MESSAGE);
						currentValue = latestDataList.get(0).getAttrNameTxt();
						auditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
					}
					auditData
							.setAuditMessage(auditMessage
									.replace(WorkbenchConstants.ATTR_ENTITY,
											entityName.getValue().replace(EnumEntityType.ATTRIBUTE.getValue(), ""))
									.trim());
					auditData.setPreviousValue(prevValue);
					auditData.setCurrentValue(currentValue);
					auditData.setDocId(latestDataList.get(0).getDocId());
					auditDataList.add(auditData);
				} else if (latestDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE)) {
					List<AttributeDbData> multiAttrDbDataList = AttributeHelper
							.convertJsonStringToMultiAttr(latestDataList.get(0).getAttrValue()).getAttributes();
					if (ListUtility.hasValue(multiAttrDbDataList)) {
						for (int j = 0; j < multiAttrDbDataList.size(); j++) {
							if (operationType == EnumOperationType.INSERT) {
								auditMessage = PropertyManager.getInstance()
										.getProperty(PROP_NAME_AUDIT_ATTRIBUTE_MESSAGE);
								currentValue = multiAttrDbDataList.get(j).getAttrNameTxt();
							}
							AuditDbData multiAuditData = new AuditDbData();
							multiAuditData.setEntityName(entityName.getValue());
							multiAuditData.setEntityValue(String.valueOf(latestInputDataList.get(i)));
							multiAuditData
									.setAuditMessage(
											auditMessage
													.replace(WorkbenchConstants.ATTR_ENTITY,
															entityName.getValue()
																	.replace(EnumEntityType.ATTRIBUTE.getValue(), ""))
													.trim());
							multiAuditData.setPreviousValue(prevValue);
							multiAuditData.setCurrentValue(currentValue);
							multiAuditData.setDocId(latestDataList.get(0).getDocId());
							auditDataList.add(multiAuditData);
						}
					}
				} else if (latestDataList.get(0).getAttrNameTxt()
						.equalsIgnoreCase(WorkbenchConstants.ATTR_NAME_TXT_MULTI_ATTRIBUTE_TABLE)) {
					fetchAuditListForTabularAttribute(String.valueOf(latestInputDataList.get(i)), prevDataList,
							latestDataList, operationType, entityName, auditDataList);
				}
			}
		}
		return auditDataList;
	}

	private static String getOverFlowHidden(String toAppend, String key, String in) {
		if (StringUtility.hasValue(in)) {
			int length = in.length();
			if (length > 10) {
				in = in.substring(0, 10) + "...";
			}
			toAppend += " " + key + ": " + in + " (" + length + " chars )";
		}
		return toAppend;
	}
}
