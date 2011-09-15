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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestRegionComparator extends TestCase{
	
	@Test
	public void testComparator(){
		Region r0 = new Region(8,12);
		Region r2 = new Region(4 ,5);
		Region r1 = new Region(3 ,6);
		Region r3 = new Region(15 ,20);
		Region r4 = new Region(15 ,19);
		Region r5 = new Region(16 ,19);
		
		
		Set<Region> set = new TreeSet<Region>(new RegionComparator());
		set.add(r0);
		set.add(r4);
		set.add(r1);
		set.add(r5);
		set.add(r3);
		set.add(r2);

		List<Region> list = new ArrayList<Region>(set);
		Assert.assertEquals(new Region(16, 19), list.get(0));
		Assert.assertEquals(new Region(15, 19), list.get(1));
		Assert.assertEquals(new Region(15, 20), list.get(2));
		Assert.assertEquals(new Region(8, 12), list.get(3));
		Assert.assertEquals(new Region(4, 5), list.get(4));
		Assert.assertEquals(new Region(3, 6), list.get(5));
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// second test
		r0 = new Region(5,10);
		r1 = new Region(0 ,11);
		
		
		set = new TreeSet<Region>(new RegionComparator());
		set.add(r0);
		set.add(r1);
		
		list = new ArrayList<Region>(set);
		Assert.assertEquals(new Region(5,10), list.get(0));
		Assert.assertEquals(new Region(0 ,11), list.get(1));
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// third test
		r0 = new Region(5,10);
		r1 = new Region(0 ,11);
		
		
		set = new TreeSet<Region>(new RegionComparator());
		set.add(r1);
		set.add(r0);
		
		list = new ArrayList<Region>(set);
		Assert.assertEquals(new Region(5,10), list.get(0));
		Assert.assertEquals(new Region(0 ,11), list.get(1));
		
	}

}
