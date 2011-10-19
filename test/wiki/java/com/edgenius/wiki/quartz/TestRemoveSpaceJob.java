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
package com.edgenius.wiki.quartz;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

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
public class TestRemoveSpaceJob {

	private RemoveSpaceJob job = new RemoveSpaceJob();
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private SpaceService spaceService;
	@Autowired
	private UserReadingService userReadingService;

	private JobExecutionContext context;
	private JobDetail jobDetail;
	
	@Before
	public void setUp() throws Exception{
		job.setApplicationContext(applicationContext);

		context = EasyMock.createMock(JobExecutionContext.class);
		jobDetail = EasyMock.createMock(JobDetail.class);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin","admin");
		SecurityContextHolder.getContext().setAuthentication(auth);
		//create
		Space space = new Space();
		space.setUnixName("New space");
		space.setName("New space title");
		space.setDescription("New space desc");
		WikiUtil.setTouchedInfo(userReadingService, space);
		spaceService.createSpace(space);
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	@After
	public void tearDown() throws Exception{
		//if test failed, here clear data
		if(spaceService.getSpaceByUname("New space") != null){
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin","admin");
			SecurityContextHolder.getContext().setAuthentication(auth);
			spaceService.removeSpace("New space", true);
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}
	@Test
	public void testJob() throws JobExecutionException{
		Assert.assertNotNull(spaceService.getSpaceByUname("New space"));
		
		JobDataMap map = new JobDataMap();
		map.put(WikiConstants.ATTR_SPACE_UNAME, "New space");
		EasyMock.expect(context.getJobDetail()).andReturn(jobDetail);
		EasyMock.expect(jobDetail.getJobDataMap()).andReturn(map);
		EasyMock.replay(jobDetail);
		EasyMock.replay(context);
		
		job.executeInternal(context);
		
		
		Assert.assertNull(spaceService.getSpaceByUname("New space"));
	}

}
