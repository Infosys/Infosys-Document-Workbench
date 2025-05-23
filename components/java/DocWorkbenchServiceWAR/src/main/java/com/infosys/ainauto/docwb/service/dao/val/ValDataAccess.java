/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;

/**
 * 
 * ActionDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */

@Component
public class ValDataAccess implements IValDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(ValDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${getValTableData}")
	private String getValTableDataSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static HashMap<String, List<ValTableDbData>> entityToTableMap = new HashMap<String, List<ValTableDbData>>();

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<ValTableDbData> getValTableData(String entityName) throws WorkbenchException {
		List<ValTableDbData> valTableDataList = new ArrayList<ValTableDbData>();
		if (entityToTableMap.containsKey(entityName)) {
			valTableDataList = entityToTableMap.get(entityName);
		}

		return valTableDataList;
	}

	private List<ValTableDbData> getValTableDataFromDb(String tableName) throws WorkbenchException {
		long startTime = System.nanoTime();
		List<ValTableDbData> valTableDbDataList;
		String sqlQuery = getValTableDataSql.replace("{TABLE_NAME}", tableName);
		try {
			valTableDbDataList = jdbcTemplate.query(StringUtility.sanitizeSql(sqlQuery), new Object[] {},
					new RowMapper<ValTableDbData>() {
						@Override
						public ValTableDbData mapRow(ResultSet rs, int rowNum) throws SQLException {
							ValTableDbData taskStatusValDbData = new ValTableDbData();
							taskStatusValDbData.setCde(rs.getInt(1));
							taskStatusValDbData.setTxt(rs.getString(2));

							taskStatusValDbData.setCreateBy(rs.getString(3));
							taskStatusValDbData.setCreateDtm(rs.getTimestamp(4));
							taskStatusValDbData.setLastModBy(rs.getString(5));
							taskStatusValDbData.setLastModDtm(rs.getTimestamp(6));

							return taskStatusValDbData;
						}
					});
		} catch (Exception ex) {
			logger.error("Error occured while fetching task status val data from database", ex);
			throw new WorkbenchException("Error occured while fetching task status val data from database", ex);
		} finally {

		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for getValTableDataSql query (secs):" + timeElapsed);
		PERF_LOGGER.info("getValTableDataSql" + "," + timeElapsed + ",secs");

		return valTableDbDataList;
	}

	@PostConstruct
	private void loadTables() throws WorkbenchException {

		// In alphabetical order of VAL Tables whose value do not change
		entityToTableMap.put("document-type", getValTableDataFromDb("DOCUMENT_TYPE_VAL"));
		entityToTableMap.put("extract-type", getValTableDataFromDb("EXTRACT_TYPE_VAL"));
		entityToTableMap.put("lock-status", getValTableDataFromDb("LOCK_STATUS_VAL"));
		entityToTableMap.put("task-status", getValTableDataFromDb("TASK_STATUS_VAL"));
		entityToTableMap.put("role-type", getValTableDataFromDb("ROLE_TYPE_VAL"));
		entityToTableMap.put("user-type", getValTableDataFromDb("USER_TYPE_VAL"));

	}
}
