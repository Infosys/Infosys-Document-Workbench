
/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.type;

public enum EnumFileSupported {

	HTML("html"), PDF("pdf"), JPG("jpg"), JPEG("jpeg"), PNG("png"), BMP("bmp"), TXT("txt"), MP4("mp4"), GIF("gif");

	private String propertyValue;

	private EnumFileSupported(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
