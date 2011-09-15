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
package com.edgenius.wiki.gwt.client.server.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestStringUtil extends TestCase{
	
	@Test
	public void testTrimStartSpace(){
		Assert.assertEquals("abc  ",StringUtil.trimStartSpace("abc  "));
		Assert.assertEquals("abc",StringUtil.trimStartSpace("abc"));
		Assert.assertEquals("abc  ",StringUtil.trimStartSpace("  abc  "));
	}

	@Test
	public void testShrinkSpaces(){
		Assert.assertEquals("ab c",StringUtil.shrinkSpaces("ab c  "));
		Assert.assertEquals("ab c",StringUtil.shrinkSpaces("ab   c  "));
		Assert.assertEquals("a b c",StringUtil.shrinkSpaces("a b   c  "));
		Assert.assertEquals("a b 12 c",StringUtil.shrinkSpaces(" a b 12  c  "));
		Assert.assertEquals("a bd 12c",StringUtil.shrinkSpaces(" a    bd  12c  "));
	}
}
