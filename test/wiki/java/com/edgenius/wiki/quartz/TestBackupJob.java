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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


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
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/ext/applicationContext-ext-orm.xml"
		,"/com/edgenius/ext/applicationContext-ext-service.xml"
})
public class TestBackupJob{

	private BackupJob job = new BackupJob();
	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception{
		job.setApplicationContext(applicationContext);
	}
	@Test
	public void testJob() throws JobExecutionException{
		job.executeInternal(null);
	}
	
}
