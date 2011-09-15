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
package com.edgenius.wiki.service.impl;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestItem;
import com.edgenius.test.TestMain;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.render.RenderContext;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
	,"/com/edgenius/core/applicationContext-cache.xml"
	,"/com/edgenius/core/applicationContext-core-orm.xml"
	,"/com/edgenius/core/applicationContext-core-service.xml"
	,"/com/edgenius/wiki/applicationContext-service.xml"
	,"/com/edgenius/wiki/applicationContext-orm.xml"
	,"/com/edgenius/wiki/applicationContext-security.xml"
	})
public class TestRenderServicePerf  extends TestMain{
	@Autowired
	RenderServiceImpl renderService;
	
	@Test
	public void testRichConversion() throws IOException{
		List<TestItem> cases = readTestcaseFile("large-markup");
		
		for (TestItem item : cases) {
			long start = System.currentTimeMillis();
			List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,item.expected, null);
			String html = renderService.renderRichHTML(TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,pieces);
			System.out.println(html);
			
			System.out.println("-------------------------------------------------");
			System.out.println("Run:" + (System.currentTimeMillis() - start));
			System.out.println("-------------------------------------------------");
			
		}
	}
	
}
