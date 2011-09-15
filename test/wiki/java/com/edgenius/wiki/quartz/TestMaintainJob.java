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

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.wiki.ActivityType;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.service.ActivityLogService;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/core/applicationContext-core-service.xml"
		,"/com/edgenius/wiki/applicationContext-security.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/wiki/applicationContext-orm.xml"
		,"/com/edgenius/wiki/applicationContext-quartz.xml"
})
//@TransactionConfiguration(defaultRollback = false)
public class TestMaintainJob{

	private MaintainJob job = new MaintainJob();
	@Autowired
	private ActivityLogService activityLog; 
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Before
	public void setUp() throws Exception{
		
		ActivityLog activity = new ActivityLog();
		activity.setType(ActivityType.Type.SYSTEM_EVENT.getCode());
		activity.setSubType(ActivityType.SubType.VERSION_PING.getCode());
		activity.setTgtResourceType(100);
		activity.setTgtResourceName("TestTgt");
		activity.setCreatedDate(DateUtils.addDays(new Date(), -400));
		activityLog.save(activity);
		
		activity = new ActivityLog();
		activity.setType(ActivityType.Type.SYSTEM_EVENT.getCode());
		activity.setSubType(ActivityType.SubType.VERSION_PING.getCode());
		activity.setTgtResourceType(100);
		activity.setTgtResourceName("TestTgt");
		activity.setCreatedDate(DateUtils.addDays(new Date(), -300));
		activityLog.save(activity);
	
		job.setApplicationContext(applicationContext);
	}
	@After
	public void tearDown() throws Exception{
		activityLog.purgeActivityLog(0);
	}
	@Test
	public void testJob() throws JobExecutionException{
		List<ActivityLog> log = activityLog.getByTarget(ActivityType.Type.SYSTEM_EVENT.getCode(), ActivityType.SubType.VERSION_PING.getCode(), 100 , "TestTgt");
		Assert.assertEquals(2, log.size());
		
		job.executeInternal(null);
		
		log = activityLog.getByTarget(ActivityType.Type.SYSTEM_EVENT.getCode(),ActivityType.SubType.VERSION_PING.getCode(), 100, "TestTgt");
		Assert.assertEquals(1, log.size());
	}
}
