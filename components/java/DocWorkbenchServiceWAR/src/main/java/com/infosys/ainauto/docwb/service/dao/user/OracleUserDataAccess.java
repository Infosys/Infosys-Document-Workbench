/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.user;

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
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
@Profile("oracle")
public class OracleUserDataAccess extends UserDataAccess implements IUserDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(OracleUserDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String UPDATE_USER_ACCOUNT_SQL = "UPDATE_USER_ACCOUNT_CALLABLE";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public EntityDbData updateUserAccount(AppUserDbData appUserDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		EntityDbData entityDbData = new EntityDbData();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(UPDATE_USER_ACCOUNT_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_ACCOUNT_ENABLED", appUserDbData.isAccountEnabled());
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_APP_USER_ID", appUserDbData.getAppUserId());
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			entityDbData.setAppUserId(Long.valueOf((String) sqlResponse.get("O_APP_USER_ID")));
			entityDbData.setAccountEnabled(Boolean.valueOf((String) sqlResponse.get("O_ACCOUNT_ENABLED")));

			logger.info("The appUserId that was updated is {}", entityDbData.getAppUserId());
		} catch (Exception ex) {
			logger.error("Error occured while updating account enabled user in database", ex);
			throw new WorkbenchException("Error occured while enabling account in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateUserAccountEnabledSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateUserAccountEnabledSql" + "," + timeElapsed + ",secs");
		return entityDbData;
	}

}
