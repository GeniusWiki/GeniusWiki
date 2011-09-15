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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringApplicationContext;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.ServletUtils;
import com.edgenius.test.TestMain;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

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
public class TestVolumnPageService extends TestMain{
	
	private String content;
	private List<String> spaces;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private SpaceService spaceService;
	
	@Autowired
	private UserReadingService userReadingService;

	
	@Before
	public void setUp() throws IOException {
		
		System.out.println("Load test file from URL:"+this.getClass().getClassLoader().getResource("testcase/pageservice/samplepagecontent.txt"));
		URL samplePage = this.getClass().getClassLoader().getResource("testcase/pageservice/samplepagecontent.txt");
		content = FileUtils.readFileToString(new File(samplePage.getPath()));
		
		System.out.println("Load test file from URL:"+this.getClass().getClassLoader().getResource("testcase/pageservice/spacelist.txt"));
		URL spaceList = this.getClass().getClassLoader().getResource("testcase/pageservice/spacelist.txt");
		spaces = FileUtils.readLines(new File(spaceList.getPath()));
		
		
		HttpServletRequest request = new MockHttpServletRequest(){

			public String getRemoteUser() {
				return "admin";
			}
			
		};
		ServletUtils.setRequest(request);
//		MockServletContext servletContext = new MockServletContext();
//		WebApplicationContext springContext;
//		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE	, springContext);
//		ServletUtils.setServletContext(servletContext);
		
		Authentication authentication = new UsernamePasswordAuthenticationToken("admin","admin");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		//create spaces
		for (String uname : spaces) {
			Space space = new Space();
			space.setUnixName(uname);
			space.setName("Title:" + uname);
			space.setDescription("Description:" + uname);
			WikiUtil.setTouchedInfo(userReadingService, space);
			try {
				spaceService.createSpace(space);
			} catch (SpaceException e) {
				e.printStackTrace();
				Assert.fail(e.toString());
			}
		}
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void testSaveVolumePage(){
		//save 15,000 pages into test database
		final int maxPages = 1500;
//		final int maxPages = 15;
		final int maxLevel = 7;
		final int spaceLen = spaces.size();
		final int maxSpacePages = (int) maxPages/spaceLen + (maxPages%spaceLen != 0?1:0);
		
		String title = "First page";
		String lastTitle ="http://geniuswiki.com";
		int level = maxLevel+1; //first page won't have parent
		Page parentPage = null;
		int spaceIndex = 0;
		
		for(int idx=0;idx<maxPages;idx++){
			
			if(idx >= (maxSpacePages * (spaceIndex+1))){
				//another space pages, need reset some value
				level = maxLevel + 1;
				parentPage = null;
				title = "First page";
				lastTitle ="http://geniuswiki.com";
				spaceIndex++;
			}
			
			Page page = new Page();
			Space space = new Space();
			PageContent pct = new PageContent();
			
			space.setUnixName(spaces.get(spaceIndex));
			
			page.setSpace(space);
			page.setContent(pct);
			page.setTitle(title);
			
			String my = content.replaceAll("@PAGE_TITLE@", lastTitle);
			pct.setContent(my);
			if(level < maxLevel){
				page.setParent(parentPage);
				level++;
			}else{
				//root page - no parent
				level = 0;
			}
			try {
				parentPage = pageService.savePage(page, WikiConstants.NOTIFY_NONE, true);
				//just get a random string
				title = parentPage.getPageUuid();
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}

		}
		
	}

}
