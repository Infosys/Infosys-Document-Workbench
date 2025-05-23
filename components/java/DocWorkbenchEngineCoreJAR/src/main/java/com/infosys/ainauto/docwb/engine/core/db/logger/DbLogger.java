/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.commonutils.SystemUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.SqlQueryManager;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;

@Component
public class DbLogger implements IDbLogger {

	private static final Logger logger = LoggerFactory.getLogger(DbLogger.class);
	private static final String startExecutionSql = SqlQueryManager.getInstance().getSql("startExecution");
	private static final String endExecutionSql = SqlQueryManager.getInstance().getSql("endExecution");
	private static final String addEventSql = SqlQueryManager.getInstance().getSql("addEvent");
	private static final String updateEventMsgSql = SqlQueryManager.getInstance().getSql("updateEventMsg");
	private static final String PROP_NAME_DB_USERNAME = "spring.datasource.username";
	private static final String PROP_NAME_TENANT_ID = "docwb.tenant.id";
	private static String myAddress;

	@Autowired
	private Environment environment;

	@Autowired
	public DbLogger() {
		String myIpAddress = "";
		String myHostName = "";
		try {
			myIpAddress = SystemUtility.getHostIpAddress();
			myHostName = " (" + SystemUtility.getHostName() + ")";
		} catch (Exception ex) {
			logger.error("Error while getting IP Address and Hostname of this system", ex);
		} finally {
			myAddress = myIpAddress + myHostName;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long startExecution(EnumExecutorType executorType, String executionTitle, String eventMsg) {
		long startTime = System.nanoTime();
		long executionId = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		String tenantId = environment.getProperty(PROP_NAME_TENANT_ID);
		try {
			String tempSQL[] = StringUtility.sanitizeSql(startExecutionSql).toLowerCase()
					.split(DocwbEngineCoreConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, executorType.getCdeValue());
					ps.setString(2, executionTitle);
					ps.setString(3, tenantId);
					ps.setString(4, myAddress);
					ps.setString(5, userName);
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			executionId = keyHolder.getKey().longValue();
			if (eventMsg != null && eventMsg.length() > 0) {
				addEvent(executionId, EnumExecutionEventType.WORK_STARTED, eventMsg);
			}
		} catch (Exception ex) {
			logger.error("Error occured while inserting execution row in database", ex);
		} finally {
		}
		logger.info("Time taken for startExecution query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return executionId;
	}

	@Override
	public long endExecution(long executionId, String eventMsg) {
		long startTime = System.nanoTime();
		int rowsImpacted = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		try {
			rowsImpacted = jdbcTemplate.update(StringUtility.sanitizeSql(endExecutionSql),
					new Object[] { userName, executionId });
			logger.info("No. of rows impacted=" + rowsImpacted);
			if (eventMsg != null && eventMsg.length() > 0) {
				addEvent(executionId, EnumExecutionEventType.WORK_COMPLETED, eventMsg);
			}
		} catch (Exception ex) {
			logger.error("Error occured while inserting execution row in database", ex);
		} finally {
		}
		logger.info("Time taken for endExecution query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return rowsImpacted;
	}

	@Override
	public long addEvent(long executionId, EnumExecutionEventType eventType, String eventMsg) {
		long startTime = System.nanoTime();
		long execEventRelId = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addEventSql).toLowerCase()
					.split(DocwbEngineCoreConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, executionId);
					ps.setLong(2, eventType.getCdeValue());
					ps.setString(3, eventMsg);
					ps.setString(4, userName);
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			execEventRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while inserting event row in database", ex);
		} finally {
		}
		logger.info("Time taken for addEvent query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return execEventRelId;
	}

	@Override
	public long updateEventMsg(long execEventRelId, String eventMsg) {
		long startTime = System.nanoTime();
		int rowsImpacted = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		try {
			rowsImpacted = jdbcTemplate.update(StringUtility.sanitizeSql(updateEventMsgSql),
					new Object[] { eventMsg, userName, execEventRelId });
			logger.info("No. of rows impacted=" + rowsImpacted);

		} catch (Exception ex) {
			logger.error("Error occured while updating event message in database", ex);
		} finally {
		}
		logger.info("Time taken for updateEventMsg query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return execEventRelId;
	}

}
