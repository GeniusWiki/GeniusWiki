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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;

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
public class TestSpaceService {

	@Autowired
	private SpaceService spaceService;
	
	@Before
	public void setUp(){
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin","admin");
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	@Test
	public void testCRUDSpace() throws SpaceException{
		Space space = new Space();
		space.setUnixName("New space");
		space.setName("New space title");
		space.setDescription("New space desc");
		
		//create
		spaceService.createSpace(space);
		
		///get
		Space newSpace = spaceService.getSpaceByUname("New space");
		Assert.assertEquals(space.getUnixName(), newSpace.getUnixName());
		Assert.assertEquals(space.getName(), newSpace.getName());
		Assert.assertEquals(space.getDescription(), newSpace.getDescription());
		
		space.setName("Update - New space title");
		space.setName("Update - New space desc");
		spaceService.updateSpace(space, true);
		///get
		newSpace = spaceService.getSpaceByUname("New space");
		Assert.assertEquals(space.getUnixName(), newSpace.getUnixName());
		Assert.assertEquals(space.getName(), newSpace.getName());
		Assert.assertEquals(space.getDescription(), newSpace.getDescription());
		
		//remove
		spaceService.removeSpace("New space", true);
		newSpace = spaceService.getSpaceByUname("New space");
		Assert.assertNull(newSpace);
		
	}
}
