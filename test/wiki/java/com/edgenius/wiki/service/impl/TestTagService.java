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
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.test.TestDataConstants;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.service.TagService;

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
public class TestTagService{
	
	@Autowired
	private TagService tagService;
	@Autowired
	private PageTagDAO pageTagDAO;
	
	@Autowired
	private PageDAO pageDAO;
	
	@Before
	public void setUp() throws Exception {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin","admin");
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	@After
	public void tearup(){
	}
	@Test
	public void testSaveUpdatePageTag2() throws PageException, SpaceNotFoundException{
		
		Page page = pageDAO.getByUuid(TestDataConstants.pageUuid1);
		tagService.saveUpdatePageTag(page, "tag1,tag2");
		pageDAO.saveOrUpdate(page);
		
		page = pageDAO.getByUuid(TestDataConstants.pageUuid2);
		tagService.saveUpdatePageTag(page, "tag1");
		pageDAO.saveOrUpdate(page);
		
		Map<String, Integer> map = tagService.getPageTagsNameList(TestDataConstants.spaceUname1);
		Assert.assertEquals(1, map.get("tag2").intValue());
		Assert.assertEquals(2, map.get("tag1").intValue());
		
	}
	@Test
	public void testSaveUpdatePageTag3() throws PageException, SpaceNotFoundException{
		
		Page page = pageDAO.getByUuid(TestDataConstants.pageUuid1);
		tagService.saveUpdatePageTag(page, "tag1 tag2 tag3");
		pageDAO.saveOrUpdate(page);

		
		Map<String, Integer> map = tagService.getPageTagsNameList(TestDataConstants.spaceUname1);
		Assert.assertEquals(1, map.get("tag3").intValue());
		Assert.assertEquals(1, map.get("tag2").intValue());
		Assert.assertEquals(1, map.get("tag1").intValue());
		
		tagService.saveUpdatePageTag(page, "tag1,tag2");
		pageDAO.saveOrUpdate(page);
		
		map = tagService.getPageTagsNameList(TestDataConstants.spaceUname1);
		Assert.assertEquals(0, map.get("tag3").intValue());
		Assert.assertEquals(1, map.get("tag2").intValue());
		Assert.assertEquals(1, map.get("tag1").intValue());
		
		tagService.saveUpdatePageTag(page, "tag1");
		pageDAO.saveOrUpdate(page);
		
		map = tagService.getPageTagsNameList(TestDataConstants.spaceUname1);
		Assert.assertEquals(0, map.get("tag3").intValue());
		Assert.assertEquals(0, map.get("tag2").intValue());
		Assert.assertEquals(1, map.get("tag1").intValue());
		
	}
	@After
	public void tearDown(){
		clean();
	}
	private void clean(){
		Page page = pageDAO.getByUuid(TestDataConstants.pageUuid1);
		tagService.saveUpdatePageTag(page, "");
		pageDAO.saveOrUpdate(page);
		
		page = pageDAO.getByUuid(TestDataConstants.pageUuid2);
		tagService.saveUpdatePageTag(page, "");
		pageDAO.saveOrUpdate(page);
		
		List<PageTag> tags = pageTagDAO.getObjects();
		if(tags != null){
			for (PageTag pageTag : tags) {
				pageTagDAO.removeObject(pageTag);
			}
		}
	}
}
