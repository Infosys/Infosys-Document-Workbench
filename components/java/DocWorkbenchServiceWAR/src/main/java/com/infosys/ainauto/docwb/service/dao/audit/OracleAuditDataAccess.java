/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.audit;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DbUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;

/**
 * 
 * AuditDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */
@Component
@Profile("oracle")
public class OracleAuditDataAccess extends AuditDataAccess implements IAuditDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(OracleAuditDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");
	@Value("${getAuditForDoc}")
	private String getAuditForDocSql;

	@Value("${getAuditForUser}")
	private String getAuditForUserSql;

	@Value("${getAuditForAppVariable}")
	private String getAuditForAppVariableSql;

	
	private static final int pageSize = 15;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<AuditDbData> getAudit(long docId, long appUserId, String appVariableKey, int pageNumber)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();
		String sqlQuery = "";
		String sqlQueryName = "";
		try {
			int offset = 0;
			if (pageNumber > 1) {
				offset = pageSize * (pageNumber - 1);
			}
			Object[] parametes = null;
			if (docId > 0) {
				sqlQuery = getAuditForDocSql;
				sqlQueryName = "getAuditForDocSql";
				parametes=new Object[] { docId, SessionHelper.getTenantId() };
			} else if (appUserId > 0) {
				sqlQuery = getAuditForUserSql;
				sqlQueryName = "getAuditForUserSql";
				parametes=new Object[] { appUserId, SessionHelper.getTenantId() };
			}else if (StringUtility.hasValue(appVariableKey)){
				sqlQuery = getAuditForAppVariableSql;
				sqlQueryName = "getAuditForAppVariableSql";
				parametes=new Object[] { "app_variable", appVariableKey, SessionHelper.getTenantId() };
			}
			
			// Works with oracle 12c version
			// TODO Enable and check it
			// sqlQuery += " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS
			// ONLY";
			auditDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					parametes, new RowMapper<AuditDbData>() {
						@Override
						public AuditDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AuditDbData dbData = new AuditDbData();
							dbData.setAuditLoginId(rs.getString("audit_login_id"));
							dbData.setAuditMessage(rs.getString("audit_message"));
							dbData.setCurrentValue(rs.getString("current_value"));
							dbData.setPreviousValue(rs.getString("previous_value"));
							dbData.setAuditEventDtm(rs.getTimestamp("audit_event_dtm"));
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
