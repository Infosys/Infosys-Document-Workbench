/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attachment;

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

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttaAttaRelReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;

@Component
@Profile("default")
public class AttachmentDataAccess implements IAttachmentDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(AttachmentDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addAttachment}")
	private String addAttachmentSql;

	@Value("${getAttachmentList}")
	private String getAttachmentListSql;

	@Value("${addDocAttachmentRel}")
	private String addDocAttachmentRelSql;

	@Value("${getAttachmentListEmail}")
	private String getAttachmentListEmailSql;

	@Value("${addEmailOutboundAttachmentRel}")
	private String addEmailOutboundAttachmentSql;
	
	@Value("${addAttaAttaRel}")
	private String addAttaAttaRelSql;
	
	@Value("${getDocAttaRelCount}")
	private String getDocAttaRelCountSql;
	
	@Value("${getAttaAttaRelCount}")
	private String getAttaAttaRelCountSql;
	

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<AttachmentDbData> getDocAttachmentList(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttachmentDbData> attachmentDbDataList = new ArrayList<>();

		try {
			attachmentDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getAttachmentListSql),
					new Object[] { docId, SessionHelper.getTenantId() }, new RowMapper<AttachmentDbData>() {
						@Override
						public AttachmentDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AttachmentDbData attachmentDbData = new AttachmentDbData();
							attachmentDbData.setAttachmentId(rs.getLong("attachment_id"));
							attachmentDbData.setLogicalName(rs.getString("logical_name"));
							attachmentDbData.setPhysicalName(rs.getString("physical_name"));
							attachmentDbData.setInlineImage(rs.getBoolean("is_inline_image"));
							attachmentDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
							attachmentDbData.setGroupName(rs.getString("group_name"));
							return attachmentDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attachment list from database", ex);
			throw new WorkbenchException("Error occured while fetching attachment list from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttachmentListSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttachmentListSql" + "," + timeElapsed + ",secs");
		return attachmentDbDataList;
	}

	@Override
	public long addAttachment(AttachmentDbData attachmentDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attachmentId = -1;

		try {
			String tempSQL[] = StringUtility.sanitizeSql(addAttachmentSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, attachmentDbData.getLogicalName());
					ps.setString(2, attachmentDbData.getPhysicalName());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setBoolean(4, attachmentDbData.isInlineImage());
					ps.setLong(5, attachmentDbData.getExtractTypeCde());
					ps.setString(6, attachmentDbData.getGroupName());
					ps.setString(7, SessionHelper.getTenantId());
					ps.setBoolean(8, attachmentDbData.isPrimary());
					ps.setLong(9, attachmentDbData.getSequenceNum());
					return ps;
				}
			};
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			attachmentId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding attachment to database", ex);
			throw new WorkbenchException("Error occured while adding attachment to database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addAttachmentSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addAttachmentSql" + "," + timeElapsed + ",secs");
		return attachmentId;
	}

	@Override
	public List<AttachmentDbData> getAttachmentListEmail(long emailOutboundId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttachmentDbData> attachmentDbDataList = new ArrayList<>();

		try {
			attachmentDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getAttachmentListEmailSql),
					new Object[] { emailOutboundId, SessionHelper.getTenantId() }, new RowMapper<AttachmentDbData>() {
						@Override
						public AttachmentDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							AttachmentDbData attachmentDbData = new AttachmentDbData();
							attachmentDbData.setAttachmentId(rs.getLong("attachment_id"));
							attachmentDbData.setLogicalName(rs.getString("logical_name"));
							attachmentDbData.setPhysicalName(rs.getString("physical_name"));
							attachmentDbData.setInlineImage(rs.getBoolean("is_inline_image"));
							attachmentDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
							attachmentDbData.setGroupName(rs.getString("group_name"));
							return attachmentDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attachment list from database", ex);
			throw new WorkbenchException("Error occured while fetching attachment list from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttachmentListEmailSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttachmentListEmailSql" + "," + timeElapsed + ",secs");
		return attachmentDbDataList;
	}

	@Override
	public List<Long> addDocAttachmentRel(List<Long> attachmentDbDataList, long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<Long> docAttachmentRelIdList = new ArrayList<Long>();
		long docAttachmentId = 0;
		try {
			for (int i = 0; i < attachmentDbDataList.size(); i++) {
				long attachmentId = attachmentDbDataList.get(i);
				String tempSQL[] = StringUtility.sanitizeSql(addDocAttachmentRelSql).toLowerCase()
						.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
				String appSQL = tempSQL[0].trim();
				String outParam = tempSQL[1].trim();
				final PreparedStatementCreator psc = new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
						final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

						ps.setLong(1, docId);
						ps.setLong(2, attachmentId);
						ps.setString(3, SessionHelper.getLoginUsername());
						ps.setString(4, SessionHelper.getTenantId());
						return ps;
					}
				};

				KeyHolder keyHolder = new GeneratedKeyHolder();
				jdbcTemplate.update(psc, keyHolder);
				docAttachmentId = keyHolder.getKey().longValue();
				if (docAttachmentId > 0) {
					docAttachmentRelIdList.add(docAttachmentId);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occured while adding document attachment relationship in database", ex);
			throw new WorkbenchException("Error occured while adding document attachment relationship in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addDocAttachmentRelSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addDocAttachmentRelSql" + "," + timeElapsed + ",secs");
		return docAttachmentRelIdList;
	}

	@Override
	public long addEmailOutboundAttachmentRel(List<Long> attachmentDbDataList, long emailOutboundId)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();
			for (int i = 0; i < attachmentDbDataList.size(); i++) {
				long attachmentId = attachmentDbDataList.get(i);
				object = new Object[] { emailOutboundId, attachmentId, SessionHelper.getLoginUsername(),
						SessionHelper.getTenantId() };
				objectList.add(object);
			}
			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(addEmailOutboundAttachmentSql),
					objectList);
			sum = rowsImpacted.length;
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while adding emailOutbound attachment relationship in database", ex);
			throw new WorkbenchException("Error occured while adding emailOutbound attachment relationship in database",
					ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addEmailOutboundAttachmentSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addEmailOutboundAttachmentSql" + "," + timeElapsed + ",secs");
		return sum;
	}
	
	@Override
	public long addAttaAttaRel(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attaAttaRelId = 0;
		try {
				String tempSQL[] = StringUtility.sanitizeSql(addAttaAttaRelSql).toLowerCase()
						.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
				String appSQL = tempSQL[0].trim();
				String outParam = tempSQL[1].trim();
				final PreparedStatementCreator psc = new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
						final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

						ps.setLong(1, attachmentRelReqData.getAttachmentId1());
						ps.setLong(2, attachmentRelReqData.getAttachmentId2());
						ps.setString(3, SessionHelper.getTenantId());
						ps.setLong(4, attachmentRelReqData.getAttaRelTypeCde());
						ps.setString(5, SessionHelper.getLoginUsername());
						
						return ps;
					}
				};

				KeyHolder keyHolder = new GeneratedKeyHolder();
				jdbcTemplate.update(psc, keyHolder);
				attaAttaRelId = keyHolder.getKey().longValue();
				
			
		} catch (Exception ex) {
			logger.error("Error occured while adding attachment attachment relationship in database", ex);
			throw new WorkbenchException("Error occured while adding attachment attachment relationship in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addAttaAttaRelSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addAttaAttaRelSql" + "," + timeElapsed + ",secs");
		return attaAttaRelId;
	}
	
	@Override
	public long countAttaAttaDocExist(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long docAttaRelCount=0;
		String sqlQuery=getDocAttaRelCountSql;
		String attachmentFilter = "("+attachmentRelReqData.getAttachmentId1()+","+attachmentRelReqData.getAttachmentId2()+")";
		try {
			sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTACH_FILTER, attachmentFilter);
			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { attachmentRelReqData.getDocId(), SessionHelper.getTenantId()},new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			docAttaRelCount=count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching document's attachment relation record from database", ex);
			throw new WorkbenchException("Error occured while fetching document's attachment relation record from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocAttaRelCountSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocAttaRelCountSql" + "," + timeElapsed + ",secs");
		return docAttaRelCount;
	}
	
	@Override
	public long countAttaAttaRelRecord(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException{
		long startTime = System.nanoTime();
		long attaAttaRelCount=0;
		String sqlQuery=getAttaAttaRelCountSql;
		
		try {

			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] {attachmentRelReqData.getAttachmentId1(),attachmentRelReqData.getAttachmentId2(),
							SessionHelper.getTenantId(),attachmentRelReqData.getAttaRelTypeCde()},new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			attaAttaRelCount=count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching attachment attachment relation record from database", ex);
			throw new WorkbenchException("Error occured while fetching attachment attachment relation record from database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttaAttaRelCountSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttaAttaRelCountSql" + "," + timeElapsed + ",secs");
		return attaAttaRelCount;
	}
}
