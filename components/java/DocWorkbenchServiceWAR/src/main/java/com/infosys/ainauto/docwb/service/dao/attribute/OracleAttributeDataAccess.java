/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attribute;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

/**
 * 
 * ActionDataAccess class deals with database and provides us
 *         with information regarding the keywords and assignment group/active
 *         flag associated with it.
 *
 */

@Component
@Profile("oracle")
public class OracleAttributeDataAccess extends AttributeDataAccess implements IAttributeDataAccess {

	private static final Logger logger = LoggerFactory.getLogger(OracleAttributeDataAccess.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");

	private static final String DELETE_ATTR_REL_SQL = "DELETE_ATTR_REL_CALLABLE";
	private static final String DELETE_ATTRIBUTE_SQL = "DELETE_ATTRIBUTE_CALLABLE";

	@Value("${deleteDocAttributeRel}")
	private String deleteDocAttributeRelSql;

	@Value("${deleteAttachAttributeRel}")
	private String deleteAttachAttributeRelSql;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	
	public AttributeDbData deleteAttribute(AttributeDbData attributeDbData) throws WorkbenchException {
		return updateEndDateRel(attributeDbData);
	}	

	private AttributeDbData updateEndDateRel(AttributeDbData attributeDbData) throws WorkbenchException {
		long startTime = System.nanoTime();
		AttributeDbData resultAttributeDbData = new AttributeDbData();
		try {
			long attrRelId = 0;
			long attributeId = 0;
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(DELETE_ATTR_REL_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_ATTR_REL_ID", attributeDbData.getId());
			mapParam.addValue("I_DOC_ID", attributeDbData.getDocId());
			mapParam.addValue("I_ATTACHMENT_ID", attributeDbData.getAttachmentId());
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			
			attrRelId = Long.valueOf((String) sqlResponse.get("O_ATTR_REL_ID"));
			attributeId = Long.valueOf((String) sqlResponse.get("O_ATTRIBUTE_ID"));
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
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(DELETE_ATTRIBUTE_SQL);

			MapSqlParameterSource mapParam = new MapSqlParameterSource();
			mapParam.addValue("I_LAST_MOD_BY", SessionHelper.getLoginUsername());
			mapParam.addValue("I_ATTRIBUTE_ID", attributeDbData.getId());
			mapParam.addValue("I_TENANT_ID", SessionHelper.getTenantId());
			
			Map<String, Object> sqlResponse = jdbcCall.execute((SqlParameterSource) mapParam);
			
			long attributeId = Long.valueOf((String) sqlResponse.get("O_ATTRIBUTE_ID"));
			String createBy = (String) sqlResponse.get("O_CREATE_BY");
			if (attributeId > 0 ) {
				resultAttributeDbData.setAttributeId(attributeId);
				resultAttributeDbData.setCreateByUserLoginId(createBy);
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

}
