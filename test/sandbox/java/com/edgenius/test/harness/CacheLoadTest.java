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
package com.edgenius.test.harness;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.core.model.User;
import com.edgenius.core.service.impl.CacheServiceImpl;
import com.edgenius.core.service.impl.UserReadingServiceImpl;
import com.edgenius.test.TestMain;
import com.edgenius.wiki.security.service.SecurityServiceImpl;
import com.edgenius.wiki.service.impl.SpaceServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
	,"/com/edgenius/core/applicationContext-cache.xml"
	,"/com/edgenius/core/applicationContext-core-orm.xml"
	,"/com/edgenius/core/applicationContext-core-service.xml"
	,"/com/edgenius/wiki/applicationContext-service.xml"
	,"/com/edgenius/wiki/applicationContext-orm.xml"
	,"/com/edgenius/wiki/applicationContext-security.xml"
	})
public class CacheLoadTest  extends TestMain{
	@Autowired
	UserReadingServiceImpl userReadingService;
	@Autowired
	SpaceServiceImpl spaceService;
	@Autowired
	SecurityServiceImpl securityService;
	@Autowired
	CacheServiceImpl cacheService;
	
	@Test
	public void testUserCache(){
		List<User> all = userReadingService.getUsers(0, 0, User.SORT_BY_USERNAME+"", null, false);
		System.out.println("All user" + all.size());
		int count = 0;
		for (User user : all) {
			userReadingService.getUserByName(user.getUsername());
			System.out.println("Read user count :" + (++count));
		}
		System.out.println("done");
	}
//	@Test
//	public void testPolicyCache(){
//		User admin = TestUtil.getAdminUser();
//		admin = userReadingService.getUserByName(admin.getUsername());
//		List<Space> all = spaceService.getSpaces(admin, 0,0,null,null,false);
//		System.out.println("All Spaces " + all.size());
//		
//		
//		int count = 0;
//		for (Space space : all) {
//			securityService.fillSpaceWikiOperations(admin, space);
//			cacheService.resetUserCache();
//			System.out.println("Fill space operation:" + space.getUnixName() + " Count:"+ (++count));
//		}
//		System.out.println("done");
//	}

	
}
