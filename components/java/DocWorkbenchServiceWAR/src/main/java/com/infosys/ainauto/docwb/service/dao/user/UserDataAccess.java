/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.UserTeammateDbData;

@Component
@Profile("default")
public class UserDataAccess implements IUserDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(UserDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${getUserDetails}")
	private String getUserDetailsSql;

	@Value("${getValTableData}")
	private String getValTableDataSql;

	@Value("${addUser}")
	private String addUserSql;

	@Value("${getTenants}")
	private String getTenantsSql;

	@Value("${addUserQueueRelationship}")
	private String addUserQueueRelationshipSql;

	@Value("${deleteUserQueueRelationship}")
	private String deleteUserQueueRelationshipSql;

	@Value("${getUserQueueRelationship}")
	private String getUserQueueRelationshipSql;

	@Value("${getUserRoleRelationship}")
	private String getUserRoleRelationshipSql;

	@Value("${updateUserAccountEnabled}")
	private String updateUserAccountEnabledSql;

	@Value("${changePassword}")
	private String changePasswordSql;
	
	@Value("${getUserDetailsFromLoginId}")
	private String getUserDetailsFromLoginIdSql;
	
	@Value("${getTeammatesDetails}")
	private String getTeammatesDetailsSql;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static HashMap<String, AppUserDbData> entityToUserTableMap = new HashMap<String, AppUserDbData>();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	private void load() throws WorkbenchException {
		loadUserTableDataToEntity();
	}

	@Override
	public AppUserDbData getUserData(String userName, String tenantId) throws WorkbenchException {
		long startTime = System.nanoTime();

		List<AppUserDbData> appUserDbDataList = new ArrayList<>();
		try {
			appUserDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getUserDetailsSql),
					new Object[] { userName, tenantId }, new RowMapper<AppUserDbData>() {
						@Override
						public AppUserDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AppUserDbData appUserDbData = new AppUserDbData();
							appUserDbData.setAppUserId(rs.getInt("app_user_id"));
							appUserDbData.setUserLoginId(rs.getString("user_login_id"));
							appUserDbData.setUserPassword(rs.getString("user_password"));
							appUserDbData.setUserEmail(rs.getString("user_email"));
							appUserDbData.setUserFullName(rs.getString("user_full_name"));
							appUserDbData.setUserTypeCde(rs.getInt("user_type_cde"));
							appUserDbData.setUserTypeTxt(rs.getString("user_type_txt"));
							appUserDbData.setAccessToken((rs.getString("access_token")));
							appUserDbData.setAccountNonExpired((rs.getBoolean("account_non_expired")));
							appUserDbData.setAccountNonLocked((rs.getBoolean("account_non_locked")));
							appUserDbData.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
							appUserDbData.setAccountEnabled(rs.getBoolean("account_enabled"));
							appUserDbData.setRoleTypeTxt(rs.getString("role_type_txt"));
							appUserDbData.setRoleTypeCde(rs.getInt("role_type_cde"));
							appUserDbData.setTenantId(rs.getString("tenant_id"));
							String roleTypeTxt = rs.getString("role_type_txt");
							if (StringUtility.hasValue(roleTypeTxt)) {
								Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
								grantedAuthorities.add(new SimpleGrantedAuthority(roleTypeTxt));
								appUserDbData.setGrantedAuthorities(grantedAuthorities);
							}

							return appUserDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching user name from database", ex);
			throw new WorkbenchException("Error occured while fetching  user data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserDetailsSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserDetailsSql" + "," + timeElapsed + ",secs");

		if (appUserDbDataList.size() > 0) {
			return appUserDbDataList.get(0);
		}

		return null;
	}

	@Override
	public long insertUser(AppUserDbData appUserDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserId = -1;

		String encodedPassword = (null != appUserDbData.getUserPassword())
				? passwordEncoder.encode(appUserDbData.getUserPassword())
				: "";
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addUserSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, appUserDbData.getUserLoginId().toLowerCase());
					ps.setString(2, encodedPassword);
					ps.setString(3, appUserDbData.getUserFullName());
					ps.setLong(4, appUserDbData.getUserTypeCde());
					ps.setString(5, appUserDbData.getUserEmail());
					ps.setString(6, SessionHelper.getLoginUsername());
					ps.setString(7, appUserDbData.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			appUserId = keyHolder.getKey().longValue();
		} catch (DataIntegrityViolationException ex) {
			logger.error(WorkbenchConstants.DB_ERROR_MSG_USERNAME_ALREADY_EXISTS, ex);
			throw new WorkbenchException(WorkbenchConstants.DB_ERROR_MSG_USERNAME_ALREADY_EXISTS);
		} catch (Exception ex) {
			logger.error("Error occured while fetching user name from database", ex);
			throw new WorkbenchException("Error occured while fetching  user data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addUserSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addUserSql" + "," + timeElapsed + ",secs");
		return appUserId;
	}

	@Override
	public long insertUserQueueRel(UserQueueDbData userQueueDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserQueueRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addUserQueueRelationshipSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, userQueueDbData.getAppUserId());
					ps.setLong(2, userQueueDbData.getQueueNameCde());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			appUserQueueRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding user queue relationship", ex);
			throw new WorkbenchException("Error occured while adding user queue relationship", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addUserQueueRelationshipSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addUserQueueRelationshipSql" + "," + timeElapsed + ",secs");

		return appUserQueueRelId;

	}

	@Override
	public long deleteUserQueueRel(long appUserQueueRelId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long sqlResponseId = -1;
		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection
							.prepareStatement(StringUtility.sanitizeSql(deleteUserQueueRelationshipSql));
					ps.setString(1, SessionHelper.getLoginUsername());
					ps.setLong(2, appUserQueueRelId);
					ps.setString(3, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowUpdated = jdbcTemplate.update(psc);
			if (noOfRowUpdated > 0) {
				logger.info("The row that was impacted=" + appUserQueueRelId);
				sqlResponseId = appUserQueueRelId;
			} else {
				logger.info("No row was impacted. Wrong Input");
			}

		} catch (Exception ex) {
			logger.error("Error occured while deleting user queue relationship", ex);
			throw new WorkbenchException("Error occured while deleting user queue relationship", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteUserQueueRelationshipSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteUserQueueRelationshipSql" + "," + timeElapsed + ",secs");
		return sqlResponseId;
	}

	@Override
	public List<UserQueueDbData> getUserQueueDetails(long appUserId) throws WorkbenchException {

		long startTime = System.nanoTime();

		List<UserQueueDbData> userQueueDbDataList = new ArrayList<>();
		try {
			userQueueDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getUserQueueRelationshipSql),
					new Object[] { appUserId, SessionHelper.getTenantId() }, new RowMapper<UserQueueDbData>() {
						@Override
						public UserQueueDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserQueueDbData userQueueDbData = new UserQueueDbData();
							userQueueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							userQueueDbData.setQueueNameTxt(rs.getString("queue_name_txt"));
							userQueueDbData.setAppUserQueueRelId(rs.getLong("app_user_queue_rel_id"));
							userQueueDbData.setDocTypeCde(rs.getLong("doc_type_cde"));
							userQueueDbData.setDocTypeTxt(rs.getString("doc_type_txt"));
							userQueueDbData.setQueueClosedDtm(rs.getString("queue_closed_dtm"));
							userQueueDbData.setQueueStatus(rs.getString("queue_status"));
							userQueueDbData.setQueueHideAfterDtm(rs.getString("queue_hide_after_dtm"));
							userQueueDbData.setUserQueueHideAfterDtm(rs.getString("user_queue_hide_after_dtm"));
							return userQueueDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching queue details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching queue details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserQueueRelationshipSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserQueueRelationshipSql" + "," + timeElapsed + ",secs");
		return userQueueDbDataList;
	}

	@Override
	public List<UserRoleDbData> getUserListDetails() throws WorkbenchException {
		long startTime = System.nanoTime();

		List<UserRoleDbData> userRoleDbDataList = new ArrayList<>();
		try {
			userRoleDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getUserRoleRelationshipSql),
					new Object[] { SessionHelper.getTenantId() }, new RowMapper<UserRoleDbData>() {
						@Override
						public UserRoleDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleDbData userRoleDbData = new UserRoleDbData();
							userRoleDbData.setAppUserId(rs.getInt("app_user_id"));
							userRoleDbData.setUserFullName((rs.getString("user_full_name")));
							userRoleDbData.setUserEmail((rs.getString("user_email")));
							userRoleDbData.setUserLoginId((rs.getString("user_login_id")));
							userRoleDbData.setUserTypeCde((rs.getInt("user_type_cde")));
							userRoleDbData.setUserTypeTxt((rs.getString("user_type_txt")));
							userRoleDbData.setUserRoleTypeCde((rs.getInt("role_type_cde")));
							userRoleDbData.setUserRoleTypeTxt((rs.getString("role_type_txt")));
							userRoleDbData.setAccountEnabled((rs.getBoolean("account_enabled")));

							return userRoleDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserRoleRelationshipSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserRoleRelationshipSql" + "," + timeElapsed + ",secs");
		return userRoleDbDataList;

	}

	@Override
	public EntityDbData updateUserAccount(AppUserDbData appUserDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		EntityDbData entityDbData = new EntityDbData();
		try {
			List<EntityDbData> entityDbDatas = jdbcTemplate
					.query(StringUtility.sanitizeSql(updateUserAccountEnabledSql),
							new Object[] { appUserDbData.isAccountEnabled(), SessionHelper.getLoginUsername(),
									appUserDbData.getAppUserId(), SessionHelper.getTenantId() },
							new RowMapper<EntityDbData>() {
								@Override
								public EntityDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
									EntityDbData data = new EntityDbData();
									data.setAppUserId(rs.getInt("app_user_id"));
									data.setAccountEnabled((rs.getBoolean("account_enabled")));
									return data;
								}
							});
			if (ListUtility.hasValue(entityDbDatas)) {
				if (entityDbDatas.get(0).getAppUserId() > 0) {
					entityDbData = entityDbDatas.get(0);
				}
			}

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

	@Override
	public long changePassword(long userId, String newPassword) throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserId = 0;

		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection
							.prepareStatement(StringUtility.sanitizeSql(changePasswordSql));
					ps.setString(1, newPassword);
					ps.setString(2, SessionHelper.getLoginUsername());
					ps.setLong(3, userId);
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowUpdated = jdbcTemplate.update(psc);
			if (noOfRowUpdated > 0) {
				appUserId = userId;
			} else {
				logger.info("No rows updated");
			}

			logger.info("Password changed for appUserId" + appUserId);
		} catch (Exception ex) {
			logger.error("Error occured while changing password for user in database", ex);
			throw new WorkbenchException("Error occured while changing password in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for changePasswordSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("changePasswordSql" + "," + timeElapsed + ",secs");
		return appUserId;
	}

	@Override
	public List<String> getTenants() throws WorkbenchException {
		long startTime = System.nanoTime();
		List<String> tenantList = new ArrayList<>();
		try {
			tenantList = jdbcTemplate.query(StringUtility.sanitizeSql(getTenantsSql), new Object[] {},
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString("tenant_id");

						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching tenatn details from database", ex);
			throw new WorkbenchException("Error occured while fetching tenant details from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getTenantsSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getTenantsSql" + "," + timeElapsed + ",secs");
		return tenantList;
	}

	public AppUserDbData getUserData(String key) throws WorkbenchException {
		AppUserDbData appUserDbData = null;
		if (!entityToUserTableMap.containsKey(key)) {
			loadUserTableDataToEntity();
		}
		if (entityToUserTableMap.containsKey(key)) {
			appUserDbData = entityToUserTableMap.get(key);
		}
		return appUserDbData;
	}

	private void loadUserTableDataToEntity() throws WorkbenchException {
		for (AppUserDbData appUserDbData : getUserTableDataFromDb()) {
			entityToUserTableMap.put(appUserDbData.getUserLoginId() + "_" + appUserDbData.getTenantId(), appUserDbData);
		}
	}

	private List<AppUserDbData> getUserTableDataFromDb() throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AppUserDbData> appUserDbDataList;
		String sqlQuery = getValTableDataSql.replace("{TABLE_NAME}", "APP_USER");
		try {
			appUserDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery), new Object[] {},
					new RowMapper<AppUserDbData>() {
						@Override
						public AppUserDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AppUserDbData appUserDbData = new AppUserDbData();
							appUserDbData.setAppUserId(rs.getInt("app_user_id"));
							appUserDbData.setUserLoginId(rs.getString("user_login_id"));
							appUserDbData.setUserPassword(rs.getString("user_password"));
							appUserDbData.setUserEmail(rs.getString("user_email"));
							appUserDbData.setUserFullName(rs.getString("user_full_name"));
							appUserDbData.setUserTypeCde(rs.getInt("user_type_cde"));
							appUserDbData.setAccessToken((rs.getString("access_token")));
							appUserDbData.setAccountNonExpired((rs.getBoolean("account_non_expired")));
							appUserDbData.setAccountNonLocked((rs.getBoolean("account_non_locked")));
							appUserDbData.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
							appUserDbData.setAccountEnabled(rs.getBoolean("account_enabled"));
							appUserDbData.setTenantId(rs.getString("tenant_id"));
							appUserDbData.setCreateBy(rs.getString("create_by"));
							appUserDbData.setCreateDtm(rs.getString("create_dtm"));
							appUserDbData.setLastModBy(rs.getString("last_mod_by"));
							appUserDbData.setLastModDtm(rs.getString("last_mod_dtm"));
							return appUserDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching user data from database", ex);
			throw new WorkbenchException("Error occured while fetching user data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getValTableDataSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getValTableDataSql" + "," + timeElapsed + ",secs");

		return appUserDbDataList;
	}

	@Override
	public AppUserDbData getUserDetailsFromLoginId(String userLoginId, String tenantId) throws WorkbenchException {
		long startTime = System.nanoTime();

		List<AppUserDbData> appUserDbDataList = new ArrayList<>();
		try {
			appUserDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getUserDetailsFromLoginIdSql),
					new Object[] { userLoginId, tenantId }, new RowMapper<AppUserDbData>() {
						@Override
						public AppUserDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AppUserDbData appUserDbData = new AppUserDbData();
							appUserDbData.setAppUserId(rs.getInt("app_user_id"));
							appUserDbData.setUserLoginId(rs.getString("user_login_id"));
							

							return appUserDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching user data from database", ex);
			throw new WorkbenchException("Error occured while fetching  user data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserDetailsFromLoginIdSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserDetailsFromLoginIdSql" + "," + timeElapsed + ",secs");

		if (appUserDbDataList.size() > 0) {
			return appUserDbDataList.get(0);
		}

		return null;
	}

	@Override
	public List<UserTeammateDbData> getTeammateUserListDetails() throws WorkbenchException {
		long startTime = System.nanoTime();
		long appUserId=SessionHelper.getLoginUserData().getUserId();
		List<UserTeammateDbData> userTeammateDbDataList = new ArrayList<>();
		try {
			userTeammateDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getTeammatesDetailsSql),
					new Object[] { appUserId,SessionHelper.getTenantId(),appUserId,
							SessionHelper.getTenantId() },
					new RowMapper<UserTeammateDbData>() {
						@Override
						public UserTeammateDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserTeammateDbData userTeammateDbData = new UserTeammateDbData();
							userTeammateDbData.setAppUserId(rs.getInt("app_user_id"));
							userTeammateDbData.setUserFullName((rs.getString("user_full_name")));
							userTeammateDbData.setUserEmail((rs.getString("user_email")));
							userTeammateDbData.setUserLoginId((rs.getString("user_login_id")));
							userTeammateDbData.setUserTypeCde((rs.getInt("user_type_cde")));
							userTeammateDbData.setUserTypeTxt((rs.getString("user_type_txt")));
							userTeammateDbData.setUserRoleTypeCde((rs.getInt("role_type_cde")));
							userTeammateDbData.setUserRoleTypeTxt((rs.getString("role_type_txt")));
							userTeammateDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							userTeammateDbData.setQueueNameTxt(rs.getString("queue_name_txt"));
							userTeammateDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							userTeammateDbData.setDocTypeTxt(rs.getString("doc_type_txt"));
							return userTeammateDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getTeammatesDetailsSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getTeammatesDetailsSql" + "," + timeElapsed + ",secs");
		return userTeammateDbDataList;

	}

}
