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
package com.edgenius.wiki.render;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestRegion  extends TestCase{
	
	@Test
	public void testOverlap(){
		Region r1 = new Region(8, 15);
		Assert.assertFalse(r1.isOverlap(new Region(5, 7)));
		Assert.assertFalse(r1.isOverlap(new Region(16, 20)));
		Assert.assertFalse(r1.isOverlap(new Region(7, 16)));
		Assert.assertFalse(r1.isOverlap(new Region(9, 14)));
		
		Assert.assertFalse(r1.isOverlap(new Region(8, 15)));
		Assert.assertFalse(r1.isOverlap(new Region(2, 8)));
		Assert.assertFalse(r1.isOverlap(new Region(15, 20)));
		
		Assert.assertTrue(r1.isOverlap(new Region(6, 9)));
		Assert.assertTrue(r1.isOverlap(new Region(9, 20)));
		Assert.assertTrue(r1.isOverlap(new Region(14, 20)));
		
		
	}

}
