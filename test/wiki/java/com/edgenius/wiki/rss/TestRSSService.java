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
package com.edgenius.wiki.rss;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.core.DataRoot;
import com.edgenius.core.util.FileUtil;
import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestMain;
import com.edgenius.test.TestUtil;
import com.edgenius.wiki.model.Page;
import com.sun.syndication.io.FeedException;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/wiki/applicationContext-search.xml"
		,"/com/edgenius/core/activemq-dummy-server.xml"
		,"/com/edgenius/core/applicationContext-mail.xml"
		,"/com/edgenius/core/applicationContext-activemq.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/wiki/applicationContext-orm.xml"
		,"/com/edgenius/wiki/applicationContext-security.xml"
		,"/com/edgenius/wiki/applicationContext-quartz.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/core/applicationContext-core-service.xml"
		})
	
public class TestRSSService extends TestMain{
	@Autowired
	private RSSService rssService;
	
	private Resource rssRoot;
	@Before
	public void setUp(){
		DefaultResourceLoader loader = new DefaultResourceLoader();
		rssRoot  = loader.getResource(FileUtil.getFullPath(DataRoot.getDataRoot(),"data", "rss"));

		rssService.cleanAllRss();
	}
	@After
	public void tearup(){
		rssService.cleanAllRss();
	}
	@Test
	public void testFeed() throws FeedException, IOException{
		rssService.createFeed(TestDataConstants.spaceUname1);
		
		File out = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),TestDataConstants.spaceUid1 + ".xml"));
		Assert.assertTrue(out.exists());
		
		//test get page items from RSS 
		List<Page> pages = rssService.getPagesFromFeed(TestDataConstants.spaceUid1, TestDataConstants.spaceUname1, TestUtil.getAdminUser());
		Assert.assertEquals(3, pages.size());
		
		//Test remove feed items
		Assert.assertTrue(rssService.removeFeedItem(TestDataConstants.spaceUname1, TestDataConstants.pageUuid2));
		
		//Test  
		rssService.removeFeed(TestDataConstants.spaceUid1);
		Assert.assertFalse(out.exists());
	}
	
	@Test
	public void testRemoveAllFeed() throws FeedException, IOException{
		rssService.createFeed(TestDataConstants.spaceUname1);
		rssService.createFeed(TestDataConstants.spaceUname2);
		
		File out = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),TestDataConstants.spaceUid1 + ".xml"));
		Assert.assertTrue(out.exists());
		
		File out2 = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),TestDataConstants.spaceUid2 + ".xml"));
		Assert.assertTrue(out2.exists());
		
		rssService.cleanAllRss();
		Assert.assertFalse(out.exists());
		Assert.assertFalse(out2.exists());
	}
}
