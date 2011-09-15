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
package com.edgenius.wiki.render.impl;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.test.TestItem;
import com.edgenius.test.TestMain;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.render.LinkReplacerEngine;
import com.edgenius.wiki.render.RenderContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"})
public class TestLinkReplacerEngineImpl extends TestMain{
	
	@Autowired
	private LinkReplacerEngine linkReplacerEngine;
	
	@Test
	public void testRenderTitleChangeInSameSpace() throws IOException{
		RenderContext context = new RenderContextImpl();
		LinkReplacer replacer = new LinkReplacer();
		context.putGlobalParam(LinkReplacer.class.getName(), replacer);
		replacer.setType(WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK);
		replacer.setFromSpaceUname("source");
		replacer.setToSpaceUname("source");
		replacer.setNewTitle("new title");
		replacer.setOldTitle("old world");
		
		List<TestItem> cases = readTestcaseFile("LinkReplacer-Title-Same");
		
		for (TestItem testItem : cases) {
			
			String output = linkReplacerEngine.render(testItem.input, context);
			Assert.assertEquals(testItem.expected, output);
		}
	}
	@Test
	public void testRenderTitleChangeInDiffSpace() throws IOException{
		RenderContext context = new RenderContextImpl();
		LinkReplacer replacer = new LinkReplacer();
		context.putGlobalParam(LinkReplacer.class.getName(), replacer);
		replacer.setType(WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK);
		replacer.setFromSpaceUname("source");
		replacer.setToSpaceUname("target");
		replacer.setNewTitle("new title");
		replacer.setOldTitle("old world");
		
		List<TestItem> cases = readTestcaseFile("LinkReplacer-Title-Diff");
		
		for (TestItem testItem : cases) {
			
			String output = linkReplacerEngine.render(testItem.input, context);
			Assert.assertEquals(testItem.expected, output);
		}
	}

	@Test
	public void testRenderCopyPage() throws IOException{
		RenderContext context = new RenderContextImpl();
		LinkReplacer replacer = new LinkReplacer();
		context.putGlobalParam(LinkReplacer.class.getName(), replacer);
		replacer.setType(WikiConstants.AUTO_FIX_COPY_LINK);
		replacer.setFromSpaceUname("source");
		replacer.setToSpaceUname("target");
		replacer.setNewTitle("new title");
		replacer.setOldTitle("old world");
		
		List<TestItem> cases = readTestcaseFile("LinkReplacer-Copy");
		
		for (TestItem testItem : cases) {
			
			String output = linkReplacerEngine.render(testItem.input, context);
			Assert.assertEquals(testItem.expected, output);
		}
	}
}
