/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.action;

import java.sql.PreparedStatement;
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
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

/**
 * 
 * ActionDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */

@Component
@Profile("oracle")
public class OracleDocActionDataAccess extends DocActionDataAccess implements IDocActionDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(OracleDocActionDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String UPDATE_ACTION_TASK_SQL = "UPDATE_ACTION_TASK_CALLABLE";

	@Value("${getActionTask}")
	private String getActionTaskSql;

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<EntityDbData> updateActionTask(List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EntityDbData> entityDatasList = new ArrayList<>();
		try {
			for (int i = 0; i < actionParamAttrMappingDbDataList.size(); i++) {
				List<EntityDbData> entityDataList = null;
				ActionParamAttrMappingDbData dbData = actionParamAttrMappingDbDataList.get(i);

				SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(UPDATE_ACTION_TASK_SQL);

				MapSqlParameterSource mapParam = new MapSqlParameterSource();
				mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
				mapParam.addValue("I_DOC_ACTION_REL_ID", dbData.getDocActionRelId());
				mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
				mapParam.addValue("I_TASK_STATUS_CDE", dbData.getTaskStatusCde());
				mapParam.addValue("I_ACTION_RESULT", dbData.getActionResult());
				mapParam.addValue("I_SNAP_SHOT", dbData.getSnapShot());

				Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
				long docActionRelId = 0;
				entityDataList = new ArrayList<>();
				EntityDbData dbData1 = new EntityDbData();
				List<Long> docActionRelIdList = new ArrayList<Long>();
				docActionRelId = Long.valueOf((String) sqlResponse.get("O_DOC_ACTION_REL_ID"));
				if (docActionRelId > 0) {
					docActionRelIdList.add(docActionRelId);
					dbData1.setDocActionRelIdList(docActionRelIdList);
					dbData1.setTaskStatusCde(Long.valueOf((String) sqlResponse.get("O_TASK_STATUS_CDE")));
					dbData1.setTaskStatusTxt((String) sqlResponse.get("O_TASK_STATUS_TXT"));
					dbData1.setActionResult((String) sqlResponse.get("O_ACTION_RESULT"));
					dbData1.setSnapShot((String) sqlResponse.get("O_SNAP_SHOT"));
					entityDatasList.add(dbData1);
				}

				if (ListUtility.hasValue(entityDataList)) {
					if (dbData.getTaskStatusCde() > 0) {
						entityDataList.get(0).setTaskStatusUpdate(true);
					}
					if (StringUtility.hasValue(dbData.getActionResult())) {
						entityDataList.get(0).setActionResultUpdated(true);
					}
					if (StringUtility.hasValue(dbData.getSnapShot())) {
						entityDataList.get(0).setSnapShotUpdated(true);
					}
					entityDatasList.add(entityDataList.get(0));
				}
			}
		} catch (Exception ex) {
			logger.error("Error occured while updating action task in database", ex);
			throw new WorkbenchException("Error occured while updating action task in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateActionTaskSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateActionTaskSql" + "," + timeElapsed + ",secs");
		return entityDatasList;
	}

	public List<ActionParamAttrMappingDbData> getActionTaskList(ActionParamAttrMappingDbData filterData,
			int queueNameCde, String taskStatusOp) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList;
		String sqlQuery = getActionTaskSql;
		String sqlWhere = "";
		int offset = 0;
		if (filterData.getPageNumber() > 1) {
			offset = pageSize * (filterData.getPageNumber() - 1);
		}
		if (filterData.getDocId() > 0) {
			sqlWhere += "DOC_ID=" + filterData.getDocId();
		}
		if (filterData.getActionNameCde() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "ACTION_NAME_CDE=" + filterData.getActionNameCde();
		}
		if (filterData.getTaskStatusCde() > EnumTaskStatus.UNDEFINED.getValue() && taskStatusOp != null) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "TASK_STATUS_CDE" + taskStatusOp + filterData.getTaskStatusCde();
		}
		if (queueNameCde > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "QUEUE_NAME_CDE=" + queueNameCde;
		}
		if (sqlWhere != "") {
			// Works with oracle 12c version
			// TODO Enable and check it
			// sqlQuery += " WHERE " + sqlWhere + "ORDER BY DOC_ID OFFSET "+offset+" ROWS
			// FETCH NEXT "+pageSize+" ROWS ONLY";
		}
		try {
			actionParamAttrMappingDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new PreparedStatementSetter() {
						public void setValues(PreparedStatement preparedStatement) throws SQLException {
							preparedStatement.setString(1, SessionHelper.getTenantId());
							preparedStatement.setString(2, SessionHelper.getTenantId());
						}
					}, new RowMapper<ActionParamAttrMappingDbData>() {
						@Override
						public ActionParamAttrMappingDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ActionParamAttrMappingDbData dbData = new ActionParamAttrMappingDbData();
							dbData.setDocActionRelId(rs.getLong("doc_action_rel_id"));
							dbData.setActionParamAttrRelId(rs.getLong("action_param_attr_rel_id"));
							dbData.setDocId(rs.getLong("doc_id"));
							dbData.setActionNameCde(rs.getInt("action_name_cde"));
							dbData.setActionNameTxt(rs.getString("action_name_txt"));
							dbData.setParamNameCde(rs.getInt("param_name_cde"));
							dbData.setParamNameTxt(rs.getString("param_name_txt"));
							dbData.setParamValue(rs.getString("param_value"));
							dbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							dbData.setTaskStatusTxt(rs.getString("task_status_txt"));
							dbData.setTaskTypeCde(rs.getInt("task_Type_cde"));
							dbData.setActionResult(rs.getString("action_result"));
							dbData.setSnapShot(rs.getString("snap_shot"));
							dbData.setCreateByUserLoginId(rs.getString("create_by_user_login_id"));
							dbData.setCreateByUserTypeCde(rs.getInt("create_by_user_type_cde"));
							dbData.setCreateByUserFullName(rs.getString("create_by_user_full_name"));
							dbData.setCreateByUserTypeTxt(rs.getString("create_by_user_type_txt"));
							dbData.setCreateDtm(rs.getTimestamp("create_dtm"));
							dbData.setLastModDtm(rs.getTimestamp("last_mod_dtm"));
							return dbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching task status val data from database", ex);
			throw new WorkbenchException("Error occured while fetching task status val data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getActionTaskSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getActionTaskSql" + "," + timeElapsed + ",secs");
		return actionParamAttrMappingDbDataList;
	}

}
