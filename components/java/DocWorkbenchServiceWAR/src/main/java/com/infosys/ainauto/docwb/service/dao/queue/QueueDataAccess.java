/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.QueueCountResData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateUserQueueReqData;
//import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.QueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;

@Component
public class QueueDataAccess implements IQueueDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(QueueDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addQueue}")
	private String addQueueSql;

	@Value("${addQueueWithSequenceVal}")
	private String addQueueWithSequenceValSql;

	@Value("${getDocCount}")
	private String getDocCountSql;

//	@Value("${getDocCountForUser}")
//	private String getDocCountForUserSql;

	@Value("${getQueueUsers}")
	private String getQueueUsersSql;

	@Value("${getQueues}")
	private String getQueuesSql;
	
	@Value("${getUserQueueAllRelationship}")
	private String getUserQueueAllRelationshipSql;
	
	@Value("${updatePersonalQueueVisibility}")
	private String updatePersonalQueueVisibilitySql;
	
	@Value("${updateQueueClosure}")
	private String updateQueueClosureSql;
	
	@Value("${updateAllUserQueueVisibility}")
	private String updateAllUserQueueVisibilitySql;
	
//	@Value("${getDocCountForDashboard}")
//	private String getDocCountForDashboardSql;
	
//	@Value("${getCommonQueueCount}")
//	private String getCommonQueueCountSql;
	

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<QueueCountResData> getDocCount(long queueNameCde,boolean assignmentCount) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<QueueCountResData> queueCountList = new ArrayList<QueueCountResData>();
		String getCountOFDocSql=getDocCountSql;
		try {
			if (assignmentCount) {
//				getCountOFDocSql = getDocCountForDashboardSql;
//				getCountOFDocSql = getCountOFDocSql.replace(WorkbenchConstants.DOC_COUNT_FOR_USER_FILTER,
//						getDocCountSql);
				queueCountList = jdbcTemplate.query(StringUtility.sanitizeSql(getCountOFDocSql),
						new Object[] { SessionHelper.getTenantId(),SessionHelper.getTenantId(),
								true,queueNameCde,false,null,null,true,SessionHelper.getLoginUserData().getUserId()
								,SessionHelper.getTenantId()},
						new RowMapper<QueueCountResData>() {
							@Override
							public QueueCountResData mapRow(ResultSet rs, int rowNum) throws SQLException {

								QueueCountResData queueDbData = new QueueCountResData();
								queueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
								queueDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
								queueDbData.setMyCasesCount(rs.getInt(3));
//								TODO:for future requirement of unassigned_count,assigned_count,mycase_count
//								queueDbData.setDocCount(rs.getInt(3));
//								queueDbData.setAssignedCount(rs.getInt("assigned_count"));
//								queueDbData.setUnassignedCount(rs.getInt("unassigned_count"));
//								queueDbData.setMyCasesCount(rs.getInt("mycase_count"));
								return queueDbData;
							}
						});
			} else {
//				modified getDocCount sql in query.properties
				queueCountList = jdbcTemplate.query(StringUtility.sanitizeSql(getCountOFDocSql),
						new Object[] { SessionHelper.getTenantId(),SessionHelper.getTenantId(),
								true,queueNameCde,false,null,null,false,null,null }, 
						new RowMapper<QueueCountResData>() {
							@Override
							public QueueCountResData mapRow(ResultSet rs, int rowNum) throws SQLException {

								QueueCountResData queueDbData = new QueueCountResData();
								queueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
								queueDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
								queueDbData.setDocCount(rs.getInt(3));
								return queueDbData;
							}
						});
			}
		} catch (Exception ex) {
			logger.error("Error occured while fetching Data from database", ex);
			throw new WorkbenchException("Error occured while fetching Data from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for "+getCountOFDocSql+" query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocCountSql" + "," + timeElapsed + ",secs");
		return queueCountList;
	}

	public List<QueueCountResData> getDocCountForUser(long appUserId, boolean assignmentCount,List<Long> assignedToList)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<QueueCountResData> queueCountList = new ArrayList<QueueCountResData>();
//		String getCountOFDocForUserSql = getDocCountForUserSql;
		String getCountOFDocForUserSql=getDocCountSql;
		Object[] inputParameter=null;
		try {
			if (assignmentCount) {
//				String userId="'"+appUserId+"'";
//				getCountOFDocForUserSql = getDocCountForDashboardSql;
				
				if(ListUtility.hasValue(assignedToList)) {
					// When assigned to is given , to get case assigned to those users in MyCasesCount
//					userId=assignedTo; 
//					getCountOFDocForUserSql = getCountOFDocForUserSql.replace(WorkbenchConstants.DOC_COUNT_FOR_USER_FILTER,
//							getCommonQueueCountSql);
//					getCountOFDocForUserSql = getCountOFDocForUserSql.replace(WorkbenchConstants.APP_USER_ID_ARRAY_FILTER,
//							userId);
					 inputParameter=new Object[] {SessionHelper.getTenantId(),SessionHelper.getTenantId(),false,null,
							true,appUserId,SessionHelper.getTenantId(),true,assignedToList.get(0),SessionHelper.getTenantId()};
				}else{
//					getCountOFDocForUserSql = getCountOFDocForUserSql.replace(WorkbenchConstants.DOC_COUNT_FOR_USER_FILTER,
//							getDocCountForUserSql);
//					getCountOFDocForUserSql = getCountOFDocForUserSql.replace(WorkbenchConstants.APP_USER_ID_ARRAY_FILTER,
//							userId);
					inputParameter=new Object[] {SessionHelper.getTenantId(),SessionHelper.getTenantId(),false,null,
							true,appUserId,SessionHelper.getTenantId(),true,appUserId,SessionHelper.getTenantId()};
				}
				
				queueCountList = jdbcTemplate.query(StringUtility.sanitizeSql(getCountOFDocForUserSql),
//						new Object[] { SessionHelper.getTenantId(), appUserId, SessionHelper.getTenantId(),
//								SessionHelper.getTenantId(),SessionHelper.getTenantId(),SessionHelper.getTenantId()},
						inputParameter,new RowMapper<QueueCountResData>() {
							@Override
							public QueueCountResData mapRow(ResultSet rs, int rowNum) throws SQLException {

								QueueCountResData queueDbData = new QueueCountResData();
								queueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
								queueDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
								queueDbData.setMyCasesCount(rs.getInt(3));
//								TODO:for future requirement of unassigned_count,assigned_count,mycase_count
//								queueDbData.setDocCount(rs.getInt(3));
//								queueDbData.setAssignedCount(rs.getInt("assigned_count"));
//								queueDbData.setUnassignedCount(rs.getInt("unassigned_count"));
//								queueDbData.setMyCasesCount(rs.getInt("mycase_count"));
								return queueDbData;
							}
						});
			} else {
//				if(StringUtility.hasValue(assignedTo)) {
//					getCountOFDocForUserSql=getCommonQueueCountSql;
//					getCountOFDocForUserSql = getCountOFDocForUserSql.replace(WorkbenchConstants.APP_USER_ID_ARRAY_FILTER,
//							assignedTo);
//				}
				//when queue name not given ,calling getDocCount
//				getCountOFDocForUserSql=getDocCountSql;
				queueCountList = jdbcTemplate.query(StringUtility.sanitizeSql(getCountOFDocForUserSql),
//						new Object[] { SessionHelper.getTenantId(),SessionHelper.getTenantId(),
//								false,null,true,appUserId,SessionHelper.getTenantId(),true,appUserId,SessionHelper.getTenantId() },
						new Object[] { SessionHelper.getTenantId(),SessionHelper.getTenantId(),false,null,true,appUserId,
								SessionHelper.getTenantId(),false,null,null},
						new RowMapper<QueueCountResData>() {
							@Override
							public QueueCountResData mapRow(ResultSet rs, int rowNum) throws SQLException {

								QueueCountResData queueDbData = new QueueCountResData();
								queueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
								queueDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
								queueDbData.setDocCount(rs.getInt(3));
								return queueDbData;
							}
						});
			}
		} catch (Exception ex) {
			logger.error("Error occured while fetching Data from database", ex);
			throw new WorkbenchException("Error occured while fetching Data from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for" + getCountOFDocForUserSql + "query (secs):" + timeElapsed);
		PERF_LOGGER.info(getCountOFDocForUserSql + "," + timeElapsed + ",secs");
		return queueCountList;
	}

	@Override
	public List<UserRoleDbData> getQueueUsers(long queueNameCde) throws WorkbenchException {
		long startTime = System.nanoTime();

		List<UserRoleDbData> userRoleDbDataList = new ArrayList<>();
		try {
			userRoleDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getQueueUsersSql),
					new Object[] { queueNameCde, SessionHelper.getTenantId() }, new RowMapper<UserRoleDbData>() {
						@Override
						public UserRoleDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleDbData userRoleDbData = new UserRoleDbData();
							userRoleDbData.setAppUserId(rs.getInt("app_user_id"));
							userRoleDbData.setUserLoginId(rs.getString("user_login_id"));
							userRoleDbData.setUserFullName((rs.getString("user_full_name")));
							userRoleDbData.setUserRoleTypeCde(rs.getInt("role_type_cde"));
							userRoleDbData.setUserRoleTypeTxt(rs.getString("role_type_txt"));
							return userRoleDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getQueueUsersSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getQueueUsersSql" + "," + timeElapsed + ",secs");
		return userRoleDbDataList;

	}

	@Override
	public List<ValTableDbData> getQueues() throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ValTableDbData> valTableDbDataList;
		try {
			valTableDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getQueuesSql),
					new Object[] { SessionHelper.getTenantId() }, new RowMapper<ValTableDbData>() {
						@Override
						public ValTableDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ValTableDbData taskStatusValDbData = new ValTableDbData();
							taskStatusValDbData.setCde(rs.getInt("queue_name_cde"));
							taskStatusValDbData.setTxt(rs.getString("queue_name_txt"));
							taskStatusValDbData.setCreateBy(rs.getString("create_by"));
							taskStatusValDbData.setCreateDtm(rs.getTimestamp("create_dtm"));
							taskStatusValDbData.setLastModBy(rs.getString("last_mod_by"));
							taskStatusValDbData.setLastModDtm(rs.getTimestamp("last_mod_dtm"));

							return taskStatusValDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching queue data from database", ex);
			throw new WorkbenchException("Error occured while fetching queue data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getQueuesSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getQueuesSql" + "," + timeElapsed + ",secs");

		return valTableDbDataList;

	}

	@Override
	public QueueDbData addQueue(QueueDbData queueDBData)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		QueueDbData dbData = new QueueDbData();
		try {
			String addNewQueueSql = addQueueWithSequenceValSql;
			if (queueDBData.getQueueNameCde() > 0) {
				addNewQueueSql = addQueueSql;
			}

			String tempSQL[] = StringUtility.sanitizeSql(addNewQueueSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, queueDBData.getQueueNameTxt());
					ps.setInt(2, queueDBData.getDocTypeCde());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					if (queueDBData.getQueueNameCde() > 0) {
						ps.setLong(5, queueDBData.getQueueNameCde());
					}

					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			dbData.setQueueNameCde(keyHolder.getKey().longValue());

		} catch (DuplicateKeyException ex) {
			dbData.setQueueNameCde(-1);
		} catch (Exception ex) {
			logger.error("Error occured while adding queue in database", ex);
			throw new WorkbenchException("Error occured while adding queue in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addQueueSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addQueueSql" + "," + timeElapsed + ",secs");
		return dbData;
	}
	@Override
	public List<UserQueueDbData> getQueueListOfUser(long appUserId,String queueStatus) throws WorkbenchException{
		long startTime = System.nanoTime();
		List<UserQueueDbData> userQueueDbData = new ArrayList<UserQueueDbData>();
		String getQueueListOfCurrentUserSql="";
		if (queueStatus.toUpperCase().contentEquals("OPEN")) { 
			getQueueListOfCurrentUserSql="SELECT * FROM (" + getUserQueueAllRelationshipSql + 
					") USER_QUEUE_DTL WHERE QUEUE_STATUS = 'OPEN' OR QUEUE_STATUS ='SCHEDULED'" ;
		}
		else
		{
			getQueueListOfCurrentUserSql="SELECT * FROM (" + getUserQueueAllRelationshipSql + 
				") USER_QUEUE_DTL WHERE QUEUE_STATUS = 'CLOSED' " ;
		}
		try {

			userQueueDbData = jdbcTemplate.query(StringUtility.sanitizeSql(getQueueListOfCurrentUserSql),
					new Object[] { appUserId, SessionHelper.getTenantId() }, new RowMapper<UserQueueDbData>() {
						@Override
						public UserQueueDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							UserQueueDbData userQueueDbData = new UserQueueDbData();
							userQueueDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							userQueueDbData.setQueueNameTxt(rs.getString("queue_name_txt"));
							userQueueDbData.setQueueClosedDtm(rs.getString("queue_closed_dtm"));
							userQueueDbData.setQueueStatus(rs.getString("queue_status"));
							userQueueDbData.setQueueHideAfterDtm(rs.getString("queue_hide_after_dtm"));
							userQueueDbData.setUserQueueHideAfterDtm(rs.getString("user_queue_hide_after_dtm"));
							return userQueueDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching Data from database", ex);
			throw new WorkbenchException("Error occured while fetching Data from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getQueueListOfCurrentUserSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getQueueListOfCurrentUserSql ," + timeElapsed + ",secs");
		return userQueueDbData;
		
	}
	
	@Override
	public long updatePersonalQueueVisibility(List<UpdateUserQueueReqData> updateUserQueueReqDataList) throws WorkbenchException {
		
		long startTime = System.nanoTime();
		long appUserQueueRelId = 0;
		long updatedRowCount=0;
		Date queue_hide_after_dtm=null;
		try {
			for(UpdateUserQueueReqData updateUserQueueReqData:updateUserQueueReqDataList) {
				int queueNameCde = updateUserQueueReqData.getQueueNameCde();
				if (StringUtility.hasTrimmedValue(updateUserQueueReqData.getUserQueueHideAfterDtm())) {
					queue_hide_after_dtm = DateUtility.toTimestamp(updateUserQueueReqData.getUserQueueHideAfterDtm(),
							WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					if (queue_hide_after_dtm == null) {
						queue_hide_after_dtm = DateUtility.toTimestamp(updateUserQueueReqData.getUserQueueHideAfterDtm(),
								WorkbenchConstants.API_DATE_FORMAT);
					}
				}
				
				Object object[] = new Object[] { queue_hide_after_dtm, SessionHelper.getLoginUsername(), queueNameCde,
								SessionHelper.getLoginUserData().getUserId(), SessionHelper.getTenantId() };
				Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(updatePersonalQueueVisibilitySql),object);
				appUserQueueRelId = Long.parseLong(String.valueOf(sqlResponse.get("APP_USER_QUEUE_REL_ID")));
				
				if(appUserQueueRelId>0) {
					updatedRowCount+=1;
				}					
			}

			if(updatedRowCount>0) {
				logger.info(updatedRowCount+" rows updated");
			} else {
				logger.info("No rows updated.");
			}
			
		} catch (Exception ex) {
			logger.error("Error occured while updating Personal Queue Visibility date in database", ex);
			throw new WorkbenchException("Error occured while updating Personal Queue Visibility date in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updatePersonalQueueVisibilitySql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updatePersonalQueueVisibilitySql ," + timeElapsed + ",secs");
		return updatedRowCount;
	}
	
	@Override
	public long updateQueueDetails (List<UpdateQueueReqData> updateQueueReqDataList) throws WorkbenchException {
		long startTime = System.nanoTime();
		long dbReturnQueueNameCde = 0;
		long updatedRowCount=0;
		long updatedVisibilityRowCount=0;
		Date end_dtm=null;
		Date queueHideAfterDtm=null;
		long updatedAppUserQueueRelIdCount=0;
		try {
			for(UpdateQueueReqData updateQueueReqData:updateQueueReqDataList) {
				int queueNameCde = updateQueueReqData.getQueueNameCde();
				if (StringUtility.hasTrimmedValue(updateQueueReqData.getEndDtm())) {
					end_dtm = DateUtility.toTimestamp(updateQueueReqData.getEndDtm(),
							WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					if (end_dtm == null) {
						end_dtm = DateUtility.toTimestamp(updateQueueReqData.getEndDtm(),
								WorkbenchConstants.API_DATE_FORMAT);
					}
				}
				if (StringUtility.hasTrimmedValue(updateQueueReqData.getQueueHideAfterDtm())) {
					queueHideAfterDtm = DateUtility.toTimestamp(updateQueueReqData.getQueueHideAfterDtm(),
							WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					if (queueHideAfterDtm == null) {
						queueHideAfterDtm = DateUtility.toTimestamp(updateQueueReqData.getQueueHideAfterDtm(),
								WorkbenchConstants.API_DATE_FORMAT);
					}	
				}
				
				Object object[] = new Object[] { end_dtm,queueHideAfterDtm, SessionHelper.getLoginUsername(),
								queueNameCde,SessionHelper.getTenantId() };
				Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(updateQueueClosureSql),object);
				dbReturnQueueNameCde =Integer.parseInt(String.valueOf(sqlResponse.get("QUEUE_NAME_CDE")));
				
				if (StringUtility.hasTrimmedValue(updateQueueReqData.getQueueHideAfterDtm())) {
					Object visibilityObject[] = new Object[] { queueHideAfterDtm, SessionHelper.getLoginUsername(),
							queueNameCde,SessionHelper.getTenantId() };
					Map<String, Object> allUserQueueVisibilitySqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(updateAllUserQueueVisibilitySql)
																				,visibilityObject);

					updatedAppUserQueueRelIdCount=Long.parseLong(String.valueOf(allUserQueueVisibilitySqlResponse.get("APP_USER_QUEUE_REL_ID_COUNT")));
				}
		
				if(dbReturnQueueNameCde == queueNameCde) {
					updatedRowCount+=1;
				}	
				
				if(updatedAppUserQueueRelIdCount > 0) {
					updatedVisibilityRowCount+=updatedAppUserQueueRelIdCount;
				}
			}

			if(updatedRowCount>0) {
				logger.info(updatedRowCount+" rows updated by updateQueueClosureSql");
			} else {
				logger.info("No rows updated by updateQueueClosureSql.");
			}
			
			if(updatedVisibilityRowCount>0) {
				logger.info(updatedVisibilityRowCount+" rows updated by updateAllUserQueueVisibilitySql");
			} else {
				logger.info("No rows updated by updateAllUserQueueVisibilitySql.");
			}
			
		} catch (Exception ex) {
			logger.error("Error occured while updating Queue Closure date in database", ex);
			throw new WorkbenchException("Error occured while updating Queue Closure date in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateQueueClosureSql and updateAllUserQueueVisibilitySql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateQueueClosureSql and updateAllUserQueueVisibilitySql" + "," + timeElapsed + ",secs");
		return updatedRowCount;
	}
	
}
