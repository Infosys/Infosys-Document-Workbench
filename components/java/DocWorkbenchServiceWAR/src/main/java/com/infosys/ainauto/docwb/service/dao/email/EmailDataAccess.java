/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.email;

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

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.db.EmailDbData;
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
public class EmailDataAccess implements IEmailDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(EmailDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addEmail}")
	private String addEmailSql;

	@Value("${getOutboundEmail}")
	private String getOutboundEmailSql;

	@Value("${getOutboundEmailByTaskStatus}")
	private String getOutboundEmailByTaskStatusSql;

	@Value("${getDraftEmail}")
	private String getDraftEmailSql;

	@Value("${addDraftEmail}")
	private String addDraftEmailSql;

	@Value("${updateDraftEmail}")
	private String updateDraftEmailSql;

	@Value("${deleteEmail}")
	private String deleteEmailSql;

	@Value("${updateEmailTaskStatus}")
	private String updateEmailTaskStatusSql;

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long insertOutboundEmail(InsertEmailReqData emailData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long emailOutboundId = -1;
		try {

			String tempSQL[] = StringUtility.sanitizeSql(addEmailSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, emailData.getEmailBodyHtml());
					ps.setLong(2, emailData.getDocId());
					ps.setString(3, emailData.getEmailTo());
					ps.setString(4, emailData.getEmailSubject());
					ps.setString(5, SessionHelper.getLoginUsername());
					ps.setLong(6, emailData.getTaskStatusCde());
					ps.setString(7, emailData.getEmailCC());
					ps.setString(8, emailData.getEmailBCC());
					ps.setString(9, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			emailOutboundId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while add template in database", ex);
			throw new WorkbenchException("Error occured while adding template in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addEmailSql" + "," + timeElapsed + ",secs");
		return emailOutboundId;
	}

	public List<EmailDbData> getOutboundEmailList(Long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EmailDbData> emailDbDataList = new ArrayList<EmailDbData>();

		try {
			emailDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getOutboundEmailSql),
					new Object[] { docId, SessionHelper.getTenantId() }, new RowMapper<EmailDbData>() {
						@Override
						public EmailDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							EmailDbData emailDbData = new EmailDbData();
							emailDbData.setDocId(rs.getInt("doc_id"));
							emailDbData.setEmailOutboundId(rs.getInt("email_outbound_id"));
							emailDbData.setEmailTo(rs.getString("email_to"));
							emailDbData.setEmailSubject(rs.getString("email_subject"));
							emailDbData.setEmailBodyText(rs.getString("email_body_text"));
							emailDbData.setEmailSentDtm(rs.getString("email_sent_dtm"));
							emailDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							emailDbData.setEmailCC(rs.getString("email_cc"));
							emailDbData.setEmailBCC(rs.getString("email_bcc"));
							emailDbData.setEmailBodyHtml(rs.getString("email_body_html"));
							emailDbData.setCreateByUserLoginId(rs.getString("create_by_user_login_id"));
							emailDbData.setCreateByUserFullName(rs.getString("create_by_user_full_name"));
							emailDbData.setCreateByUserTypeCde(rs.getInt("create_by_user_type_cde"));
							emailDbData.setCreateByUserTypeTxt(rs.getString("create_by_user_type_txt"));
							return emailDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attribute name code from database", ex);
			throw new WorkbenchException("Error occured while fetching attribute name code from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getOutboundEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getOutboundEmailSql" + "," + timeElapsed + ",secs");
		return emailDbDataList;
	}

	public Long getTotalEmailCount(int taskStatusCde) throws WorkbenchException {
		long startTime = System.nanoTime();
		Long total;
		String sqlQuery = getOutboundEmailByTaskStatusSql;
		sqlQuery = "SELECT COUNT(*) FROM ( " + getOutboundEmailByTaskStatusSql + " ) DATA ";
		try {
			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { taskStatusCde, SessionHelper.getTenantId() }, new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			total = count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching email list count from database", ex);
			throw new WorkbenchException("Error occured while fetching email list count from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getOutboundEmailByTaskStatusSql-Count query (secs):" + timeElapsed);
		PERF_LOGGER.info("getOutboundEmailByTaskStatusSql-Count" + "," + timeElapsed + ",secs");
		return total;
	}

	public List<EmailDbData> getOutboundEmailListByTaskStatus(int taskStatusCde, int pageNumber)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EmailDbData> emailDbDataList = new ArrayList<EmailDbData>();
		String sqlQuery = getOutboundEmailByTaskStatusSql;
		int offset = 0;
		if (pageNumber > 1) {
			offset = pageSize * (pageNumber - 1);
		}
		sqlQuery += " ORDER BY DOC_ID OFFSET " + offset + " LIMIT " + pageSize;
		try {
			emailDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { taskStatusCde, SessionHelper.getTenantId() }, new RowMapper<EmailDbData>() {
						@Override
						public EmailDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							EmailDbData emailDbData = new EmailDbData();
							emailDbData.setDocId(rs.getInt("doc_id"));
							emailDbData.setEmailTo(rs.getString("email_to"));
							emailDbData.setEmailSubject(rs.getString("email_subject"));
							emailDbData.setEmailBodyText(rs.getString("email_body_text"));
							emailDbData.setEmailSentDtm(rs.getString("email_sent_dtm"));
							emailDbData.setEmailCC(rs.getString("email_cc"));
							emailDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							emailDbData.setEmailBCC(rs.getString("email_bcc"));
							emailDbData.setEmailOutboundId(rs.getLong("email_outbound_id"));
							emailDbData.setEmailBodyHtml(rs.getString("email_body_html"));
							return emailDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching email list from database", ex);
			throw new WorkbenchException("Error occured while fetching email list from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getOutboundEmailByTaskStatusSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getOutboundEmailByTaskStatusSql" + "," + timeElapsed + ",secs");
		return emailDbDataList;
	}

	public EmailDbData getDraftEmail(Long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EmailDbData> emailDbDataList = new ArrayList<EmailDbData>();

		try {
			emailDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getDraftEmailSql),
					new Object[] { docId, SessionHelper.getTenantId() }, new RowMapper<EmailDbData>() {
						@Override
						public EmailDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							EmailDbData emailDbData = new EmailDbData();
							emailDbData.setDocId(rs.getInt("doc_id"));
							emailDbData.setEmailTo(rs.getString("email_to"));
							emailDbData.setEmailBodyText(rs.getString("email_body_text"));
							emailDbData.setEmailBodyHtml(rs.getString("email_body_html"));
							emailDbData.setEmailSubject(rs.getString("email_subject"));
							emailDbData.setEmailOutboundId(rs.getInt("email_outbound_id"));
							emailDbData.setEmailSentDtm(rs.getString("email_sent_dtm"));
							emailDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							emailDbData.setEmailCC(rs.getString("email_cc"));
							emailDbData.setEmailBCC(rs.getString("email_bcc"));
							return emailDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching email draft from database", ex);
			throw new WorkbenchException("Error occured while fetching attribute name code from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDraftEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDraftEmailSql" + "," + timeElapsed + ",secs");
		if (emailDbDataList.size() > 0) {
			return emailDbDataList.get(0);
		}
		return null;
	}

	public long insertDraftEmail(EmailDbData emailDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long emailOutboundId = 0;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addDraftEmailSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, emailDbData.getEmailBodyHtml());
					ps.setString(2, emailDbData.getEmailSubject());
					ps.setLong(3, emailDbData.getDocId());
					ps.setString(4, SessionHelper.getLoginUsername());
					ps.setLong(5, emailDbData.getTaskStatusCde());
					ps.setString(6, emailDbData.getEmailTo());
					ps.setString(7, emailDbData.getEmailCC());
					ps.setString(8, emailDbData.getEmailBCC());
					ps.setString(9, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			emailOutboundId = keyHolder.getKey().longValue();
			logger.info("No. of rows impacted=" + emailOutboundId);
		} catch (Exception ex) {
			logger.error("Error occured while add template in database", ex);
			throw new WorkbenchException("Error occured while adding template in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addDraftEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addDraftEmailSql" + "," + timeElapsed + ",secs");
		return emailOutboundId;
	}

	public long updateDraftEmail(EmailDbData emailDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long emailOutboundId = 0;
		try {

			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection
							.prepareStatement(StringUtility.sanitizeSql(updateDraftEmailSql));
					ps.setString(1, emailDbData.getEmailBodyHtml());
					ps.setString(2, emailDbData.getEmailSubject());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, emailDbData.getEmailTo());
					ps.setString(5, emailDbData.getEmailCC());
					ps.setString(6, emailDbData.getEmailBCC());
					ps.setLong(7, emailDbData.getTaskStatusCde());
					ps.setLong(8, emailDbData.getEmailOutboundId());
					ps.setString(9, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowsUpdated = jdbcTemplate.update(psc);
			if (noOfRowsUpdated > 0) {
				emailOutboundId = emailDbData.getEmailOutboundId();
			} else {
				logger.info("No rows updated");
			}

		} catch (Exception ex) {
			logger.error("Error occured while add template in database", ex);
			throw new WorkbenchException("Error occured while adding template in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateDraftEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateDraftEmailSql" + "," + timeElapsed + ",secs");
		return emailOutboundId;
	}

	public long deleteEmail(long emailId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long emailOutboundId = -1;
		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(StringUtility.sanitizeSql(deleteEmailSql));
					ps.setString(1, SessionHelper.getLoginUsername());
					ps.setLong(2, emailId);
					ps.setString(3, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowsUpdated = jdbcTemplate.update(psc);
			if (noOfRowsUpdated > 0) {
				emailOutboundId = emailId;
			} else {
				logger.info("No rows updated");
			}
		} catch (Exception ex) {
			logger.error("Error occured while deleting Email in database", ex);
			throw new WorkbenchException("Error occured while deleting Email in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteEmailSql" + "," + timeElapsed + ",secs");
		return emailOutboundId;
	}

	public EntityDbData updateEmailTaskStatus(long emailOutboundId, long taskStatusCde) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EntityDbData> entityDbDatas = new ArrayList<>();
		EntityDbData entityData = new EntityDbData();
		try {
			Object object[];
			object = new Object[] { taskStatusCde, SessionHelper.getLoginUsername(), emailOutboundId,
					SessionHelper.getTenantId() };
			entityDbDatas = jdbcTemplate.query(StringUtility.sanitizeSql(updateEmailTaskStatusSql), object,
					new RowMapper<EntityDbData>() {
						@Override
						public EntityDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							EntityDbData entityData = new EntityDbData();
							long emailId = rs.getLong("email_outbound_id");
							if (emailId > 0) {
								List<Long> emailIdList = new ArrayList<>();
								emailIdList.add(emailId);
								entityData.setEmailIdList(emailIdList);
								entityData.setTaskStatusCde(rs.getInt("task_status_cde"));
								entityData.setTaskStatusTxt(rs.getString("task_status_txt"));
							}
							return entityData;
						}
					});
			if (ListUtility.hasValue(entityDbDatas)) {
				entityData = entityDbDatas.get(0);
			}

		} catch (Exception ex) {
			logger.error("Error occured while update updateEmailTaskStatus in database", ex);
			throw new WorkbenchException("Error occured while updating email task status in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateEmailTaskStatusSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateEmailTaskStatusSql" + "," + timeElapsed + ",secs");
		return entityData;
	}

}
