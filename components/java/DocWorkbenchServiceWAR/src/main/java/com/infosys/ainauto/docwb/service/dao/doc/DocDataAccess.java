/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.doc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.SqlUtil;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAttrWrapperDbData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData.AttributeData;
import com.infosys.ainauto.docwb.service.model.db.DocUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
@Profile("default")
public class DocDataAccess implements IDocDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(DocDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addDocument}")
	private String addDocumentSql;

	@Value("${deleteDocument}")
	private String deleteDocumentSql;

	@Value("${getDocumentList}")
	private String getDocumentListSql;
	
	@Value("${getDocByAttr}")
	private String getDocByAttrSql;
	
	@Value("${getDocumentListV2}")
	private String getDocumentListV2Sql;
	
	@Value("${sqGetDocOnSearchCriteria}")
	private String sqGetDocOnSearchCriteriaSql;
	
	@Value("${getDocAttachAttrList}")
	private String getDocAttachAttrListSql;

	@Value("${updateDocActionStatus}")
	private String updateDocActionStatusSql;

	@Value("${insertDocEventType}")
	private String insertDocEventTypeSql;

	@Value("${getUserDocDetails}")
	private String getUserDocDetailsSql;

	@Value("${getDocDetails}")
	private String getDocDetailsSql;

	@Value("${addUserDocRelationship}")
	private String addUserDocRelationshipSql;
	
	@Value("${getDocUserDetails}")
	private String getDocUserDetailsSql;

	@Value("${updateDocAppUser}")
	private String updateDocAppUserSql;

	@Value("${getTotalCountOnFilter}")
	private String getTotalCountOnFilterSql;
	
	@Value("${sqGetDocOnFilterV3}")
	private String sqGetDocOnFilterV3Sql;
	
	@Value("${sqGetDocAttrDtl}")
	private String sqGetDocAttrDtlSql;
	
	@Value("${sqGetAttachAttrDtl}")
	private String sqGetAttachAttrDtlSql;
	
	@Value("${pageSize}")
	private int pageSize;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long addDocument(DocumentDbData documentDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long documentId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addDocumentSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, documentDbData.getDocTypeCde());
					ps.setString(2, documentDbData.getDocLocation());
					ps.setLong(3, documentDbData.getLockStatusCde());
					ps.setLong(4, documentDbData.getQueueNameCde());
					ps.setLong(5, documentDbData.getTaskStatusCde());
					ps.setString(6, SessionHelper.getLoginUsername());
					ps.setString(7, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			documentId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while updating rules data in database", ex);
			throw new WorkbenchException("Error occured while updating rules data in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addDocumentSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addDocumentSql" + "," + timeElapsed + ",secs");
		return documentId;
	}

	public long deleteDocument(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long docIdOut = 0;
		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection
							.prepareStatement(StringUtility.sanitizeSql(deleteDocumentSql));
					ps.setString(1, SessionHelper.getLoginUsername());
					ps.setLong(2, docId);
					ps.setString(3, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowUpdated = jdbcTemplate.update(psc);
			if (noOfRowUpdated > 0) {
				docIdOut = docId;
			} else {
				logger.info("No rows updated");
			}

		} catch (Exception ex) {
			logger.error("Error occured while deleting document in database", ex);
			throw new WorkbenchException("Error occured while deleting document in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteDocumentSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteDocumentSql" + "," + timeElapsed + ",secs");
		return docIdOut;
	}

	public long getTotalDocCount(DocumentDbData documentDbDataIn) throws WorkbenchException {
		long startTime = System.nanoTime();
		boolean isSqlBlockLatestEventRequired = false;
		Long total;
		String sqlQuery = getTotalCountOnFilterSql;
		String sqlWhere = "";		
		if (documentDbDataIn.getTaskStatusCde() > EnumTaskStatus.UNDEFINED.getValue()
				&& (documentDbDataIn.getTaskStatusOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "TASK_STATUS_CDE" + documentDbDataIn.getTaskStatusOperator()
					+ documentDbDataIn.getTaskStatusCde();
		}
		if ((documentDbDataIn.getHighestEventTypeCde() > 0)
				&& (documentDbDataIn.getHighestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "HIGHEST_EVENT_TYPE_CDE" + documentDbDataIn.getHighestEventTypeOperator()
					+ documentDbDataIn.getHighestEventTypeCde();
		} else {
			sqlQuery = SqlUtil.removeSqlBlock(sqlQuery, WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_BEGIN,
					WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_END);
		}
		if ((documentDbDataIn.getLatestEventTypeCde() > 0) && (documentDbDataIn.getLatestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LATEST_EVENT_TYPE_CDE" + documentDbDataIn.getLatestEventTypeOperator()
					+ documentDbDataIn.getLatestEventTypeCde();
			isSqlBlockLatestEventRequired = true;
		}
		if (documentDbDataIn.getLockStatusCde() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LOCK_STATUS_CDE=" + documentDbDataIn.getLockStatusCde();
		}
		if (documentDbDataIn.getAppUserId() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID=" + documentDbDataIn.getAppUserId();
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_UNASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NULL";
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_ASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NOT NULL AND APP_USER_ID <>"+SessionHelper.getLoginUserData().getUserId();
		} else if(documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_FOR_MY_REVIEW) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "CASE_REVIEWER_ID=" + SessionHelper.getLoginUserData().getUserId();
		} 
		
		if(documentDbDataIn.getDocIdOperationList()!=null && documentDbDataIn.getDocIdOperationList().size()==1) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			for(DocumentDbData.DocIdOperation docIdOperation:documentDbDataIn.getDocIdOperationList())
				{
				sqlWhere+= "DOC_ID"+docIdOperation.getOperator() + docIdOperation.getDocId();
					
				}
		}if(documentDbDataIn.getDocIdOperationList()!=null && documentDbDataIn.getDocIdOperationList().size()==2) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			String fromDocId="";
			String toDocId="";		
			for(DocumentDbData.DocIdOperation docIdOperation:documentDbDataIn.getDocIdOperationList())
				{
					if(docIdOperation.getOperator().equals(">="))
					{
						fromDocId+= docIdOperation.getDocId();
					}
					if(docIdOperation.getOperator().equals("<="))
					{
						toDocId+=docIdOperation.getDocId();
					}
				}
			sqlWhere += "DOC_ID BETWEEN '" +fromDocId + "' AND '" + toDocId + "'";
		}
		if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromEventDtm()) && StringUtility.hasTrimmedValue(documentDbDataIn.getToEventDtm())) {
			String toEventDtm = getUpdatedDate(documentDbDataIn.getToEventDtm());
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "EVENT_DTM BETWEEN '" + documentDbDataIn.getFromEventDtm() + "' AND '" + toEventDtm + "'";
			isSqlBlockLatestEventRequired = true;
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromEventDtm())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "EVENT_DTM >='" + documentDbDataIn.getFromEventDtm() + "'";
			isSqlBlockLatestEventRequired = true;
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getToEventDtm())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";

			String toEventDtm = getUpdatedDate(documentDbDataIn.getToEventDtm());
			sqlWhere += "EVENT_DTM <='" + toEventDtm + "'";
			isSqlBlockLatestEventRequired = true;
		}
		if (StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedTo())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "ASSIGNED_TO='" + documentDbDataIn.getAssignedTo() + "'";
			isSqlBlockLatestEventRequired = true;
		} else if (!StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedTo())
				&& StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedToKey())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "ASSIGNED_TO IS NULL";
			isSqlBlockLatestEventRequired = true;
		}
		
		if (sqlWhere != "") {
			sqlQuery += " WHERE " + sqlWhere;
		}
		if (!isSqlBlockLatestEventRequired) {
			sqlQuery = SqlUtil.removeSqlBlock(sqlQuery,
					WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_BEGIN, WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_END);
		}
		// Remove traces of multi-line comments left by SqlUtil.removeSqlBlock
		sqlQuery = SqlUtil.removeMultilineComments(sqlQuery);
		// Remove SQL block tags, if not removed already 
		for (String item : Arrays.asList(WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_BEGIN,
				WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_END, WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_BEGIN,
				WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_END)) {
			sqlQuery = sqlQuery.replace(item, "");
		}
		try {
			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { documentDbDataIn.getQueueNameCde(), SessionHelper.getTenantId() },
					new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			total = count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching count of document list data from database", ex);
			throw new WorkbenchException("Error occured while fetching count of document list data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getTotalCountOnFilterSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getTotalCountOnFilterSql" + "," + timeElapsed + ",secs");
		return total;
	}

	public long getTotalCountOnSearchCriteria(String docIdListStr,DocumentDbData documentDbDataIn) throws WorkbenchException {
		long startTime = System.nanoTime();
		Long total;
		String sqlQuery=sqGetDocOnSearchCriteriaSql;
		String attachmentFilter = "";
		
		if (docIdListStr==null) {
			docIdListStr="(0)";
		}
		sqlQuery += docIdListStr;
		attachmentFilter="AND ATT.LOGICAL_NAME ='"+documentDbDataIn.getDocumentName()+"'";
		sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTACH_FILTER, attachmentFilter);
		sqlQuery="SELECT COUNT(DOC.*)FROM("+sqlQuery+")DOC";
		try {
			List<Long> count = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { documentDbDataIn.getQueueNameCde(), SessionHelper.getTenantId() },
					new RowMapper<Long>() {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					});
			total = count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching count of document list data from database", ex);
			throw new WorkbenchException("Error occured while fetching count of document list data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for sqGetDocOnSearchCriteriaSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("sqGetDocOnSearchCriteriaSql" + "," + timeElapsed + ",secs");
		return total;
	}
	
	@Override
	public DocumentDbData getDocumentDetails(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		DocumentDbData documentDbData = new DocumentDbData();
		try {
			documentDbData = jdbcTemplate.query(StringUtility.sanitizeSql(getDocDetailsSql),
					new Object[] { docId, SessionHelper.getTenantId() }, new RowMapper<DocumentDbData>() {
						@Override
						public DocumentDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							DocumentDbData documentDbData = new DocumentDbData();
							documentDbData.setDocId(rs.getInt("doc_id"));
							documentDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							return documentDbData;
						}
					}).get(0);

		} catch (Exception ex) {
			logger.error("Error occured while fetching details for docId from database", ex);
			throw new WorkbenchException("Error occured while fetching details for docId from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocDetailsSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocDetailsSql" + "," + timeElapsed + ",secs");

		return documentDbData;
	}

	protected void populateAttachmentAttributes(DocumentDbData documentDbDataIn,
			List<DocAttrWrapperDbData> docAttrWrapperDbDataListParam) throws WorkbenchException {
		long startTime = System.nanoTime();
		String sqlQuery = getDocAttachAttrListSql;
		String docIds = docAttrWrapperDbDataListParam.stream()
				.map(docAttrWrapperDbData -> String.valueOf(docAttrWrapperDbData.getDocumentDbData().getDocId()))
				.collect(Collectors.joining(","));
		if (StringUtility.hasValue(docIds)) {
			String docFilter = " AND dar.doc_id in ( " + docIds + " ) ";
			sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_FILTER, docFilter);

			String attrFilter = " AND ATTR.ATTR_NAME_CDE IN (" + documentDbDataIn.getAttachmentAttrNameCdes() + ") ";
			sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);

			String sortOrder = StringUtility.hasValue(documentDbDataIn.getSortOrder()) ? documentDbDataIn.getSortOrder()
					: "DESC";
			sqlQuery += " ORDER BY DAR.DOC_ID " + sortOrder + " , ATTR.ATTR_NAME_CDE";
			try {
				jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery), new Object[] { SessionHelper.getTenantId() },
						new RowMapper<DocAttrWrapperDbData>() {
							@Override
							public DocAttrWrapperDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								DocAttrWrapperDbData docAttrWrapperDbData = new DocAttrWrapperDbData();
								AttributeDbData attributeDbData = new AttributeDbData();
								if (rs.getInt("attachment_attr_rel_id") > 0) {
									attributeDbData.setId(rs.getInt("attachment_attr_rel_id"));
									attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
									attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
									attributeDbData.setAttrValue(rs.getString("attr_value"));
									attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
									attributeDbData.setExtractTypeTxt(rs.getString("extract_type_txt"));
									attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
											.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
													RoundingMode.HALF_EVEN)
											.floatValue());
									docAttrWrapperDbData.setAttributeDbData(attributeDbData);
									long docId = rs.getLong("doc_id");
									for (int i = 0; i < docAttrWrapperDbDataListParam.size(); i++) {
										if (docAttrWrapperDbDataListParam.get(i).getDocumentDbData()
												.getDocId() == docId) {
											docAttrWrapperDbData.setDocumentDbData(
													docAttrWrapperDbDataListParam.get(i).getDocumentDbData());
											for (int j = i; j <= docAttrWrapperDbDataListParam.size(); j++) {
												if (j == docAttrWrapperDbDataListParam.size()
														|| docAttrWrapperDbDataListParam.get(j).getDocumentDbData()
																.getDocId() != docId) {
													docAttrWrapperDbDataListParam.add(j, docAttrWrapperDbData);
													break;
												}
											}
											break;
										}
									}
								}
								return docAttrWrapperDbData;
							}
						});
			} catch (Exception ex) {
				logger.error("Error occured while fetching document attachment attribute data from database", ex);
				throw new WorkbenchException(
						"Error occured while fetching document attachment attribute data from database", ex);
			} finally {

			}
			double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
			logger.info("Time taken for getDocAttachAttrListSql query (secs):" + timeElapsed);
			PERF_LOGGER.info("getDocAttachAttrListSql" + "," + timeElapsed + ",secs");
		}
	}

	private String getUpdatedDate(String dateStr) {
		
		Date date = DateUtility.toTimestamp(dateStr, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
		if (date==null)
		{
			int daysToAdd = 1;
			date = DateUtility.toTimestamp(dateStr, WorkbenchConstants.API_DATE_FORMAT);
			Date incrementedDate = DateUtility.addDate(date, daysToAdd);
			String incrementedDateStr = DateUtility.toString(incrementedDate, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
			return incrementedDateStr;
		}else
		{
			return dateStr;
		}
			
	}
	
	public List<DocAttrWrapperDbData> getDocumentList(DocumentDbData documentDbDataIn) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = new ArrayList<DocAttrWrapperDbData>();
		String sqlQuery = getDocumentListSql;
		String sqlQueryForFilter = sqGetDocOnFilterV3Sql.trim();
		String dateFilter1 = "";
		String dateFilter2 = "";
		String attrFilter = "";		
		String sqlWhere = "";
		String sortOrder = StringUtility.hasValue(documentDbDataIn.getSortOrder()) ? documentDbDataIn.getSortOrder()
				: "DESC";
		int offset = 0;
		int pageSizeVal=pageSize;
		boolean isSqlBlockLatestEventRequired = false;
		if(documentDbDataIn.getPageSize()>0)
		{
			pageSizeVal=documentDbDataIn.getPageSize();
		}
		if (documentDbDataIn.getPageNumber() > 1) {
			offset = pageSizeVal * (documentDbDataIn.getPageNumber() - 1);
		}
		if (StringUtility.hasValue(documentDbDataIn.getAttrNameCdes())) {
			attrFilter = "AND ATTR.ATTR_NAME_CDE IN (" + documentDbDataIn.getAttrNameCdes() + ") ";
		}
		if (documentDbDataIn.getTaskStatusCde() > EnumTaskStatus.UNDEFINED.getValue()
				&& (documentDbDataIn.getTaskStatusOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "TASK_STATUS_CDE" + documentDbDataIn.getTaskStatusOperator()
					+ documentDbDataIn.getTaskStatusCde();
		}
		if ((documentDbDataIn.getHighestEventTypeCde() > 0)
				&& (documentDbDataIn.getHighestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "HIGHEST_EVENT_TYPE_CDE" + documentDbDataIn.getHighestEventTypeOperator()
					+ documentDbDataIn.getHighestEventTypeCde();
		} else {
			sqlQueryForFilter = SqlUtil.removeSqlBlock(sqlQueryForFilter,
					WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_BEGIN, WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_END);
		}
		if ((documentDbDataIn.getLatestEventTypeCde() > 0) && (documentDbDataIn.getLatestEventTypeOperator() != null)) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LATEST_EVENT_TYPE_CDE" + documentDbDataIn.getLatestEventTypeOperator()
					+ documentDbDataIn.getLatestEventTypeCde();
			isSqlBlockLatestEventRequired = true;
		}
		if (documentDbDataIn.getLockStatusCde() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "LOCK_STATUS_CDE=" + documentDbDataIn.getLockStatusCde();
		}
		if (documentDbDataIn.getAppUserId() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID=" + documentDbDataIn.getAppUserId();
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_UNASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NULL";
		} else if (documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_IS_ASSIGNED) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "APP_USER_ID IS NOT NULL AND APP_USER_ID <>"+ SessionHelper.getLoginUserData().getUserId();
		}else if(documentDbDataIn.getAppUserId() == WorkbenchConstants.CASE_FOR_MY_REVIEW) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "CASE_REVIEWER_ID=" + SessionHelper.getLoginUserData().getUserId();
		} 
		if (documentDbDataIn.getDocId() > 0) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "DOC_ID=" + documentDbDataIn.getDocId();
			// Don't apply queue name code when passed as -1 along with docid 	
			if (documentDbDataIn.getQueueNameCde()==-1) {
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DOC_TABLE_FILTER, "");	
			}
		}
		if (documentDbDataIn.getQueueNameCde() !=-1 ) {
			sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DOC_TABLE_FILTER,
					"AND DOC.QUEUE_NAME_CDE = " + documentDbDataIn.getQueueNameCde());
		}		 
		
		if(documentDbDataIn.getDocIdOperationList()!=null && documentDbDataIn.getDocIdOperationList().size()==1) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			for(DocumentDbData.DocIdOperation docIdOperation:documentDbDataIn.getDocIdOperationList())
				{
				sqlWhere+= "DOC_ID"+docIdOperation.getOperator() + docIdOperation.getDocId();
					
				}
		}if(documentDbDataIn.getDocIdOperationList()!=null && documentDbDataIn.getDocIdOperationList().size()==2) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			String fromDocId="";
			String toDocId="";		
			for(DocumentDbData.DocIdOperation docIdOperation:documentDbDataIn.getDocIdOperationList())
				{
					if(docIdOperation.getOperator().equals(">="))
					{
						fromDocId+=docIdOperation.getDocId();
					}
					if(docIdOperation.getOperator().equals("<="))
					{
						toDocId+= docIdOperation.getDocId();
					}
					
				}
			sqlWhere += "DOC_ID BETWEEN '" +fromDocId + "' AND '" + toDocId + "'";
		}
		if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromEventDtm()) && StringUtility.hasTrimmedValue(documentDbDataIn.getToEventDtm())) {
			String toEventDtm = getUpdatedDate(documentDbDataIn.getToEventDtm());
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "EVENT_DTM BETWEEN '" + documentDbDataIn.getFromEventDtm() + "' AND '" + toEventDtm + "'";
			dateFilter1+="WHERE EVENT_DTM BETWEEN '" + documentDbDataIn.getFromEventDtm() + "' AND '" + toEventDtm + "'";
			dateFilter2+="AND DER.EVENT_DTM BETWEEN '" + documentDbDataIn.getFromEventDtm() + "' AND '" + toEventDtm + "'";
			isSqlBlockLatestEventRequired = true;
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromEventDtm())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "EVENT_DTM >='" + documentDbDataIn.getFromEventDtm() + "'";
			dateFilter1+= "WHERE EVENT_DTM >='" + documentDbDataIn.getFromEventDtm() + "'";
			dateFilter2+="AND DER.EVENT_DTM >='" + documentDbDataIn.getFromEventDtm() + "'";
			isSqlBlockLatestEventRequired = true;
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getToEventDtm())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			String toEventDtm = getUpdatedDate(documentDbDataIn.getToEventDtm());
			sqlWhere += "EVENT_DTM <='" + toEventDtm + "'";
			dateFilter1+= "WHERE EVENT_DTM <='" + toEventDtm + "'";
			dateFilter2+= "AND DER.EVENT_DTM <='" + toEventDtm + "'";
			isSqlBlockLatestEventRequired = true;
		}
		
		if (StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedTo())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "ASSIGNED_TO='" + documentDbDataIn.getAssignedTo()+"'";
			isSqlBlockLatestEventRequired = true;
		}else if (!StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedTo()) 
					&& StringUtility.hasTrimmedValue(documentDbDataIn.getAssignedToKey())) {
			sqlWhere = sqlWhere.length() > 0 ? sqlWhere + " AND " : "";
			sqlWhere += "ASSIGNED_TO IS NULL";
			isSqlBlockLatestEventRequired = true;
		}

		if (sqlWhere != "") {
			if (StringUtility.hasValue(documentDbDataIn.getSortByAttrNameCde())
					&& !StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_1,
						",ATTACH_ATTRCDE.ATTR_NAME_CDE ATTACHMNT_ORDER_KEY, "
								+ "ATTACH_ATTRCDE.ATTR_VALUE ATTACHMNT_ORDER_KEY_VALUE, "
								+ "ATTRCDE.ATTR_NAME_CDE DOC_ORDER_KEY," + "ATTRCDE.ATTR_VALUE DOC_ORDER_KEY_VALUE,"
								+ "COALESCE(COALESCE(ATTACH_ATTRCDE.ATTR_VALUE,ATTRCDE.ATTR_VALUE),'') AS ORDER_KEY_VALUE  ");

					sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_1, "");
					sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_2, "");
					sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_2,
							"LEFT JOIN( <<DOC_ATTR_DETAIL>> )ATTRCDE ON (DOC.DOC_ID=ATTRCDE.DOC_ID) "
									+ "LEFT JOIN( <<ATTACH_ATTR_DETAIL>> "
									+ ")ATTACH_ATTRCDE ON (DOC.DOC_ID=ATTACH_ATTRCDE.DOC_ID)" + " WHERE");

					sqlQueryForFilter += " WHERE " + sqlWhere + " ORDER BY  ORDER_KEY_VALUE " + sortOrder + " OFFSET "
							+ offset + " LIMIT " + pageSizeVal;

				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DOC_ATTR_DETAIL, sqGetDocAttrDtlSql);
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.ATTACH_ATTR_DETAIL,sqGetAttachAttrDtlSql);

				String docAttrNameCde = " (" + documentDbDataIn.getSortByAttrNameCde() + ")";
				String attachmentAttrNameCde = " (" + documentDbDataIn.getSortByAttrNameCde() + ")";
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DOC_ATTR_NAME_CDE_FILTER,
						docAttrNameCde);
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.ATTACH_ATTR_NAME_CDE_FILTER,
						attachmentAttrNameCde);	

				sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_FILTER, ("(" + sqlQueryForFilter + ") "));
				sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);
				sqlQuery += " ORDER BY ORDER_KEY_VALUE " + sortOrder + " ,DOC.DOC_ID, ATTRIB.ATTR_NAME_CDE";
			} 
			else if (StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
				sqlQueryForFilter += " WHERE " + sqlWhere + " ORDER BY DOC_ID " + sortOrder;
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_1, "");
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_1, dateFilter1);
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_2,
						",DOC_EVENT_REL DER WHERE DER.DOC_ID = DOC.DOC_ID " + dateFilter2 + " AND ");
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_2,"");
				sqlQuery = sqlQueryForFilter;
				isSqlBlockLatestEventRequired = true;
			} 
			else {
				sqlQueryForFilter += " WHERE " + sqlWhere + " ORDER BY DOC_ID " + sortOrder + " OFFSET " + offset
						+ " LIMIT " + pageSizeVal;
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_1, "");
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_1, "");
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.DATE_FILTER_2, "WHERE");
				sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_2,"");
				sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_FILTER, ("(" + sqlQueryForFilter + ") "));
				sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);

				sqlQuery += " ORDER BY DOC.DOC_ID " + sortOrder + " , ATTRIB.ATTR_NAME_CDE";
			}
		}
		
		if (!isSqlBlockLatestEventRequired) {
			sqlQuery = SqlUtil.removeSqlBlock(sqlQuery,
					WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_BEGIN, WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_END);
		}
		// Remove traces of multi-line comments left by SqlUtil.removeSqlBlock
		sqlQuery = SqlUtil.removeMultilineComments(sqlQuery);
		// Remove SQL block tags, if not removed already 
		for (String item : Arrays.asList(WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_BEGIN,
				WorkbenchConstants.SQL_BLOCK_HIGHEST_EVENT_END, WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_BEGIN,
				WorkbenchConstants.SQL_BLOCK_LATEST_EVENT_END)) {
			sqlQuery = sqlQuery.replace(item, "");
		}
		
		try {
			docAttrWrapperDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { SessionHelper.getTenantId() },
					new RowMapper<DocAttrWrapperDbData>() {
						@Override
						public DocAttrWrapperDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ResultSetMetaData rsmd = rs.getMetaData();
							List<String> columnNames = new ArrayList<String>();
							for (int i = 1; i <= rsmd.getColumnCount(); i++) {
								columnNames.add(rsmd.getColumnName(i));
							}
							
							DocAttrWrapperDbData docAttrWrapperDbData = new DocAttrWrapperDbData();

							DocumentDbData documentDbData = new DocumentDbData();
							documentDbData.setDocId(rs.getLong("doc_id"));
							documentDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							documentDbData.setDocLocation(rs.getString("doc_location"));
							documentDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							if (!StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
								documentDbData.setTaskStatusTxt(rs.getString("task_status_txt"));
								documentDbData.setLockStatusCde(rs.getInt("lock_status_cde"));
								documentDbData.setLockStatusTxt(rs.getString("lock_status_txt"));
								documentDbData.setAttachmentCount(rs.getInt("attachment_count"));
							}
							// Conditional params
							if (columnNames.contains("highest_event_type_cde")) {
								documentDbData.setHighestEventTypeCde(rs.getInt("highest_event_type_cde"));
							}
							// Conditional params
							if (columnNames.contains("latest_event_type_cde")) {
								documentDbData.setLatestEventTypeCde(rs.getInt("latest_event_type_cde"));
							}
							
							documentDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							documentDbData.setAppUserId(rs.getLong("app_user_id"));
							documentDbData.setAssignedTo(rs.getString("assigned_to"));
							documentDbData.setCaseReviewer(rs.getString("case_reviewer"));
							documentDbData.setCaseReviewerId(rs.getLong("case_reviewer_id"));
							documentDbData.setCreateDtm(rs.getString("create_dtm"));
							docAttrWrapperDbData.setDocumentDbData(documentDbData);

							if (!StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
								AttributeDbData attributeDbData = new AttributeDbData();
								if (rs.getInt("doc_attr_rel_id") > 0) {
									attributeDbData.setId(rs.getInt("doc_attr_rel_id"));
									attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
									attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
									attributeDbData.setAttrValue(rs.getString("attr_value"));
									attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
									attributeDbData.setExtractTypeTxt(rs.getString("extract_type_txt"));
									attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
											.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
													RoundingMode.HALF_EVEN)
											.floatValue());
									docAttrWrapperDbData.setAttributeDbData(attributeDbData);
								}
						}
							return docAttrWrapperDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching document list data from database", ex);
			throw new WorkbenchException("Error occured while fetching document list data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		if (sqlQuery == sqlQueryForFilter) {
			logger.info("Time taken for sqGetDocOnFilterV3Sql query (secs):" + timeElapsed);
			PERF_LOGGER.info("sqGetDocOnFilterV3Sql" + "," + timeElapsed + ",secs");
		} else {
			logger.info("Time taken for getDocumentListSql query (secs):" + timeElapsed);
			PERF_LOGGER.info("getDocumentListSql" + "," + timeElapsed + ",secs");
		}
		if (StringUtility.hasValue(documentDbDataIn.getAttachmentAttrNameCdes())
				&& !StringUtility.hasTrimmedValue(documentDbDataIn.getDocumentName())) {
			populateAttachmentAttributes(documentDbDataIn, docAttrWrapperDbDataList);
		}
		return docAttrWrapperDbDataList;
	}

	public List<DocAttrWrapperDbData> getSearchCriteriaDetails(String docIdList, DocumentDbData documentDbDataIn) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocAttrWrapperDbData> docAttrWrapperDbDataList = new ArrayList<DocAttrWrapperDbData>();
		String sqlQuery = getDocumentListV2Sql;
		String sqlQueryForFilter=sqGetDocOnSearchCriteriaSql;
		int offset = 0;
		String attachmentFilter=" ";
		String attrFilter=" ";
		String sortOrder = StringUtility.hasValue(documentDbDataIn.getSortOrder()) ? documentDbDataIn.getSortOrder()
				: "DESC";
		int pageSizeVal=pageSize;
		if (documentDbDataIn.getPageSize()>0)
		{
			pageSizeVal=documentDbDataIn.getPageSize();
		}
		
		String pageFilter=" OFFSET " + offset + " LIMIT " + pageSizeVal;
		
		if (documentDbDataIn.getPageNumber() > 1) {
			offset = pageSizeVal * (documentDbDataIn.getPageNumber() - 1);
		}
		if (StringUtility.hasValue(documentDbDataIn.getAttrNameCdes())) {
			attrFilter = "AND ATTR.ATTR_NAME_CDE IN (" + documentDbDataIn.getAttrNameCdes() + ") ";
		}
		if (docIdList==null) {
			docIdList="(0)";
		}
		sqlQueryForFilter+=docIdList;
		attachmentFilter=" AND ATT.LOGICAL_NAME ='"+documentDbDataIn.getDocumentName()+"'";
		sqlQueryForFilter = sqlQueryForFilter.replace(WorkbenchConstants.ATTACH_FILTER, attachmentFilter);
		sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_FILTER, sqlQueryForFilter);
		sqlQuery = sqlQuery.replace(WorkbenchConstants.PAGE_FILTER, pageFilter);
		sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);
		if (!StringUtility.hasTrimmedValue(documentDbDataIn.getSortByAttrNameCde())) {
			sqlQuery += "ORDER BY DOC.DOC_ID "+ sortOrder +", ATTRIB.ATTR_NAME_CDE";
		
			sqlQuery = sqlQuery.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_1,"");
			sqlQuery = sqlQuery.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_2,"");
		}
		//
		else if (StringUtility.hasTrimmedValue(documentDbDataIn.getSortByAttrNameCde())) {
		sqlQuery = sqlQuery.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_1,
				",ATTACH_ATTRCDE.ATTR_NAME_CDE ATTACHMNT_ORDER_KEY, "
						+ "ATTACH_ATTRCDE.ATTR_VALUE ATTACHMNT_ORDER_KEY_VALUE, "
						+ "ATTRCDE.ATTR_NAME_CDE DOC_ORDER_KEY," + "ATTRCDE.ATTR_VALUE DOC_ORDER_KEY_VALUE,"
						+ "COALESCE(COALESCE(ATTACH_ATTRCDE.ATTR_VALUE,ATTRCDE.ATTR_VALUE),'') AS ORDER_KEY_VALUE  ");
		sqlQuery = sqlQuery.replace(WorkbenchConstants.SORT_BY_ATTR_NAME_CDE_FILTER_2,
				"LEFT JOIN( <<DOC_ATTR_DETAIL>> )ATTRCDE ON (D.DOC_ID=ATTRCDE.DOC_ID) "
						+ "LEFT JOIN( <<ATTACH_ATTR_DETAIL>> "
						+ ")ATTACH_ATTRCDE ON (D.DOC_ID=ATTACH_ATTRCDE.DOC_ID)");
		
		sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_ATTR_DETAIL, sqGetDocAttrDtlSql);
		sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTACH_ATTR_DETAIL,sqGetAttachAttrDtlSql);
		
		String docAttrNameCde = " (" + documentDbDataIn.getSortByAttrNameCde() + ")";
		String attachmentAttrNameCde = " (" + documentDbDataIn.getSortByAttrNameCde() + ")";
		
		sqlQuery = sqlQuery.replace(WorkbenchConstants.DOC_ATTR_NAME_CDE_FILTER,
				docAttrNameCde);
		sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTACH_ATTR_NAME_CDE_FILTER,
				attachmentAttrNameCde);
		
		sqlQuery += "ORDER BY ORDER_KEY_VALUE "+ sortOrder +",DOC.DOC_ID, ATTRIB.ATTR_NAME_CDE";
		}
//		//
		
		try {
			docAttrWrapperDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] {documentDbDataIn.getQueueNameCde(), SessionHelper.getTenantId()},
					new RowMapper<DocAttrWrapperDbData>() {
						@Override
						public DocAttrWrapperDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							DocAttrWrapperDbData docAttrWrapperDbData = new DocAttrWrapperDbData();

							DocumentDbData documentDbData = new DocumentDbData();
							documentDbData.setDocId(rs.getLong("doc_id"));
							documentDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							documentDbData.setDocLocation(rs.getString("doc_location"));
							documentDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							documentDbData.setTaskStatusTxt(rs.getString("task_status_txt"));
							documentDbData.setLockStatusCde(rs.getInt("lock_status_cde"));
							documentDbData.setLockStatusTxt(rs.getString("lock_status_txt"));
							documentDbData.setAttachmentCount(rs.getInt("attachment_count"));
//							//TODO:if needed-add missing columns
//							documentDbData.setHighestEventTypeCde(rs.getInt("highest_event_type_cde"));
//							documentDbData.setLatestEventTypeCde(rs.getInt("latest_event_type_cde"));
							
							documentDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							documentDbData.setAppUserId(rs.getLong("app_user_id"));
							documentDbData.setAssignedTo(rs.getString("assigned_to"));
							documentDbData.setCreateDtm(rs.getString("create_dtm"));
							documentDbData.setCaseReviewer(rs.getString("case_reviewer"));
							documentDbData.setCaseReviewerId(rs.getLong("case_reviewer_id"));
							docAttrWrapperDbData.setDocumentDbData(documentDbData);

							AttributeDbData attributeDbData = new AttributeDbData();
							if (rs.getInt("doc_attr_rel_id") > 0) {
								attributeDbData.setId(rs.getInt("doc_attr_rel_id"));
								attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
								attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
								attributeDbData.setAttrValue(rs.getString("attr_value"));
								attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
								attributeDbData.setExtractTypeTxt(rs.getString("extract_type_txt"));
								attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
										.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
												RoundingMode.HALF_EVEN)
										.floatValue());
								docAttrWrapperDbData.setAttributeDbData(attributeDbData);
							}
							return docAttrWrapperDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching document list data from database", ex);
			throw new WorkbenchException("Error occured while fetching document list data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocumentListV2Sql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocumentListV2Sql" + "," + timeElapsed + ",secs");
		
		if (StringUtility.hasValue(documentDbDataIn.getAttachmentAttrNameCdes())) {
			populateAttachmentAttributes(documentDbDataIn, docAttrWrapperDbDataList);
		}
		
		return docAttrWrapperDbDataList;
	}
	
	
	public List<DocDetailDbData> getDocumentListByAttribute(DocumentDbData documentDbDataIn, String searchCriteria, String queueNameCdes) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocDetailDbData> documentDbData = new ArrayList<DocDetailDbData>();
		String sqlQuery = getDocByAttrSql;
		String sqlWhere = "";
		String attrFilterBySC = ""; 
		String sortOrder = StringUtility.hasValue(documentDbDataIn.getSortOrder()) ? documentDbDataIn.getSortOrder()
				: "DESC";
		int offset = 0;
		int pageSizeVal=documentDbDataIn.getPageSize();
		if (documentDbDataIn.getPageNumber() > 1) {
			offset = pageSizeVal * (documentDbDataIn.getPageNumber() - 1);
		}
		if (documentDbDataIn.getTaskStatusCde() > EnumTaskStatus.UNDEFINED.getValue()
				&& (documentDbDataIn.getTaskStatusOperator() != null)) {
			sqlWhere += sqlWhere.length() > 0 ? " AND " : "";
			sqlWhere += "doc.TASK_STATUS_CDE" + documentDbDataIn.getTaskStatusOperator()
					+ documentDbDataIn.getTaskStatusCde();
		}
		
		if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromCaseCreateDtm()) && StringUtility.hasTrimmedValue(documentDbDataIn.getToCaseCreateDtm())) {
			String toEventDtm = getUpdatedDate(documentDbDataIn.getToCaseCreateDtm());
			sqlWhere += sqlWhere.length() > 0 ? " AND " : "";
			sqlWhere += "doc.CREATE_DTM BETWEEN '" + documentDbDataIn.getFromCaseCreateDtm() + "' AND '" + toEventDtm + "'";
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getFromCaseCreateDtm())) {
			sqlWhere += sqlWhere.length() > 0 ? " AND " : "";
			sqlWhere += "doc.CREATE_DTM >='" + documentDbDataIn.getFromCaseCreateDtm() + "'";
		} else if (StringUtility.hasTrimmedValue(documentDbDataIn.getToCaseCreateDtm())) {
			sqlWhere += sqlWhere.length() > 0 ? " AND " : "";
			String toEventDtm = getUpdatedDate(documentDbDataIn.getToCaseCreateDtm());
			sqlWhere += "doc.CREATE_DTM <='" + toEventDtm + "'";
		}
		
		if (StringUtility.hasTrimmedValue(queueNameCdes)) {
			sqlWhere += sqlWhere.length() > 0 ? " AND " : "";
			String[] numbers = queueNameCdes.split(",");
			String result = " doc.queue_name_cde in (";
			for (String n : numbers) {
			    result += n + ",";
			}
			result = result.substring(0, result.length()-1);
			result += ")";
			sqlWhere += result;
		}
		
		String[] arrOfMultiSearchCriteria = StringUtility.splitWithEscape(searchCriteria, ";", -2);
		for (String searchCriteriaVal : arrOfMultiSearchCriteria) {
			if (!StringUtility.hasTrimmedValue(searchCriteriaVal)) {
				continue;
			}
			String[] arrOfSearchCriteria = StringUtility.splitWithEscape(searchCriteriaVal, ":", 2);
			String searchKey = (arrOfSearchCriteria.length>0)?arrOfSearchCriteria[0].trim().toLowerCase():"";
			String searchVal = (arrOfSearchCriteria.length>1)?arrOfSearchCriteria[1].trim().toLowerCase():"";
			searchVal = searchVal.replace("*", "%");
			String temp = "(lower(attrn.attr_name_txt) LIKE '"+searchKey+"' and lower(attr.attr_value) LIKE '"+searchVal+"')";
			Matcher m = Pattern.compile("[0-9]+").matcher(searchKey);
			if(m.matches()) {
				temp = "(attrn.attr_name_cde = "+searchKey+" and lower(attr.attr_value) LIKE '"+searchVal+"')";
			}
			attrFilterBySC += (!StringUtility.hasTrimmedValue(attrFilterBySC)) ? temp: "OR" + temp;
		}
		sqlQuery=sqlQuery.replace(WorkbenchConstants.ATTR_FILTER_BY_SEARCH_CRITERIA, attrFilterBySC);
		// Adding WHERE conditions 
		sqlQuery += sqlWhere.length() > 0 ? " AND "+sqlWhere : "";
		// Logic to get flat doc list with all attributes matched based on page size
		pageSizeVal=documentDbDataIn.getPageSize()*(arrOfMultiSearchCriteria.length*2);
		sqlQuery+=" order by doc.doc_id "+sortOrder+" offset "+offset+" limit "+pageSizeVal;
		try {
			documentDbData = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { SessionHelper.getTenantId(), SessionHelper.getTenantId()},
					new RowMapper<DocDetailDbData>() {
						@Override
						public DocDetailDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ResultSetMetaData rsmd = rs.getMetaData();
							List<String> columnNames = new ArrayList<String>();
							for (int i = 1; i <= rsmd.getColumnCount(); i++) {
								columnNames.add(rsmd.getColumnName(i));
							}
							DocDetailDbData documentDbData = new DocDetailDbData();
							
							documentDbData.setDocId(rs.getLong("doc_id"));
							documentDbData.setDocTypeCde(rs.getInt("doc_type_cde"));
							documentDbData.setDocLocation(rs.getString("doc_location"));
							documentDbData.setTaskStatusCde(rs.getInt("task_status_cde"));
							documentDbData.setTaskStatusTxt(rs.getString("task_status_txt"));
							documentDbData.setQueueNameCde(rs.getInt("queue_name_cde"));
							documentDbData.setCreateDtm(rs.getString("create_dtm"));
							
							AttributeData attributeData = new DocDetailDbData().new AttributeData();
							attributeData.setAttributeId(rs.getLong("attribute_id"));
							attributeData.setAttrNameCde(rs.getInt("attr_name_cde"));
							attributeData.setAttrNameTxt(rs.getString("attr_name_txt"));
							attributeData.setAttrValue(rs.getString("attr_value"));
							attributeData.setConfidencePct(rs.getFloat("confidence_pct"));
							attributeData.setExtractTypeCde(rs.getInt("extract_type_cde"));
							
							List<AttributeData> attributeDataList = new ArrayList<DocDetailDbData.AttributeData>();
							attributeDataList.add(attributeData);
							documentDbData.setAttributeData(attributeDataList);
							return documentDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching document list by attribute data from database", ex);
			throw new WorkbenchException("Error occured while fetching document list  by attribute data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocByAttrSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocByAttrSql" + "," + timeElapsed + ",secs");
		return documentDbData;
	}
	
	public List<DocAppUserDbData> getUserDocDetails(long docId, long docRoleTypeCde) throws WorkbenchException {

		long startTime = System.nanoTime();
		if (docRoleTypeCde<1) {
			docRoleTypeCde=1;
		}

		List<DocAppUserDbData> docUserList = new ArrayList<>();
		try {
			docUserList = jdbcTemplate.query(StringUtility.sanitizeSql(getUserDocDetailsSql),
					new Object[] { docId, SessionHelper.getTenantId(), docRoleTypeCde }, new RowMapper<DocAppUserDbData>() {
						@Override
						public DocAppUserDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							DocAppUserDbData docAppData = new DocAppUserDbData();
							docAppData.setAppUserId(rs.getInt("app_user_id"));
							docAppData.setDocAppUserRelId(rs.getInt("doc_app_user_rel_id"));
							docAppData.setDocId(rs.getInt("doc_id"));
							docAppData.setDocRoleTypeCde(rs.getLong("doc_id"));
							return docAppData;
						}
					});

		} catch (Exception ex) {
			logger.error("Error occured while fetching details for user from database", ex);
			throw new WorkbenchException("Error occured while fetching details for user from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getUserDocDetailsSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getUserDocDetailsSql" + "," + timeElapsed + ",secs");
		return docUserList;
	}

	public long updateDocAppUser(DocAppUserDbData docUser) throws WorkbenchException {
		long startTime = System.nanoTime();
		long docAppUserRelId = -1;
		try {
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection
							.prepareStatement(StringUtility.sanitizeSql(updateDocAppUserSql));
					ps.setString(1, SessionHelper.getLoginUsername());
					ps.setLong(2, docUser.getDocAppUserRelId());
					ps.setString(3, SessionHelper.getTenantId());
					return ps;
				}
			};

			int noOfRowUpdated = jdbcTemplate.update(psc);
			if (noOfRowUpdated > 0) {
				docAppUserRelId = docUser.getDocAppUserRelId();
			} else {
				logger.info("No rows updated");
			}

		} catch (Exception ex) {
			logger.error("Error occured while update DocAppUser in database", ex);
			throw new WorkbenchException("Error occured while updating DocAppUser in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateDocAppUserSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateDocAppUserSql" + "," + timeElapsed + ",secs");
		return docAppUserRelId;
	}

	public long insertUserDocRel(long appUserId, long docId, long docRoleTypeCde) throws WorkbenchException {
		long startTime = System.nanoTime();
		long docAppUserRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addUserDocRelationshipSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setLong(1, appUserId);
					ps.setLong(2, docId);
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					ps.setLong(5, docRoleTypeCde);
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			docAppUserRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while adding user document relationship", ex);
			throw new WorkbenchException("Error occured while adding user document relationship", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addUserDocRelationshipSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addUserDocRelationshipSql" + "," + timeElapsed + ",secs");

		return docAppUserRelId;

	}
	
	@Override
	public List<DocUserDbData> getDocUserRel(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<DocUserDbData> documentDbDataList = new ArrayList<DocUserDbData>();
		try {
			documentDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getDocUserDetailsSql),
					new Object[] { SessionHelper.getTenantId(), docId }, new RowMapper<DocUserDbData>() {
						@Override
						public DocUserDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							DocUserDbData documentDbData = new DocUserDbData();
							documentDbData.setDocId(rs.getLong("DOC_ID"));
							documentDbData.setDocRoleTypeCde(rs.getLong("DOC_ROLE_TYPE_CDE"));
							documentDbData.setDocRoleTypeTxt(rs.getString("DOC_ROLE_TYPE_TXT"));
							documentDbData.setRoleTypeCde(rs.getLong("ROLE_TYPE_CDE"));
							documentDbData.setRoleTypeTxt(rs.getString("ROLE_TYPE_TXT"));
							documentDbData.setUserEmail(rs.getString("USER_EMAIL"));
							documentDbData.setUserFullName(rs.getString("USER_FULL_NAME"));
							documentDbData.setUserId(rs.getLong("APP_USER_ID"));
							documentDbData.setUserLoginId(rs.getString("USER_LOGIN_ID"));
							documentDbData.setUserTypeCde(rs.getLong("USER_TYPE_CDE"));
							documentDbData.setUserTypeTxt(rs.getString("USER_TYPE_TXT"));
							return documentDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching user details for docId from database", ex);
			throw new WorkbenchException("Error occured while fetching user details for docId from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocUserRel query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocUserRel" + "," + timeElapsed + ",secs");

		return documentDbDataList;
	}

	public List<EntityDbData> updateDocActionStatus(DocumentDbData documentDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<EntityDbData> entityList = new ArrayList<>();
		try {
			entityList = jdbcTemplate.query(StringUtility.sanitizeSql(updateDocActionStatusSql),
					new Object[] { documentDbData.getTaskStatusCde(), SessionHelper.getLoginUsername(),
							documentDbData.getDocId(), documentDbData.getTaskStatusCde(), SessionHelper.getTenantId() },
					new RowMapper<EntityDbData>() {
						@Override
						public EntityDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							EntityDbData entityData = new EntityDbData();
							entityData.setDocId(rs.getInt("doc_id"));
							entityData.setTaskStatusCde(rs.getInt("task_status_cde"));
							entityData.setTaskStatusTxt(rs.getString("task_status_txt"));
							return entityData;
						}
					});
			if (!ListUtility.hasValue(entityList) || !(entityList.get(0).getDocId() > 0)) {
				entityList = new ArrayList<>();
			}
		} catch (Exception ex) {
			logger.error("Error occured while update DocActionStatus in database", ex);
			throw new WorkbenchException("Error occured while updating Document task status in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for updateDocActionStatusSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("updateDocActionStatusSql" + "," + timeElapsed + ",secs");
		return entityList;
	}

	public int insertDocEventType(DocumentDbData documentDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();

			object = new Object[] { documentDbData.getEventTypeCde(), documentDbData.getDocId(),
					SessionHelper.getLoginUsername(), SessionHelper.getTenantId() };
			objectList.add(object);

			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(insertDocEventTypeSql), objectList);
			sum = IntStream.of(rowsImpacted).sum();
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while update DocEventType in database", ex);
			throw new WorkbenchException("Error occured while updating Document task status in database", ex);
		} finally {
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for insertDocEventTypeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("insertDocEventTypeSql" + "," + timeElapsed + ",secs");
		return sum;
	}
}
