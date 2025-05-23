/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class AttributeDbData {

	private long id;
	private long docId;
	private long attachmentId;
	private long attributeId;
	private long docAttrRelId;
	private long attachmentAttrRelId;
	private int attrNameCde;
	private String attrNameTxt;
	private String attrValue;
	private String attrValueOrig;
	private List<AttributeDbData> attributes;
	private int extractTypeCde;
	private String extractTypeTxt;
	private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET;
	private String notification;
	private String endDtm = WorkbenchConstants.END_DTM_UNSET;
	private String createByUserLoginId;
	private String createDtm;
	private String lastModByUserLoginId;
	private String lastModDtm;
	private String createByUserFullName;
	private int createByUserTypeCde;
	private String lastModByUserFullName;
	private int lastModByUserTypeCde;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public void setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public String getAttrValueOrig() {
		return attrValueOrig;
	}

	public void setAttrValueOrig(String attrValueOrig) {
		this.attrValueOrig = attrValueOrig;
	}

	// @JsonGetter("attributes")
	public List<AttributeDbData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDbData> attributes) {
		this.attributes = attributes;
	}

	public int getExtractTypeCde() {
		return extractTypeCde;
	}

	public void setExtractTypeCde(int extractTypeCde) {
		this.extractTypeCde = extractTypeCde;
	}

	public float getConfidencePct() {
		return confidencePct;
	}

	public void setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
	}

	public String getExtractTypeTxt() {
		return extractTypeTxt;
	}

	public void setExtractTypeTxt(String extractTypeTxt) {
		this.extractTypeTxt = extractTypeTxt;
	}

	public String getAttrNameTxt() {
		return attrNameTxt;
	}

	public void setAttrNameTxt(String attrNameTxt) {
		this.attrNameTxt = attrNameTxt;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	public long getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(long attributeId) {
		this.attributeId = attributeId;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getEndDtm() {
		return endDtm;
	}

	public void setEndDtm(String endDtm) {
		this.endDtm = endDtm;
	}

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public String getLastModDtm() {
		return lastModDtm;
	}

	public void setLastModDtm(String lastModDtm) {
		this.lastModDtm = lastModDtm;
	}

	public String getCreateByUserLoginId() {
		return createByUserLoginId;
	}

	public void setCreateByUserLoginId(String createByUserLoginId) {
		this.createByUserLoginId = createByUserLoginId;
	}

	public String getCreateByUserFullName() {
		return createByUserFullName;
	}

	public void setCreateByUserFullName(String createByUserFullName) {
		this.createByUserFullName = createByUserFullName;
	}

	public int getCreateByUserTypeCde() {
		return createByUserTypeCde;
	}

	public void setCreateByUserTypeCde(int createByUserTypeCde) {
		this.createByUserTypeCde = createByUserTypeCde;
	}

	public String getLastModByUserLoginId() {
		return lastModByUserLoginId;
	}

	public void setLastModByUserLoginId(String lastModByUserLoginId) {
		this.lastModByUserLoginId = lastModByUserLoginId;
	}

	public String getLastModByUserFullName() {
		return lastModByUserFullName;
	}

	public void setLastModByUserFullName(String lastModByUserFullName) {
		this.lastModByUserFullName = lastModByUserFullName;
	}

	public int getLastModByUserTypeCde() {
		return lastModByUserTypeCde;
	}

	public void setLastModByUserTypeCde(int lastModByUserTypeCde) {
		this.lastModByUserTypeCde = lastModByUserTypeCde;
	}

	public long getDocAttrRelId() {
		return docAttrRelId;
	}

	public void setDocAttrRelId(long docAttrRelId) {
		this.docAttrRelId = docAttrRelId;
	}

	public long getAttachmentAttrRelId() {
		return attachmentAttrRelId;
	}

	public void setAttachmentAttrRelId(long attachmentAttrRelId) {
		this.attachmentAttrRelId = attachmentAttrRelId;
	}

}
