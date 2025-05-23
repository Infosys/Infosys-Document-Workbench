/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;

@Component
@Profile("oracle")
public class OracleAttachmentDataAccess extends AttachmentDataAccess implements IAttachmentDataAccess {
	private static final Logger logger = LoggerFactory.getLogger(OracleAttachmentDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	@Value("${addAttachment}")
	private String addAttachmentSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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
					ps.setString(4, String.valueOf(attachmentDbData.isInlineImage()));
					ps.setLong(5, attachmentDbData.getExtractTypeCde());
					ps.setString(6, attachmentDbData.getGroupName());
					ps.setString(7, SessionHelper.getTenantId());
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
}
