/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.role;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.user.UserDataAccess;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;

@Component
@Profile("oracle")
public class OracleRoleDataAccess extends RoleDataAccess implements IRoleDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(UserDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String DELETE_ROLE_SQL = "DELETE_ROLE_CALLABLE";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long deleteUserRole(UserRoleDbData userRoleDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserRoleRelId = -1;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(DELETE_ROLE_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_APP_USER_ID", userRoleDbData.getAppUserId());
			mapParam.addValue("I_ROLE_TYPE_CDE", userRoleDbData.getUserRoleTypeCde());
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			appUserRoleRelId = Long.valueOf((String) sqlResponse.get("O_APP_USER_ROLE_REL_ID"));
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

}
