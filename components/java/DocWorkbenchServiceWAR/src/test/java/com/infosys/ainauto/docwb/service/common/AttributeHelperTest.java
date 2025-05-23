/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AttributeHelperTest {

	private static final Logger logger = LoggerFactory.getLogger(AttributeHelperTest.class);

	@Test
	public void testJsonStringConversion() {
		AttributeDbData multiAttrData = new AttributeDbData();
		List<AttributeDbData> attributeDbDataList = new ArrayList<>();
		List<String> attrNames = new ArrayList<>();
		List<String> attrValues = new ArrayList<>();
		attrNames.add("Name");
		attrValues.add("John Doe");
		attrNames.add("Passport Number");
		attrValues.add("H833802");
		attrNames.add("DOB");
		attrValues.add("12/9/1970");
		attrNames.add("Expiry Date");
		attrValues.add("6/24/2019");
		int i = 0;
		while (i < attrNames.size()) {
			AttributeDbData data = new AttributeDbData();
			data.setAttrNameTxt(attrNames.get(i));
			data.setAttrValue(attrValues.get(i));
			data.setConfidencePct(90);
			attributeDbDataList.add(data);
			i++;
		}
		multiAttrData.setAttrNameTxt("passport-group");
		multiAttrData.setAttributes(attributeDbDataList);
		String text = AttributeHelper.convertMultiAttrToJsonString(multiAttrData);
		logger.debug(text);
		multiAttrData = AttributeHelper.convertJsonStringToMultiAttr(text);
		multiAttrData.getAttributes().forEach(x -> logger.debug(x.getAttrValue()));
		assertTrue(multiAttrData.getAttributes().size() == i);
		
	}
	
	
	@Test
	public void testJsoup1() {
		String strTemp = "SKETCH NUMBER 1<S 16003";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
	}
	
	@Test
	public void testJsoup2() {
		String strTemp = "<div class=\"WordSection1\"><p class=\"MsoNormal\"><span>Hi,</span><span><br><br><span>I wanted some help on my <b>new card</b> hence have called customer care but they could not do. The number is in the<span>attachment</span>. Please help me in this regards.</span><br><br><span>Thanks,</span><br><span>John Doe</span></span></p></div>";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
	}
	
	
	@Test
	public void testJsoup3() {
		String strTemp = "Hi, I wanted some help on my new card hence have called customer care but they could not do. The number is in theattachment. Please help me in this regards. Thanks, John Doe";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
		
	}
	
	
	@Test
	public void testJsoup4() {	
		String strTemp = "10<SKETCH<1 <b>Hi There</b>";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
	}
	
	@Test
	public void testJsoup5() {		
		String strTemp = "SKETCH<10, SKETCH>1";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
	}
	
	@Test
	public void testJsoup6() {			
		String strTemp = "<p><,Hi</p>";
		String json = jsoupMethod(strTemp);
		assertEquals(strTemp.replaceAll("\\s+",""), json.replaceAll("\\s+",""));	
	}

	
	private String jsoupMethod(String strTemp) {
		OutputSettings settings = new OutputSettings();
		ArrayList<Integer> openIdxList = new ArrayList<Integer>();
		int commaIdx = -1;
		for(int i=0; i<strTemp.length(); i++) {
			if(strTemp.charAt(i) == ',')
				commaIdx = i;
			else if(strTemp.charAt(i) == '<')
				openIdxList.add(i);
			else if(strTemp.charAt(i) == '>')
				if(openIdxList.size()>0 && openIdxList.get(openIdxList.size()-1) > commaIdx)
					openIdxList.remove(openIdxList.size()-1);
		}
		StringBuilder strTemp1 = new StringBuilder(strTemp);
		int asciiValue = 6;
		for(Integer i: openIdxList) {
			strTemp1.setCharAt(i, (char)asciiValue);
		}
		
		String json = Jsoup
				.clean(strTemp1.toString(), "http://",
						Whitelist.relaxed().removeAttributes("img", "alt").preserveRelativeLinks(true)
								.addAttributes(":all", "class"), settings)
				.replaceAll("\n", "").replaceAll("\\\"\\\\&quot;", "\\\\\"").replaceAll("\\\\&quot;\\\"", "\\\\\"");
		// Replace &amp; (HTML character) back to & (text character)		
		json = json.replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll(Character.toString((char) asciiValue), "<");
		return json;
	}

}
