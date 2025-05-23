/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DbUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAuditDbData;
import com.infosys.ainauto.docwb.service.model.db.UserAuditDbData;

/**
 * 
 * AuditDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */

@Component
@Profile("default")
public class AuditDataAccess implements IAuditDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(AuditDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addAudit}")
	private String addAuditSql;

	@Value("${addDocAudit}")
	private String addDocAuditSql;

	@Value("${addUserAudit}")
	private String addUserAuditSql;
	
	@Value("${actionValue}")
	private String getDocActionValueSql;

	@Value("${docattributeValue}")
	private String getDocAttributeValueSql;

	@Value("${attachattributeValue}")
	private String getAttachAttributeValueSql;

	@Value("${attachmentValue}")
	private String getDocAttachmentValueSql;
	
	@Value("${getattachmentValue}")
	private String getAttachmentValueSql;

	@Value("${emailValue}")
	private String getDocEmailValueSql;

	@Value("${assignmentValue}")
	private String getDocAssignmentValueSql;

	@Value("${roleValue}")
	private String getUserRoleValueSql;

	@Value("${queueValue}")
	private String getUserQueueValueSql;

	@Value("${userValue}")
	private String getUserValueSql;

	@Value("${docValue}")
	private String getDocValueSql;

	@Value("${getAuditForDoc}")
	private String getAuditForDocSql;

	@Value("${getAuditForUser}")
	private String getAuditForUserSql;
	
	@Value("${getAuditForAppVariable}")
	private String getAuditForAppVariableSql;

	@Value("${getUserAuditCount}")
	private String getUserAuditCountSql;
	
	@Value("${getAuditForAppVariableCount}")
	private String getAuditForAppVariableCountSql;

	@Value("${getDocAuditCount}")
	private String getDocAuditCountSql;
	
	@Value("${getAuditForCurrentUserAtDocLevel}")
	private String getAuditForCurrentUserAtDocLevelSql;
	
	private static final int PAGE_SIZE = 15;
	//Buffer to allow required aggregated count
	private static final int OFFSET_BUFFER = 6; 

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<AuditDbData> addAudit(List<AuditDbData> auditDbDataList, String loggedInUser, String tenantId)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		try {
			for (int i = 0; i < auditDbDataList.size(); i++) {
				AuditDbData dbData = auditDbDataList.get(i);
				String tempSQL[] = StringUtility.sanitizeSql(addAuditSql).toLowerCase()
						.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
				String appSQL = tempSQL[0].trim();
				String outParam = tempSQL[1].trim();
				final PreparedStatementCreator psc = new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
						final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

						ps.setString(1, dbData.getEntityName());
						ps.setString(2, dbData.getEntityValue());
						ps.setString(3, dbData.getAuditMessage());
						ps.setString(4, dbData.getCurrentValue());
						ps.setString(5, dbData.getPreviousValue());
						ps.setString(6, loggedInUser);
						ps.setString(7, loggedInUser);
						ps.setString(8, tenantId);

						return ps;
					}
				};

				KeyHolder keyHolder = new GeneratedKeyHolder();
				jdbcTemplate.update(psc, keyHolder);
				dbData.setAuditId(keyHolder.getKey().longValue());
				auditDataList.add(dbData);
			}
		} catch (Exception ex) {
			logger.error("Error occured while adding audit in database", ex);
			throw new WorkbenchException("Error occured while adding audit in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addAuditSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addAuditSql" + "," + timeElapsed + ",secs");
		return auditDataList;
	}

	@Override
	public void addDocAuditRel(AuditDbData auditDbData, String loggedInUser, String tenantId)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		long docAuditRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addDocAuditSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, auditDbData.getAuditId());
					ps.setLong(2, auditDbData.getDocId());
					ps.setString(3, loggedInUser);
					ps.setString(4, tenantId);
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			docAuditRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding document audit data in database", ex);
			throw new WorkbenchException("Error occured while adding document audit data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addDocAuditSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addDocAuditSql" + "," + timeElapsed + ",secs");

	}

	@Override
	public void addUserAuditRel(AuditDbData auditDbData, String loggedInUser, String tenantId)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		long userAuditRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addUserAuditSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, auditDbData.getAuditId());
					ps.setLong(2, auditDbData.getAppUserId());
					ps.setString(3, loggedInUser);
					ps.setString(4, tenantId);
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			userAuditRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding user audit data in database", ex);
			throw new WorkbenchException("Error occured while adding user audit data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addUserAuditSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addUserAuditSql" + "," + timeElapsed + ",secs");

	}
	
	@Override
	public List<DocAuditDbData> getDocValues(EnumEntityType entityName, String columnName, long entityValue,
			String tenantId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocAuditDbData> docAuditDbDataList = new ArrayList<DocAuditDbData>();
		String sqlQuery;
		try {
			switch (entityName) {
			case ACTION:
				sqlQuery = StringUtility.sanitizeSql(getDocActionValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setActionNameTxt(rs.getString("action_name_txt"));
								dbData.setTaskStatusTxt(rs.getString("task_status_txt"));
								dbData.setActionResult(rs.getString("action_result"));
								dbData.setSnapShot(rs.getString("snap_shot"));
								return dbData;
							}
						});
				break;
			case CASE_OWNER_ASSIGNMENT:
			case CASE_REVIEWER_ASSIGNMENT:
				sqlQuery = StringUtility.sanitizeSql(getDocAssignmentValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setUserFullName(rs.getString("user_full_name"));
								return dbData;
							}
						});
				break;
			case ATTACHMENT_REL:
				sqlQuery = StringUtility.sanitizeSql(getAttachmentValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();
								dbData.setFileName(rs.getString("logical_name"));
								return dbData;
							}
						});
				break;
			case ATTACHMENT:
				sqlQuery = StringUtility.sanitizeSql(getDocAttachmentValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setFileName(rs.getString("logical_name"));
								return dbData;
							}
						});
				break;
			case DOC_ATTRIBUTE:
				sqlQuery = StringUtility.sanitizeSql(getDocAttributeValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setAttrNameTxt(rs.getString("attr_name_txt"));
								dbData.setAttrValue(rs.getString("attr_value"));
								return dbData;
							}
						});
				break;
			case ATTACH_ATTRIBUTE:
				sqlQuery = StringUtility.sanitizeSql(getAttachAttributeValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setAttrNameTxt(rs.getString("attr_name_txt"));
								dbData.setAttrValue(rs.getString("attr_value"));
								return dbData;
							}
						});
				break;
			case EMAIL:
				sqlQuery = StringUtility.sanitizeSql(getDocEmailValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setTaskStatusCde(rs.getInt("task_status_cde"));
								dbData.setTaskStatusTxt(rs.getString("task_status_txt"));
								dbData.setEmailSubject(rs.getString("email_subject"));
								return dbData;
							}
						});
				break;
			case DOCUMENT:
				sqlQuery = StringUtility.sanitizeSql(getDocValueSql.replace("{COLUMN_NAME}", columnName));
				docAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<DocAuditDbData>() {
							@Override
							public DocAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAuditDbData dbData = new DocAuditDbData();

								dbData.setDocId(rs.getLong("doc_id"));
								dbData.setTaskStatusTxt(rs.getString("task_status_txt"));
								return dbData;
							}
						});
				break;
			default:
				break;

			}
		} catch (Exception ex) {
			logger.error("Error occured while fetching Document audit data in database", ex);
			throw new WorkbenchException("Error occured while fetching Document audit data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocValues() query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocValues()" + "," + timeElapsed + ",secs");
		return docAuditDbDataList;
	}

	@Override
	public List<UserAuditDbData> getUserValues(EnumEntityType entityName, String columnName, Long entityValue,
			String tenantId) throws WorkbenchException {
		long startTime = System.nanoTime();
		String sqlQuery;
		List<UserAuditDbData> userAuditDbDataList = new ArrayList<UserAuditDbData>();
		try {
			switch (entityName) {
			case QUEUE_ASSIGNMENT:
				sqlQuery = StringUtility.sanitizeSql(getUserQueueValueSql.replace("{COLUMN_NAME}", columnName));
				userAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<UserAuditDbData>() {
							@Override
							public UserAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								UserAuditDbData dbData = new UserAuditDbData();
								dbData.setAppUserId(rs.getLong("app_user_id"));
								dbData.setQueueNameTxt(rs.getString("queue_name_txt"));
								return dbData;
							}
						});
				break;
			case ROLE:
				sqlQuery = StringUtility.sanitizeSql(getUserRoleValueSql.replace("{COLUMN_NAME}", columnName));
				userAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<UserAuditDbData>() {
							@Override
							public UserAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								UserAuditDbData dbData = new UserAuditDbData();
								dbData.setAppUserId(rs.getLong("app_user_id"));
								dbData.setRoleTypeTxt(rs.getString("role_type_txt"));
								return dbData;
							}
						});
				break;
			case USER:
				sqlQuery = StringUtility.sanitizeSql(getUserValueSql.replace("{COLUMN_NAME}", columnName));
				userAuditDbDataList = jdbcTemplate.query(sqlQuery, new Object[] { entityValue, tenantId },
						new RowMapper<UserAuditDbData>() {
							@Override
							public UserAuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								UserAuditDbData dbData = new UserAuditDbData();
								dbData.setAppUserId(rs.getLong("app_user_id"));
								dbData.setUserFullName(rs.getString("user_full_name"));
								dbData.setAccountEnabled(rs.getBoolean("account_enabled"));
								return dbData;
							}
						});
				break;
			default:
				break;
			}

		} catch (Exception ex) {
			logger.error("Error occured while fetching User audit data in database", ex);
			throw new WorkbenchException("Error occured while fetching User audit data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserValues() query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserValues()" + "," + timeElapsed + ",secs");
		return userAuditDbDataList;
	}

	@Override
	public long getAuditCount(long docId, long appUserId, String appVariableKey) throws WorkbenchException {
		long startTime = System.nanoTime();
		long total;
		String sqlQuery = "";
		String sqlQueryStr = "";
		try {
			Object[] parameters=null;
			
			if (docId > 0) {
				sqlQuery = getDocAuditCountSql;
				sqlQueryStr = "getDocAuditCountSql";
				parameters=new Object[] { docId, SessionHelper.getTenantId() };
			} else if (appUserId > 0) {
				sqlQuery = getUserAuditCountSql;
				sqlQueryStr = "getUserAuditCountSql";
				parameters=new Object[] { appUserId, SessionHelper.getTenantId() };
			}else if (StringUtility.hasValue(appVariableKey)){
				sqlQuery = getAuditForAppVariableCountSql;
				sqlQueryStr = "getAuditForAppVariableCountSql";
				parameters=new Object[] { "app_variable", appVariableKey, SessionHelper.getTenantId() };
			}
			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					parameters, new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			total = count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching audit count from database", ex);
			throw new WorkbenchException("Error occured while fetching audit count from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for "+sqlQueryStr+" query (secs):" + timeElapsed);
		PERF_LOGGER.info(sqlQueryStr + "," + timeElapsed + ",secs");
		return total;
	}

	@Override
	public List<AuditDbData> getAudit(long docId, long appUserId, String appVariableKey, int pageNumber) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		String sqlQuery = "";
		String sqlQueryName = "";
		try {
			int offset = 0;
			if (pageNumber > 1) {
				offset = PAGE_SIZE * (pageNumber - 1);
			}
			Object[] parameters = null;
			if (docId > 0) {
				sqlQuery = getAuditForDocSql;
				sqlQueryName = "getAuditForDocSql";
				parameters=new Object[] { docId, SessionHelper.getTenantId() };
			} else if (appUserId > 0) {
				sqlQuery = getAuditForUserSql;
				sqlQueryName = "getAuditForUserSql";
				parameters=new Object[] { appUserId, SessionHelper.getTenantId() };
			}else if (StringUtility.hasValue(appVariableKey)){
				sqlQuery = getAuditForAppVariableSql;
				sqlQueryName = "getAuditForAppVariableSql";
				parameters=new Object[] { "app_variable", appVariableKey, SessionHelper.getTenantId() };
			}else {
				sqlQuery=getAuditForCurrentUserAtDocLevelSql;
				sqlQueryName="getAuditForCurrentUserAtDocLevelSql";
				int innerQueryLimit = (offset + PAGE_SIZE)*OFFSET_BUFFER;
				parameters=new Object[] {SessionHelper.getTenantId(),
						SessionHelper.getLoginUserData().getUsername(), innerQueryLimit};
			}
			sqlQuery += " OFFSET " + offset + " LIMIT " + PAGE_SIZE;
			auditDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					parameters, new RowMapper<AuditDbData>() {
						@Override
						public AuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AuditDbData dbData = new AuditDbData();
							dbData.setAuditLoginId(rs.getString("audit_login_id"));
							dbData.setAuditMessage(rs.getString("audit_message"));
							dbData.setCurrentValue(rs.getString("current_value"));
							dbData.setPreviousValue(rs.getString("previous_value"));
							dbData.setAuditEventDtm(rs.getTimestamp("audit_event_dtm"));
							if (DbUtility.hasColumn(rs, "doc_id")) {
								dbData.setDocId(rs.getLong("doc_id"));
							}
							if (DbUtility.hasColumn(rs, "queue_name_cde")) {
								dbData.setQueueNameCde(rs.getLong("queue_name_cde"));
							}							
							if (DbUtility.hasColumn(rs, "user_type_cde")) {
								dbData.setUserType(rs.getInt("user_type_cde"));
							}
							return dbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching audit from database", ex);
			throw new WorkbenchException("Error occured while fetching audit from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for " + sqlQueryName + " query (secs):" + timeElapsed);
		PERF_LOGGER.info(sqlQueryName + "," + timeElapsed + ",secs");
		return auditDataList;
	}

}
