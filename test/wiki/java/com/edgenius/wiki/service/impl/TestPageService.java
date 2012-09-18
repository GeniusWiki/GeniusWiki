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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestMain;
import com.edgenius.test.TestUtil;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.hibernate.PageProgressDAOHibernate;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.DraftContent;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityServiceImpl;
import com.edgenius.wiki.service.CommentException;
import com.opensymphony.xwork2.interceptor.annotations.After;

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
@Transactional		
public class TestPageService   extends TestMain{
	@Autowired
	PageServiceImpl pageService;
	@Autowired
	CommentServiceImpl commentService;
	@Autowired
	SecurityServiceImpl securityService;
	@Autowired
	PageProgressDAOHibernate pageProgressDAO; 
	@Before
	public void setup(){
		securityService.proxyLogin("admin");
	}
	@After 
	public void tearDown(){
		securityService.proxyLogout();
	}
	
	@Test
	public void testPageSaveRemove(){
		String spaceUname = TestDataConstants.spaceUname1;
		String pageTitle = "testsave1"; 
		Page page = new Page();
		page.setTitle(pageTitle);
		Space space = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		PageContent content = new PageContent();
		content.setContent("testsave1 content");
		page.setContent(content);
		PageProgress progress = new PageProgress();
		progress.setLinkExtInfo("testinfo");
		page.setPageProgress(progress);
		
		Page retPage = null;
		try {
			retPage = pageService.savePage(page, WikiConstants.NOTIFY_NONE, false);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		String pageUuid = retPage.getPageUuid();
		
		PageComment comment = new PageComment();
		comment.setBody("comment1");
		comment.setLevel(0);
		try {
			commentService.createComment(spaceUname, pageUuid, comment, WikiConstants.NOTIFY_NONE);
		} catch (CommentException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
		//confirm page saved
		retPage = pageService.getPage(retPage.getUid());
		Assert.assertNotNull(retPage);
		Assert.assertEquals(pageTitle, retPage.getTitle());
		
		//confirm comment saved
		int commentUid = 0;
		try {
			List<PageComment> comments = commentService.getPageComments(spaceUname, pageUuid);
			Assert.assertEquals(1, comments.size());
			commentUid = comments.get(0).getUid();
		} catch (CommentException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
		
		//save a new copy and create a history
		page.setPageUuid(pageUuid);
		page.setTitle(pageTitle+"ver2");
		page.setVersion(1);
		try {
			retPage = pageService.savePage(page, WikiConstants.NOTIFY_NONE, false);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		//confirm new page version saved.
		retPage = pageService.getPage(retPage.getUid());
		Assert.assertNotNull(retPage);
		Assert.assertEquals(pageTitle+"ver2", retPage.getTitle());
		
		//confirm PageProgress saved
		progress = pageProgressDAO.get(retPage.getPageProgress().getUid());
		Assert.assertNotNull(progress);
		Assert.assertEquals("testinfo", progress.getLinkExtInfo());
		
		//confirm history saved
		List<History> histories = pageService.getHistoryPages(spaceUname, pageUuid, 0, -1, null);
		Assert.assertEquals(1, histories.size());
		
		//remove page
		retPage = pageService.getPage(retPage.getUid());
		pageService.removePage(spaceUname, pageUuid, false, true);
		
		//confirm pagePrpogress removed
		progress = pageProgressDAO.get(retPage.getPageProgress().getUid());
		Assert.assertNull(progress);
		
		//confirm history removed as well.
		histories = pageService.getHistoryPages(spaceUname, pageUuid, 0, -1, null);
		Assert.assertEquals(0, histories.size());
		
		//confirm page removed
		retPage = pageService.getPage(retPage.getUid());
		Assert.assertNull(retPage);
		
		//confirm comment removed
		PageComment retComm = commentService.getComment(commentUid);
		Assert.assertNull(retComm);
		
	}
	@Test
	public void testDraftSaveRemove(){
		String spaceUname = TestDataConstants.spaceUname1;
		String draftTitle = "testdraft1"; 
		Draft draft = new Draft();
		draft.setTitle(draftTitle);
		Space space = new Space();
		space.setUnixName(spaceUname);
		draft.setSpace(space);
		DraftContent content = new DraftContent();
		content.setContent("testdraftsave1 content");
		draft.setContent(content);
		PageProgress progress = new PageProgress();
		progress.setLinkExtInfo("testdraftinfo");
		draft.setPageProgress(progress);
		try {
			draft = pageService.saveDraft(TestUtil.getAdminUser(), draft, PageType.AUTO_DRAFT);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
		draft = pageService.getDraft(TestUtil.getAdminUser(),draft.getUid());
		Assert.assertNotNull(draft);
		Assert.assertEquals(draftTitle, draft.getTitle());
		
		pageService.removeDraft(TestUtil.getAdminUser(),spaceUname, draft.getPageUuid(), PageType.AUTO_DRAFT);
		draft = pageService.getDraft(TestUtil.getAdminUser(),draft.getUid());
		Assert.assertNull(draft);
		
		
	}
}
 