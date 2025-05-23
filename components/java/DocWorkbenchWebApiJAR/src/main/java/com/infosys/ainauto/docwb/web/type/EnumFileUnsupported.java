/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.web.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EnumFileUnsupported {

	DOCX("docx"), DOC("doc"), DOCM("docm"), DOT("dot"), DOTM("dotm"), DOTX("dotx"), ODT("odt"), RTF("rtf"), WPS(
			"wps"), XML("xml"), XPS("xps"), EML("eml"), EMLX("emlx"), ICS(
					"ics"), MBOX("mbox"), MSG("msg"), OFT("oft"), OST("ost"), PST("pst"), TNEF("tnef"), VCF("vcf");

	private String propertyValue;

	private EnumFileUnsupported(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}

	public List<String> toList() {
		List<String> valueList = new ArrayList<>();
		Arrays.asList(EnumFileUnsupported.values())
				.forEach(fileType -> valueList.add(fileType.getValue().toLowerCase()));
		return valueList;
	}
}
