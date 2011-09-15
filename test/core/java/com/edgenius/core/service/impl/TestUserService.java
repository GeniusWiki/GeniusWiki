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
package com.edgenius.core.service.impl;

import static com.edgenius.test.TestDataConstants.username1;
import static com.edgenius.test.TestDataConstants.username2;
import static com.edgenius.test.TestDataConstants.username_admin;
import static com.edgenius.test.TestDataConstants.username_demo;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserService;
import com.opensymphony.xwork2.interceptor.annotations.After;
/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/core/applicationContext-core-service.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/wiki/applicationContext-orm.xml"
		,"/com/edgenius/wiki/applicationContext-security.xml"
})
@TransactionConfiguration(defaultRollback = false)
@Transactional
public class TestUserService  {
	@Autowired
	UserService userService;
	
	@Autowired
	UserReadingService userReadingService;
	
	@Before
	public void setup(){
		
	}
	@After 
	public void tearDown(){
		
	}
	
	@Test
	public void testFollow(){
		
		User u1 = userReadingService.getUserByName(username1);
		User u2 = userReadingService.getUserByName(username2);
		User admin = userReadingService.getUserByName(username_admin);
		User u4 = userReadingService.getUserByName(username_demo);
		
		userService.follow(admin,u1);
		userService.follow(admin,u2);
		
		userReadingService.getUser(admin.getUid());
		
		List<User> following = admin.getFollowings();
		Assert.assertEquals(2,following.size());
		Assert.assertEquals(u1,following.get(0));
		Assert.assertEquals(u2,following.get(1));
		
		//unfollow one
		userService.unfollow(admin,u2);
		Assert.assertEquals(1,following.size());
		Assert.assertEquals(u1,following.get(0));
		//unfollow not exist one
		userService.unfollow(admin,u4);
		Assert.assertEquals(1,following.size());
		Assert.assertEquals(u1,following.get(0));
		
		//follow another
		userService.follow(admin,u4);
		
		userReadingService.getUser(admin.getUid());
		following = admin.getFollowings();
		Assert.assertEquals(2,following.size());
		Assert.assertEquals(u4,following.get(1));

		//follwers
		u4 = userReadingService.getUser(u4.getUid());
		List<User> followers =  u4.getFollowers();
		Assert.assertEquals(1,followers.size());
		Assert.assertEquals(admin,followers.get(0));
		
		//unfollow all
		userService.unfollow(admin,u4);
		userService.unfollow(admin,u1);
		Assert.assertEquals(0,following.size());
	}
}
