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

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.wiki.security.service.SecurityService;

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
public class TestVolumnPermssion  extends TestCase{
	@Autowired
	SecurityService securityService;
	@Test
	public void testProxyLogin(){
		for(int idx=0;idx<10000;idx++){
			securityService.proxyLogin("admin");
			System.out.println("idx:"+idx);
		}
	}
}
