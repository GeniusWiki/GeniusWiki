/* 
 * =============================================================
 * Copyright (C) 2007-2010 Edgenius (http://www.edgenius.com)
 * =============================================================
 * Edgenius, Confidential and Proprietary
 * License Information: http://www.edgenius.com/licensing/edgenius/1.0/
 *
 * This computer program contains valuable, confidential and proprietary
 * information.  Disclosure, use, or reproduction without the written
 * authorization of Edgenius is prohibited.  This unpublished
 * work by Edgenius is protected by the laws of the United States
 * and other countries.  If publication of the computer program should occur,
 * the following notice shall apply:
 *  
 * Copyright (C) 2007-2010 Edgenius.  All rights reserved.                                                              
 * ****************************************************************
 */
package com.edgenius.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;

import com.edgenius.core.util.ServletUtils;
import com.edgenius.core.util.WebUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional(TransactionMode.COMMIT)
@SpringApplicationContext({"testAplicationContext-database.xml"
	,"com/edgenius/core/applicationContext-cache.xml"
	,"com/edgenius/core/applicationContext-core-orm.xml"
	,"com/edgenius/core/applicationContext-core-service.xml"
	,"com/edgenius/wiki/applicationContext-service.xml"
	,"com/edgenius/wiki/applicationContext-orm.xml"
	,"com/edgenius/wiki/applicationContext-security.xml"
	,"com/edgenius/core/applicationContext-repository.xml"
	})
public class TestT extends UnitilsJUnit4{
	
   @SpringApplicationContext
   private ApplicationContext applicationContext;
   
	@Test
	public void testMock(){
		MockServletContext servletContext = new MockServletContext();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
		ServletUtils.setServletContext(servletContext);
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(WebUtil.getServletContext());
		System.out.println(ctx);
	}
}
