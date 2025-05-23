/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.controller.template;


import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.infosys.ainauto.docwb.rules.controller.api.template.TemplateController;
import com.infosys.ainauto.docwb.rules.model.api.template.GetRecommendedTemplateReqData;
import com.infosys.ainauto.docwb.rules.process.template.ITemplateProcess;
import com.infosys.ainauto.docwb.rules.process.template.TemplateProcess;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TemplateControllerTest {

	@TestConfiguration
	static class TestContextConfiguration {

		@Bean
		public TemplateController templateController() {
			return new TemplateController();
		}

		@Bean
		public ITemplateProcess templateProcess() {
			return new TemplateProcess();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TemplateControllerTest.class);
	private static GetRecommendedTemplateReqData getRecommendedTemplateReqData;

	@SuppressWarnings("serial")
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		getRecommendedTemplateReqData = new GetRecommendedTemplateReqData();
		getRecommendedTemplateReqData.setDocId(123);
		GetRecommendedTemplateReqData.ActionData actionData = new GetRecommendedTemplateReqData.ActionData();
		actionData.setActionNameCde(8);
		actionData.setActionNameTxt("Get Order Status");
		actionData.setActionResult("OrderStatus=received|TrackingNumber=941160291291134|DeliveryDate=12/13/2018");
		getRecommendedTemplateReqData.setActionDataList(new ArrayList<GetRecommendedTemplateReqData.ActionData>() {
			{
				add(actionData);
			}
		});
		GetRecommendedTemplateReqData.AttributeData attributeReqData = new GetRecommendedTemplateReqData.AttributeData();
		attributeReqData.setAttrNameCde(19);
		attributeReqData.setAttrNameTxt("Category");
		attributeReqData.setAttrValue("order-status");
		attributeReqData.setConfidencePct(78);
		attributeReqData.setExtractTypeCde(2);
		attributeReqData.setExtractTypeTxt("Custom Logic");
		getRecommendedTemplateReqData.setAttributes(new ArrayList<GetRecommendedTemplateReqData.AttributeData>() {
			{
				add(attributeReqData);
			}
		});
	}

	@Test
	public void testCondition2() throws Exception {
		getTemplateName("OrderStatus=shipped|TrackingNumber=941160291291134|DeliveryDate=12/13/2018");
	}

	static String getTemplateName(String text) {
		logger.info("Entering getTemplateName() with string={}", text);
		if (text == null || text.length() == 0) {
			return "";
		}
		String[] tokens = text.split("\\|");
		logger.info("tokens={}", tokens[0]);
		String result = "none";
		String orderStatus = tokens[0].split("=")[1];
		logger.info("orderStatus= {}", orderStatus);
		if (orderStatus.equals("shipped")) {
			result = "OrderStatusShippedTemplate";
		} else if (orderStatus.equals("received")) {
			result = "OrderStatusReceivedTemplate";
		}
		return result;
	}

	@AfterClass
	public static void tearDown() throws Exception {
		getTemplateName("OrderStatus=shipped|TrackingNumber=941160291291134|DeliveryDate=12/13/2018");
	}
}
