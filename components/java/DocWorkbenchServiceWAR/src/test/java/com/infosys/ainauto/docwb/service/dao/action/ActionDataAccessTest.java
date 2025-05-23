/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

//package com.infosys.ainauto.docwb.service.dao.action;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import com.infosys.ainauto.docwb.service.common.WorkbenchException;
//import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
//import com.infosys.ainauto.docwb.service.test.ApplicationContextTest;
//
//@WebAppConfiguration
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ApplicationContextTest.class)
//public class ActionDataAccessTest {
//
//	@Autowired
//	IActionDataAccess actionDataAccess;
//
//	 @Test
//	 public final void testGetActionTaskList() throws WorkbenchException {
//	 ActionParamAttrMappingDbData filterData = new ActionParamAttrMappingDbData();
//	 filterData.setDocId(1005394);
//	 filterData.setActionNameCde(101);
//	 filterData.setTaskStatusCde(100);
//	 actionDataAccess.getActionTaskList(filterData, 13, null);
//	 }
//
//	 @Test
//	 public final void testDeleteActionFromDoc() throws WorkbenchException {
//	 actionDataAccess.deleteActionFromDoc(2124L);
//	 }
//
//	 @Test
//	 public final void testGetActionMappingList() throws WorkbenchException {
//	 actionDataAccess.getActionMappingList();
//	 }
//
//	@Test
//	public final void testGetActionData() throws WorkbenchException {
//		 actionDataAccess.getActionData(101, 1005394L);
//	}
//
//}
