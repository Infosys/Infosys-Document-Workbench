/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
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
@Profile("default")
public class DocActionDataAccess implements IDocActionDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(DocActionDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${getActionMapping}")
	private String getActionMappingSql;

	@Value("${getActionParamMapping}")
	private String getActionParamMappingSql;

	@Value("${addActionToDocument}")
	private String addActionToDocumentSql;

	@Value("${addActionParamAttrRel}")
	private String addActionParamAttrRelSql;

	@Value("${getActionTask}")
	private String getActionTaskSql;

	@Value("${updateActionTask}")
	private String updateActionTaskSql;

	@Value("${deleteActionDoc}")
	private String deleteActionSql;

	@Value("${deleteActionParamAttrRel}")
	private String deleteActionSqlAttb;

	@Value("${getActionResult}")
	private String getActionResultSql;

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long addActionToDocument(ActionParamAttrMappingDbData actionParamAttrMappingDbData)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		long docActionRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addActionToDocumentSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, actionParamAttrMappingDbData.getDocId());
					ps.setLong(2, actionParamAttrMappingDbData.getActionNameCde());
					ps.setLong(3, actionParamAttrMappingDbData.getTaskStatusCde());
					ps.setLong(4, actionParamAttrMappingDbData.getTaskTypeCde());
					ps.setString(5, SessionHelper.getLoginUsername());
					ps.setString(6, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			docActionRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while updating rules data in database", ex);
			throw new WorkbenchException("Error occured while updating rules data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addActionToDocumentSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addActionToDocumentSql" + "," + timeElapsed + ",secs");
		return docActionRelId;
	}

	public int addActionParamAttrRel(List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();
			for (int i = 0; i < actionParamAttrMappingDbDataList.size(); i++) {
				ActionParamAttrMappingDbData dbData = actionParamAttrMappingDbDataList.get(i);
				object = new Object[] { dbData.getDocActionRelId(), dbData.getParamNameCde(), dbData.getAttrNameCde(),
						dbData.getAttrNameTxt(), dbData.getParamValue(), SessionHelper.getLoginUsername(),
						SessionHelper.getTenantId() };
				objectList.add(object);
			}
			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(addActionParamAttrRelSql),
					objectList);
			sum = IntStream.of(rowsImpacted).sum();
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while add attributes in database", ex);
			throw new WorkbenchException("Error occured while add attributes in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addActionParamAttrRelSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addActionParamAttrRelSql" + "," + timeElapsed + ",secs");
		return sum;
	}

	public List<EntityDbData> updateActionTask(List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		String query = updateActionTaskSql;
		List<EntityDbData> entityDatasList = new ArrayList<>();
		try {
			Object object[];
			for (int i = 0; i < actionParamAttrMappingDbDataList.size(); i++) {
				List<EntityDbData> entityDataList = null;
				ActionParamAttrMappingDbData dbData = actionParamAttrMappingDbDataList.get(i);
				object = new Object[] {};
				String column = "";
				if (dbData.getActionResult() != null && dbData.getTaskStatusCde() > 0) {
					column = "TASK_STATUS_CDE=?, ACTION_RESULT=?";
					object = new Object[] { dbData.getTaskStatusCde(), dbData.getActionResult(),
							SessionHelper.getLoginUsername(), dbData.getDocActionRelId(), SessionHelper.getTenantId() };
				} else if (dbData.getActionResult() != null) {
					column = "ACTION_RESULT=? ";
					object = new Object[] { dbData.getActionResult(), SessionHelper.getLoginUsername(),
							dbData.getDocActionRelId(), SessionHelper.getTenantId() };
				} else if (dbData.getSnapShot() != null && dbData.getTaskStatusCde() > 0) {
					column = "SNAP_SHOT=?::JSON, TASK_STATUS_CDE=? ";
					object = new Object[] { dbData.getSnapShot(), dbData.getTaskStatusCde(),
							SessionHelper.getLoginUsername(), dbData.getDocActionRelId(), SessionHelper.getTenantId() };
				} else if (dbData.getTaskStatusCde() > 0) {
					column = "TASK_STATUS_CDE=? ";
					object = new Object[] { dbData.getTaskStatusCde(), SessionHelper.getLoginUsername(),
							dbData.getDocActionRelId(), SessionHelper.getTenantId() };
				} else {
					entityDatasList = new ArrayList<>();
					return entityDatasList;
				}

				query = StringUtility.sanitizeSql(query.replace(WorkbenchConstants.COLUMN, column));
				entityDataList = jdbcTemplate.query(query, object, new RowMapper<EntityDbData>() {
					@Override
					public EntityDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
						long docActionRelId = 0;
						EntityDbData dbData = new EntityDbData();
						List<Long> docActionRelIdList = new ArrayList<Long>();
						docActionRelId = rs.getLong("doc_action_rel_id");
						if (docActionRelId > 0) {
							docActionRelIdList.add(docActionRelId);
							dbData.setDocActionRelIdList(docActionRelIdList);
							dbData.setTaskStatusCde(rs.getLong("task_status_cde"));
							dbData.setTaskStatusTxt(rs.getString("task_status_txt"));
							dbData.setActionResult(rs.getString("action_result"));
							dbData.setSnapShot(rs.getString("snap_shot"));
						}
						return dbData;
					}
				});
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

	public List<ActionParamAttrMappingDbData> getActionMappingList() throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList;
		try {
			actionParamAttrMappingDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getActionMappingSql),
					new PreparedStatementSetter() {
						public void setValues(PreparedStatement preparedStatement) throws SQLException {
							preparedStatement.setString(1, SessionHelper.getTenantId());
						}
					},
					// new Object[] { SessionHelper.getTenantId() },
					new RowMapper<ActionParamAttrMappingDbData>() {
						@Override
						public ActionParamAttrMappingDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ActionParamAttrMappingDbData dbData = new ActionParamAttrMappingDbData();

							dbData.setActionNameCde(rs.getInt("action_name_cde"));
							dbData.setActionNameTxt(rs.getString("action_name_txt"));
							dbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							if (StringUtility.hasValue(rs.getString("apam_attr_name_txt"))) {
								dbData.setAttrNameTxt(rs.getString("apam_attr_name_txt"));
							} else {
								dbData.setAttrNameTxt(rs.getString("attrval_attr_name_txt"));
							}
							dbData.setParamNameCde(rs.getInt("param_name_cde"));
							dbData.setParamNameTxt(rs.getString("param_name_txt"));
							return dbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching task status val data from database", ex);
			throw new WorkbenchException("Error occured while fetching task status val data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getActionMappingSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getActionMappingSql" + "," + timeElapsed + ",secs");
		return actionParamAttrMappingDbDataList;
	}

	public long getTotalActionCount(ActionParamAttrMappingDbData filterData, int queueNameCde, String taskStatusOp)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		Long total;
		String sqlQuery = getActionTaskSql;
		String sqlWhere = "";
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
			sqlQuery = "SELECT COUNT(*) FROM ( " + getActionTaskSql + " ) DATA " + "WHERE " + sqlWhere;
		}
		try {
			sqlQuery = StringUtility.sanitizeSql(sqlQuery);
			List<Long> count = jdbcTemplate.query(sqlQuery,
					new Object[] { SessionHelper.getTenantId(), SessionHelper.getTenantId() }, new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			total = count.get(0);

		} catch (Exception ex) {
			logger.error("Error occured while fetching task status count data from database", ex);
			throw new WorkbenchException("Error occured while fetching task status count data from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getActionTaskSql-TotalActionCount query (secs):" + timeElapsed);
		PERF_LOGGER.info("getActionTaskSql-TotalActionCount" + "," + timeElapsed + ",secs");
		return total;

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
			sqlQuery += " WHERE " + sqlWhere + " ORDER BY DOC_ID OFFSET " + offset + " LIMIT " + pageSize;
		}
		try {
			sqlQuery = StringUtility.sanitizeSql(sqlQuery);
			actionParamAttrMappingDbDataList = jdbcTemplate.query(sqlQuery, new PreparedStatementSetter() {
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
					dbData.setQueueNameCde(rs.getInt("queue_name_cde"));
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

	public long deleteActionFromDoc(final long docActionRelId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long docActionRelIdRes = 0;
		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(StringUtility.sanitizeSql(deleteActionSql));
					ps.setString(1, SessionHelper.getLoginUsername());
					ps.setLong(2, docActionRelId);
					ps.setString(3, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowsUpdated = jdbcTemplate.update(psc);
			if (noOfRowsUpdated > 0) {
				docActionRelIdRes = docActionRelId;
			} else {
				logger.info("No rows updated");
			}

		} catch (Exception ex) {
			logger.error("Error occured while deleting action in database", ex);
			throw new WorkbenchException("Error occured while deleting action in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteActionSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteActionSql" + "," + timeElapsed + ",secs");
		return docActionRelIdRes;
	}

	public int deleteActionParamAttrRel(long docActionRelId) throws WorkbenchException {
		long startTime = System.nanoTime();
		int rowsImpacted;
		try {
			rowsImpacted = jdbcTemplate.update(StringUtility.sanitizeSql(deleteActionSqlAttb),
					// new Object[] { SessionHelper.getLoginUsername(), docActionRelId,
					// SessionHelper.getTenantId() }
					new PreparedStatementSetter() {
						public void setValues(PreparedStatement preparedStatement) throws SQLException {
							preparedStatement.setString(1, SessionHelper.getLoginUsername());
							preparedStatement.setLong(2, docActionRelId);
							preparedStatement.setString(3, SessionHelper.getTenantId());
						}
					});
			logger.info("No. of rows impacted=" + rowsImpacted);

		} catch (Exception ex) {
			logger.error("Error occured while deleting action in database", ex);
			throw new WorkbenchException("Error occured while deleting action in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteActionSqlAttb query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteActionSqlAttb" + "," + timeElapsed + ",secs");
		return rowsImpacted;
	}

	public List<ActionParamAttrMappingDbData> getActionData(int actionNameCde, long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList;
		try {
			actionParamAttrMappingDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getActionParamMappingSql),
					new PreparedStatementSetter() {
						public void setValues(PreparedStatement preparedStatement) throws SQLException {
							preparedStatement.setInt(1, actionNameCde);
							preparedStatement.setString(2, SessionHelper.getTenantId());
							preparedStatement.setLong(3, docId);
							preparedStatement.setString(4, SessionHelper.getTenantId());
							preparedStatement.setInt(5, actionNameCde);
							preparedStatement.setString(6, SessionHelper.getTenantId());
							preparedStatement.setLong(7, docId);
							preparedStatement.setString(8, SessionHelper.getTenantId());
						}
					}, new RowMapper<ActionParamAttrMappingDbData>() {
						@Override
						public ActionParamAttrMappingDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ActionParamAttrMappingDbData dbData = new ActionParamAttrMappingDbData();

							dbData.setActionNameCde(rs.getInt("action_name_cde"));
							dbData.setActionNameTxt(rs.getString("action_name_txt"));
							dbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							dbData.setAttrValue(rs.getString("attr_value"));
							if (StringUtility.hasValue(rs.getString("apam_attr_name_txt"))) {
								dbData.setAttrNameTxt(rs.getString("apam_attr_name_txt"));
							} else {
								dbData.setAttrNameTxt(rs.getString("attrval_attr_name_txt"));
							}
							dbData.setParamNameCde(rs.getInt("param_name_cde"));
							dbData.setParamNameTxt(rs.getString("param_name_txt"));
							return dbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching task status val data from database", ex);
			throw new WorkbenchException("Error occured while fetching task status val data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getActionParamMappingSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getActionParamMappingSql" + "," + timeElapsed + ",secs");

		return actionParamAttrMappingDbDataList;
	}

	public String getActionResult(long docActionRelId) throws WorkbenchException {
		long startTime = System.nanoTime();
		String actionResult;
		try {
			List<String> actionResults = jdbcTemplate.query(StringUtility.sanitizeSql(getActionResultSql),
					new Object[] { docActionRelId, SessionHelper.getTenantId() }, new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString(1);
						}
					});
			actionResult = actionResults.get(0);
			double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
			logger.info("Time taken for getActionResultSql query (secs):" + timeElapsed);
			PERF_LOGGER.info("getActionResultSql" + "," + timeElapsed + ",secs");
		} catch (Exception ex) {
			logger.error("Error occured while fetching action result from database", ex);
			throw new WorkbenchException("Error occured while fetching action result from database", ex);
		} finally {
		}
		return actionResult;
	}

}
