/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attribute;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeNameValueDbData;
import com.infosys.ainauto.docwb.service.model.db.annotation.ExportIOBDbData;

/**
 * 
 * ActionDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */

@Component
@Profile("default")
public class AttributeDataAccess implements IAttributeDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(AttributeDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addNewAttribute}")
	private String addNewAttributeSql;

	@Value("${addExistingAttribute}")
	private String addExistingAttributeSql;

	@Value("${addDocAttributeRel}")
	private String addDocAttributeRelSql;

	@Value("${addAttributeSource}")
	private String addAttributeSourceSql;

	@Value("${addAttachmentAttributeRel}")
	private String addAttachmentAttrRelSql;

	@Value("${getAttrNameCode}")
	private String getAttrNameCodeSql;

	@Value("${getDocAttributes}")
	private String getDocAttributesSql;

	@Value("${getAttachmentAttributes}")
	private String getAttachmentAttributesSql;
	
	@Value("${getAttachmentAttributesWithOrigVal}")
	private String getAttachmentAttributesWithOrigValSql;

	@Value("${deleteAttribute}")
	private String deleteAttributeSql;

	@Value("${deleteAttributeSource}")
	private String deleteAttributeSourceSql;

	@Value("${getAttributeNameValues}")
	private String getAttributeNameValuesSql;

	@Value("${deleteDocAttributeRel}")
	private String deleteDocAttributeRelSql;

	@Value("${deleteAttachAttributeRel}")
	private String deleteAttachAttributeRelSql;

	@Value("${getAnnotation}")
	private String getAnnotationSql;

	@Value("${getAnnotationIOBAttrFilter}")
	private String getAnnotationIOBAttrFilterSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long addNewAttribute(AttributeDbData attributeDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attrId = -1;
		long docAttrRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addNewAttributeSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, attributeDbData.getAttrNameCde());
					ps.setString(2, attributeDbData.getAttrValue());
					ps.setLong(3, attributeDbData.getExtractTypeCde());
					ps.setFloat(4, attributeDbData.getConfidencePct());
					ps.setString(5,
							attributeDbData.getCreateByUserLoginId() != null ? attributeDbData.getCreateByUserLoginId()
									: SessionHelper.getLoginUsername());
					ps.setString(6, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			attrId = keyHolder.getKey().longValue();
			if (attrId > 0) {
				attributeDbData.setAttributeId(attrId);
				docAttrRelId = addAttributeRel(attributeDbData);
			}
		} catch (Exception ex) {
			logger.error("Error occured while add attributes in database", ex);
			throw new WorkbenchException("Error occured while add attributes in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addNewAttributeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addNewAttributeSql" + "," + timeElapsed + ",secs");
		return docAttrRelId;
	}

	public long addExistingAttribute(AttributeDbData attributeDbData, String prevCreateBy) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attrId = -1;
		long docAttrRelId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addExistingAttributeSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, attributeDbData.getAttrNameCde());
					ps.setString(2, attributeDbData.getAttrValue());
					ps.setLong(3, attributeDbData.getExtractTypeCde());
					ps.setFloat(4, attributeDbData.getConfidencePct());
					ps.setString(5, prevCreateBy); // create_by
					ps.setString(6, SessionHelper.getTenantId());
					ps.setString(7,
							attributeDbData.getLastModByUserLoginId() != null
									? attributeDbData.getLastModByUserLoginId()
									: SessionHelper.getLoginUsername()); // last_mod_by
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			attrId = keyHolder.getKey().longValue();
			if (attrId > 0) {
				attributeDbData.setAttributeId(attrId);
				docAttrRelId = addAttributeRel(attributeDbData);
			}
		} catch (Exception ex) {
			logger.error("Error occured while add attributes in database", ex);
			throw new WorkbenchException("Error occured while add attributes in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addAttributeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addExistingAttributeSql" + "," + timeElapsed + ",secs");
		return docAttrRelId;
	}

	private long addAttributeRel(AttributeDbData attributeDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attrRelId = -1;
		String query = addDocAttributeRelSql;
		try {
			if (attributeDbData.getAttachmentId() > 0) {
				query = addAttachmentAttrRelSql;
			}
			String tempSQL[] = StringUtility.sanitizeSql(query).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, attributeDbData.getDocId());
					if (attributeDbData.getAttachmentId() > 0) {
						ps.setLong(1, attributeDbData.getAttachmentId());
					}

					ps.setLong(2, attributeDbData.getAttributeId());
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			attrRelId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while add attributes in database", ex);
			throw new WorkbenchException("Error occured while add attributes in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addDocAttributeRelSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addDocAttributeRelSql" + "," + timeElapsed + ",secs");
		return attrRelId;
	}

	@Override
	public long addAttributeSource(long docId, String record) throws WorkbenchException {
		long startTime = System.nanoTime();
		long attrSrcId = -1;
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addAttributeSourceSql).toLowerCase()
					.split(WorkbenchConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });
					ps.setLong(1, docId);
					ps.setString(2, record);
					ps.setString(3, SessionHelper.getLoginUsername());
					ps.setString(4, SessionHelper.getTenantId());
					return ps;
				}
			};

			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			attrSrcId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while add attribute source in database", ex);
			throw new WorkbenchException(ex.getMessage());
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for addAttributeSourceSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("addAttributeSourceSql" + "," + timeElapsed + ",secs");
		return attrSrcId;
	}

	@Override
	public long deleteAttributeSource(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		long sqlResponseId = -1;
		try {
			Object object[] = new Object[] { SessionHelper.getLoginUsername(), docId, SessionHelper.getTenantId() };
			Map<String, Object> sqlResponse = jdbcTemplate
					.queryForMap(StringUtility.sanitizeSql(deleteAttributeSourceSql), object);
			long resOutId = Long.parseLong(String.valueOf(sqlResponse.get("attr_src_id")));
			if (resOutId > 0) {
				logger.info("The row that was impacted=" + resOutId);
				sqlResponseId = resOutId;
			} else {
				logger.info("No row was impacted. Wrong Input");
			}
		} catch (Exception ex) {
			logger.error("Error occured while executing deleteAttributeSourceSql", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteAttributeSourceSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteAttributeSourceSql" + "," + timeElapsed + ",secs");
		return sqlResponseId;
	}

	private AttributeDbData updateEndDateRel(AttributeDbData attributeDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		AttributeDbData resultAttributeDbData = new AttributeDbData();
		try {
			long attrRelId = 0;
			long attributeId = 0;
			Object object[];
			String lastModBy = attributeDbData.getLastModByUserLoginId() != null
					? attributeDbData.getLastModByUserLoginId()
					: SessionHelper.getLoginUsername();
			object = new Object[] { lastModBy, attributeDbData.getId(), attributeDbData.getDocId(),
					SessionHelper.getTenantId() };
			String query = deleteDocAttributeRelSql;
			if (attributeDbData.getAttachmentId() > 0) {
				object = new Object[] { lastModBy, attributeDbData.getAttachmentId(), attributeDbData.getDocId(),
						attributeDbData.getId(), SessionHelper.getTenantId() };
				query = deleteAttachAttributeRelSql;
			}

			Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(query), object);

			attrRelId = sqlResponse.values().toArray(new Long[2])[0];
			attributeId = sqlResponse.values().toArray(new Long[2])[1];

			if (attrRelId > 0 && attributeId > 0) {
				attributeDbData.setAttributeId(attributeId);
				// Call for soft deleting attribute row
				resultAttributeDbData = updateEndDateAttribute(attributeDbData);
				// Populate relId appropriately
				if (attributeDbData.getAttachmentId() > 0) {
					resultAttributeDbData.setAttachmentAttrRelId(attrRelId);
				} else {
					resultAttributeDbData.setDocAttrRelId(attrRelId);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occured while updating attribute rel in database", ex);
			throw new WorkbenchException("Error occured while updating attribute rel in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteAttributeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteAttributeRelSql" + "," + timeElapsed + ",secs");
		return resultAttributeDbData;
	}

	private AttributeDbData updateEndDateAttribute(AttributeDbData attributeDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		AttributeDbData resultAttributeDbData = new AttributeDbData();
		try {
			Object object[] = new Object[] {
					attributeDbData.getLastModByUserLoginId() != null ? attributeDbData.getLastModByUserLoginId()
							: SessionHelper.getLoginUsername(),
					attributeDbData.getAttributeId(), SessionHelper.getTenantId() };
			Map<String, Object> sqlResponse = jdbcTemplate.queryForMap(StringUtility.sanitizeSql(deleteAttributeSql),
					object);
			long attributeId = Long.parseLong(String.valueOf(sqlResponse.get("attribute_id")));
			String createBy = String.valueOf(sqlResponse.get("create_by"));

			if (attributeId > 0) {
				resultAttributeDbData.setAttributeId(attributeId);
				resultAttributeDbData.setCreateByUserLoginId(createBy);
			} else {
				logger.info("No rows updated");
			}
		} catch (Exception ex) {
			logger.error("Error occured while updating attributes in database", ex);
			throw new WorkbenchException("Error occured while updating attributes in database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for deleteAttributeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("deleteAttributeSql" + "," + timeElapsed + ",secs");
		return resultAttributeDbData;
	}

	public AttributeDbData deleteAttribute(AttributeDbData attributeDbData) throws WorkbenchException {
		return updateEndDateRel(attributeDbData);
	}

	@Override
	public List<AttributeDbData> getAttributeText() throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttributeDbData> attributeDbDataList = new ArrayList<AttributeDbData>();

		try {
			attributeDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getAttrNameCodeSql),
					new Object[] { SessionHelper.getTenantId() }, new RowMapper<AttributeDbData>() {
						@Override
						public AttributeDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							AttributeDbData attributeDbData = new AttributeDbData();
							attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
							return attributeDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attribute name code from database", ex);
			throw new WorkbenchException("Error occured while fetching attribute name code from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttrNameCodeSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttrNameCodeSql" + "," + timeElapsed + ",secs");
		return attributeDbDataList;
	}

	@Override
	public List<AttributeDbData> getDocumentAttributes(long docId) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttributeDbData> attributeDbDataList = new ArrayList<AttributeDbData>();

		try {
			Object object[] = new Object[] { docId, SessionHelper.getTenantId() };
			attributeDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getDocAttributesSql), object,
					new RowMapper<AttributeDbData>() {
						@Override
						public AttributeDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							AttributeDbData attributeDbData = new AttributeDbData();
							attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							attributeDbData.setDocId(rs.getLong("doc_id"));
							attributeDbData.setId(rs.getLong("doc_attr_rel_id"));
							attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
							attributeDbData.setAttrValue(rs.getString("attr_value"));
							attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
									.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
											RoundingMode.HALF_EVEN)
									.floatValue());
							attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
							attributeDbData.setCreateByUserLoginId(rs.getString("create_by"));
							attributeDbData.setCreateDtm(rs.getString("create_dtm"));
							attributeDbData.setLastModByUserLoginId(rs.getString("last_mod_by"));
							attributeDbData.setLastModDtm(rs.getString("last_mod_dtm"));
							return attributeDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching document attributes from database", ex);
			throw new WorkbenchException("Error occured while fetching document attributes from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getDocAttributesSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getDocAttributesSql" + "," + timeElapsed + ",secs");
		return attributeDbDataList;
	}

	@Override
	public List<AttributeDbData> getAttachmentAttributes(long docId, String attachmentIds) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttributeDbData> attributeDbDataList = new ArrayList<AttributeDbData>();
		try {
			String query = getAttachmentAttributesSql;
			Object object[] = new Object[] { docId, SessionHelper.getTenantId() };
			if (StringUtility.hasValue(attachmentIds)) {
				query = "SELECT * FROM (" + getAttachmentAttributesSql + ") DATA WHERE ATTACHMENT_ID IN ("
						+ attachmentIds + ")";
			}
			attributeDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(query), object,
					new RowMapper<AttributeDbData>() {
						@Override
						public AttributeDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							AttributeDbData attributeDbData = new AttributeDbData();
							attributeDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							attributeDbData.setDocId(rs.getLong("doc_id"));
							attributeDbData.setAttachmentId(rs.getLong("attachment_id"));
							attributeDbData.setId(rs.getLong("attachment_attr_rel_id"));
							attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
							attributeDbData.setAttrValue(rs.getString("attr_value"));
							attributeDbData.setConfidencePct(new BigDecimal(rs.getFloat("confidence_pct"))
									.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
											RoundingMode.HALF_EVEN)
									.floatValue());
							attributeDbData.setExtractTypeCde(rs.getInt("extract_type_cde"));
							attributeDbData.setCreateByUserLoginId(rs.getString("create_by"));
							attributeDbData.setCreateDtm(rs.getString("create_dtm"));
							attributeDbData.setLastModByUserLoginId(rs.getString("last_mod_by"));
							attributeDbData.setLastModDtm(rs.getString("last_mod_dtm"));
							return attributeDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attachment attributes from database", ex);
			throw new WorkbenchException("Error occured while fetching attachment attributes from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttachmentAttributesSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttachmentAttributesSql" + "," + timeElapsed + ",secs");
		return attributeDbDataList;
	}
	
	
	@Override
	public List<AttributeDbData> getAttachmentAttributesOrigValue(long docId,boolean origValue) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttributeDbData> attributeDbDataList = new ArrayList<AttributeDbData>();
		try {

			String query=getAttachmentAttributesWithOrigValSql;
			Object object[] = new Object[] { docId, SessionHelper.getTenantId(),docId, SessionHelper.getTenantId() };
				//For Original/Extracted Value 
				
				attributeDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(query), object,
						new RowMapper<AttributeDbData>() {
							@Override
							public AttributeDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
								AttributeDbData attributeDbData = new AttributeDbData();
								attributeDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
								attributeDbData.setAttrValueOrig(rs.getString("attr_value"));
							return attributeDbData;
							}
						});			
		} catch (Exception ex) {
			logger.error("Error occured while fetching attachment attributes from database", ex);
			throw new WorkbenchException("Error occured while fetching attachment attributes from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttachmentAttributesWithOrigValSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttachmentAttributesWithOrigValSql" + "," + timeElapsed + ",secs");
		return attributeDbDataList;
	}

	
	public List<AttributeNameValueDbData> getAttributeNameValues(String attrNameCdes) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<AttributeNameValueDbData> attributeNameValueDbDataList = new ArrayList<AttributeNameValueDbData>();
		String sqlQuery = getAttributeNameValuesSql;
		String attrFilter = "";

		if (attrNameCdes != null) {
			if (!(attrNameCdes.isEmpty())) {
				attrFilter = "AND AN.ATTR_NAME_CDE IN (" + attrNameCdes + ") ";
			}
		}

		sqlQuery = sqlQuery.replace(WorkbenchConstants.ATTR_FILTER, attrFilter);

		try {
			attributeNameValueDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { SessionHelper.getTenantId() }, new RowMapper<AttributeNameValueDbData>() {
						@Override
						public AttributeNameValueDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							AttributeNameValueDbData attributeNameValueDbData = new AttributeNameValueDbData();
							attributeNameValueDbData.setAttrNameCde(rs.getInt("attr_name_cde"));
							attributeNameValueDbData.setAttrNameTxt(rs.getString("attr_name_txt"));
							attributeNameValueDbData.setSequenceNum(rs.getInt("sequence_num"));
							attributeNameValueDbData.setTxt(rs.getString("attr_name_value"));
							return attributeNameValueDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching attribute name values from database", ex);
			throw new WorkbenchException("Error occured while fetching attribute name values from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAttributeNameValuesSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAttributeNameValuesSql" + "," + timeElapsed + ",secs");
		return attributeNameValueDbDataList;
	}

	@Override
	public List<ExportIOBDbData> getAnnotationIob(AttributeDbData attributeDbData, Date startDtm, Date endDtm)
			throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ExportIOBDbData> exportIOBDbDataList = null;
		String sqlQuery = getAnnotationSql;
		try {
			if (attributeDbData != null) {
				sqlQuery += " WHERE ATTR_ANN.doc_id IN ( " + getAnnotationIOBAttrFilterSql + " )";
			}
			exportIOBDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new PreparedStatementSetter() {
						public void setValues(PreparedStatement preparedStatement) throws SQLException {
							preparedStatement.setString(1, SessionHelper.getTenantId());
							preparedStatement.setTimestamp(2, new Timestamp(startDtm.getTime()));
							preparedStatement.setTimestamp(3, new Timestamp(endDtm.getTime()));
							preparedStatement.setString(4, SessionHelper.getTenantId());
							if (attributeDbData != null) {
								preparedStatement.setInt(5, attributeDbData.getAttrNameCde());
								preparedStatement.setString(6, attributeDbData.getAttrValue());
							}
						}
					}, new RowMapper<ExportIOBDbData>() {
						@Override
						public ExportIOBDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ExportIOBDbData exportIOBDbData = new ExportIOBDbData();
							exportIOBDbData.setAttrValue(rs.getString("attr_value"));
							exportIOBDbData.setDocId(rs.getLong("doc_id"));
							exportIOBDbData.setLogicalName(rs.getString("logical_name"));
							exportIOBDbData.setPhysicalName(rs.getString("physical_name"));
							return exportIOBDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching annotation data for getAnnotationIob from database", ex);
			throw new WorkbenchException(
					"Error occured while fetching annotation data for getAnnotationIob from database", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getAnnotationSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getAnnotationSql" + "," + timeElapsed + ",secs");
		return exportIOBDbDataList;
	}
}
