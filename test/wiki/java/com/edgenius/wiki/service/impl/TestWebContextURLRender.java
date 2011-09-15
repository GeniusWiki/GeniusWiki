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

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestMain;
import com.edgenius.test.TestUtil;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.impl.RenderContextImpl;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testApplicationContext-webcontext-services.xml"
	,"/com/edgenius/core/applicationContext-cache.xml"
	,"/com/edgenius/core/applicationContext-core-orm.xml"
	,"/com/edgenius/core/applicationContext-core-service.xml"
	,"/com/edgenius/wiki/applicationContext-service.xml"
	,"/com/edgenius/wiki/applicationContext-orm.xml"
	,"/com/edgenius/wiki/applicationContext-security.xml"
	})
public class TestWebContextURLRender extends TestMain{
	@Autowired
	RenderServiceImpl renderService;
	
	@Before
	public void setUp() throws Exception {
	}
	@After
	public void tearDown(){
	}
	@Test
	public void testURLRender(){

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//general URL with web context
		List<RenderPiece> pieces =renderService.renderHTML("[title>link1@test space]");
		String html = renderService.renderNativeHTML(null, null, pieces);
		Assert.assertTrue(TestUtil.equalsHMTL(appendSurroundP("<a href ='https://geniuswiki.com/webcontext/page/test+space/link1'>title</a>"), html));
	
	}
	
	@Test
	public void testImageURLRender(){
		Page page = new Page();
		page.setUid(1);
		Space space = new Space();
		PageContent content = new PageContent();
		page.setSpace(space);
		page.setContent(content);
		
		//NOTE: setUp() method Global setting does not impact below code, as it will initial SettingService, which will
		//load global.xml from file system... So it is the reason why testAplicationContext-services.xml include 
		//a new SetttingService bean which will load global.test.xml...
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//image URL with web context
		List<RenderPiece> pieces =renderService.renderHTML(RenderContext.RENDER_TARGET_PAGE, TestDataConstants.spaceUname1,TestDataConstants.pageUuid1, "!image.jpg|title=title!", null);
		String html = renderService.renderNativeHTML(null, null, pieces);
		Assert.assertTrue(TestUtil.equalsHMTL(appendSurroundP("<img title=\"title\" wajax=\"aname:com.edgenius.wiki.gwt.client.html.ImageModel|filename:image.jpg\" src=\"/webcontext/download?space=test+space&uuid=nuuid1&file=image.jpg&download=false\">")
					, html));
		

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// absolute URL of Image
		space.setUnixName("test space");
		page.setPageUuid("puuid1");
		content.setContent("!image.jpg|title=title!");
		pieces =renderService.renderHTML(RenderContextImpl.RENDER_TARGET_PLAIN_VIEW, page);
		html = renderService.renderNativeHTML(null, null, pieces);
		Assert.assertTrue(TestUtil.equalsHMTL(appendSurroundP("<img title=\"title\" wajax=\"aname:com.edgenius.wiki.gwt.client.html.ImageModel|filename:image.jpg\" src=\"https://geniuswiki.com/webcontext/download?space=test+space&uuid=nuuid1&file=image.jpg&download=false\">")
					, html));
		space.setUnixName("test space");
		page.setPageUuid("puuid1");
		content.setContent("!image.jpg|title=title!");
		

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// export : local file
		pieces =renderService.renderHTML(RenderContextImpl.RENDER_TARGET_EXPORT, page);
		html = renderService.renderNativeHTML(null, null, pieces);
		Assert.assertTrue(TestUtil.equalsHMTL(appendSurroundP("<img title=\"title\" wajax=\"aname:com.edgenius.wiki.gwt.client.html.ImageModel|filename:image.jpg\" src=\"export_files/puuid1/image.jpg\">")
				, html));
	}
}
