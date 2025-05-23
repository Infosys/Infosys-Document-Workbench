/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.attachment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.FileUtil;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.dao.attachment.IAttachmentDataAccess;
import com.infosys.ainauto.docwb.service.dao.attribute.IAttributeDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AttachmentResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttaAttaRelReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttaAttaRelResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttachmentResData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

@Component
public class AttachmentProcess implements IAttachmentProcess {

	@Autowired
	private IAttachmentDataAccess attachmentDataAccess;

	@Autowired
	private IAttributeDataAccess attributeDataAccess;

	@Value("${attachmentFilePath}")
	private String attachmentFilePath;

	private final static String FILENAME_DELIMITER = "|";

	private List<InsertAttachmentResData> addAttachment(AttachmentReqData attachmentRequestData)
			throws WorkbenchException {
		long attachmentId = 0;
		List<InsertAttachmentResData> attachmentResList = new ArrayList<>();
		List<MultipartFile> multipartFileList = attachmentRequestData.getMultipartFileList();
		String groupName = attachmentRequestData.getGroupName();
		for (int i = 0; i < multipartFileList.size(); i++) {
			if (multipartFileList.get(i) != null) {
				if (!StringUtility.hasValue(attachmentRequestData.getGroupName())) {
					groupName = getAttachmentGroupName();
				}
				InsertAttachmentResData insertAttachmentResData = new InsertAttachmentResData();
				String fileName = multipartFileList.get(i).getOriginalFilename().replace(FILENAME_DELIMITER,
						WorkbenchConstants.FILE_SEPARATOR);
				String storedFileName = FileUtility.generateUniqueFileName(fileName);
				String storedFileFullPath = FileUtility.getConcatenatedName(attachmentFilePath, storedFileName);
				boolean fileSaveStatus = FileUtil.saveMultipartFile(multipartFileList.get(i), storedFileFullPath);
				if (!fileSaveStatus) {
					throw new WorkbenchException("Error while saving file to location");
				}
				AttachmentDbData attachmentDbData = new AttachmentDbData();
				attachmentDbData.setPhysicalName(storedFileName);
				attachmentDbData.setLogicalName(fileName);
				attachmentDbData.setInlineImage(attachmentRequestData.isInlineImage());
				attachmentDbData.setPrimary(attachmentRequestData.isPrimary());
				attachmentDbData.setSequenceNum(WorkbenchConstants.SEQUENCE_NUM);
				if (multipartFileList.size() != attachmentRequestData.getExtractTypeCdeList().size()) {
					attachmentDbData.setExtractTypeCde(attachmentRequestData.getExtractTypeCdeList().get(0));
				} else {
					attachmentDbData.setExtractTypeCde(attachmentRequestData.getExtractTypeCdeList().get(i));
				}
				attachmentDbData.setGroupName(groupName);
				attachmentId = attachmentDataAccess.addAttachment(attachmentDbData);
				insertAttachmentResData.setAttachmentId(attachmentId);
				insertAttachmentResData.setGroupName(groupName);
				attachmentResList.add(insertAttachmentResData);
			}
		}
		return attachmentResList;
	}

	@Override
	public List<InsertAttachmentResData> addAttachmentToDoc(AttachmentReqData attachmentRequestData, long docId)
			throws WorkbenchException {
		if (attachmentRequestData.getMultipartFileList().size() > 1 && attachmentRequestData.getMultipartFileList()
				.size() == attachmentRequestData.getExtractTypeCdeList().size()) {
			attachmentRequestData.setGroupName(getAttachmentGroupName());
		}
		List<InsertAttachmentResData> attachmentResDataList = addAttachment(attachmentRequestData);
		List<Long> attachmentIdList = attachmentResDataList.stream().map(InsertAttachmentResData::getAttachmentId)
				.collect(Collectors.toList());
		if (ListUtility.hasValue(attachmentIdList)) {
			List<Long> docAttachmentIdList = attachmentDataAccess.addDocAttachmentRel(attachmentIdList, docId);
			if (ListUtility.hasValue(docAttachmentIdList) && (docAttachmentIdList.size() == attachmentIdList.size())) {
				for (int i = 0; i < docAttachmentIdList.size(); i++) {
					attachmentResDataList.get(i).setDocAttachmentRelId(docAttachmentIdList.get(i));
				}
			}
		}
		return attachmentResDataList;
	}

	@Override
	public int addAttachmentToEmail(AttachmentReqData attachmentRequestData, long outboundEmailId)
			throws WorkbenchException {
		List<InsertAttachmentResData> attachmentResDataList = addAttachment(attachmentRequestData);
		List<Long> attachmentIdList = attachmentResDataList.stream().map(InsertAttachmentResData::getAttachmentId)
				.collect(Collectors.toList());
		int count = 0;
		if (attachmentIdList != null && attachmentIdList.size() > 0) {
			count = (int) attachmentDataAccess.addEmailOutboundAttachmentRel(attachmentIdList, outboundEmailId);
		}
		return count;
	}

	@Override
	public List<AttachmentResData> getDocAttachmentList(long docId) throws WorkbenchException {
		List<AttachmentResData> attachmentResponseDataList = new ArrayList<>();
		List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getDocAttachmentList(docId);
		List<AttributeDbData> attributeDbDataList = attributeDataAccess.getAttachmentAttributes(docId, "");
		Map<Long, List<AttributeDbData>> attachmentAttrMap = new HashMap<>();
		if (ListUtility.hasValue(attributeDbDataList)) {
			attachmentAttrMap = attributeDbDataList.stream()
					.collect(Collectors.groupingBy(AttributeDbData::getAttachmentId, Collectors.toList()));
		}
		attachmentDbDataList.sort(Comparator.comparing(AttachmentDbData::getAttachmentId));
		for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
			if (!attachmentDbData.isInlineImage()) {
				AttachmentResData attachmentResponseData = new AttachmentResData();
				attachmentResponseData.setAttachmentId(attachmentDbData.getAttachmentId());
				attachmentResponseData.setFileName(attachmentDbData.getLogicalName());
				attachmentResponseData.setPhysicalName(attachmentDbData.getPhysicalName());
				attachmentResponseData.setInlineImage(attachmentDbData.isInlineImage());
				attachmentResponseData.setExtractTypeCde(attachmentDbData.getExtractTypeCde());
				attachmentResponseData.setGroupName(attachmentDbData.getGroupName());
				List<AttributeDbData> attributes = new ArrayList<>();
				if (attachmentAttrMap != null && !attachmentAttrMap.isEmpty()
						&& attachmentAttrMap.containsKey(attachmentDbData.getAttachmentId())) {
					attributes = attachmentAttrMap.get(attachmentDbData.getAttachmentId()).stream().filter(
							attribute -> attribute.getAttrNameCde() == EnumSystemAttributeName.FILE_METADATA.getCde())
							.collect(Collectors.toList());

					attachmentResponseData.setAttributes(attributes);
				}
				attachmentResponseDataList.add(attachmentResponseData);
			}

		}
		sortAttachmentDataList(attachmentResponseDataList);
		return attachmentResponseDataList;
	}

	@Override
	public AttachmentDbData getDocAttachmentFile(long docId, long attachmentId) throws WorkbenchException {
		List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getDocAttachmentList(docId);
		Optional<AttachmentDbData> attachmentDbDataOptional = attachmentDbDataList.stream()
				.filter(a -> a.getAttachmentId() == attachmentId).findFirst();
		if (attachmentDbDataOptional != null) {
			AttachmentDbData attachmentDbData = attachmentDbDataOptional.get();
			if (!attachmentDbData.isInlineImage()) {
				String storedFileFullPath = FileUtility.getConcatenatedName(attachmentFilePath,
						attachmentDbData.getPhysicalName());
				attachmentDbData.setPhysicalPath(storedFileFullPath);
				return attachmentDbData;
			}
		}

		return null;
	}

	@Override
	public AttachmentDbData getOutboudAttachmentFile(long emailOutboundId, long attachmentId)
			throws WorkbenchException {
		List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getAttachmentListEmail(emailOutboundId);
		Optional<AttachmentDbData> attachmentDbDataOptional = attachmentDbDataList.stream()
				.filter(a -> a.getAttachmentId() == attachmentId).findFirst();
		AttachmentDbData attachmentDbData = attachmentDbDataOptional.get();
		String storedFileFullPath = FileUtility.getConcatenatedName(attachmentFilePath,
				attachmentDbData.getPhysicalName());
		attachmentDbData.setPhysicalPath(storedFileFullPath);
		return attachmentDbData;

	}

	@Override
	public List<AttachmentResData> getAttachmentListEmail(long emailOutboundId) throws WorkbenchException {
		List<AttachmentResData> attachmentResponseDataList = new ArrayList<>();
		List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getAttachmentListEmail(emailOutboundId);
		for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
			AttachmentResData attachmentResponseData = new AttachmentResData();
			attachmentResponseData.setAttachmentId(attachmentDbData.getAttachmentId());
			attachmentResponseData.setFileName(attachmentDbData.getLogicalName());
			attachmentResponseData.setInlineImage(attachmentDbData.isInlineImage());
			attachmentResponseData.setExtractTypeCde(attachmentDbData.getExtractTypeCde());
			attachmentResponseData.setGroupName(attachmentDbData.getGroupName());
			attachmentResponseDataList.add(attachmentResponseData);

		}
		return attachmentResponseDataList;
	}

	/**
	 * @return
	 */
	private String getAttachmentGroupName() {
		return "AG-" + StringUtility.getUniqueString();
	}

	/**
	 * Method will perform custom sort for attachmentDataList based on fileName,
	 * level of hierarchy.
	 * 
	 * @return
	 */
	private void sortAttachmentDataList(List<AttachmentResData> attachmentDataList) {
		if (ListUtility.hasValue(attachmentDataList)) {
			final class SortAttachmentData extends AttachmentResData {
				private int hierarchy;
				private String originalFileName;

				public int getHierarchy() {
					return hierarchy;
				}

				public void setHierarchy(int hierarchy) {
					this.hierarchy = hierarchy;
				}

				public String getOriginalFileName() {
					return originalFileName;
				}

				public void setOriginalFileName(String originalFileName) {
					this.originalFileName = originalFileName;
				}

			}

			List<SortAttachmentData> tempAttachmentDataList = new ArrayList<>();
			for (AttachmentResData attachmentData : attachmentDataList) {
				SortAttachmentData tempAttachmentData = new SortAttachmentData();
				BeanUtils.copyProperties(attachmentData, tempAttachmentData);
				String[] fileNameParts = attachmentData.getFileName()
						.split(Pattern.quote(WorkbenchConstants.FILE_SEPARATOR));
				int hierarchy = fileNameParts.length - 1;
				tempAttachmentData.setHierarchy(hierarchy);
				tempAttachmentData.setOriginalFileName(fileNameParts[hierarchy]);
				tempAttachmentDataList.add(tempAttachmentData);
			}

			Comparator<SortAttachmentData> comparator = Comparator.comparing(SortAttachmentData::getOriginalFileName)
					.thenComparing(Comparator.comparing(SortAttachmentData::getHierarchy))
					.thenComparing(Comparator.comparing(SortAttachmentData::getFileName));
			List<SortAttachmentData> sortedAttachmentDataList = tempAttachmentDataList.stream().sorted(comparator)
					.collect(Collectors.toList());

			for (AttachmentResData attachmentData : attachmentDataList) {
				for (int i = 0; i < sortedAttachmentDataList.size(); i++) {
					if (attachmentData.getAttachmentId() == sortedAttachmentDataList.get(i).getAttachmentId()) {
						attachmentData.setSortOrder(i);
						break;
					}
				}
			}
		}
	}
	@Override
	public List<InsertAttaAttaRelResData> addAttaAttaRel(AttaAttaRelReqData attachmentRelReqData)
			throws WorkbenchException {
		List<InsertAttaAttaRelResData> insertAttaAttaRelResDataList =new ArrayList<>();
		InsertAttaAttaRelResData insertAttaAttaRelResData=new InsertAttaAttaRelResData();
		
		long attaAttaRelId = attachmentDataAccess.addAttaAttaRel(attachmentRelReqData);
		insertAttaAttaRelResData.setAttaAttaRelId(attaAttaRelId);
		insertAttaAttaRelResData.setAttachmentId1(attachmentRelReqData.getAttachmentId1());
		insertAttaAttaRelResData.setAttachmentId2(attachmentRelReqData.getAttachmentId2());
		insertAttaAttaRelResData.setDocId(attachmentRelReqData.getDocId());	
		insertAttaAttaRelResData.setAttaRelTypeCde(attachmentRelReqData.getAttaRelTypeCde());
		insertAttaAttaRelResDataList.add(insertAttaAttaRelResData);
		
		return insertAttaAttaRelResDataList;
	}
	@Override
	public long countAttaAttaDocExist(AttaAttaRelReqData attachmentRelReqData)throws WorkbenchException  {
		long docAttaRelCount=attachmentDataAccess.countAttaAttaDocExist(attachmentRelReqData);
		return docAttaRelCount;
	}
	@Override
	public long countAttaAttaRelRecord(AttaAttaRelReqData attachmentRelReqData)throws WorkbenchException{
		long attaAttaRelCount=attachmentDataAccess.countAttaAttaRelRecord(attachmentRelReqData);
		return attaAttaRelCount;
	}
}
