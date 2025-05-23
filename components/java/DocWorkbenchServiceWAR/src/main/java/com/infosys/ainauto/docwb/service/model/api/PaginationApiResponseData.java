/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class PaginationApiResponseData<T> extends ApiResponseData<T> {
	private PaginationResData pagination;

	public PaginationResData getPagination() {
		return pagination;
	}

	public void setPagination(PaginationResData pagination) {
		this.pagination = pagination;

	}

}
