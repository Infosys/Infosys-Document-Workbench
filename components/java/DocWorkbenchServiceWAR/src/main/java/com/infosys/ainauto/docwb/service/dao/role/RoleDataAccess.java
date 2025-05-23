/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.role;

import java.sql.Connection;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.user.UserDataAccess;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;

@Component
@Profile("default")
public class RoleDataAccess implements IRoleDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(UserDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addRole}")
	private String addRoleSql;

	@Value("${deleteRole}")
	private String deleteRoleSql;

	@Value("${getRole}")
	private String getRoleSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long insertUserRole(UserRoleDbData userRoleDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserRoleRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addRoleSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, userRoleDbData.getAppUserId());
					ps.setLong(2, userRoleDbData.getUserRoleTypeCde());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			appUserRoleRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding role for user", ex);
			throw new WorkbenchException("Error occured while adding role for user", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addRoleSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addRoleSql" + "," + timeElapsed + ",secs");

		return appUserRoleRelId;

	}

	@Override
	public long deleteUserRole(UserRoleDbData userRoleDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserRoleRelId = -1;
		try {
			Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(deleteRoleSql), // AC
					new Object[] { SessionHelper.getLoginUsername(), userRoleDbData.getAppUserId(),
							userRoleDbData.getUserRoleTypeCde(), SessionHelper.getTenantId() });
			appUserRoleRelId = sqlResponse.values().toArray(new Long[0])[0];
		} catch (Exception ex) {
			logger.error("Error occured while deleting role for user", ex);
			throw new WorkbenchException("Error occured while deleting role for user", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteRoleSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteRoleSql" + "," + timeElapsed + ",secs");
		return appUserRoleRelId;
	}

	@Override
	public List<UserRoleDbData> getRole(int appUserId) throws WorkbenchException {
		long startTime = System.nanoTime();

		List<UserRoleDbData> userRoleDbDataList = new ArrayList<>();
		try {
			userRoleDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getRoleSql),
					new Object[] { appUserId, SessionHelper.getTenantId() }, new RowMapper<UserRoleDbData>() {
						@Override
						public UserRoleDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleDbData userRoleDbData = new UserRoleDbData();
							userRoleDbData.setUserRoleTypeCde(rs.getInt("role_type_cde"));
							userRoleDbData.setAppUserId(rs.getInt("app_user_id"));
							userRoleDbData.setAppUserRoleRelId(rs.getLong("app_user_role_rel_id"));
							return userRoleDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getRoleSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getRoleSql" + "," + timeElapsed + ",secs");
		return userRoleDbDataList;

	}
}
