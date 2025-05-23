/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.variable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AppVariableDbData;

@Component
@Profile("default")
public class VariableDataAccess implements IVariableDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(VariableDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${getAppVariable}")
	private String getAppVariableSql;

	@Value("${addNewAppVariable}")
	private String addNewAppVariableSql;

	@Value("${deleteAppVariable}")
	private String deleteAppVariableSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	private void load() throws WorkbenchException {
	}

	@Override
	public List<AppVariableDbData> getAppVariableDataFor(String appVarKey, boolean isFetchAll) throws WorkbenchException {
		long startTime = System.nanoTime();

		List<AppVariableDbData> rbacDbDataList = new ArrayList<AppVariableDbData>();
		try {
			rbacDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getAppVariableSql),
					new Object[] { appVarKey, SessionHelper.getTenantId() }, new RowMapper<AppVariableDbData>() {
						@Override
						public AppVariableDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AppVariableDbData appVariableDbData = new AppVariableDbData();
							appVariableDbData.setAppVarId(rs.getLong("app_var_id"));
							appVariableDbData.setAppVarKey(rs.getString("app_var_key"));
							appVariableDbData.setAppVarValue(rs.getString("app_var_value"));
							appVariableDbData.setTenantId(rs.getString("tenant_id"));
							appVariableDbData.setCreateBy(rs.getString("create_by"));
							appVariableDbData.setCreateDtm(rs.getString("create_dtm"));
							appVariableDbData.setLastModBy(rs.getString("last_mod_by"));
							appVariableDbData.setLastModDtm(rs.getString("last_mod_dtm"));
							return appVariableDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching app variable from database", ex);
			throw new WorkbenchException("Error occured while fetching  app variable from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAppVariableSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAppVariableSql" + "," + timeElapsed + ",secs");
		return rbacDbDataList;
	}

	@Override
	public long insertAppVariable(String appVarKey, String appVarVal) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserQueueRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addNewAppVariableSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setString(1, appVarKey);
					ps.setString(2, appVarVal);
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			appUserQueueRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding addNewAppVariable", ex);
			throw new WorkbenchException("Error occured while adding addNewAppVariable", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addNewAppVariableSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addNewAppVariableSql" + "," + timeElapsed + ",secs");

		return appUserQueueRelId;

	}

	@Override
	public long deleteAppVariable(long appVarId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long sqlResponseId = -1;
		try {
			Object object[] = new Object[] {SessionHelper.getLoginUsername(), appVarId, SessionHelper.getTenantId() };
			Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(deleteAppVariableSql),object);
			long appVarIdOut = Long.parseLong(String.valueOf(sqlResponse.get("app_var_id")));
			if (appVarIdOut > 0) {
				logger.info("The row that was impacted=" + appVarIdOut);
				sqlResponseId = appVarIdOut;
			} else {
				logger.info("No row was impacted. Wrong Input");
			}

		} catch (Exception ex) {
			logger.error("Error occured while executing deleteAppVariableSql", ex);
			throw new WorkbenchException("Error occured while executing deleteAppVariableSql", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteAppVariableSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteAppVariableSql" + "," + timeElapsed + ",secs");
		return sqlResponseId;
	}

}
