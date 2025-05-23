/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.SqlQueryManager;
import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.model.db.KeyValuePairData;
import com.infosys.ainauto.docwb.engine.core.model.db.TransactionDbData;

@Component
public class TransactionDataAccess implements ITransactionDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(TransactionDataAccess.class);
	private static final String addTransactionSql = SqlQueryManager.getInstance().getSql("addTransaction");
	private static final String updateTransactionStatusSql = SqlQueryManager.getInstance()
			.getSql("updateTransactionStatus");
	private static final String updateExternalTransactionStatusSql = SqlQueryManager.getInstance()
			.getSql("updateExtTransactionStatus");
	private static final String getKeyPairSql = SqlQueryManager.getInstance().getSql("getKeyPair");
	private static final String getTransactionByStatusSql = SqlQueryManager.getInstance()
			.getSql("getTransactionByStatus");
	private static final String PROP_NAME_DB_USERNAME = "spring.datasource.username";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Environment environment;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long addTransaction(TransactionDbData dbData) throws DocwbEngineException {
		long startTime = System.nanoTime();
		long transactionId = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addTransactionSql).toLowerCase()
					.split(DocwbEngineCoreConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, dbData.getTransactionExtId());
					ps.setString(2, dbData.getKeyName());
					ps.setString(3, dbData.getKeyValue());
					ps.setLong(4, dbData.getStatusTypeCde());
					ps.setString(5, userName);
					return ps;
				}
			};
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			transactionId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while inserting transaction row in database", ex);
			throw new DocwbEngineException("Error occured while inserting transaction in database", ex);
		} finally {
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return transactionId;
	}

	@Override
	public List<KeyValuePairData> getKeyValuePairList(String transactionIdExt) throws DocwbEngineException {
		long startTime = System.nanoTime();
		String sqlQuery = getKeyPairSql;
		List<KeyValuePairData> keyValuePairDataList = new ArrayList<KeyValuePairData>();
		try {
			keyValuePairDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery),
					new Object[] { transactionIdExt }, new RowMapper<KeyValuePairData>() {
						@Override
						public KeyValuePairData mapRow(ResultSet rs, int rowNum) throws SQLException {
							KeyValuePairData keyValuePairData = new KeyValuePairData();

							keyValuePairData.setKeyName(rs.getString("key_name"));
							keyValuePairData.setKeyValue(rs.getString("key_value"));

							return keyValuePairData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching data from database", ex);
			throw new DocwbEngineException("Error occured while fetching data from database", ex);
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return keyValuePairDataList;
	}

	@Override
	public int updateTransaction(TransactionDbData transactionData) throws DocwbEngineException {
		long startTime = System.nanoTime();
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();

			object = new Object[] { transactionData.getStatusTypeCde(), transactionData.getTransactionExtMessage(),
					userName, transactionData.getTransactionExtId() };
			objectList.add(object);

			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(updateTransactionStatusSql),
					objectList);
			sum = IntStream.of(rowsImpacted).sum();
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while updating transaction in database", ex);
			throw new DocwbEngineException("Error occured while updating transaction in database", ex);
		} finally {
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return sum;
	}

	@Override
	public int updateExternalTransaction(TransactionDbData transactionData) throws DocwbEngineException {
		long startTime = System.nanoTime();
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();

			object = new Object[] { transactionData.getTransactionExtStatusTxt(), userName,
					transactionData.getTransactionExtId() };
			objectList.add(object);

			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(updateExternalTransactionStatusSql),
					objectList);
			sum = IntStream.of(rowsImpacted).sum();
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while updating transaction in database", ex);
			throw new DocwbEngineException("Error occured while updating transaction in database", ex);
		} finally {
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return sum;
	}

	@Override
	public List<TransactionDbData> getTransactionByStatus(int statusTypeCde) throws DocwbEngineException {
		long startTime = System.nanoTime();
		List<TransactionDbData> transactionDbDataList = new ArrayList<TransactionDbData>();

		try {
			transactionDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(getTransactionByStatusSql),
					new Object[] { statusTypeCde }, new RowMapper<TransactionDbData>() {
						@Override
						public TransactionDbData mapRow(ResultSet rs, int rowNum) throws SQLException {

							TransactionDbData transactionDbData = new TransactionDbData();
							transactionDbData.setTransactionExtId(rs.getString("transaction_id"));
							transactionDbData.setKeyName(rs.getString("key_name"));
							transactionDbData.setKeyValue(rs.getString("key_value"));
							transactionDbData.setStatusTypeCde(rs.getInt("status_type_cde"));
							transactionDbData.setTransactionExtId(rs.getString("transaction_ext_id"));
							return transactionDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching transactions from database", ex);
			throw new DocwbEngineException("Error occured while fetching transactions from database", ex);
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return transactionDbDataList;
	}

}
