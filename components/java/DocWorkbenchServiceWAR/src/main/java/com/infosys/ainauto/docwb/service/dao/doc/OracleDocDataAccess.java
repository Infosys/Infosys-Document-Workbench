/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.doc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAttrWrapperDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
@Profile("oracle")
public class OracleDocDataAccess extends DocDataAccess implements IDocDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(OracleDocDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String UPDATE_DOC_ACTION_STATUS_SQL = "U_DOC_ACTION_STATUS_CALLABLE";

	@Value("${getDocumentList}")
	private String getDocumentListSql;

	@Value("${getDocOnFilter}")
	private String getDocOnFilterSql;

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<EntityDbData> updateDocActionStatus(DocumentDbData documentDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EntityDbData> entityList = new ArrayList<>();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(UPDATE_DOC_ACTION_STATUS_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_TASK_STATUS_CDE", documentDbData.getTaskStatusCde());
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_DOC_ID", documentDbData.getDocId());
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			EntityDbData entityData = new EntityDbData();
			entityData.setDocId(Long.valueOf((String) sqlResponse.get("O_DOC_ID")));
			entityData.setTaskStatusCde(Long.valueOf((String) sqlResponse.get("O_TASK_STATUS_CDE")));
			entityData.setTaskStatusTxt((String) sqlResponse.get("O_TASK_STATUS_TXT"));
			entityList.add(entityData);
		} catch (Exception ex) {
			logger.error("Error occured while update DocActionStatus in database", ex);
			throw new WorkbenchException("Error occured while updating Document task status in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateDocActionStatusSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateDocActionStatusSql" + "," + timeElapsed + ",secs");
		return entityList;
	}

	public List<DocAttrWrapperDbData> getDocumentList(DocumentDbData documentDbDataIn) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = new ArrayList<DocAttrWrapperDbData>();
		String sqlQuery = getDocumentListSql;
		String sqlQueryForFilter = getDocOnFilterSql.trim();
		String attrFilter = "";
		String sqlWhere = "";
		String sortOrder = StringUtility.hasValue(documentDbDataIn.getSortOrder()) ? documentDbDataIn.getSortOrder()
				: "DESC";
		int offset = 0;
		if (documentDbDataIn.getPageNumber() > 1) {
			offset = pageSize * (documentDbDataIn.getPageNumber() - 1);
		}
		if (StringUtility.hasValue(documentDbDataIn.getAttrNameCdes())) {
			attrFilter = "AND ATTR.ATTR_NAME_CDE IN (" + documentDbDataIn.getAttrNameCdes() + ") ";
		}
		if (documentDbDataIn.getTaskStatusCde() > EnumTaskStatus.UNDEFINED.getValue()
				&& (documentDbDataIn.getTaskStatusOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "TASK_STATUS_CDE" + documentDbDataIn.getTaskStatusOperator()
					+ documentDbDataIn.getTaskStatusCde();
		}
		if ((documentDbDataIn.getHighestEventTypeCde() > 0)
				&& (documentDbDataIn.getHighestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "HIGHEST_EVENT_TYPE_CDE" + documentDbDataIn.getHighestEventTypeOperator()
					+ documentDbDataIn.getHighestEventTypeCde();
		}
		if ((documentDbDataIn.getLatestEventTypeCde() > 0) && (documentDbDataIn.getLatestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LATEST_EVENT_TYPE_CDE" + documentDbDataIn.getLatestEventTypeOperator()
					+ documentDbDataIn.getLatestEventTypeCde();
		}
		if (documentDbDataIn.getLockStatusCde() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LOCK_STATUS_CDE=" + documentDbDataIn.getLockStatusCde();
		}
		if (documentDbDataIn.getAppUserId() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID=" + documentDbDataIn.getAppUserId();
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_UNASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NULL";
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_ASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NOT NULL";
		}
		if (documentDbDataIn.getDocId() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "DOC_ID=" + documentDbDataIn.getDocId();
		}

		if (sqlWhere != "") {
			// Works with oracle 12c version
			// TODO Enable and check it
			// sqlQueryForFilter += " WHERE " + sqlWhere + " ORDER BY DOC_ID " + sortOrder +
			// " OFFSET " + offset
			// + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
			sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_FILTER, ("(" + sqlQueryForFilter + ") "));
			sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);
			sqlQuery += " ORDER BY DOC.DOC_ID " + sortOrder + " , ATTRIB.ATTR_NAME_CDE";
		}
		try {
			docAttrWrapperDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { documentDbDataIn.getQueueNameCde(), SessionHelper.getTenantId() },
					new RowMapper<DocAttrWrapperDbData>() {
						@Override
						public DocAttrWrapperDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							DocAttrWrapperDbData docAttrWrapperDbData = new DocAttrWrapperDbData();

							DocumentDbData documentDbData = new DocumentDbData();
							documentDbData.setDocId(rs.getLong("doc_id"));
							documentDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							documentDbData.setDocLocation(rs.getString("doc_location"));
							documentDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							documentDbData.setTaskStatusTxt(rs.getString("task_status_txt"));
							documentDbData.setHighestEventTypeCde(rs.getInt("highest_event_type_cde"));
							documentDbData.setLatestEventTypeCde(rs.getInt("latest_event_type_cde"));
							documentDbData.setLockStatusCde(rs.getInt("lock_status_cde"));
							documentDbData.setLockStatusTxt(rs.getString("lock_status_txt"));
							documentDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							documentDbData.setAttachmentCount(rs.getInt("attachment_count"));
							documentDbData.setAppUserId(rs.getLong("app_user_id"));
							documentDbData.setAssignedTo(rs.getString("assigned_to"));
							documentDbData.setCreateDtm(rs.getString("create_dtm"));
							docAttrWrapperDbData.setDocumentDbData(documentDbData);

							AttributeDbData attributeDbData = new AttributeDbData();
							if (rs.getInt("doc_attr_rel_id") > 0) {
								attributeDbData.setId(rs.getInt("doc_attr_rel_id"));
								attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
								attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
								attributeDbData.setAttrValue(rs.getString("attr_value"));
								attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
								attributeDbData.setExtractTypeTxt(rs.getString("extract_type_txt"));
								attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
										.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
												RoundingMode.HALF_EVEN)
										.floatValue());
								docAttrWrapperDbData.setAttributeDbData(attributeDbData);
							}
							return docAttrWrapperDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching document list data from database", ex);
			throw new WorkbenchException("Error occured while fetching document list data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocumentListSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocumentListSql" + "," + timeElapsed + ",secs");
		if (StringUtility.hasValue(documentDbDataIn.getAttachmentAttrNameCdes())) {
			populateAttachmentAttributes(documentDbDataIn, docAttrWrapperDbDataList);
		}
		return docAttrWrapperDbDataList;
	}

}
