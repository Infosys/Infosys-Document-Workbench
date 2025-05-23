/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.common;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttributeData;

public class AnnotationDataHelper {

	public static <T> List<AnnotationData> getAnnotationsFromAttributes(List<T> attributes,
			List<AnnotationData> annotationDatas) {
		if (ListUtility.hasValue(attributes)) {
			attributes.stream().forEach(attribute -> {
				ExtendedAttributeData attributeData = new ExtendedAttributeData();
				BeanUtils.copyProperties(attribute, attributeData);
				processAttributeForAnnotation(attributeData, annotationDatas);
			});
		}
		return annotationDatas;
	}

	/**
	 * @param attributeData
	 * @param annotationDatas
	 */
	private static void processAttributeForAnnotation(ExtendedAttributeData attributeData,
			List<AnnotationData> annotationDatas) {
		if (ListUtility.hasValue(attributeData.getAttributeDataList())) {
			AnnotationDataHelper.getAnnotationsFromAttributes(attributeData.getAttributeDataList(), annotationDatas);
		} else {
			if (attributeData.getAttrValue() != null) {
				AnnotationData annotationData = new AnnotationData();
				annotationData.setLabel(attributeData.getAttrNameTxt());
				annotationData.setValue(attributeData.getAttrValue());
				annotationData.setOccurrenceNum(attributeData.getOccurrenceNum());
				annotationDatas.add(annotationData);
			}
		}
	}

	/**
	 * This is a convenience class for holding contextual data for attributes
	 *
	 */
	public static class ExtendedAttributeData extends AttributeData {

		private int occurrenceNum;

		/**
		 * @return the occurrenceNum
		 */
		public int getOccurrenceNum() {
			return occurrenceNum;
		}

		/**
		 * @param occurrenceNum
		 *            the occurrenceNum to set
		 */
		public void setOccurrenceNum(int occurrenceNum) {
			this.occurrenceNum = occurrenceNum;
		}

	}

}
