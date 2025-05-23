/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attribute;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class AttributeNameResData {
	public static class AttributeNameValueData {
		private String attrValue;
		private List<AttributeData> attributes;

		public String getAttrValue() {
			return attrValue;
		}

		public void setAttrValue(String attrValue) {
			this.attrValue = attrValue;
		}

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	public static class RegExpData {
		private String pattern;
		private String flag;

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		public String getFlag() {
			return flag;
		}

		public void setFlag(String flag) {
			this.flag = flag;
		}
	}

	public static class TabularAttributeData {
		private List<RegExpData> orderColumnUsingAnyOfRegExp;
		private List<AttributeData> attributes;

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}

		public List<RegExpData> getOrderColumnUsingAnyOfRegExp() {
			return orderColumnUsingAnyOfRegExp;
		}

		public void setOrderColumnUsingAnyOfRegExp(List<RegExpData> orderColumnUsingAnyOfRegExp) {
			this.orderColumnUsingAnyOfRegExp = orderColumnUsingAnyOfRegExp;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public static class AttributeData {
		private int attrNameCde;
		private String attrNameTxt;
		private List<AttributeData> nonTabular;
		private List<TabularAttributeData> tabular;

		public int getAttrNameCde() {
			return attrNameCde;
		}

		public void setAttrNameCde(int attrNameCde) {
			this.attrNameCde = attrNameCde;
		}

		public String getAttrNameTxt() {
			return attrNameTxt;
		}

		public void setAttrNameTxt(String attrNameTxt) {
			this.attrNameTxt = attrNameTxt;
		}

		public List<AttributeData> getNonTabular() {
			return nonTabular;
		}

		public void setNonTabular(List<AttributeData> nonTabular) {
			this.nonTabular = nonTabular;
		}

		public List<TabularAttributeData> getTabular() {
			return tabular;
		}

		public void setTabular(List<TabularAttributeData> tabular) {
			this.tabular = tabular;
		}

	}

	private int attrNameCde;
	private String attrNameTxt;
	private List<AttributeNameValueData> attrNameValues;

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public void setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
	}

	public String getAttrNameTxt() {
		return attrNameTxt;
	}

	public void setAttrNameTxt(String attrNameTxt) {
		this.attrNameTxt = attrNameTxt;
	}

	public List<AttributeNameValueData> getAttrNameValues() {
		return attrNameValues;
	}

	public void setAttrNameValues(List<AttributeNameValueData> attrNameValues) {
		this.attrNameValues = attrNameValues;
	}
}
