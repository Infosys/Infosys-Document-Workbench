/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

public class SqlUtil {

	public static String removeSqlBlock(String sql, String sqlBlockStartTag, String sqlBlockEndTag) {
		return SqlUtil.replaceSqlBlock(sql, "", sqlBlockStartTag, sqlBlockEndTag);
	}
	
	public static String replaceSqlBlock(String sql, String replaceWith, String sqlBlockStartTag, String sqlBlockEndTag) {
		if (sql == null) {
			return sql;
		}
		return sql.replaceAll(sqlBlockStartTag + "(.*?)" + sqlBlockEndTag, replaceWith);
	}

	public static String removeMultilineComments(String sql) {
		if (sql == null) {
			return sql;
		}
		return sql.replaceAll("(?s)/\\*.*?\\*/", "");
	}
}