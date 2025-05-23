/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqlUtilTest {


	@Test
	public void testRemoveSqlBlock() {			
		String testSql = "SELECT COL1, /* 1a <BLOCK-ONE> */ COL9, /* </BLOCK-ONE> */ COL2 FROM DOCUMENT DOC;";
		
		String expectedSql = "SELECT COL1, /* 1a  */ COL2 FROM DOCUMENT DOC;";
		String result = SqlUtil.removeSqlBlock(testSql, "<BLOCK-ONE>", "</BLOCK-ONE>");
		System.out.println(result);
		assertTrue(result.equals(expectedSql));
	}
	
	@Test
	public void testRemoveMultilineComments() {			
		String testSql = "SELECT COL1, /* 1a  */ /* 2a  */ COL2 FROM DOCUMENT DOC /* 1b  */ /* 2b  */ WHERE COL1=100";
		String expectedSql = "SELECT COL1,   COL2 FROM DOCUMENT DOC   WHERE COL1=100";
		String result = SqlUtil.removeMultilineComments(testSql);
		System.out.println(result);
		assertTrue(result.equals(expectedSql));
	}

	
	

}
