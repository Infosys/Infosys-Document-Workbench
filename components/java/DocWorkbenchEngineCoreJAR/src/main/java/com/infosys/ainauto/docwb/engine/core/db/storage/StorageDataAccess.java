/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.storage;

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

@Component
public class StorageDataAccess implements IStorageDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(StorageDataAccess.class);
	private static final String getValueSql = SqlQueryManager.getInstance().getSql("getValue");
	private static final String updateKeyValueSql = SqlQueryManager.getInstance().getSql("updateKeyValue");
	private static final String addKeyValueSql = SqlQueryManager.getInstance().getSql("addKeyValue");

	private static final String PROP_NAME_DB_USERNAME = "spring.datasource.username";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Environment environment;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String getValue(String key) throws DocwbEngineException {
		long startTime = System.nanoTime();
		String value = null;
		try {
			List<String> count = jdbcTemplate.query(StringUtility.sanitizeSql(getValueSql), new Object[] { key },
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getString("value");
						}
					});
			if (count != null && count.size() > 0)
				value = count.get(0);
		} catch (Exception ex) {
			logger.error("Error occured while fetching key value from database", ex);
			throw new DocwbEngineException("Error occured while fetching key value from database", ex);
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return value;

	}

	@Override
	public int updateKeyValue(String key, String value) throws DocwbEngineException {
		long startTime = System.nanoTime();
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		int sum = 0;
		try {
			Object object[];
			List<Object[]> objectList = new ArrayList<Object[]>();

			object = new Object[] { value, userName, key };
			objectList.add(object);

			int[] rowsImpacted = jdbcTemplate.batchUpdate(StringUtility.sanitizeSql(updateKeyValueSql), objectList);
			sum = IntStream.of(rowsImpacted).sum();
			logger.info("No. of rows impacted=" + sum);
		} catch (Exception ex) {
			logger.error("Error occured while updating key value in database", ex);
			throw new DocwbEngineException("Error occured while updating key value in database", ex);
		} finally {
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return sum;
	}

	@Override
	public long addKeyValue(String key, String value) throws DocwbEngineException {
		long startTime = System.nanoTime();
		long keyValueId = -1;
		String userName = environment.getProperty(PROP_NAME_DB_USERNAME);
		try {
			String tempSQL[] = StringUtility.sanitizeSql(addKeyValueSql).toLowerCase()
					.split(DocwbEngineCoreConstants.SQL_DELIMITER_RETURNING);
			String appSQL = tempSQL[0].trim();
			String outParam = tempSQL[1].trim();
			final PreparedStatementCreator psc = new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(appSQL, new String[] { outParam });

					ps.setString(1, key);
					ps.setString(2, value);
					ps.setString(3, userName);
					return ps;
				}
			};
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(psc, keyHolder);
			keyValueId = keyHolder.getKey().longValue();
		} catch (Exception ex) {
			logger.error("Error occured while inserting key value row in database", ex);
			throw new DocwbEngineException("Error occured while inserting key value in database", ex);
		} finally {
		}
		logger.info("Time taken for query (secs):" + (System.nanoTime() - startTime) / 1000000000.0);
		return keyValueId;
	}
}
