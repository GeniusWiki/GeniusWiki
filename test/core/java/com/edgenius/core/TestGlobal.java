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
package com.edgenius.core;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestGlobal extends TestCase {
	
	@Test
	public void testResetURLInfo(){
		Global.resetURLInfo("http://www.geniuswiki.com/test");
		Assert.assertEquals("http://",Global.SysHostProtocol);
		Assert.assertEquals("www.geniuswiki.com",Global.SysHostAddress);
		Assert.assertEquals("test",Global.SysContextPath);
		
		Global.resetURLInfo("https://www.geniuswiki.com/");
		Assert.assertEquals("https://",Global.SysHostProtocol);
		Assert.assertEquals("www.geniuswiki.com",Global.SysHostAddress);
		Assert.assertEquals("",Global.SysContextPath);
		
		Global.resetURLInfo("https://www.geniuswiki.com");
		Assert.assertEquals("https://",Global.SysHostProtocol);
		Assert.assertEquals("www.geniuswiki.com",Global.SysHostAddress);
		Assert.assertEquals("",Global.SysContextPath);
		
		Global.resetURLInfo("https://www.geniuswiki.com:8080/test1/test2?test3=1");
		Assert.assertEquals("https://",Global.SysHostProtocol);
		Assert.assertEquals("www.geniuswiki.com:8080",Global.SysHostAddress);
		Assert.assertEquals("test1/test2",Global.SysContextPath);
		
	}
	@After
	public void tearDown(){
		Global.resetURLInfo("http://localhost:8080/");
	}
	

}
