/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerated values for <b>SYSTEM</b> attribute names
 *
 */
public enum EnumSystemAttributeName {
	
	FROM(1, "From"), 
	FROM_ID(20, "FromId"), 
	RECEIVED_DATE (2, "ReceivedDate"), 
	SUBJECT(3, "Subject"), 
	TO_ADDRESS(4, "To Address"), 
	TO_ADDRESS_ID(5, "To Address Id"), 
	CC_ADDRESS(6, "CC Address"), 
	CC_ADDRESS_ID(7, "CC Address Id"), 
	CONTENT(9, "Content"), 
	CONTENT_HTML (10, "ContentHtml"), 
	
	FILE_NAME(30, "File Name"), 
	FILE_METADATA(33, "File Metadata"),
	
	CATEGORY(19, "Category"), 
	DOCUMENT_TYPE(31, "Document Type"),
	SENTIMENT(27, "Sentiment"),
	
	MULTI_ATTRIBUTE(44, "Multi-Attribute"), 
	MULTI_ATTRIBUTE_TABLE(45, "Multi-Attribute Table"), 
	
	CONTENT_ANNOTATION_ANNOTATOR(46, "Content Annotation - AnnotatorJS"),
	CONTENT_ANNOTATION(47, "Content Annotation");
	
	private int cde;
	//private String txt;

	private EnumSystemAttributeName(int cde, String txt) {
		this.cde= cde;
		//this.txt = txt;
	}

	public int getCde() {
		return this.cde;
	}
	
	// Don't implement this method as client code should use cde value
	
//	public String getTxt() {
//		return this.txt;
//	}

	// Reverse Lookup Logic 
	private static final Map<Integer, EnumSystemAttributeName> cdeLookup = new HashMap<>();
	
	static {
		for (EnumSystemAttributeName enumType : EnumSystemAttributeName.values()) {
			cdeLookup.put(enumType.getCde(), enumType);
		}
	}
	
	public static EnumSystemAttributeName get(int cde) {
		return cdeLookup.get(cde);
	}
}
