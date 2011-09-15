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
package com.edgenius.wiki.render.macro;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestTableGroupProcessor extends TestCase{

	@Test
	public void testGetGroupKey(){
		//case one - standard 3x2 table
		TableGroupProcessor prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableCellMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableRowMacro(), 50, 55);
		prc.adoptChild(new TableCellMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-0-0-3",prc.getGroupKey(10));
		Assert.assertEquals("0-0-1-3",prc.getGroupKey(20));
		Assert.assertEquals("0-0-2-3",prc.getGroupKey(30));
		Assert.assertEquals("0-1",prc.getGroupKey(50));
		Assert.assertEquals("0-1-0-3",prc.getGroupKey(60));
		Assert.assertEquals("0-1-1-3",prc.getGroupKey(70));
		Assert.assertEquals("0-1-2-3",prc.getGroupKey(80));
		
		//case one - first row is 4, second is 3
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableCellMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableCellMacro(), 40, 45);
		prc.adoptChild(new TableRowMacro(), 50, 55);
		prc.adoptChild(new TableCellMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-0-0-4",prc.getGroupKey(10));
		Assert.assertEquals("0-0-1-4",prc.getGroupKey(20));
		Assert.assertEquals("0-0-2-4",prc.getGroupKey(30));
		Assert.assertEquals("0-0-3-4",prc.getGroupKey(40));
		Assert.assertEquals("0-1",prc.getGroupKey(50));
		Assert.assertEquals("0-1-0-3",prc.getGroupKey(60));
		Assert.assertEquals("0-1-1-3",prc.getGroupKey(70));
		Assert.assertEquals("0-1-2-3",prc.getGroupKey(80));
		
		//case one - first row is 3, second is 4
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableCellMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableRowMacro(), 50, 55);
		prc.adoptChild(new TableCellMacro(), 40, 45);
		prc.adoptChild(new TableCellMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-0-0-3",prc.getGroupKey(10));
		Assert.assertEquals("0-0-1-3",prc.getGroupKey(20));
		Assert.assertEquals("0-0-2-3",prc.getGroupKey(30));
		Assert.assertEquals("0-1",prc.getGroupKey(50));
		Assert.assertEquals("0-1-0-4",prc.getGroupKey(40));
		Assert.assertEquals("0-1-1-4",prc.getGroupKey(60));
		Assert.assertEquals("0-1-2-4",prc.getGroupKey(70));
		Assert.assertEquals("0-1-3-4",prc.getGroupKey(80));
		
		//case one - 3*3
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableCellMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableRowMacro(), 50, 55);
		prc.adoptChild(new TableCellMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		prc.adoptChild(new TableRowMacro(), 100, 105);
		prc.adoptChild(new TableCellMacro(), 110, 115);
		prc.adoptChild(new TableCellMacro(), 120, 125);
		prc.adoptChild(new TableCellMacro(), 130, 135);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-0-0-3",prc.getGroupKey(10));
		Assert.assertEquals("0-0-1-3",prc.getGroupKey(20));
		Assert.assertEquals("0-0-2-3",prc.getGroupKey(30));
		Assert.assertEquals("0-1",prc.getGroupKey(50));
		Assert.assertEquals("0-1-0-3",prc.getGroupKey(60));
		Assert.assertEquals("0-1-1-3",prc.getGroupKey(70));
		Assert.assertEquals("0-1-2-3",prc.getGroupKey(80));
		Assert.assertEquals("0-2",prc.getGroupKey(100));
		Assert.assertEquals("0-2-0-3",prc.getGroupKey(110));
		Assert.assertEquals("0-2-1-3",prc.getGroupKey(120));
		Assert.assertEquals("0-2-2-3",prc.getGroupKey(130));
		
		//case one - 2 row only
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableRowMacro(), 50, 55);
		prc.adoptChild(new TableRowMacro(), 60, 65);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-1",prc.getGroupKey(50));
		Assert.assertEquals("0-2",prc.getGroupKey(60));
	
		
		//case one - table then row
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableRowMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableRowMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		prc.adoptChild(new TableRowMacro(), 100, 105);
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-1",prc.getGroupKey(10));
		Assert.assertEquals("0-1-0-2",prc.getGroupKey(20));
		Assert.assertEquals("0-1-1-2",prc.getGroupKey(30));
		Assert.assertEquals("0-2",prc.getGroupKey(60));
		Assert.assertEquals("0-2-0-2",prc.getGroupKey(70));
		Assert.assertEquals("0-2-1-2",prc.getGroupKey(80));
		Assert.assertEquals("0-3",prc.getGroupKey(100));
		
		//case one - one row 7
		prc = new TableGroupProcessor(new TableMacro(), 0, 0);
		prc.adoptChild(new TableCellMacro(), 10, 15);
		prc.adoptChild(new TableCellMacro(), 20, 25);
		prc.adoptChild(new TableCellMacro(), 30, 35);
		prc.adoptChild(new TableCellMacro(), 40, 45);
		prc.adoptChild(new TableCellMacro(), 60, 65);
		prc.adoptChild(new TableCellMacro(), 70, 75);
		prc.adoptChild(new TableCellMacro(), 80, 85);
		
		Assert.assertEquals("0",prc.getGroupKey(0));
		Assert.assertEquals("0-0-0-7",prc.getGroupKey(10));
		Assert.assertEquals("0-0-1-7",prc.getGroupKey(20));
		Assert.assertEquals("0-0-2-7",prc.getGroupKey(30));
		Assert.assertEquals("0-0-3-7",prc.getGroupKey(40));
		Assert.assertEquals("0-0-4-7",prc.getGroupKey(60));
		Assert.assertEquals("0-0-5-7",prc.getGroupKey(70));
		Assert.assertEquals("0-0-6-7",prc.getGroupKey(80));
	}
}
