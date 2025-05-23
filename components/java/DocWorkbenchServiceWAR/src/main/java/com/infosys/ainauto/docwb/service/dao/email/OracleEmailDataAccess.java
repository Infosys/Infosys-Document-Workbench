/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.email;

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
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
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
@Profile("oracle")
public class OracleEmailDataAccess extends EmailDataAccess implements IEmailDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(OracleEmailDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String UPDATE_EMAIL_TASK_STATUS_SQL = "UPDATE_EMAIL_TASK_STATUS_CALLABLE";

	@Value("${getOutboundEmailByTaskStatus}")
	private String getOutboundEmailByTaskStatusSql;

	@Value("${pageSize}")
	private int pageSize;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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
		// Works with oracle 12c version
		// TODO Enable and check it
		// sqlQuery += " ORDER BY DOC_ID OFFSET " + offset + " ROWS FETCH NEXT " +
		// pageSize + " ROWS ONLY";
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

	public EntityDbData updateEmailTaskStatus(long emailOutboundId, long taskStatusCde) throws WorkbenchException {
		long startTime = System.nanoTime();
		EntityDbData entityData = new EntityDbData();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(UPDATE_EMAIL_TASK_STATUS_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_TASK_STATUS_CDE", taskStatusCde);
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_EMAIL_OUTBOUND_ID", emailOutboundId);
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			long emailId = Long.valueOf((String) sqlResponse.get("O_EMAIL_OUTBOUND_ID"));
			if (emailId > 0) {
				List<Long> emailIdList = new ArrayList<>();
				emailIdList.add(emailId);
				entityData.setEmailIdList(emailIdList);
				entityData.setTaskStatusCde(Long.valueOf((String) sqlResponse.get("O_TASK_STATUS_CDE")));
				entityData.setTaskStatusTxt((String) sqlResponse.get("O_TASK_STATUS_TXT"));
			}
		} catch (Exception ex) {
			logger.error("Error occured while update DocActionStatus in database", ex);
			throw new WorkbenchException("Error occured while updating Document task status in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateEmailTaskStatusSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateEmailTaskStatusSql" + "," + timeElapsed + ",secs");
		return entityData;

	}

}
