/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.process.annotation;

import java.util.List;

import com.infosys.ainauto.docwb.service.model.process.AnnotationData;

public class ExportIOBData extends AnnotationData {

	public class RangeData {
		private String start;
		private long startOffset;
		private String end;
		private long endOffset;

		public String getStart() {
			return start;
		}

		public void setStart(String start) {
			this.start = start;
		}

		public long getStartOffset() {
			return startOffset;
		}

		public void setStartOffset(long startOffset) {
			this.startOffset = startOffset;
		}

		public String getEnd() {
			return end;
		}

		public void setEnd(String end) {
			this.end = end;
		}

		public long getEndOffset() {
			return endOffset;
		}

		public void setEndOffset(long endOffset) {
			this.endOffset = endOffset;
		}
	}

	private List<RangeData> ranges;

	public List<RangeData> getRanges() {
		return ranges;
	}

	public void setRanges(List<RangeData> ranges) {
		this.ranges = ranges;
	}

}
