/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.common;

public class DocWbConstants {

	public static final String API_TIMESTAMP_FORMAT_12HR = "yyyy-MM-dd hh:mm:ss a";
	public static final String API_DATE_FORMAT = "yyyy-MM-dd";

	// File Extensions
	public static final String FILE_EXTENSION_RULE = ".drl";
	public static final String FILE_EXTENSION_JSON = ".json";
	public static final String FILE_EXTENSION_TXT = ".txt";
	public static final String FILE_EXTENSION_HTML = ".html";
	
	public static final String IMAGE_EXTRACTION_REGEX = "<img[^>]*src=[\"']([^\"^']*)";
	public static final String FIND_REPLACE_STRING = "FindReplace((";
	public static final String HTML_TAG_REGEX = "<[/]?html[^>]*>";
	
	// Attribute Name Cde/Txt
	public static final int ATTR_NAME_CDE_CC_ADDRESS_ID = 7;
	public static final int ATTR_NAME_CDE_CONTENT_HTML = 10;
	public static final int ATTR_NAME_CDE_MULTI_ATTRIBUTE = 44;

	// Folder Names
	public static final String ASSETS_FOLDER_TEMPLATE = "templates";
	public static final String ASSETS_FOLDER_IMAGES = "images";
	public static final String RULES_FOLDER_TEMPLATE = "rules/template";
	public static final String RULES_FOLDER_ATTRIBUTE = "rules/attribute";
	public static final String RULES_FOLDER_ACTION = "rules/action";
	public static final String ATTRIBUTE_ATTRIBUTE_MAPPING_FOLDER = "mapping/attribute";
	public static final String ATTRIBUTE_SORTING_KEY_FOLDER = "sorting/attribute";
	
	// File Names
	public static final String RULE_FILE_NAME_RECOMMENDED_ACTION = "RecommendedAction.drl";
	public static final String RULE_FILE_NAME_ATTRIBUTE_NOTIFICATION = "AttributeNotification.drl";
	public static final String RULE_FILE_NAME_RECOMMENDED_TEMPLATE = "RecommendedTemplate.drl";

	public static final String TEMPLATE_INPUT = "Input";
	public static final String TEMPLATE_OUTPUT = "Output";
	public static final String ATTR_VALUE_DELIMITER = ",";
}
